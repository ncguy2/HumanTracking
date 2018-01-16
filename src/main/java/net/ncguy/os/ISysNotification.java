package net.ncguy.os;

import java.awt.*;

public interface ISysNotification {

    boolean IsSupported();

    void Startup(String imgPath, String caption);
    void PostNotification(String title, String text, TrayIcon.MessageType type);
    void Shutdown();

}
