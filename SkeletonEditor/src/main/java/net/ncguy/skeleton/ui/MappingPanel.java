package net.ncguy.skeleton.ui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.api.PropertyHelper;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.skeleton.TrackedBones;
import net.ncguy.skeleton.ui.components.BoneAllocationWidget;
import net.ncguy.tracking.Launcher;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.tracking.display.TrackingSpace;
import net.ncguy.ui.detachable.IPanel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Deprecated
public class MappingPanel extends VisTable implements IPanel {

    VisScrollPane scroller;
    VisTextButton saveBtn;
    VerticalGroup vbox;

    public MappingPanel() {
        super(false);
        Init();
    }

    public void Save(File file) {
        List<PropertyHelper.PropertyWrapper> props = PropertyHelper.GetProperties(TrackedBones.class);
        StringBuilder sb = new StringBuilder();
        sb.append(TrackingSpace.currentLoadedModel).append("\n");
        props.forEach(p -> {
            Object o = p.property.get();
            if(o != null && o instanceof SKJoint) {
                sb.append(p.meta.Name())
                        .append(":")
                        .append(((SKJoint) o).id)
                        .append("\n");
            }
        });
        String s = sb.toString();
        Path path = file.toPath();
        try {
            Files.write(path, s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean Load(final String file) {
        List<PropertyHelper.PropertyWrapper> props = PropertyHelper.GetProperties(TrackedBones.class);

        Map<String, SimpleObjectProperty> propMap = props.stream().collect(Collectors.toMap(p -> p.meta.Name(), p -> p.property));

        boolean isFirst = true;

        try {
            List<String> strings = Files.readAllLines(new File(file).toPath());
            String idealModelName = "";
            for (String line : strings) {
                line = line.trim();
                if(line.isEmpty()) continue;

                if (isFirst) {
                    isFirst = false;
                    idealModelName = line;
                    continue;
                }
                String[] split = line.split(":");
                String propName = split[0];
                String boneName = split[1];

                SimpleObjectProperty prop = propMap.get(propName);
                if (prop != null) {
                    SKJoint bone = TrackedBones.RootBone().Find(boneName, idealModelName.equalsIgnoreCase(TrackingSpace.currentLoadedModel));
                    if (bone != null)
                        prop.set(bone);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean FileDropped(final String[] files, int screenX, int screenY) {
        Vector2 screenPos = localToStageCoordinates(new Vector2());
        Rectangle bounds = new Rectangle(screenPos.x, screenPos.y, getWidth(), getHeight());
        return bounds.contains(screenX, screenY) && Load(files[0]);
    }

    @Override
    public void InitUI() {
        List<PropertyHelper.PropertyWrapper> props = PropertyHelper.GetProperties(TrackedBones.class);
        vbox = new VerticalGroup();
        scroller = new VisScrollPane(vbox);
        saveBtn = new VisTextButton("Save mappings");

        props.forEach(prop -> {
            BoneAllocationWidget widget = new BoneAllocationWidget(prop);

            CollapsibleWidget accordion = new CollapsibleWidget(widget.GetRootTable());
            VisTextButton button = new VisTextButton(widget.GetTitle());
            accordion.setCollapsed(true);
            if(!widget.GetTooltip().isEmpty()) {
                Tooltip.Builder b = new Tooltip.Builder(widget.GetTooltip());
                b.target(button);
                b.build();
            }

            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    accordion.setCollapsed(!accordion.isCollapsed());
                }
            });

            VisTable group = new VisTable(true);

            group.add(button).growX().padBottom(4).row();
            group.add(accordion).growX().padBottom(4).row();
            group.addSeparator().growX().row();

//            vbox.addActor(button);
//            vbox.addActor(accordion);
            vbox.addActor(group);
        });
    }

    @Override
    public void AttachListeners() {
        saveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Stage stage = getStage();
                if(stage instanceof ModularStage)
                    ((ModularStage) stage).SelectFile("Save bone mappings", FileChooser.Mode.SAVE, FileChooser.SelectionMode.FILES, MappingPanel.this::Save);
            }
        });

        Launcher.fileDroppedListeners.add(this::FileDropped);

    }

    @Override
    public void Assemble() {
        add(saveBtn).growX().pad(4).row();
        add(scroller).grow().pad(4).row();
    }

    @Override
    public void Style() {

    }

    @Override
    public String GetTitle() {
        return "Skeletal Mapping";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }
}
