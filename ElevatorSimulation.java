import java.util.*;

/**
 * Simple elevator simulation (floors 1..10).
 * Commands:
 *   call <floor>      // hall call from a floor
 *   select <floor>    // passenger presses floor in the car
 *   step [n]          // advance time by 1 (or n) ticks
 *   status            // show elevator state
 *   help              // show commands
 *   quit
 */
public class ElevatorSimulation {
    public static void main(String[] args) {
        Elevator e = new Elevator(10); // change max floors here
        Scanner sc = new Scanner(System.in);
        System.out.println("Elevator sim started. Type 'help' for commands.");
        e.printStatus();

        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "help":
                        System.out.println("""
                            Commands:
                              call <floor>       - hall call at floor
                              select <floor>     - choose floor inside car
                              step [n]           - advance time by 1 (or n) ticks
                              status             - show state
                              quit               - exit
                            """);
                        break;
                    case "call":
                    case "select": {
                        if (parts.length < 2) {
                            System.out.println("Need a floor number.");
                            break;
                        }
                        int f = Integer.parseInt(parts[1]);
                        if (cmd.equals("call")) {
                            e.addRequest(f);
                        } else {
                            e.addRequest(f);
                        }
                        break;
                    }
                    case "step": {
                        int n = 1;
                        if (parts.length >= 2) n = Math.max(1, Integer.parseInt(parts[1]));
                        for (int i = 0; i < n; i++) e.step();
                        break;
                    }
                    case "status":
                        e.printStatus();
                        break;
                    case "quit":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Unknown command. Type 'help'.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number.");
            }
        }
    }
}

class Elevator {
    enum Direction { UP, DOWN, IDLE }

    private final int minFloor = 1;
    private final int maxFloor;
    private int currentFloor = 1;
    private Direction dir = Direction.IDLE;

    // Requests ahead of us:
    private final TreeSet<Integer> upRequests = new TreeSet<>();    // higher floors
    private final TreeSet<Integer> downRequests = new TreeSet<>();  // lower floors

    // Door is "instant" here; you can extend to add open/close timing if desired.
    private boolean doorsOpen = false;

    Elevator(int maxFloor) {
        this.maxFloor = Math.max(2, maxFloor);
    }

    public void addRequest(int floor) {
        if (floor < minFloor || floor > maxFloor) {
            System.out.printf("Floor %d out of range [%d..%d].%n", floor, minFloor, maxFloor);
            return;
        }
        if (floor == currentFloor) {
            // Serve immediately
            System.out.printf("Serving floor %d now (already here). Doors opening...%n", floor);
            doorsOpen = true;
            // Close immediately for simplicity
            doorsOpen = false;
            // Keep direction unchanged
            return;
        }

        if (floor > currentFloor) {
            upRequests.add(floor);
        } else {
            downRequests.add(floor);
        }
        if (dir == Direction.IDLE) {
            dir = (floor > currentFloor) ? Direction.UP : Direction.DOWN;
        }
        printStatus();
    }

    public void step() {
        // If no requests, stay idle
        if (upRequests.isEmpty() && downRequests.isEmpty()) {
            dir = Direction.IDLE;
            doorsOpen = false;
            System.out.println(tickPrefix() + "No requests. Standing by.");
            return;
        }

        // If going up but nothing above, consider switching
        if (dir == Direction.UP && upRequests.isEmpty()) {
            dir = !downRequests.isEmpty() ? Direction.DOWN : Direction.IDLE;
        }
        // If going down but nothing below, consider switching
        if (dir == Direction.DOWN && downRequests.isEmpty()) {
            dir = !upRequests.isEmpty() ? Direction.UP : Direction.IDLE;
        }

        // Move one floor in the decided direction
        switch (dir) {
            case UP -> moveUpOne();
            case DOWN -> moveDownOne();
            case IDLE -> {
                // Shouldn't hit often; try to pick a direction based on nearest request
                pickDirectionTowardNearest();
                System.out.println(tickPrefix() + "Picking direction: " + dir);
            }
        }
    }

    private void moveUpOne() {
        if (currentFloor < maxFloor) currentFloor++;
        System.out.println(tickPrefix() + "Moving UP to " + currentFloor);
        arriveAndServeIfNeeded();
        // If nothing else above, maybe switch next tick
        if (upRequests.isEmpty() && !downRequests.isEmpty() && dir == Direction.UP) {
            System.out.println(tickPrefix() + "No more up requests; will switch to DOWN soon.");
        }
    }

    private void moveDownOne() {
        if (currentFloor > minFloor) currentFloor--;
        System.out.println(tickPrefix() + "Moving DOWN to " + currentFloor);
        arriveAndServeIfNeeded();
        // If nothing else below, maybe switch next tick
        if (downRequests.isEmpty() && !upRequests.isEmpty() && dir == Direction.DOWN) {
            System.out.println(tickPrefix() + "No more down requests; will switch to UP soon.");
        }
    }

    private void arriveAndServeIfNeeded() {
        boolean served = false;
        if (upRequests.remove(currentFloor)) served = true;
        if (downRequests.remove(currentFloor)) served = true;

        if (served) {
            doorsOpen = true;
            System.out.println(tickPrefix() + "Arrived at floor " + currentFloor + ". Doors opening...");
            // Close immediately for simplicity
            doorsOpen = false;
            System.out.println(tickPrefix() + "Doors closing.");
        }

        // If we’ve served everything in the current direction, flip if needed
        if (dir == Direction.UP && upRequests.isEmpty() && !downRequests.isEmpty()) {
            dir = Direction.DOWN;
            System.out.println(tickPrefix() + "Switching direction to DOWN.");
        } else if (dir == Direction.DOWN && downRequests.isEmpty() && !upRequests.isEmpty()) {
            dir = Direction.UP;
            System.out.println(tickPrefix() + "Switching direction to UP.");
        } else if (upRequests.isEmpty() && downRequests.isEmpty()) {
            dir = Direction.IDLE;
            System.out.println(tickPrefix() + "All requests done. Going IDLE.");
        }
    }

    private void pickDirectionTowardNearest() {
        if (upRequests.isEmpty() && downRequests.isEmpty()) {
            dir = Direction.IDLE;
            return;
        }
        Integer upNearest = upRequests.isEmpty() ? null : upRequests.ceiling(currentFloor);
        Integer downNearest = downRequests.isEmpty() ? null : downRequests.floor(currentFloor);

        int upDist = upNearest == null ? Integer.MAX_VALUE : Math.abs(upNearest - currentFloor);
        int downDist = downNearest == null ? Integer.MAX_VALUE : Math.abs(currentFloor - downNearest);

        dir = (upDist <= downDist) ? Direction.UP : Direction.DOWN;
    }

    public void printStatus() {
        System.out.printf(
            "[Status] Floor=%d, Dir=%s, Doors=%s, UpQueue=%s, DownQueue=%s%n",
            currentFloor, dir, doorsOpen ? "OPEN" : "CLOSED", upRequests, downRequests
        );
    }

    private String tickPrefix() {
        return "[Tick] ";
        }
}
