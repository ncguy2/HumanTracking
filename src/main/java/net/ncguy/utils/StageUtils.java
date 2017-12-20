package net.ncguy.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class StageUtils {

    public static boolean ActorContainsPoint(Actor actor, Vector2 point) {
        final Vector2 vec = actor.localToStageCoordinates(new Vector2());
        float x = vec.x;
        float y = vec.y;
        float w = actor.getWidth();
        float h = actor.getHeight();

        if(point.x < x) return false;
        if(point.y < y) return false;

        if(point.x > x + w) return false;
        if(point.y > y + h) return false;

        return true;
    }

    public static void PreventClickthrough(Actor actor) {
        // Listen to touch events
        actor.setTouchable(Touchable.enabled);
        // Consume touchDown event. Any children nodes will take precedence, and will fire first.
        actor.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
    }

}
