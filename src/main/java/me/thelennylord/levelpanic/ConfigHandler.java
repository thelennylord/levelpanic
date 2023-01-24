package me.thelennylord.levelpanic;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {
    public static File configFile;
    public static Configuration config;

    public static String APIKey;

    public static boolean avoidNicks;
    public static int levelThreshold;
    public static float kdrThreshold;
    
    public static boolean autoPlay = false;

    public static void init(File file) {
        config = new Configuration(file);
        syncConfig();
    }

    public static void syncConfig() {
        String category = "general";
        
        config.addCustomCategoryComment(category, "General configurations");
        APIKey = config.getString("APIKey", category, "c1ddb1e5-8923-469b-91af-4ff72defb211", "Your Hypixel API key");

        avoidNicks = config.getBoolean("avoidNicks", category, true, "Avoid nicked players in the game");
        levelThreshold = config.getInt("levelThreshold", category, 150, 0, Integer.MAX_VALUE, "Avoid players who meet the level threshold");
        kdrThreshold = config.getFloat("kdrThreshold", category, 10.0f, 0.0f, Float.MAX_VALUE, "Avoid players who meet the KDR threshold (average of kdr and fkdr of all modes)");

        autoPlay = config.getBoolean("autoPlay", category, false, "Join a new game if one of the checks returns true");

        config.save();
    }
}
