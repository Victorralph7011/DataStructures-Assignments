import java.util.LinkedHashMap;
import java.util.Map;

public class DNSCache {
    private final int capacity;
    private final Map<String, DNSEntry> cache;
    private int hits = 0;
    private int misses = 0;

    // Custom Entry Class
    class DNSEntry {
        String ipAddress;
        long expiryTime;

        public DNSEntry(String ipAddress, long ttlSeconds) {
            this.ipAddress = ipAddress;
            // Calculate absolute expiry time in milliseconds
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public DNSCache(int capacity) {
        this.capacity = capacity;

        // Implementing LRU Eviction using LinkedHashMap
        // true for access-order (LRU), false for insertion-order
        this.cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        // Background thread to clean expired entries automatically
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Run cleanup every 5 seconds
                    cleanExpiredEntries();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true); // Don't block JVM shutdown
        cleanupThread.start();
    }

    private synchronized void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public synchronized String resolve(String domain) {
        long startTime = System.nanoTime();
        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            double lookupTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
            return "Cache HIT -> " + entry.ipAddress + String.format(" (retrieved in %.1fms)", lookupTimeMs);
        } else {
            misses++;
            String status = (entry != null) ? "Cache EXPIRED" : "Cache MISS";
            cache.remove(domain);

            // Mocking the upstream DNS query
            String newIp = mockUpstreamQuery(domain);
            return status + " -> Query upstream -> " + newIp + " (TTL: 300s)";
        }
    }

    // Helper to simulate fetching from a real DNS server
    private String mockUpstreamQuery(String domain) {
        String ip = "172.217.14." + (int)(Math.random() * 255 + 1);
        cache.put(domain, new DNSEntry(ip, 300)); // Store with 300s TTL
        return ip;
    }

    public synchronized String getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (double) hits / total * 100;
        return String.format("Hit Rate: %.1f%%", hitRate);
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCache dns = new DNSCache(100); // Cache capacity of 100

        System.out.println("resolve(\"google.com\") -> " + dns.resolve("google.com"));
        System.out.println("resolve(\"google.com\") -> " + dns.resolve("google.com"));

        System.out.println("\n... waiting 1 second to simulate time passing ...");
        Thread.sleep(1000);

        System.out.println("resolve(\"amazon.com\") -> " + dns.resolve("amazon.com"));
        System.out.println("getCacheStats() -> " + dns.getCacheStats());
    }
}