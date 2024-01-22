package dev.hunter.deobf;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class YarnUtil {

    public static JSONArray getYarnBuilds(String minecraftVersion) {
        JSONArray jsonArray = null;
        try {
            URL url = new URL("https://meta.fabricmc.net/v2/versions/yarn/" + minecraftVersion);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            jsonArray = new JSONArray(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static int getLatestYarnBuild(String minecraftVersion) {
        JSONArray yarnBuilds = getYarnBuilds(minecraftVersion);
        int latestBuild = 0;
        for (int i = 0; i < yarnBuilds.length(); i++) {
            JSONObject jsonObject = yarnBuilds.getJSONObject(i);
            int build = jsonObject.getInt("build");
            if (build > latestBuild) {
                latestBuild = build;
            }
        }
        return latestBuild;
    }

    public static String getMavenYarnMapping(String minecraftVersion) {
        return minecraftVersion + "+build." + getLatestYarnBuild(minecraftVersion);
    }
}
