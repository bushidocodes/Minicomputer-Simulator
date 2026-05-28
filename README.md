# Minicomputer Simulator

A Java/Swing simulator for the CSCI 6461 minicomputer architecture — a 16-bit
machine with 4 general-purpose registers, 3 index registers, 2 floating-point
registers, a small instruction set, memory-mapped I/O, vector and FPU support,
and a software-managed trap table.

Originally built by "Team Yellow" for CSCI 6461 at GWU.

## Requirements

- JDK 21 or newer (the Gradle build uses a Java 21 toolchain — Gradle will
  download one automatically if you don't have it).

That's it. The Gradle wrapper bootstraps everything else.

## Build & run

From the project root:

```sh
# Linux / macOS
./gradlew run

# Windows (PowerShell or cmd)
.\gradlew.bat run
```

This compiles the sources, instruments the IntelliJ GUI form, and launches the
Swing front panel.

To produce a runnable JAR:

```sh
./gradlew jar
# Output: build/libs/minicomputer-simulator.jar
# Must be run from the project root so the simulator can find ./static.
java -jar build/libs/minicomputer-simulator.jar
```

## Using the simulator

The interface mimics a front-panel minicomputer console:

- **IPL** — Initial Program Load. Boots the ROM (trap table, fault handler,
  bootloader, and built-in subroutines like `print-int`, `read-string`).
- **Load Program 1 / 2** — Assemble and load one of the bundled demo programs.
- **Load Instruction Demo / Floating-Point Demo / Vector Demo** — Quick demos
  for various instruction classes.
- **Choose File** then **Load Program** — Assemble an arbitrary `.txt` program
  from `static/` (or anywhere on disk) into memory at a chosen address.
- **SS** — Single-step the next instruction.
- **RUN** — Execute until halt, fault, or I/O wait.
- **HALT** — Stop a running program.
- **RESET** — Wipe registers and memory.

The two consoles on the right are the **operator console** (program I/O — text
that programs read/write via `IN`/`OUT`) and the **field engineer console**
(simulator diagnostics).

The card-reader pane lets you load a "punched card" deck (a text file of
newline-terminated records) for programs that read input via the card reader.

## Source layout

```
src/com/simulator/awesome/   Simulator core + Swing UI
  Simulator.java               Top-level machine state (registers, subsystems)
  ControlUnit.java             Fetch/decode/execute loop
  Memory.java, Cache.java      Memory + cache
  ArithmeticLogicUnit.java     Integer ALU
  FloatingPointUnit.java       FPU
  *Instruction.java            Instruction-class implementations
  Assembler.java               Two-pass assembler for the .txt program format
  ReadOnlyMemory.java          Loads trap table and built-in routines on IPL
  InputOutput.java             Console keyboard/printer + card reader buffers
  Interface.java / .form       Swing front panel (IntelliJ GUI Designer)
  Main.java                    Entry point

static/                      Demo programs, ROM payloads, indicator-light PNGs
```

## Build internals

The Swing UI is defined in `Interface.form`, an IntelliJ GUI Designer file
that gets woven into `Interface.class` at compile time. The Gradle build
delegates to IntelliJ's published `java-compiler-ant-tasks` (Javac2) to do the
instrumentation, so the form keeps working without IntelliJ being installed.

## License

No license has been declared. Treat as all rights reserved by the original
authors unless they say otherwise.
