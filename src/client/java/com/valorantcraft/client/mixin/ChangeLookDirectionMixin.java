package com.valorantcraft.client.mixin;

import com.valorantcraft.client.ClientAgentState;
import com.valorantcraft.client.ClientSmokeAim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * While the local player is guiding the smoke (holding C), mouse movement should steer the smoke
 * instead of turning the camera. Cancelling this at the source (rather than trying to "undo" a
 * camera turn afterward) avoids any visible camera jitter.
 */
@Mixin(Entity.class)
public abstract class ChangeLookDirectionMixin {
	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	private void valorantcraft$redirectToSmokeAim(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (ClientAgentState.guidingSmoke && (Object) this == client.player) {
			ClientSmokeAim.accumulate(cursorDeltaX, cursorDeltaY);
			ci.cancel();
		}
	}
}
