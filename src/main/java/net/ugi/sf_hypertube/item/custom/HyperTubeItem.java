package net.ugi.sf_hypertube.item.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.ugi.sf_hypertube.block.ModBlocks;
import net.ugi.sf_hypertube.block.entity.HypertubeSupportBlockEntity;
import net.ugi.sf_hypertube.item.ModItems;
import net.ugi.sf_hypertube.util.Bezier;
import org.joml.Vector3f;

import java.util.*;


public class HyperTubeItem extends Item {
    public HyperTubeItem(Properties properties) {
        super(properties);
    }


    private final WeakHashMap<ItemStack, BlockPos> block1Pos = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Direction.Axis> block1Axis = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Integer> block1Direction = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Boolean> selectedBlock1 = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, BlockPos> block2Pos = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Direction.Axis> block2Axis = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Integer> block2Direction  = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, String> curveType  = new WeakHashMap<>();


    double bezierHelpPosMultiplier =0.5;//default 0.5
    Block hyperTubeSupportBlock = ModBlocks.HYPERTUBE_SUPPORT.get();
    Block hyperTubeBlock = ModBlocks.HYPERTUBE.get();
    double placeDistance = 20;

    Bezier bezier = new Bezier();


    private int intValue(boolean val){
        return val ? 1 : 0;
    }

    private boolean checkValidCurve(Level level, BlockPos[] blockPosArray){
        boolean isValidCurve = true;
        for (int i = 0; i < blockPosArray.length; i++) {
            if(!(level.getBlockState(blockPosArray[i]).isAir() || level.getBlockState(blockPosArray[i]).is(ModBlocks.HYPERTUBE_SUPPORT))){
                isValidCurve = false;
            }
        }
        return isValidCurve;

    }
    private void changeCurveType(ItemStack stack){
        if(curveType.get(stack).equals("Curved")){
            curveType.put(stack, "Overkill");
        }
        else if (curveType.get(stack).equals("Overkill")){
            curveType.put(stack, "Straight");
        }
        else  {
            curveType.put(stack, "Curved");
        }
        bezier.setCurve(curveType.get(stack));
    }

    private void placeHypertubeSupport(Level level, BlockPos pos, Direction.Axis axis){
        level.setBlock(pos, hyperTubeSupportBlock.defaultBlockState().setValue(BlockStateProperties.AXIS, axis),2 );
        level.blockUpdated(pos,hyperTubeSupportBlock);
        level.setBlock(pos.below(1), Blocks.BRICK_WALL.defaultBlockState(),2 );
        level.blockUpdated(pos.below(1),Blocks.BRICK_WALL);
    }

