package com.bonepipe.items;

import com.bonepipe.BonePipe;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for upgrade cards
 */
public class UpgradeCardItem extends Item {
    
    public enum UpgradeType {
        SPEED("Speed", "Increases transfer rate by 2x", 2.0, 0, 0, 0),
        FILTER("Filter", "Adds item/fluid filtering", 1.0, 0, 0, 1),
        RANGE("Range", "Increases wireless range by 2x", 1.0, 2.0, 0, 0),
        STACK("Stack Size", "Increases stack size by +8", 1.0, 0, 8, 0),
        CAPACITY("Filter Capacity", "Adds +9 filter slots", 1.0, 0, 0, 9);
        
        public final String name;
        public final String description;
        public final double speedMultiplier;
        public final double rangeMultiplier;
        public final int stackBonus;
        public final int filterSlots; // New: number of filter slots this card provides
        
        UpgradeType(String name, String description, double speedMultiplier, 
                   double rangeMultiplier, int stackBonus, int filterSlots) {
            this.name = name;
            this.description = description;
            this.speedMultiplier = speedMultiplier;
            this.rangeMultiplier = rangeMultiplier;
            this.stackBonus = stackBonus;
            this.filterSlots = filterSlots;
        }
    }
    
    private final UpgradeType upgradeType;
    
    public UpgradeCardItem(UpgradeType upgradeType) {
        super(new Properties()
            .tab(net.minecraft.world.item.CreativeModeTab.TAB_REDSTONE)
            .stacksTo(16));
        this.upgradeType = upgradeType;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, 
                               List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(upgradeType.description)
            .withStyle(ChatFormatting.GRAY));
        
        if (upgradeType.speedMultiplier != 1.0) {
            tooltip.add(Component.literal("Speed: x" + upgradeType.speedMultiplier)
                .withStyle(ChatFormatting.GREEN));
        }
        
        if (upgradeType.rangeMultiplier != 0) {
            tooltip.add(Component.literal("Range: x" + upgradeType.rangeMultiplier)
                .withStyle(ChatFormatting.BLUE));
        }
        
        if (upgradeType.stackBonus != 0) {
            tooltip.add(Component.literal("Stack Bonus: +" + upgradeType.stackBonus)
                .withStyle(ChatFormatting.YELLOW));
        }
        
        if (upgradeType.filterSlots > 0) {
            tooltip.add(Component.literal("Filter Slots: +" + upgradeType.filterSlots)
                .withStyle(ChatFormatting.AQUA));
        }
    }
    
    public UpgradeType getUpgradeType() {
        return upgradeType;
    }
    
    /**
     * Check if stack is an upgrade card
     */
    public static boolean isUpgradeCard(ItemStack stack) {
        return stack.getItem() instanceof UpgradeCardItem;
    }
    
    /**
     * Get upgrade type from stack
     */
    public static UpgradeType getUpgradeType(ItemStack stack) {
        if (stack.getItem() instanceof UpgradeCardItem card) {
            return card.getUpgradeType();
        }
        return null;
    }
}
