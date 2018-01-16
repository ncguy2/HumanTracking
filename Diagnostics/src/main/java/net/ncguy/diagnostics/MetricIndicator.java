package net.ncguy.diagnostics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import net.ncguy.utils.ParallelUtils;

import java.util.concurrent.ForkJoinPool;

public abstract class MetricIndicator extends VisProgressBar {

    protected static ForkJoinPool Pool() {
        return ParallelUtils.Pool();
    }

    protected static void Post(Runnable task) {
        Pool().execute(task);
    }

    VisLabel label;
    float deltaSeconds;

    protected abstract int GetCurrent();
    protected abstract int GetMax();
    protected abstract String GetLabelPattern();

    protected float GetPercentage() {
        return ((float) GetCurrent()) / ((float) GetMax());
    }

    public MetricIndicator() {
        super(0, 100, 1, false);
        label = new VisLabel();
    }

    @Override
    public void act(float delta) {


        setAnimateDuration(.15f);
        deltaSeconds += delta;

        if(deltaSeconds > 1) {
            Post(this::Update);
            deltaSeconds -= 1;
        }
        label.setBounds(getX(), getY(), getWidth(), getHeight());
        label.setAlignment(Align.center);
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        label.draw(batch, parentAlpha);
    }

    public void Update() {
        int current = GetCurrent();
        int max = GetMax();

        float val = GetPercentage() * 100;
        this.setValue(val);

        String text = String.format(GetLabelPattern(), current, max);
        Gdx.app.postRunnable(() -> label.setText(text));
    }

}
