package net.ncguy.utils.tools;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import net.ncguy.utils.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        String[] prepend = ArrayUtils.Prepend(args, GetToolPath(), String.class);
        Process process = new ProcessBuilder(prepend).start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        String line;

        while((line = reader.readLine()) != null) {
            if(onLine != null)
                onLine.accept(line);
        }

        process.waitFor();
    }
}
