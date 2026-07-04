package com.valorantcraft.weapon;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/** Reads/writes per-stack ammo counters stored in the item's custom NBT data component. */
public final class WeaponAmmoData {
	private static final String MAGAZINE_KEY = "ValorantMagazine";
	private static final String RESERVE_KEY = "ValorantReserve";

	private WeaponAmmoData() {
	}

	public static int getMagazine(ItemStack stack, WeaponType type) {
		NbtCompound nbt = readNbt(stack);
		return nbt.contains(MAGAZINE_KEY) ? nbt.getInt(MAGAZINE_KEY) : type.magazineSize();
	}

	public static int getReserve(ItemStack stack, WeaponType type) {
		NbtCompound nbt = readNbt(stack);
		return nbt.contains(RESERVE_KEY) ? nbt.getInt(RESERVE_KEY) : type.reserveAmmoMax();
	}

	public static void set(ItemStack stack, int magazine, int reserve) {
		NbtCompound nbt = readNbt(stack);
		nbt.putInt(MAGAZINE_KEY, magazine);
		nbt.putInt(RESERVE_KEY, reserve);
		writeNbt(stack, nbt);
	}

	public static void initializeIfAbsent(ItemStack stack, WeaponType type) {
		NbtCompound nbt = readNbt(stack);
		if (!nbt.contains(MAGAZINE_KEY)) {
			set(stack, type.magazineSize(), type.reserveAmmoMax());
		}
	}

	private static NbtCompound readNbt(ItemStack stack) {
		NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
		return component.copyNbt();
	}

	private static void writeNbt(ItemStack stack, NbtCompound nbt) {
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
	}
}
