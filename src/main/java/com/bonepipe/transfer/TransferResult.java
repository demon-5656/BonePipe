package com.bonepipe.transfer;

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
