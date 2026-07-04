package com.valorantcraft.weapon;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class WeaponItem extends Item {
	private final WeaponType weaponType;

	public WeaponItem(Settings settings, WeaponType weaponType) {
		super(settings);
		this.weaponType = weaponType;
	}

	public WeaponType getWeaponType() {
		return weaponType;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		WeaponAmmoData.initializeIfAbsent(stack, weaponType);
		int mag = WeaponAmmoData.getMagazine(stack, weaponType);
		int reserve = WeaponAmmoData.getReserve(stack, weaponType);
		tooltip.add(Text.literal(mag + " / " + reserve).formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip.valorantcraft.damage", weaponType.bodyDamage()).formatted(Formatting.RED));
		tooltip.add(Text.translatable("tooltip.valorantcraft.fire_mode", weaponType.fireMode().name()).formatted(Formatting.DARK_GRAY));
	}
}
