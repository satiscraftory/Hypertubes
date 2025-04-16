package net.ugi.sf_hypertube.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.ugi.sf_hypertube.SfHyperTube;
import net.ugi.sf_hypertube.entity.HypertubeEntity;

@EventBusSubscriber(modid = SfHyperTube.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value =  Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(player.isCreative()) return;
        System.out.println("player exits");
        if (player.getVehicle() instanceof HypertubeEntity) {
            System.out.println("player rides");
            InputConstants.Key key = Minecraft.getInstance().options.keyShift.getKey();
            KeyMapping.set(key, false);


        }
    }
}