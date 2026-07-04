package com.valorantcraft.ability;

public record AgentDefinition(String id, String displayName, Ability q, Ability e, Ability c, Ability ultimate, int ultimateCost) {
	public Ability abilityFor(AbilitySlot slot) {
		return switch (slot) {
			case Q -> q;
			case E -> e;
			case C -> c;
			case ULTIMATE -> ultimate;
		};
	}
}
