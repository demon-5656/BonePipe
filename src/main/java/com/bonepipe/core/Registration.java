package com.bonepipe.core;

import com.bonepipe.BonePipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

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
    
    // TODO: Add MENUS register for containers
    // TODO: Add CREATIVE_TABS register
    
    /**
     * Initialize all registrations
     */
    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        
        BonePipe.LOGGER.info("Registered all deferred registers");
    }
}
