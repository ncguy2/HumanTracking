package net.ncguy.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.IPanel;
import net.ncguy.utils.Reference;
import net.ncguy.utils.task.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskWatcher extends VisTable implements IPanel {

    SimpleBooleanProperty isHovered = new SimpleBooleanProperty(false);
    VisLabel currentTaskLbl;
    List<Task> taskModel;

    public TaskWatcher(ModularStage stage) {
        super();
        taskModel = new ArrayList<>();
        stage.tasks.addListener((ListChangeListener<Task>) c -> {
            while(c.next()) {
                if(c.wasAdded())
                    c.getAddedSubList().forEach(this::AddTask);

                if(c.wasRemoved())
                    c.getRemoved().forEach(this::RemoveTask);
            }
        });
        Init();
    }

    private void AddTask(Task task) {
        if(taskModel.contains(task)) return;
        taskModel.add(task);
        UpdateLabel();
    }

    private void RemoveTask(Task task) {
        if(!taskModel.contains(task)) return;
        taskModel.remove(task);
        UpdateLabel();
    }

    private void UpdateLabel() {
        if(taskModel.isEmpty()) {
            currentTaskLbl.setTouchable(Touchable.disabled);
            currentTaskLbl.setText("");
        }else if(taskModel.size() == 1) {
            currentTaskLbl.setTouchable(Touchable.enabled);
            currentTaskLbl.setText(taskModel.get(0).label);
        }else {
            currentTaskLbl.setTouchable(Touchable.enabled);
            currentTaskLbl.setText(String.format("%s tasks running", taskModel.size()));
        }
        Style();
    }

    @Override
    public void InitUI() {
        currentTaskLbl = new VisLabel("");
    }

    @Override
    public void AttachListeners() {

        isHovered.addListener((observable, oldValue, newValue) -> this.Style());

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                isHovered.set(true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                isHovered.set(false);
            }
        });
    }

    @Override
    public void Assemble() {
        add(currentTaskLbl).grow();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    @Override
    public void Style() {
        setBackground("white");
        setColor(getTouchable().equals(Touchable.enabled) && isHovered.get() ? Reference.Colour.HOVER_GREY : Reference.Colour.DEFAULT_GREY);
    }

    @Override
    public String GetTitle() {
        return "Tasks";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }


}
