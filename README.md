# ELEVATOR SIMULATION (JAVA)

A simple command-line program that simulates the basic logic of an elevator moving between floors.
It supports making floor requests, stepping through time, and showing current status all through commands in the terminal.

# OVERVIEW

The simulation models a single elevator that:
- Serves floors between 1 and 10 (configurable).
- Moves up or down depending on queued requests.
- Does not reverse direction mid-trip — it finishes all requests in its current direction before switching.
- Handles both floor calls (`call`) and in-car selections (`select`).

# FEATURES

- Two queues:
  - upRequests for all floors above current floor.
  - downRequests for all floors below current floor.
- Realistic direction handling:
  - If it’s going down and you press a higher floor, it’ll come back later — not immediately.
- Instant door open/close when serving a floor.
- Simple text-based interface.

# USAGE
```bash

## 1️. COMPILE AND RUN
javac ElevatorSimulation.java
java ElevatorSimulation

## 2️. COMMANDS
---------------------------------------------------------
call <floor>      → Request the elevator to go to that floor (like pressing a hall button)
select <floor>    → Choose a floor from inside the car
step [n]          → Advance simulation time by 1 (or n) ticks, each tick = one floor
status            → Show elevator’s floor, direction, door state, and queues
help              → Show available commands
quit              → Exit the simulation
---------------------------------------------------------

# EXAMPLE SESSION

Elevator sim started. Type 'help' for commands.
[Status] Floor=1, Dir=IDLE, Doors=CLOSED, UpQueue=[], DownQueue=[]

> call 7
[Status] Floor=1, Dir=UP, Doors=CLOSED, UpQueue=[7], DownQueue=[]
> call 3
[Status] Floor=1, Dir=UP, Doors=CLOSED, UpQueue=[7], DownQueue=[3]
> step 4
[Tick] Moving UP to 2
[Tick] Moving UP to 3
[Tick] Arrived at floor 3. Doors opening...
[Tick] Doors closing.
[Tick] Moving UP to 4
[Tick] Moving UP to 5
> status
[Status] Floor=5, Dir=UP, Doors=CLOSED, UpQueue=[7], DownQueue=[]
> quit
Goodbye!
```bash

# BEHAVIOR DETAILS

• Directional persistence:
  Once the elevator starts going up or down, it continues until there are no requests
  left in that direction.

• Late requests:
  If you press a floor that’s already been passed, the elevator won’t reverse.
  It will add to the downQueue, finish current requests first, then switch direction.

• Idle state:
  When no requests remain, the elevator stays idle at its current floor until a new call arrives.

# POSSIBLE EXTENSIONS

- Add door open/close delay
- Support multiple elevators
- Track passengers or capacity
- Create a GUI or animation view

# ASSUMPTIONS

- Building floors range from 1 to 10 by default (modifiable in code).
- Each `step` represents one time tick → elevator moves exactly one floor per tick.
- Doors open and close instantly (no delay modeled).
- Requests for the same floor are ignored once it’s already queued.
- If a floor is requested after being passed, it will only be served when the elevator returns.
- No concept of passengers entering or leaving — purely floor-based simulation.
- Input is assumed valid (no non-integer floors, negative floors, etc.).
- Only one elevator is simulated.
