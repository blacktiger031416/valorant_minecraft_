package com.valorantcraft.network.payload;

import com.valorantcraft.ValorantCraft;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: sent every client tick while the player holds "use" aimed at the planted spike. */
public record DefuseProgressPayload() implements CustomPayload {
	public static final CustomPayload.Id<DefuseProgressPayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "defuse_progress"));
	public static final PacketCodec<PacketByteBuf, DefuseProgressPayload> CODEC = PacketCodec.unit(new DefuseProgressPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
