package com.valorantcraft.ability;

import net.minecraft.server.network.ServerPlayerEntity;

public interface Ability {
	String id();

	String displayName();

	/** Charges granted per buy phase (non-ultimate abilities only). */
	int chargesPerRound();

	/** Credits cost to purchase during the buy phase. */
	int price();

	void activate(ServerPlayerEntity player);
}
