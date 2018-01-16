package net.ncguy.diagnostics;

import javafx.beans.property.SimpleObjectProperty;

public class MemoryIndicator extends MetricIndicator {

    private static Runtime runtime = Runtime.getRuntime();

    public SimpleObjectProperty<MemoryUnit> memUnit = new SimpleObjectProperty<>(null);
    long maxMemory;
    long totalMemory;

    public MemoryIndicator() {
        super();
        maxMemory = runtime.maxMemory();
        totalMemory = runtime.totalMemory();
        memUnit.set(MemoryUnit.MEGABYTES);
    }

    @Override
    protected int GetCurrent() {
        totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return FormatMemory(totalMemory - freeMemory);
    }

    @Override
    protected int GetMax() {
        return FormatMemory(runtime.totalMemory());
    }

    @Override
    protected String GetLabelPattern() {
        return "%d/%d " + memUnit.get().unit;
    }

//    public void Update() {
//        totalMemory = runtime.totalMemory();
//        long freeMemory = runtime.freeMemory();
//        long usedMemory = totalMemory - freeMemory;
//        MemoryUnit unit = memUnit.get();
//        int used = FormatMemory(usedMemory);
//        formattedTotal = FormatMemory(totalMemory);
//        label.setText(String.format("%d/%d %s", used, formattedTotal, unit.unit));
//        int val = (int) ((((float) used) / ((float) formattedTotal)) * 100);
//        this.setValue(val);
//    }

    public int FormatMemory(long mem) {
        return FormatMemory(mem, memUnit.get());
    }

    public int FormatMemory(long mem, MemoryUnit unit) {
        return Math.round(mem / unit.unitScalar);
    }

    public static enum MemoryUnit {
        BYTES(1, "B"),
        KILOBYTES(1_000, "K"),
        MEGABYTES(1_000_000, "M"),
        GIGABYTES(1_000_000_000, "G"),
        ;

        private final long unitScalar;
        private final String unit;

        MemoryUnit(long unitScalar, String unit) {
            this.unitScalar = unitScalar;
            this.unit = unit;
        }
    }

}
