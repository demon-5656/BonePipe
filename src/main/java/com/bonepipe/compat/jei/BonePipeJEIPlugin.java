package com.bonepipe.compat.jei;

import com.bonepipe.BonePipe;
import com.bonepipe.core.Registration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * JEI Integration Plugin
 * Provides recipe and item information in JEI
 */
@JeiPlugin
public class BonePipeJEIPlugin implements IModPlugin {
    
    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(BonePipe.MODID, "jei_plugin");
    
    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Add info pages for main items
        addAdapterInfo(registration);
    }
    
    /**
     * Add information page for Wireless Adapter
     */
    private void addAdapterInfo(IRecipeRegistration registration) {
        ItemStack adapter = new ItemStack(Registration.ADAPTER_ITEM.get());
        
        List<Component> info = Arrays.asList(
            Component.literal("§l§nWireless Adapter"),
            Component.literal(""),
            Component.literal("§7Place next to any machine to enable"),
            Component.literal("§7wireless resource transfer."),
            Component.literal(""),
            Component.literal("§e§lFeatures:"),
            Component.literal("§7• Items, Fluids, Energy transfer"),
            Component.literal("§7• Mekanism chemicals (Gas, Infusion, etc.)"),
            Component.literal("§7• Frequency-based networking"),
            Component.literal("§7• Per-side configuration"),
            Component.literal(""),
            Component.literal("§e§lHow to use:"),
            Component.literal("§71. Place adapter next to machine"),
            Component.literal("§72. Open GUI and set frequency"),
            Component.literal("§73. Configure sides and transfer modes"),
            Component.literal("§74. Place second adapter on same frequency"),
            Component.literal("§75. Resources will transfer automatically!")
        );
        
        registration.addIngredientInfo(adapter, VanillaTypes.ITEM_STACK, 
            info.toArray(new Component[0]));
    }
}
