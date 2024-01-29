# Fabric-Remapper

Fabric-Remapper is a program in Java that remaps jar files with the intermediary mappings to yarn mappings, based on [FabricMod-Remapper](https://github.com/HuntingDev/FabricMod-Remapper).

## Installation

To use Fabric-Remapper, you need to have Java 17 (I'm not sure if other Java versions work) installed on your system.

- To build the project from source, clone this repository and run `gradlew build` in the root directory. The executable jar file will be generated in the `build/libs` folder.
- To download the latest release, go to the [release](https://github.com/MrBubles/Fabric-Remapper/releases/) page and
  download the jar file.

## Usage

To run the program, you have two options:

- GUI: Double-click the jar file. A GUI will appear, where you can select the input and output
  files and the minecraft version.
- CLI: Run `java -jar Fabric-Remapper.jar --input <input jar> --output <output jar> --minecraftVersion <minecraft version>` in the terminal, or run `start.cmd`.

Wait until the program finishes remapping. The output file will be saved in the specified location.
