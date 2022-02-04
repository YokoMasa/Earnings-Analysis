package com.masalab.earnings.xbrl.calc;

public class CalculationItem<T> {
    
    private T item;
    private double weight;
    private double order;

    public T getItem() {
        return this.item;
    }

    public double getWeight() {
        return this.weight;
    }

    public double getOrder() {
        return this.order;
    }

    public CalculationItem(T item, double weight, double order) {
        this.item = item;
        this.weight = weight;
        this.order = order;
    }

}
