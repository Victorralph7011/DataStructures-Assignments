import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsernameChecker {

    private Map<String, Integer> takenUsernames = new HashMap<>();
    private Map<String, Integer> attemptFrequency = new HashMap<>();

    public UsernameChecker() {
        takenUsernames.put("john_doe", 101);
        takenUsernames.put("admin", 102);

        attemptFrequency.put("admin", 10543);
    }

    public boolean checkAvailability(String username) {
        if (takenUsernames.containsKey(username)) {
            attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
            return false;
        }
        return true;
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int suffix = 1;

        while (suggestions.size() < 2) {
            String suggestion = username + suffix;
            if (!takenUsernames.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
            suffix++;
        }

        String dotSuggestion = username.replace("_", ".");
        if (!takenUsernames.containsKey(dotSuggestion) && !dotSuggestion.equals(username)) {
            suggestions.add(dotSuggestion);
        }

        return suggestions;
    }

    public String getMostAttempted() {
        String mostAttempted = null;
        int maxAttempts = 0;

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
        System.out.println("getMostAttempted() -> \"" + system.getMostAttempted().split(" ")[0] + "\" (" + system.getMostAttempted().split(" ")[1].replace("(", "") + " attempts)");
    }
}
