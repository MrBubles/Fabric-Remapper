package me.mrbubbles.fabricremapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class YarnDownloading {
    public static Path path;

    public static Path resolve(String minecraftVersion) {
        String mappingsVersion = getMappingsVersion(minecraftVersion);
        Path currentDirectory = Paths.get("");
        Path mappingsTemp;

        try {
            String name = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

            if (name.contains("win")) {
                mappingsTemp = currentDirectory.resolve("mappings.gz");
            } else {
                mappingsTemp = currentDirectory.resolve("mappings" + mappingsVersion);
            }

            try (InputStream inputStream = getMappingsFromMaven(mappingsVersion)) {
                Files.copy(inputStream, mappingsTemp, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            Main.print("Error during downloading mappings: " + e.getMessage(), true);
            return null;
        }

        return mappingsTemp;
    }

    public static Path resolveTiny2(String minecraftVersion) {
        String mappingsVersion = getMappingsVersion(minecraftVersion);
        Path currentDirectory = Paths.get("");
        Path mappingsTemp;
        String fileMappings = "mappings.tiny";

        try {
            String name = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

            if (name.contains("win")) {
                mappingsTemp = currentDirectory.resolve("mappings-v2.zip");
            } else {
                mappingsTemp = currentDirectory.resolve("mappings-v2" + mappingsVersion);
            }

            try (InputStream inputStream = getTiny2Mappings(mappingsVersion)) {
                Files.copy(inputStream, mappingsTemp, StandardCopyOption.REPLACE_EXISTING);
            }

            path = mappingsTemp;
        } catch (Exception e) {
            Main.print("Error during resolving Tiny2: " + e.getMessage(), true);
            return null;
        }

        return extractFileFromZip(mappingsTemp, fileMappings, currentDirectory);
    }

    private static Path extractFileFromZip(Path zipPath, String targetFileName, Path outputDir) {
        try {
            try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                Path outputPath;
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith(".tiny")) {
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            outputPath = outputDir.resolve(targetFileName);
                            Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        return outputPath;
                    }
                }
            }
        } catch (Exception e) {
            Main.print("Error during extracting file: " + e.getMessage(), true);
            return null;
        }
        Main.print("File " + targetFileName + " cannot be found in " + zipPath, true);
        return null;
    }

    private static InputStream getTiny2Mappings(String mappingsVersion) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://maven.fabricmc.net/net/fabricmc/yarn/" + mappingsVersion + "/yarn-" + mappingsVersion + "-v2.jar"))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
    }

    private static InputStream getMappingsFromMaven(String mappingsVersion) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://maven.fabricmc.net/net/fabricmc/yarn/" + mappingsVersion + "/yarn-" + mappingsVersion + "-tiny.gz"))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
    }

    public static Set<Integer> getYarnBuilds(String minecraftVersion) {
        Set<Integer> builds = new HashSet<>();
        try {
            URI uri = new URI("https://meta.fabricmc.net/v2/versions/yarn/" + minecraftVersion);
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            Pattern pattern = Pattern.compile("\"build\":\\s*(\\d+)");
            Matcher matcher = pattern.matcher(responseBody);

            while (matcher.find()) {
                builds.add(Integer.parseInt(matcher.group(1)));
            }
        } catch (Exception e) {
            Main.print("Error during getting the latest mappings build: " + e.getMessage(), true);
        }
        return builds;
    }

    public static int getLatestYarnBuild(String minecraftVersion) {
        Set<Integer> builds = getYarnBuilds(minecraftVersion);
        int latestBuild = 0;
        for (int build : builds) {
            if (build > latestBuild) latestBuild = build;
        }
        return latestBuild;
    }

    public static String getMappingsVersion(String minecraftVersion) {
        return minecraftVersion + "+build." + getLatestYarnBuild(minecraftVersion);
    }
}
