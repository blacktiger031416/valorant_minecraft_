package com.valorantcraft.client.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ModKeyBindings {
	public static final String CATEGORY = "key.category.valorantcraft";

	public static KeyBinding fire;
	public static KeyBinding reload;
	public static KeyBinding abilityQ;
	public static KeyBinding abilityE;
	public static KeyBinding abilityC;
	public static KeyBinding abilityX;
	public static KeyBinding openShop;

	private ModKeyBindings() {
	}

	public static void register() {
		fire = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.valorantcraft.fire", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, CATEGORY));
		reload = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.valorantcraft.reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));
		abilityQ = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.valorantcraft.ability_q", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Q, CATEGORY));
		abilityE = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.valorantcraft.ability_e", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY));
		abilityC = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.valorantcraft.ability_c", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));
		abilityX = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.valorantcraft.ability_ultimate", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY));
		openShop = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.valorantcraft.shop", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));
	}
}
