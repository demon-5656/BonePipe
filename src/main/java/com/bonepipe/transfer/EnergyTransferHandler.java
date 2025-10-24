package com.bonepipe.transfer;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.util.MachineDetector;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Transfer handler for energy using Forge's IEnergyStorage capability
 */
public class EnergyTransferHandler implements ITransferHandler {
    
    @Override
    public TransferChannel getChannel() {
        return TransferChannel.ENERGY;
    }
    
    @Override
    public TransferResult transfer(NetworkNode source, NetworkNode destination, long maxAmount) {
        AdapterBlockEntity srcAdapter = source.getAdapter();
        AdapterBlockEntity dstAdapter = destination.getAdapter();
        
        if (srcAdapter == null || dstAdapter == null) {
            return TransferResult.failure("Invalid adapters");
        }
        
        // Get machine connections
        BlockEntity srcMachine = getMachineConnection(srcAdapter);
        BlockEntity dstMachine = getMachineConnection(dstAdapter);
        
        if (srcMachine == null || dstMachine == null) {
            return TransferResult.empty();
        }
        
        // Get energy handlers
        Direction srcSide = getSideToMachine(srcAdapter);
        Direction dstSide = getSideToMachine(dstAdapter);
        IEnergyStorage srcHandler = getEnergyHandler(srcMachine, srcSide);
        IEnergyStorage dstHandler = getEnergyHandler(dstMachine, dstSide);
        
        if (srcHandler == null || dstHandler == null) {
            return TransferResult.empty();
        }
        
        // Check if source can extract and destination can receive
        if (!srcHandler.canExtract() || !dstHandler.canReceive()) {
            return TransferResult.empty();
        }
        
        // Calculate transfer amount
        int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        
        // Simulate extract from source
        int extractable = srcHandler.extractEnergy(amount, true);
        if (extractable == 0) {
            return TransferResult.empty();
        }
        
        // Simulate insert to destination
        int insertable = dstHandler.receiveEnergy(extractable, true);
        if (insertable == 0) {
            return TransferResult.empty();
        }
        
        // Real extract
        int extracted = srcHandler.extractEnergy(insertable, false);
        
        // Real insert
        int inserted = dstHandler.receiveEnergy(extracted, false);
        
        // Return result
        return inserted > 0 ? 
            TransferResult.success(inserted) : 
            TransferResult.empty();
    }
    
    @Override
    public boolean canExtract(NetworkNode node) {
        AdapterBlockEntity adapter = node.getAdapter();
        if (adapter == null) return false;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return false;
        
        IEnergyStorage handler = getEnergyHandler(machine, getSideToMachine(adapter));
        if (handler == null) return false;
        
        return handler.canExtract() && handler.getEnergyStored() > 0;
    }
    
    @Override
    public boolean canInsert(NetworkNode node) {
        AdapterBlockEntity adapter = node.getAdapter();
        if (adapter == null) return false;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return false;
        
        IEnergyStorage handler = getEnergyHandler(machine, getSideToMachine(adapter));
        if (handler == null) return false;
        
        return handler.canReceive() && 
               handler.getEnergyStored() < handler.getMaxEnergyStored();
    }
    
    @Override
    public long getExtractableAmount(NetworkNode source) {
        AdapterBlockEntity adapter = source.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IEnergyStorage handler = getEnergyHandler(machine, getSideToMachine(adapter));
        if (handler == null || !handler.canExtract()) return 0;
        
        return handler.getEnergyStored();
    }
    
    @Override
    public long getInsertableAmount(NetworkNode destination) {
        AdapterBlockEntity adapter = destination.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IEnergyStorage handler = getEnergyHandler(machine, getSideToMachine(adapter));
        if (handler == null || !handler.canReceive()) return 0;
        
        return handler.getMaxEnergyStored() - handler.getEnergyStored();
    }
    
    /**
     * Get connected machine's BlockEntity
     */
    private BlockEntity getMachineConnection(AdapterBlockEntity adapter) {
        return MachineDetector.findConnectedMachine(adapter.getLevel(), adapter.getBlockPos());
    }

    /**
     * Get side to access machine
     */
    private Direction getSideToMachine(AdapterBlockEntity adapter) {
        return MachineDetector.findMachineDirection(adapter.getLevel(), adapter.getBlockPos());
    }    /**
     * Get energy handler capability from a BlockEntity
     */
    private IEnergyStorage getEnergyHandler(BlockEntity be, Direction side) {
        return be.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
    }
}
