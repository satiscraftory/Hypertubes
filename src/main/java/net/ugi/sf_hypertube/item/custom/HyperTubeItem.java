package net.ugi.sf_hypertube.item.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.ugi.sf_hypertube.block.ModBlocks;
import net.ugi.sf_hypertube.item.ModItems;
import org.joml.Vector3f;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.WeakHashMap;


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




    double bezierHelpPosMultiplier =0.5;//default 0.5
    DeferredBlock<Block> hyperTubeSupportBlock = ModBlocks.HYPERTUBE_SUPPORT;
    Block hyperTubeBlock = Blocks.GLASS;
    double placeDistance = 20;

    private BlockPos[] calcBezierArray(BlockPos b1Pos, Direction.Axis b1Axis, int b1Direction,BlockPos b2Pos, Direction.Axis b2Axis, int b2Direction){
        BlockPos pos0 = b1Pos;
        BlockPos pos1 = null;
        BlockPos pos2 = null;
        BlockPos pos3 = b2Pos;
        double distanceBetweenBlocks =  b2Pos.getCenter().distanceTo(b1Pos.getCenter());
        double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;

        if (b1Direction == 0) { // 2 sides available
            pos1 = b2Pos.getCenter().distanceTo(b1Pos.offset(getVectorFromAxis(b1Axis, (int) helperPosOffSet)).getCenter()) < b2Pos.getCenter().distanceTo(b1Pos.offset(getVectorFromAxis(b1Axis, -(int) helperPosOffSet)).getCenter()) ? b1Pos.offset(getVectorFromAxis(b1Axis, (int) helperPosOffSet)) : b1Pos.offset(getVectorFromAxis(b1Axis, -(int) helperPosOffSet));
        }
        else { // 1 side available
            pos1 = b1Pos.offset(getVectorFromAxis(b1Axis, (int) helperPosOffSet * b1Direction));
        }

        if (b2Direction == 0) { // 2 sides available
            pos2 = b1Pos.getCenter().distanceTo(b2Pos.offset(getVectorFromAxis(b2Axis,(int)helperPosOffSet)).getCenter()) < b1Pos.getCenter().distanceTo(b2Pos.offset(getVectorFromAxis(b2Axis,-(int)helperPosOffSet)).getCenter())  ? b2Pos.offset(getVectorFromAxis(b2Axis,(int)helperPosOffSet)) : b2Pos.offset(getVectorFromAxis(b2Axis,-(int)helperPosOffSet));
        }
        else { // 1 side available
            pos2 = b2Pos.offset(getVectorFromAxis(b2Axis,(int)helperPosOffSet * b2Direction));
        }

        int steps = (int)(1.5*Math.abs(b1Pos.getX() - b2Pos.getX()) + 1.5*Math.abs(b1Pos.getY() - b2Pos.getY()) + 1.5*Math.abs(b1Pos.getZ() - b2Pos.getZ()) + 10);


        BlockPos[] blockPosArray = new BlockPos[steps];

        //bezier curve calc
        //https://www.desmos.com/calculator/cahqdxeshd?lang=nl
        //3D version of this

        for(int i = 0 ; i < steps; i++) {
            double t = i/(double)steps;

            int x = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getX() + t*pos1.getX()) + t*((1-t)*pos1.getX() + t*pos2.getX())) + t*((1-t)*((1-t)*pos1.getX() + t*pos2.getX()) + t*((1-t)*pos2.getX() + t*pos3.getX())));
            int y = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getY() + t*pos1.getY()) + t*((1-t)*pos1.getY() + t*pos2.getY())) + t*((1-t)*((1-t)*pos1.getY() + t*pos2.getY()) + t*((1-t)*pos2.getY() + t*pos3.getY())));
            int z = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getZ() + t*pos1.getZ()) + t*((1-t)*pos1.getZ() + t*pos2.getZ())) + t*((1-t)*((1-t)*pos1.getZ() + t*pos2.getZ()) + t*((1-t)*pos2.getZ() + t*pos3.getZ())));

            blockPosArray[i] = new BlockPos(x,y,z);
        }
        Set<BlockPos> blockSet = new LinkedHashSet<>();
        for (BlockPos pos : blockPosArray) {
            if (pos != null) {
                blockSet.add(pos);
            }
        }

        return blockSet.toArray(new BlockPos[0]);
    }

    private BlockPos getVectorFromAxis(Direction.Axis axis, int offset){
        BlockPos vector = new BlockPos(offset,0,0);
        if (axis == Direction.Axis.X)vector = new BlockPos(offset,0,0);
        if (axis == Direction.Axis.Y)vector = new BlockPos(0,offset,0);
        if (axis == Direction.Axis.Z)vector = new BlockPos(0,0,offset);
        return vector;
    }

    private int intValue(boolean val){
        return val ? 1 : 0;
    }



    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        ItemStack stack = player.getItemInHand(usedHand);

        Vec3 looking = player.getLookAngle();
        BlockPos blockpos = level.clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(looking.x * placeDistance, looking.y * placeDistance, looking.z * placeDistance), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();


        if (level.getBlockState(blockpos).isAir()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

        if(!selectedBlock1.get(stack)){ // get first block pos
            if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){ // if clicking on hypertyubeSupportBlock
                block1Pos.put(stack,blockpos);
                block1Axis.put(stack,level.getBlockState(block1Pos.get(stack)).getValue(BlockStateProperties.AXIS));

                boolean dir1IsHyperTube = level.getBlockState(block1Pos.get(stack).offset(getVectorFromAxis(block1Axis.get(stack),1))).is(hyperTubeBlock);
                boolean dir2IsHyperTube = level.getBlockState(block1Pos.get(stack).offset(getVectorFromAxis(block1Axis.get(stack),-1))).is(hyperTubeBlock);

                if (dir1IsHyperTube && dir2IsHyperTube) return InteractionResultHolder.pass(player.getItemInHand(usedHand));; // no connection possible to this tube
                block1Direction.put(stack,intValue(dir2IsHyperTube) - intValue(dir1IsHyperTube)); // gives the direction the tubes need to be , if 0 => code chooses later ( both available)

                selectedBlock1.put(stack,true);
            }
            else { //if clicking on ground to start placing a hypertube
                block1Pos.put(stack,blockpos.above(2));
                block1Axis.put(stack,Direction.Axis.X); // default axis
                block1Direction.put(stack,0); // default

                assert player != null;
                Vec3 lookingVec = player.getLookAngle();
                Vec3 biasLookingVec = new Vec3(lookingVec.x,lookingVec.y/1.9,lookingVec.z);
                if (Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.z)) {
                    block1Axis.put(stack,Direction.Axis.X);
                }
                if (Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.x) && Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.z)) {
                    block1Axis.put(stack,Direction.Axis.Y);
                }
                if (Math.abs(biasLookingVec.z) > Math.abs(lookingVec.y) && Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.x)) {
                    block1Axis.put(stack,Direction.Axis.Z);
                }
                level.setBlock(block1Pos.get(stack), hyperTubeSupportBlock.get().defaultBlockState().setValue(BlockStateProperties.AXIS, block1Axis.get(stack)),2 );
                level.blockUpdated(block1Pos.get(stack),hyperTubeSupportBlock.get());
                level.setBlock(block1Pos.get(stack).below(1), Blocks.BRICK_WALL.defaultBlockState(),2 );
                level.blockUpdated(block1Pos.get(stack).below(1),Blocks.BRICK_WALL);
                selectedBlock1.put(stack,true);
            }
        }

        else { // get second block pos
            if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){
                block2Pos.put(stack,blockpos);
                block2Axis.put(stack,level.getBlockState(block2Pos.get(stack)).getValue(BlockStateProperties.AXIS));

                boolean dir1IsHyperTube = level.getBlockState(blockpos.offset(getVectorFromAxis(block2Axis.get(stack),1))).is(hyperTubeBlock);
                boolean dir2IsHyperTube = level.getBlockState(blockpos.offset(getVectorFromAxis(block2Axis.get(stack),-1))).is(hyperTubeBlock);

                if (dir1IsHyperTube && dir2IsHyperTube) return InteractionResultHolder.pass(player.getItemInHand(usedHand)); // no connection possible to this tube
                block2Direction.put(stack,intValue(dir2IsHyperTube) - intValue(dir1IsHyperTube)); // gives the direction the tubes need to be , if 0 => code chooses later ( both available)

            }
            else { //if clicking on ground to end placing a hypertube
                block2Pos.put(stack,blockpos.above(2));
                block2Axis.put(stack,Direction.Axis.X); // default axis
                block2Direction.put(stack,0); // default

                assert player != null;
                Vec3 lookingVec = player.getLookAngle();
                Vec3 biasLookingVec = new Vec3(lookingVec.x,lookingVec.y/1.9,lookingVec.z);
                if (Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.z)) {
                    block2Axis.put(stack,Direction.Axis.X);
                }
                if (Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.x) && Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.z)) {
                    block2Axis.put(stack,Direction.Axis.Y);
                }
                if (Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.x)) {
                    block2Axis.put(stack,Direction.Axis.Z);
                }
            }

            BlockPos[] blockPosArray = calcBezierArray(block1Pos.get(stack),block1Axis.get(stack),block1Direction.get(stack),block2Pos.get(stack),block2Axis.get(stack),block2Direction.get(stack));
            boolean isValid = true;
            for (int i = 0; i < blockPosArray.length; i++) {
                if(!(level.getBlockState(blockPosArray[i]).isAir() || level.getBlockState(blockPosArray[i]).is(ModBlocks.HYPERTUBE_SUPPORT))){
                    isValid = false;
                }
            }
            if (!isValid) return InteractionResultHolder.pass(player.getItemInHand(usedHand));

            for (int i = 0; i < blockPosArray.length; i++) {
                if (level.getBlockState(blockPosArray[i]).is(hyperTubeSupportBlock)) continue;
                level.setBlock(blockPosArray[i], hyperTubeBlock.defaultBlockState(),2 );
                level.blockUpdated(blockPosArray[i],hyperTubeBlock);
            }
            level.setBlock(block2Pos.get(stack), hyperTubeSupportBlock.get().defaultBlockState().setValue(BlockStateProperties.AXIS, block2Axis.get(stack)),2 );
            level.blockUpdated(block2Pos.get(stack),hyperTubeSupportBlock.get());
            level.setBlock(block2Pos.get(stack).below(1), Blocks.BRICK_WALL.defaultBlockState(),2 );
            level.blockUpdated(block2Pos.get(stack).below(1),Blocks.BRICK_WALL);
            selectedBlock1.put(stack,false);
        }

        return super.use(level, player, usedHand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide()) return;
        Player player = level.getNearestPlayer(entity,0.01);
        ItemStack selectedItem = player.getMainHandItem().getItem() == ModItems.HYPERTUBE.get()? player.getMainHandItem() : player.getOffhandItem();
        if(selectedItem != stack) return;

        selectedBlock1.putIfAbsent(stack, false);

        Vec3 looking = entity.getLookAngle();
        if(selectedBlock1.get(stack)) {
            block2Pos.put(stack,level.clip(new ClipContext(entity.getEyePosition(), entity.getEyePosition().add(looking.x * placeDistance, looking.y * placeDistance, looking.z * placeDistance), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos());

            if (level.getBlockState(block2Pos.get(stack)).is(hyperTubeSupportBlock)){ // if clicking on hypertyubeSupportBlock
                block2Axis.put(stack,level.getBlockState(block2Pos.get(stack)).getValue(BlockStateProperties.AXIS));

                boolean dir1IsHyperTube = level.getBlockState(block2Pos.get(stack).offset(getVectorFromAxis(block2Axis.get(stack),1))).is(hyperTubeBlock);
                boolean dir2IsHyperTube = level.getBlockState(block2Pos.get(stack).offset(getVectorFromAxis(block2Axis.get(stack),-1))).is(hyperTubeBlock);

                if (!(dir1IsHyperTube && dir2IsHyperTube)) { // no connection possible to this tube
                    block2Direction.put(stack,intValue(dir2IsHyperTube) - intValue(dir1IsHyperTube)); // gives the direction the tubes need to be , if 0 => code chooses later ( both available)
                }
            }
            else {

                if (!level.getBlockState(block2Pos.get(stack)).isAir()) {
                    block2Pos.put(stack,block2Pos.get(stack).above(2));
                }


                Vec3 biasLookingVec = new Vec3(looking.x, looking.y / 1.9, looking.z);
                if (Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.z)) {
                    block2Axis.put(stack,Direction.Axis.X);
                    block2Direction.put(stack,0);//(int)(lookingVec.x / Math.abs(lookingVec.x));
                }
                if (Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.x) && Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.z)) {
                    block2Axis.put(stack,Direction.Axis.Y);
                    block2Direction.put(stack,0);//(int)(lookingVec.y / Math.abs(lookingVec.y));
                }
                if (Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.x)) {
                    block2Axis.put(stack,Direction.Axis.Z);
                    block2Direction.put(stack,0);//(int)(lookingVec.z / Math.abs(lookingVec.z));
                }
            }

            BlockPos[] blockPosArray = calcBezierArray(block1Pos.get(stack),block1Axis.get(stack),block1Direction.get(stack),block2Pos.get(stack),block2Axis.get(stack),block2Direction.get(stack));

            Vector3f color = new Vector3f(0,255,255);
            boolean isValid = true;
            for (int i = 0; i < blockPosArray.length; i++) {
                if(!(level.getBlockState(blockPosArray[i]).isAir() || level.getBlockState(blockPosArray[i]).is(ModBlocks.HYPERTUBE_SUPPORT))){
                    isValid = false;
                }
            }
            if (!isValid) {
                color = new Vector3f(255,0,0);
            }
            for (int i = 0; i < blockPosArray.length; i++) {
                for(int j = 0; j < level.players().size(); ++j) {
                    ServerPlayer serverplayer = (ServerPlayer)level.players().get(j);
                    ((ServerLevel) level).sendParticles(serverplayer, new DustParticleOptions(color,1), true,
                        blockPosArray[i].getX()+0.5,blockPosArray[i].getY()+0.5,blockPosArray[i].getZ()+0.5, 1, 0.2, 0.2, 0.2,1);
                }
                //((ServerLevel) level).sendParticles( new DustParticleOptions(new Vector3f(0,255,255),1),
                        //blockPosArray[i].getX()+0.5,blockPosArray[i].getY()+0.5,blockPosArray[i].getZ()+0.5, 1, 0.2, 0.2, 0.2,1);

                //level.addParticle(new DustParticleOptions(new Vector3f(0,255,255),1), true,blockPosArray[i].getX()+0.5,blockPosArray[i].getY()+0.5,blockPosArray[i].getZ()+0.5, 0.2,0.2,0.2);
            }
        }
    }
}
