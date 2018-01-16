package net.ncguy.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Skins {

    private static Skin cloud;

    public static Skin Cloud() {
        if (cloud == null)
            cloud = new Skin(Gdx.files.classpath("skins/cloud/cloud-form-ui.json"));
        return cloud;
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(cloud != null) {
                cloud.dispose();
                cloud = null;
            }
        }));
    }

}
