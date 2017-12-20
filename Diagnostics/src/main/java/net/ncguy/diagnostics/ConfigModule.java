package net.ncguy.diagnostics;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisSlider;
import net.ncguy.api.IMiscModule;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.tracking.display.TrackingSpace;
import net.ncguy.ui.LabeledSeparator;

import static net.ncguy.api.StageHelpers.*;
import static net.ncguy.utils.Reference.runScalar;

public class ConfigModule implements IMiscModule {

    MenuItem popup;

    public Menu BuildMenu(ModularStage stage) {
        Menu menu = new Menu("Configuration");

        menu.add(new LabeledSeparator("Sky sphere", 4)).padTop(2).padBottom(2).grow().row();
        CheckBoxMenuItem("Render Sky Sphere", false, is -> stage.FindNode("Sky sphere").ifPresent(node -> node.isVisible = is), menu);
        SpinnerMenuItem("Sky sphere texture", 0, 0, 4, idx -> {
            if(stage.HasSkySphereSetter())
                stage.GetSkySphereSetter().accept(idx);
        }, menu);

        menu.add(new LabeledSeparator("Skeleton", 4)).padTop(2).padBottom(2).grow().row();
        CheckBoxMenuItem("Render Bone Connections", TrackingSpace.showBoneConnections, menu);
        CheckBoxMenuItem("Render Bone Directions", TrackingSpace.showBoneConnections, menu);
        CheckBoxMenuItem("Render Bone Locations", TrackingSpace.showBoneConnections, menu);

        menu.add(new LabeledSeparator("Mesh", 4)).padTop(2).padBottom(2).grow().row();
        CheckBoxMenuItem("Render Skeletal Mesh", true, is -> stage.FindNode("SkeletonMesh").ifPresent(node -> node.isVisible = is), menu);
        CheckBoxMenuItem("Render Skeletal Wireframe", false, is -> stage.FindNode("SkeletonMesh").ifPresent(node -> node.drawWireframe.set(is)), menu);
        PercentageMenuItem("Grid Opacity", 1.f, alpha -> stage.FindNode("Grid").ifPresent(g -> g.SetAlpha(alpha)), menu);

        VisSlider slider = new VisSlider(0, 5, 1, false);
        slider.setValue(runScalar.floatValue());
        MenuItem runSpeedItem = new MenuItem("Run speed multiplier");
        runSpeedItem.add(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                runScalar.set(slider.getValue());
                runSpeedItem.setText("Run speed multiplier: " + runScalar.get());
                event.stop();
            }
        });

        menu.addSeparator();

        menu.addItem(runSpeedItem);

        return menu;
    }



    public void AddToScene(ModularStage stage) {

        Menu settings = stage.RequestMenu("Settings");

        Menu menu = BuildMenu(stage);

        popup = new MenuItem(menu.getTitle());
        popup.setSubMenu(menu);

        stage.FixMenu(menu);
        settings.addItem(popup);

    }

    public void RemoveFromScene(ModularStage stage) {
        if(popup != null) {
            popup.remove();
            popup = null;
        }
    }

    public void Startup() {

    }

    public void Shutdown() {

    }

}
