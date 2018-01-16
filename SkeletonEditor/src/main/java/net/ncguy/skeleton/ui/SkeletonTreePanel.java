package net.ncguy.skeleton.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.*;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.skeleton.TrackedBones;
import net.ncguy.tracking.display.TrackingSpace;
import net.ncguy.ui.detachable.IPanel;
import net.ncguy.utils.Reference;

import java.util.ArrayList;
import java.util.List;

import static net.ncguy.skeleton.TrackedBones.selectedBone;

public class SkeletonTreePanel extends VisTable implements IPanel {

    VisTextButton resetSkeletonBtn;
    VisTable content;
    Tree tree;
    VisScrollPane scroller;
    boolean changedHere = false;
    List<Tree.Node> treeNodes;

    public SkeletonTreePanel() {
        super(false);
        treeNodes = new ArrayList<>();
        Init();
    }

    @Override
    public void InitUI() {
        content = new VisTable();
//        tree = new Tree(Skins.Cloud());
        tree = new VisTree();
        scroller = new VisScrollPane(tree);
        resetSkeletonBtn = new VisTextButton("Reset skeleton");
    }

    @Override
    public void AttachListeners() {

        tree.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                tree.invalidateHierarchy();
                treeNodes.forEach(n -> {
                    Tree.Node p = n.getParent();
                    n.getActor().setVisible(p == null || p.isExpanded());
                });
            }
        });

        TrackedBones.rootBone.addListener((observable, oldValue, newValue) -> {
            tree.clearChildren();
            if(newValue == null) return;
            AddToTree(null, newValue);
            tree.layout();
            treeNodes.forEach(n -> {
                Tree.Node p = n.getParent();
                n.getActor().setVisible(p == null || p.isExpanded());
            });
        });

        resetSkeletonBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                TrackingSpace.modelReloadRequested = true;
            }
        });

        selectedBone.addListener((observable, oldValue, newValue) -> {

//            if(changedHere) {
//                changedHere = false;
//                return;
//            }

            FlattenTree().forEach(n -> {
                Actor actor = n.getActor();
                Object userObject = actor.getUserObject();
                if(newValue.equals(userObject)) {
                    actor.setColor(Reference.Colour.RendererColours.BONE_LOCATION_ACTIVE_COLOUR);
                }else{
                    actor.setColor(Reference.Colour.RendererColours.BONE_LOCATION_COLOUR);
                }
            });
        });
    }

    List<Tree.Node> FlattenTree() {
        final List<Tree.Node> nodes = new ArrayList<>();
        tree.getRootNodes().forEach(n -> FlattenTree(n, nodes));
        return nodes;
    }

    void FlattenTree(Tree.Node node, final List<Tree.Node> nodes) {
        nodes.add(node);
        node.getChildren().forEach(n -> FlattenTree(n, nodes));
    }

    public void AddToTree(Tree.Node parentNode, SKJoint joint) {
        Label label = new VisLabel(joint.id);
        label.setUserObject(joint);
        label.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                changedHere = true;
                selectedBone.set(joint);
            }
        });

//        Label label = new Label(joint.id, Skins.Cloud());
        tree.addActor(label);
        Tree.Node node = new Tree.Node(label);

        treeNodes.add(node);

        if(parentNode == null) tree.getRootNodes().add(node);
        else parentNode.add(node);
        joint.GetChildJoints().forEach(j -> AddToTree(node, j));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        tree.setDebug(false, true);
//        scroller.setSize(getWidth() - 4, getHeight() - (resetSkeletonBtn.getHeight() + 12));
        tree.setFillParent(false);
}

    public void DrawNode(Batch batch, float alpha, Tree.Node node) {
        node.getActor().draw(batch, alpha);
        node.getChildren().forEach(n -> DrawNode(batch, alpha, n));
    }

    @Override
    public void Assemble() {
        content.add(resetSkeletonBtn).pad(4).growX().height(32).row();
        content.add(scroller).pad(4).grow();

        add(content).grow();
    }

    @Override
    public void Style() {
        content.align(Align.topLeft);
        scroller.setFlickScroll(false);
        scroller.setOverscroll(false, false);
    }

    public void SetDebug(Tree.Node node) {
        node.getActor().setDebug(true);
        node.getChildren().forEach(this::SetDebug);
    }

    @Override
    public String GetTitle() {
        return "Skeleton";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }
}
