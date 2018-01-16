package net.ncguy.tracking.display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FloatFrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.VisLabel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.os.ToastNotification;
import net.ncguy.tracking.Launcher;
import net.ncguy.tracking.utils.ShaderPreprocessor;
import net.ncguy.tracking.world.Node;
import net.ncguy.utils.AssetUtils;

public abstract class AbstractScreen implements Screen {

    public static SimpleObjectProperty<Node> rootNode = new SimpleObjectProperty<>(null);

    protected FrameBuffer fbo;
    protected FrameBuffer fbo2;
    protected ScreenViewport stageViewport;
    protected OrthographicCamera stageCamera;
    protected ModularStage stage;

    protected SimpleBooleanProperty showFPS;
    protected SimpleBooleanProperty showMenuBar;
    protected SimpleBooleanProperty showMenuBar_internal;
    protected VisLabel fpsLabel;
    protected MenuBar menuBar;

    protected int width;
    protected int height;

    protected ShaderProgram screenShader;
    protected BitmapFont font;

    Image imageColour;
    Texture defTex;

    @Override
    public void show() {

        ShaderProgram.pedantic = false;

        String screenVert = ShaderPreprocessor.LoadShader(Gdx.files.internal("shaders/screen/screen.vert"));
        String screenFrag = ShaderPreprocessor.LoadShader(Gdx.files.internal("shaders/screen/screen.frag"));

        screenShader = new ShaderProgram(screenVert, screenFrag);
        if(!screenShader.isCompiled()) {
            System.out.println(screenShader.getLog());
        }

        showFPS = new SimpleBooleanProperty(true);
        showMenuBar = new SimpleBooleanProperty(true);
        showMenuBar_internal = new SimpleBooleanProperty(true);
        stageCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stageViewport = new ScreenViewport(stageCamera);
        stage = new ModularStage(stageViewport);

        if(Launcher.notificationToolkit != null) {
            if(Launcher.notificationToolkit instanceof ToastNotification)
                ((ToastNotification) Launcher.notificationToolkit).SetStage(stage);
        }

        fpsLabel = new VisLabel();
        showFPS.addListener(observable -> fpsLabel.setVisible(showFPS.get()));

        menuBar = new MenuBar();
        showMenuBar_internal.addListener(observable -> menuBar.getTable().setVisible(showMenuBar_internal.get()));

        Skin skin = VisUI.getSkin();
        font = skin.getFont("default-font");

        stage.SetMenuBar(menuBar);

        imageColour = new Image();

        stage.addActor(menuBar.getTable());
        stage.addActor(fpsLabel);
        stage.addActor(imageColour);

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        fbo = CreateFloatFrameBuffer(width, height, true);
        fbo2 = CreateFrameBuffer(Pixmap.Format.RGB888, 1, width, height, true);
        imageColour.setBounds(0, 0, width, height);

//        defTex = new Texture("textures/default.png");
        AssetUtils.instance().GetAsync("textures/default.png", Texture.class, t -> defTex = t);
    }

    public static FloatFrameBuffer CreateFloatFrameBuffer (int width, int height, boolean hasDepth) {
        GLFrameBuffer.FloatFrameBufferBuilder bufferBuilder = new GLFrameBuffer.FloatFrameBufferBuilder(width, height);
        bufferBuilder.addFloatAttachment(GL30.GL_RGBA32F, GL30.GL_RGBA, GL30.GL_FLOAT, false);
        if (hasDepth) bufferBuilder.addDepthRenderBufferAttachment();
        return bufferBuilder.build();
    }

    public static FrameBuffer CreateFrameBuffer (Pixmap.Format format, int attachments, int width, int height, boolean hasDepth) {
        GLFrameBuffer.FrameBufferBuilder frameBufferBuilder = new GLFrameBuffer.FrameBufferBuilder(width, height);
        for (int i = 0; i < Math.max(attachments, 1); i++)
            frameBufferBuilder.addBasicColorTextureAttachment(format);
        if (hasDepth) frameBufferBuilder.addDepthRenderBufferAttachment();
        return frameBufferBuilder.build();
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
    protected abstract Texture GetShapeTexture();

    @Override
    public void render(float delta) {
        Clear();

        if(showFPS.get())
            fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_GEQUAL);

        Render_Impl(delta);
//        fbo2.begin();
//        screenShader.begin();
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//
//        Texture shapeTex = GetShapeTexture();
        Texture colTex = fbo.getColorBufferTexture();


//        screenShader.setUniformi("u_shapeTex", 10);
//        shapeTex.bind(10);
//        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

//        Batch batch = stage.getBatch();
//        batch.setProjectionMatrix(stageCamera.combined);
//        batch.begin();
//        batch.draw(colTex, 0, height, width, -height);
//        batch.end();

//        screenShader.end();
//        fbo2.end();

        imageColour.setDrawable(new TextureRegionDrawable(new TextureRegion(colTex)));
        imageColour.toBack();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

        this.width = width;
        this.height = height;

        imageColour.setBounds(0, height, width, -height);

        stageCamera.setToOrtho(false, width, height);
        stage.getViewport().update(width, height);

        fpsLabel.pack();
        fpsLabel.setPosition(width - (fpsLabel.getWidth() + 10), height - ((fpsLabel.getHeight() * .75f) + 10));
        stage.GetMenuBar().getTable().setBounds(0, height - 30, width, 30);

        if(fbo != null) {
            fbo.dispose();
            fbo = null;
        }
        fbo = CreateFloatFrameBuffer(width, height, true);

        if(fbo2 != null) {
            fbo2.dispose();
            fbo2 = null;
        }
        fbo2 = CreateFrameBuffer(Pixmap.Format.RGB888, 1, width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

        if(screenShader != null) {
            screenShader.dispose();
            screenShader = null;
        }

        if(fbo != null) {
            fbo.dispose();
            fbo = null;
        }
    }

    @Override
    public void dispose() {
        fbo.dispose();
    }
}
