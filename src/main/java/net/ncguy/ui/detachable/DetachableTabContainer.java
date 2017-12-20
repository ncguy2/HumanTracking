package net.ncguy.ui.detachable;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

import static net.ncguy.utils.StageUtils.ActorContainsPoint;

public class DetachableTabContainer extends TabbedPane {

    boolean isHidden;
    float defaultHeight;
    float animDuration = .15f;
    Actor anchor;

    Map<IDetachable, DetachableTab> tabMapping;

    public List<Consumer<DetachableTab>> onTabAddedListeners = new ArrayList<>();

    public DetachableTabContainer(Actor anchor) {
        super();
        tabMapping = new HashMap<>();
        this.anchor = anchor;
        Init();
    }

    public void AddOnTabAddedListener(Consumer<DetachableTab> listener) {
        onTabAddedListeners.add(listener);
    }

    public void RemoveOnTabAddedListener(Consumer<DetachableTab> listener) {
        onTabAddedListeners.add(listener);
    }

    public void AddAction(Action action) {
        this.anchor.addAction(action);
    }

    public void RemoveAction(Action action) {
        this.anchor.removeAction(action);
    }

    protected void Init() {
        isHidden = getTabs().size > 1;
        defaultHeight = getTabsPane().getHeight();
        IDetachableContainer.Globals.detachableTabContainers.add(this);

        addListener(new TabbedPaneAdapter() {
            @Override
            public void removedTab(Tab tab) {
                super.removedTab(tab);
                InvalidateHiddenState();
            }

            @Override
            public void removedAllTabs() {
                super.removedAllTabs();
                InvalidateHiddenState();
            }
        });
    }

    public boolean RemoveTab(IDetachable contents) {
        if(!tabMapping.containsKey(contents)) return false;
        DetachableTab detachableTab = tabMapping.remove(contents);
        return remove(detachableTab);
    }

    public void AddTab(IDetachable contents) {
        final DetachableTab tab = new DetachableTab();
        tab.SetContents(contents);
        add(tab);
        onTabAddedListeners.forEach(l -> l.accept(tab));
        tabMapping.put(contents, tab);
        final Optional<Actor> actor = GetActorFromTab(tab);
        actor.ifPresent(a -> {
            a.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    IDetachableContainer.Globals.isDragging = false;
                    IDetachableContainer.Globals.draggingTarget.set(event.getStageX(), event.getStageY());
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    super.touchDragged(event, x, y, pointer);
                    IDetachableContainer.Globals.isDragging = true;
                    IDetachableContainer.Globals.draggingTarget.set(event.getStageX(), event.getStageY());
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                    if(IDetachableContainer.Globals.handleDrop != null && IDetachableContainer.Globals.isDragging) {
                        if (!ActorContainsPoint(anchor, IDetachableContainer.Globals.draggingTarget))
                            IDetachableContainer.Globals.handleDrop.accept(tab, IDetachableContainer.Globals.draggingTarget);
                        IDetachableContainer.Globals.isDragging = false;
                    }
                    super.touchUp(event, x, y, pointer, button);
                }
            });
        });
    }

    public Optional<Actor> GetActorFromTab(DetachableTab tab) {
        final SnapshotArray<Actor> children = getTabsPane().getChildren();
        for (Actor child : children) {
            try {
                final Field tabField = child.getClass().getDeclaredField("tab");
                if(tabField == null) continue;
                tabField.setAccessible(true);
                final Object o = tabField.get(child);
                if(o == tab)
                    return Optional.of(child);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }
        return Optional.empty();
    }

    protected void InvalidateHiddenState() {
        boolean shouldHide = getTabs().size <= 1;
        if(shouldHide == isHidden) return;
        isHidden = shouldHide;

        getTabsPane().addAction(Actions.alpha(shouldHide ? 0.1f : 1.f, animDuration));
    }

    @Override
    protected void addTab(Tab tab, int index) {
        super.addTab(tab, index);
        InvalidateHiddenState();
    }

    public void SetWidth(float width) {
        getTabsPane().setWidth(width);
    }

    public void SizeChanged() {
        getTabsPane().pack();
    }


}
