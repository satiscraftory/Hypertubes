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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.ugi.sf_hypertube.block.ModBlocks;
import org.joml.Vector3f;

import java.util.Date;

public class HyperTubeItem extends Item {
    public HyperTubeItem(Item.Properties properties) {
        super(properties);
    }


    BlockPos block1Pos;
    BlockState block1State;
    boolean selectedBlock1 = false;
    BlockPos block2Pos;
    BlockState block2State;

    private BlockPos getVectorFromAxis(Direction.Axis axis, int offset){
        BlockPos vector = new BlockPos(offset,0,0);
        if (axis == Direction.Axis.X)vector = new BlockPos(offset,0,0);
        if (axis == Direction.Axis.Y)vector = new BlockPos(0,offset,0);
        if (axis == Direction.Axis.Z)vector = new BlockPos(0,0,offset);
        return vector;
    }


    /**
     * Called when this item is used when targeting a Block
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        if (level.isClientSide()) return InteractionResult.FAIL;
        //if (!level.getBlockState(blockpos).is(ModBlocks.HYPERTUBE_SUPPORT)) return InteractionResult.FAIL;


        if(!selectedBlock1) {
            block1Pos = blockpos;
            block1State = level.getBlockState(block1Pos);
            selectedBlock1 = true;
        }
        else {
            System.out.println(new Date().getTime());
            block2Pos = blockpos;
            block2State = level.getBlockState(block2Pos);

            //bezier curve calc
            //https://www.desmos.com/calculator/cahqdxeshd?lang=nl
            //3D version of this
            BlockPos pos0 = block1Pos;
            BlockPos pos1;
            BlockPos pos2;
            BlockPos pos3 = block2Pos;
            double distanceBetweenBlocks =  block2Pos.getCenter().distanceTo(block1Pos.getCenter());
            double bezierHelpPosMultiplier = 0.5;//default 0.5
            double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;
            pos1 = block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)).getCenter()) < block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet)).getCenter())  ? block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)) : block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet));
            pos2 = block1Pos.getCenter().distanceTo(block2Pos.offset(getVectorFromAxis(block2State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)).getCenter()) < block1Pos.getCenter().distanceTo(block2Pos.offset(getVectorFromAxis(block2State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet)).getCenter())  ? block2Pos.offset(getVectorFromAxis(block2State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)) : block2Pos.offset(getVectorFromAxis(block2State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet));

            int steps = (int)(1.5*Math.abs(block1Pos.getX() - block2Pos.getX()) + 1.5*Math.abs(block1Pos.getY() - block2Pos.getY()) + 1.5*Math.abs(block1Pos.getZ() - block2Pos.getZ()) + 10);

            BlockPos[] blockPosArray = new BlockPos[steps];

            for(int i = 0 ; i < steps; i++) {
                double t = i/(double)steps;

                 int x = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getX() + t*pos1.getX()) + t*((1-t)*pos1.getX() + t*pos2.getX())) + t*((1-t)*((1-t)*pos1.getX() + t*pos2.getX()) + t*((1-t)*pos2.getX() + t*pos3.getX())));
                 int y = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getY() + t*pos1.getY()) + t*((1-t)*pos1.getY() + t*pos2.getY())) + t*((1-t)*((1-t)*pos1.getY() + t*pos2.getY()) + t*((1-t)*pos2.getY() + t*pos3.getY())));
                 int z = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getZ() + t*pos1.getZ()) + t*((1-t)*pos1.getZ() + t*pos2.getZ())) + t*((1-t)*((1-t)*pos1.getZ() + t*pos2.getZ()) + t*((1-t)*pos2.getZ() + t*pos3.getZ())));

                 blockPosArray[i] = new BlockPos(x,y,z);
            }
            System.out.println(new Date().getTime());
            for (int i = 0; i < steps; i++) {
                level.setBlock(blockPosArray[i], Blocks.GLASS.defaultBlockState(),2 );
                level.blockUpdated(blockPosArray[i],Blocks.GLASS);
            }
            selectedBlock1 = false;
            System.out.println(new Date().getTime());
        }
        return InteractionResult.SUCCESS;

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(level.isClientSide()) return InteractionResultHolder.pass(player.getItemInHand(usedHand));


        if(selectedBlock1) {
            Vec3 vec =  player.getLookAngle().multiply(new Vec3(3,3,3)).add(player.position());
            block2Pos = new BlockPos((int)Math.round(vec.x),(int)Math.round(vec.y),(int)Math.round(vec.z));
            block2State = level.getBlockState(block2Pos);

            Vec3 lookingVec = player.getLookAngle();
            if (Math.abs(lookingVec.x) > Math.abs(lookingVec.y) && Math.abs(lookingVec.x) > Math.abs(lookingVec.z)) {
                lookingVec = new Vec3(lookingVec.x / Math.abs(lookingVec.x),0,0);
            }
            if (Math.abs(lookingVec.y) > Math.abs(lookingVec.x) && Math.abs(lookingVec.y) > Math.abs(lookingVec.z)) {
                lookingVec = new Vec3(0,lookingVec.y / Math.abs(lookingVec.y),0);
            }
            if (Math.abs(lookingVec.z) > Math.abs(lookingVec.y) && Math.abs(lookingVec.z) > Math.abs(lookingVec.x)) {
                lookingVec = new Vec3(0,0,lookingVec.z / Math.abs(lookingVec.z));
            }


            //bezier curve calc
            //https://www.desmos.com/calculator/cahqdxeshd?lang=nl
            //3D version of this
            BlockPos pos0 = block1Pos;
            BlockPos pos1;
            BlockPos pos2;
            BlockPos pos3 = block2Pos;
            double distanceBetweenBlocks =  block2Pos.getCenter().distanceTo(block1Pos.getCenter());
            double bezierHelpPosMultiplier = 0.5;//default 0.5
            double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;
            pos1 = block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)).getCenter()) < block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet)).getCenter())  ? block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)) : block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet));
            Vec3 offset =  lookingVec.multiply(new Vec3((int)helperPosOffSet,(int)helperPosOffSet,(int)helperPosOffSet));
            pos2 = block2Pos.offset(new Vec3i((int)Math.round(offset.x),(int)Math.round(offset.y),(int)Math.round(offset.z)));

            int steps = (int)(1.5*Math.abs(block1Pos.getX() - block2Pos.getX()) + 1.5*Math.abs(block1Pos.getY() - block2Pos.getY()) + 1.5*Math.abs(block1Pos.getZ() - block2Pos.getZ()) + 10);

            BlockPos[] blockPosArray = new BlockPos[steps];

            for(int i = 0 ; i < steps; i++) {
                double t = i/(double)steps;

                int x = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getX() + t*pos1.getX()) + t*((1-t)*pos1.getX() + t*pos2.getX())) + t*((1-t)*((1-t)*pos1.getX() + t*pos2.getX()) + t*((1-t)*pos2.getX() + t*pos3.getX())));
                int y = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getY() + t*pos1.getY()) + t*((1-t)*pos1.getY() + t*pos2.getY())) + t*((1-t)*((1-t)*pos1.getY() + t*pos2.getY()) + t*((1-t)*pos2.getY() + t*pos3.getY())));
                int z = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getZ() + t*pos1.getZ()) + t*((1-t)*pos1.getZ() + t*pos2.getZ())) + t*((1-t)*((1-t)*pos1.getZ() + t*pos2.getZ()) + t*((1-t)*pos2.getZ() + t*pos3.getZ())));

                blockPosArray[i] = new BlockPos(x,y,z);
            }
            Vector3f color = new Vector3f(0,255,255);
            boolean isValid = true;
            for (int i = 0; i < steps; i++) {
                if(!(level.getBlockState(blockPosArray[i]).isAir() || level.getBlockState(blockPosArray[i]).is(ModBlocks.HYPERTUBE_SUPPORT))){
                    isValid = false;
                }
            }
            if (!isValid) {
                color = new Vector3f(255,0,0);
            }

            for (int i = 0; i < steps; i++) {
                level.setBlock(blockPosArray[i], Blocks.GLASS.defaultBlockState(),2 );
                level.blockUpdated(blockPosArray[i],Blocks.GLASS);
            }
        }
        selectedBlock1 = false;
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide()) return;


        if(selectedBlock1) {
            Vec3 vec =  entity.getLookAngle().multiply(new Vec3(3,3,3)).add(entity.position());
            block2Pos = new BlockPos((int)Math.round(vec.x),(int)Math.round(vec.y),(int)Math.round(vec.z));
            block2State = level.getBlockState(block2Pos);

            Vec3 lookingVec = entity.getLookAngle();
            if (Math.abs(lookingVec.x) > Math.abs(lookingVec.y) && Math.abs(lookingVec.x) > Math.abs(lookingVec.z)) {
                lookingVec = new Vec3(lookingVec.x / Math.abs(lookingVec.x),0,0);
            }
            if (Math.abs(lookingVec.y) > Math.abs(lookingVec.x) && Math.abs(lookingVec.y) > Math.abs(lookingVec.z)) {
                lookingVec = new Vec3(0,lookingVec.y / Math.abs(lookingVec.y),0);
            }
            if (Math.abs(lookingVec.z) > Math.abs(lookingVec.y) && Math.abs(lookingVec.z) > Math.abs(lookingVec.x)) {
                lookingVec = new Vec3(0,0,lookingVec.z / Math.abs(lookingVec.z));
            }


            //bezier curve calc
            //https://www.desmos.com/calculator/cahqdxeshd?lang=nl
            //3D version of this
            BlockPos pos0 = block1Pos;
            BlockPos pos1;
            BlockPos pos2;
            BlockPos pos3 = block2Pos;
            double distanceBetweenBlocks =  block2Pos.getCenter().distanceTo(block1Pos.getCenter());
            double bezierHelpPosMultiplier = 0.5;//default 0.5
            double helperPosOffSet = distanceBetweenBlocks * bezierHelpPosMultiplier;
            pos1 = block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)).getCenter()) < block2Pos.getCenter().distanceTo(block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet)).getCenter())  ? block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),(int)helperPosOffSet)) : block1Pos.offset(getVectorFromAxis(block1State.getValue(BlockStateProperties.AXIS),-(int)helperPosOffSet));
            Vec3 offset =  lookingVec.multiply(new Vec3((int)helperPosOffSet,(int)helperPosOffSet,(int)helperPosOffSet));
            pos2 = block2Pos.offset(new Vec3i((int)Math.round(offset.x),(int)Math.round(offset.y),(int)Math.round(offset.z)));

            int steps = (int)(1.5*Math.abs(block1Pos.getX() - block2Pos.getX()) + 1.5*Math.abs(block1Pos.getY() - block2Pos.getY()) + 1.5*Math.abs(block1Pos.getZ() - block2Pos.getZ()) + 10);

            BlockPos[] blockPosArray = new BlockPos[steps];

            for(int i = 0 ; i < steps; i++) {
                double t = i/(double)steps;

                int x = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getX() + t*pos1.getX()) + t*((1-t)*pos1.getX() + t*pos2.getX())) + t*((1-t)*((1-t)*pos1.getX() + t*pos2.getX()) + t*((1-t)*pos2.getX() + t*pos3.getX())));
                int y = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getY() + t*pos1.getY()) + t*((1-t)*pos1.getY() + t*pos2.getY())) + t*((1-t)*((1-t)*pos1.getY() + t*pos2.getY()) + t*((1-t)*pos2.getY() + t*pos3.getY())));
                int z = (int)Math.round((1-t)*((1-t)*((1-t)*pos0.getZ() + t*pos1.getZ()) + t*((1-t)*pos1.getZ() + t*pos2.getZ())) + t*((1-t)*((1-t)*pos1.getZ() + t*pos2.getZ()) + t*((1-t)*pos2.getZ() + t*pos3.getZ())));

                blockPosArray[i] = new BlockPos(x,y,z);
            }
            Vector3f color = new Vector3f(0,255,255);
            boolean isValid = true;
            for (int i = 0; i < steps; i++) {
                if(!(level.getBlockState(blockPosArray[i]).isAir() || level.getBlockState(blockPosArray[i]).is(ModBlocks.HYPERTUBE_SUPPORT))){
                    isValid = false;
                }
            }
            if (!isValid) {
                color = new Vector3f(255,0,0);
            }
            for (int i = 0; i < steps; i++) {
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
