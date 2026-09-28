package com.codealpha.stocktrading.repository;

import com.codealpha.stocktrading.model.Holding;
import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.model.Transaction;
import com.codealpha.stocktrading.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class FileDataStorage implements DataStorage {

    private static final String HEADER = "# CodeAlpha Stock Trading Platform data file (v1)";
    private static final String SEP = "|";

    private final Path file;

    public FileDataStorage(String path) {
        this.file = Paths.get(path);
    }

    @Override
    public boolean exists() {
        return Files.isRegularFile(file);
    }

    @Override
    public String describeLocation() {
        return file.toAbsolutePath().toString();
    }



    @Override
    public void save(Collection<Stock> stocks, Collection<User> users) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (Stock s : stocks) {
            lines.add(String.join(SEP, "STOCK", field(s.getSymbol()), field(s.getName()),
                    Double.toString(s.getPrice())));
        }
        for (User u : users) {
            lines.add(String.join(SEP, "USER", field(u.getId()), field(u.getName()),
                    Double.toString(u.getCashBalance())));
            for (Holding h : u.getHoldings().values()) {
                lines.add(String.join(SEP, "HOLDING", field(u.getId()), field(h.getSymbol()),
                        Integer.toString(h.getQuantity()), Double.toString(h.getAverageBuyPrice())));
            }
            for (Transaction t : u.getTransactionHistory()) {
                lines.add(String.join(SEP, "TXN", field(u.getId()), t.getType().name(), field(t.getSymbol()),
                        Integer.toString(t.getQuantity()), Double.toString(t.getPricePerShare()),
                        t.getTimestamp().toString()));
            }
        }

        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Could not write data file '" + file + "': " + e.getMessage(), e);
        }
    }

    private static String field(String value) {
        if (value.contains(SEP) || value.contains("\n") || value.contains("\r")) {
            throw new StorageException("Cannot save a value containing '|' or a line break: " + value);
        }
        return value;
    }


    @Override
    public void load(MarketRepository marketRepository, UserRepository userRepository) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Could not read data file '" + file + "': " + e.getMessage(), e);
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int lineNo = i + 1;
            String[] p = line.split("\\|", -1);

            try {
                switch (p[0]) {
                    case "STOCK" -> {
                        expectFields(p, 4, lineNo);
                        double price = Double.parseDouble(p[3]);
                        requirePositive(price, "price", lineNo);
                        marketRepository.addStock(new Stock(p[1], p[2], price));
                    }
                    case "USER" -> {
                        expectFields(p, 4, lineNo);
                        double cash = Double.parseDouble(p[3]);
                        requireNonNegative(cash, "cash balance", lineNo);
                        userRepository.save(new User(p[1], p[2], cash));
                    }
                    case "HOLDING" -> {
                        expectFields(p, 5, lineNo);
                        int quantity = Integer.parseInt(p[3]);
                        double avgPrice = Double.parseDouble(p[4]);
                        requirePositive(quantity, "quantity", lineNo);
                        requirePositive(avgPrice, "average buy price", lineNo);
                        requireUser(userRepository, p[1], lineNo).putHolding(new Holding(p[2], quantity, avgPrice));
                    }
                    case "TXN" -> {
                        expectFields(p, 7, lineNo);
                        Transaction.Type type = Transaction.Type.valueOf(p[2]);
                        int quantity = Integer.parseInt(p[4]);
                        double price = Double.parseDouble(p[5]);
                        requirePositive(quantity, "quantity", lineNo);
                        requirePositive(price, "price", lineNo);
                        LocalDateTime when = LocalDateTime.parse(p[6]);
                        requireUser(userRepository, p[1], lineNo)
                                .recordTransaction(new Transaction(type, p[3], quantity, price, when));
                    }
                    default -> throw new StorageException("Line " + lineNo + ": unknown record type '" + p[0] + "'.");
                }
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw new StorageException("Line " + lineNo + ": invalid value (" + e.getMessage() + ").", e);
            }
        }
    }

    private static void expectFields(String[] parts, int expected, int lineNo) {
        if (parts.length != expected) {
            throw new StorageException("Line " + lineNo + ": expected " + expected
                    + " fields but found " + parts.length + ".");
        }
    }

    private static void requirePositive(double value, String what, int lineNo) {
        if (!(value > 0)) {
            throw new StorageException("Line " + lineNo + ": " + what + " must be greater than zero.");
        }
    }

    private static void requireNonNegative(double value, String what, int lineNo) {
        if (!(value >= 0)) {
            throw new StorageException("Line " + lineNo + ": " + what + " cannot be negative.");
        }
    }

    private static User requireUser(UserRepository userRepository, String userId, int lineNo) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new StorageException("Line " + lineNo + ": refers to unknown user '" + userId + "'."));
    }


    public Path backupCorruptFile() {
        Path backup = file.resolveSibling(file.getFileName() + ".corrupt");
        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            return backup;
        } catch (IOException e) {
            throw new StorageException("Could not back up unreadable data file: " + e.getMessage(), e);
        }
    }
}
