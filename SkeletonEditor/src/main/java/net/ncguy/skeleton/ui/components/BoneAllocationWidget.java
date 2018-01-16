package net.ncguy.skeleton.ui.components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import net.ncguy.api.PropertyHelper;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.skeleton.TrackedBones;
import net.ncguy.ui.detachable.IPanel;

public class BoneAllocationWidget extends VisTable implements IPanel {

    protected final PropertyHelper.PropertyWrapper wrapper;
    protected VisLabel currentSelectedBoneNameLabel;
    protected VisTextButton useSelectedButton;

    public BoneAllocationWidget(PropertyHelper.PropertyWrapper wrapper) {
        super();
        this.wrapper = wrapper;
        Init();
    }

    @Override
    public void InitUI() {
        currentSelectedBoneNameLabel = new VisLabel();
        useSelectedButton = new VisTextButton("Use selected bone here");

        Object o = wrapper.property.get();
        if(o == null || !(o instanceof SKJoint)) {
            currentSelectedBoneNameLabel.setText("None bound");
            return;
        }
        currentSelectedBoneNameLabel.setText(((SKJoint) o).id);
    }

    @Override
    public void AttachListeners() {
        useSelectedButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                wrapper.property.set(TrackedBones.SelectedBone());
            }
        });

        wrapper.property.addListener((observable, oldValue, newValue) -> {
            if(newValue == null || !(newValue instanceof SKJoint)) {
                currentSelectedBoneNameLabel.setText("None bound");
                return;
            }

            currentSelectedBoneNameLabel.setText(((SKJoint) newValue).id);
        });
    }

    @Override
    public void Assemble() {
        add(currentSelectedBoneNameLabel).grow().left().padTop(4).padLeft(4).padRight(4).row();
        add(useSelectedButton).growX().pad(4);
    }

    @Override
    public void Style() {

    }

    @Override
    public String GetTooltip() {
        return wrapper.meta.Description();
    }

    @Override
    public String GetTitle() {
        return wrapper.meta.Name();
    }

    @Override
    public Table GetRootTable() {
        return this;
    }
}
