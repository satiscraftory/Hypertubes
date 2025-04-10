package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod
{
    public static final String MODID = "examplemod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public static final DeferredBlock<Block> HYPERTUBE_BLOCK = registerBlock("hypertube_block",
            () -> new HypertubeBlock(BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID,"hypertube_block")),ResourceLocation.fromNamespaceAndPath(MODID,"hypertube_block")))));

    //public static final DeferredItem<BlockItem> HYPERTUBE_ITEM = ITEMS.registerSimpleBlockItem("hypertube_block", HYPERTUBE_BLOCK);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());
                output.accept(HYPERTUBE_BLOCK.get());
            }).build());

    private final WeakHashMap<Player, Vec3> hypertubeMotion = new WeakHashMap<>();



    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID,name)), ResourceLocation.fromNamespaceAndPath(MODID,name)))));
    }



    public ExampleMod(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::PlayerTickEvent);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS){
            event.accept(EXAMPLE_BLOCK_ITEM);
            event.accept(HYPERTUBE_BLOCK);
        }


    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }



    public static class HypertubeBlock extends HorizontalDirectionalBlock {
        public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

        public HypertubeBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
            return null;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Override
        public RenderShape getRenderShape(BlockState state) {
            return RenderShape.MODEL;
        }

        @Override
        public BlockState rotate(BlockState state, Rotation rotation) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        @Override
        public BlockState mirror(BlockState state, Mirror mirror) {
            return state.rotate(mirror.getRotation(state.getValue(FACING)));
        }
    }

    @SubscribeEvent
    public void PlayerTickEvent(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        Level world = player.level();
        BlockPos pos = player.blockPosition();

        List<BlockPos> positions = BlockPos.betweenClosedStream(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
                .map(BlockPos::immutable)
                .toList();

        boolean foundTube = false;

        for (BlockPos checkPos : positions) {
            BlockState state = world.getBlockState(checkPos);
            if (state.is(HYPERTUBE_BLOCK.get())) {
                Direction facing = state.getValue(HypertubeBlock.FACING);
                Vec3 playerToBlock = Vec3.atCenterOf(checkPos).subtract(player.position());
                Vec3 facingVec = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
                double dot = playerToBlock.normalize().dot(facingVec);

                if (dot > 0.5) {
                    Vec3 motion = new Vec3(facing.getStepX() * 0.5, facing.getStepY() * 0.5, facing.getStepZ() * 0.5);
                    hypertubeMotion.put(player, motion);
                    foundTube = true;

                    // Move player inside the block
                    movePlayerInsideBlock(player, checkPos, facing);

                    // Change camera to outside view
                    adjustCameraToOutside(player);

                    break;
                }
            }
        }

        if (foundTube) {
            Vec3 motion = hypertubeMotion.get(player);
            if (motion != null) {
                player.setDeltaMovement(motion);
                player.setOnGround(false);
                player.fallDistance = 0;
                player.setNoGravity(true);
                player.hurtMarked = true;
            }
        } else {
            player.setNoGravity(false);
            hypertubeMotion.remove(player);
        }
    }

    private void movePlayerInsideBlock(Player player, BlockPos blockPos, Direction facing) {
        // Calculate the player's new position inside the block
        Vec3 insidePosition = Vec3.atCenterOf(blockPos).add(0, 0.5, 0); // Move them slightly inside the block
        player.setPos(insidePosition.x, insidePosition.y, insidePosition.z);
    }

    private void adjustCameraToOutside(Player player) {
        // This line ensures the camera is moved to simulate an external view
        // If necessary, adjust the offset based on how you want the camera to behave
        //Minecraft.getInstance().gameRenderer.setFirstPerson();
        // Optionally, adjust the player's yaw/pitch to simulate an "outside" looking direction
        //player.setDi(player.getYHeadRot() + 180); // Flip the camera for looking around
    }

}
