package net.ncguy.skeleton.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import net.ncguy.skeleton.ui.components.MeshTreeRow;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.tracking.world.Node;
import net.ncguy.ui.detachable.IPanel;

import java.util.*;

import static net.ncguy.skeleton.TrackedBones.skeletalNode;

public class MeshTreePanel extends VisTable implements IPanel {

    private final ModularStage stage;
    VisTextButton resetMeshBtn;
    VisTable content;
    VisScrollPane scroller;
    Map<NodePart, Integer> primitiveMap;
    List<MeshTreeRow> rows;

    public MeshTreePanel(ModularStage stage) {
        super(false);
        this.stage = stage;
        primitiveMap = new HashMap<>();
        Init();
    }

    @Override
    public void InitUI() {
        content = new VisTable();
        scroller = new VisScrollPane(content);
        resetMeshBtn = new VisTextButton("Reset Mesh");
        rows = new ArrayList<>();

        skeletalNode.addListener((observable, oldValue, newValue) -> {
            SkeletalNodeListener(newValue);
        });

        if(skeletalNode.get() != null)
            SkeletalNodeListener(skeletalNode.get());
    }

    protected void SkeletalNodeListener(Node newValue) {
        content.clear();
        primitiveMap.clear();
        rows.clear();
        if(newValue == null) return;

        newValue.GetInstance().nodes.forEach(node ->
                node.parts.forEach(part ->
                        primitiveMap.put(part, part.meshPart.primitiveType)));

        primitiveMap.keySet()
                .stream()
                .sorted(Comparator.comparing(p -> p.meshPart.id))
                .map(MeshTreeRow::new)
                .peek(rows::add)
                .forEach(r -> content.add(r).growX().padRight(8).row());
    }

    @Override
    public void AttachListeners() {

        resetMeshBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rows.forEach(MeshTreeRow::Reset);
            }
        });
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        content.setDebug(false, true);
    }

    @Override
    public void Assemble() {
        add(resetMeshBtn).growX().pad(4).row();
        add(scroller).grow().top().pad(4).row();
    }

    @Override
    public void Style() {
        scroller.setFlickScroll(false);
        scroller.setOverscroll(false, false);
    }

    @Override
    public String GetTitle() {
        return "Mesh parts";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }
}
