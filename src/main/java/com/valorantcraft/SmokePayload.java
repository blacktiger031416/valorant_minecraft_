package com.valorantcraft;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: the player pressed the C (smoke) key. */
public record SmokePayload() implements CustomPayload {
	public static final CustomPayload.Id<SmokePayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "smoke"));
	public static final PacketCodec<PacketByteBuf, SmokePayload> CODEC = PacketCodec.unit(new SmokePayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
