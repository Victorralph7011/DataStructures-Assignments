import java.util.*;

public class FlashSaleManager {
    // Stores product ID -> current stock count
    private Map<String, Integer> inventory = new HashMap<>();

    // Stores product ID -> Queue of User IDs (waiting list - FIFO)
    private Map<String, LinkedHashSet<Integer>> waitingLists = new HashMap<>();

    public FlashSaleManager() {
        // Initialize with 100 units as per requirements
        inventory.put("IPHONE15_256GB", 100);
    }

    // O(1) instant stock lookup
    public int checkStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }

    // Synchronized to handle concurrent requests safely
    public synchronized String purchaseItem(String productId, int userId) {
        int currentStock = checkStock(productId);

        if (currentStock > 0) {
            // Decrement stock atomically
            inventory.put(productId, currentStock - 1);
            return "Success, " + (currentStock - 1) + " units remaining";
        } else {
            // Add to waiting list (LinkedHashSet preserves insertion order - FIFO)
            waitingLists.putIfAbsent(productId, new LinkedHashSet<>());
            LinkedHashSet<Integer> list = waitingLists.get(productId);
            list.add(userId);

            // Find position in waitlist
            int position = new ArrayList<>(list).indexOf(userId) + 1;
            return "Added to waiting list, position #" + position;
        }
    }

    public static void main(String[] args) {
        FlashSaleManager system = new FlashSaleManager();
        String item = "IPHONE15_256GB";

        System.out.println("checkStock(\"" + item + "\") -> " + system.checkStock(item));

        // Simulating purchases
        System.out.println("purchaseItem(\"" + item + "\", 12345) -> " + system.purchaseItem(item, 12345));
        System.out.println("purchaseItem(\"" + item + "\", 67890) -> " + system.purchaseItem(item, 67890));

        // Simulate stock running out (buying the remaining 98 units)
        for(int i = 0; i < 98; i++) {
            system.purchaseItem(item, i);
        }

        System.out.println("... (after 100 purchases)");
        System.out.println("purchaseItem(\"" + item + "\", 99999) -> " + system.purchaseItem(item, 99999));
    }
}