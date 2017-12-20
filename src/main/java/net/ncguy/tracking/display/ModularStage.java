package net.ncguy.tracking.display;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import net.ncguy.api.ik.BoneNode;
import net.ncguy.tracking.utils.ReflectionUtils;
import net.ncguy.tracking.world.Node;
import net.ncguy.ui.LabeledSeparator;
import net.ncguy.ui.detachable.DetachableTab;
import net.ncguy.ui.detachable.IDetachable;
import net.ncguy.ui.detachable.Sidebar;
import net.ncguy.ui.detachable.TabContainer;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ModularStage extends Stage {

    public ModularStage() {
        super();
    }

    public ModularStage(Viewport viewport) {
        super(viewport);
    }

    public ModularStage(Viewport viewport, Batch batch) {
        super(viewport, batch);
    }

    Map<String, Menu> menuMap = new HashMap<>();

    public void FixMenu(Menu menu) {
        Optional<MenuBar> menuBar = ReflectionUtils.Get(menu, menu.getClass(), "menuBar", MenuBar.class);
        if(!menuBar.isPresent()) {
            ReflectionUtils.Set(menu, "menuBar", this.menuBar);
        }
    }

    public void AddSeparatorToMenu(String menuTitle, String label) {
        AddSeparatorToMenu(menuTitle, label, 4);
    }
    public void AddSeparatorToMenu(String menuTitle, String label, int padding) {
        RequestMenu(menuTitle).add(new LabeledSeparator(label, padding)).padTop(2).padBottom(2).grow().row();
    }

    public void AddSeparatorToMenu(String menuTitle) {
        RequestMenu(menuTitle).addSeparator();
    }

    public MenuItem AddItemToMenu(String menuTitle, MenuItem item) {
        RequestMenu(menuTitle).addItem(item);
        return item;
    }

    public MenuItem AddItemToMenu(String menuTitle, String itemLabel, ChangeListener listener) {
        return AddItemToMenu(menuTitle, new MenuItem(itemLabel, listener));
    }

    public MenuItem AddItemToMenu(String menuTitle, String itemLabel, Runnable listener) {
        return AddItemToMenu(menuTitle, itemLabel, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.run();
            }
        });
    }

    public int GetMenuCount() {
        return menuMap.size();
    }

    public Menu RequestMenu(final String title) {
        if(menuMap.containsKey(title))
            return menuMap.get(title);

        Optional<Array> menus = ReflectionUtils.Get(menuBar, menuBar.getClass(), "menus", Array.class);

        if(menus.isPresent()) {
            Array<Menu> array = menus.get();

            if(array.size > 0) {
                Optional<Menu> first = Stream.of(array.items)
                        .filter(Objects::nonNull)
                        .filter(m -> m.getTitle().equals(title))
                        .findFirst();

                if (first.isPresent())
                    return first.get();
            }

            Menu menu = new Menu(title);
            menuBar.addMenu(menu);
            menuMap.put(title, menu);
            return menu;
        }
        return null;
    }

    public void RemoveTab(IDetachable tab) {
        RemoveTab(tab, false);
    }

    public void RemoveTab(IDetachable tab, boolean notify) {
        if(leftTabContainer.RemoveTab(tab) && notify)
            leftSidebar.PulseForAttention(Color.YELLOW, .2f, 1.5f, .5f);
        if(rightTabContainer.RemoveTab(tab) && notify)
            rightSidebar.PulseForAttention(Color.YELLOW, .2f, 1.5f, .5f);
    }

    public void AddTab(IDetachable tab, Sidebars sidebar) {
        AddTab(tab, sidebar, false);
    }

    public void AddTab(IDetachable tab, Sidebars sidebar, boolean notify) {
        switch(sidebar) {
            case LEFT:
                leftTabContainer.AddTab(tab);
                if(notify) leftSidebar.PulseForAttention(Color.YELLOW, .2f, 1.5f, .5f);
                break;
            case RIGHT:
                rightTabContainer.AddTab(tab);
                if(notify) rightSidebar.PulseForAttention(Color.YELLOW, .2f, 1.5f, .5f);
                break;
        }
    }

    public void RemoveTab(DetachableTab tab) {
        RemoveTab(tab, false);
    }

    public void RemoveTab(DetachableTab tab, boolean notify) {
        if(leftTabContainer.RemoveTab(tab) && notify)
            leftSidebar.PulseForAttention(Color.YELLOW, .2f, 1.5f, .5f);
        if(rightTabContainer.RemoveTab(tab) && notify)
            rightSidebar.PulseForAttention(Color.YELLOW, .2f, 1.5f, .5f);
    }

    public void AddTab(DetachableTab tab, Sidebars sidebar) {
        AddTab(tab, sidebar, false);
    }

    public void AddTab(DetachableTab tab, Sidebars sidebar, boolean notify) {
        switch(sidebar) {
            case LEFT:
                leftTabContainer.AddTab(tab);
                if(notify) leftSidebar.PulseForAttention(Color.YELLOW, .2f, 1.5f, .5f);
                break;
            case RIGHT:
                rightTabContainer.AddTab(tab);
                if(notify) rightSidebar.PulseForAttention(Color.YELLOW, .2f, 1.5f, .5f);
                break;
        }
    }

    protected MenuBar menuBar;

    protected Sidebar leftSidebar;
    protected Sidebar rightSidebar;

    protected TabContainer leftTabContainer;
    protected TabContainer rightTabContainer;

    protected Consumer<BoneNode> boneStructureSetter;
    protected Consumer<Integer> skySphereSetter;

    public final List<Node> worldNodes = new ArrayList<>();


    public boolean HasMenuBar() { return menuBar != null; }
    public MenuBar GetMenuBar() { return menuBar; }
    public void SetMenuBar(MenuBar menuBar) { this.menuBar = menuBar; }

    public boolean HasLeftSidebar() { return leftSidebar != null; }
    public Sidebar GetLeftSidebar() { return leftSidebar; }
    public void SetLeftSidebar(Sidebar leftSidebar) {
        this.leftSidebar = leftSidebar;
        SetLeftTabContainer(leftSidebar.TabContainer());
    }

    public boolean HasRightSidebar() { return rightSidebar != null; }
    public Sidebar GetRightSidebar() { return rightSidebar; }
    public void SetRightSidebar(Sidebar rightSidebar) {
        this.rightSidebar = rightSidebar;
        SetRightTabContainer(rightSidebar.TabContainer());
    }

    public boolean HasLeftTabContainer() { return leftTabContainer != null; }
    public TabContainer GetLeftTabContainer() { return leftTabContainer; }
    public void SetLeftTabContainer(TabContainer leftTabContainer) { this.leftTabContainer = leftTabContainer; }

    public boolean HasRightTabContainer() { return rightTabContainer != null; }
    public TabContainer GetRightTabContainer() { return rightTabContainer; }
    public void SetRightTabContainer(TabContainer rightTabContainer) { this.rightTabContainer = rightTabContainer; }

    public boolean HasBoneStructureSetter() { return boneStructureSetter != null; }
    public Consumer<BoneNode> GetBoneStructureSetter() { return boneStructureSetter; }
    public void SetBoneStructureSetter(Consumer<BoneNode> boneStructureSetter) { this.boneStructureSetter = boneStructureSetter; }

    public boolean HasSkySphereSetter() { return skySphereSetter != null; }
    public Consumer<Integer> GetSkySphereSetter() { return skySphereSetter; }
    public void SetSkySphereSetter(Consumer<Integer> skySphereSetter) { this.skySphereSetter = skySphereSetter; }


    public Optional<Node> FindNode(String name) {
        return worldNodes.stream()
                .filter(n -> name.equalsIgnoreCase(n.name))
                .findFirst();
    }

    public static enum Sidebars {
        LEFT,
        RIGHT,
        ;
    }

}
