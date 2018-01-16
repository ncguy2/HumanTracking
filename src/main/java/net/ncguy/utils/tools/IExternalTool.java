package net.ncguy.utils.tools;

import net.ncguy.ui.swing.ProgressDetails;

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

    default void InvokeSafeLong(String title, String summary, Consumer<String> onLine, String... args) {
        ProgressDetails d = new ProgressDetails(title);
        d.Summary(summary);

        if(onLine == null)
            onLine = d::Feedback;

        final Consumer<String> finalOnLine = onLine;
        d.Invoke(() -> InvokeSafe(finalOnLine, args));
    }

    void Invoke(Consumer<String> onLine, String... args) throws IOException, InterruptedException;
}
