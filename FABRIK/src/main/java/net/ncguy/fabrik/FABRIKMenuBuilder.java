package net.ncguy.fabrik;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;
import net.ncguy.api.loaders.LoaderHub;

public class FABRIKMenuBuilder {

    public Menu menu;
    FABRIKSolver solver;

    public FABRIKMenuBuilder(FABRIKSolver solver) {
        this.solver = solver;
    }

    public void BuildMenu() {
        menu = new Menu("FABRIK");

        MenuItem activateItem = new MenuItem("Activate");
        activateItem.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (solver.IsActive()) {
                    LoaderHub.ikLoader.TrySelect(null);
                    activateItem.setText("Activate");
                } else {
                    LoaderHub.ikLoader.TrySelect(solver);
                    activateItem.setText("Deactivate");
                }
            }
        });

        menu.addItem(activateItem);
    }

}
