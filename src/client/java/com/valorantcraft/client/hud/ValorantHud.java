package com.valorantcraft.client.hud;

import com.valorantcraft.client.ClientMatchState;
import com.valorantcraft.weapon.WeaponAmmoData;
import com.valorantcraft.weapon.WeaponItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class ValorantHud {
	private ValorantHud() {
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) {
			return;
		}

		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();

		renderAmmo(context, client, screenWidth, screenHeight);
		renderMatchInfo(context, client, screenWidth);
	}

	private static void renderAmmo(DrawContext context, MinecraftClient client, int screenWidth, int screenHeight) {
		ItemStack stack = client.player.getMainHandStack();
		if (!(stack.getItem() instanceof WeaponItem weaponItem)) {
			return;
		}
		int mag = WeaponAmmoData.getMagazine(stack, weaponItem.getWeaponType());
		int reserve = WeaponAmmoData.getReserve(stack, weaponItem.getWeaponType());
		Text ammoText = Text.literal(mag + " / " + reserve);
		int x = screenWidth - client.textRenderer.getWidth(ammoText) - 12;
		int y = screenHeight - 32;
		context.drawTextWithShadow(client.textRenderer, ammoText, x, y, 0xFFFFFF);
	}

	private static void renderMatchInfo(DrawContext context, MinecraftClient client, int screenWidth) {
		if ("WAITING".equals(ClientMatchState.phase)) {
			return;
		}

		String phaseLabel = switch (ClientMatchState.phase) {
			case "BUY" -> "BUY PHASE";
			case "ROUND" -> "ROUND";
			case "END_ROUND" -> "ROUND OVER";
			default -> ClientMatchState.phase;
		};
		int seconds = Math.max(0, ClientMatchState.phaseTicksLeft) / 20;
		Text timerText = Text.literal(phaseLabel + "  " + (seconds / 60) + ":" + String.format("%02d", seconds % 60));
		context.drawCenteredTextWithShadow(client.textRenderer, timerText, screenWidth / 2, 6, 0xFFFFFF);

		Text scoreText = Text.literal("ATK " + ClientMatchState.attackerScore + " - " + ClientMatchState.defenderScore + " DEF");
		context.drawCenteredTextWithShadow(client.textRenderer, scoreText, screenWidth / 2, 18, 0xAAAAAA);

		if (ClientMatchState.spikePlanted) {
			int spikeSeconds = Math.max(0, ClientMatchState.spikeTicksLeft) / 20;
			Text spikeText = Text.literal("SPIKE ARMED: " + spikeSeconds + "s");
			context.drawCenteredTextWithShadow(client.textRenderer, spikeText, screenWidth / 2, 32, 0xFF5555);
		}

		Text creditsText = Text.literal("$" + ClientMatchState.credits);
		context.drawTextWithShadow(client.textRenderer, creditsText, 8, 8, 0x55FF55);

		Text teamText = Text.literal(ClientMatchState.team);
		context.drawTextWithShadow(client.textRenderer, teamText, 8, 20, 0xAAAAAA);
	}
}
