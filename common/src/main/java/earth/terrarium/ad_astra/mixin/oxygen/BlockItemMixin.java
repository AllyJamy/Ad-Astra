package earth.terrarium.ad_astra.mixin.oxygen;

import earth.terrarium.ad_astra.AdAstra;
import earth.terrarium.ad_astra.registry.ModBlocks;
import earth.terrarium.ad_astra.util.OxygenUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Unique
    private static void adastra_playFireExtinguish(BlockPos pos, Level level) {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
    }

    @Inject(method = "place*", at = @At(value = "TAIL"))
    public void adastra_place(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> info) {
        if (!AdAstra.CONFIG.general.doOxygen) {
            return;
        }
        // Extinguish fire items in dimensions with no oxygen.
        Level level = context.getLevel();
        if (!level.isClientSide) {
            BlockPos pos = context.getClickedPos();
            if (!OxygenUtils.posHasOxygen(level, pos)) {
                BlockState blockstate = level.getBlockState(context.getClickedPos());
                Block block = blockstate.getBlock();

                boolean playSound = false;

                // Wall Torch.
                if (block instanceof WallTorchBlock && !block.equals(Blocks.SOUL_WALL_TORCH)) {
                    level.setBlock(pos, ModBlocks.WALL_EXTINGUISHED_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, blockstate.getValue(WallTorchBlock.FACING)), 3);
                    playSound = true;
                }

                // Torch.
                else if (block instanceof TorchBlock && !block.equals(Blocks.SOUL_TORCH) && !block.equals(Blocks.SOUL_WALL_TORCH)) {
                    if (!block.equals(Blocks.REDSTONE_TORCH) && !block.equals(Blocks.REDSTONE_WALL_TORCH)) {
                        level.setBlock(pos, ModBlocks.EXTINGUISHED_TORCH.get().defaultBlockState(), 3);
                        playSound = true;
                    }
                }

                // Lantern.
                else if (block instanceof LanternBlock && !block.equals(Blocks.SOUL_LANTERN)) {
                    level.setBlock(pos, ModBlocks.EXTINGUISHED_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, blockstate.getValue(LanternBlock.HANGING)), 3);
                    playSound = true;
                }

                // Campfire.
                else if ((block instanceof CampfireBlock && !block.equals(Blocks.SOUL_CAMPFIRE)) && blockstate.getValue(CampfireBlock.LIT)) {
                    level.setBlock(pos, blockstate.setValue(CampfireBlock.LIT, false), 3);
                    playSound = true;
                }

                if (playSound) {
                    adastra_playFireExtinguish(pos, level);
                }
            }
        }
    }
}