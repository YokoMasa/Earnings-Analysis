package com.masalab.earnings.xbrl.calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.masalab.earnings.xbrl.FactSet;

import java.util.Set;

public class Concept {
    
    private String name;

    // key: role, val: calculation items list
    private Map<String, List<CalculationItem<Concept>>> calculationItemsMap = new HashMap<>();

    // key: role, val: parent concept list
    private Map<String, List<Concept>> parentConceptsMap = new HashMap<>();
    private double currentPriority = 0;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addParentConcept(String role, Concept concept) {
        if (!parentConceptsMap.containsKey(role)) {
            parentConceptsMap.put(role, new ArrayList<>());
        }
        parentConceptsMap.get(role).add(concept);
    }

    public List<Concept> getParentConcepts(String role) {
        return parentConceptsMap.get(role);
    }

    public boolean hasParentConcepts(String role) {
        return parentConceptsMap.containsKey(role);
    }

    public boolean hasParentConcepts() {
        return parentConceptsMap.size() != 0;
    }

    public boolean hasConceptInDescendants(String name) {
        return hasConceptInDescendants(name, new HashSet<>());
    }

    public boolean hasConceptInDescendants(String name, Set<String> checkedNames) {
        if (checkedNames.contains(this.name)) {
            return false;
        }

        checkedNames.add(this.name);
        for (List<CalculationItem<Concept>> calculationItems: calculationItemsMap.values()) {
            for (CalculationItem<Concept> calcItem: calculationItems) {
                if (calcItem.getItem().getName().equals(name)) {
                    return true;
                } else if (calcItem.getItem().hasConceptInDescendants(name, checkedNames)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addCalculationItem(CalculationItem<Concept> calculationItem, String resourceId, double priority) {
        if (priority < currentPriority) {
            return;
        } else if (currentPriority < priority) {
            calculationItemsMap = new HashMap<>();
            currentPriority = priority;
        }

        if (!calculationItemsMap.containsKey(resourceId)) {
            calculationItemsMap.put(resourceId, new ArrayList<>());
        }
        calculationItemsMap.get(resourceId).add(calculationItem);
    }

    public CalculatedFact calculate(CalculatedFactTree factTree, FactSet factSet, String role, String date, int level) {
        CalculatedFact fact = null;
        fact = calculateChildren(factTree, factSet, role, date, level);
        factTree.addFact(fact);
        printFact(fact, level);
        return fact;
    }

    private CalculatedFact calculateChildren(CalculatedFactTree factTree, FactSet factSet, String role, String date, int level) {
        double absoluteVal = 0;

        // If factset contains actual value
        if (factSet.hasFact(name, date)) {
            absoluteVal = factSet.getSingleFact(name, date).getValueAsDoubleIgnoringError();
        }

        CalculatedFact fact = new CalculatedFact(name, absoluteVal);

        // childConcept list loop
        for (Entry<String, List<CalculationItem<Concept>>> calculationItems: calculationItemsMap.entrySet()) {
            if (role != null && !role.equals(calculationItems.getKey())) {
                continue;
            }
            
            double val = 0;
            List<CalculationItem<CalculatedFact>> childFacts = new ArrayList<>();

            // childConcept loop
            for (CalculationItem<Concept> conceptCalculationItem: calculationItems.getValue()) {
                Concept childConcept = conceptCalculationItem.getItem();
                CalculatedFact childFact = childConcept.calculate(factTree, factSet, role, date, level+1);
                val += childFact.getVal() * conceptCalculationItem.getWeight();
                // val += childFact.getVal();

                CalculationItem<CalculatedFact> factCalculationItem = new CalculationItem<CalculatedFact>(childFact, conceptCalculationItem.getWeight(), conceptCalculationItem.getOrder());
                childFacts.add(factCalculationItem);
            }

            // If there are several calculationItems, the first calculation result that is nonzero will be the calculated value for this concept.
            fact.setCalculatedVal(calculationItems.getKey(), val, childFacts);
        }

        return fact;
    }

    private void printFact(CalculatedFact f, int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }

        // System.out.println(indent + f.getName() + " abs: " + f.getAbsoluteVal() + ", cal: " + f.getCalculatedVal());
    }

    @Override
    public String toString() {
        return getCalcTreeString(1);
    }

    public String getCalcTreeString(int maxDepth) {
        return getCalcTreeString(0, maxDepth, null);
    }

    public String getCalcTreeString(int maxDepth, String role) {
        return getCalcTreeString(0, maxDepth, role);
    }

    private String getCalcTreeString(int level, int maxDepth, String role) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }

        if (calculationItemsMap.size() == 0 || maxDepth <= level) {
            return name;
        }

        StringBuilder sb = new StringBuilder();
        for (String url: calculationItemsMap.keySet()) {
            if (role != null && !role.equals(url)) {
                continue;
            }
            sb.append(name).append(" (").append(url).append(")").append("\n");
            calculationItemsMap.get(url).stream().forEach((calculationItem) -> {
                Concept c = calculationItem.getItem();
                String operator = calculationItem.getWeight() < 0 ? "(-) " : "(+) ";
                sb.append(indent).append(" ").append(operator).append(c.getCalcTreeString(level + 1, maxDepth, role)).append("\n");
            });
        }

        if (sb.length() == 0) {
            return name;
        }
        return sb.toString().substring(0, sb.length()-1);
    }

    public Concept(String name) {
        this.name = name;
    }

}
