package com.valorantcraft.client;

/** Which agent the local player picked in AgentScreen. Client-side only for now. */
public final class ClientAgentState {
	public enum Agent {
		NONE, JETT
	}

	public static volatile Agent selected = Agent.NONE;

	/** True while holding C to guide the smoke - camera turning is frozen and redirected to ClientSmokeAim. */
	public static volatile boolean guidingSmoke = false;

	private ClientAgentState() {
	}
}
