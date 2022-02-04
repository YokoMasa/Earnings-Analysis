package com.masalab.earnings.xbrl.calc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.masalab.earnings.xbrl.FactSet;

public class CalculationTree {
    
    // key: uri + id, val: concept
    private Map<String, Concept> concepts = new HashMap<>();

    // key: role, val: root concept list of the role
    private Map<String, List<Concept>> rootConcepts = new HashMap<>();

    public void addConcept(Concept concept) {
        if (!concepts.containsKey(concept.getName())) {
            concepts.put(concept.getName(), concept);
        }
    }

    public Concept getConcept(String name) {
        return concepts.get(name);
    }

    public boolean hasConceptInTree(String name) {
        return concepts.containsKey(name);
    }

    public void addRootConcepts(String role, List<Concept> concepts) {
        rootConcepts.put(role, concepts);
    }

    public boolean hasRootConcept(String role, Concept concept) {
        if (!rootConcepts.containsKey(role)) {
            return false;
        }

        if (rootConcepts.get(role).contains(concept)) {
            return true;
        }
        return false;
    }

    public Set<String> getRoles() {
        return rootConcepts.keySet();
    }

    public boolean hasRootConcept(String role) {
        return rootConcepts.containsKey(role);
    }

    public List<Concept> getRootConcepts(String role) {
        return rootConcepts.get(role);
    }

    public CalculatedFactTree calculate(String conceptName, FactSet factSet, String date) {
        return calculate(conceptName, factSet, null, date);
    }

    public CalculatedFactTree calculate(String conceptName, FactSet factSet, String role, String date) {
        CalculatedFactTree factTree = new CalculatedFactTree();
        getConcept(conceptName).calculate(factTree, factSet, role, date, 0);
        return factTree;
    }

}
