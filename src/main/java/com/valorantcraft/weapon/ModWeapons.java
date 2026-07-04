package com.valorantcraft.weapon;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of weapon stat presets. Placeholder vanilla sounds are used until
 * custom gunshot audio is added under assets/valorantcraft/sounds.
 */
public final class ModWeapons {
	private static final Map<String, WeaponType> BY_ID = new LinkedHashMap<>();

	public static final WeaponType CLASSIC = register(new WeaponType(
			"classic", 26, 4.0f, 0.75f, FireMode.SEMI_AUTO, 1,
			6, 12, 24, 35, 50.0, 1.5, 2.0,
			new float[]{-1.0f, 0.3f, -1.2f, -0.5f, -1.4f, 0.8f}, 0,
			SoundEvents.ITEM_CROSSBOW_SHOOT, SoundEvents.BLOCK_LEVER_CLICK));

	public static final WeaponType SHERIFF = register(new WeaponType(
			"sheriff", 55, 4.0f, 0.75f, FireMode.SEMI_AUTO, 1,
			14, 6, 18, 40, 60.0, 1.2, 2.2,
			new float[]{-2.2f, 0.4f, -2.6f, -0.9f}, 800,
			SoundEvents.ITEM_CROSSBOW_SHOOT, SoundEvents.BLOCK_LEVER_CLICK));

	public static final WeaponType SPECTRE = register(new WeaponType(
			"spectre", 22, 4.0f, 0.75f, FireMode.FULL_AUTO, 1,
			2, 30, 90, 55, 40.0, 2.0, 2.5,
			new float[]{-0.6f, 0.1f, -0.9f, -0.3f, -1.2f, 0.4f, -1.4f, -0.6f, -1.6f, 0.8f}, 1600,
			SoundEvents.ITEM_CROSSBOW_SHOOT, SoundEvents.BLOCK_LEVER_CLICK));

	public static final WeaponType VANDAL = register(new WeaponType(
			"vandal", 40, 4.0f, 0.75f, FireMode.FULL_AUTO, 1,
			2, 25, 75, 50, 100.0, 1.2, 2.0,
			new float[]{-0.8f, 0.1f, -1.1f, -0.4f, -1.4f, 0.5f, -1.6f, -0.8f, -1.8f, 1.0f, -1.9f, -1.1f}, 2900,
			SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundEvents.BLOCK_LEVER_CLICK));

	public static final WeaponType PHANTOM = register(new WeaponType(
			"phantom", 39, 4.0f, 0.75f, FireMode.FULL_AUTO, 1,
			2, 30, 90, 50, 80.0, 1.0, 1.8,
			new float[]{-0.7f, 0.1f, -1.0f, -0.3f, -1.2f, 0.4f, -1.4f, -0.7f, -1.5f, 0.9f, -1.6f, -1.0f}, 2900,
			SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundEvents.BLOCK_LEVER_CLICK));

	public static final WeaponType OPERATOR = register(new WeaponType(
			"operator", 150, 1.0f, 0.85f, FireMode.SEMI_AUTO, 1,
			30, 5, 20, 80, 300.0, 0.2, 3.0,
			new float[]{-4.0f, 0.0f}, 4700,
			SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundEvents.BLOCK_LEVER_CLICK));

	private ModWeapons() {
	}

	private static WeaponType register(WeaponType type) {
		BY_ID.put(type.id(), type);
		return type;
	}

	public static WeaponType byId(String id) {
		return BY_ID.get(id);
	}

	public static Map<String, WeaponType> all() {
		return BY_ID;
	}
}
