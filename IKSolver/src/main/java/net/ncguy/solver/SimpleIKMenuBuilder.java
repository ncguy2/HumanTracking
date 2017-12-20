package net.ncguy.solver;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisSlider;
import net.ncguy.api.loaders.LoaderHub;

public class SimpleIKMenuBuilder {

    public Menu menu;
    SimpleIKSolver solver;

    public SimpleIKMenuBuilder(SimpleIKSolver solver) {
        this.solver = solver;
    }

    public void BuildMenu() {
        menu = new Menu("Simple IK");

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

        VisSlider slider = new VisSlider(-20, 20, 1, false);
        slider.setValue(solver.GRAVITY);
        MenuItem gravityItem = new MenuItem("Gravity");
        gravityItem.add(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                solver.GRAVITY = slider.getValue();
                gravityItem.setText("Gravity: " + solver.GRAVITY);
                event.stop();
            }
        });

        gravityItem.setText("Gravity: " + solver.GRAVITY);

        menu.addItem(activateItem);
        menu.addItem(gravityItem);
    }

}
