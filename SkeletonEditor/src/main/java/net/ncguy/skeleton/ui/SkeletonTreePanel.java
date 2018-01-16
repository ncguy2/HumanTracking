package net.ncguy.skeleton.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
import net.ncguy.ui.LabeledSeparator;
import net.ncguy.ui.detachable.IPanel;
import net.ncguy.utils.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ncguy.skeleton.TrackedBones.selectedBone;

public class SkeletonTreePanel extends VisTable implements IPanel {

    public VisTextButton resetSkeletonBtn;
    public VisTable content;
    public Tree tree;
    public VisScrollPane scroller;
    public boolean changedHere = false;
    public List<Tree.Node> treeNodes;

    public Map<Class<?>, List<MenuItem>> managedMenuItems;
    public Map<Class<?>, String> managedMenuLocalisation;
    public PopupMenu contextMenu;

    public SkeletonTreePanel() {
        super(false);
        treeNodes = new ArrayList<>();
        managedMenuItems = new HashMap<>();
        managedMenuLocalisation = new HashMap<>();
        Init();
    }

    @Override
    public void AddManagedMenuName(Class<?> owner, String name) {
        managedMenuLocalisation.put(owner, name);
        contextMenu = null;
    }

    @Override
    public void AddManagedMenuItem(Class<?> owner, MenuItem item) {
        if(!managedMenuItems.containsKey(owner))
            managedMenuItems.put(owner, new ArrayList<>());
        managedMenuItems.get(owner).add(item);
        contextMenu = null;
    }

    @Override
    public void RemoveManagedItems(Class<?> owner) {
        if(!managedMenuItems.containsKey(owner)) return;
        managedMenuItems.remove(owner);
        managedMenuLocalisation.remove(owner);
        contextMenu = null;
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

        tree.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if(event.getButton() == Input.Buttons.LEFT) {
                    tree.invalidateHierarchy();
                    treeNodes.forEach(n -> {
                        Tree.Node p = n.getParent();
                        n.getActor().setVisible(p == null || p.isExpanded());
                    });
                }else if(event.getButton() == Input.Buttons.RIGHT) {
                    OpenContextMenu(x, y);
                }
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

    private void OpenContextMenu(float x, float y) {
        if(managedMenuItems.isEmpty()) return;

        if(contextMenu == null) {
            contextMenu = new PopupMenu();

            boolean first = true;
            for (Map.Entry<Class<?>, List<MenuItem>> entry : managedMenuItems.entrySet()) {
                Class<?> owner = entry.getKey();
                List<MenuItem> items = entry.getValue();
                String s = managedMenuLocalisation.get(owner);
                if(s != null && !s.isEmpty())
                    contextMenu.add(new LabeledSeparator(s));
                else if(!first)
                    contextMenu.addSeparator();
                items.forEach(contextMenu::addItem);
                first = false;
            }

        }
        contextMenu.showMenu(getStage(), x, y);
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
