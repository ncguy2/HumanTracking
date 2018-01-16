package net.ncguy.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTree;

import java.util.Optional;

public class TreeTable extends VisTree {

    public TreeTable(String styleName) {
        super(styleName);
        Init();
    }

    public TreeTable() {
        Init();
    }

    public TreeTable(TreeStyle style) {
        super(style);
        Init();
    }

    void Init() {
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                GetNodeAt(y).ifPresent(Node::Toggle);
            }
        });
    }

    Optional<Node> GetNodeAt(float y) {
        Tree.Node nodeAt = getNodeAt(y);
        if(nodeAt instanceof Node)
            return Optional.of((Node) nodeAt);
        return Optional.empty();
    }

    public static class Node extends Tree.Node {

        private final String label;
        private final Table table;

        public Node(String label, Table table) {
            super(new VisLabel(label));
            this.label = label;
            this.table = table;
            add(new Tree.Node(table));
        }

        public void Toggle() {
            setExpanded(!isExpanded());
        }

    }

}
