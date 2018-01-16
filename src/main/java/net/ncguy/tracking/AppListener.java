package net.ncguy.tracking;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.VisUI;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.tracking.display.TrackingSpace;
import net.ncguy.utils.AssetUtils;
import net.ncguy.utils.tween.QuaternionInterpolator;
import net.ncguy.utils.tween.SKJointTweenAccessor;

public class AppListener extends Game {

    public static TweenManager tweenManager;

    static {
        Tween.registerAccessor(QuaternionInterpolator.class, new QuaternionInterpolator.QITweenAccessor());
        Tween.registerAccessor(SKJoint.class, new SKJointTweenAccessor());
        tweenManager = new TweenManager();
    }

    @Override
    public void create() {
        VisUI.load();
        setScreen(new TrackingSpace());
    }

    @Override
    public void render() {
        tweenManager.update(Gdx.graphics.getDeltaTime());
        AssetUtils.instance().Update();
        super.render();
    }
}
