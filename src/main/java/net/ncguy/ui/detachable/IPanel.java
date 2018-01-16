package net.ncguy.ui.detachable;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public interface IPanel {

    default void Init() {
        InitUI();
        AttachListeners();
        Assemble();
        Style();
    }

    void InitUI();
    void AttachListeners();
    void Assemble();
    void Style();

    default String GetTooltip() {
        return "";
    }

    String GetTitle();
    Table GetRootTable();

}
