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
        addControllerInfo(registration);
        addUpgradeInfo(registration);
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
            Component.literal("§7• Upgrade system (4 slots)"),
            Component.literal(""),
            Component.literal("§e§lHow to use:"),
            Component.literal("§71. Place adapter next to machine"),
            Component.literal("§72. Open GUI and set frequency"),
            Component.literal("§73. Configure sides and transfer types"),
            Component.literal("§74. Place second adapter on same frequency"),
            Component.literal("§75. Resources will transfer automatically!"),
            Component.literal(""),
            Component.literal("§e§lUpgrades:"),
            Component.literal("§7• Speed: x2.0 per card (stackable!)"),
            Component.literal("§7• Stack: +8 items per card"),
            Component.literal("§7• Filter: Enables filtering"),
            Component.literal("§7• Capacity: +9 filter slots"),
            Component.literal("§7• Range: x2.0 wireless range")
        );
        
        registration.addIngredientInfo(adapter, VanillaTypes.ITEM_STACK, 
            info.toArray(new Component[0]));
    }
    
    /**
     * Add information page for Network Controller
     */
    private void addControllerInfo(IRecipeRegistration registration) {
        ItemStack controller = new ItemStack(Registration.CONTROLLER_ITEM.get());
        
        List<Component> info = Arrays.asList(
            Component.literal("§l§nNetwork Controller"),
            Component.literal(""),
            Component.literal("§7Central management block for wireless"),
            Component.literal("§7networks (similar to Flux Networks)."),
            Component.literal(""),
            Component.literal("§e§lFeatures:"),
            Component.literal("§7• Shows all adapters on frequency"),
            Component.literal("§7• Real-time network statistics"),
            Component.literal("§7• Per-node transfer metrics"),
            Component.literal("§7• Current/Peak/Average rates"),
            Component.literal("§7• Uptime tracking"),
            Component.literal("§7• Status indicators"),
            Component.literal(""),
            Component.literal("§e§lHow to use:"),
            Component.literal("§71. Place controller anywhere"),
            Component.literal("§72. Open GUI"),
            Component.literal("§73. Set frequency (same as adapters)"),
            Component.literal("§74. View all network information"),
            Component.literal(""),
            Component.literal("§e§lIndicator:"),
            Component.literal("§7• Glows when network is active"),
            Component.literal("§7• Light level 15 when working")
        );
        
        registration.addIngredientInfo(controller, VanillaTypes.ITEM_STACK, 
            info.toArray(new Component[0]));
    }
    
    /**
     * Add information page for Upgrade Cards
     */
    private void addUpgradeInfo(IRecipeRegistration registration) {
        // Speed Upgrade
        ItemStack speedUpgrade = new ItemStack(Registration.SPEED_UPGRADE.get());
        registration.addIngredientInfo(speedUpgrade, VanillaTypes.ITEM_STACK,
            Component.literal("§l§nSpeed Upgrade"),
            Component.literal(""),
            Component.literal("§7Increases transfer rate by §ex2.0§7."),
            Component.literal("§7Stacks multiplicatively!"),
            Component.literal(""),
            Component.literal("§e§lExamples:"),
            Component.literal("§71 card: x2 speed"),
            Component.literal("§72 cards: x4 speed"),
            Component.literal("§73 cards: x8 speed"),
            Component.literal("§74 cards: §c§lx16 speed!§7 (max)"),
            Component.literal(""),
            Component.literal("§7Use 4 cards for insane transfer rates!")
        );
        
        // Stack Upgrade
        ItemStack stackUpgrade = new ItemStack(Registration.STACK_UPGRADE.get());
        registration.addIngredientInfo(stackUpgrade, VanillaTypes.ITEM_STACK,
            Component.literal("§l§nStack Size Upgrade"),
            Component.literal(""),
            Component.literal("§7Increases stack size by §e+8§7."),
            Component.literal("§7Stacks additively."),
            Component.literal(""),
            Component.literal("§e§lExamples:"),
            Component.literal("§71 card: +8 items (72 total)"),
            Component.literal("§72 cards: +16 items (80 total)"),
            Component.literal("§73 cards: +24 items (88 total)"),
            Component.literal("§74 cards: §c+32 items (96 total!)"),
            Component.literal(""),
            Component.literal("§7Great for bulk transfers!")
        );
        
        // Filter Upgrade
        ItemStack filterUpgrade = new ItemStack(Registration.FILTER_UPGRADE.get());
        registration.addIngredientInfo(filterUpgrade, VanillaTypes.ITEM_STACK,
            Component.literal("§l§nFilter Upgrade"),
            Component.literal(""),
            Component.literal("§7Enables item/fluid filtering."),
            Component.literal("§7Provides §e1 filter slot§7."),
            Component.literal(""),
            Component.literal("§7Use with §eCapacity Upgrades§7 to"),
            Component.literal("§7increase filter slots (up to 37!).")
        );
        
        // Capacity Upgrade
        ItemStack capacityUpgrade = new ItemStack(Registration.CAPACITY_UPGRADE.get());
        registration.addIngredientInfo(capacityUpgrade, VanillaTypes.ITEM_STACK,
            Component.literal("§l§nFilter Capacity Upgrade"),
            Component.literal(""),
            Component.literal("§7Adds §e+9 filter slots§7."),
            Component.literal("§7Requires Filter Upgrade to work."),
            Component.literal(""),
            Component.literal("§e§lExamples:"),
            Component.literal("§7Filter + 1 Capacity: 10 slots"),
            Component.literal("§7Filter + 2 Capacity: 19 slots"),
            Component.literal("§7Filter + 3 Capacity: 28 slots"),
            Component.literal("§7Filter + 4 Capacity: §c37 slots!"),
            Component.literal(""),
            Component.literal("§7Similar to AE2 Capacity Cards!")
        );
        
        // Range Upgrade
        ItemStack rangeUpgrade = new ItemStack(Registration.RANGE_UPGRADE.get());
        registration.addIngredientInfo(rangeUpgrade, VanillaTypes.ITEM_STACK,
            Component.literal("§l§nRange Upgrade"),
            Component.literal(""),
            Component.literal("§7Increases wireless range by §ex2.0§7."),
            Component.literal(""),
            Component.literal("§7Note: Default range is unlimited,"),
            Component.literal("§7but this can be configured in server"),
            Component.literal("§7config file.")
        );
    }
}
