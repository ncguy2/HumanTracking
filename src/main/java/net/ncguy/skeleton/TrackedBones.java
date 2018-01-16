package net.ncguy.skeleton;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import net.ncguy.api.Property;
import net.ncguy.api.PropertyHelper;
import net.ncguy.tracking.world.Node;

import java.util.List;

public class TrackedBones implements ChangeListener<SKJoint> {

    @Property(Name = "Head Bone", Description = "The bone to map to head-related inputs", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> headBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Jaw Bone", Description = "The bone to map to jaw-related inputs", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> jawBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Mouth top Bone", Description = "The bone to map to the upper lip", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> mouthTopBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Left mouth Bone", Description = "The bone to map to the left corner of the mouth", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> jawLeftBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Right mouth Bone", Description = "The bone to map to the right corner of the mouth", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> jawRightBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Top left eye Bone", Description = "The bone to map to the top of the left eye", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> eyeLeftTopBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Bottom left eye Bone", Description = "The bone to map to the bottom of the left eye", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> eyeLeftBotBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Top right eye Bone", Description = "The bone to map to the top of the right eye", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> eyeRightTopBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Bottom right eye Bone", Description = "The bone to map to the bottom of the right eye", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> eyeRightBotBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Hair root", Description = "The bone to map hair IK", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> hairRootBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Hair Terminal", Description = "The bone to map hair IK", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> hairTerminalBone = new SimpleObjectProperty<>(null);
    @Property(Name = "Origin Bone", Description = "The top-level bone of the skeletal hierarchy", PropertyType = SKJoint.class)
    public static final SimpleObjectProperty<SKJoint> originBone = new SimpleObjectProperty<>(null);

    public static final SimpleObjectProperty<SKJoint> rootBone = new SimpleObjectProperty<>(null);
    public static final SimpleObjectProperty<SKJoint> selectedBone = new SimpleObjectProperty<>(null);

    public static final SimpleObjectProperty<Node> skeletalNode = new SimpleObjectProperty<>(null);

    static {
        rootBone.addListener((observable, oldValue, newValue) -> ResetMappings());
    }

    private static List<PropertyHelper.PropertyWrapper> wrappers;
    public static List<PropertyHelper.PropertyWrapper> GetProperties() {
        if (wrappers == null)
            wrappers = PropertyHelper.GetProperties(TrackedBones.class);
        return wrappers;
    }

    public static void ResetMappings() {
        GetProperties()
                .stream()
                .filter(w -> !w.property.equals(rootBone))      // Don't set the root bone to null
                .map(w -> w.property)                           // Get the property from the wrapper
                .forEach(w -> w.set(null));                     // Set the property value to null
    }

    // Getters

    public static SKJoint RootBone() {
        return rootBone.get();
    }

    public static SKJoint HeadBone() {
        return headBone.get();
    }

    public static SKJoint JawBone() {
        return jawBone.get();
    }

    public static SKJoint MouthTopBone() {
        return mouthTopBone.get();
    }

    public static SKJoint JawLeftBone() {
        return jawLeftBone.get();
    }

    public static SKJoint JawRightBone() {
        return jawRightBone.get();
    }

    public static SKJoint SelectedBone() {
        return selectedBone.get();
    }

    public static SKJoint EyeLeftTopBone() {
        return eyeLeftTopBone.get();
    }

    public static SKJoint EyeLeftBotBone() {
        return eyeLeftBotBone.get();
    }

    public static SKJoint EyeRightTopBone() {
        return eyeRightTopBone.get();
    }

    public static SKJoint EyeRightBotBone() {
        return eyeRightBotBone.get();
    }

    public static SKJoint HairRootBone() {
        return hairRootBone.get();
    }

    public static SKJoint HairTerminalBone() {
        return hairTerminalBone.get();
    }

    public static SKJoint OriginBone() {
        return originBone.get();
    }

    // Instance

    @Override
    public void changed(ObservableValue<? extends SKJoint> observable, SKJoint oldValue, SKJoint newValue) {
        if (oldValue != null)
            oldValue.Reset(true);
    }

}
