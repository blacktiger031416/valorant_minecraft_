package com.valorantcraft.network;

import com.valorantcraft.ability.AbilityManager;
import com.valorantcraft.match.MatchManager;
import com.valorantcraft.network.payload.BuyPayload;
import com.valorantcraft.network.payload.DefuseProgressPayload;
import com.valorantcraft.network.payload.FireWeaponPayload;
import com.valorantcraft.network.payload.MatchStatePayload;
import com.valorantcraft.network.payload.ReloadWeaponPayload;
import com.valorantcraft.network.payload.UseAbilityPayload;
import com.valorantcraft.shop.ShopServerLogic;
import com.valorantcraft.weapon.WeaponServerLogic;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ModNetworking {
	private ModNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(FireWeaponPayload.ID, FireWeaponPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ReloadWeaponPayload.ID, ReloadWeaponPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(UseAbilityPayload.ID, UseAbilityPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DefuseProgressPayload.ID, DefuseProgressPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(BuyPayload.ID, BuyPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MatchStatePayload.ID, MatchStatePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(FireWeaponPayload.ID, (payload, context) ->
				context.server().execute(() -> WeaponServerLogic.handleFire(context.player())));

		ServerPlayNetworking.registerGlobalReceiver(ReloadWeaponPayload.ID, (payload, context) ->
				context.server().execute(() -> WeaponServerLogic.handleReload(context.player())));

		ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.ID, (payload, context) ->
				context.server().execute(() -> AbilityManager.get().handleUseAbility(context.player(), payload.slot())));

		ServerPlayNetworking.registerGlobalReceiver(DefuseProgressPayload.ID, (payload, context) ->
				context.server().execute(() -> MatchManager.get().handleDefuseTick(context.player())));

		ServerPlayNetworking.registerGlobalReceiver(BuyPayload.ID, (payload, context) ->
				context.server().execute(() -> ShopServerLogic.handleBuy(context.player(), payload.itemId())));
	}

	public static void sendMatchState(ServerPlayerEntity player, MatchStatePayload payload) {
		ServerPlayNetworking.send(player, payload);
	}
}
