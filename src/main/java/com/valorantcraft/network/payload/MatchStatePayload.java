package com.valorantcraft.network.payload;

import com.valorantcraft.ValorantCraft;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** S2C: broadcast periodically so every client's HUD can show phase/timer/spike/score/credits. */
public record MatchStatePayload(
		String phase,
		int phaseTicksLeft,
		boolean spikePlanted,
		int spikeTicksLeft,
		int credits,
		String team,
		int attackerScore,
		int defenderScore
) implements CustomPayload {
	public static final CustomPayload.Id<MatchStatePayload> ID = new CustomPayload.Id<>(Identifier.of(ValorantCraft.MOD_ID, "match_state"));

	public static final PacketCodec<PacketByteBuf, MatchStatePayload> CODEC = PacketCodec.of(
			(value, buf) -> {
				buf.writeString(value.phase());
				buf.writeVarInt(value.phaseTicksLeft());
				buf.writeBoolean(value.spikePlanted());
				buf.writeVarInt(value.spikeTicksLeft());
				buf.writeVarInt(value.credits());
				buf.writeString(value.team());
				buf.writeVarInt(value.attackerScore());
				buf.writeVarInt(value.defenderScore());
			},
			(buf) -> new MatchStatePayload(
					buf.readString(),
					buf.readVarInt(),
					buf.readBoolean(),
					buf.readVarInt(),
					buf.readVarInt(),
					buf.readString(),
					buf.readVarInt(),
					buf.readVarInt()
			)
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
