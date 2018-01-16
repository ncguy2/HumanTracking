package net.ncguy.ui.detachable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;
import net.ncguy.tracking.display.ModularStage;

public class Sidebar extends VisTable implements IPanel {

    private final boolean btnOnLeft;
    TabContainer tabContainer;
    VisTextButton toggleButton;

    float visibleX;
    float hiddenX;
    boolean isHidden;

    float desiredWidth = 320f;
    float btnWidth = 20;
    float animDuration = .15f;

    public Sidebar(boolean onLeft) {
        super();
        this.btnOnLeft = !onLeft;
        Init();
    }

    public TabContainer TabContainer() {
        return tabContainer;
    }

    public void PulseForAttention(Color colour, float durIn, float durOut) {
        PulseForAttention(colour, durIn, durOut);
    }
    public void PulseForAttention(Color colour, float durIn, float durOut, float holdAtMax) {
        PulseForAttention(colour, durIn, durOut, holdAtMax, null);
    }
    public void PulseForAttention(Color colour, float durIn, float durOut, float holdAtMax, Action parallel) {
        Color orig = Color.WHITE.cpy();

        SequenceAction sequence = Actions.sequence(
                Actions.color(colour, durIn),
                Actions.delay(holdAtMax),
                Actions.color(orig, durOut)
        );

        Action action;

        if(parallel != null)
            action = Actions.parallel(parallel, sequence);
        else action = sequence;

        toggleButton.addAction(action);

    }

    @Override
    public void InitUI() {
        toggleButton = new VisTextButton(btnOnLeft ? "<" : ">");
        isHidden = true;
        toggleButton.getLabel().rotateBy(90);
        tabContainer = new TabContainer();
    }

    @Override
    public void AttachListeners() {
        toggleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ToggleHidden();
            }
        });

        tabContainer.container.AddOnTabAddedListener(tab -> {
            if(tabContainer.container.getTabs().size > 0) {
                PulseForAttention(Color.YELLOW, .15f, 1.5f, .25f, Actions.fadeIn(.15f));
            }
        });

        tabContainer.container.addListener(new TabbedPaneAdapter() {
            @Override
            public void removedTab(Tab tab) {
                super.removedTab(tab);
                if(tabContainer.container.getTabs().size == 0) {
                    toggleButton.clearActions();
                    toggleButton.addAction(Actions.fadeOut(.15f));
                    SetHidden(true);
                }
            }

            @Override
            public void removedAllTabs() {
                super.removedAllTabs();
                toggleButton.addAction(Actions.fadeOut(.15f));
                SetHidden(true);
            }
        });
    }

    public boolean IsOpenable() {
        return tabContainer.container.getTabs().size > 0;
    }

    @Override
    public void Assemble() {
        if(btnOnLeft) {
            add(toggleButton).growY().width(btnWidth);
            add(tabContainer).grow();
        }else {
            add(tabContainer).grow();
            add(toggleButton).growY().width(btnWidth);
        }
    }

    @Override
    public void Style() {
        if(tabContainer.container.getTabs().size == 0) {
            toggleButton.clearActions();
            toggleButton.addAction(Actions.fadeOut(.15f));
            SetHidden(true);
        }
    }

    @Override
    public String GetTitle() {
        return "Sidebar";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }

    public Sidebar Resize(ModularStage stage, int width, int height) {

        float desiredWidth = this.desiredWidth;
        desiredWidth = 320f;
        if(desiredWidth < 1.f)
            desiredWidth = width * desiredWidth;

        setSize(desiredWidth + btnWidth, height);
        if(btnOnLeft) {
            // On right of screen
            visibleX = width - (desiredWidth + btnWidth);
            hiddenX = width - btnWidth;
        }else{
            // On left of screen
            visibleX = 0;
            hiddenX = -desiredWidth;
        }

        Reposition();

        return this;
    }

    public void SetHidden(boolean shouldHide) {
        isHidden = shouldHide;
        Reposition();
    }

    public void ToggleHidden() {
        isHidden = !isHidden;
        Reposition();
    }

    public void Reposition() {

        if (isHidden) {
            toggleButton.setText(btnOnLeft ? "<" : ">");
            addAction(Actions.moveTo(hiddenX, getY(), animDuration));
        } else {
            toggleButton.setText(!btnOnLeft ? "<" : ">");
            addAction(Actions.moveTo(visibleX, getY(), animDuration));
        }

    }
}
