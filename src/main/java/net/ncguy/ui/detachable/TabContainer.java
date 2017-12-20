package net.ncguy.ui.detachable;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;
import net.ncguy.utils.Reference;
import net.ncguy.utils.StageUtils;

public class TabContainer extends VisTable implements IPanel {

    DetachableTabContainer container;
    VisTable contentTable;

    public TabContainer() {
        super();
        Init();
    }

    public boolean RemoveTab(IDetachable tab) {
        return container.RemoveTab(tab);
    }
    public boolean RemoveTab(DetachableTab tab) {
        return container.remove(tab);
    }

    public void AddTab(IDetachable tab) {
        container.AddTab(tab);
    }
    public void AddTab(DetachableTab tab) {
        container.add(tab);
    }

    public void AddTabs(IDetachable... tabs) {
        for (IDetachable tab : tabs)
            AddTab(tab);
    }

    public void AddTabs(DetachableTab... tabs) {
        for (DetachableTab tab : tabs)
            AddTab(tab);
    }

    @Override
    public void InitUI() {
        container = new DetachableTabContainer(this);
        contentTable = new VisTable();
    }

    @Override
    public void AttachListeners() {
        StageUtils.PreventClickthrough(this);
        container.addListener(new TabbedPaneAdapter() {
            @Override
            public void switchedTab(Tab tab) {
                super.switchedTab(tab);
                contentTable.clearChildren();
                if(tab != null)
                    contentTable.add(tab.getContentTable()).grow();
            }
        });
    }

    @Override
    public void Assemble() {
        add(container.getTabsPane()).growX().row();
        add(contentTable).grow().row();

        contentTable.toBack();
    }

    @Override
    public void Style() {
        setBackground("white");
        setColor(Reference.Colour.DEFAULT_GREY);
        container.getTabsPane().setBackground(VisUI.getSkin().getDrawable("grey"), false);
    }

    @Override
    public String GetTitle() {
        return "Tab";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }
}
