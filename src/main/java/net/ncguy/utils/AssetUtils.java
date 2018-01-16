package net.ncguy.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;
import net.ncguy.tracking.Launcher;
import net.ncguy.utils.loaders.G3DXNAModelLoader;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AssetUtils {

    private static AssetUtils instance;
    public static AssetUtils instance() {
        if (instance == null)
            instance = new AssetUtils();
        return instance;
    }

    private AssetUtils() {
        manager = new AssetManager();
        asyncRequests = new HashMap<>();

        manager.setLoader(Model.class, ".g3dj", new G3DXNAModelLoader(new JsonReader(), manager.getFileHandleResolver()));
        manager.setLoader(Model.class, ".g3db", new G3DXNAModelLoader(new UBJsonReader(), manager.getFileHandleResolver()));

        manager.setErrorListener((asset, throwable) -> {
            throwable.printStackTrace();
            if(asyncRequests.containsKey(asset.fileName)) {
                asyncRequests.remove(asset.fileName);
            }
        });

        Launcher.fileDroppedListeners.add(this::FileDropped);
    }

    private boolean FileDropped(final String[] strings, final int screenX, final int screenY) {
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {

            boolean handled = false;
            for (String string : strings) {
                if(manager.isLoaded(string)) {
                    manager.unload(string);
                    handled = true;
                }
            }

            return handled;
        }
        return false;
    }

    Map<String, Consumer<?>> asyncRequests;
    AssetManager manager;

    public void Update() {
        try {
            manager.update();
        }catch(GdxRuntimeException gre) {
            gre.printStackTrace();
        }

        asyncRequests
                .entrySet()
                .stream()
                .filter(e -> manager.isLoaded(e.getKey()))
                .peek(e -> e.getValue().accept(manager.get(e.getKey())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .forEach(asyncRequests::remove);
    }

    public <T> T Get(String path, Class<T> type) {

        if(!manager.isLoaded(path, type)) {
            manager.load(path, type);
            manager.finishLoadingAsset(path);
        }

        return manager.get(path, type);
    }

    public <T> void GetAsync(String path, Class<T> type, Consumer<T> func) {
        if(manager.isLoaded(path, type)) {
            func.accept(manager.get(path, type));
            return;
        }

        manager.load(path, type);
        asyncRequests.put(path, func.andThen(t -> {
            Launcher.PostNotification(type.getSimpleName() + " loaded", path + " loaded successfully", TrayIcon.MessageType.INFO);
        }));
    }

    public boolean IsLoaded(String path) {
        return manager.isLoaded(path);
    }

    public boolean IsLoaded(String path, Class<?> cls) {
        return manager.isLoaded(path, cls);
    }

    public void UsingManager(Consumer<AssetManager> func) {
        func.accept(manager);
    }

}
