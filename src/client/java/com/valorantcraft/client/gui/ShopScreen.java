package com.valorantcraft.client.gui;

import com.valorantcraft.client.ClientMatchState;
import com.valorantcraft.network.payload.BuyPayload;
import com.valorantcraft.weapon.ModWeapons;
import com.valorantcraft.weapon.WeaponType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/** Simple, self-contained buy menu. The server is authoritative and re-validates every purchase. */
public class ShopScreen extends Screen {
	private static final List<WeaponType> ENTRIES = List.of(
			ModWeapons.CLASSIC, ModWeapons.SHERIFF, ModWeapons.SPECTRE,
			ModWeapons.VANDAL, ModWeapons.PHANTOM, ModWeapons.OPERATOR
	);

	public ShopScreen() {
		super(Text.literal("Buy Menu"));
	}

	@Override
	protected void init() {
		int startY = this.height / 2 - (ENTRIES.size() * 24) / 2;
		int width = 220;
		int x = this.width / 2 - width / 2;

		for (int i = 0; i < ENTRIES.size(); i++) {
			WeaponType type = ENTRIES.get(i);
			int y = startY + i * 24;
			this.addDrawableChild(ButtonWidget.builder(
					Text.literal(capitalize(type.id()) + "  -  $" + type.price()),
					button -> {
						ClientPlayNetworking.send(new BuyPayload(type.id()));
						this.close();
					}
			).dimensions(x, y, width, 20).build());
		}
	}

	private static String capitalize(String id) {
		return id.substring(0, 1).toUpperCase() + id.substring(1);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, "Credits: $" + ClientMatchState.credits, this.width / 2, this.height / 2 - (ENTRIES.size() * 24) / 2 - 24, 0x55FF55);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
