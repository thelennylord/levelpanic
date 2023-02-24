package me.thelennylord.levelpanic.commands;

import java.util.UUID;

import me.thelennylord.levelpanic.ConfigHandler;
import me.thelennylord.levelpanic.LobbyHandler;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ConfigModifyCommand extends CommandBase {
    public static final ChatComponentText INVALID_USAGE = new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] Usage: /lpconfig get <name>");

    public String getCommandName() {
        return "lpconfig";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/lpconfig";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(INVALID_USAGE);
            return;
        }
        
        switch (args[0]) {
            case "get":
                switch (args[1].toLowerCase()) {
                    case "apikey":
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] Due to security reasons, APIKey cannot be viewed."));
                        break;
                        
                    case "autoplay":
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] " + EnumChatFormatting.RESET + "autoPlay: " + EnumChatFormatting.GREEN + ConfigHandler.autoPlay));
                        break;
                        
                    case "avoidnicks":
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] " + EnumChatFormatting.RESET + "avoidNicks: " + EnumChatFormatting.GREEN + ConfigHandler.avoidNicks));
                        break;
                        
                    case "kdrthreshold":
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] " + EnumChatFormatting.RESET + "kdrThreshold: " + EnumChatFormatting.GREEN + ConfigHandler.kdrThreshold));
                        break;
                        
                    case "levelthreshold":
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] " + EnumChatFormatting.RESET + "levelThreshold: " + EnumChatFormatting.GREEN + ConfigHandler.levelThreshold));
                        break;
                        
                    case "wlrThreshold":
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] " + EnumChatFormatting.RESET + "wlrThreshold: " + EnumChatFormatting.GREEN + ConfigHandler.levelThreshold));
                        break;

                    default:
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] Invalid config '" + args[1] + "'"));
                        break;
                }
                break;


            case "set":
                String category = "general";
                if (args.length < 2) {
                    sender.addChatMessage(INVALID_USAGE);
                    return;
                }

                switch (args[1]) {
                    case "APIKey": {
                        UUID value;
                        try {
                            value = UUID.fromString(args[2]);
                        } catch (IllegalArgumentException exception) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] APIKey should be a valid UUID."));
                            break;
                        }

                        ConfigHandler.config.get(category, "APIKey", "c1ddb1e5-8923-469b-91af-4ff72defb211", "Your Hypixel API key").setValue(args[2]);
                        ConfigHandler.config.save();
                        
                        // Create new ApacheHttpClient with tne new api key
                        LobbyHandler.API.shutdown();
                        LobbyHandler.client = new ApacheHttpClient(value);
                        LobbyHandler.API = new HypixelAPI(LobbyHandler.client);

                        break;
                    }
                        
                    case "autoPlay":
                        if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")) {
                            ConfigHandler.config.get(category, args[1], false, "Join a new game if one of the checks returns true").setValue(Boolean.parseBoolean(args[2]));
                            ConfigHandler.config.save();
                        } else
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] This config's value should either be true or false."));
                        break;
                        
                    case "avoidNicks":
                        if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")) {
                            ConfigHandler.config.get(category, args[1], true, "Avoid nicked players in the game").setValue(Boolean.parseBoolean(args[2]));
                            ConfigHandler.config.save();
                        } else
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] This config's value should either be true or false."));
                        break;
                        
                    case "kdrThreshold": {
                        float value;
                        try {
                            value = Float.parseFloat(args[2]);    
                        } catch (NumberFormatException exception) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] This config's value should be a float >= 0."));
                            break;
                        }
                        
                        if (value < 0.0f) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] This config's value should be a float >= 0."));
                            break;
                        }

                        ConfigHandler.config.get(category, args[1], 10.0f, "Avoid players who meet the KDR threshold (average of kdr and fkdr of all modes)").setValue(value);
                        ConfigHandler.config.save();
                        break;
                    }
                        
                    case "levelThreshold": {
                        int value;
                        try {
                            value = Integer.parseInt(args[2]);
                        } catch (NumberFormatException exception) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] This config's value should be an integer >= 0."));
                            break;
                        }

                        if (value < 0) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] This config's value should be a float >= 0."));
                            break;
                        }

                        ConfigHandler.config.get(category, args[1], 150, "Avoid players who meet the level threshold").setValue(value);
                        ConfigHandler.config.save();
                        break;
                    }
                        
                    case "wlrThreshold": {
                        float value;
                        try {
                            value = Float.parseFloat(args[2]);    
                        } catch (NumberFormatException exception) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] This config's value should be a float >= 0."));
                            break;
                        }
                        
                        if (value < 0.0f) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] This config's value should be a float >= 0."));
                            break;
                        }

                        ConfigHandler.config.get(category, args[1], 5.0f, "Avoid players who meet the W/L ratio threshold").setValue(value);
                        ConfigHandler.config.save();
                        break;
                    }
                        
                    default:
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] Invalid config '" + args[1] + "'"));
                        break;
                }
                break;

            default:
                sender.addChatMessage(INVALID_USAGE);
                return;
        }

    }

    public int getRequiredPermissionLevel() {
        return 0;
    }
}
