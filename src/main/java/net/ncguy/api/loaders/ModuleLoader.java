package net.ncguy.api.loaders;

import net.ncguy.api.BaseModuleInterface;
import net.ncguy.api.log.DefaultLogger;
import net.ncguy.api.log.ILogger;
import net.ncguy.tracking.display.ModularStage;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ModuleLoader<T extends BaseModuleInterface> {

    protected Class<T> type;
    protected Map<File, List<T>> loaded = new HashMap<>();

    protected ILogger logger = new DefaultLogger();

    public ModuleLoader() {
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public ModuleLoader(Class<T> type) {
        this.type = type;
    }

    public void Log(ILogger.LogLevel level, String text) {
        if(logger != null) {
            logger.Log(level, text);
        }
    }

    public void Log(ILogger.LogLevel level, String pattern, String... args) {
        Log(level, String.format(pattern, (Object[]) args));
    }

    public void Load(ModularStage stage, File jar) {
        List<T> items = loaded.get(jar);
        for (T item : items) {
            Log(ILogger.LogLevel.DEBUG, "[%s] %s loaded from %s", getClass().getSimpleName(), item.Name(), jar.getName());
            item.SetJarFileLocation(jar.getAbsolutePath());
            item.Startup();
            item.AddToScene(stage);
        }
    }

    public void Unload(ModularStage stage, File jar) {
        List<T> items = loaded.remove(jar);
        for (T item : items) {
            Log(ILogger.LogLevel.DEBUG, "[%s] %s unloaded from %s", getClass().getSimpleName(), item.Name(), jar.getName());
            item.RemoveFromScene(stage);
            item.Shutdown();
        }
    }

    public void LoadAll(ModularStage stage) {
        loaded.keySet().forEach(jar -> Load(stage, jar));
    }

    public void UnloadAll(ModularStage stage) {
        loaded.keySet().stream().collect(Collectors.toList()).forEach(jar -> Unload(stage, jar));
    }

    public Map<File, List<T>> LoadFromDirectory(File dir) {
        Map<File, List<T>> items = new HashMap<>();
        try {
            LoadFromDirectory(dir, items);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        loaded.putAll(items);
        return items;
    }

    protected void LoadFromDirectory(File dir, Map<File, List<T>> items) throws MalformedURLException {
        if(!dir.exists()) return;
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".jar"));
        if(files == null) return;
        for (File file : files) {
            List<T> ts = LoadFromFile(file);
            items.put(file, ts);
        }
    }

    protected List<T> LoadFromFile(File file) throws MalformedURLException {
        URLClassLoader ucl = new URLClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader());
        ServiceLoader<T> loader = ServiceLoader.load(this.type, ucl);
        List<T> items = new ArrayList<>();
        loader.forEach(items::add);
        return items;
    }

}
