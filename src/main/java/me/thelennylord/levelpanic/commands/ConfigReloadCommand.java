package me.thelennylord.levelpanic.commands;

import me.thelennylord.levelpanic.ConfigHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ConfigReloadCommand extends CommandBase {
    
    public String getCommandName() {
        return "lpreload";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/lpreload";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        ConfigHandler.syncConfig();
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] Reloaded config"));
    }

    public int getRequiredPermissionLevel() {
        return 0;
    }
}
