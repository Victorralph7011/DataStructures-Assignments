import java.util.*;

public class TransactionAnalyzer {

    // Inner class representing a single financial transaction
    static class Transaction {
        int id;
        int amount;
        String merchant;
        String time;
        String account;

        public Transaction(int id, int amount, String merchant, String time, String account) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.time = time;
            this.account = account;
        }
    }

    // 1. Classic Two-Sum adapted for transaction complement lookup in O(n) time
    public String findTwoSum(List<Transaction> transactions, int target) {
        Map<Integer, Integer> seenAmounts = new HashMap<>(); // Maps amount -> transaction ID
        List<String> results = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (seenAmounts.containsKey(complement)) {
                results.add("{id:" + seenAmounts.get(complement) + ", id:" + t.id + "}");
            }
            seenAmounts.put(t.amount, t.id);
        }
        return "[" + String.join(", ", results) + "]";
    }

    // 2. Duplicate Detection using composite keys
    public String detectDuplicates(List<Transaction> transactions) {
        // Key: "amount|merchant", Value: Set of accounts that made the transaction
        Map<String, Set<String>> transactionGroups = new HashMap<>();
        List<String> duplicates = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "|" + t.merchant;
            transactionGroups.putIfAbsent(key, new LinkedHashSet<>());
            transactionGroups.get(key).add(t.account);
        }

        // Filter and format the duplicates
        for (Map.Entry<String, Set<String>> entry : transactionGroups.entrySet()) {
            if (entry.getValue().size() > 1) { // Same amount/merchant, multiple different accounts
                String[] parts = entry.getKey().split("\\|");
                String amount = parts[0];
                String merchant = parts[1];
                List<String> accounts = new ArrayList<>(entry.getValue());

                duplicates.add("{amount:" + amount + ", merchant:\"" + merchant + "\", accounts:[" + String.join(", ", accounts) + "]}");
            }
        }
        return "[" + String.join(", ", duplicates) + "]";
    }

    // 3. K-Sum using Backtracking
    public String findKSum(List<Transaction> transactions, int k, int target) {
        List<List<Integer>> results = new ArrayList<>();
        backtrack(transactions, k, target, 0, new ArrayList<>(), results);

        if (results.isEmpty()) return "[]";

        // Format the first match to keep the output clean
        List<Integer> match = results.get(0);
        StringBuilder sb = new StringBuilder("[{");
        for (int i = 0; i < match.size(); i++) {
            sb.append("id:").append(match.get(i));
            if (i < match.size() - 1) sb.append(", ");
        }
        sb.append("}]");
        return sb.toString();
    }

    private void backtrack(List<Transaction> txns, int k, int target, int start, List<Integer> current, List<List<Integer>> results) {
        if (k == 0 && target == 0) {
            results.add(new ArrayList<>(current));
            return;
        }
        if (k == 0 || target < 0) return;

        for (int i = start; i < txns.size(); i++) {
            current.add(txns.get(i).id);
            backtrack(txns, k - 1, target - txns.get(i).amount, i + 1, current, results);
            current.remove(current.size() - 1); // Backtrack
        }
    }

    public static void main(String[] args) {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();
        List<Transaction> transactions = new ArrayList<>();

        // Seeding the exact mock data from the requirements
        transactions.add(new Transaction(1, 500, "Store A", "10:00", "acc1"));
        transactions.add(new Transaction(2, 300, "Store B", "10:15", "acc3"));
        transactions.add(new Transaction(3, 200, "Store C", "10:30", "acc4"));

        // Adding an extra transaction to trigger the duplicate detection requirement
        transactions.add(new Transaction(4, 500, "Store A", "10:45", "acc2"));

        System.out.println("findTwoSum(target=500) -> " + analyzer.findTwoSum(transactions, 500) + " // 300 + 200");
        System.out.println("detectDuplicates() -> " + analyzer.detectDuplicates(transactions));
        System.out.println("findKSum(k=3, target=1000) -> " + analyzer.findKSum(transactions, 3, 1000) + " // 500+300+200");
    }
}