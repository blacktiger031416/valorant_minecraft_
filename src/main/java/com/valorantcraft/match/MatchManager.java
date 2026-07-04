package com.valorantcraft.match;

import com.valorantcraft.ability.AbilityManager;
import com.valorantcraft.network.ModNetworking;
import com.valorantcraft.network.payload.MatchStatePayload;
import com.valorantcraft.registry.ModItems;
import com.valorantcraft.weapon.WeaponServerLogic;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Owns round/phase state, teams, economy, and the single active spike for the whole server. */
public final class MatchManager {
	public static final int BUY_PHASE_TICKS = 30 * 20;
	public static final int ROUND_TICKS = 100 * 20;
	public static final int END_ROUND_TICKS = 5 * 20;
	public static final int PLANT_FUSE_TICKS = 45 * 20;
	public static final int DEFUSE_TICKS_REQUIRED = 7 * 20;
	public static final int DEFUSE_INPUT_TIMEOUT_TICKS = 4;
	public static final double DEFUSE_RANGE = 4.5;
	public static final int ROUNDS_TO_WIN = 4;
	public static final int START_CREDITS = 800;
	public static final int WIN_CREDITS = 3000;
	public static final int LOSS_CREDITS = 1900;
	public static final int PLANT_BONUS_CREDITS = 300;
	public static final int KILL_CREDITS = 200;

	private static final MatchManager INSTANCE = new MatchManager();

	private Phase phase = Phase.WAITING;
	private int phaseTicks = 0;
	private int roundNumber = 0;
	private int attackerScore = 0;
	private int defenderScore = 0;

	private final Map<UUID, Team> teams = new HashMap<>();
	private final Map<UUID, Integer> credits = new HashMap<>();
	private final Map<UUID, Boolean> alive = new HashMap<>();
	private final List<BombSite> bombSites = new ArrayList<>();
	private final SpikeState spike = new SpikeState();

	private MatchManager() {
	}

	public static MatchManager get() {
		return INSTANCE;
	}

	public Phase getPhase() {
		return phase;
	}

	public boolean isMatchActive() {
		return phase != Phase.WAITING;
	}

	public Team teamOf(UUID uuid) {
		return teams.getOrDefault(uuid, Team.SPECTATOR);
	}

	public int getCredits(UUID uuid) {
		return credits.getOrDefault(uuid, 0);
	}

	public void addCredits(UUID uuid, int amount) {
		credits.merge(uuid, amount, Integer::sum);
	}

	public boolean spendCredits(UUID uuid, int amount) {
		int current = getCredits(uuid);
		if (current < amount) {
			return false;
		}
		credits.put(uuid, current - amount);
		return true;
	}

	public boolean isAlive(UUID uuid) {
		return alive.getOrDefault(uuid, true);
	}

	public void addBombSite(String name, net.minecraft.util.math.Box box) {
		bombSites.removeIf(site -> site.name().equalsIgnoreCase(name));
		bombSites.add(new BombSite(name, box));
	}

	public boolean isInsideAnyBombSite(Vec3d pos) {
		for (BombSite site : bombSites) {
			if (site.contains(pos)) {
				return true;
			}
		}
		return false;
	}

	public SpikeState getSpike() {
		return spike;
	}

	public void start(MinecraftServer server) {
		teams.clear();
		credits.clear();
		alive.clear();
		attackerScore = 0;
		defenderScore = 0;
		roundNumber = 0;
		spike.reset();

		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		if (players.isEmpty()) {
			return;
		}
		int i = 0;
		for (ServerPlayerEntity player : players) {
			teams.put(player.getUuid(), (i % 2 == 0) ? Team.ATTACKER : Team.DEFENDER);
			credits.put(player.getUuid(), START_CREDITS);
			alive.put(player.getUuid(), true);
			i++;
		}
		broadcast(server, Text.literal("[ValorantCraft] Match started! ").formatted(Formatting.GOLD)
				.append(Text.literal(players.size() + " players split into Attackers/Defenders.").formatted(Formatting.GRAY)));
		beginBuyPhase(server);
	}

	public void stop(MinecraftServer server) {
		phase = Phase.WAITING;
		spike.reset();
		broadcast(server, Text.literal("[ValorantCraft] Match stopped.").formatted(Formatting.RED));
	}

