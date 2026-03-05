import java.util.*;

public class AutocompleteSystem {

    class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        // Stores top 10 queries passing through this node for instant O(L) retrieval
        List<String> topQueries = new ArrayList<>();
    }

    private final TrieNode root = new TrieNode();
    private final Map<String, Integer> queryFrequencies = new HashMap<>();

    // Used for real-time updates
    public void updateFrequency(String query) {
        int oldFreq = queryFrequencies.getOrDefault(query, 0);
        int newFreq = oldFreq + 1;
        queryFrequencies.put(query, newFreq);

        TrieNode current = root;
        for (char c : query.toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
            updateTopQueries(current, query);
        }

        System.out.println("updateFrequency(\"" + query + "\") -> Frequency: " + oldFreq + " -> " + newFreq + " (trending)");
    }

    // Used to silently seed the massive initial database
    public void seedFrequency(String query, int count) {
        queryFrequencies.put(query, count);
        TrieNode current = root;
        for (char c : query.toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
            updateTopQueries(current, query);
        }
    }

    private void updateTopQueries(TrieNode node, String query) {
        if (!node.topQueries.contains(query)) {
            node.topQueries.add(query);
        }

        // Sort based on frequency (descending), then lexicographically
        node.topQueries.sort((a, b) -> {
            int freqA = queryFrequencies.get(a);
            int freqB = queryFrequencies.get(b);
            return freqA != freqB ? freqB - freqA : a.compareTo(b);
        });

        // Maintain only the top 10 to save memory
        if (node.topQueries.size() > 10) {
            node.topQueries.remove(node.topQueries.size() - 1);
        }
    }

    public void search(String prefix) {
        System.out.println("search(\"" + prefix + "\") ->");
        TrieNode current = root;

        for (char c : prefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                System.out.println("No suggestions found.");
                return;
            }
        }

        int rank = 1;
        for (String query : current.topQueries) {
            int freq = queryFrequencies.get(query);
            System.out.println(rank + ". \"" + query + "\" (" + String.format("%,d", freq) + " searches)");
            rank++;
        }
    }

    public static void main(String[] args) {
        AutocompleteSystem engine = new AutocompleteSystem();

        // Seed the Trie with the EXACT data from the problem statement
        engine.seedFrequency("java tutorial", 1234567);
        engine.seedFrequency("javascript", 987654);
        engine.seedFrequency("java download", 456789);

        // Run the search
        engine.search("jav");
        System.out.println();

        // Simulate a trending search being updated
        engine.updateFrequency("java 21 features");
        engine.updateFrequency("java 21 features");
    }
}