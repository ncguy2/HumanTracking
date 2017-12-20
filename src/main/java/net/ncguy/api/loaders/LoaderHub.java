package net.ncguy.api.loaders;

import net.ncguy.api.IMiscModule;
import net.ncguy.tracking.display.ModularStage;

import java.io.File;

public class LoaderHub {

    public static IKLoader ikLoader = new IKLoader();
    public static TrackerLoader trackerLoader = new TrackerLoader();
    public static ModuleLoader<IMiscModule> miscLoader = new ModuleLoader<IMiscModule>() {};

    public static void Load(String directory, ModularStage stage) {
        Load(new File(directory), stage);
    }

    public static void Load(File directory, ModularStage stage) {
        Load(ikLoader, directory, stage);
        Load(trackerLoader, directory, stage);
        Load(miscLoader, directory, stage);
    }

    public static void Load(ModuleLoader loader, File directory, ModularStage stage) {
        loader.LoadFromDirectory(directory);
        loader.LoadAll(stage);
    }

}
