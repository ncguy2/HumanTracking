package net.ncguy.telemetry;

import net.ncguy.telemetry.graph.TGraph;

import java.util.ArrayList;
import java.util.List;

public class TelemetryHost {

    protected static TGraph telemetryGraph;
    protected static TGraph TelemetryGraph() {
        if (telemetryGraph == null)
            telemetryGraph = new TGraph();
        return telemetryGraph;
    }

    public static void Set(String group, String id, String jsonData) {
        TelemetryGraph().Add(group, id, jsonData);
    }

    public static List<String> ToString() {
        List<String> lines = new ArrayList<>();
        TelemetryGraph().ForAll((group, set) -> lines.add(String.format("%s::%s: %s\n", group.name, set.name, set.toString())));
        return lines;
    }

}
