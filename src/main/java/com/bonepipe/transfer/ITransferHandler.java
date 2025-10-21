package com.bonepipe.transfer;

import com.bonepipe.network.NetworkNode;

/**
 * Result of a transfer operation
 */
public class TransferResult {
    
    private final boolean success;
    private final long amountTransferred;
    private final String errorMessage;
    
    private TransferResult(boolean success, long amountTransferred, String errorMessage) {
        this.success = success;
        this.amountTransferred = amountTransferred;
        this.errorMessage = errorMessage;
    }
    
    public static TransferResult success(long amount) {
        return new TransferResult(true, amount, null);
    }
    
    public static TransferResult failure(String error) {
        return new TransferResult(false, 0, error);
    }
    
    public static TransferResult empty() {
        return new TransferResult(true, 0, null);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public long getAmountTransferred() {
        return amountTransferred;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String toString() {
        return success ? 
            String.format("Success[%d]", amountTransferred) : 
            String.format("Failure[%s]", errorMessage);
    }
}

/**
 * Interface for transfer handlers
 * Each channel type implements this interface
 */
public interface ITransferHandler {
    
    /**
     * Get the channel this handler supports
     */
    TransferChannel getChannel();
    
    /**
     * Perform a transfer operation between two nodes
     * 
     * @param source Source node (INPUT)
     * @param destination Destination node (OUTPUT)
     * @param maxAmount Maximum amount to transfer
     * @return Result of the transfer
     */
    TransferResult transfer(NetworkNode source, NetworkNode destination, long maxAmount);
    
    /**
     * Check if a node can act as a source (has extractable resources)
     */
    boolean canExtract(NetworkNode node);
    
    /**
     * Check if a node can act as a destination (can receive resources)
     */
    boolean canInsert(NetworkNode node);
    
    /**
     * Get the maximum amount that can be extracted from a source
     */
    long getExtractableAmount(NetworkNode source);
    
    /**
     * Get the maximum amount that can be inserted into a destination
     */
    long getInsertableAmount(NetworkNode destination);
}
