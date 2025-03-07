#  Real-Time Stock Trading Engine

##  Overview
This project implements a **real-time stock trading engine** that efficiently matches **buy and sell orders**. The system supports **1,024 stock tickers**, ensures high concurrency using **lock-free data structures**, and executes order matching with an **O(n) time complexity**.

The trading engine guarantees optimal trade execution, ensuring **the highest buy price matches the lowest sell price** for each transaction.

---

##  Features

###  **Order Management**
- Supports adding `BUY` and `SELL` orders for up to **1,024 stock tickers**.
- Each ticker can store up to **10,000 orders**.
- Uses `AtomicReferenceArray` to ensure **thread-safe** order storage.

###  **Real-Time Matching**
- Orders are matched when **buy price ≥ lowest sell price**.
- Implements **O(n) order matching** using **sorting and dual-pointer traversal** for efficiency.
- Ensures **highest buy price is matched with the lowest sell price** to optimize trade execution.

###  **Multi-Threading & High Concurrency**
- Simulates real-time trading with **multiple threads for buyers and sellers**.
- Uses **lock-free data structures** (`AtomicReferenceArray`, `AtomicInteger`) for **fast, thread-safe execution**.
- Avoids **deadlocks and race conditions** in concurrent environments.

###  **Optimized Performance**
- **No dictionaries or maps used**, ensuring **constant-time access** to order storage.
- Orders are **sorted before matching**, allowing **efficient execution**.
- **Prevents infinite loops** by handling edge cases in order matching.

---

##  Example Output
```sh
Matched: AAPL -> 5 shares at $97
Matched: AAPL -> 10 shares at $100
Matched: AAPL -> 3 shares at $98
