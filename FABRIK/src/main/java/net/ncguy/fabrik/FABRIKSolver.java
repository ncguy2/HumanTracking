package net.ncguy.fabrik;

import com.badlogic.gdx.math.Vector3;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;
import net.ncguy.api.ik.BoneChain;
import net.ncguy.api.ik.BoneNode;
import net.ncguy.api.ik.IIKSolver;
import net.ncguy.fabrik.data.FABRIK;
import net.ncguy.skeleton.SKChain;
import net.ncguy.tracking.display.ModularStage;

import java.util.List;

public class FABRIKSolver implements IIKSolver {

    FABRIK fabrik;
    FABRIKMenuBuilder menuBuilder;
    MenuItem popup;
    public float GRAVITY = 0;
    Vector3 endPoint = new Vector3();
    Vector3 diff = new Vector3();

    public FABRIKSolver() {
        menuBuilder = new FABRIKMenuBuilder(this);
    }

    @Override
    public String Name() {
        return "FABRIK Solver";
    }

    @Override
    public void Solve(BoneChain chain, Vector3 target) {
        fabrik.Solve(chain, target);
    }

    @Override
    public void AddToScene(ModularStage stage) {
        if(!stage.HasMenuBar()) return;
        menuBuilder.BuildMenu();

        Menu ikMenu = stage.RequestMenu("IK Solvers");
        popup = new MenuItem(menuBuilder.menu.getTitle());
        popup.setSubMenu(menuBuilder.menu);
        stage.FixMenu(menuBuilder.menu);
        ikMenu.addItem(popup);
    }

    @Override
    public void RemoveFromScene(ModularStage stage) {
        if(popup != null) {
            popup.remove();
            popup = null;
        }
    }

    @Override
    public void Startup() {

    }

    @Override
    public void Shutdown() {

    }

    @Override
    public void StartupIK() {
        fabrik = new FABRIK();
    }

    @Override
    public void ShutdownIK() {
        fabrik = null;
    }

    @Override
    public boolean SupportsTree() {
        return true;
    }

    @Override
    public void Solve(BoneNode tree, List<Vector3> targets) {
        fabrik.Solve(tree, targets);
    }

    @Override
    public boolean SupportsSKJoints() {
        return true;
    }

    @Override
    public void Solve(SKChain chain) {
        fabrik.Solve(chain);
    }
}
