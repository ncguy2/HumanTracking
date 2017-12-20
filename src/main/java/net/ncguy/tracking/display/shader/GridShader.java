package net.ncguy.tracking.display.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GridShader extends BaseShader {

    protected final int u_projTrans = register(new Uniform("u_projTrans"));
    protected final int u_worldTrans = register(new Uniform("u_worldTrans"));
    protected final int u_texture = register(new Uniform("u_texture"));
    private final Renderable renderable;

    protected ShaderProgram program;

    public GridShader(Renderable renderable, ShaderProgram program) {
        super();
        this.renderable = renderable;
        ShaderProgram.pedantic = false;
        this.program = program;
    }

    @Override
    public void init() {
        super.init(this.program, null);
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL, 0.f, 1.f);
        context.setDepthMask(true);
//        program.setUniformMatrix("u_projTrans", camera.combined);
        set(u_projTrans, camera.projection);
    }

    @Override
    public void render(Renderable renderable) {
        set(u_worldTrans, renderable.worldTransform);
//        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        boolean has = renderable.material.has(TextureAttribute.Diffuse);
        if(has) {
            TextureAttribute attribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
            Texture tex = attribute.textureDescription.texture;
            tex.bind(0);
            set(u_texture, 0);
        }
            renderable.meshPart.render(program);
//        super.render(renderable);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        program.dispose();
    }
}
