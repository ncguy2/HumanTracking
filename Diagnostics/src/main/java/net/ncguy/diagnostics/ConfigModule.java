package net.ncguy.diagnostics;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.api.IMiscModule;
import net.ncguy.tracking.Launcher;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.tracking.display.TrackingSpace;
import net.ncguy.ui.LabeledSeparator;
import net.ncguy.ui.scene3d.AnchoredLabel;

import static net.ncguy.api.StageHelpers.*;
import static net.ncguy.utils.Reference.runScalar;

public class ConfigModule implements IMiscModule {

    MenuItem popup;
    MemoryIndicator memIndicator;
    CPUIndicator cpuIndicator;
    SimpleObjectProperty<LocalModels> selectedModel = new SimpleObjectProperty<>(LocalModels.DEFAULT);
    private ModularStage stage;

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
        CheckBoxMenuItem("Render Bone Directions", TrackingSpace.showBoneDirections, menu);
        CheckBoxMenuItem("Render Bone Locations", TrackingSpace.showBoneLocations, menu);
        CheckBoxMenuItem("Render Bone Labels", AnchoredLabel.shouldRenderLabel, menu);

        menu.add(new LabeledSeparator("Mesh", 4)).padTop(2).padBottom(2).grow().row();
        CheckBoxMenuItem("Render Skeletal Mesh", true, is -> stage.FindNode("SkeletonMesh").ifPresent(node -> node.isVisible = is), menu);
        CheckBoxMenuItem("Render Skeletal Wireframe", false, is -> stage.FindNode("SkeletonMesh").ifPresent(node -> node.drawWireframe.set(is)), menu);

//        DropdownMenuItem("Wireframe Primitive", Node.wireframePrimitiveType, Node.PrimitiveType.values(), menu);

        DropdownMenuItem("Skeletal Mesh", selectedModel, LocalModels.values(), menu);
        MenuItem("Custom Mesh", () -> selectedModel.set(LocalModels.CUSTOM), menu);

        menu.add(new LabeledSeparator("Miscellaneous", 4)).padTop(2).padBottom(2).grow().row();
        PercentageMenuItem("Grid Opacity", 1.f, alpha -> stage.FindNode("Grid").ifPresent(g -> g.SetAlpha(alpha)), menu);
        DropdownMenuItem("Memory unit", memIndicator.memUnit, MemoryIndicator.MemoryUnit.values(), menu);

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
        this.stage = stage;

        Menu settings = stage.RequestMenu("Settings");

        memIndicator = new MemoryIndicator();
        cpuIndicator = new CPUIndicator();

        Menu menu = BuildMenu(stage);

        popup = new MenuItem(menu.getTitle());
        popup.setSubMenu(menu);

        stage.FixMenu(menu);
        settings.addItem(popup);

        stage.GetStatusBar().AddToRight(cpuIndicator);
        stage.GetStatusBar().AddToRight(memIndicator);

    }

    public void RemoveFromScene(ModularStage stage) {
        if(memIndicator != null) {
            memIndicator.remove();
            memIndicator = null;
        }
        if(popup != null) {
            popup.remove();
            popup = null;
        }
    }

    public void Startup() {
        selectedModel.addListener((observable, oldValue, newValue) -> {
            String modelPath = newValue.path;
            if(newValue == LocalModels.DEFAULT)
                modelPath = Launcher.modelPath;
            else if(newValue == LocalModels.CUSTOM) {
                stage.SelectFile("Select file", FileChooser.Mode.OPEN, FileChooser.SelectionMode.FILES, file -> {
                    TrackingSpace.SelectModel(file.getPath());
                });
                return;
            }

            TrackingSpace.SelectModel(modelPath);
        });
    }

    public void Shutdown() {

    }


    public static enum LocalModels {
        DEFAULT("Default", ""),
        _2B("2B", "models/2B/2B.fbx"),
        HISOKA("Hisoka", "models/Hisoka/Hisoka.fbx"),
        MERCY("Mercy", "models/Mercy/Mercy.fbx"),
        MISAKI("Misaki", "models/Misaki/Misaki.fbx"),
        KATYUSHA("Katyusha", "models/Katyusha/Katyusha.fbx"),
        VEGAS("Big Vegas", "models/BigVegas/bigvegas.fbx"),
        NIGHTSHADE("Nightshade", "models/Nightshade/nightshade_j_friedrich.fbx"),
        PARASITE("Parasite", "models/Parasite/parasite_l_starkie.fbx"),
        PASSIVE("Passive Marker Man", "models/Passive/passive_marker_man.fbx"),
        PEASANT_GIRL("Peasant Girl", "models/PeasantGirl/peasant_girl.fbx"),
        PIRATE("Pirate", "models/Pirate/pirate_p_konstantinov.fbx"),
        URIEL("Uriel", "models/Uriel/uriel_a_plotexia.fbx"),
        WARROK("Warrok", "models/Warrok/warrok_w_kurniawan.fbx"),
        X_BOT("X bot", "models/XBot/xbot.fbx"),
        Y_BOT("Y bot", "models/YBot/ybot.fbx"),
        ZOMBIE("Zombie", "models/Zombie/zombie.fbx"),
        ZOMBIE_GIRL("Zombie Girl", "models/ZombieGirl/zombiegirl_w_kurniawan.fbx"),
        CUSTOM("Custom", ""),
        ;

        public final String displayName;
        public final String path;

        LocalModels(String displayName, String path) {
            this.displayName = displayName;
            this.path = path;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

}
