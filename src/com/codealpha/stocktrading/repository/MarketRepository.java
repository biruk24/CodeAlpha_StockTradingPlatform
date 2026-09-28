package com.codealpha.stocktrading.repository;

import com.codealpha.stocktrading.model.Stock;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;


public class MarketRepository {

    private final Map<String, Stock> stocks = new LinkedHashMap<>();
    private final Random random = new Random();

    public void addStock(Stock stock) {

        stocks.put(stock.getSymbol(), stock);
    }

    public Optional<Stock> findBySymbol(String symbol) {

        return Optional.ofNullable(stocks.get(symbol));
    }

    public Collection<Stock> findAll() {

        return stocks.values();
    }

    public void simulateMarketMovement() {
        for (Stock stock : stocks.values()) {
            double changePercent = (random.nextDouble() * 10) - 5;
            double newPrice = stock.getPrice() * (1 + changePercent / 100);

            stock.setPrice(Math.max(newPrice, 0.01));
        }
    }
}
