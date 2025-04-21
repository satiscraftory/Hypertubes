package net.ugi.hypertubes.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.ugi.hypertubes.entity.HypertubeEntity;

@EventBusSubscriber(modid = net.ugi.hypertubes.HyperTubes.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value =  Dist.CLIENT)
public class ModClientEvents {
    private static boolean forcedThirdPerson = false;
    private static CameraType originalCameraType = CameraType.FIRST_PERSON;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = Minecraft.getInstance().player;
        if (player == null || mc.level == null)
            return;

        //if(player.getVehicle() == null) return;//maybe not needed
        if (player.getVehicle() instanceof HypertubeEntity) {
            if (!forcedThirdPerson) {
                forcedThirdPerson = true;
                originalCameraType = mc.options.getCameraType();
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            if(player.isCreative()) return;
            InputConstants.Key key = Minecraft.getInstance().options.keyShift.getKey();
            KeyMapping.set(key, false);
/*            player.setForcedPose(Pose.FALL_FLYING);
            player.setPose(Pose.FALL_FLYING);*/
        }else {
            // Reset camera if no longer riding
            if (forcedThirdPerson) {
                mc.options.setCameraType(originalCameraType);
                forcedThirdPerson = false;
            }
        }
    }
}