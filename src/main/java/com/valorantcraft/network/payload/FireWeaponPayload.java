package com.valorantcraft.network.payload;

import com.valorantcraft.ValorantCraft;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: player is holding the fire key while a weapon is in their main hand. */
public record FireWeaponPayload() implements CustomPayload {
	public static final CustomPayload.Id<FireWeaponPayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "fire_weapon"));
	public static final PacketCodec<net.minecraft.network.PacketByteBuf, FireWeaponPayload> CODEC = PacketCodec.unit(new FireWeaponPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
