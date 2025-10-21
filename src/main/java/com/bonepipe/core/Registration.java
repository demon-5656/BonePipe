package com.bonepipe.core;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlock;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.blocks.ControllerBlock;
import com.bonepipe.blocks.ControllerBlockEntity;
import com.bonepipe.gui.AdapterMenu;
import com.bonepipe.gui.ControllerMenu;
import com.bonepipe.items.UpgradeCardItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Central registration class for all mod content
 */
public class Registration {
    
    // Deferred Registers
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, BonePipe.MOD_ID);
    
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, BonePipe.MOD_ID);
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BonePipe.MOD_ID);
    
    public static final DeferredRegister<MenuType<?>> MENUS = 
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, BonePipe.MOD_ID);
    
    // ========== BLOCKS ==========
    
    public static final RegistryObject<Block> ADAPTER_BLOCK = BLOCKS.register("adapter",
        AdapterBlock::new);
    
    public static final RegistryObject<Block> CONTROLLER_BLOCK = BLOCKS.register("controller",
        ControllerBlock::new);
    
    // ========== ITEMS ==========
    
    public static final RegistryObject<Item> ADAPTER_ITEM = ITEMS.register("adapter",
        () -> new BlockItem(ADAPTER_BLOCK.get(), 
            new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    
    public static final RegistryObject<Item> CONTROLLER_ITEM = ITEMS.register("controller",
        () -> new BlockItem(CONTROLLER_BLOCK.get(), 
            new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    
    // Upgrade cards
    public static final RegistryObject<Item> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
        () -> new UpgradeCardItem(UpgradeCardItem.UpgradeType.SPEED));
    
    public static final RegistryObject<Item> FILTER_UPGRADE = ITEMS.register("filter_upgrade",
        () -> new UpgradeCardItem(UpgradeCardItem.UpgradeType.FILTER));
    
    public static final RegistryObject<Item> RANGE_UPGRADE = ITEMS.register("range_upgrade",
        () -> new UpgradeCardItem(UpgradeCardItem.UpgradeType.RANGE));
    
    public static final RegistryObject<Item> STACK_UPGRADE = ITEMS.register("stack_upgrade",
        () -> new UpgradeCardItem(UpgradeCardItem.UpgradeType.STACK));
    
    public static final RegistryObject<Item> CAPACITY_UPGRADE = ITEMS.register("capacity_upgrade",
        () -> new UpgradeCardItem(UpgradeCardItem.UpgradeType.CAPACITY));
    
    // ========== BLOCK ENTITIES ==========
    
    public static final RegistryObject<BlockEntityType<AdapterBlockEntity>> ADAPTER_BE = 
        BLOCK_ENTITIES.register("adapter",
            () -> BlockEntityType.Builder.of(AdapterBlockEntity::new, ADAPTER_BLOCK.get())
                .build(null));
    
    public static final RegistryObject<BlockEntityType<ControllerBlockEntity>> CONTROLLER_BE = 
        BLOCK_ENTITIES.register("controller",
            () -> BlockEntityType.Builder.of(ControllerBlockEntity::new, CONTROLLER_BLOCK.get())
                .build(null));
    
    // ========== MENUS ==========
    
    public static final RegistryObject<MenuType<AdapterMenu>> ADAPTER_MENU = 
        MENUS.register("adapter",
            () -> IForgeMenuType.create(AdapterMenu::new));
    
    public static final RegistryObject<MenuType<ControllerMenu>> CONTROLLER_MENU = 
        MENUS.register("controller",
            () -> IForgeMenuType.create(ControllerMenu::new));
    
    /**
     * Initialize all registrations
     */
    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        
        BonePipe.LOGGER.info("Registered all deferred registers");
    }
}
