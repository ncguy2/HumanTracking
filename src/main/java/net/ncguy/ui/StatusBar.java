package net.ncguy.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisTable;
import net.ncguy.ui.detachable.IPanel;
import net.ncguy.utils.Reference;

public class StatusBar extends VisTable implements IPanel {

    private final boolean enforceHeight;

    HorizontalGroup left;
    HorizontalGroup right;

    float actorHeight;

    public StatusBar() {
        this(true);
    }

    public StatusBar(boolean enforceHeight) {
        super(false);
        this.enforceHeight = enforceHeight;
        this.actorHeight = 20;
        Init();
    }

    public void AddToLeft(Actor actor) {
        AddTo(left, actor);
    }

    public void AddToRight(Actor actor) {
        AddTo(right, actor);
    }

    public void AddTo(HorizontalGroup group, Actor actor) {
        if(enforceHeight)
            actor.setHeight(actorHeight);
        group.addActor(actor);
    }

    @Override
    public void InitUI() {
        left = new HorizontalGroup();
        right = new HorizontalGroup();

        left.align(Align.left);
        right.align(Align.right);
    }

    @Override
    public void AttachListeners() {

    }

    @Override
    public void Assemble() {
        add(left).pad(2).grow().left();
        add(right).pad(2).grow().right().row();
    }

    @Override
    public void Style() {
        setHeight(24);
        setBackground("white");
        setColor(Reference.Colour.DEFAULT_GREY);
    }

    @Override
    public String GetTitle() {
        return "Status bar";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }
}
