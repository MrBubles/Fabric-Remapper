package me.mrbubbles.fabricremapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class Main {

    public static Gui gui;

    public static void main(String[] args) throws Exception {
        if (System.console() != null) {
            start(args);
        } else (gui = new Gui()).start();
    }

    private static void start(String[] args) throws Exception {
        OptionParser optionParser = new OptionParser();

        OptionSpec<String> inputArg = optionParser.accepts("input").withRequiredArg().ofType(String.class);
        OptionSpec<String> outputArg = optionParser.accepts("output").withRequiredArg().ofType(String.class);
        OptionSpec<String> minecraftVersion = optionParser.accepts("minecraftVersion").withRequiredArg().ofType(String.class);

        OptionSet options = optionParser.parse(args);

        Path input = Paths.get(Objects.requireNonNull(getOption(options, inputArg)));
        Path output = Paths.get(Objects.requireNonNull(getOption(options, outputArg)));
        String mcVersion = getOption(options, minecraftVersion);

        remap(input, output, mcVersion);
    }

    public static void print(String message, boolean error) {
        (error ? System.err : System.out).println(message);

        if (gui != null) gui.displayMessage(message, "", error ? JOptionPane.ERROR_MESSAGE : JOptionPane.PLAIN_MESSAGE);
    }

    public static boolean isPathValid(Path path) {
        try {
            return isPathUsable(path) && Files.exists(path) && Files.isReadable(path) && Files.isRegularFile(path) && Files.size(path) > 0;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isPathUsable(Path path) {
        return path != null && !path.toString().isEmpty();
    }

    public static boolean isJar(Path path) {
        if (!isPathValid(path)) return false;

        String name = path.getFileName().toString().toLowerCase();

        return name.endsWith(".jar") || name.endsWith(".zip");
    }

    public static boolean isValidMCVersion(String minecraftVersion) {
        return minecraftVersion != null && !minecraftVersion.isEmpty() && minecraftVersion.matches("^\\d+\\.\\d+(?:\\.\\d+)?$");
    }

    public static void remap(Path input, Path output, String minecraftVersion) throws IOException {
        if (!isJar(input)) {
            print("Input is invalid! Please give a valid input.", true);
            return;
        } else if (!isPathUsable(output)) {
            print("Output is invalid! Please give a valid output.", true);
            return;
        } else if (!isValidMCVersion(minecraftVersion)) {
            minecraftVersion = getMinecraftVersion(input);
            if (!isValidMCVersion(minecraftVersion)) {
                print("Minecraft version is invalid! Please give a valid Minecraft version.", true);
                return;
            }
        }
        Path mappingsPath = YarnDownloading.resolve(minecraftVersion);
        String outputName = output.getFileName().toString();
        int lastIndex = outputName.lastIndexOf('.');

        output = output.resolveSibling(lastIndex == -1 ? outputName + ".jar" : outputName.substring(0, lastIndex) + ".jar");

        if (output.toFile().exists()) output.toFile().delete();

        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(mappingsPath, "intermediary", "named"))
                .renameInvalidLocals(true)
                .rebuildSourceFilenames(true)
                .ignoreConflicts(true)
                .keepInputData(true)
                .skipLocalVariableMapping(true)
                .ignoreFieldDesc(true)
                .build();

        try {
            OutputConsumerPath outputConsumer = new OutputConsumerPath(output);
            outputConsumer.addNonClassFiles(input);
            remapper.readInputs(input);

            remapper.readClassPath(input);
            remapper.apply(outputConsumer);
            remapper.finish();

            outputConsumer.close();
        } catch (IOException e) {
            print("Error during remapping: " + e.getMessage(), true);
        }

        Path mappingsTiny2 = YarnDownloading.resolveTiny2(minecraftVersion);

        try {
            Map<String, String> mapping = RemapUtil.getMappings(mappingsTiny2);

            RemapUtil.remapJar(output, remapper, mapping);
        } catch (IOException e) {
            print("Error during obtaining Tiny v2 mappings: " + e.getMessage(), true);
        }

        Files.delete(mappingsPath);
        if (mappingsTiny2 != null) {
            Files.delete(mappingsTiny2);
        }
        Files.delete(YarnDownloading.path);

        print("Finished remapping '" + input.toFile().getName() + "'!", false);
    }

    private static <T> T getOption(OptionSet optionSet, OptionSpec<T> optionSpec) {
        try {
            return optionSet.valueOf(optionSpec);
        } catch (Throwable throwable) {
            print("Error during getting option: " + throwable.getMessage(), true);
            return null;
        }
    }

    public static String getMinecraftVersion(Path jarPath) {
        String minecraftVersion = null;

        try {
            JarFile jarFile = new JarFile(jarPath.toFile());

            ZipEntry jsonEntry = jarFile.getEntry("fabric.mod.json");

            if (jsonEntry != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jsonEntry)));

                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("\"minecraft\"")) {
                        minecraftVersion = line.split(":")[1].replace("\"", "").replace(",", "").trim();
                        break;
                    }
                }

                reader.close();
            }

            jarFile.close();
        } catch (IOException e) {
            print("Error during getting the Minecraft version automatically: " + e.getMessage(), true);
        }

        return minecraftVersion;
    }
}