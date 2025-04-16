package net.ugi.sf_hypertube.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;

import java.util.WeakHashMap;

public class CustomItem extends Item {
    // Runtime-only variable storage — lost on game exit
    private final WeakHashMap<ItemStack, Integer> usageMap = new WeakHashMap<>();

    public CustomItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();

        // Just an example: increase a counter every time it's used
        int count = usageMap.getOrDefault(stack, 0) + 1;
        usageMap.put(stack, count);

        if (!level.isClientSide) {
            System.out.println("Item used " + count + " times.");
        }

        return InteractionResult.SUCCESS;
    }
}