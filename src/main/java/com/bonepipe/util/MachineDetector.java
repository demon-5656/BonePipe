package com.bonepipe.util;

import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * Utility class for detecting machine connections
 */
public final class MachineDetector {
    
    private static final boolean MEKANISM_LOADED;
    
    static {
        boolean loaded;
        try {
            Class.forName("mekanism.api.chemical.gas.IGasHandler");
            Class.forName("mekanism.common.capabilities.Capabilities");
            loaded = true;
        } catch (ClassNotFoundException e) {
            loaded = false;
        }
        MEKANISM_LOADED = loaded;
    }
    
    private MachineDetector() {}
    
    /**
     * Find connected machine in the direction the adapter is facing
     * Returns null if no machine found
     */
    public static BlockEntity findConnectedMachine(Level level, BlockPos adapterPos) {
        // Get the adapter's facing direction from blockstate
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(adapterPos);
        if (!(state.getBlock() instanceof com.bonepipe.blocks.AdapterBlock)) {
            return null;
        }
        
        Direction facing = state.getValue(com.bonepipe.blocks.AdapterBlock.FACING);
        BlockPos checkPos = adapterPos.relative(facing);
        BlockEntity be = level.getBlockEntity(checkPos);
        
        if (be == null) {
            return null;
        }
        
        if (isMachine(be, facing.getOpposite())) {
            return be;
        }
        
        return null;
    }
    
    /**
     * Find the direction to the connected machine (same as adapter facing)
     * Returns null if no machine found
     */
    public static Direction findMachineDirection(Level level, BlockPos adapterPos) {
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(adapterPos);
        if (!(state.getBlock() instanceof com.bonepipe.blocks.AdapterBlock)) {
            return null;
        }
        
        Direction facing = state.getValue(com.bonepipe.blocks.AdapterBlock.FACING);
        BlockPos checkPos = adapterPos.relative(facing);
        BlockEntity be = level.getBlockEntity(checkPos);
        
        if (be != null && isMachine(be, facing.getOpposite())) {
            return facing;
        }
        
        return null;
    }
    
    /**
     * Check if a BlockEntity is a "machine" (has any supported capability on the given side)
     * @param be The BlockEntity to check
     * @param side The side of the machine to check (from machine's perspective)
     */
    public static boolean isMachine(BlockEntity be, Direction side) {
        // Check for common capabilities on the specific side
        boolean hasItem = be.getCapability(ForgeCapabilities.ITEM_HANDLER, side).isPresent();
        boolean hasFluid = be.getCapability(ForgeCapabilities.FLUID_HANDLER, side).isPresent();
        boolean hasEnergy = be.getCapability(ForgeCapabilities.ENERGY, side).isPresent();
        boolean hasGas = false;
        
        if (!hasItem) hasItem = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent();
        if (!hasFluid) hasFluid = be.getCapability(ForgeCapabilities.FLUID_HANDLER, null).isPresent();
        if (!hasEnergy) hasEnergy = be.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
        
        if (MEKANISM_LOADED) {
            try {
                hasGas = be.getCapability(Capabilities.GAS_HANDLER, side).isPresent();
                if (!hasGas) {
                    hasGas = be.getCapability(Capabilities.GAS_HANDLER, null).isPresent();
                }
            } catch (NoClassDefFoundError ignored) {
                // Mekanism not actually present at runtime
            }
        }
        
        return hasItem || hasFluid || hasEnergy || hasGas;
    }
    
    /**
     * Get human-readable machine name
     */
    public static String getMachineName(BlockEntity be) {
        if (be == null) return "None";
        
        String className = be.getClass().getSimpleName();
        
        // Clean up common prefixes
        className = className.replace("TileEntity", "");
        className = className.replace("BlockEntity", "");
        
        // Add spaces before capital letters
        return className.replaceAll("([A-Z])", " $1").trim();
    }
    
    /**
     * Get the opposite direction (from machine perspective to adapter)
     */
    public static Direction getMachineSide(Direction adapterToMachine) {
        return adapterToMachine.getOpposite();
    }
}
