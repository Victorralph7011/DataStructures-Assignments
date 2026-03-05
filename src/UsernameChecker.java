import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameChecker {

    // HashMaps for O(1) instant lookup and thread-safe concurrent checks
    private Map<String, Integer> takenUsernames = new ConcurrentHashMap<>();
    private Map<String, Integer> attemptFrequency = new ConcurrentHashMap<>();

    public UsernameChecker() {
        // Initializing sample data
        takenUsernames.put("john_doe", 101);
        takenUsernames.put("admin", 102);
        attemptFrequency.put("admin", 10543);
    }

    public boolean checkAvailability(String username) {
        if (takenUsernames.containsKey(username)) {
            // Track popularity of taken usernames
            attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
            return false;
        }
        return true;
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int suffix = 1;

        // Suggest by appending numbers
        while (suggestions.size() < 2) {
            String suggestion = username + suffix;
            if (!takenUsernames.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
            suffix++;
        }

        // Suggest by modifying characters
        String dotSuggestion = username.replace("_", ".");
        if (!takenUsernames.containsKey(dotSuggestion) && !dotSuggestion.equals(username)) {
            suggestions.add(dotSuggestion);
        }

        return suggestions;
    }

    public String getMostAttempted() {
        String mostAttempted = null;
        int maxAttempts = -1;

        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxAttempts) {
                maxAttempts = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }
        return mostAttempted != null ? mostAttempted + " (" + maxAttempts + " attempts)" : "None";
    }

    public static void main(String[] args) {
        UsernameChecker system = new UsernameChecker();

        System.out.println("checkAvailability(\"john_doe\") -> " + system.checkAvailability("john_doe") + " (already taken)");
        System.out.println("checkAvailability(\"jane_smith\") -> " + system.checkAvailability("jane_smith") + " (available)");
        System.out.println("suggestAlternatives(\"john_doe\") -> " + system.suggestAlternatives("john_doe"));

        String mostAttemptedRaw = system.getMostAttempted();
        String name = mostAttemptedRaw.split(" ")[0];
        String attempts = mostAttemptedRaw.split(" ")[1].replace("(", "");
        System.out.println("getMostAttempted() -> \"" + name + "\" (" + attempts + " attempts)");
    }
}