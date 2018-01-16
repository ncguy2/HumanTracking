package net.ncguy.ui.swing;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

public class ProgressDetails {

    protected AtomicReference<ProgressDialog> dialog;

    public ProgressDetails(String title) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                dialog = new AtomicReference<>(new ProgressDialog());
                dialog.get().setTitle(title);
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void Summary(String text) {
        if(dialog.get() != null)
            dialog.get().Summary(text);
    }

    public void Feedback(String line) {
        if(dialog.get() != null)
            dialog.get().Feedback(line);
    }

    public void Invoke(Runnable task) {
        if(dialog.get() != null)
            dialog.get().Show();
        task.run();
        if(dialog.get() != null)
            dialog.get().Hide();
    }


}
