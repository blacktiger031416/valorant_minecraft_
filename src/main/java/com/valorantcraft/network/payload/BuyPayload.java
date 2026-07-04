package com.valorantcraft.network.payload;

import com.valorantcraft.ValorantCraft;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: player bought an item from the shop during buy phase. itemId matches a WeaponType id. */
public record BuyPayload(String itemId) implements CustomPayload {
	public static final CustomPayload.Id<BuyPayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "buy"));
	public static final PacketCodec<PacketByteBuf, BuyPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.STRING, BuyPayload::itemId,
			BuyPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
