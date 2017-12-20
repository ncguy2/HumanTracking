package net.ncguy.tracking.display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.VisLabel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.tracking.world.Node;

public abstract class AbstractScreen implements Screen {

    public static SimpleObjectProperty<Node> rootNode = new SimpleObjectProperty<>(null);

    protected ScreenViewport stageViewport;
    protected OrthographicCamera stageCamera;
    protected ModularStage stage;

    protected SimpleBooleanProperty showFPS;
    protected SimpleBooleanProperty showMenuBar;
    protected SimpleBooleanProperty showMenuBar_internal;
    protected VisLabel fpsLabel;
    protected MenuBar menuBar;

    protected BitmapFont font;

    @Override
    public void show() {
        showFPS = new SimpleBooleanProperty(true);
        showMenuBar = new SimpleBooleanProperty(true);
        showMenuBar_internal = new SimpleBooleanProperty(true);
        stageCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stageViewport = new ScreenViewport(stageCamera);
        stage = new ModularStage(stageViewport);

        fpsLabel = new VisLabel();
        showFPS.addListener(observable -> fpsLabel.setVisible(showFPS.get()));

        menuBar = new MenuBar();
        showMenuBar_internal.addListener(observable -> menuBar.getTable().setVisible(showMenuBar_internal.get()));

        Skin skin = VisUI.getSkin();
        font = skin.getFont("default-font");

        stage.SetMenuBar(menuBar);

        stage.addActor(menuBar.getTable());
        stage.addActor(fpsLabel);
    }

    public void Load() {
        LoaderHub.Load("Plugins", stage);
    }

    protected void Clear() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if(showMenuBar.get()) {
            showMenuBar_internal.set(stage.GetMenuCount() > 0);
        }

    }

    protected abstract void Render_Impl(float delta);

    @Override
    public void render(float delta) {


        Clear();

        if(showFPS.get())
            fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());

        Render_Impl(delta);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

        stageCamera.setToOrtho(false, width, height);
        stage.getViewport().update(width, height);

        fpsLabel.pack();
        fpsLabel.setPosition(width - (fpsLabel.getWidth() + 10), height - ((fpsLabel.getHeight() * .75f) + 10));
        stage.GetMenuBar().getTable().setBounds(0, height - 30, width, 30);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
