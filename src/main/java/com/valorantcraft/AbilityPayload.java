package com.valorantcraft;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: the player pressed the (single, test) ability key. */
public record AbilityPayload() implements CustomPayload {
	public static final CustomPayload.Id<AbilityPayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "ability"));
	public static final PacketCodec<PacketByteBuf, AbilityPayload> CODEC = PacketCodec.unit(new AbilityPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
