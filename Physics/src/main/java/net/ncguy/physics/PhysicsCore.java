package net.ncguy.physics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.bullet.Bullet;
import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.api.BaseModuleInterface;
import net.ncguy.api.IMiscModule;
import net.ncguy.api.StageHelpers;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.physics.sim.PhyWorld;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.skeleton.TrackedBones;
import net.ncguy.tracking.AppListener;
import net.ncguy.tracking.Launcher;
import net.ncguy.tracking.display.ModularStage;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PhysicsCore implements IMiscModule {

    PhyWorld world;
    Consumer<Float> worldUpdate;
    BiConsumer<Batch, Camera> worldRender;

    SimpleObjectProperty<DebugDrawModes> debugDrawMode = new SimpleObjectProperty<>(DebugDrawModes.DBG_NoDebug);

    public void AddToScene(ModularStage stage) {

        LoaderHub.miscLoader.Get("net.ncguy.skeleton.SkeletalModule").ifPresent(mod -> {
            mod.GetPanel("net.ncguy.skeleton.ui.SkeletonTreePanel").ifPresent(pnl -> {
                pnl.AddManagedMenuItem(this.getClass(), StageHelpers.MenuItem("Toggle dynamic chain", () -> {

                    SKJoint skJoint = TrackedBones.SelectedBone();

                    if(skJoint == null) {
                        Launcher.PostNotification("Physics core", "Unable to toggle physics state of chain: No node selected", TrayIcon.MessageType.WARNING);
                        return;
                    }

                    world.ToggleChain(skJoint);

                }, null));
            });
        });

        stage.AddItemToMenu("Physics", StageHelpers.DropdownMenuItem("Debug draw mode", debugDrawMode, DebugDrawModes.values(), null));

        debugDrawMode.addListener((observable, oldValue, newValue) -> world.DebugMode(newValue.flag));
    }

    public void RemoveFromScene(ModularStage stage) {
    }

    public void Startup() {
        Bullet.init();
        world = new PhyWorld();
        worldUpdate = world::Update;
        worldRender = world::DebugDraw;
        AppListener.updateTasks.add(worldUpdate);
        AppListener.renderTasks.add(worldRender);
    }

    public void Shutdown() {
        if(worldUpdate != null)
            AppListener.updateTasks.remove(worldUpdate);

        if(worldRender != null)
            AppListener.renderTasks.remove(worldRender);

        worldUpdate = null;
        world.dispose();
        world = null;
    }

    public String Name() {
        return "Physics";
    }

    @Override
    public Class<? extends BaseModuleInterface>[] Dependencies() {
        //noinspection unchecked
//        return new Class[] { SkeletalModule.class };
        return new Class[0];
    }

    public static enum DebugDrawModes {
        DBG_NoDebug,
        DBG_DrawWireframe,
        DBG_DrawAabb,
        DBG_DrawFeaturesText,
        DBG_DrawContactPoints,
        DBG_NoDeactivation,
        DBG_NoHelpText,
        DBG_DrawText,
        DBG_ProfileTimings,
        DBG_EnableSatComparison,
        DBG_DisableBulletLCP,
        DBG_EnableCCD,
        DBG_DrawConstraints,
        DBG_DrawConstraintLimits,
        DBG_FastWireframe,
        DBG_DrawNormals,
        DBG_DrawFrames,
        ;

        public final int flag;
        DebugDrawModes() {

            flag = ordinal() == 0 ? 0 : (int) (Math.pow(2, ordinal() - 1));
        }
    }

}
