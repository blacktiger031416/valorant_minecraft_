package com.valorantcraft.ability;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Tracks per-player ability charges/ultimate points and runs scheduled ability cleanup (e.g. smoke walls). */
public final class AbilityManager {
	private static final AbilityManager INSTANCE = new AbilityManager();

	private final Map<UUID, PlayerAgentState> states = new HashMap<>();
	private final List<PendingWallRestore> pendingRestores = new ArrayList<>();

	private AbilityManager() {
	}

	public static AbilityManager get() {
		return INSTANCE;
	}

	public PlayerAgentState state(ServerPlayerEntity player) {
		return states.computeIfAbsent(player.getUuid(), id -> new PlayerAgentState(ModAgents.defaultAgent()));
	}

	public void refillChargesAtRoundStart(ServerPlayerEntity player) {
		PlayerAgentState state = state(player);
		state.charges.put(AbilitySlot.Q, state.agent.q().chargesPerRound());
		state.charges.put(AbilitySlot.E, state.agent.e().chargesPerRound());
		state.charges.put(AbilitySlot.C, state.agent.c().chargesPerRound());
	}

	public void handleUseAbility(ServerPlayerEntity player, int slotIndex) {
		AbilitySlot slot = AbilitySlot.fromIndex(slotIndex);
		PlayerAgentState state = state(player);
		Ability ability = state.agent.abilityFor(slot);

		if (slot == AbilitySlot.ULTIMATE) {
			if (state.ultimatePoints < state.agent.ultimateCost()) {
				player.sendMessage(Text.literal("Ultimate not ready (" + state.ultimatePoints + "/" + state.agent.ultimateCost() + ")"), true);
				return;
			}
			state.ultimatePoints = 0;
			ability.activate(player);
			return;
		}

		int remaining = state.charges.getOrDefault(slot, 0);
		if (remaining <= 0) {
			player.sendMessage(Text.literal("No charges left for " + ability.displayName()), true);
			return;
		}
		state.charges.put(slot, remaining - 1);
		ability.activate(player);
	}

	public void addUltimatePoints(ServerPlayerEntity player, int amount) {
		PlayerAgentState state = state(player);
		state.ultimatePoints = Math.min(state.agent.ultimateCost(), state.ultimatePoints + amount);
	}

	/** Temporarily overwrites a rectangular wall of blocks, restoring the originals after durationTicks. */
	public void spawnTemporaryWall(ServerWorld world, BlockPos center, int width, int height, int durationTicks, BlockState wallState) {
		List<BlockPos> placed = new ArrayList<>();
		List<BlockState> previous = new ArrayList<>();
		int half = width / 2;
		for (int dx = -half; dx <= half; dx++) {
			for (int dy = 0; dy < height; dy++) {
				BlockPos pos = center.add(dx, dy, 0);
				previous.add(world.getBlockState(pos));
				world.setBlockState(pos, wallState);
				placed.add(pos);
			}
		}
		pendingRestores.add(new PendingWallRestore(world.getTime() + durationTicks, world, placed, previous));
	}

	public void tick(MinecraftServer server) {
		long now = server.getOverworld().getTime();
		Iterator<PendingWallRestore> iterator = pendingRestores.iterator();
		while (iterator.hasNext()) {
			PendingWallRestore restore = iterator.next();
			if (now >= restore.restoreAtTick) {
				for (int i = 0; i < restore.positions.size(); i++) {
					restore.world.setBlockState(restore.positions.get(i), restore.previousStates.get(i));
				}
				iterator.remove();
			}
		}
	}

	private record PendingWallRestore(long restoreAtTick, ServerWorld world, List<BlockPos> positions, List<BlockState> previousStates) {
	}
}
