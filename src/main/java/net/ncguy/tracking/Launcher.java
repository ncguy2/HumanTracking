package net.ncguy.tracking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.bulenkov.darcula.DarculaLaf;
import net.ncguy.os.ISysNotification;
import net.ncguy.os.ToastNotification;
import net.ncguy.os.TrayNotification;
import net.ncguy.utils.NativeUtils;
import net.ncguy.utils.ParallelUtils;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Launcher {

    static boolean useNative = false;

    static Random rand = new Random();
    public static String modelPath;
    public static ISysNotification notificationToolkit;

    public static int windowXPosition = 0;
    public static int windowYPosition = 0;

    public static int windowWidth;
    public static int windowHeight;

    public static final List<FileDropListener> fileDroppedListeners = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        System.out.println("Waiting for key press...");
        new Scanner(System.in).next();

        LoadModelFromArgs(args);
        LoadOpenCV();
        LoadSwingTheme();
        LoadNotificationToolkit();

        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();

        windowWidth = ss.width;
        windowHeight = ss.height;

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.useOpenGL3(true, 4, 5);
        cfg.enableGLDebugOutput(true, System.out);
        cfg.setTitle("Human Tracking");
        cfg.setWindowedMode(1600, 900);
        cfg.setBackBufferConfig(16, 16, 16, 16, 16, 0, 0);

        cfg.setWindowListener(new LauncherWindowListener());

        new Lwjgl3Application(new AppListener(), cfg);

        TrackerPipeline.threadAlive = false;
        try {
            ParallelUtils.Pool().awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(notificationToolkit != null)
            notificationToolkit.Shutdown();

        System.out.println("End");

    }

    public static void PostNotification(String title, String text, TrayIcon.MessageType type) {
        if(notificationToolkit == null) {
            System.out.printf("[%s] %s >> %s\n", type.name(), title, text);
            return;
        }
        notificationToolkit.PostNotification(title, text, type);
    }

    private static void LoadNotificationToolkit() {
        ISysNotification notificationToolkit;

        if(useNative) notificationToolkit = new TrayNotification();
        else notificationToolkit = new ToastNotification();

        Launcher.notificationToolkit = notificationToolkit;

        if(!notificationToolkit.IsSupported()) return;
        notificationToolkit.Startup("images/tray.png", "Human Tracking");
    }

    private static void LoadSwingTheme() {
        try {
            UIManager.setLookAndFeel(DarculaLaf.class.getCanonicalName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static void LoadOpenCV() {
        String cvLibLocation = Core.NATIVE_LIBRARY_NAME;
        String formatPattern = "libs/%s/%s.dll";

        String platformDir = "x86";
        if(SharedLibraryLoader.is64Bit)
            platformDir = "x64";

        File file = new File(String.format(formatPattern, platformDir, cvLibLocation));
        NativeUtils.LoadLibrary(file);
    }

    static void LoadModelFromArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if(args[i - 1].equalsIgnoreCase("-m")) {
                modelPath = LoadModel(args[i]);
                return;
            }
        }

        modelPath = LoadModel("Nightshade");

    }

    static String LoadModel(String path) {
        switch(path.toLowerCase()) {
            case "bigvegas": case "vegas":
                return "models/BigVegas/bigvegas.fbx";
            case "nightshade":
                return "models/Nightshade/nightshade_j_friedrich.fbx";
            case "nightshade_m":
                return "models/Nightshade/nightshade_modified.fbx";
            case "parasite":
                return "models/Parasite/parasite_l_starkie.fbx";
            case "passive":
                return "models/Passive/passive_marker_man.fbx";
            case "peasant":
                return "models/PeasantGirl/peasant_girl.fbx";
            case "pirate":
                return "models/Pirate/pirate_p_konstantinov.fbx";
            case "uriel":
                return "models/Uriel/uriel_a_plotexia.fbx";
            case "warrok":
                return "models/Warrok/warrok_w_kurniawan.fbx";
            case "xbot":
                return "models/XBot/xbot.fbx";
            default:
            case "ybot":
                return "models/YBot/ybot.fbx";
            case "zombie":
                return "models/Zombie/zombie.fbx";
            case "zombiegirl":
                return "models/ZombieGirl/zombiegirl_w_kurniawan.fbx";
        }
    }

    static <T> T[] Sample(int amt, Class<T> cls, T... options) {
        return Sample(amt, cls, Arrays.asList(options));
    }

    static <T> T[] Sample(int amt, Class<T> cls, List<T> options) {
        T[] arr = (T[]) Array.newInstance(cls, amt);

        for (int i = 0; i < amt; i++) {
            int idx = rand.nextInt(options.size());
            T selected = options.get(idx);
            arr[i] = selected;
            options.remove(selected);
        }

        return arr;
    }

    @FunctionalInterface
    public static interface FileDropListener {
        boolean OnFilesDropped(final String[] files, final int screenX, final int screenY);
    }

    public static class LauncherWindowListener extends Lwjgl3WindowAdapter {

        private Lwjgl3Window window;

        @Override
        public void filesDropped(final String[] files) {
            for (FileDropListener fileDroppedListener : fileDroppedListeners) {
                if(fileDroppedListener.OnFilesDropped(files, Gdx.input.getX(), Gdx.input.getY()))
                    return;
            }
        }

        @Override
        public void created(Lwjgl3Window window) {
            this.window = window;
            super.created(window);
        }

        @Override
        public void refreshRequested() {
            System.out.println("LauncherWindowListener.refreshRequested");

            windowXPosition = window.getPositionX();
            windowYPosition = window.getPositionY();
            windowWidth = Gdx.graphics.getWidth();
            windowHeight = Gdx.graphics.getHeight();
            super.refreshRequested();

        }
    }


}
