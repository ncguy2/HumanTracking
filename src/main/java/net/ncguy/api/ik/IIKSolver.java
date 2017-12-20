package net.ncguy.api.ik;

import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.BaseModuleInterface;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.skeleton.SKChain;

import java.util.List;

public interface IIKSolver extends BaseModuleInterface {

    void Solve(BoneChain chain, Vector3 target);

    default boolean SupportsTree() { return false; }
    default void Solve(BoneNode tree, List<Vector3> targets) {}

    default boolean IsActive() {
        return this.equals(LoaderHub.ikLoader.activeSolver.get());
    }

    void StartupIK();
    void ShutdownIK();

    default boolean SupportsSKJoints() { return false; }
    default void Solve(SKChain chain) {}

}
