package net.ncguy.skeleton.ui.components;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import net.ncguy.api.PropertyHelper;

public class BoneAllocationTreeWidget extends BoneAllocationWidget {

    public BoneAllocationTreeWidget(PropertyHelper.PropertyWrapper wrapper) {
        super(wrapper);
    }

    public VisLabel currentSelectedBoneNameLabel() {
        return currentSelectedBoneNameLabel;
    }

    public VisTextButton useSelectedButton() {
        return useSelectedButton;
    }

    @Override
    public void Assemble() {
//        add(currentSelectedBoneNameLabel).grow().left().padTop(4).padLeft(4).padRight(4).row();
//        add(useSelectedButton).growX().pad(4);
    }

}
