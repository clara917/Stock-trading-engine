import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

class Order {
    enum Type { BUY, SELL }

    Type type;
    String ticker;
    int quantity;
    int price;
    volatile boolean matched = false; // Flag to indicate if the order has been matched

    public Order(Type type, String ticker, int quantity, int price) {
        this.type = type;
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
    }
}

class StockTradingEngine {
    private static final int MAX_TICKERS = 1024; // Maximum number of stock tickers supported
    private static final int MAX_ORDERS = 10000; // Maximum orders per ticker
    private AtomicReferenceArray<AtomicReferenceArray<Order>> orderBook; // Order book stored in an array
    private AtomicInteger[] orderCount; // Order count for each ticker

    public StockTradingEngine() {
        orderBook = new AtomicReferenceArray<>(MAX_TICKERS);
        orderCount = new AtomicInteger[MAX_TICKERS];

        for (int i = 0; i < MAX_TICKERS; i++) {
            orderBook.set(i, new AtomicReferenceArray<>(MAX_ORDERS));
            orderCount[i] = new AtomicInteger(0);
        }
    }

    // Maps a ticker symbol to an index in the order book
    private int getTickerIndex(String ticker) {
        return Math.abs(ticker.hashCode()) % MAX_TICKERS;
    }

    // Adds an order to the order book
    public void addOrder(Order.Type type, String ticker, int quantity, int price) {
        int index = getTickerIndex(ticker);
        int pos = orderCount[index].getAndIncrement();

        if (pos >= MAX_ORDERS) {
            System.out.println("Order book for " + ticker + " is full.");
            return;
        }

        Order order = new Order(type, ticker, quantity, price);
        orderBook.get(index).set(pos, order);
    }

    // Matches buy and sell orders for a given ticker
    public void matchOrders(String ticker) {
        int index = getTickerIndex(ticker);
        AtomicReferenceArray<Order> orders = orderBook.get(index);
        int count = orderCount[index].get();

        // Temporary arrays for buy and sell orders
        Order[] buyOrders = new Order[count];
        Order[] sellOrders = new Order[count];
        int buySize = 0, sellSize = 0;

        // Separate orders into buy and sell lists
        for (int i = 0; i < count; i++) {
            Order order = orders.get(i);
            if (order != null && !order.matched) {
                if (order.type == Order.Type.BUY) buyOrders[buySize++] = order;
                else sellOrders[sellSize++] = order;
            }
        }

        // Sort buy orders in descending price order (higher price first)
        java.util.Arrays.sort(buyOrders, 0, buySize, (a, b) -> Integer.compare(b.price, a.price));
        // Sort sell orders in ascending price order (lower price first)
        java.util.Arrays.sort(sellOrders, 0, sellSize, (a, b) -> Integer.compare(a.price, b.price));

        int buyIndex = 0, sellIndex = 0;

        // Matching process
        while (buyIndex < buySize && sellIndex < sellSize) {
            Order buyOrder = buyOrders[buyIndex];
            Order sellOrder = sellOrders[sellIndex];

            // Buy order matches sell order if buy price >= sell price
            if (buyOrder.price >= sellOrder.price) {
                int matchedQuantity = Math.min(buyOrder.quantity, sellOrder.quantity);

                System.out.println("Matched: " + buyOrder.ticker + " -> " + matchedQuantity + " shares at $" + sellOrder.price);

                // Adjust remaining quantity
                buyOrder.quantity -= matchedQuantity;
                sellOrder.quantity -= matchedQuantity;

                // Mark orders as matched if fully executed
                if (buyOrder.quantity == 0) buyOrder.matched = true;
                if (sellOrder.quantity == 0) sellOrder.matched = true;

                if (buyOrder.matched) buyIndex++;
                if (sellOrder.matched) sellIndex++;
            } else {
                break; // No further matching possible
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        StockTradingEngine engine = new StockTradingEngine();

        // Simulate real-time trading with multiple threads
        Thread buyer = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                engine.addOrder(Order.Type.BUY, "AAPL", (int) (Math.random() * 10) + 1, 100 + (int) (Math.random() * 10));
            }
        });

        Thread seller = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                engine.addOrder(Order.Type.SELL, "AAPL", (int) (Math.random() * 10) + 1, 95 + (int) (Math.random() * 10));
            }
        });

        buyer.start();
        seller.start();

        try {
            buyer.join();
            seller.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Execute order matching
        engine.matchOrders("AAPL");
    }
}
