package com.valorantcraft.weapon;

import net.minecraft.sound.SoundEvent;

/**
 * Data-driven definition of a weapon's stats. One instance is shared by every
 * ItemStack of that weapon; per-stack state (current ammo) is stored in NBT.
 */
public record WeaponType(
		String id,
		int bodyDamage,
		float headMultiplier,
		float legMultiplier,
		FireMode fireMode,
		int burstCount,
		int fireIntervalTicks,
		int magazineSize,
		int reserveAmmoMax,
		int reloadTicks,
		double range,
		double baseSpreadDegrees,
		double movingSpreadMultiplier,
		float[] recoilPattern,
		int price,
		SoundEvent fireSound,
		SoundEvent reloadSound
) {
	/** Recoil kick (pitch, yaw in degrees) for the given consecutive shot index (0-based). */
	public float[] recoilFor(int shotIndex) {
		if (recoilPattern.length == 0) {
			return new float[]{0f, 0f};
		}
		int pairIndex = Math.min(shotIndex, (recoilPattern.length / 2) - 1);
		return new float[]{recoilPattern[pairIndex * 2], recoilPattern[pairIndex * 2 + 1]};
	}
}
