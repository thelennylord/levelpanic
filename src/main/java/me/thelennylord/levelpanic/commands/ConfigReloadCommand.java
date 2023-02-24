package me.thelennylord.levelpanic.commands;

import java.io.File;

import me.thelennylord.levelpanic.ConfigHandler;
import me.thelennylord.levelpanic.LevelPanic;
import me.thelennylord.levelpanic.LobbyHandler;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.Loader;

public class ConfigReloadCommand extends CommandBase {
    
    public String getCommandName() {
        return "lpreload";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/lpreload";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        ConfigHandler.init(new File(Loader.instance().getConfigDir(), LevelPanic.MODID + ".cfg"));
        
        LobbyHandler.API.shutdown();
        LobbyHandler.client = new ApacheHttpClient(ConfigHandler.APIKey);
        LobbyHandler.API = new HypixelAPI(LobbyHandler.client);

        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] " + EnumChatFormatting.WHITE + "Reloaded config"));
    }

    public int getRequiredPermissionLevel() {
        return 0;
    }
}
