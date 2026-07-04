package com.valorantcraft.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Minimal agent-select screen: currently just one placeholder button ("제트" / Jett). */
public class AgentScreen extends Screen {
	public AgentScreen() {
		super(Text.literal("Agent Select"));
	}

	@Override
	protected void init() {
		int width = 150;
		int x = this.width / 2 - width / 2;
		int y = this.height / 2 - 10;

		this.addDrawableChild(ButtonWidget.builder(Text.literal("제트"), button -> {
		}).dimensions(x, y, width, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
