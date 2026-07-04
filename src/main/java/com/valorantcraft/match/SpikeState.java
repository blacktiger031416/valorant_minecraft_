package com.valorantcraft.match;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/** Server-side state for the single active spike (bomb) in the match. */
public class SpikeState {
	public boolean planted = false;
	public BlockPos pos = null;
	public UUID displayEntityUuid = null;
	public int fuseTicksRemaining = 0;
	public int defuseProgressTicks = 0;
	public long lastDefuseInputTick = Long.MIN_VALUE;
	public UUID defusingPlayer = null;

	public void reset() {
		planted = false;
		pos = null;
		displayEntityUuid = null;
		fuseTicksRemaining = 0;
		defuseProgressTicks = 0;
		lastDefuseInputTick = Long.MIN_VALUE;
		defusingPlayer = null;
	}
}
