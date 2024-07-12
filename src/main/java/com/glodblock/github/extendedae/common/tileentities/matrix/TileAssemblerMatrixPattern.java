package com.glodblock.github.extendedae.common.tileentities.matrix;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix;
import com.glodblock.github.glodium.util.GlodUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TileAssemblerMatrixPattern extends TileAssemblerMatrixFunction implements InternalInventoryHost, ICraftingProvider {

    private final AppEngInternalInventory patternInventory;
    private final List<IPatternDetails> patterns = new ArrayList<>();

    public TileAssemblerMatrixPattern(BlockPos pos, BlockState blockState) {
        super(GlodUtil.getTileType(TileAssemblerMatrixPattern.class, TileAssemblerMatrixPattern::new, EAESingletons.ASSEMBLER_MATRIX_PATTERN), pos, blockState);
        this.patternInventory = new AppEngInternalInventory(this, 18);
        this.patternInventory.setFilter(new Filter(this::getLevel));
        this.getMainNode().addService(ICraftingProvider.class, this);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.patternInventory.writeToNBT(data, "pattern", registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.patternInventory.readFromNBT(data, "pattern", registries);
    }

    public void updatePatterns() {
        this.patterns.clear();
        for (var stack : this.patternInventory) {
            var details = PatternDetailsHelper.decodePattern(stack, this.getLevel());
            if (details != null) {
                patterns.add(details);
            }
        }
        ICraftingProvider.requestUpdate(this.getMainNode());
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (var pattern : this.patternInventory) {
            drops.add(pattern);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.patternInventory.clear();
    }

    @Override
    public void add(ClusterAssemblerMatrix c) {
        c.addPattern(this);
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
        this.updatePatterns();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.updatePatterns();
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!isFormed() || !this.getMainNode().isActive() || !this.patterns.contains(patternDetails)) {
            return false;
        }
        return this.cluster.pushCraftingJob(patternDetails, inputHolder);
    }

    @Override
    public boolean isBusy() {
        return this.cluster == null || this.cluster.isBusy();
    }

    private record Filter(Supplier<Level> world) implements IAEItemFilter {

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return PatternDetailsHelper.decodePattern(stack, world.get()) instanceof IMolecularAssemblerSupportedPattern;
        }

    }

}
