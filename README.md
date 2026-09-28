# CodeAlpha_StockTradingPlatform

A console-based Java stock trading simulator with **file-based persistence** — built for the **CodeAlpha Java Programming Internship (Task 2)**.

Simulates a basic trading environment: market data, buying and selling stocks, and portfolio performance tracking over time. Your portfolio, cash, market prices, and full trade history are saved automatically and restored the next time you launch the app.

---

## Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Data Persistence (File I/O)](#data-persistence-file-io)
- [Example Session](#example-session)
- [Validation & Error Handling](#validation--error-handling)
- [Possible Future Improvements](#possible-future-improvements)
- [Author](#author)

---

## Features
- 📈 View market prices for 7 preloaded stocks
- 💰 Buy and sell shares with a real cash balance that is enforced
- 🧮 Weighted average cost basis — buying more of a stock you already own correctly blends the purchase price
- 📊 Portfolio view: quantity, average buy price, current price, current value, and profit/loss per holding
- 🕒 Full transaction history with timestamps
- 🎲 "Simulate market movement" — advances prices randomly (±5%) so you can watch your portfolio value change over time
- 💾 **Automatic saving** — every buy, sell, and market move is written to disk; quit and relaunch to pick up exactly where you left off
- 🛡 Robust validation: can't overspend, can't oversell, can't trade an unknown stock, and bad input or a damaged data file never crashes the app
  - 🌍 Includes sample stocks related to both international and Ethiopian companies

## Architecture
Layered architecture — each class has one job:

```
┌──────────────────────┐
│  UI Layer              │  ConsoleUI — talks to the user only
├──────────────────────┤
│  Service Layer         │  TradingService — all trading rules
├──────────────────────┤
│  Model Layer           │  Stock, User, Holding, Transaction
├──────────────────────┤
│  Repository Layer      │  MarketRepository, UserRepository (in-memory)
│                        │  DataStorage  ◄── FileDataStorage (disk)
└──────────────────────┘
```

**Key design decisions:**
- **Storage is behind an interface.** `TradingService` depends on the `DataStorage` interface, never on files directly. `FileDataStorage` is one implementation; a database-backed one could replace it without changing any business logic.
- **`Transaction` is immutable** — once a trade happens its record can never be edited, which is what makes the history trustworthy. It also has a second constructor so a reloaded trade keeps its *original* timestamp.
- **`Transaction.Type` is an enum** (`BUY`/`SELL`), not a raw string, so an invalid trade type cannot even compile.
- **`Holding` owns its own average-cost math**, keeping the logic next to the data it belongs to.
- **Repositories only store data**; every rule (funds check, share check, price lookup) lives in `TradingService`.

## Project Structure
```
CodeAlpha_StockTradingPlatform/
├── src/com/codealpha/stocktrading/
│   ├── model/
│   │   ├── Stock.java
│   │   ├── User.java
│   │   ├── Holding.java
│   │   └── Transaction.java
│   ├── repository/
│   │   ├── MarketRepository.java
│   │   ├── UserRepository.java
│   │   ├── DataStorage.java          (interface)
│   │   ├── FileDataStorage.java      (file implementation)
│   │   └── StorageException.java
│   ├── service/
│   │   └── TradingService.java
│   ├── ui/
│   │   └── ConsoleUI.java
│   └── Main.java
├── data/                              (created on first run, git-ignored)
│   └── trading_data.txt
├── .gitignore
└── README.md
```

## How to Run
Requires **JDK 17 or later**.

```bash
# 1. Clone the repo
git clone https://github.com/<your-username>/CodeAlpha_StockTradingPlatform.git
cd CodeAlpha_StockTradingPlatform

# 2. Compile
find src -name "*.java" > sources.txt
javac -d bin @sources.txt

# 3. Run
java -cp bin com.codealpha.stocktrading.Main

# (optional) use a custom data file location
java -cp bin com.codealpha.stocktrading.Main path/to/my_data.txt
```

A new account starts with **$10,000** in simulated cash and 5 preloaded stocks: AAPL, GOOG, TSLA, ABAYB, TELE, BOAX, WGBX.

## Data Persistence (File I/O)

On startup the app looks for `data/trading_data.txt`:
- **Found** → the saved account is loaded ("Loaded saved data from ...").
- **Not found** → a new account is created and the file is written straight away.

After every buy, sell, and market simulation, the full state is saved again.

**File format** — plain, human-readable text, one record per line, fields separated by `|`:

```
# CodeAlpha Stock Trading Platform data file (v1)
STOCK|AAPL|Apple Inc.|186.2263193942208
USER|trader1|Trader|8158.678958182662
HOLDING|trader1|AAPL|7|190.0
TXN|trader1|BUY|AAPL|10|190.0|2026-09-28T17:40:43.275053937
TXN|trader1|SELL|AAPL|3|186.2263193942208|2026-09-28T17:40:43.302932988
```

| Record | Fields |
|---|---|
| `STOCK` | symbol, name, current price |
| `USER` | user id, name, cash balance |
| `HOLDING` | user id, symbol, quantity, average buy price |
| `TXN` | user id, `BUY`/`SELL`, symbol, quantity, price per share, timestamp |

**Reliability details:**
- **Safe writes** — data is written to a temporary file and then swapped in, so a crash mid-save can never leave a half-written data file.
- **Strict loading** — every line is validated (correct field count, numbers parse, quantities and prices positive, holdings belong to a known user). A bad line reports its exact line number.
- **Corrupt files are never destroyed** — if a file can't be read, it is renamed to `trading_data.txt.corrupt` and a fresh account starts, so nothing is silently overwritten.
- **Save failures don't crash the app** — if the disk is unwritable, the app tells you the action succeeded but was *not saved*, and keeps running.

## Example Session
```
No saved data found - starting a new account.
=== CodeAlpha Stock Trading Platform ===

Cash balance: $10000.00
1. View market prices
2. Buy stock
3. Sell stock
4. View portfolio
5. View transaction history
6. Simulate market movement (advance a day)
7. Exit
Choose an option: 2
Symbol: AAPL
Quantity: 10
Bought 10 shares of AAPL.

Choose an option: 7
Goodbye!
```
*...relaunch the app later:*
```
Loaded saved data from /home/you/CodeAlpha_StockTradingPlatform/data/trading_data.txt

Cash balance: $8100.00
Choose an option: 4
=== Portfolio ===
AAPL   qty=10    avgBuy=$190.00   current=$190.00   value=$1900.00    P/L=$0.00
Cash: $8100.00
Total portfolio value: $10000.00
```

## Validation & Error Handling
| Scenario | Behavior |
|---|---|
| Buying with insufficient cash | Rejected, exact shortfall shown |
| Selling more shares than owned | Rejected, current holding shown |
| Trading an unknown symbol | Rejected with a clear message |
| Non-numeric quantity | Caught and reported, no crash |
| Buying more of a stock already held | Weighted average buy price recalculated |
| Selling all shares of a stock | Holding removed from the portfolio *and* from the saved file |
| Viewing portfolio with no holdings | Friendly "No holdings yet" message |
| Data file is corrupt / has bad values | Exact line reported, file backed up as `.corrupt`, new account started |
| Data file location not writable | Warning shown, app keeps running, trades still work in memory |
| No data file yet (first launch) | New account created automatically |

## Possible Future Improvements
- Swap `FileDataStorage` for a database (SQLite/JDBC) — only a new `DataStorage` implementation is needed
- Support multiple users with login
- Real market data via a stock price API instead of random simulation
- Portfolio value history and charts over time
- Export transaction history to CSV

## Author
Built as part of the **CodeAlpha Java Programming Internship** — Task 2: Stock Trading Platform.
