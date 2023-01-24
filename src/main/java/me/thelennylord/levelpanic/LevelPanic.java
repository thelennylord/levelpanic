package me.thelennylord.levelpanic;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.thelennylord.levelpanic.commands.ConfigModifyCommand;
import me.thelennylord.levelpanic.commands.ConfigReloadCommand;
import me.thelennylord.levelpanic.miscs.Moyai;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = LevelPanic.MODID, version = LevelPanic.VERSION)
public class LevelPanic {
    public static final String MODID = "levelpanic";
    public static final String VERSION = "1.0";

    public static final Logger logger = LogManager.getLogger(LevelPanic.MODID);


    @EventHandler()
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.init(new File(Loader.instance().getConfigDir(), MODID + ".cfg"));
    }

    @EventHandler()
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new LobbyHandler());
        MinecraftForge.EVENT_BUS.register(new Moyai());

        // Register command
        ClientCommandHandler.instance.registerCommand(new ConfigReloadCommand());
        ClientCommandHandler.instance.registerCommand(new ConfigModifyCommand());
    }
}
