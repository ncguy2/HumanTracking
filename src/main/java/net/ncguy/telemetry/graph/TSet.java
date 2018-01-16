package net.ncguy.telemetry.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TSet {

    public String name;
    public List<String> jsonSets;

    public TSet(String name) {
        this.name = name;
        this.jsonSets = new ArrayList<>();
    }

    public void Add(String json) {
        jsonSets.add(json);
    }


    @Override
    public String toString() {
        return jsonSets.stream().collect(Collectors.joining(", "));
    }
}
