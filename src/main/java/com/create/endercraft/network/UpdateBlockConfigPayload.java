package com.create.endercraft.network;

import com.create.endercraft.EndercraftMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateBlockConfigPayload(BlockPos pos, CompoundTag data) implements CustomPacketPayload {
    public static final Type<UpdateBlockConfigPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EndercraftMod.MODID, "update_block_config")
    );

    public static final StreamCodec<ByteBuf, UpdateBlockConfigPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateBlockConfigPayload::pos,
            ByteBufCodecs.COMPOUND_TAG,
            UpdateBlockConfigPayload::data,
            UpdateBlockConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
