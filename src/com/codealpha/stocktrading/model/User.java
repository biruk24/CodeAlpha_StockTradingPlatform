package com.codealpha.stocktrading.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class User {

    private final String id;
    private final String name;
    private double cashBalance;
    private final Map<String, Holding> holdings = new LinkedHashMap<>();
    private final List<Transaction> transactionHistory = new ArrayList<>();

    public User(String id, String name, double startingCash) {
        this.id = id;
        this.name = name;
        this.cashBalance = startingCash;
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public double getCashBalance() {

        return cashBalance;
    }

    public void deductCash(double amount) {

        cashBalance -= amount;
    }

    public void addCash(double amount) {

        cashBalance += amount;
    }

    public Map<String, Holding> getHoldings() {

        return holdings;
    }

    public Holding getHolding(String symbol) {

        return holdings.get(symbol);
    }

    public void putHolding(Holding holding) {

        holdings.put(holding.getSymbol(), holding);
    }

    public void removeHolding(String symbol) {

        holdings.remove(symbol);
    }

    public void recordTransaction(Transaction transaction) {

        transactionHistory.add(transaction);
    }

    public List<Transaction> getTransactionHistory() {

        return transactionHistory;
    }
}
