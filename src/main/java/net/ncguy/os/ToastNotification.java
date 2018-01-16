package net.ncguy.os;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.ToastManager;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.toast.Toast;
import com.kotcrab.vis.ui.widget.toast.ToastTable;
import javafx.beans.property.SimpleObjectProperty;

import java.awt.*;

public class ToastNotification implements ISysNotification {

    ToastManager toaster;
    SimpleObjectProperty<Stage> stageProperty;

    public ToastNotification() {
        this(null);
    }

    public ToastNotification(Stage stage) {
        stageProperty = new SimpleObjectProperty<>(stage);
        stageProperty.addListener((observable, oldValue, newValue) -> {
            if(toaster != null && newValue != null)
                toaster.clear();
            toaster = new ToastManager(newValue);
        });
    }

    @Override
    public boolean IsSupported() {
        return VisUI.isLoaded();
    }

    @Override
    public void Startup(String imgPath, String caption) {
        if(stageProperty.get() == null) return;
        toaster = new ToastManager(stageProperty.get());
    }

    @Override
    public void PostNotification(String title, String text, TrayIcon.MessageType type) {
        if(toaster == null) return;

        ToastTable table = new ToastTable(true);
        table.add(title).left().colspan(2).row();
        table.add(text).left();
        Toast t = new Toast(table) {
            @Override
            public Table fadeIn() {
                getMainTable().addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(VisWindow.FADE_TIME, Interpolation.fade)));
                return getMainTable();
            }
        };
//        toaster.show(t);

        switch (type) {
            case INFO:
                t.getMainTable().setColor(0, .7f, 1f, 1);
                break;
            case WARNING:
                t.getMainTable().setColor(1, .7f, 0f, 1);
                break;
            case ERROR:
                t.getMainTable().setColor(1, .2f, .2f, 1);
                break;
        }

        toaster.show(t, 5);
    }

    @Override
    public void Shutdown() {
        toaster.clear();
        toaster = null;
    }

    public void SetStage(Stage stage) {
        stageProperty.set(stage);
    }
}
