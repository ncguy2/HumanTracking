package net.ncguy.ui;

import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

public class LabeledSeparator extends VisTable {

    private final String text;
    private final int padding;

    public LabeledSeparator(String text) {
        this(text, 4);
    }
    public LabeledSeparator(String text, int padding) {
        super(false);
        this.text = text;
        this.padding = padding;
        Build();
    }

    public void Build() {

        VisLabel visLabel = new VisLabel(text);
        visLabel.setColor(.7f, .7f, .7f, 1f);

        add(new Separator("menu")).growX();
        add(visLabel).padLeft(padding).padRight(padding);
        add(new Separator("menu")).growX();
    }


}
