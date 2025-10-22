package com.bonepipe.core;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlock;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.gui.AdapterMenu;
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
    
    // ========== ITEMS ==========
    
    public static final RegistryObject<Item> ADAPTER_ITEM = ITEMS.register("adapter",
        () -> new BlockItem(ADAPTER_BLOCK.get(), 
            new Item.Properties().tab(ModCreativeTab.BONEPIPE_TAB)));
    
    // ========== BLOCK ENTITIES ==========
    
    public static final RegistryObject<BlockEntityType<AdapterBlockEntity>> ADAPTER_BE = 
        BLOCK_ENTITIES.register("adapter",
            () -> BlockEntityType.Builder.of(AdapterBlockEntity::new, ADAPTER_BLOCK.get())
                .build(null));
    
    // ========== MENUS ==========
    
    public static final RegistryObject<MenuType<AdapterMenu>> ADAPTER_MENU = 
        MENUS.register("adapter",
            () -> IForgeMenuType.create(AdapterMenu::new));
    
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
