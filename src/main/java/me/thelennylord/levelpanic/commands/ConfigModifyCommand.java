package me.thelennylord.levelpanic.commands;

import me.thelennylord.levelpanic.ConfigHandler;
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
