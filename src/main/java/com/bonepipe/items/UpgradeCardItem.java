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
        SPEED("Speed", "Increases transfer rate", 1.5, 0, 0),
        FILTER("Filter", "Adds item/fluid filtering", 1.0, 0, 0),
        RANGE("Range", "Increases wireless range", 1.0, 2.0, 0),
        STACK("Stack Size", "Increases stack size per transfer", 1.0, 0, 4);
        
        final String name;
        final String description;
        final double speedMultiplier;
        final double rangeMultiplier;
        final int stackBonus;
        
        UpgradeType(String name, String description, double speedMultiplier, 
                   double rangeMultiplier, int stackBonus) {
            this.name = name;
            this.description = description;
            this.speedMultiplier = speedMultiplier;
            this.rangeMultiplier = rangeMultiplier;
            this.stackBonus = stackBonus;
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
