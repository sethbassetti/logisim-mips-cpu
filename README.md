# README

[Logisim](http://www.cburch.com/logisim/index.html) components for implementing a 16-bit MIPS computer to be used in USU's CS2810 course.

## Dependencies
The only external dependency is `logisim.jar`. Since it's not available in the maven repos we need to add it manually before the first build:

    mvn install:install-file -Dfile=${PATH_TO_LOGISIM}/logisim.jar -DgroupId=com.cbrunch -DartifactId=logisim -Dversion=2.7.1 -Dpackaging=jar

This needs to be executed just once.

## Build
This project uses [maven](https://maven.apache.org/).

To build it, use the following command:

    mvn package

The jar file is in the `target` directory.

## Usage
Open Logisim and load the jar file from the Project -> Load Library -> JAR Library... menu.

## Components

When you load the library in Logisim (Project → Load Library → JAR Library… and select the jar file in `target/`), you'll see **AggieMIPS Components** with:

### ALU
* A (WIDTH-bit input) — first operand.
* B (WIDTH-bit input) — second operand.
* OP (3-bit input) — operation selector:
  - `000`: AND
  - `001`: OR
  - `010`: ADD
  - `100`: ANDN
  - `101`: ORN
  - `110`: SUB
  - `111`: SLT
* Zero (1-bit output) — high when result equals zero.
* Out (WIDTH-bit output) — ALU result.

### Inc16
* In (WIDTH-bit input) — value to increment.
* Out (WIDTH-bit output) — value plus one.

### Screen
* I (16-bit input) — segment data.
* A (13-bit input) — segment address.
* L (1-bit input) — load enable.
* CK (1-bit input) — clock (rising edge).
* O (16-bit output) — data out.
* Display (1-bit input) — render signal.

### RegisterFile
* Read Register 1 (3-bit input)
* Read Register 2 (3-bit input)
* Write Register (3-bit input)
* Write Data (16-bit input)
* Write Enable (1-bit input)
* Read Data 1 (16-bit output)
* Read Data 2 (16-bit output)
* Clock (1-bit input)
* Reset (1-bit input)

### Control
* OpCode (4-bit input)
* Funct (3-bit input)
* bne, beq, j, jal, jr, MemtoReg, MemWrite, RegWrite, ALUSrc, RegDst (1-bit outputs)
* ALUOp (3-bit output)

### CPU
* Instruction (16-bit input)
* MemData (16-bit input)
* Reset (1-bit input)
* Clock (1-bit input)
* MemWriteData (16-bit output)
* MemAddress (15-bit output)
* MemWrite (1-bit output)
* PC (15-bit output)
* Display (1-bit output)

### Keyboard
* KeyCode (16-bit output) — ASCII code of the pressed key (0 if none).

## Author

Seth Bassetti
