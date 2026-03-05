import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnalyticsDashboard {

    private final Map<String, Integer> pageViews = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private final Map<String, Integer> trafficSources = new ConcurrentHashMap<>();
    private int totalEvents = 0;

    public synchronized void processEvent(String url, String userId, String source) {
        pageViews.put(url, pageViews.getOrDefault(url, 0) + 1);

        uniqueVisitors.putIfAbsent(url, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(url).add(userId);

        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
        totalEvents++;
    }

    public synchronized void getDashboard() {
        System.out.println("Top Pages:");

        List<Map.Entry<String, Integer>> sortedPages = new ArrayList<>(pageViews.entrySet());
        sortedPages.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int rank = 1;
        for (int i = 0; i < Math.min(10, sortedPages.size()); i++) {
            String url = sortedPages.get(i).getKey();
            int views = sortedPages.get(i).getValue();
            int unique = uniqueVisitors.get(url).size();
            System.out.println(rank + ". " + url + " - " + views + " views (" + unique + " unique)");
            rank++;
        }

        System.out.print("\nTraffic Sources:\n");
        List<String> sourceStats = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            int percentage = (int) Math.round((entry.getValue() * 100.0) / totalEvents);
            String sourceName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1).toLowerCase();
            sourceStats.add(sourceName + ": " + percentage + "%");
        }
        System.out.println(String.join(", ", sourceStats));
    }

    public static void main(String[] args) {
        AnalyticsDashboard dashboard = new AnalyticsDashboard();

        for (int i = 0; i < 15423; i++) dashboard.processEvent("/article/breaking-news", "user_" + (i % 8234), "google");
        for (int i = 0; i < 12091; i++) dashboard.processEvent("/sports/championship", "user_" + (i % 9871), "facebook");
        for (int i = 0; i < 10000; i++) dashboard.processEvent("/local/weather", "user_" + i, "direct");
        for (int i = 0; i < 3300; i++) dashboard.processEvent("/opinion/editorial", "user_" + i, "other");

        dashboard.getDashboard();
    }
}