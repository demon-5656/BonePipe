package com.bonepipe.transfer.mekanism;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.transfer.ITransferHandler;
import com.bonepipe.transfer.TransferChannel;
import com.bonepipe.transfer.TransferResult;
import com.bonepipe.util.MachineDetector;
import mekanism.api.Action;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Transfer handler for Mekanism Pigment
 * Used in Painting Machine and dye production
 */
public class PigmentTransferHandler implements ITransferHandler {
    
    @Override
    public TransferChannel getChannel() {
        return TransferChannel.MEK_PIGMENT;
    }
    
    @Override
    public TransferResult transfer(NetworkNode source, NetworkNode destination, long maxAmount) {
        AdapterBlockEntity srcAdapter = source.getAdapter();
        AdapterBlockEntity dstAdapter = destination.getAdapter();
        
        if (srcAdapter == null || dstAdapter == null) {
            return TransferResult.failure("Invalid adapters");
        }
        
        BlockEntity srcMachine = getMachineConnection(srcAdapter);
        BlockEntity dstMachine = getMachineConnection(dstAdapter);
        
        if (srcMachine == null || dstMachine == null) {
            return TransferResult.empty();
        }
        
        IPigmentHandler srcHandler = getPigmentHandler(srcMachine, getSideToMachine(srcAdapter));
        IPigmentHandler dstHandler = getPigmentHandler(dstMachine, getSideToMachine(dstAdapter));
        
        if (srcHandler == null || dstHandler == null) {
            return TransferResult.empty();
        }
        
        long transferred = 0;
        long remaining = maxAmount;
        
        for (int srcTank = 0; srcTank < srcHandler.getTanks() && remaining > 0; srcTank++) {
            PigmentStack extracted = srcHandler.extractChemical(remaining, Action.SIMULATE);
            if (extracted.isEmpty()) {
                continue;
            }
            
            PigmentStack remainder = dstHandler.insertChemical(extracted, Action.SIMULATE);
            long canTransfer = extracted.getAmount() - remainder.getAmount();
            
            if (canTransfer > 0) {
                PigmentStack realExtracted = srcHandler.extractChemical(canTransfer, Action.EXECUTE);
                
                if (!realExtracted.isEmpty()) {
                    PigmentStack realRemainder = dstHandler.insertChemical(realExtracted, Action.EXECUTE);
                    long actualTransferred = realExtracted.getAmount() - realRemainder.getAmount();
                    transferred += actualTransferred;
                    remaining -= actualTransferred;
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
        
        IPigmentHandler handler = getPigmentHandler(machine, getSideToMachine(adapter));
        if (handler == null) return false;
        
        for (int i = 0; i < handler.getTanks(); i++) {
            if (!handler.getChemicalInTank(i).isEmpty()) {
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
        
        IPigmentHandler handler = getPigmentHandler(machine, getSideToMachine(adapter));
        return handler != null && handler.getTanks() > 0;
    }
    
    @Override
    public long getExtractableAmount(NetworkNode source) {
        AdapterBlockEntity adapter = source.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IPigmentHandler handler = getPigmentHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        long total = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            total += handler.getChemicalInTank(i).getAmount();
        }
        return total;
    }
    
    @Override
    public long getInsertableAmount(NetworkNode destination) {
        AdapterBlockEntity adapter = destination.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IPigmentHandler handler = getPigmentHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        long capacity = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            PigmentStack stack = handler.getChemicalInTank(i);
            long tankCapacity = handler.getTankCapacity(i);
            capacity += (tankCapacity - stack.getAmount());
        }
        return capacity;
    }
    
    private BlockEntity getMachineConnection(AdapterBlockEntity adapter) {
        return MachineDetector.findConnectedMachine(adapter.getLevel(), adapter.getBlockPos());
    }
    
    private Direction getSideToMachine(AdapterBlockEntity adapter) {
        return MachineDetector.findMachineDirection(adapter.getLevel(), adapter.getBlockPos());
    }
    
    private IPigmentHandler getPigmentHandler(BlockEntity entity, Direction side) {
        if (entity == null) return null;
        
        try {
            return entity.getCapability(Capabilities.PIGMENT_HANDLER, side).orElse(null);
        } catch (Exception e) {
            BonePipe.LOGGER.error("Error getting pigment handler from {}: {}", 
                entity.getBlockPos(), e.getMessage());
            return null;
        }
    }
}
