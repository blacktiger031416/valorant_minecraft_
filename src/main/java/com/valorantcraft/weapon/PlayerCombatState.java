package com.valorantcraft.weapon;

/** Server-side per-player transient combat bookkeeping. Not persisted. */
public class PlayerCombatState {
	public long lastShotTick = Long.MIN_VALUE;
	public int consecutiveShots = 0;
	public long reloadEndTick = -1;
	public String reloadingWeaponId = null;
}
