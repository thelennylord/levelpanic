package me.thelennylord.levelpanic;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.hypixel.api.exceptions.BadStatusCodeException;
import net.hypixel.api.http.HypixelHttpClient;
import net.hypixel.api.reply.PlayerReply.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class LobbyHandler {
    public static HypixelHttpClient client = new ApacheHttpClient(ConfigHandler.APIKey);
    public static HypixelAPI API = new HypixelAPI(client);

    private boolean onHypixel = false;
    private boolean awaitingStats = false;
    private boolean autoPlayed = false;

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final String NICK_DETECTED = EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "[WARNING] " + EnumChatFormatting.RESET + EnumChatFormatting.LIGHT_PURPLE + "%s" + EnumChatFormatting.RED + " is nicked!";
    private final String MET_LEVEL_THRESHOLD = EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "[WARNING] " + EnumChatFormatting.RESET + EnumChatFormatting.LIGHT_PURPLE + "%s" + EnumChatFormatting.AQUA + " (" + "%d" + ") " + EnumChatFormatting.RED + "meets the level threshold!";
    private final String MET_KDR_THRESHOLD = EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "[WARNING] " + EnumChatFormatting.RESET + EnumChatFormatting.LIGHT_PURPLE + "%s" + EnumChatFormatting.GREEN + " (" + "%.2f" + ") " + EnumChatFormatting.RED + "meets the KDR threshold!";
    private final String MET_WLR_THRESHOLD = EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "[WARNING] " + EnumChatFormatting.RESET + EnumChatFormatting.LIGHT_PURPLE + "%s" + EnumChatFormatting.YELLOW + " (" + "%.2f" + ") " + EnumChatFormatting.RED + "meets the W/L threshold!";


    public boolean isBedwarsLobby() {
        final ScoreObjective objective = mc.thePlayer.getWorldScoreboard().getObjective("PreScoreboard");
        if (objective == null)
            return false;
        
        String displayName = objective.getDisplayName().replaceAll("§.", "");
        if (displayName.trim().equals("BED WARS")) {
            return true;
        }

        return false;
    }

    public void shouldAutoPlay() {
        if (!ConfigHandler.autoPlay || this.autoPlayed)
            return;

        this.autoPlayed = true;
        
        mc.thePlayer.sendChatMessage("/locraw");
        mc.thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "[LevelPanic] " + EnumChatFormatting.GREEN + "Joining new game.."));
        this.awaitingStats = true;
    }

    public void checkPlayer(EntityPlayer entityPlayer) {
        checkPlayer(entityPlayer.getUniqueID(), entityPlayer.getName());
    }

    public void checkPlayer(NetworkPlayerInfo networkPlayerInfo) {
        GameProfile gameProfile = networkPlayerInfo.getGameProfile();
        checkPlayer(gameProfile.getId(), gameProfile.getName());
    }

    public void checkPlayer(UUID playerUUID, String playerName) {
        if (mc.thePlayer.getUniqueID().equals(playerUUID))
            return;
        
        LevelPanic.logger.info("Checking: " + playerName + "@" + playerUUID);
        LobbyHandler.API.getPlayerByUuid(playerUUID).whenComplete((reply, error) -> {
            if (error != null) {
                LevelPanic.logger.error(error.getStackTrace());
                return;
            }
            
            EntityPlayerSP thePlayer = mc.thePlayer;
            Player player = reply.getPlayer();

            if (ConfigHandler.avoidNicks && !player.exists()) {
                thePlayer.addChatMessage(new ChatComponentText(String.format(NICK_DETECTED, playerName)));
                shouldAutoPlay();
                return;
            }
            
            float wlr = player.getIntProperty("stats.Bedwars.wins_bedwars", 0) / player.getIntProperty("stats.Bedwars.losses_bedwars", 1);
            if ( wlr >= ConfigHandler.wlrThreshold ) {
                thePlayer.addChatMessage(new ChatComponentText(String.format(MET_WLR_THRESHOLD, playerName, wlr)));
                shouldAutoPlay();
                return;
            }

            float kdr = player.getIntProperty("stats.Bedwars.kills_bedwars", 0) / player.getIntProperty("stats.Bedwars.deaths_bedwars", 1);
            float fkdr = player.getIntProperty("stats.Bedwars.final_kills_bedwars", 0) / player.getIntProperty("stats.Bedwars.final_deaths_bedwars", 1);
            float avgKdr = (kdr + fkdr) * 0.5f;
            if ( avgKdr >= ConfigHandler.kdrThreshold ) {
                thePlayer.addChatMessage(new ChatComponentText(String.format(MET_KDR_THRESHOLD, playerName, avgKdr)));
                shouldAutoPlay();
                return;
            }

            int level = player.getIntProperty("achievements.bedwars_level", 0);
            if (level >= ConfigHandler.levelThreshold) {
                thePlayer.addChatMessage(new ChatComponentText(String.format(MET_LEVEL_THRESHOLD, playerName, level)));
                shouldAutoPlay();
                return;
            }

        });
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        this.autoPlayed = false;
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!this.awaitingStats)
            return;

        try {
            JsonObject object = new Gson().fromJson(event.message.getUnformattedText(), JsonObject.class);
            this.awaitingStats = false;

            event.setCanceled(true);

            if (!object.has("mode"))
                return;


            TimerTask timerTask = new TimerTask() {
                public void run() {
                    if (isBedwarsLobby())
                        mc.thePlayer.sendChatMessage("/play " + object.get("mode").getAsString());
                }
            };

            new Timer().schedule(timerTask, 5000L);

        } catch (JsonSyntaxException exception) {
            return;
        }
    }

    @SubscribeEvent
    public void onServerConnect(ClientConnectedToServerEvent event) {
        if (mc.isSingleplayer() || !mc.getCurrentServerData().serverIP.endsWith("hypixel.net"))
            this.onHypixel = false;
        else
            this.onHypixel = true;
    }

    @SubscribeEvent
    public void onServerDisconnect(ClientDisconnectionFromServerEvent event) {
        this.onHypixel = false;
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (this.onHypixel && this.isBedwarsLobby() && event.entity instanceof EntityPlayer) {
            this.checkPlayer((EntityPlayer) event.entity);
        }
    }
}
