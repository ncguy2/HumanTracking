package net.ncguy.api;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.function.Consumer;

public class StageHelpers {

    public static MenuItem PercentageMenuItem(String label, float initial, Consumer<Float> listener, Menu menu) {
        MenuItem menuItem = new MenuItem("");

        menuItem.clear();

        VisSlider slider = new VisSlider(0, 100, 1, false);
        slider.setValue(100.f * initial);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.accept(slider.getPercent());
                event.reset();
                event.stop();
                event.setBubbles(false);
            }
        });

        menuItem.add(label).grow();
        menuItem.add(slider).grow().right();

        if(menu != null)
            menu.addItem(menuItem);

        return menuItem;
    }

    public static MenuItem SpinnerMenuItem(String label, int initial, int min, int max, Consumer<Integer> listener, Menu menu) {
        MenuItem menuItem = new MenuItem("");

        menuItem.clear();

        IntSpinnerModel model = new IntSpinnerModel(initial, min, max);
        Spinner spinner = new Spinner("", model);
        spinner.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.accept(model.getValue());
                event.reset();
                event.stop();
                event.setBubbles(false);
            }
        });

        menuItem.add(label).grow();
        menuItem.add(spinner).grow().right();

        if(menu != null)
            menu.addItem(menuItem);

        return menuItem;
    }

    public static <T extends Enum<T>> MenuItem DropdownMenuItem(String label, SimpleObjectProperty<T> prop, T[] values, Menu menu) {
        MenuItem menuItem = new MenuItem("");

        menuItem.clear();

        VisSelectBox<T> box = new VisSelectBox<>();
        box.setItems(values);
        box.setSelected(prop.get());
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prop.set(box.getSelected());
            }
        });

        menuItem.add(label).grow();
        menuItem.add(box).growY().right();

        if(menu != null)
            menu.addItem(menuItem);

        return menuItem;
    }

    public static MenuItem CheckBoxMenuItem(String label, SimpleBooleanProperty property, Menu menu) {
        return CheckBoxMenuItem(label, property.get(), property::set, menu);
    }

    public static MenuItem CheckBoxMenuItem(String label, boolean value, Consumer<Boolean> listener) {
        return CheckBoxMenuItem(label, value, listener, null);
    }

    public static MenuItem CheckBoxMenuItem(String label, boolean value, Consumer<Boolean> listener, Menu menu) {
        MenuItem menuItem = new MenuItem("");

        menuItem.clear();

        VisCheckBox box = new VisCheckBox("", value);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.accept(box.isChecked());
                event.reset();
                event.stop();
                event.setBubbles(false);
            }
        });
        menuItem.add(label).grow();
        menuItem.add(box).growY().right();

        if(menu != null)
            menu.addItem(menuItem);

        return menuItem;
    }

}
