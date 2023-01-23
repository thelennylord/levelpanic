package me.thelennylord.levelpanic.miscs;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Moyai {
    public final Minecraft minecraft = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (event.message.getUnformattedText().toLowerCase().contains("moyai")) {
            this.playBoomSound();
        }
    }

    private void playBoomSound() {
        EntityPlayerSP player = minecraft.thePlayer;
        minecraft.theWorld.playSound(player.posX, player.posY, player.posZ, "levelpanic:moyai", 100.0f, 1.0f, false);
    }
}
