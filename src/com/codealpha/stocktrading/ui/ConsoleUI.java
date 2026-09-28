package com.codealpha.stocktrading.ui;

import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.model.Transaction;
import com.codealpha.stocktrading.repository.StorageException;
import com.codealpha.stocktrading.service.TradingService;

import java.util.Scanner;


public class ConsoleUI {

    private final TradingService service;
    private final Scanner scanner;
    private final String userId;

    public ConsoleUI(TradingService service, String userId) {
        this.service = service;
        this.userId = userId;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println(">>>>        CodeAlpha Stock Trading Platform <<<<");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewMarket();
                case "2" -> buyStock();
                case "3" -> sellStock();
                case "4" -> viewPortfolio();
                case "5" -> viewTransactionHistory();
                case "6" -> simulateMarket();
                case "7" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option, please choose 1-7.");
            }
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                }

        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println();
        System.out.printf("Cash balance: $%.2f%n", service.getCashBalance(userId));
        System.out.println("1. View market prices");
        System.out.println("2. Buy stock");
        System.out.println("3. Sell stock");
        System.out.println("4. View portfolio");
        System.out.println("5. View transaction history");
        System.out.println("6. Simulate market movement (advance a day)");
        System.out.println("7. Exit");
        System.out.print("Choose an option: ");
    }

    private void viewMarket() {
        System.out.println();
        System.out.println(">>> Market <<<");
        for (Stock s : service.getMarket()) {
            System.out.println(s);
        }
    }

    private void buyStock() {
        System.out.print("Symbol: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        System.out.print("Quantity: ");
        String rawQty = scanner.nextLine().trim();
        try {
            int quantity = Integer.parseInt(rawQty);
            service.buy(userId, symbol, quantity);
            System.out.println("Bought " + quantity + " shares of " + symbol + ".");
        } catch (NumberFormatException e) {
            System.out.println("Error: '" + rawQty + "' is not a valid whole number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (StorageException e) {
            System.out.println("Warning: done, but NOT saved to disk. " + e.getMessage());
        }
    }

    private void sellStock() {
        System.out.print("Symbol: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        System.out.print("Quantity: ");
        String rawQty = scanner.nextLine().trim();
        try {
            int quantity = Integer.parseInt(rawQty);
            service.sell(userId, symbol, quantity);
            System.out.println("Sold " + quantity + " shares of " + symbol + ".");
        } catch (NumberFormatException e) {
            System.out.println("Error: '" + rawQty + "' is not a valid whole number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (StorageException e) {
            System.out.println("Warning: done, but NOT saved to disk. " + e.getMessage());
        }
    }

    private void viewPortfolio() {
        System.out.println();
        System.out.println(">>>Portfolio <<<");
        var breakdown = service.getPortfolioBreakdown(userId);
        if (breakdown.isEmpty()) {
            System.out.println("No holdings yet.");
        } else {
            for (var h : breakdown) {
                System.out.printf("%-6s qty=%-5d avgBuy=$%-8.2f current=$%-8.2f value=$%-10.2f P/L=$%.2f%n",
                        h.symbol(), h.quantity(), h.avgBuyPrice(), h.currentPrice(), h.currentValue(), h.profitLoss());
            }
        }
        System.out.printf("Cash: $%.2f%n", service.getCashBalance(userId));
        System.out.printf("Total portfolio value: $%.2f%n", service.getPortfolioValue(userId));
    }

    private void viewTransactionHistory() {
        System.out.println();
        System.out.println(">>> Transaction History <<<");
        var history = service.getTransactionHistory(userId);
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (Transaction t : history) {
                System.out.println(t);
            }
        }
    }

    private void simulateMarket() {
        try {
            service.simulateMarketMovement();
            System.out.println("Market moved. New prices:");
        } catch (StorageException e) {
            System.out.println("Market moved, but NOT saved to disk. " + e.getMessage());
        }
        viewMarket();
    }
}
