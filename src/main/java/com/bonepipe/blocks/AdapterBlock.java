package com.bonepipe.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

/**
 * Main Adapter Block - wireless resource transfer hub
 * Connects to any machine side and operates through frequency networks
 */
public class AdapterBlock extends Block implements EntityBlock {
    
    public AdapterBlock() {
        super(Properties.of(Material.METAL)
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdapterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Client-side: no ticking needed
        if (level.isClientSide()) {
            return null;
        }
        
        // Server-side: main logic ticker
        return (lvl, pos, st, be) -> {
            if (be instanceof AdapterBlockEntity adapter) {
                adapter.tick();
            }
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AdapterBlockEntity adapter) {
                adapter.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
