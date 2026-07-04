package com.valorantcraft.registry;

import com.valorantcraft.ValorantCraft;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
	public static final RegistryKey<ItemGroup> MAIN = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(ValorantCraft.MOD_ID, "main"));

	private ModItemGroups() {
	}

	public static void register() {
		Registry.register(Registries.ITEM_GROUP, MAIN, FabricItemGroup.builder()
				.displayName(Text.translatable("itemGroup.valorantcraft.main"))
				.icon(() -> new ItemStack(ModItems.VANDAL))
				.entries((displayContext, entries) -> {
					entries.add(ModItems.CLASSIC);
					entries.add(ModItems.SHERIFF);
					entries.add(ModItems.SPECTRE);
					entries.add(ModItems.VANDAL);
					entries.add(ModItems.PHANTOM);
					entries.add(ModItems.OPERATOR);
					entries.add(ModItems.SPIKE);
				})
				.build());
	}
}
