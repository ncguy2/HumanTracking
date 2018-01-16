package net.ncguy.utils;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;

public class StringUtils {

    public static String ToTitleCase(String str) {
        StringBuilder sb = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : str.toCharArray()) {
            if(Character.isSpaceChar(c))
                nextTitleCase = true;
            else if(nextTitleCase) {
                c = Character.toUpperCase(c);
                nextTitleCase = false;
            }else if(!nextTitleCase) {
                c = Character.toLowerCase(c);
            }

            sb.append(c);
        }

        return sb.toString();
    }

    public static String TruncateString(String str, int maxLength) {
        Skin skin = VisUI.getSkin();
        BitmapFont fnt = skin.getFont("default-font");
        GlyphLayout glyphRun = new GlyphLayout();

        glyphRun.setText(fnt, str);
        float width = glyphRun.width;

        boolean wasTruncated = false;

        while(width > maxLength) {
            str = str.substring(0, str.length() - 1);
            glyphRun.setText(fnt, str);
            width = glyphRun.width;
            wasTruncated = true;
        }

        if(wasTruncated)
            str += "...";

        return str;
    }

}
