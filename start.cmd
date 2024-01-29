@echo off
set /p input="Enter the path to the input file: "
set /p output="Enter the path to the output file: "
set /p minecraftVersion="Enter the minecraft version: "

java -jar Fabric-Remapper.jar --input %input% --output %output% --minecraftVersion %minecraftVersion%
pause
