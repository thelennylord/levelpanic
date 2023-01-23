package me.thelennylord.levelpanic;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.hypixel.api.http.HypixelHttpClient;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.PlayerReply.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class LobbyHandler {
    public static final HypixelHttpClient client = new ApacheHttpClient(UUID.fromString(ConfigHandler.APIKey));
    public static final HypixelAPI API = new HypixelAPI(client);


    private int lastTick = 400;
    private boolean processed = false;

    private boolean onHypixel = false;
    private boolean inBedwarsLobby = false;
    public static String mode;

    public boolean shouldProcess(String message) {
        try {
            JsonObject object = new Gson().fromJson(message, JsonObject.class);
            return object != null;
        } catch (Exception exception) {
            return false;
        }
    }

    public void process(String message) {
        JsonObject object = new Gson().fromJson(message, JsonObject.class);
        this.processed = true;

        // Check if player is in a BedWars game
        if (object.has("mode") && object.get("mode").getAsString().startsWith("BEDWARS")) {
            LobbyHandler.mode = object.get("mode").getAsString();
            this.inBedwarsLobby = true;

            for (NetworkPlayerInfo networkPlayerInfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
                checkPlayer(networkPlayerInfo);
            }
        }
    }

    public void checkPlayer(EntityPlayer entityPlayer) {
        checkPlayer(entityPlayer.getUniqueID(), entityPlayer.getName());
    }

    public void checkPlayer(NetworkPlayerInfo networkPlayerInfo) {
        GameProfile gameProfile = networkPlayerInfo.getGameProfile();
        checkPlayer(gameProfile.getId(), gameProfile.getName());
    }

    public void checkPlayer(UUID playerUUID, String playerName) {
        if (Minecraft.getMinecraft().thePlayer.getUniqueID().equals(playerUUID))
            return;

        Thread t1 = new Thread() {
                
            public void run() {
                try {
                    PlayerReply reply = LobbyHandler.API.getPlayerByUuid(playerUUID).get();
                    Player player = reply.getPlayer();
                    EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

                    if (ConfigHandler.avoidNicks && !player.exists()) {
                        thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[WARNING] " + EnumChatFormatting.RESET + EnumChatFormatting.GOLD + playerName + EnumChatFormatting.RED + " is nicked"));
                        return;
                    }
                    
                    int level = player.getIntProperty("achievements.bedwars_level", 0);
                    if (level >= ConfigHandler.levelThreshold) {
                        thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[WARNING] " + EnumChatFormatting.RESET + EnumChatFormatting.GOLD + playerName + EnumChatFormatting.AQUA + " (" + level + ") " + EnumChatFormatting.RED + "meets the level threshold"));
                        return;
                    }
    
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        };

        t1.start();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldEvent.Load event) {
        if (!this.onHypixel)
            return;

        this.inBedwarsLobby = false;
        this.lastTick = 400;
        this.processed = false;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onClientTickEvent(TickEvent.ClientTickEvent event) {
        if (!this.onHypixel || this.lastTick < 0)
            return;
        
        if (this.lastTick == 0) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/locraw");
            this.lastTick = -1;
            return;
        }

        this.lastTick--;
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        for ( Score score : Minecraft.getMinecraft().thePlayer.getWorldScoreboard().getScores() ) {
            if (score.getScorePoints() > 0)
                LevelPanic.logger.info("[" + score.getObjective().getName() + " | " + score.getObjective().getDisplayName() + "] " + score.getPlayerName() + ": " + score.getScorePoints());
        }

        if (!this.onHypixel)
            return;
        
        String message = event.message.getUnformattedText();

        if (this.processed) {
            if (message.trim().equals("Bed Wars"))
                this.inBedwarsLobby = false;
            return;
        }

        if (!this.shouldProcess(message))
            return;

        event.setCanceled(true);
        this.process(message);
    }

    @SubscribeEvent
    public void onServerConnect(ClientConnectedToServerEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.isSingleplayer() || !mc.getCurrentServerData().serverIP.endsWith("hypixel.net"))
            this.onHypixel = false;
        else
            this.onHypixel = true;
    }

    @SubscribeEvent
    public void onServerDisconnect(ClientDisconnectionFromServerEvent event) {
        this.onHypixel = false;
        this.inBedwarsLobby = false;
        this.lastTick = 400;
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (this.inBedwarsLobby && event.entity instanceof EntityPlayer) {
            this.checkPlayer((EntityPlayer) event.entity);
        }
    }
}
