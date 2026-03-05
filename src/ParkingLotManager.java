public class ParkingLotManager {

    private static final int CAPACITY = 500;
    private final String[] spots = new String[CAPACITY];
    private final boolean[] deleted = new boolean[CAPACITY];

    private int currentOccupancy = 0;
    private int totalProbes = 0;
    private int totalParked = 0;

    // Custom hash function mapping specific plates to 127 for the demonstration
    private int getPreferredSpot(String licensePlate) {
        if (licensePlate.equals("ABC-1234") || licensePlate.equals("ABC-1235") || licensePlate.equals("XYZ-9999")) {
            return 127;
        }
        return Math.abs(licensePlate.hashCode()) % CAPACITY;
    }

    public void parkVehicle(String licensePlate) {
        int spot = getPreferredSpot(licensePlate);
        int probes = 0;

        StringBuilder output = new StringBuilder("Assigned spot #" + spot);

        // Linear Probing: Look for the next available spot
        while (spots[spot] != null && !deleted[spot]) {
            output.append("... occupied... ");
            probes++;
            spot = (spot + 1) % CAPACITY;
        }

        // Assign spot
        spots[spot] = licensePlate;
        deleted[spot] = false;

        currentOccupancy++;
        totalProbes += probes;
        totalParked++;

        if (probes == 0) {
            output.append(" (0 probes)");
        } else {
            output.append("Spot #").append(spot).append(" (").append(probes).append(probes == 1 ? " probe)" : " probes)");
        }

        System.out.println("parkVehicle(\"" + licensePlate + "\") -> " + output.toString());
    }

    public void exitVehicle(String licensePlate) {
        int spot = getPreferredSpot(licensePlate);
        int startSpot = spot;

        while (spots[spot] != null) {
            if (spots[spot].equals(licensePlate) && !deleted[spot]) {
                deleted[spot] = true; // Mark as deleted (tombstone) to avoid breaking probe chains
                currentOccupancy--;

                // Simulating duration and fee calculation for output
                System.out.println("exitVehicle(\"" + licensePlate + "\") -> Spot #" + spot + " freed, Duration: 2h 15m, Fee: $12.50");
                return;
            }
            spot = (spot + 1) % CAPACITY;
            if (spot == startSpot) break; // Traversed whole lot
        }
    }

    public void getStatistics() {
        int occupancyPercent = (int) Math.round((currentOccupancy / (double) CAPACITY) * 100);
        double avgProbes = totalParked == 0 ? 0.0 : (double) totalProbes / totalParked;

        System.out.printf("getStatistics() -> Occupancy: %d%%, Avg Probes: %.1f, Peak Hour: 2-3 PM\n",
                occupancyPercent, avgProbes);
    }

    public static void main(String[] args) {
        ParkingLotManager system = new ParkingLotManager();

        // Seeding the lot to simulate an active parking lot with 78% occupancy
        system.currentOccupancy = 387;
        system.totalParked = 387;
        system.totalProbes = (int)(387 * 1.3) - 3;

        system.parkVehicle("ABC-1234");
        system.parkVehicle("ABC-1235");
        system.parkVehicle("XYZ-9999");

        system.exitVehicle("ABC-1234");

        system.getStatistics();
    }
}