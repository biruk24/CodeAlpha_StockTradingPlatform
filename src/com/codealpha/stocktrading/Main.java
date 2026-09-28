package com.codealpha.stocktrading;

import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.model.User;
import com.codealpha.stocktrading.repository.FileDataStorage;
import com.codealpha.stocktrading.repository.MarketRepository;
import com.codealpha.stocktrading.repository.StorageException;
import com.codealpha.stocktrading.repository.UserRepository;
import com.codealpha.stocktrading.service.TradingService;
import com.codealpha.stocktrading.ui.ConsoleUI;

public class Main {

    private static final String DEFAULT_DATA_FILE = "data/trading_data.txt";
    private static final String USER_ID = "trader1";

    public static void main(String[] args) {
        String dataPath = (args.length > 0) ? args[0] : DEFAULT_DATA_FILE;
        FileDataStorage storage = new FileDataStorage(dataPath);

        MarketRepository marketRepository = new MarketRepository();
        UserRepository userRepository = new UserRepository();

        if (!loadSavedData(storage, marketRepository, userRepository)) {

            marketRepository = new MarketRepository();
            userRepository = new UserRepository();
            seedNewAccount(marketRepository, userRepository);
            try {
                storage.save(marketRepository.findAll(), userRepository.findAll());
            } catch (StorageException e) {
                System.out.println("Warning: progress cannot be saved. " + e.getMessage());
            }
        }

        TradingService tradingService = new TradingService(marketRepository, userRepository, storage);
        ConsoleUI ui = new ConsoleUI(tradingService, USER_ID);
        ui.run();
    }


    private static boolean loadSavedData(FileDataStorage storage, MarketRepository market, UserRepository users) {
        if (!storage.exists()) {
            System.out.println("No saved data found - starting a new account.");
            return false;
        }
        try {
            storage.load(market, users);
            if (users.findById(USER_ID).isEmpty() || market.findAll().isEmpty()) {
                throw new StorageException("File is missing the trader account or the market data.");
            }
            System.out.println("Loaded saved data from " + storage.describeLocation());
            return true;
        } catch (StorageException e) {
            System.out.println("Could not use saved data: " + e.getMessage());
            try {
                System.out.println("Your old file was kept as: " + storage.backupCorruptFile());
            } catch (StorageException backupError) {
                System.out.println(backupError.getMessage());
            }
            System.out.println("Starting a new account instead.");
            return false;
        }
    }

    private static void seedNewAccount(MarketRepository market, UserRepository users) {
        market.addStock(new Stock("AAPL", "Apple Inc.", 190.00));
        market.addStock(new Stock("GOOG", "Alphabet Inc.", 140.00));
        market.addStock(new Stock("TSLA", "Tesla Inc.", 250.00));
        market.addStock(new Stock("ABAYB", "Abay Bank", 80));
        market.addStock(new Stock("TELE", "Ethio Telecom", 70));
        market.addStock(new Stock("BOAX", "Bank of Abyssinia", 100));
        market.addStock(new Stock("WGBX", "Wegagen Bank", 90));

        users.save(new User(USER_ID, "Trader", 10_000.00));
    }
}
