package com.valorantcraft;

import com.valorantcraft.ability.AbilityManager;
import com.valorantcraft.command.ValorantCommand;
import com.valorantcraft.match.MatchManager;
import com.valorantcraft.network.ModNetworking;
import com.valorantcraft.registry.ModItemGroups;
import com.valorantcraft.registry.ModItems;
import com.valorantcraft.weapon.WeaponServerLogic;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValorantCraft implements ModInitializer {
	public static final String MOD_ID = "valorantcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[ValorantCraft] Initializing tactical shooter systems...");

		ModItems.register();
		ModItemGroups.register();
		ModNetworking.register();
		ValorantCommand.register();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			WeaponServerLogic.tick(server);
			AbilityManager.get().tick(server);
			MatchManager.get().tick(server);
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof ServerPlayerEntity victim) {
				ServerPlayerEntity killer = null;
				if (damageSource.getAttacker() instanceof ServerPlayerEntity attacker) {
					killer = attacker;
				}
				MatchManager.get().onPlayerKilled(victim, killer);
			}
		});
	}
}
