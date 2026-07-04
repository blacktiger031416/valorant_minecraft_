package com.valorantcraft.client;

import com.valorantcraft.AbilityPayload;
import com.valorantcraft.SmokePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class ValorantCraftClient implements ClientModInitializer {
	private static KeyBinding abilityKey;
	private static KeyBinding dashKey;
	private static KeyBinding smokeKey;
	private static volatile boolean openAgentScreenNextTick = false;

	@Override
	public void onInitializeClient() {
		abilityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.valorantcraft.ability",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_Q,
				"key.category.valorantcraft"
		));
		dashKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.valorantcraft.dash",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_E,
				"key.category.valorantcraft"
		));
		smokeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.valorantcraft.smoke",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_C,
				"key.category.valorantcraft"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			while (abilityKey.wasPressed()) {
				ClientPlayNetworking.send(new AbilityPayload());
			}

			// Deferred by a tick: opening a screen directly from a chat-typed command gets
			// immediately closed again by the chat screen's own "close after send" logic.
			if (openAgentScreenNextTick) {
				openAgentScreenNextTick = false;
				client.setScreen(new AgentScreen());
			}

			handleJettPassive(client);
			handleJettDash(client);

			if (ClientAgentState.selected == ClientAgentState.Agent.JETT && smokeKey.wasPressed()) {
				ClientPlayNetworking.send(new SmokePayload());
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("valorant")
						.then(ClientCommandManager.literal("agent").executes(context -> {
							openAgentScreenNextTick = true;
							return 1;
						}))));
	}

	/** Jett passive: hold jump while airborne to slow her descent. */
	private static void handleJettPassive(net.minecraft.client.MinecraftClient client) {
		if (ClientAgentState.selected != ClientAgentState.Agent.JETT) {
			return;
		}
		var player = client.player;
		if (player.isOnGround() || !client.options.jumpKey.isPressed()) {
			return;
		}
		Vec3d velocity = player.getVelocity();
		double slowFallSpeed = -0.15;
		if (velocity.y < slowFallSpeed) {
			player.setVelocity(velocity.x, slowFallSpeed, velocity.z);
		}
	}

	/**
	 * Jett's Tailwind: a one-time velocity impulse in the direction she's facing (horizontal only),
	 * not a teleport - normal collision and friction still apply, so it decays naturally and can't
	 * punch through walls.
	 */
	private static void handleJettDash(net.minecraft.client.MinecraftClient client) {
		if (ClientAgentState.selected != ClientAgentState.Agent.JETT) {
			return;
		}
		if (!dashKey.wasPressed()) {
			return;
		}
		var player = client.player;

		Vec3d look = player.getRotationVec(1.0f);
		Vec3d forward = new Vec3d(look.x, 0, look.z).normalize();

		double dashSpeed = 1.6;
		Vec3d velocity = player.getVelocity();
		player.setVelocity(forward.x * dashSpeed, velocity.y, forward.z * dashSpeed);
		player.velocityModified = true;
	}
}