	private void beginBuyPhase(MinecraftServer server) {
		roundNumber++;
		phase = Phase.BUY;
		phaseTicks = BUY_PHASE_TICKS;
		spike.reset();
		for (UUID uuid : teams.keySet()) {
			alive.put(uuid, true);
		}
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.setHealth(player.getMaxHealth());
			player.getHungerManager().setFoodLevel(20);
			AbilityManager.get().refillChargesAtRoundStart(player);
		}
		broadcast(server, Text.literal("[ValorantCraft] Round " + roundNumber + " - Buy phase (" + (BUY_PHASE_TICKS / 20) + "s)").formatted(Formatting.YELLOW));
	}

	private void beginRoundPhase(MinecraftServer server) {
		phase = Phase.ROUND;
		phaseTicks = ROUND_TICKS;
		giveSpikeToAttackerIfMissing(server);
		broadcast(server, Text.literal("[ValorantCraft] Fight!").formatted(Formatting.RED));
	}

	private void giveSpikeToAttackerIfMissing(MinecraftServer server) {
		List<ServerPlayerEntity> attackers = new ArrayList<>();
		boolean someoneHasSpike = false;
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (teamOf(player.getUuid()) == Team.ATTACKER) {
				attackers.add(player);
				if (player.getInventory().contains(new ItemStack(ModItems.SPIKE))) {
					someoneHasSpike = true;
				}
			}
		}
		if (!someoneHasSpike && !attackers.isEmpty()) {
			ServerPlayerEntity carrier = attackers.get(server.getOverworld().random.nextInt(attackers.size()));
			carrier.getInventory().insertStack(new ItemStack(ModItems.SPIKE));
			carrier.sendMessage(Text.literal("You are carrying the Spike.").formatted(Formatting.GOLD), false);
		}
	}

	public void tick(MinecraftServer server) {
		if (phase == Phase.WAITING) {
			return;
		}
		phaseTicks--;

		if (phase == Phase.ROUND && spike.planted) {
			tickSpike(server);
		}

		if (phase == Phase.ROUND) {
			checkElimination(server);
			if (phaseTicks % 240 == 0) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					AbilityManager.get().addUltimatePoints(player, 1);
				}
			}
		}

		if (phaseTicks <= 0) {
			advancePhase(server);
		}

		if (phaseTicks % 10 == 0) {
			syncState(server);
		}
	}

	private void tickSpike(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		if (spike.lastDefuseInputTick >= 0 && world.getTime() - spike.lastDefuseInputTick > DEFUSE_INPUT_TIMEOUT_TICKS) {
			spike.defuseProgressTicks = 0;
			spike.defusingPlayer = null;
		}

		spike.fuseTicksRemaining--;
		if (spike.fuseTicksRemaining <= 0) {
			detonateSpike(server);
			return;
		}
		if (spike.fuseTicksRemaining % 20 == 0 && spike.pos != null) {
			float pitch = 1.0f + (1.0f - (float) spike.fuseTicksRemaining / PLANT_FUSE_TICKS) * 1.5f;
			world.playSound(null, spike.pos, SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), SoundCategory.BLOCKS, 1.0f, pitch);
		}
	}

	private void detonateSpike(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		if (spike.pos != null) {
			Vec3d center = Vec3d.ofCenter(spike.pos);
			world.playSound(null, spike.pos, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 4.0f, 0.7f);
			world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0, 0, 0, 0.0);
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (player.getPos().distanceTo(center) <= 8.0 && isAlive(player.getUuid())) {
					player.damage(world.getDamageSources().generic(), 1000.0f);
				}
			}
		}
		removeSpikeDisplay(server);
		broadcast(server, Text.literal("[ValorantCraft] The spike has detonated! Attackers win the round.").formatted(Formatting.RED));
		endRound(server, Team.ATTACKER, "spike detonated");
	}

	private void removeSpikeDisplay(MinecraftServer server) {
		if (spike.displayEntityUuid != null) {
			ServerWorld world = server.getOverworld();
			var entity = world.getEntity(spike.displayEntityUuid);
			if (entity != null) {
				entity.discard();
			}
		}
		spike.reset();
	}

	private void checkElimination(MinecraftServer server) {
		boolean attackersAlive = false;
		boolean defendersAlive = false;
		for (Map.Entry<UUID, Team> entry : teams.entrySet()) {
			if (!alive.getOrDefault(entry.getKey(), false)) {
				continue;
			}
			if (entry.getValue() == Team.ATTACKER) {
				attackersAlive = true;
			} else if (entry.getValue() == Team.DEFENDER) {
				defendersAlive = true;
			}
		}
		if (!attackersAlive && !spike.planted) {
			broadcast(server, Text.literal("[ValorantCraft] Attackers eliminated! Defenders win the round.").formatted(Formatting.BLUE));
			endRound(server, Team.DEFENDER, "elimination");
		} else if (!defendersAlive) {
			broadcast(server, Text.literal("[ValorantCraft] Defenders eliminated! Attackers win the round.").formatted(Formatting.RED));
			endRound(server, Team.ATTACKER, "elimination");
		}
	}

	private void advancePhase(MinecraftServer server) {
		switch (phase) {
			case BUY -> beginRoundPhase(server);
			case ROUND -> {
				if (spike.planted) {
					// The spike fuse (tickSpike) governs the outcome now, not the round clock.
					return;
				}
				broadcast(server, Text.literal("[ValorantCraft] Time expired. Defenders win the round.").formatted(Formatting.BLUE));
				endRound(server, Team.DEFENDER, "time expired");
			}
			case END_ROUND -> {
				if (attackerScore >= ROUNDS_TO_WIN || defenderScore >= ROUNDS_TO_WIN) {
					Team winner = attackerScore > defenderScore ? Team.ATTACKER : Team.DEFENDER;
					broadcast(server, Text.literal("[ValorantCraft] Match over! " + winner + " wins " +
							Math.max(attackerScore, defenderScore) + "-" + Math.min(attackerScore, defenderScore)).formatted(Formatting.GOLD));
					phase = Phase.WAITING;
				} else {
					beginBuyPhase(server);
				}
			}
			default -> {
			}
		}
	}

	private void endRound(MinecraftServer server, Team winner, String reason) {
		boolean spikeWasPlanted = spike.planted;
		if (winner == Team.ATTACKER) {
			attackerScore++;
		} else {
			defenderScore++;
		}
		for (Map.Entry<UUID, Team> entry : teams.entrySet()) {
			boolean won = entry.getValue() == winner;
			int amount = won ? WIN_CREDITS : LOSS_CREDITS;
			if (!won && entry.getValue() == Team.ATTACKER && spikeWasPlanted) {
				amount += PLANT_BONUS_CREDITS;
			}
			addCredits(entry.getKey(), amount);
		}
		phase = Phase.END_ROUND;
		phaseTicks = END_ROUND_TICKS;
		syncState(server);
	}

	public void onPlayerKilled(ServerPlayerEntity victim, ServerPlayerEntity killer) {
		if (!teams.containsKey(victim.getUuid())) {
			return;
		}
		alive.put(victim.getUuid(), false);
		if (killer != null && teams.containsKey(killer.getUuid())) {
			addCredits(killer.getUuid(), KILL_CREDITS);
			AbilityManager.get().addUltimatePoints(killer, 1);
		}
		WeaponServerLogic.removeState(victim);
	}

	public boolean tryPlantSpike(ServerPlayerEntity player, BlockPos pos) {
		if (phase != Phase.ROUND || spike.planted) {
			return false;
		}
		if (teamOf(player.getUuid()) != Team.ATTACKER) {
			return false;
		}
		if (!isInsideAnyBombSite(Vec3d.ofCenter(pos))) {
			player.sendMessage(Text.literal("You must be inside a bomb site to plant.").formatted(Formatting.RED), true);
			return false;
		}

		ServerWorld world = player.getServerWorld();
		spike.reset();
		spike.planted = true;
		spike.pos = pos;
		spike.fuseTicksRemaining = PLANT_FUSE_TICKS;

		DisplayEntity.ItemDisplayEntity display = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);
		display.setItemStack(new ItemStack(ModItems.SPIKE));
		display.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		world.spawnEntity(display);
		spike.displayEntityUuid = display.getUuid();

		world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 1.4f);
		broadcast(player.getServer(), Text.literal("[ValorantCraft] The spike has been planted!").formatted(Formatting.RED));
		return true;
	}

	public void handleDefuseTick(ServerPlayerEntity player) {
		if (phase != Phase.ROUND || !spike.planted || spike.pos == null) {
			return;
		}
		if (teamOf(player.getUuid()) != Team.DEFENDER) {
			return;
		}
		if (player.getPos().distanceTo(Vec3d.ofCenter(spike.pos)) > DEFUSE_RANGE) {
			return;
		}
		ServerWorld world = player.getServerWorld();
		long now = world.getTime();
		if (spike.defusingPlayer != null && !spike.defusingPlayer.equals(player.getUuid())
				&& now - spike.lastDefuseInputTick <= DEFUSE_INPUT_TIMEOUT_TICKS) {
			return;
		}
		spike.defusingPlayer = player.getUuid();
		spike.lastDefuseInputTick = now;
		spike.defuseProgressTicks++;

		if (spike.defuseProgressTicks >= DEFUSE_TICKS_REQUIRED) {
			removeSpikeDisplay(world.getServer());
			broadcast(world.getServer(), Text.literal("[ValorantCraft] The spike has been defused! Defenders win the round.").formatted(Formatting.BLUE));
			endRound(world.getServer(), Team.DEFENDER, "spike defused");
		}
	}

	private void broadcast(MinecraftServer server, Text text) {
		if (server == null) {
			return;
		}
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.sendMessage(text, false);
		}
	}

	private void syncState(MinecraftServer server) {
		String phaseName = phase.name();
		int spikeTicksLeft = spike.planted ? spike.fuseTicksRemaining : 0;
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			MatchStatePayload payload = new MatchStatePayload(
					phaseName, Math.max(0, phaseTicks), spike.planted, spikeTicksLeft,
					getCredits(player.getUuid()), teamOf(player.getUuid()).name(), attackerScore, defenderScore
			);
			ModNetworking.sendMatchState(player, payload);
		}
	}
}
