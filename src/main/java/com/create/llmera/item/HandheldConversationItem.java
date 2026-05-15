package com.create.llmera.item;

import com.create.llmera.block.IntelligentTransmitterBlock;
import com.create.llmera.blockentity.IntelligentTransmitterBlockEntity;
import com.create.llmera.util.NetworkBinding;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class HandheldConversationItem extends Item {
    public HandheldConversationItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos networkPos = NetworkBinding.readNetworkPos(stack);

        if (networkPos == null) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.llmera.handheld.not_bound"));
            }
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(networkPos) instanceof IntelligentTransmitterBlockEntity) {
                IntelligentTransmitterBlock.openConfigMenu(serverPlayer, networkPos, true);
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("message.llmera.transmitter.error"));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        BlockPos networkPos = NetworkBinding.readNetworkPos(stack);
        if (networkPos == null) {
            tooltip.add(Component.translatable("tooltip.llmera.handheld.bind_hint"));
        } else {
            tooltip.add(Component.translatable("tooltip.llmera.handheld.bound", networkPos.toShortString()));
        }
    }
}
