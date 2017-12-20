package net.ncguy.solver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;
import net.ncguy.api.ik.Bone;
import net.ncguy.api.ik.BoneChain;
import net.ncguy.api.ik.IIKSolver;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.utils.MathsUtils;

public class SimpleIKSolver implements IIKSolver {

    SimpleIKMenuBuilder menuBuilder;
    MenuItem popup;
    public float GRAVITY = 0;
    Vector3 endPoint = new Vector3();
    Vector3 diff = new Vector3();

    public SimpleIKSolver() {
        menuBuilder = new SimpleIKMenuBuilder(this);
    }

    @Override
    public String Name() {
        return "Simple IK Solver";
    }

    @Override
    public void Solve(BoneChain chain, Vector3 target) {
        float delta = Gdx.graphics.getDeltaTime();
        float gravity = delta * GRAVITY;

        endPoint.set(target);
        Bone first = chain.First();
        first.GetPosition().set(endPoint);
        Bone last = chain.Last();
        last.GetPosition().set(last.GetPosition());

        chain.Iterate((here, next) -> {
            endPoint.set(here.GetPosition());

            diff.set(endPoint).sub(next.GetPosition());
            diff.add(0, gravity, 0);
            diff.add(next.inertia);
            diff.nor().scl(next.Length());

            float x = endPoint.x - diff.x;
            float y = endPoint.y - diff.y;
            float z = endPoint.z - diff.z;
            next.inertia.add((next.GetPosition().x - x) * delta, (next.GetPosition().y - y) * delta, (next.GetPosition().z - z) * delta).scl(0.99f);
            next.SetPosition(x, y, z);

            here.SetDirection(MathsUtils.GetDirection(here.GetPosition(), next.GetPosition()));

        });

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

    }

    @Override
    public void ShutdownIK() {

    }
}
