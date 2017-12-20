package net.ncguy.ui.scene3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import javafx.beans.property.SimpleStringProperty;

import java.util.function.Supplier;

public class AnchoredLabel {

    protected SimpleStringProperty label;
    protected Vector3 anchor;

    public Supplier<String> subTextSupplier;

    protected Vector3 direction;
    protected BitmapFont font;
    protected float distance;

    protected float width;
    protected float height;

    protected transient Vector3 screenPosition;

    static GlyphLayout layout = new GlyphLayout();

    public AnchoredLabel(String label, Vector3 anchor, BitmapFont font) {
        this(label, anchor, Vector3.Zero, font);
    }

    public AnchoredLabel(String label, Vector3 anchor, Vector3 origin, BitmapFont font) {

        if(origin == null)
            origin = Vector3.Zero;

        this.label = new SimpleStringProperty();
        this.anchor = anchor;
        this.direction = new Vector3(this.anchor.cpy().sub(origin).nor());
        this.font = font;
        this.distance = 3;
        this.screenPosition = new Vector3();

        this.label.addListener((observable, oldValue, newValue) -> {
            layout.setText(font, newValue);
            width = layout.width;
            height = layout.height;
        });

        this.label.set(label);
    }

    public void Update(PerspectiveCamera camera) {
        screenPosition.set(anchor).add(direction.cpy().scl(distance));
        camera.project(screenPosition);
    }

    public void RenderText(SpriteBatch batch) {
        font.draw(batch, label.get(), screenPosition.x, screenPosition.y);
        if(subTextSupplier != null) {
            String s = subTextSupplier.get();
            font.draw(batch, s, screenPosition.x, screenPosition.y + height);
        }
    }

    public void RenderLines(ShapeRenderer renderer, Camera camera) {

        Vector3 pos = screenPosition.cpy().sub(0, height + 2, 0);
        pos.z = 0;
        Vector3 project = camera.project(this.anchor.cpy());


        Vector3 left = pos.cpy();
        Vector3 right = pos.cpy().add(width, 0, 0);


        Vector3 best;
        if(left.dst(project) < right.dst(project))
            best = left;
        else best = right;

        renderer.line(pos.cpy(), right);

        project.z = 0;

        renderer.line(best, project);
    }

}
