package net.ncguy.fabrik.sk;

import net.ncguy.skeleton.SKChain;

public class SKFABRIK {

    private final SKChain chain;
    SKFABRIKChain fabrikChain;

    public SKFABRIK(SKChain chain) {
        this.chain = chain;
        fabrikChain = new SKFABRIKChain(chain);
    }

    public void Solve() {
        fabrikChain.Solve();
    }




}
