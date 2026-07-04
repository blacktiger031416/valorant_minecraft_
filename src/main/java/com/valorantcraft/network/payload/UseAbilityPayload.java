package com.valorantcraft.network.payload;

import com.valorantcraft.ValorantCraft;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: player pressed an ability key. slot: 0=Q, 1=E, 2=C, 3=X (ultimate). */
public record UseAbilityPayload(int slot) implements CustomPayload {
	public static final CustomPayload.Id<UseAbilityPayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "use_ability"));
	public static final PacketCodec<PacketByteBuf, UseAbilityPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER, UseAbilityPayload::slot,
			UseAbilityPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
