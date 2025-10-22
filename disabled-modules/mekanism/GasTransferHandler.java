package com.bonepipe.transfer.mekanism;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.transfer.ITransferHandler;
import com.bonepipe.transfer.TransferChannel;
import com.bonepipe.transfer.TransferResult;
import com.bonepipe.util.MachineDetector;
import mekanism.api.Action;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Transfer handler for Mekanism Gas using Gas Capability
 * Supports all gases: Hydrogen, Oxygen, Chlorine, etc.
 */
public class GasTransferHandler implements ITransferHandler {
    
    @Override
    public TransferChannel getChannel() {
        return TransferChannel.MEK_GAS;
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
        
        // Get gas handlers
        IGasHandler srcHandler = getGasHandler(srcMachine, getSideToMachine(srcAdapter));
        IGasHandler dstHandler = getGasHandler(dstMachine, getSideToMachine(dstAdapter));
        
        if (srcHandler == null || dstHandler == null) {
            return TransferResult.empty();
        }
        
        // Perform transfer
        long transferred = 0;
        long remaining = maxAmount;
        
        // Try to extract and insert gas from each tank
        for (int srcTank = 0; srcTank < srcHandler.getTanks() && remaining > 0; srcTank++) {
            // Simulate extract
            GasStack extracted = srcHandler.extractChemical(remaining, Action.SIMULATE);
            if (extracted.isEmpty()) {
                continue;
            }
            
            // Simulate insert
            GasStack remainder = dstHandler.insertChemical(extracted, Action.SIMULATE);
            long canTransfer = extracted.getAmount() - remainder.getAmount();
            
            if (canTransfer > 0) {
                // Real extract
                GasStack realExtracted = srcHandler.extractChemical(canTransfer, Action.EXECUTE);
                
                if (!realExtracted.isEmpty()) {
                    // Real insert
                    GasStack realRemainder = dstHandler.insertChemical(realExtracted, Action.EXECUTE);
                    
                    long actualTransferred = realExtracted.getAmount() - realRemainder.getAmount();
                    transferred += actualTransferred;
                    remaining -= actualTransferred;
                    
                    if (actualTransferred != canTransfer) {
                        BonePipe.LOGGER.warn("Gas transfer mismatch: expected {} but transferred {}", 
                            canTransfer, actualTransferred);
                    }
                }
            }
        }
        
        return transferred > 0 ? 
            TransferResult.success(transferred) : 
            TransferResult.empty();
    }
    
    @Override
    public boolean canExtract(NetworkNode node) {
        AdapterBlockEntity adapter = node.getAdapter();
        if (adapter == null) return false;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return false;
        
        IGasHandler handler = getGasHandler(machine, getSideToMachine(adapter));
        if (handler == null) return false;
        
        // Check if any tank has extractable gas
        for (int i = 0; i < handler.getTanks(); i++) {
            GasStack gas = handler.getChemicalInTank(i);
            if (!gas.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean canInsert(NetworkNode node) {
        AdapterBlockEntity adapter = node.getAdapter();
        if (adapter == null) return false;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return false;
        
        IGasHandler handler = getGasHandler(machine, getSideToMachine(adapter));
        return handler != null && handler.getTanks() > 0;
    }
    
    @Override
    public long getExtractableAmount(NetworkNode source) {
        AdapterBlockEntity adapter = source.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IGasHandler handler = getGasHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        long total = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            GasStack gas = handler.getChemicalInTank(i);
            total += gas.getAmount();
        }
        
        return total;
    }
    
    @Override
    public long getInsertableAmount(NetworkNode destination) {
        AdapterBlockEntity adapter = destination.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IGasHandler handler = getGasHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        long capacity = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            GasStack gas = handler.getChemicalInTank(i);
            long tankCapacity = handler.getTankCapacity(i);
            capacity += (tankCapacity - gas.getAmount());
        }
        
        return capacity;
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
    }
    
    /**
     * Get gas handler capability from a BlockEntity
     */
    private IGasHandler getGasHandler(BlockEntity entity, Direction side) {
        if (entity == null) return null;
        
        try {
            return entity.getCapability(Capabilities.GAS_HANDLER, side).orElse(null);
        } catch (Exception e) {
            BonePipe.LOGGER.error("Error getting gas handler from {}: {}", 
                entity.getBlockPos(), e.getMessage());
            return null;
        }
    }
}
