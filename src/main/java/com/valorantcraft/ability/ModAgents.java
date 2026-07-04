package com.valorantcraft.ability;

import com.valorantcraft.ability.impl.FlashAbility;
import com.valorantcraft.ability.impl.OverdriveUltimate;
import com.valorantcraft.ability.impl.ReconAbility;
import com.valorantcraft.ability.impl.SmokeAbility;

import java.util.LinkedHashMap;
import java.util.Map;

/** Registry of playable agents. Currently ships one original agent kit; more can be added the same way. */
public final class ModAgents {
	private static final Map<String, AgentDefinition> BY_ID = new LinkedHashMap<>();

	public static final AgentDefinition RANGER = register(new AgentDefinition(
			"ranger", "Ranger",
			new FlashAbility("flash", "Flashbang", 1, 200, 20.0, 6.0, 100),
			new SmokeAbility("smoke", "Smoke Screen", 1, 200, 20.0, 300),
			new ReconAbility("recon", "Recon Dart", 2, 200, 25.0, 10.0, 100),
			new OverdriveUltimate("overdrive", "Overdrive", 200),
			6
	));

	private ModAgents() {
	}

	private static AgentDefinition register(AgentDefinition definition) {
		BY_ID.put(definition.id(), definition);
		return definition;
	}

	public static AgentDefinition byId(String id) {
		return BY_ID.get(id);
	}

	public static AgentDefinition defaultAgent() {
		return RANGER;
	}

	public static Map<String, AgentDefinition> all() {
		return BY_ID;
	}
}
