package com.glodblock.github.extendedae.common.blocks.matrix;

import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixCrafter;
import net.minecraft.world.item.Item;

public class BlockAssemblerMatrixCrafter extends BlockAssemblerMatrixBase<TileAssemblerMatrixCrafter> {

    @Override
    public Item getPresentItem() {
        return EAESingletons.ASSEMBLER_MATRIX_CRAFTER.asItem();
    }

}