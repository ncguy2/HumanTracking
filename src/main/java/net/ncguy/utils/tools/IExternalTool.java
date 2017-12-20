package net.ncguy.utils.tools;

import java.io.IOException;
import java.util.function.Consumer;

public interface IExternalTool {

    boolean SupportsCurrentPlatform();
    String GetToolPath();

    default void InvokeSafe(String... args) {
        InvokeSafe(null, args);
    }

    default void InvokeSafe(Consumer<String> onLine, String... args) {
        try {
            Invoke(onLine, args);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    default void Invoke(String... args) throws IOException, InterruptedException {
        Invoke(null, args);
    }

    void Invoke(Consumer<String> onLine, String... args) throws IOException, InterruptedException;
}
