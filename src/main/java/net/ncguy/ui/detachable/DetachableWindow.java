package net.ncguy.ui.detachable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.kotcrab.vis.ui.widget.VisWindow;
import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.utils.Reference;
import net.ncguy.utils.StageUtils;

import java.util.Optional;

import static net.ncguy.ui.detachable.IDetachableContainer.Globals.detachableTabContainers;
import static net.ncguy.utils.Reference.Colour.DEFAULT_GREY;

public class DetachableWindow extends VisWindow implements IDetachableContainer {

    protected IDetachable content;
    protected transient SimpleObjectProperty<DetachableTabContainer> dropCandidate = new SimpleObjectProperty<>(null);

    public DetachableWindow() {
        this("Window");
    }

    public DetachableWindow(String title) {
        super(title);
        Init();
        setResizable(true);
    }

    protected void Init() {
        float animTime = .15f;
        dropCandidate.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null)
                oldValue.AddAction(Actions.color(DEFAULT_GREY, animTime));

            if(newValue != null) {
                Color colour = Reference.Colour.FromHue(100);
                newValue.AddAction(Actions.color(colour, animTime));
                this.addAction(Actions.alpha(.7f, animTime));
            }else this.addAction(Actions.alpha(1.f, animTime));
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(isDragging()) {
            Optional<DetachableTabContainer> opt = GetBestContainer(new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
            if(opt.isPresent()) dropCandidate.set(opt.get());
            else dropCandidate.set(null);
        }else if(dropCandidate.get() != null) {
            Drop(dropCandidate.get());
            dropCandidate.set(null);
        }
    }

    private void Drop(DetachableTabContainer container) {
        container.AddTab(this.content);
        SetContents(null);
    }

    protected Optional<DetachableTabContainer> GetBestContainer(Vector2 point) {
//        List<DetachableTabContainer> candidates = new ArrayList<>();
//        for (DetachableTabContainer cont : detachableTabContainers) {
//            boolean b = StageUtils.ActorContainsPoint(cont.getTabsPane().getParent(), point);
//            if(b) candidates.add(cont);
//        }
//        return candidates.stream().findFirst();

        return detachableTabContainers.stream()
                .filter(cont -> StageUtils.ActorContainsPoint(cont.getTabsPane().getParent(), point))
                .findFirst();
    }

    @Override
    public void OnRemove(IDetachable removed) {

    }

    @Override
    public Optional<IDetachable> GetContents() {
        return Optional.ofNullable(content);
    }

    @Override
    public void SetContents(IDetachable contents) {
        this.content = contents;
        clearChildren();
        if(this.content == null) {
            this.fadeOut(.15f);
        }else{
            content.SetParent(this);
            content.GetRootTable().ifPresent(this::add);
            getTitleLabel().setText(content.GetTitle());
        }
    }

    @Override
    public float GetMetric() {
        return getZIndex();
    }
}
