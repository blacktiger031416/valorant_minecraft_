package com.valorantcraft;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S: whether the player is currently holding the C (smoke) key down, plus the aim
 * yaw/pitch used to steer the smoke while held (this is a separate value from the player's own
 * look direction, since the camera is frozen while guiding).
 */
public record SmokePayload(boolean held, float yaw, float pitch) implements CustomPayload {
	public static final CustomPayload.Id<SmokePayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "smoke"));
	public static final PacketCodec<PacketByteBuf, SmokePayload> CODEC = PacketCodec.tuple(
			PacketCodecs.BOOL, SmokePayload::held,
			PacketCodecs.FLOAT, SmokePayload::yaw,
			PacketCodecs.FLOAT, SmokePayload::pitch,
			SmokePayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
