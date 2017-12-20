package net.ncguy.utils;

import com.badlogic.gdx.graphics.Color;
import javafx.beans.property.SimpleFloatProperty;

public class Reference {

    public static class Colour {
        public static final float hoverGlowHue = 60;
        public static final float defaultSat = .3f;
        public static final float defaultVal = .5f;

        public static final Color DEFAULT_GREY = new Color(.32f, .32f, .32f, 1.f);
        public static final Color HOVER_COLOUR = FromHue(hoverGlowHue);

        public static Color FromHue(float hue) {
            return FromHue(hue, 1.f);
        }
        public static Color FromHue(float hue, float alpha) {
            final Color c = new Color().fromHsv(hue, defaultSat, defaultVal);
            c.a = alpha;
            return c;
        }

        public static class RendererColours {
            public static final Color BONE_CONNECTION_COLOUR = Color.GREEN.cpy();
            public static final Color BONE_DIRECTION_PARENTED_COLOUR = Color.CYAN.cpy();
            public static final Color BONE_DIRECTION_ORPHANED_COLOUR = Color.WHITE.cpy();
            public static final Color BONE_LOCATION_COLOUR = Color.WHITE.cpy();
            public static final Color BONE_LOCATION_ACTIVE_COLOUR = Color.CORAL.cpy();
            public static final Color IK_CURSOR_TARGET_COLOUR = Color.GOLD.cpy();
        }

    }

    public static final SimpleFloatProperty runScalar = new SimpleFloatProperty(1.f);

}
