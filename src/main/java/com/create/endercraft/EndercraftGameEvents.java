package com.create.endercraft;

import com.create.endercraft.block.IntelligentTransmitterBlock;
import com.create.endercraft.blockentity.IntelligentTransmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;

@EventBusSubscriber(modid = EndercraftMod.MODID)
public final class EndercraftGameEvents {
    private EndercraftGameEvents() {
    }

    @SubscribeEvent
    public static void openTransmitterFromBlazeBurner(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        if (!IntelligentTransmitterBlock.isBlazeBurner(level, clickedPos)) {
            return;
        }

        Optional<BlockPos> transmitterPos = BlockPos.betweenClosedStream(clickedPos.offset(-1, -1, -1), clickedPos.offset(1, 1, 1))
                .filter(pos -> level.getBlockEntity(pos) instanceof IntelligentTransmitterBlockEntity)
                .findFirst();
        if (transmitterPos.isEmpty()) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (!level.isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            IntelligentTransmitterBlock.openConfigMenu(serverPlayer, transmitterPos.get());
        }
    }
}
