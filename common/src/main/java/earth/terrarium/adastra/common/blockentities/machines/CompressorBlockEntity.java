package earth.terrarium.adastra.common.blockentities.machines;

import earth.terrarium.adastra.common.blockentities.base.RecipeMachineBlockEntity;
import earth.terrarium.adastra.common.blockentities.base.sideconfig.Configuration;
import earth.terrarium.adastra.common.blockentities.base.sideconfig.ConfigurationEntry;
import earth.terrarium.adastra.common.blockentities.base.sideconfig.ConfigurationType;
import earth.terrarium.adastra.common.constants.ConstantComponents;
import earth.terrarium.adastra.common.menus.machines.CompressorMenu;
import earth.terrarium.adastra.common.recipes.machines.CompressingRecipe;
import earth.terrarium.adastra.common.registry.ModRecipeTypes;
import earth.terrarium.adastra.common.utils.ItemUtils;
import earth.terrarium.adastra.common.utils.TransferUtils;
import earth.terrarium.botarium.common.energy.impl.InsertOnlyEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class CompressorBlockEntity extends RecipeMachineBlockEntity<CompressingRecipe> {
    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, 3);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CompressorMenu(id, inventory, this);
    }

    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        if (energyContainer != null) return energyContainer;
        return energyContainer = new WrappedBlockEnergyContainer(
            this,
            new InsertOnlyEnergyContainer(10_000) {
                @Override
                public long maxInsert() {
                    return 2_000;
                }
            });
    }

    @Override
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {
        if (canFunction()) tickSideInteractions(pos, d -> true);
    }

    @Override
    public void tickSideInteractions(BlockPos pos, Predicate<Direction> filter) {
        TransferUtils.pullItemsNearby(this, pos, new int[]{1}, getSideConfig().get(0), filter);
        TransferUtils.pushItemsNearby(this, pos, new int[]{2}, getSideConfig().get(1), filter);
        TransferUtils.pullEnergyNearby(this, pos, getEnergyStorage().maxInsert(), getSideConfig().get(2), filter);
    }

    @Override
    public void recipeTick(ServerLevel level, WrappedBlockEnergyContainer energyStorage) {
        if (recipe == null) return;
        if (!canCraft(energyStorage)) {
            clearRecipe();
            return;
        }

        energyStorage.internalExtract(recipe.energy(), false);

        cookTime++;
        if (cookTime < cookTimeTotal) return;
        craft();
    }

    @Override
    public boolean canCraft(WrappedBlockEnergyContainer energyStorage) {
        if (recipe == null) return false;
        if (energyStorage.internalExtract(recipe.energy(), true) < recipe.energy()) return false;
        if (!recipe.ingredient().test(getItem(1))) return false;
        return ItemUtils.canAddItem(getItem(2), recipe.result());
    }

    @Override
    public void craft() {
        if (recipe == null) return;

        getItem(1).shrink(1);
        ItemUtils.addItem(this, recipe.result(), 2);

        cookTime = 0;
        if (getItem(1).isEmpty()) clearRecipe();
    }

    @Override
    public void update() {
        if (level().isClientSide()) return;
        level().getRecipeManager().getAllRecipesFor(ModRecipeTypes.COMPRESSING.get())
            .stream()
            .filter(r -> r.ingredient().test(getItem(1)))
            .findFirst()
            .ifPresent(r -> {
                recipe = r;
                cookTimeTotal = r.cookingTime();
            });
    }

    @Override
    public List<ConfigurationEntry> getDefaultConfig() {
        return List.of(
            new ConfigurationEntry(ConfigurationType.SLOT, Configuration.NONE, ConstantComponents.SIDE_CONFIG_INPUT_SLOTS),
            new ConfigurationEntry(ConfigurationType.SLOT, Configuration.NONE, ConstantComponents.SIDE_CONFIG_OUTPUT_SLOTS),
            new ConfigurationEntry(ConfigurationType.ENERGY, Configuration.NONE, ConstantComponents.SIDE_CONFIG_ENERGY)
        );
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return new int[]{1, 2};
    }
}