package com.codealpha.stocktrading.service;

import com.codealpha.stocktrading.model.Holding;
import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.model.Transaction;
import com.codealpha.stocktrading.model.User;
import com.codealpha.stocktrading.repository.DataStorage;
import com.codealpha.stocktrading.repository.MarketRepository;
import com.codealpha.stocktrading.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TradingService {

    private final MarketRepository marketRepository;
    private final UserRepository userRepository;
    private final DataStorage storage;

    public TradingService(MarketRepository marketRepository, UserRepository userRepository, DataStorage storage) {
        this.marketRepository = marketRepository;
        this.userRepository = userRepository;
        this.storage = storage;
    }


    private void persist() {
        storage.save(marketRepository.findAll(), userRepository.findAll());
    }

    private User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user found with ID '" + userId + "'."));
    }

    private Stock requireStock(String symbol) {
        return marketRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("No stock found with symbol '" + symbol + "'."));
    }

    public void buy(String userId, String symbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        User user = requireUser(userId);
        Stock stock = requireStock(symbol);

        double cost = stock.getPrice() * quantity;
        if (cost > user.getCashBalance()) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient funds: need $%.2f but only have $%.2f.", cost, user.getCashBalance()));
        }

        user.deductCash(cost);

        Holding existing = user.getHolding(symbol);
        if (existing == null) {
            user.putHolding(new Holding(symbol, quantity, stock.getPrice()));
        } else {
            existing.addShares(quantity, stock.getPrice());
        }

        user.recordTransaction(new Transaction(Transaction.Type.BUY, symbol, quantity, stock.getPrice()));
        persist();
    }

    public void sell(String userId, String symbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        User user = requireUser(userId);
        Stock stock = requireStock(symbol);

        Holding holding = user.getHolding(symbol);
        if (holding == null || holding.getQuantity() < quantity) {
            int owned = (holding == null) ? 0 : holding.getQuantity();
            throw new IllegalArgumentException(String.format(
                    "Cannot sell %d shares of %s - you only own %d.", quantity, symbol, owned));
        }

        double proceeds = stock.getPrice() * quantity;
        holding.removeShares(quantity);
        user.addCash(proceeds);

        if (holding.getQuantity() == 0) {
            user.removeHolding(symbol);
        }

        user.recordTransaction(new Transaction(Transaction.Type.SELL, symbol, quantity, stock.getPrice()));
        persist();
    }

    public double getPortfolioValue(String userId) {
        User user = requireUser(userId);
        double holdingsValue = 0.0;
        for (Holding h : user.getHoldings().values()) {
            Stock stock = marketRepository.findBySymbol(h.getSymbol()).orElse(null);
            if (stock != null) {
                holdingsValue += h.getCurrentValue(stock.getPrice());
            }
        }
        return user.getCashBalance() + holdingsValue;
    }


    public record HoldingView(String symbol, int quantity, double avgBuyPrice, double currentPrice,
                               double currentValue, double profitLoss) {
    }

    public List<HoldingView> getPortfolioBreakdown(String userId) {
        User user = requireUser(userId);
        List<HoldingView> views = new ArrayList<>();
        for (Holding h : user.getHoldings().values()) {
            Stock stock = marketRepository.findBySymbol(h.getSymbol()).orElse(null);
            double price = (stock != null) ? stock.getPrice() : 0.0;
            views.add(new HoldingView(h.getSymbol(), h.getQuantity(), h.getAverageBuyPrice(),
                    price, h.getCurrentValue(price), h.getProfitLoss(price)));
        }
        return views;
    }

    public List<Transaction> getTransactionHistory(String userId) {
        return requireUser(userId).getTransactionHistory();
    }

    public double getCashBalance(String userId) {
        return requireUser(userId).getCashBalance();
    }

    public void simulateMarketMovement() {
        marketRepository.simulateMarketMovement();
        persist();
    }

    public Collection<Stock> getMarket() {
        return marketRepository.findAll();
    }
}
