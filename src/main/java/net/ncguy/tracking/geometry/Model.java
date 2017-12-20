package net.ncguy.tracking.geometry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Model {

    public BaseGeometry geometry;
    public Operation operation;
    public Color colour;
    public boolean isDynamic = false;

    public Vector3 translation;
    public Quaternion rotation;
    public Vector3 scale;

    public Model(BaseGeometry geometry, Operation operation) {
        this(geometry, operation, Color.WHITE.cpy());
    }

    public Model(BaseGeometry geometry, Operation operation, Color colour) {
        this.geometry = geometry;
        this.operation = operation;
        this.colour = colour;

        this.translation = new Vector3();
        this.rotation = new Quaternion();
        this.scale = new Vector3(1.f, 1.f, 1.f);
    }

    public Matrix4 Transform() {
        return new Matrix4(translation, rotation, scale);
    }

    public static enum Operation {
        NONE, // Should only be used for the first value. If used on later models, it takes no effect
        UNION,
        INTERSECTION,
        DIFFERENCE;
    }

}
