import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    private final Map<String, TokenBucket> clients = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowSizeMs;

    public RateLimiter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
    }

    class TokenBucket {
        int tokens;
        long resetTime;

        TokenBucket() {
            this.tokens = maxRequests;
            this.resetTime = System.currentTimeMillis() + windowSizeMs;
        }

        synchronized boolean consume() {
            long now = System.currentTimeMillis();
            if (now >= resetTime) {
                tokens = maxRequests;
                resetTime = now + windowSizeMs;
            }
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        synchronized long getResetTime() {
            return resetTime;
        }

        synchronized int getTokens() {
            return tokens;
        }
    }

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = clients.computeIfAbsent(clientId, k -> new TokenBucket());
        boolean allowed = bucket.consume();

        if (allowed) {
            return "Allowed (" + bucket.getTokens() + " requests remaining)";
        } else {
            long retryAfterSeconds = (bucket.getResetTime() - System.currentTimeMillis()) / 1000;
            return "Denied (0 requests remaining, retry after " + Math.max(1, retryAfterSeconds) + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clients.get(clientId);
        if (bucket == null) {
            long defaultReset = (System.currentTimeMillis() + windowSizeMs) / 1000;
            return "{used: 0, limit: " + maxRequests + ", reset: " + defaultReset + "}";
        }

        synchronized(bucket) {
            int used = maxRequests - bucket.getTokens();
            long resetSeconds = bucket.getResetTime() / 1000;
            return "{used: " + used + ", limit: " + maxRequests + ", reset: " + resetSeconds + "}";
        }
    }

    public static void main(String[] args) {
        // 1000 requests per 1 hour (3600000 ms)
        RateLimiter gateway = new RateLimiter(1000, 3600 * 1000L);

        System.out.println("checkRateLimit(clientId=\"abc123\") -> " + gateway.checkRateLimit("abc123"));
        System.out.println("checkRateLimit(clientId=\"abc123\") -> " + gateway.checkRateLimit("abc123"));

        // Simulating a burst of traffic that exhausts the limit
        for (int i = 0; i < 998; i++) {
            gateway.checkRateLimit("abc123");
        }

        System.out.println("checkRateLimit(clientId=\"abc123\") -> " + gateway.checkRateLimit("abc123"));
        System.out.println("getRateLimitStatus(\"abc123\") -> " + gateway.getRateLimitStatus("abc123"));
    }
}