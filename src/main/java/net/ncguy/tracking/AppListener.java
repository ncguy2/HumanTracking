package net.ncguy.tracking;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.VisUI;
import net.ncguy.tracking.display.TrackingSpace;

public class AppListener extends Game {

    @Override
    public void create() {
        VisUI.load();
        setScreen(new TrackingSpace());
    }

}
