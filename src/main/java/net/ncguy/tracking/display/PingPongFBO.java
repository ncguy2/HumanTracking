package net.ncguy.tracking.display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.FloatFrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class PingPongFBO {

    private FrameBuffer fboA;
    private FrameBuffer fboB;

    private boolean usingA = true;
    private boolean hasDepth;

    public PingPongFBO(boolean hasDepth) {
        this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), hasDepth);
    }

    public PingPongFBO(int width, int height, boolean hasDepth) {
        this.hasDepth = hasDepth;
        fboA = FloatFrameBuffer.createFloatFrameBuffer(width, height, hasDepth);
        fboB = FloatFrameBuffer.createFloatFrameBuffer(width, height, hasDepth);
    }

    public void Resize(int width, int height) {
        fboA.dispose();
        fboB.dispose();

        fboA = FloatFrameBuffer.createFloatFrameBuffer(width, height, hasDepth);
        fboB = FloatFrameBuffer.createFloatFrameBuffer(width, height, hasDepth);
    }

    public FrameBuffer GetCurrent() {
        return usingA ? fboA : fboB;
    }

    public FrameBuffer GetOther() {
        return usingA ? fboB : fboA;
    }

    public FrameBuffer Next() {
        usingA = !usingA;
        return GetCurrent();
    }
}
