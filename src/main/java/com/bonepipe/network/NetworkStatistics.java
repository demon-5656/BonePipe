package com.bonepipe.network;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks network-wide statistics and performance metrics
 * Thread-safe for multi-threaded environments
 */
public class NetworkStatistics {
    
    // Statistics per frequency
    private final Map<String, FrequencyStats> frequencyStats = new ConcurrentHashMap<>();
    
    // Global statistics
    private long globalTotalTransferred = 0;
    private long globalPeakRate = 0;
    private long globalStartTime = System.currentTimeMillis();
    
    /**
     * Record a transfer operation
     */
    public void recordTransfer(String frequency, TransferType type, long amount) {
        FrequencyStats stats = frequencyStats.computeIfAbsent(frequency, k -> new FrequencyStats());
        stats.recordTransfer(type, amount);
        globalTotalTransferred += amount;
        
        // Update peak rate if current rate exceeds it
        long currentRate = stats.getCurrentRate();
        if (currentRate > globalPeakRate) {
            globalPeakRate = currentRate;
        }
    }
    
    /**
     * Record an error
     */
    public void recordError(String frequency, String errorType) {
        FrequencyStats stats = frequencyStats.computeIfAbsent(frequency, k -> new FrequencyStats());
        stats.recordError(errorType);
    }
    
    /**
     * Get statistics for a frequency
     */
    public FrequencyStats getStats(String frequency) {
        return frequencyStats.get(frequency);
    }
    
    /**
     * Get all frequencies with statistics
     */
    public Set<String> getActiveFrequencies() {
        return frequencyStats.keySet();
    }
    
    /**
     * Get global total transferred
     */
    public long getGlobalTotalTransferred() {
        return globalTotalTransferred;
    }
    
    /**
     * Get global peak rate
     */
    public long getGlobalPeakRate() {
        return globalPeakRate;
    }
    
    /**
     * Get global uptime in milliseconds
     */
    public long getGlobalUptime() {
        return System.currentTimeMillis() - globalStartTime;
    }
    
    /**
     * Reset all statistics
     */
    public void reset() {
        frequencyStats.clear();
        globalTotalTransferred = 0;
        globalPeakRate = 0;
        globalStartTime = System.currentTimeMillis();
    }
    
    /**
     * Reset statistics for a specific frequency
     */
    public void reset(String frequency) {
        frequencyStats.remove(frequency);
    }
    
    /**
     * Statistics for a single frequency
     */
    public static class FrequencyStats {
        // Transfer history (circular buffer for last 100 data points)
        private final CircularBuffer<TransferRecord> transferHistory = new CircularBuffer<>(100);
        
        // Counters per transfer type
        private final Map<TransferType, Long> totalTransferred = new EnumMap<>(TransferType.class);
        private final Map<TransferType, Long> peakRates = new EnumMap<>(TransferType.class);
        
        // Error tracking
        private final Map<String, Integer> errorCounts = new ConcurrentHashMap<>();
        
        // Timing
        private long startTime = System.currentTimeMillis();
        private long lastTransferTime = 0;
        
        public void recordTransfer(TransferType type, long amount) {
            long currentTime = System.currentTimeMillis();
            
            // Update totals
            totalTransferred.merge(type, amount, Long::sum);
            
            // Add to history
            transferHistory.add(new TransferRecord(currentTime, type, amount));
            
            // Update peak rate
            long currentRate = calculateCurrentRate(type);
            peakRates.merge(type, currentRate, Math::max);
            
            lastTransferTime = currentTime;
        }
        
        public void recordError(String errorType) {
            errorCounts.merge(errorType, 1, Integer::sum);
        }
        
        /**
         * Calculate current transfer rate (per second)
         */
        private long calculateCurrentRate(TransferType type) {
            long now = System.currentTimeMillis();
            long windowMs = 1000; // 1 second window
            
            return transferHistory.stream()
                .filter(r -> r.type == type && (now - r.timestamp) < windowMs)
                .mapToLong(r -> r.amount)
                .sum();
        }
        
        /**
         * Get current overall rate
         */
        public long getCurrentRate() {
            long now = System.currentTimeMillis();
            long windowMs = 1000;
            
            return transferHistory.stream()
                .filter(r -> (now - r.timestamp) < windowMs)
                .mapToLong(r -> r.amount)
                .sum();
        }
        
        /**
         * Get average rate over entire history
         */
        public long getAverageRate() {
            long uptime = System.currentTimeMillis() - startTime;
            if (uptime == 0) return 0;
            
            long total = totalTransferred.values().stream().mapToLong(Long::longValue).sum();
            return (total * 1000) / uptime; // per second
        }
        
        /**
         * Get total transferred for a type
         */
        public long getTotalTransferred(TransferType type) {
            return totalTransferred.getOrDefault(type, 0L);
        }
        
        /**
         * Get peak rate for a type
         */
        public long getPeakRate(TransferType type) {
            return peakRates.getOrDefault(type, 0L);
        }
        
        /**
         * Get transfer history for graphing
         */
        public List<TransferRecord> getHistory() {
            return transferHistory.toList();
        }
        
        /**
         * Get error counts
         */
        public Map<String, Integer> getErrorCounts() {
            return new HashMap<>(errorCounts);
        }
        
        /**
         * Get uptime in milliseconds
         */
        public long getUptime() {
            return System.currentTimeMillis() - startTime;
        }
        
        /**
         * Check if active (had transfer in last 5 seconds)
         */
        public boolean isActive() {
            return (System.currentTimeMillis() - lastTransferTime) < 5000;
        }
    }
    
    /**
     * Record of a single transfer operation
     */
    public static class TransferRecord {
        public final long timestamp;
        public final TransferType type;
        public final long amount;
        
        public TransferRecord(long timestamp, TransferType type, long amount) {
            this.timestamp = timestamp;
            this.type = type;
            this.amount = amount;
        }
    }
    
    /**
     * Types of transfers
     */
    public enum TransferType {
        ITEMS,
        FLUIDS,
        ENERGY,
        GAS,
        INFUSION,
        PIGMENT,
        SLURRY
    }
    
    /**
     * Circular buffer implementation
     */
    private static class CircularBuffer<T> {
        private final List<T> buffer;
        private final int capacity;
        private int index = 0;
        
        public CircularBuffer(int capacity) {
            this.capacity = capacity;
            this.buffer = new ArrayList<>(capacity);
        }
        
        public void add(T item) {
            if (buffer.size() < capacity) {
                buffer.add(item);
            } else {
                buffer.set(index, item);
            }
            index = (index + 1) % capacity;
        }
        
        public java.util.stream.Stream<T> stream() {
            return buffer.stream();
        }
        
        public List<T> toList() {
            return new ArrayList<>(buffer);
        }
    }
}
