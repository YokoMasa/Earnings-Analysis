package com.masalab.earnings.xbrl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

public class FactSet {
    
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    // key: name, val: list of Fact
    private Map<String, List<Fact>> factsMap = new HashMap<>();

    public void addFact(Fact fact) {        
        if (!factsMap.containsKey(fact.name)) {
            factsMap.put(fact.name, new ArrayList<>());
        }
        factsMap.get(fact.name).add(fact);
    }

    /**
     * Get single fact having factName and date.
     * Returns the first fact that has no dimension, if it exists.
     * Otherwise, returns the first fact with dimension.
     * 
     * @param factName
     * @param date nullable.
     * @return
     */
    public Fact getSingleFact(String factName, String date) {
        List<Fact> factsWithoutDimension = getFacts(null, factName, date, false);
        if (factsWithoutDimension.size() != 0) {
            return factsWithoutDimension.get(0);
        }

        List<Fact> factsWithDimension = getFacts(null, factName, date, true);
        if (factsWithDimension.size() != 0) {
            return factsWithDimension.get(0);
        }
        return null;
    }

    public List<Fact> getFacts(String factName) {
        return factsMap.get(factName);
    }

    public List<Fact> getAllFacts() {
        List<Fact> allFacts = new ArrayList<>();
        for (List<Fact> facts: factsMap.values()) {
            allFacts.addAll(facts);
        }
        return allFacts;
    }

    /**
     * Get facts.
     * 
     * @param ns Namespace. nullable.
     * @param factName Name of the fact. Cannot be null.
     * @param date Date in a form of yyyyMMdd. nullable.
     * @param hasDimension If the context has dimensions or not.
     * @return
     */
    private List<Fact> getFacts(String ns, String factName, String date, boolean hasDimension) {
        List<Fact> filtered = new ArrayList<>();
        if (!factsMap.containsKey(factName)) {
            return filtered;
        }

        for (Fact f: factsMap.get(factName)) {
            if (StringUtils.hasText(ns) && !ns.equals(f.ns)) {
                continue;
            }

            if (StringUtils.hasText(date) && !getDateString(f).equals(date)) {
                continue;
            }

            if (hasDimension) {
                if (!StringUtils.hasText(f.context.scenarioContent) && !StringUtils.hasText(f.context.segmentContent)) {
                    continue;
                }
            } else {
                if (StringUtils.hasText(f.context.scenarioContent) || StringUtils.hasText(f.context.segmentContent)) {
                    continue;
                }
            }
            filtered.add(f);
        }

        return filtered;
    }

    public boolean hasFact(String factName, String date) {
        return getSingleFact(factName, date) != null;
    }

    public boolean hasFact(String factName) {
        return getFacts(factName) != null;
    }

    private String getDateString(Fact f) {
        Date d = null;
        if (f.context.periodType == Context.PeriodType.INSTANT) {
            d = f.context.instant;
        } else {
            d = f.context.endDate;
        }

        if (d == null) {
            return "";
        } else {
            return dateFormat.format(d);
        }
    }

}
