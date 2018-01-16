package net.ncguy.skeleton.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MeshTreeRow extends VisTable {

    protected final NodePart part;
    protected final int primitiveType;
    protected HorizontalGroup actionGroup;

    protected VisTextButton visibility;
    protected VisTextButton highlight;

    Color defaultColour;
    ColorAttribute attr;

    Color highlightColour;
    boolean isHighlighted;

    public MeshTreeRow(NodePart part) {
        super();
        this.part = part;
        this.primitiveType = part.meshPart.primitiveType;

        long attrType = ColorAttribute.Diffuse;
        Material mtl = this.part.material;
        if (mtl.has(attrType)) {
            attr = (ColorAttribute) mtl.get(attrType);
            defaultColour = attr.color.cpy();
        }else {
            defaultColour = Color.WHITE.cpy();
            attr = ColorAttribute.createDiffuse(defaultColour);
            mtl.set(attr);
        }

        highlightColour = Color.YELLOW.cpy().mul(defaultColour);
        isHighlighted = false;

        Init();
    }

    public void AddTooltip(Actor actor, String text) {
        Tooltip.Builder b = new Tooltip.Builder(text);
        b.target(actor);
        b.build();
    }

    public void Init() {
        actionGroup = new HorizontalGroup();
        actionGroup.align(Align.right);

        visibility = new VisTextButton("I");
        highlight = new VisTextButton("H");

        AddTooltip(visibility, "Toggle visibility");
        AddTooltip(highlight, "Highlight material");

        visibility.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                part.enabled = !part.enabled;
            }
        });

        highlight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                isHighlighted = !isHighlighted;
                if(isHighlighted) attr.color.set(highlightColour);
                else attr.color.set(defaultColour);
            }
        });

        visibility.setSize(getHeight() - 4, getHeight() - 4);
        highlight.setSize(getHeight() - 4, getHeight() - 4);

        actionGroup.addActor(visibility);
        actionGroup.addActor(highlight);

        Skin skin = VisUI.getSkin();
        BitmapFont fnt = skin.getFont("default-font");
        GlyphLayout glyphRun = new GlyphLayout();

        int maxLblWidth = 192;

        String str = part.meshPart.id;

        glyphRun.setText(fnt, str);
        float width = glyphRun.width;

        boolean wasTruncated = false;

        while(width > maxLblWidth) {
            str = str.substring(0, str.length() - 1);
            glyphRun.setText(fnt, str);
            width = glyphRun.width;
            wasTruncated = true;
        }

        VisLabel label = new VisLabel();

        if(wasTruncated) {
            str += "...";
            Tooltip.Builder builder = new Tooltip.Builder(part.meshPart.id);
            builder.target(label);
            builder.build();
        }

        label.setText(str);

        add(label).left().growX().pad(2);
        add(actionGroup).right().growX().pad(2).row();
    }

    @Override
    protected void sizeChanged() {
        visibility.setSize(getHeight() - 4, getHeight() - 4);
        highlight.setSize(getHeight() - 4, getHeight() - 4);
    }

    public void Reset() {
        part.enabled = true;
        isHighlighted = false;
        attr.color.set(defaultColour);
    }
}
