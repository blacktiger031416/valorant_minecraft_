package com.valorantcraft.client;

/** Which agent the local player picked in AgentScreen. Client-side only for now. */
public final class ClientAgentState {
	public enum Agent {
		NONE, JETT
	}

	public static volatile Agent selected = Agent.NONE;

	private ClientAgentState() {
	}
}
