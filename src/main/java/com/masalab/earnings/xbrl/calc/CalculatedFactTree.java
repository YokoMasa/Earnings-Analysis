package com.masalab.earnings.xbrl.calc;

import java.util.HashMap;
import java.util.Map;

public class CalculatedFactTree {
    
    private Map<String, CalculatedFact> facts = new HashMap<>();

    public void addFact(CalculatedFact fact) {
        facts.put(fact.getName(), fact);
    }

    public CalculatedFact getFact(String name) {
        return facts.get(name);
    }

    public boolean hasFact(String name) {
        return facts.containsKey(name);
    }

    public double getFactValIgnoringError(String name) {
        if (hasFact(name)) {
            return facts.get(name).getVal();
        } else {
            return 0;
        }
    }

}
