package com.bonepipe.core;

import com.bonepipe.BonePipe;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Custom Creative Mode Tab for BonePipe
 */
public class ModCreativeTab {
    
    public static final CreativeModeTab BONEPIPE_TAB = new CreativeModeTab(BonePipe.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.ADAPTER_ITEM.get());
        }
    };
}
