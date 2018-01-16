package net.ncguy.assets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.ui.detachable.IPanel;
import net.ncguy.utils.AssetUtils;
import net.ncguy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AssetTable extends VisTable implements IPanel {

    VisScrollPane scroller;
    VisTable content;

    List<String> loadedAssets;

    Map<String, AssetRow> rowMap;

    SimpleObjectProperty<String> hoveredAsset;
    NinePatch patch;
    NinePatch fullPatch;
    NinePatch thirdPatch;

    public AssetTable() {
        super(false);
        loadedAssets = new ArrayList<>();
        rowMap = new HashMap<>();
        hoveredAsset = new SimpleObjectProperty<>();

        patch = new NinePatch(new Texture(Gdx.files.internal("textures/backgroundThird.png")), 1, 1, 1, 1);
        thirdPatch = new NinePatch(new Texture(Gdx.files.internal("textures/backgroundHalf.png")), 1, 1, 1, 1);
        fullPatch = new NinePatch(new Texture(Gdx.files.internal("textures/backgroundFull.png")), 1, 1, 1, 1);

        Init();
    }

    public void UpdateAssetList() {
        AssetUtils.instance().UsingManager(manager -> {
            Array<String> assetNames = manager.getAssetNames();

            if(loadedAssets.size() == assetNames.size) return;
            loadedAssets.clear();
            rowMap.clear();
            assetNames.forEach(loadedAssets::add);
            loadedAssets.sort(String::compareTo);
            RebuildList(manager);
        });
    }

    public void RebuildList(AssetManager manager) {
        content.clear();
        loadedAssets
                .forEach(assetName -> {
                    Class assetType = manager.getAssetType(assetName);
                    boolean loaded = manager.isLoaded(assetName, assetType);
                    int referenceCount = manager.getReferenceCount(assetName);
                    AddRow(manager, assetName, assetType, loaded, referenceCount);
                });

        String last = loadedAssets.get(0);
        AssetRow assetRow = rowMap.get(last);
        NinePatchDrawable backgroundThird = new NinePatchDrawable(thirdPatch);
        assetRow.All(row -> {
            if(row.isEndRow()) return;
            Actor actor = row.getActor();
            if(actor instanceof Label)
                ((Label) actor).getStyle().background = backgroundThird;
        });
    }

    public void AddRow(AssetManager manager, String assetName, Class assetType, boolean loaded, int refCount) {

        AssetRow row = new AssetRow();

        row.assetName = assetName;

        row.name = content.add(StringUtils.TruncateString(assetName, 120));
        row.type = content.add(assetType.getSimpleName());
        row.loaded = content.add(loaded ? "Loaded" : "Pending");
        row.count = content.add(String.valueOf(refCount));
        content.row();

        NinePatchDrawable background = new NinePatchDrawable(patch);
        NinePatchDrawable backgroundFull = new NinePatchDrawable(fullPatch);

        row.All(cell -> {
            Actor actor = cell.getActor();
            if(actor instanceof Label) {
//                ((Label) actor).setFillParent(true);

                actor.setBounds(0, 0, cell.getPrefWidth(), cell.getPrefHeight());

                Label.LabelStyle curStyle = ((Label) actor).getStyle();
                Label.LabelStyle style = new Label.LabelStyle();

                style.background = cell.isEndRow() ? backgroundFull : background;
                style.font = curStyle.font;
                style.fontColor = curStyle.fontColor;

                ((Label) actor).setStyle(style);
            }
        });

        Array<String> dependencies = manager.getDependencies(assetName);
        if(dependencies != null && dependencies.size > 0)
            dependencies.forEach(row::AddDependency);

        row.EnsureIntegrity();
        rowMap.put(assetName, row);
    }

    AssetRow GetRow(float y) {
        int row = content.getRow(y);
        if (row < 0 || row >= loadedAssets.size())
            return null;
        return rowMap.get(loadedAssets.get(row));
    }

    void AssetHovered(float x, float y) {
        int row = content.getRow(y);
        String s = null;
        if(row >= 0 && row < loadedAssets.size())
            s = loadedAssets.get(row);
        hoveredAsset.set(s);
    }

    void AssetLeft() {
        hoveredAsset.set(null);
    }

    @Override
    public void InitUI() {
        content = new VisTable(false);
        scroller = new VisScrollPane(content);

        content.columnDefaults(0).width(128);
        content.columnDefaults(1).width(64);
        content.columnDefaults(2).width(64);
        content.columnDefaults(3).width(16);
    }

    public void ActRow(AssetRow row, Supplier<Action> action) {
        row.AddAction(action);
    }

    @Override
    public void AttachListeners() {

        hoveredAsset.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null) {
                AssetRow oldRow = rowMap.get(oldValue);
                ActRow(oldRow, () -> Actions.color(Color.WHITE, .15f));
                oldRow.AllDependencies(rowMap, cell -> cell.getActor().addAction(Actions.color(Color.WHITE, .15f)));
            }

            if(newValue != null) {
                AssetRow newRow = rowMap.get(newValue);
                ActRow(newRow, () -> Actions.color(Color.RED, .15f));
                newRow.AllDependencies(rowMap, cell -> cell.getActor().addAction(Actions.color(Color.YELLOW, .15f)));
            }

        });

        content.setTouchable(Touchable.enabled);

        scroller.addListener(new InputListener() {

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                AssetLeft();
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                Vector2 vector2 = scroller.localToDescendantCoordinates(content, new Vector2(x, y));
                AssetHovered(vector2.x, vector2.y);
                event.stop();
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(button == Input.Buttons.RIGHT) {
                    Vector2 vector2 = scroller.localToDescendantCoordinates(content, new Vector2(x, y));
                    OpenContextMenu(event.getStageX(), event.getStageY(), vector2.y);
                    event.handle();
                    event.stop();
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    PopupMenu menu;
    public void OpenContextMenu(float screenX, float screenY, float localY) {

        if(menu == null) {
            menu = new PopupMenu();

            menu.addItem(new MenuItem("Unload asset", new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    AssetRow row = GetRow(localY);
                    if(row == null) return;
                    AssetUtils.instance().UsingManager(manager -> Unload(manager, row));
                }
            }));
        }

        menu.showMenu(getStage(), screenX, screenY);
    }

    public void Unload(AssetManager manager, AssetRow row) {
        row.AllDependencyRows(rowMap, r -> Unload(manager, r));
        manager.unload(row.assetName);
    }

    @Override
    public void Assemble() {
        add(scroller).grow();
    }

    @Override
    public void Style() {

    }

    @Override
    public String GetTitle() {
        return "Loaded Assets";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        UpdateAssetList();

//        NinePatchDrawable background = new NinePatchDrawable(patch);

//        setBackground("grey");
        for (AssetRow row : rowMap.values()) {
            row.All(cell -> {
                Actor actor = cell.getActor();
                if(actor instanceof Label) {
                    actor.setBounds(cell.getActorX(), cell.getActorY(), cell.getMaxWidth(), cell.getPrefHeight());
                }
            });
        }

    }

    public static class AssetRow {
        public Cell name;
        public Cell type;
        public Cell loaded;
        public Cell count;

        public String assetName;

        public List<String> dependencies;

        public AssetRow() {
            dependencies = new ArrayList<>();
        }

        public AssetRow(Cell name, Cell type, Cell loaded, Cell count) {
            this();
            this.name = name;
            this.type = type;
            this.loaded = loaded;
            this.count = count;
            EnsureIntegrity();
        }

        public void EnsureIntegrity() {
            All(cell -> {
                Actor actor = cell.getActor();
                if(actor != null)
                    actor.setColor(1, 1, 1, 1);
            });
        }

        public void AddAction(Supplier<Action> action) {
            All(cell -> {
                Actor actor = cell.getActor();
                if(actor != null)
                    actor.addAction(action.get());
            });
        }

        public void All(Consumer<Cell> func) {
            if(name != null)    func.accept(name);
            if(type != null)    func.accept(type);
            if(loaded != null)  func.accept(loaded);
            if(count != null)   func.accept(count);
        }

        public void AllDependencies(Map<String, AssetRow> rowMap, Consumer<Cell> func) {
            dependencies.forEach(dep -> {
                AssetRow row = rowMap.get(dep);
                if(row != null)
                    row.All(func);
            });
        }

        public void AllDependencyRows(Map<String, AssetRow> rowMap, Consumer<AssetRow> func) {
            dependencies.forEach(dep -> {
                AssetRow row = rowMap.get(dep);
                if(row != null)
                    func.accept(row);
            });
        }

        public void AddDependency(String s) {
            dependencies.add(s);
        }
    }

}
