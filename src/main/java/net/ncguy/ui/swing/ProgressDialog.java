package net.ncguy.ui.swing;

import net.ncguy.tracking.Launcher;

import javax.swing.*;

public class ProgressDialog extends JDialog {
    private JPanel contentPane;
    private JProgressBar progressBar1;
    private JTextArea feedbackArea;
    private JLabel textLabel;

    public ProgressDialog() {
        setUndecorated(true);

        setContentPane(contentPane);
        pack();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationByPlatform(false);

        int x = Launcher.windowXPosition;
        int y = Launcher.windowYPosition;

        int w = Launcher.windowWidth;
        int h = Launcher.windowHeight;

        int sW = (int) (getWidth() * .5f);
        int sH = (int) (getHeight() * .5f);
        setLocation((int) (x + (w * .5f)) - sW, (int) (y + (h * .5f)) - sH);
        setAlwaysOnTop(true);
    }

    public void Summary(String text) {
        textLabel.setText(text);
    }

    public void Feedback(String line) {
        feedbackArea.append(line + "\n");
        feedbackArea.setCaretPosition(feedbackArea.getDocument().getLength());
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    public void Show() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    public void Hide() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }

}
