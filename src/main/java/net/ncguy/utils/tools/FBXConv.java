package net.ncguy.utils.tools;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import net.ncguy.utils.ArrayUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FBXConv implements IExternalTool {

    public static final String rootPath = "tools/fbx/";
    public static final String filePattern = rootPath + "fbx-conv%s";

    @Override
    public boolean SupportsCurrentPlatform() {
        return SharedLibraryLoader.isWindows || SharedLibraryLoader.isLinux || SharedLibraryLoader.isMac;
    }

    @Override
    public String GetToolPath() {

        String platformExt = "";

        if(SharedLibraryLoader.isWindows)
            platformExt = "-win32.exe";
        else if(SharedLibraryLoader.isLinux)
            platformExt = "-lin64";
        else if(SharedLibraryLoader.isMac)
            platformExt = "-mac";

        return String.format(filePattern, platformExt);
    }

    @Override
    public void Invoke(Consumer<String> onLine, String... args) throws IOException, InterruptedException {

        onLine.accept("Initializing conversion process...");
        Thread.sleep(1000);

        String[] prepend = ArrayUtils.Prepend(args, GetToolPath(), String.class);
        Process process = new ProcessBuilder(prepend).start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        String line;

        onLine.accept("Conversion process started");

        while((line = reader.readLine()) != null) {
            if(onLine != null)
                onLine.accept(line);
        }

        process.waitFor();

        onLine.accept("Moving textures to usable location");

        String filePath = args[2];
        String texPath = filePath.substring(0, filePath.length() - 1) + "m";
        File file = new File(texPath);

        String root = filePath.substring(0, filePath.lastIndexOf("/") + 1);

        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files == null) {
                onLine.accept("No textures found in export location");
                return;
            }
            onLine.accept(String.format("Found %s file%s", files.length, files.length != 1 ? "s" : ""));
            for (File f : files) {
                File destFile = new File(root + f.getName());
                Path dest = destFile.toPath();
                Files.copy(f.toPath(), dest);
                onLine.accept(String.format("Copied %s", f.getName()));
            }
        }else {
            System.err.println("Unable to find texture path for "+filePath);
            onLine.accept("Unable to find texture path for "+filePath);
        }

        Thread.sleep(1000);

    }
}
