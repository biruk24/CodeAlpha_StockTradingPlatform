package com.codealpha.stocktrading.model;


public class Stock {

    private final String symbol;
    private final String name;
    private double price;

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public String getSymbol() {

        return symbol;
    }

    public String getName() {

        return name;
    }

    public double getPrice() {

        return price;
    }

    public void setPrice(double price) {

        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%-6s %-12s $%.2f", symbol, name, price);
    }
}
