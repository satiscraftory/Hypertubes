package net.ugi.sf_hypertube.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.ugi.sf_hypertube.SfHyperTube;
import net.ugi.sf_hypertube.entity.HypertubeEntity;

/*
@EventBusSubscriber(modid = SfHyperTube.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

}
*/
