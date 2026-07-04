package com.valorantcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal test build: one ability key that gives the player Jump Boost + sends a chat message.
 * This exists purely to prove the client-input -> network -> server-effect pipeline works before
 * anything more complex (weapons, spike, matches) is added back on top of it.
 */
public class ValorantCraft implements ModInitializer {
	public static final String MOD_ID = "valorantcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[ValorantCraft] Initializing (minimal test build)...");

		PayloadTypeRegistry.playC2S().register(AbilityPayload.ID, AbilityPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SmokePayload.ID, SmokePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(AbilityPayload.ID, (payload, context) ->
				context.server().execute(() -> {
					ServerPlayerEntity player = context.player();
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 100, 5));
					player.sendMessage(Text.literal("[ValorantCraft] Ability used!"), true);
					LOGGER.info("[ValorantCraft] {} used the test ability", player.getName().getString());
				}));

		ServerPlayNetworking.registerGlobalReceiver(SmokePayload.ID, (payload, context) ->
				context.server().execute(() -> SmokeAbility.setHolding(context.player(), payload.held())));

		ServerTickEvents.END_SERVER_TICK.register(SmokeAbility::tick);
	}
}
