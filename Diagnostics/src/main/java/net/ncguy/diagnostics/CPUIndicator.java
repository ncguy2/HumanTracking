package net.ncguy.diagnostics;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class CPUIndicator extends MetricIndicator {

    OperatingSystemMXBean mxBean;

    public CPUIndicator() {
        mxBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    }

    @Override
    protected int GetCurrent() {
        return (int) (mxBean.getProcessCpuLoad() * 100);
    }

    @Override
    protected int GetMax() {
        return 100;
    }

    @Override
    protected String GetLabelPattern() {
        return "%s%%";
    }
}
