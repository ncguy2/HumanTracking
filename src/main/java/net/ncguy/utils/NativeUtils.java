package net.ncguy.utils;

import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.io.File;
import java.util.function.Function;

public class NativeUtils {

    public static boolean Load(String pathFormat, Function<Boolean, String> platformIdGetter) {

        String platform = platformIdGetter.apply(SharedLibraryLoader.is64Bit);

        String path = String.format(pathFormat, platform);
        File file = new File(path);

        if(file.exists()) {
            String absolutePath = file.getAbsolutePath();
            System.out.printf("Loading \"%s\"\n", absolutePath);
            System.load(absolutePath);
            return true;
        }

        System.out.printf("Unable to find native \"%s\"\n", path);
        return false;
    }

    public static void LoadLibrary(File file) {
        String absolutePath = file.getAbsolutePath();
        System.out.printf("[INFO] Loading native library \"%s\"\n", absolutePath);
        System.load(absolutePath);
    }

}