    private void modifyData(Level level, BlockPos pos, int dir,BlockPos targetPos, String curveType){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity){
            if(dir == 1){
                hypertubeSupportBlockEntity.targetPositive = targetPos;
                hypertubeSupportBlockEntity.targetPositiveType = curveType;
            }
            if(dir == -1){
                hypertubeSupportBlockEntity.targetNegative = targetPos;
                hypertubeSupportBlockEntity.targetNegativeType = curveType;

            }
        }

    }



    private boolean calcBlockIfHyperTubeSupport(Level level, ItemStack stack, BlockPos blockpos, WeakHashMap<ItemStack, BlockPos> blockPosMap, WeakHashMap<ItemStack, Direction.Axis> blockAxisMap, WeakHashMap<ItemStack, Integer> blockDirectionMap){
        blockPosMap.put(stack,blockpos);
        blockAxisMap.put(stack,level.getBlockState(blockPosMap.get(stack)).getValue(BlockStateProperties.AXIS));

        BlockEntity blockEntity = level.getBlockEntity(blockpos);
        if (blockEntity instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity){

            boolean dir1IsHyperTube = hypertubeSupportBlockEntity.targetPositive != null;
            boolean dir2IsHyperTube = hypertubeSupportBlockEntity.targetNegative != null;


            if (dir1IsHyperTube && dir2IsHyperTube) return false; // no connection possible to this tube
            blockDirectionMap.put(stack,intValue(dir2IsHyperTube) - intValue(dir1IsHyperTube)); // gives the direction the tubes need to be , if 0 => code chooses later ( both available)
        }
        return true;
    }

    private boolean calcBlockIfBlock(Level level, ItemStack stack, BlockPos blockpos, Player player, WeakHashMap<ItemStack, BlockPos> blockPosMap, WeakHashMap<ItemStack, Direction.Axis> blockAxisMap, WeakHashMap<ItemStack, Integer> blockDirectionMap){
        blockPosMap.put(stack,blockpos.above(2));
        blockAxisMap.put(stack,Direction.Axis.X); // default axis
        blockDirectionMap.put(stack,0); // default

        assert player != null;
        Vec3 lookingVec = player.getLookAngle();
        Vec3 biasLookingVec = new Vec3(lookingVec.x,lookingVec.y/1.9,lookingVec.z);
        if (Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.z)) {
            blockAxisMap.put(stack,Direction.Axis.X);
        }
        if (Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.x) && Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.z)) {
            blockAxisMap.put(stack,Direction.Axis.Y);
        }
        if (Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.x)) {
            blockAxisMap.put(stack,Direction.Axis.Z);
        }
        return true;
    }

    private  boolean calcBlockIfAir(Level level, ItemStack stack, BlockPos blockpos, Player player, WeakHashMap<ItemStack, BlockPos> blockPosMap, WeakHashMap<ItemStack, Direction.Axis> blockAxisMap, WeakHashMap<ItemStack, Integer> blockDirectionMap){
        return calcBlockIfBlock(level,stack,blockpos.below(2),player, blockPosMap, blockAxisMap, blockDirectionMap);
    }



    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        System.out.println("HyperTubeItem::use");

        if (level.isClientSide()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        ItemStack stack = player.getItemInHand(usedHand);

        selectedBlock1.putIfAbsent(stack, false);
        curveType.putIfAbsent(stack, "Curved");

        if(player.isShiftKeyDown()){
            changeCurveType(stack);
            return InteractionResultHolder.success(stack);
        }

        Vec3 looking = player.getLookAngle();
        BlockPos blockpos = level.clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(looking.x * placeDistance, looking.y * placeDistance, looking.z * placeDistance), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();


        if (level.getBlockState(blockpos).isAir()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

        if(!selectedBlock1.get(stack)){ // get first block pos
            if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){ // if clicking on hypertyubeSupportBlock

                boolean result = calcBlockIfHyperTubeSupport(level,stack,blockpos,block1Pos,block1Axis,block1Direction);
                if (!result) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

                selectedBlock1.put(stack,true);
            }
            else { //if clicking on ground to start placing a hypertube
                if (!level.getBlockState(blockpos.above(1)).isAir()||!level.getBlockState(blockpos.above(2)).isAir()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

                boolean result = calcBlockIfBlock(level,stack,blockpos,player,block1Pos,block1Axis,block1Direction);
                if (!result) return InteractionResultHolder.fail(player.getItemInHand(usedHand));


                placeHypertubeSupport(level,block1Pos.get(stack),block1Axis.get(stack));
                selectedBlock1.put(stack,true);
            }
        }

        else { // get second block pos
            if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){


                //cancel placement by clicking on first support
                if (blockpos.equals(block1Pos.get(stack))) {
                    selectedBlock1.put(stack,false);
                    return InteractionResultHolder.fail(player.getItemInHand(usedHand));
                }


                boolean result = calcBlockIfHyperTubeSupport(level,stack,blockpos,block2Pos,block2Axis,block2Direction);
                if (!result) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

            }
            else { //if clicking on ground to end placing a hypertube

                if (!level.getBlockState(blockpos.above(1)).isAir()||!level.getBlockState(blockpos.above(2)).isAir()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

                boolean result = calcBlockIfBlock(level,stack,blockpos,player,block2Pos,block2Axis,block2Direction);
                if (!result) return InteractionResultHolder.fail(player.getItemInHand(usedHand));
            }

            BlockPos[] blockPosArray = bezier.calcBezierArray(block1Pos.get(stack),block1Axis.get(stack),block1Direction.get(stack),block2Pos.get(stack),block2Axis.get(stack),block2Direction.get(stack));

            boolean isValidCurve = checkValidCurve(level,blockPosArray);
            if (!isValidCurve) return InteractionResultHolder.pass(player.getItemInHand(usedHand));

            for (int i = 0; i < blockPosArray.length; i++) {
                if (level.getBlockState(blockPosArray[i]).is(hyperTubeSupportBlock)) continue;
                level.setBlock(blockPosArray[i], hyperTubeBlock.defaultBlockState(),2 );
                level.blockUpdated(blockPosArray[i],hyperTubeBlock);
            }
            placeHypertubeSupport(level,block2Pos.get(stack),block2Axis.get(stack));

            modifyData(level,block1Pos.get(stack),bezier.getBlock1Direction(),block2Pos.get(stack), curveType.get(stack));
            modifyData(level,block2Pos.get(stack),bezier.getBlock2Direction(),block1Pos.get(stack), curveType.get(stack));

            selectedBlock1.put(stack,false);
        }

        return super.use(level, player, usedHand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide()) return;
        if(entity instanceof Player player){
            ItemStack selectedItem = player.getMainHandItem().getItem() == ModItems.HYPERTUBEPLACER.get()? player.getMainHandItem() : player.getOffhandItem();
            if(selectedItem != stack) return;

            selectedBlock1.putIfAbsent(stack, false);

            Vec3 looking = entity.getLookAngle();
            if(selectedBlock1.get(stack)) {
                BlockPos blockpos = level.clip(new ClipContext(entity.getEyePosition(), entity.getEyePosition().add(looking.x * placeDistance, looking.y * placeDistance, looking.z * placeDistance), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();

                if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){ // if clicking on hypertyubeSupportBlock

                    boolean result = calcBlockIfHyperTubeSupport(level,stack,blockpos,block2Pos,block2Axis,block2Direction);
                    if (!result) return;
                }
                else {

                    if (!level.getBlockState(blockpos).isAir() && level.getBlockState(blockpos.above(1)).isAir() && level.getBlockState(blockpos.above(2)).isAir()) {
                        boolean result = calcBlockIfBlock(level,stack,blockpos,player,block2Pos,block2Axis,block2Direction);
                        if (!result) return;
                    }
                    else {
                        boolean result = calcBlockIfAir(level,stack,blockpos,player,block2Pos,block2Axis,block2Direction);
                        if (!result) return;
                    }
                }

                BlockPos[] blockPosArray = bezier.calcBezierArray(block1Pos.get(stack),block1Axis.get(stack),block1Direction.get(stack),block2Pos.get(stack),block2Axis.get(stack),block2Direction.get(stack));


                boolean isValidCurve = checkValidCurve(level,blockPosArray);
                Vector3f color = isValidCurve ?new Vector3f(0,255,255) : new Vector3f(255,0,0);

                player.displayClientMessage(Component.literal(  player.getInventory().countItem(Items.GLASS) + "/" +String.valueOf(blockPosArray.length-2)), true);

                for (int i = 0; i < blockPosArray.length; i++) {
                    for(int j = 0; j < level.players().size(); ++j) {
                        ServerPlayer serverplayer = (ServerPlayer)level.players().get(j);
                        ((ServerLevel) level).sendParticles(serverplayer, new DustParticleOptions(color,1), true,
                            blockPosArray[i].getX()+0.5,blockPosArray[i].getY()+0.5,blockPosArray[i].getZ()+0.5, 1, 0.2, 0.2, 0.2,1);
                    }
                }
            }
        }
    }
}
