package com.valorantcraft.registry;

import com.valorantcraft.ValorantCraft;
import com.valorantcraft.spike.SpikeItem;
import com.valorantcraft.weapon.ModWeapons;
import com.valorantcraft.weapon.WeaponItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class ModItems {
	private static final Map<String, Item> WEAPON_ITEMS_BY_ID = new HashMap<>();

	public static final Item CLASSIC = registerWeapon("classic", new WeaponItem(new Item.Settings().maxCount(1), ModWeapons.CLASSIC));
	public static final Item SHERIFF = registerWeapon("sheriff", new WeaponItem(new Item.Settings().maxCount(1), ModWeapons.SHERIFF));
	public static final Item SPECTRE = registerWeapon("spectre", new WeaponItem(new Item.Settings().maxCount(1), ModWeapons.SPECTRE));
	public static final Item VANDAL = registerWeapon("vandal", new WeaponItem(new Item.Settings().maxCount(1), ModWeapons.VANDAL));
	public static final Item PHANTOM = registerWeapon("phantom", new WeaponItem(new Item.Settings().maxCount(1), ModWeapons.PHANTOM));
	public static final Item OPERATOR = registerWeapon("operator", new WeaponItem(new Item.Settings().maxCount(1), ModWeapons.OPERATOR));
	public static final Item SPIKE = register("spike", new SpikeItem(new Item.Settings().maxCount(1)));

	private ModItems() {
	}

	private static Item register(String path, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(ValorantCraft.MOD_ID, path), item);
	}

	private static Item registerWeapon(String path, Item item) {
		Item registered = register(path, item);
		WEAPON_ITEMS_BY_ID.put(path, registered);
		return registered;
	}

	public static Item weaponItemForId(String weaponId) {
		return WEAPON_ITEMS_BY_ID.get(weaponId);
	}

	public static void register() {
		// Triggers static initializers above.
	}
}
