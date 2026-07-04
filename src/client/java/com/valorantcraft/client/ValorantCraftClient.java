package com.valorantcraft.client;

import com.valorantcraft.client.gui.ShopScreen;
import com.valorantcraft.client.hud.ValorantHud;
import com.valorantcraft.client.keybind.ModKeyBindings;
import com.valorantcraft.network.payload.DefuseProgressPayload;
import com.valorantcraft.network.payload.FireWeaponPayload;
import com.valorantcraft.network.payload.MatchStatePayload;
import com.valorantcraft.network.payload.ReloadWeaponPayload;
import com.valorantcraft.network.payload.UseAbilityPayload;
import com.valorantcraft.weapon.FireMode;
import com.valorantcraft.weapon.WeaponItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public class ValorantCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModKeyBindings.register();

		ClientPlayNetworking.registerGlobalReceiver(MatchStatePayload.ID, (payload, context) -> context.client().execute(() -> {
			ClientMatchState.phase = payload.phase();
			ClientMatchState.phaseTicksLeft = payload.phaseTicksLeft();
			ClientMatchState.spikePlanted = payload.spikePlanted();
			ClientMatchState.spikeTicksLeft = payload.spikeTicksLeft();
			ClientMatchState.credits = payload.credits();
			ClientMatchState.team = payload.team();
			ClientMatchState.attackerScore = payload.attackerScore();
			ClientMatchState.defenderScore = payload.defenderScore();
		}));

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
				isHoldingWeapon(player.getMainHandStack()) ? ActionResult.FAIL : ActionResult.PASS);
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
				isHoldingWeapon(player.getMainHandStack()) ? ActionResult.FAIL : ActionResult.PASS);

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

		HudRenderCallback.EVENT.register(ValorantHud::render);
	}

	private static boolean isHoldingWeapon(ItemStack stack) {
		return stack.getItem() instanceof WeaponItem;
	}

	private void onClientTick(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}

		if (client.currentScreen == null) {
			handleWeaponInput(client);
			handleAbilityInput(client);
			handleDefuseInput(client);

			if (ModKeyBindings.openShop.wasPressed()) {
				client.setScreen(new ShopScreen());
			}
		}
	}

	private void handleWeaponInput(MinecraftClient client) {
		ItemStack stack = client.player.getMainHandStack();
		if (!(stack.getItem() instanceof WeaponItem weaponItem)) {
			return;
		}

		if (weaponItem.getWeaponType().fireMode() == FireMode.FULL_AUTO) {
			if (ModKeyBindings.fire.isPressed()) {
				ClientPlayNetworking.send(new FireWeaponPayload());
			}
		} else {
			while (ModKeyBindings.fire.wasPressed()) {
				ClientPlayNetworking.send(new FireWeaponPayload());
			}
		}

		if (ModKeyBindings.reload.wasPressed()) {
			ClientPlayNetworking.send(new ReloadWeaponPayload());
		}
	}

	private void handleAbilityInput(MinecraftClient client) {
		if (ModKeyBindings.abilityQ.wasPressed()) {
			ClientPlayNetworking.send(new UseAbilityPayload(0));
		}
		if (ModKeyBindings.abilityE.wasPressed()) {
			ClientPlayNetworking.send(new UseAbilityPayload(1));
		}
		if (ModKeyBindings.abilityC.wasPressed()) {
			ClientPlayNetworking.send(new UseAbilityPayload(2));
		}
		if (ModKeyBindings.abilityX.wasPressed()) {
			ClientPlayNetworking.send(new UseAbilityPayload(3));
		}
	}

	private void handleDefuseInput(MinecraftClient client) {
		if (!ClientMatchState.spikePlanted) {
			return;
		}
		if (!client.options.useKey.isPressed()) {
			return;
		}
		ClientPlayNetworking.send(new DefuseProgressPayload());
	}
}
