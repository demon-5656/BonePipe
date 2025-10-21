package com.bonepipe.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * Utility class for detecting machine connections
 */
public class MachineDetector {
    
    /**
     * Find the first adjacent BlockEntity that has any supported capability
     * Returns null if no machine found
     */
    public static BlockEntity findConnectedMachine(Level level, BlockPos adapterPos) {
        for (Direction dir : Direction.values()) {
            BlockPos checkPos = adapterPos.relative(dir);
            BlockEntity be = level.getBlockEntity(checkPos);
            
            if (be != null && isMachine(be)) {
                return be;
            }
        }
        return null;
    }
    
    /**
     * Find the direction to the connected machine
     * Returns null if no machine found
     */
    public static Direction findMachineDirection(Level level, BlockPos adapterPos) {
        for (Direction dir : Direction.values()) {
            BlockPos checkPos = adapterPos.relative(dir);
            BlockEntity be = level.getBlockEntity(checkPos);
            
            if (be != null && isMachine(be)) {
                return dir;
            }
        }
        return null;
    }
    
    /**
     * Check if a BlockEntity is a "machine" (has any supported capability)
     */
    public static boolean isMachine(BlockEntity be) {
        // Check for common capabilities
        if (hasItemHandler(be)) return true;
        if (hasFluidHandler(be)) return true;
        if (hasEnergy(be)) return true;
        
        // Check Mekanism chemical capabilities (optional dependency)
        if (hasMekanismChemical(be)) return true;
        
        return false;
    }
    
    /**
     * Check if BlockEntity has Mekanism chemical capabilities
     * Uses reflection to avoid hard dependency on Mekanism
     */
    public static boolean hasMekanismChemical(BlockEntity be) {
        try {
            // Try to load Mekanism's chemical capability classes
            Class<?> gasCapabilityClass = Class.forName("mekanism.api.chemical.gas.GasHandler");
            Class<?> infusionCapabilityClass = Class.forName("mekanism.api.chemical.infuse.InfusionHandler");
            Class<?> pigmentCapabilityClass = Class.forName("mekanism.api.chemical.pigment.PigmentHandler");
            Class<?> slurryCapabilityClass = Class.forName("mekanism.api.chemical.slurry.SlurryHandler");
            
            // Check for capabilities using Mekanism's API
            // This is a simplified check - actual implementation would need proper capability references
            return false; // Placeholder - would require Mekanism dependency to implement fully
            
        } catch (ClassNotFoundException e) {
            // Mekanism not installed - that's okay
            return false;
        }
    }
    
    /**
     * Check if BlockEntity has item handler capability on any side
     */
    public static boolean hasItemHandler(BlockEntity be) {
        for (Direction dir : Direction.values()) {
            if (be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).isPresent()) {
                return true;
            }
        }
        // Also check without side
        return be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent();
    }
    
    /**
     * Check if BlockEntity has fluid handler capability on any side
     */
    public static boolean hasFluidHandler(BlockEntity be) {
        for (Direction dir : Direction.values()) {
            if (be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir).isPresent()) {
                return true;
            }
        }
        return be.getCapability(ForgeCapabilities.FLUID_HANDLER, null).isPresent();
    }
    
    /**
     * Check if BlockEntity has energy capability on any side
     */
    public static boolean hasEnergy(BlockEntity be) {
        for (Direction dir : Direction.values()) {
            if (be.getCapability(ForgeCapabilities.ENERGY, dir).isPresent()) {
                return true;
            }
        }
        return be.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
    }
    
    /**
     * Get the opposite direction (from machine perspective to adapter)
     */
    public static Direction getMachineSide(Direction adapterToMachine) {
        return adapterToMachine.getOpposite();
    }
}
