package com.codealpha.stocktrading.model;


public class Holding {

    private final String symbol;
    private int quantity;
    private double averageBuyPrice;

    public Holding(String symbol, int quantity, double averageBuyPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
    }

    public String getSymbol() {

        return symbol;
    }

    public int getQuantity() {

        return quantity;
    }

    public double getAverageBuyPrice() {

        return averageBuyPrice;
    }


    public void addShares(int additionalQuantity, double purchasePrice) {
        double existingCost = quantity * averageBuyPrice;
        double newCost = additionalQuantity * purchasePrice;
        quantity += additionalQuantity;
        averageBuyPrice = (existingCost + newCost) / quantity;
    }


    public void removeShares(int quantityToRemove) {
        quantity -= quantityToRemove;
    }

    public double getCurrentValue(double currentPrice) {
        return quantity * currentPrice;
    }

    public double getProfitLoss(double currentPrice) {
        return (currentPrice - averageBuyPrice) * quantity;
    }
}
