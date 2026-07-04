package com.valorantcraft.client;

import net.minecraft.util.math.MathHelper;

/**
 * While guiding the smoke, mouse movement is redirected here instead of turning the camera
 * (see ChangeLookDirectionMixin). Mirrors Entity#changeLookDirection's own math so it feels the
 * same as normal mouse look, just applied to this separate yaw/pitch instead of the player's.
 */
public final class ClientSmokeAim {
	public static float yaw;
	public static float pitch;

	private ClientSmokeAim() {
	}

	public static void start(float currentYaw, float currentPitch) {
		yaw = currentYaw;
		pitch = currentPitch;
	}

	public static void accumulate(double cursorDeltaX, double cursorDeltaY) {
		pitch += (float) cursorDeltaY * 0.15F;
		yaw += (float) cursorDeltaX * 0.15F;
		pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
	}
}
