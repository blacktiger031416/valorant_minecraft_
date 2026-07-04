package com.valorantcraft.ability;

import java.util.EnumMap;
import java.util.Map;

public class PlayerAgentState {
	public final AgentDefinition agent;
	public final Map<AbilitySlot, Integer> charges = new EnumMap<>(AbilitySlot.class);
	public int ultimatePoints = 0;

	public PlayerAgentState(AgentDefinition agent) {
		this.agent = agent;
	}
}
