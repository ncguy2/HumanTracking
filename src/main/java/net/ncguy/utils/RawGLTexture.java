package net.ncguy.utils;

import com.badlogic.gdx.graphics.GLTexture;

public class RawGLTexture extends GLTexture {

    private final int width;
    private final int height;
    private final int depth;

    public RawGLTexture(int glTarget, int width, int height, int depth) {
        super(glTarget);
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public RawGLTexture(int glTarget, int glHandle, int width, int height, int depth) {
        super(glTarget, glHandle);
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public boolean isManaged() {
        return false;
    }

    @Override
    protected void reload() {

    }
}
