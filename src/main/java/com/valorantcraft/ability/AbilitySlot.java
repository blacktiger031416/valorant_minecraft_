package com.valorantcraft.ability;

public enum AbilitySlot {
	Q, E, C, ULTIMATE;

	public static AbilitySlot fromIndex(int index) {
		return switch (index) {
			case 0 -> Q;
			case 1 -> E;
			case 2 -> C;
			default -> ULTIMATE;
		};
	}
}
