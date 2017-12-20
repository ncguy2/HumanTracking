package net.ncguy.tracker.alpha.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.widget.VisTable;
import net.ncguy.ui.detachable.IPanel;

public class DebugPanel extends VisTable implements IPanel {

    Image image;

    public DebugPanel() {
        super(true);
        Init();
    }

    @Override
    public void InitUI() {
        image = new Image();
    }

    @Override
    public void AttachListeners() {

    }

    @Override
    public void Assemble() {
        add(image).grow().row();
    }

    @Override
    public void Style() {

    }

    @Override
    public String GetTitle() {
        return "Preview";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }

    public void SetImage(Texture tex) {
        image.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));
    }
}
