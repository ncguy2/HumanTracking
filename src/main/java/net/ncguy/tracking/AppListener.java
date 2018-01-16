package net.ncguy.tracking;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.kotcrab.vis.ui.VisUI;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.tracking.display.TrackingSpace;
import net.ncguy.utils.AssetUtils;
import net.ncguy.utils.tween.QuaternionInterpolator;
import net.ncguy.utils.tween.SKJointTweenAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AppListener extends Game {

    public static TweenManager tweenManager;

    public static List<Consumer<Float>> updateTasks;
    public static List<BiConsumer<Batch, Camera>> renderTasks;

    static {
        Tween.registerAccessor(QuaternionInterpolator.class, new QuaternionInterpolator.QITweenAccessor());
        Tween.registerAccessor(SKJoint.class, new SKJointTweenAccessor());
        tweenManager = new TweenManager();

        updateTasks = new ArrayList<>();
        updateTasks.add(tweenManager::update);
        updateTasks.add(d -> AssetUtils.instance().Update());

        renderTasks = new ArrayList<>();
    }

    @Override
    public void create() {
        VisUI.load();
        setScreen(new TrackingSpace());
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        updateTasks.forEach(t -> t.accept(delta));
        super.render();
    }
}
