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
import org.joml.Vector3f;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;


public class HyperTubeItem extends Item {
    public HyperTubeItem(Properties properties) {
        super(properties);
    }

    BlockPos block1Pos;
    Direction.Axis block1Axis;
    int block1Direction = 0;
    boolean selectedBlock1 = false;
    BlockPos block2Pos;
    Direction.Axis block2Axis;
    int block2Direction = 0;

    double bezierHelpPosMultiplier =0.5;//default 0.5
    DeferredBlock<Block> hyperTubeSupportBlock = ModBlocks.HYPERTUBE_SUPPORT;
    Block hyperTubeBlock = Blocks.GLASS;
    double placeDistance = 20;

    private BlockPos[] calcBezierArray(){
        BlockPos pos0 = block1Pos;
        BlockPos pos1 = null;
        BlockPos pos2 = null;
        BlockPos pos3 = block2Pos;
        double distanceBetweenBlocks =  block2Pos.getCenter().distanceTo(block1Pos.getCenter());
        double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;

        if (block1Direction == 0) { // 2 sides available
            pos1 = block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1Axis, (int) helperPosOffSet)).getCenter()) < block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1Axis, -(int) helperPosOffSet)).getCenter()) ? block1Pos.offset(getVectorFromAxis(block1Axis, (int) helperPosOffSet)) : block1Pos.offset(getVectorFromAxis(block1Axis, -(int) helperPosOffSet));
        }
        else { // 1 side available
            pos1 = block1Pos.offset(getVectorFromAxis(block1Axis, (int) helperPosOffSet * block1Direction));
        }

        if (block2Direction == 0) { // 2 sides available
            pos2 = block1Pos.getCenter().distanceTo(block2Pos.offset(getVectorFromAxis(block2Axis,(int)helperPosOffSet)).getCenter()) < block1Pos.getCenter().distanceTo(block2Pos.offset(getVectorFromAxis(block2Axis,-(int)helperPosOffSet)).getCenter())  ? block2Pos.offset(getVectorFromAxis(block2Axis,(int)helperPosOffSet)) : block2Pos.offset(getVectorFromAxis(block2Axis,-(int)helperPosOffSet));
        }
        else { // 1 side available
            pos2 = block2Pos.offset(getVectorFromAxis(block2Axis,(int)helperPosOffSet * block2Direction));
        }

        int steps = (int)(1.5*Math.abs(block1Pos.getX() - block2Pos.getX()) + 1.5*Math.abs(block1Pos.getY() - block2Pos.getY()) + 1.5*Math.abs(block1Pos.getZ() - block2Pos.getZ()) + 10);


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
        Vec3 looking = player.getLookAngle();
        BlockPos blockpos = level.clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(looking.x * placeDistance, looking.y * placeDistance, looking.z * placeDistance), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();


        if (level.getBlockState(blockpos).isAir()) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

        if(!selectedBlock1){ // get first block pos
            if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){ // if clicking on hypertyubeSupportBlock
                block1Pos = blockpos;
                block1Axis = level.getBlockState(block1Pos).getValue(BlockStateProperties.AXIS);

                boolean dir1IsHyperTube = level.getBlockState(block1Pos.offset(getVectorFromAxis(block1Axis,1))).is(hyperTubeBlock);
                boolean dir2IsHyperTube = level.getBlockState(block1Pos.offset(getVectorFromAxis(block1Axis,-1))).is(hyperTubeBlock);

                if (dir1IsHyperTube && dir2IsHyperTube) return InteractionResultHolder.pass(player.getItemInHand(usedHand));; // no connection possible to this tube
                block1Direction = intValue(dir2IsHyperTube) - intValue(dir1IsHyperTube); // gives the direction the tubes need to be , if 0 => code chooses later ( both available)

                selectedBlock1 = true;
            }
            else { //if clicking on ground to start placing a hypertube
                block1Pos = blockpos.above(2);
                block1Axis = Direction.Axis.X; // default axis
                block1Direction = 0; // default

                assert player != null;
                Vec3 lookingVec = player.getLookAngle();
                Vec3 biasLookingVec = new Vec3(lookingVec.x,lookingVec.y/1.9,lookingVec.z);
                if (Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.z)) {
                    block1Axis = Direction.Axis.X;
                }
                if (Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.x) && Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.z)) {
                    block1Axis = Direction.Axis.Y;
                }
                if (Math.abs(biasLookingVec.z) > Math.abs(lookingVec.y) && Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.x)) {
                    block1Axis = Direction.Axis.Z;
                }
                level.setBlock(block1Pos, hyperTubeSupportBlock.get().defaultBlockState().setValue(BlockStateProperties.AXIS, block1Axis),2 );
                level.blockUpdated(block1Pos,hyperTubeSupportBlock.get());
                level.setBlock(block1Pos.below(1), Blocks.BRICK_WALL.defaultBlockState(),2 );
                level.blockUpdated(block1Pos.below(1),Blocks.BRICK_WALL);
                selectedBlock1 = true;
            }
        }

        else { // get second block pos
            if (level.getBlockState(blockpos).is(hyperTubeSupportBlock)){
                block2Pos = blockpos;
                block2Axis = level.getBlockState(block2Pos).getValue(BlockStateProperties.AXIS);

                boolean dir1IsHyperTube = level.getBlockState(blockpos.offset(getVectorFromAxis(block2Axis,1))).is(hyperTubeBlock);
                boolean dir2IsHyperTube = level.getBlockState(blockpos.offset(getVectorFromAxis(block2Axis,-1))).is(hyperTubeBlock);

                if (dir1IsHyperTube && dir2IsHyperTube) return InteractionResultHolder.pass(player.getItemInHand(usedHand)); // no connection possible to this tube
                block2Direction = intValue(dir2IsHyperTube) - intValue(dir1IsHyperTube); // gives the direction the tubes need to be , if 0 => code chooses later ( both available)

            }
            else { //if clicking on ground to end placing a hypertube
                block2Pos = blockpos.above(2);
                block2Axis = Direction.Axis.X; // default axis
                block2Direction = 0; // default

                assert player != null;
                Vec3 lookingVec = player.getLookAngle();
                Vec3 biasLookingVec = new Vec3(lookingVec.x,lookingVec.y/1.9,lookingVec.z);
                if (Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.z)) {
                    block2Axis = Direction.Axis.X;
                }
                if (Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.x) && Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.z)) {
                    block2Axis = Direction.Axis.Y;
                }
                if (Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.x)) {
                    block2Axis = Direction.Axis.Z;
                }
            }

            BlockPos[] blockPosArray = calcBezierArray();
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
            level.setBlock(block2Pos, hyperTubeSupportBlock.get().defaultBlockState().setValue(BlockStateProperties.AXIS, block2Axis),2 );
            level.blockUpdated(block2Pos,hyperTubeSupportBlock.get());
            level.setBlock(block2Pos.below(1), Blocks.BRICK_WALL.defaultBlockState(),2 );
            level.blockUpdated(block2Pos.below(1),Blocks.BRICK_WALL);
            selectedBlock1 = false;
        }

        return super.use(level, player, usedHand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide()) return;
        Vec3 looking = entity.getLookAngle();
        if(selectedBlock1) {
            block2Pos =  level.clip(new ClipContext(entity.getEyePosition(), entity.getEyePosition().add(looking.x * placeDistance, looking.y * placeDistance, looking.z * placeDistance), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();

            if (level.getBlockState(block2Pos).is(hyperTubeSupportBlock)){ // if clicking on hypertyubeSupportBlock
                block2Axis = level.getBlockState(block2Pos).getValue(BlockStateProperties.AXIS);

                boolean dir1IsHyperTube = level.getBlockState(block2Pos.offset(getVectorFromAxis(block2Axis,1))).is(hyperTubeBlock);
                boolean dir2IsHyperTube = level.getBlockState(block2Pos.offset(getVectorFromAxis(block2Axis,-1))).is(hyperTubeBlock);

                if (!(dir1IsHyperTube && dir2IsHyperTube)) { // no connection possible to this tube
                    block2Direction = intValue(dir2IsHyperTube) - intValue(dir1IsHyperTube); // gives the direction the tubes need to be , if 0 => code chooses later ( both available)
                }
            }
            else {

                if (!level.getBlockState(block2Pos).isAir()) {
                    block2Pos = block2Pos.above(2);
                }


                Vec3 biasLookingVec = new Vec3(looking.x, looking.y / 1.9, looking.z);
                if (Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.x) > Math.abs(biasLookingVec.z)) {
                    block2Axis = Direction.Axis.X;
                    block2Direction = 0;//(int)(lookingVec.x / Math.abs(lookingVec.x));
                }
                if (Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.x) && Math.abs(biasLookingVec.y) > Math.abs(biasLookingVec.z)) {
                    block2Axis = Direction.Axis.Y;
                    block2Direction = 0;//(int)(lookingVec.y / Math.abs(lookingVec.y));
                }
                if (Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.y) && Math.abs(biasLookingVec.z) > Math.abs(biasLookingVec.x)) {
                    block2Axis = Direction.Axis.Z;
                    block2Direction = 0;//(int)(lookingVec.z / Math.abs(lookingVec.z));
                }
            }

            BlockPos[] blockPosArray = calcBezierArray();

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
