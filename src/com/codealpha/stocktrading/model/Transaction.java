package com.codealpha.stocktrading.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public  class Transaction {

    public enum Type { BUY, SELL }

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Type type;
    private final String symbol;
    private final int quantity;
    private final double pricePerShare;
    private final LocalDateTime timestamp;


    public Transaction(Type type, String symbol, int quantity, double pricePerShare) {
        this(type, symbol, quantity, pricePerShare, LocalDateTime.now());
    }


    public Transaction(Type type, String symbol, int quantity, double pricePerShare, LocalDateTime timestamp) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.timestamp = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public double getTotalValue() {
        return quantity * pricePerShare;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-4s %-6s x%-4d @ $%-8.2f total=$%.2f",
                timestamp.format(FORMAT), type, symbol, quantity, pricePerShare, getTotalValue());
    }
}
