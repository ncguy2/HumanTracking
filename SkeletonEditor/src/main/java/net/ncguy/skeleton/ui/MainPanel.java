package net.ncguy.skeleton.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import net.ncguy.api.ik.BoneNode;
import net.ncguy.skeleton.SkeletonFactory;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.IPanel;

public class MainPanel extends VisTable implements IPanel {

    private final ModularStage stage;
    VisTextButton fileBrowserBtn;
    VisTextButton applyBtn;
    VisTextButton reloadBtn;
    FileChooser fileChooser;

    FileHandle selectedFile;
    BoneNode selectedSkeleton;

    public MainPanel(ModularStage stage) {
        super();
        this.stage = stage;
        Init();
    }

    @Override
    public void InitUI() {
        fileBrowserBtn = new VisTextButton("Browse...");
        applyBtn = new VisTextButton("Apply new skeleton");
        reloadBtn = new VisTextButton("Reload and Apply");
        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
    }

    @Override
    public void AttachListeners() {
        fileBrowserBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fileChooser.setDirectory(selectedFile != null ? selectedFile.path() : "./");
                fileChooser.setMultiSelectionEnabled(false);
                stage.addActor(fileChooser.fadeIn(.4f));
            }
        });

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> files) {
                FileHandle first = files.first();
                if(first == null) return;
                LoadSkeletonFromFile(first);
            }
        });

        applyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ApplySkeleton();
            }
        });

        reloadBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ReloadAndApply();
            }
        });
    }

    @Override
    public void Assemble() {
        add(fileBrowserBtn).growX().height(30).pad(4).row();
        add(applyBtn).growX().height(30).pad(4).row();
        add(reloadBtn).growX().height(30).pad(4).row();
    }

    @Override
    public void Style() {

    }

    public void LoadSkeletonFromFile(FileHandle handle) {
        selectedFile = handle;
        SkeletonFactory skeletonFactory = new SkeletonFactory(handle.file());
        selectedSkeleton = skeletonFactory.Parse();
    }

    public void ReloadAndApply() {
        if(selectedFile == null || !selectedFile.exists()) return;
        LoadSkeletonFromFile(selectedFile);
        ApplySkeleton();
    }

    public void ApplySkeleton() {
        if(!stage.HasBoneStructureSetter()) return;
        stage.GetBoneStructureSetter().accept(selectedSkeleton);
    }

    @Override
    public String GetTitle() {
        return "Skeleton editor";
    }

    @Override
    public Table GetRootTable() {
        return this;
    }
}
