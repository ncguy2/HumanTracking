package net.ncguy.os;

import java.awt.*;

public class TrayNotification implements ISysNotification {

    TrayIcon icon;
    SystemTray tray;
    boolean isReady = false;

    @Override
    public boolean IsSupported() {
        return SystemTray.isSupported();
    }

    @Override
    public void Startup(String imgPath, String caption) {
        Image img = Toolkit.getDefaultToolkit().getImage(imgPath);
        icon = new TrayIcon(img, caption);
        tray = SystemTray.getSystemTray();
        icon.setImageAutoSize(true);
        try {
            tray.add(icon);
            isReady = true;
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void PostNotification(String title, String text, TrayIcon.MessageType type) {
        if(!isReady) return;
        icon.displayMessage(title, text, type);
    }

    @Override
    public void Shutdown() {
        tray.remove(icon);
        isReady = false;
    }

}
