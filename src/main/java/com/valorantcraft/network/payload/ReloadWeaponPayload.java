package com.valorantcraft.network.payload;

import com.valorantcraft.ValorantCraft;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: player pressed reload while holding a weapon. */
public record ReloadWeaponPayload() implements CustomPayload {
	public static final CustomPayload.Id<ReloadWeaponPayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "reload_weapon"));
	public static final PacketCodec<PacketByteBuf, ReloadWeaponPayload> CODEC = PacketCodec.unit(new ReloadWeaponPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
