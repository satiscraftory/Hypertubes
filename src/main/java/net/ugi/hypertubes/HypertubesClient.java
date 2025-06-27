package net.ugi.hypertubes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.ugi.hypertubes.block.ModBlocks;
import net.ugi.hypertubes.block.entity.ModBlockEntities;
import net.ugi.hypertubes.block.entity.renderer.HypertubeSupportBlockEntityRenderer;
import net.ugi.hypertubes.entity.ModEntities;
import net.ugi.hypertubes.hypertube.UI.HyperTubePlacerOverlayRenderer;

@Mod(value = HyperTubes.MOD_ID, dist = Dist.CLIENT)
public class HypertubesClient {
    public HypertubesClient(IEventBus modBus, ModContainer container) {
        // Perform logic in that should only be executed on the physical client
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @EventBusSubscriber(modid = HyperTubes.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.HYPERTUBE_ENTITY.get(), NoopRenderer::new);

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPERTUBE.get(), RenderType.translucent());
            NeoForge.EVENT_BUS.register(HyperTubePlacerOverlayRenderer.class);

        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.HYPERTUBE_SUPPORT_BE.get(), HypertubeSupportBlockEntityRenderer::new);
        }
    }
}
