package com.valorantcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValorantCraft implements ModInitializer {
	public static final String MOD_ID = "valorantcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[ValorantCraft] Initializing...");

		PayloadTypeRegistry.playC2S().register(SmokePayload.ID, SmokePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SmokePayload.ID, (payload, context) ->
				context.server().execute(() -> SmokeAbility.updateHoldState(context.player(), payload.held(), payload.yaw(), payload.pitch())));

		ServerTickEvents.END_SERVER_TICK.register(SmokeAbility::tick);
	}
}
