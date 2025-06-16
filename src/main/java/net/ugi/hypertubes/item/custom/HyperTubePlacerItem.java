package net.ugi.hypertubes.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.ugi.hypertubes.Config;
import net.ugi.hypertubes.block.ModBlocks;
import net.ugi.hypertubes.block.entity.HypertubeSupportBlockEntity;
import net.ugi.hypertubes.hypertube.Curves.HyperTubeCalcCore;
import net.ugi.hypertubes.hypertube.Curves.CurveTypes;
import net.ugi.hypertubes.hypertube.HyperTubeUtil;
import net.ugi.hypertubes.item.ModItems;
import net.ugi.hypertubes.network.HyperTubeOverlayPacket;
import net.ugi.hypertubes.util.ModTags;
import org.joml.Vector3f;

import java.util.*;


public class HyperTubePlacerItem extends Item {
    public HyperTubePlacerItem(Properties properties) {
        super(properties);
    }


    private final WeakHashMap<ItemStack, BlockPos> block1Pos = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Direction.Axis> block1Axis = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Integer> block1Direction = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, String> extraData1 = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Boolean> selectedBlock1 = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, BlockPos> block2Pos = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Direction.Axis> block2Axis = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, Integer> block2Direction  = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, String> extraData2  = new WeakHashMap<>();
    private final WeakHashMap<ItemStack, CurveTypes.Curves> curveType  = new WeakHashMap<>();

    Block hyperTubeSupportBlock = ModBlocks.HYPERTUBE_SUPPORT.get();
    Block hyperTubeBlock = ModBlocks.HYPERTUBE.get();



    private void placeHypertubeSupport(Level level, BlockPos pos, Direction.Axis axis, Direction placeDirection){
        level.setBlock(pos, hyperTubeSupportBlock.defaultBlockState().setValue(BlockStateProperties.AXIS, axis),2 );
        level.blockUpdated(pos,hyperTubeSupportBlock);
        level.setBlock(pos.relative(placeDirection,-1), ModBlocks.HYPERTUBE_SUPPORT_POLE.get().defaultBlockState().setValue(BlockStateProperties.AXIS,placeDirection.getAxis()),2 );
        level.blockUpdated(pos.relative(placeDirection,-1),ModBlocks.HYPERTUBE_SUPPORT_POLE.get());
    }

    private void modifyData(Level level, BlockPos pos, int dir,BlockPos targetPos, CurveTypes.Curves curveType, String extraData){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof HypertubeSupportBlockEntity hypertubeSupportBlockEntity){
            if(dir == 1){
                hypertubeSupportBlockEntity.targetPositive = targetPos;
                hypertubeSupportBlockEntity.targetPositiveType = curveType;
                hypertubeSupportBlockEntity.positiveTypeInfo = extraData;
                hypertubeSupportBlockEntity.setChanged();
            }
            if(dir == -1){
                hypertubeSupportBlockEntity.targetNegative = targetPos;
                hypertubeSupportBlockEntity.targetNegativeType = curveType;
                hypertubeSupportBlockEntity.negativeTypeInfo = extraData;
                hypertubeSupportBlockEntity.setChanged();
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

            blockDirectionMap.put(stack,HyperTubeUtil.intValue(dir2IsHyperTube) - HyperTubeUtil.intValue(dir1IsHyperTube)); // gives the direction the tubes need to be , if 0 => code chooses later ( both available)
        }
        return true;
    }

    private boolean calcBlockIfBlock(Level level, ItemStack stack, BlockPos blockpos, Player player, Direction placeDirection, WeakHashMap<ItemStack, BlockPos> blockPosMap, WeakHashMap<ItemStack, Direction.Axis> blockAxisMap, WeakHashMap<ItemStack, Integer> blockDirectionMap){
        blockPosMap.put(stack,blockpos.relative(placeDirection,2));
        blockAxisMap.put(stack,Direction.Axis.X); // default axis
        blockDirectionMap.put(stack,0); // default

        assert player != null;
        Vec3 lookingVec = player.getLookAngle();

        Direction.Axis axis = placeDirection.getAxis();
        Vec3 biasLookingVec = new Vec3(lookingVec.x,lookingVec.y,lookingVec.z);
        if (axis == Direction.Axis.X) biasLookingVec = new Vec3(lookingVec.x/1.9,lookingVec.y,lookingVec.z);
        if (axis == Direction.Axis.Y) biasLookingVec = new Vec3(lookingVec.x,lookingVec.y/1.9,lookingVec.z);
        if (axis == Direction.Axis.Z) biasLookingVec = new Vec3(lookingVec.x,lookingVec.y,lookingVec.z/1.9);

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
        return calcBlockIfBlock(level,stack,blockpos.below(2),player, Direction.UP, blockPosMap, blockAxisMap, blockDirectionMap);
    }

