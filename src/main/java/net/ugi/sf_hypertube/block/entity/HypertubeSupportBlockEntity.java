package net.ugi.sf_hypertube.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;



public class HypertubeSupportBlockEntity extends BlockEntity {

    public BlockPos targetPositive = null;
    public BlockPos targetNegative = null;
    public String targetPositiveType = null;
    public String targetNegativeType = null;
    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    //constructor
    public HypertubeSupportBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HYPERTUBE_SUPPORT_BE.get(), pos, blockState);
    }

    public void clearContents() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        if(targetPositive != null) {
            tag.putIntArray("target_positive", List.of(targetPositive.getX(), targetPositive.getY(), targetPositive.getZ()));
        }
        if(targetPositiveType != null) {
            tag.putString("target_positive_type", targetPositiveType);
        }
        if(targetNegative != null) {
            tag.putIntArray("target_negative", List.of(targetNegative.getX(), targetNegative.getY(), targetNegative.getZ()));
        }
        if(targetNegativeType != null) {
            tag.putString("target_negative_type", targetNegativeType);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));

        targetPositive = null;
        targetPositiveType = null;
        targetNegative = null;
        targetNegativeType = null;

        if (tag.contains("target_positive")) {
            int[] arr = tag.getIntArray("target_positive");
            if (arr.length == 3) {
                targetPositive = new BlockPos(arr[0], arr[1], arr[2]);
            }
        }
        if(tag.contains("target_positive_type")) {
            targetPositiveType = tag.getString("target_positive_type");
        }
        if (tag.contains("target_negative")) {
            int[] arr = tag.getIntArray("target_negative");
            if (arr.length == 3) {
                targetNegative = new BlockPos(arr[0], arr[1], arr[2]);
            }
        }
        if(tag.contains("target_negative_type")) {
            targetNegativeType = tag.getString("target_negative_type");
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    public Integer getDirection(BlockPos previousBlockPos){
        if(this.targetPositive == previousBlockPos) return 1;
        if(this.targetNegative == previousBlockPos) return -1;
        return 0;
    }

    public BlockPos getTargetPos(Integer direction){
        if(direction==1) return targetPositive;
        if(direction==-1) return targetNegative;
        return null;
    }

}
