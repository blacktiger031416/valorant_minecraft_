package com.valorantcraft.client;

import com.valorantcraft.AbilityPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ValorantCraftClient implements ClientModInitializer {
	private static KeyBinding abilityKey;

	@Override
	public void onInitializeClient() {
		abilityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.valorantcraft.ability",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_Q,
				"key.category.valorantcraft"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			while (abilityKey.wasPressed()) {
				ClientPlayNetworking.send(new AbilityPayload());
			}
		});
	}
}