    public static void removeItems(Player player, Item itemToRemove, int amount) {
        Container inventory = player.getInventory(); // player's inventory
        int remaining = amount;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (!stack.isEmpty() && stack.getItem() == itemToRemove) {
                int stackCount = stack.getCount();

                if (stackCount <= remaining) {
                    inventory.setItem(i, ItemStack.EMPTY); // remove whole stack
                    remaining -= stackCount;
                } else {
                    stack.shrink(remaining); // remove part of the stack
                    remaining = 0;
                }

                if (remaining <= 0) {
                    break;
                }
            }
        }
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        if (level.isClientSide()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        ItemStack stack = player.getItemInHand(usedHand);

        selectedBlock1.putIfAbsent(stack, false);
        curveType.putIfAbsent(stack, CurveTypes.Curves.CURVED);

        extraData1.putIfAbsent(stack,null);
        extraData2.putIfAbsent(stack,null);

        if(player.isShiftKeyDown()){
            this.curveType.put(stack, CurveTypes.cycle(this.curveType.get(stack)));
            return InteractionResultHolder.success(stack);
        }

        Vec3 looking = player.getLookAngle();
        BlockPos blockpos = level.clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(looking.x * Config.hypertubePlaceReach, looking.y * Config.hypertubePlaceReach, looking.z * Config.hypertubePlaceReach), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();
        Direction placeDirection = level.clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(looking.x * Config.hypertubePlaceReach, looking.y * Config.hypertubePlaceReach, looking.z * Config.hypertubePlaceReach), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getDirection();

        if (level.getBlockState(blockpos).isAir()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

        if(!selectedBlock1.get(stack)){ // get first block pos
            if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){ // if clicking on hypertyubeSupportBlock

                boolean result = calcBlockIfHyperTubeSupport(level,stack,blockpos,block1Pos,block1Axis,block1Direction);
                if (!result) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

                selectedBlock1.put(stack,true);
            }
            else { //if clicking on ground to start placing a hypertube
                if (!level.getBlockState(blockpos.relative(placeDirection)).is(ModTags.Blocks.DONT_OBSTRUCT_HYPERTUBE)||!level.getBlockState(blockpos.relative(placeDirection,2)).is(ModTags.Blocks.DONT_OBSTRUCT_HYPERTUBE)) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

                boolean result = calcBlockIfBlock(level,stack,blockpos,player,placeDirection,block1Pos,block1Axis,block1Direction);
                if (!result) return InteractionResultHolder.fail(player.getItemInHand(usedHand));


                placeHypertubeSupport(level,block1Pos.get(stack),block1Axis.get(stack), placeDirection);
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

                if (!level.getBlockState(blockpos.relative(placeDirection)).is(ModTags.Blocks.DONT_OBSTRUCT_HYPERTUBE)||!level.getBlockState(blockpos.relative(placeDirection,2)).is(ModTags.Blocks.DONT_OBSTRUCT_HYPERTUBE)) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

                boolean result = calcBlockIfBlock(level,stack,blockpos,player,placeDirection,block2Pos,block2Axis,block2Direction);
                if (!result) return InteractionResultHolder.fail(player.getItemInHand(usedHand));
            }

            HyperTubeCalcCore curveCore = new HyperTubeCalcCore();
            curveCore.setData(block1Pos.get(stack),block1Axis.get(stack),block1Direction.get(stack), extraData1.get(stack), block2Pos.get(stack),block2Axis.get(stack),block2Direction.get(stack), extraData2.get(stack));
            BlockPos[] blockPosArray = curveCore.getHyperTubeBlockPosArray(this.curveType.get(stack));

            boolean isValidCurve = HyperTubeUtil.checkValidCurve(level,blockPosArray,player,Config.maxHypertubeLength);
            if (!isValidCurve) return InteractionResultHolder.pass(player.getItemInHand(usedHand));

            for (int i = 0; i < blockPosArray.length; i++) {
                if (level.getBlockState(blockPosArray[i]).is(hyperTubeSupportBlock)) continue;
                level.setBlock(blockPosArray[i], hyperTubeBlock.defaultBlockState(),2 );
                level.blockUpdated(blockPosArray[i],hyperTubeBlock);
            }
            if(!player.isShiftKeyDown()){
                removeItems(player, Items.GLASS_PANE,blockPosArray.length);
                removeItems(player, Items.GOLD_NUGGET,blockPosArray.length);
            }

            placeHypertubeSupport(level,block2Pos.get(stack),block2Axis.get(stack), placeDirection);

//            if (this.curveType.get(stack) == CurveTypes.Curves.MINECRAFT){
//                this.extraData1.put(stack,"isFirst");
//            }

            modifyData(level,block1Pos.get(stack),curveCore.block1UsedDirection,block2Pos.get(stack), curveType.get(stack),extraData1.get(stack));
            modifyData(level,block2Pos.get(stack),curveCore.block2UsedDirection,block1Pos.get(stack), curveType.get(stack),extraData2.get(stack));

            selectedBlock1.put(stack,false);
        }

        return super.use(level, player, usedHand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide()) return;



        if(entity instanceof Player player){

            ItemStack selectedItem = player.getMainHandItem().getItem() == ModItems.HYPERTUBE_PLACER.get()? player.getMainHandItem() : player.getOffhandItem();
            if(selectedItem != stack) return;

            this.selectedBlock1.putIfAbsent(stack, false);
            this.curveType.putIfAbsent(stack, CurveTypes.Curves.CURVED);

            Vec3 looking = entity.getLookAngle();
            if(selectedBlock1.get(stack)) {
                BlockPos blockpos = level.clip(new ClipContext(entity.getEyePosition(), entity.getEyePosition().add(looking.x * Config.hypertubePlaceReach, looking.y * Config.hypertubePlaceReach, looking.z * Config.hypertubePlaceReach), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
                Direction placeDirection = level.clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(looking.x * Config.hypertubePlaceReach, looking.y * Config.hypertubePlaceReach, looking.z * Config.hypertubePlaceReach), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getDirection();

                if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){ // if clicking on hypertyubeSupportBlock

                    boolean result = calcBlockIfHyperTubeSupport(level,stack,blockpos,block2Pos,block2Axis,block2Direction);
                    if (!result) return;
                }
                else {

                    if (!level.getBlockState(blockpos).is(ModTags.Blocks.DONT_OBSTRUCT_HYPERTUBE) && level.getBlockState(blockpos.above(1)).is(ModTags.Blocks.DONT_OBSTRUCT_HYPERTUBE) && level.getBlockState(blockpos.above(2)).is(ModTags.Blocks.DONT_OBSTRUCT_HYPERTUBE)) {
                        boolean result = calcBlockIfBlock(level,stack,blockpos,player,placeDirection,block2Pos,block2Axis,block2Direction);
                        if (!result) return;
                    }
                    else {
                        boolean result = calcBlockIfAir(level,stack,blockpos,player,block2Pos,block2Axis,block2Direction);
                        if (!result) return;
                    }
                }
                HyperTubeCalcCore curveCore = new HyperTubeCalcCore();
                curveCore.setData(block1Pos.get(stack),block1Axis.get(stack),block1Direction.get(stack), extraData1.get(stack), block2Pos.get(stack),block2Axis.get(stack),block2Direction.get(stack), extraData2.get(stack));
                BlockPos[] blockPosArray = curveCore.getHyperTubeBlockPosArray(this.curveType.get(stack));

                boolean isValidCurve = HyperTubeUtil.checkValidCurve(level,blockPosArray,player,Config.maxHypertubeLength);
                Vector3f color = isValidCurve ?new Vector3f(0,255,255) : new Vector3f(255,0,0);


                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new HyperTubeOverlayPacket(this.curveType.get(stack).toString(), blockPosArray.length - 2, Config.maxHypertubeLength, HyperTubeUtil.getResourcesCount(player), isValidCurve));
                }

                for (int i = 0; i < blockPosArray.length; i++) {
                    for(int j = 0; j < level.players().size(); ++j) {
                        ServerPlayer serverplayer = (ServerPlayer)level.players().get(j);
                        ((ServerLevel) level).sendParticles(serverplayer, new DustParticleOptions(color,1), true,
                            blockPosArray[i].getX()+0.5,blockPosArray[i].getY()+0.5,blockPosArray[i].getZ()+0.5, 1, 0.2, 0.2, 0.2,1);
                    }
                }

                //display support direction ( with particles)
                BlockPos startpos = block2Pos.get(stack);
                BlockPos endpos = block2Pos.get(stack).relative(block2Axis.get(stack), -curveCore.block2UsedDirection);
                Vec3 vec = endpos.getCenter().subtract(startpos.getCenter()).scale(1/5.0);
                for (int i = -5; i < 6; i++) {
                    for(int j = 0; j < level.players().size(); ++j) {
                        ServerPlayer serverplayer = (ServerPlayer)level.players().get(j);
                        ((ServerLevel) level).sendParticles(serverplayer, new DustParticleOptions(new Vector3f(1,1,1),0.5f), true,
                                block2Pos.get(stack).getX()+0.5+vec.x*i,block2Pos.get(stack).getY()+0.5+vec.y*i,block2Pos.get(stack).getZ()+0.5+vec.z*i, 1, 0, 0, 0,1);
                    }
                }


            }else{
                if (player instanceof ServerPlayer serverPlayer) {
                    // if not selected block 1 => send packet for only "Type" overlay, no length / resources
                    serverPlayer.connection.send(new HyperTubeOverlayPacket(this.curveType.get(stack).toString(), -1,-1,-1,false));
                }
            }
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) { // cancel when left-clicking
        if(!entity.isShiftKeyDown()){ // entity swings when shift + right click, this fixes the problem
            selectedBlock1.put(stack,false);
        }
        return super.onEntitySwing(stack, entity, hand);

    }
}
