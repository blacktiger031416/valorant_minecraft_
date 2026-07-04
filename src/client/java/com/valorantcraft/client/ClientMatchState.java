package com.valorantcraft.client;

/** Mutable client-side cache of the last MatchStatePayload received from the server, used by the HUD. */
public final class ClientMatchState {
	public static String phase = "WAITING";
	public static int phaseTicksLeft = 0;
	public static boolean spikePlanted = false;
	public static int spikeTicksLeft = 0;
	public static int credits = 0;
	public static String team = "SPECTATOR";
	public static int attackerScore = 0;
	public static int defenderScore = 0;

	private ClientMatchState() {
	}
}
