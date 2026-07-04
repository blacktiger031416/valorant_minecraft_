package com.valorantcraft;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * White-wool block-display "smoke" projectile.
 *
 * <p>While the owner holds C, it's guided: steers smoothly toward wherever they're currently
 * looking, ignoring gravity. The moment they release C, it locks onto whatever direction it had
 * at that instant and becomes a normal falling projectile (gravity applies) until it lands on a
 * block. Landing is what triggers the "grow + slowly spin" smoke-cloud transformation, which then
 * disappears 3.5s later. Only one un-landed projectile per player is allowed at a time.
 */
public final class SmokeAbility {
	private static final double GUIDED_SPEED = 0.6;
	private static final double TURN_RATE = 0.15;
	private static final double GRAVITY_PER_TICK = 0.05;
	private static final int MAX_FLIGHT_TICKS = 100;

	private static final float INITIAL_SCALE = 0.3f;
	private static final float EXPANDED_SCALE = 2.5f;
	private static final int GROW_TICKS = 10;
	private static final int SMOKE_LIFETIME_TICKS = 70; // 3.5s
	private static final int ROTATE_STEP_TICKS = 8;
	private static final float ROTATE_STEP_DEGREES = 25f;

	private static final List<Projectile> ACTIVE = new ArrayList<>();
	private static final Map<UUID, Boolean> HOLDING = new HashMap<>();

	private SmokeAbility() {
	}

	public static void setHolding(ServerPlayerEntity player, boolean held) {
		UUID ownerId = player.getUuid();
		boolean wasHolding = HOLDING.getOrDefault(ownerId, false);
		HOLDING.put(ownerId, held);

		if (held && !wasHolding) {
			if (!hasActiveProjectile(ownerId)) {
				launch(player);
			}
		} else if (!held && wasHolding) {
			for (Projectile projectile : ACTIVE) {
				if (projectile.ownerId.equals(ownerId) && !projectile.expanded) {
					projectile.guided = false;
				}
			}
		}
	}

	private static boolean hasActiveProjectile(UUID ownerId) {
		for (Projectile projectile : ACTIVE) {
			if (projectile.ownerId.equals(ownerId) && !projectile.expanded) {
				return true;
			}
		}
		return false;
	}

	private static void launch(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d direction = player.getRotationVec(1.0f);
		Vec3d spawnPos = player.getEyePos().add(direction.multiply(0.5));

		DisplayEntity.BlockDisplayEntity display = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
		display.setBlockState(Blocks.WHITE_WOOL.getDefaultState());
		display.setTeleportDuration(3);
		display.setTransformation(centeredTransform(INITIAL_SCALE, new Quaternionf()));
		display.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, 0, 0);
		world.spawnEntity(display);

		Projectile projectile = new Projectile(display, player.getUuid());
		projectile.velocity = direction.multiply(GUIDED_SPEED);
		projectile.guided = true;
		ACTIVE.add(projectile);
	}

	public static void tick(MinecraftServer server) {
		Iterator<Projectile> iterator = ACTIVE.iterator();
		while (iterator.hasNext()) {
			Projectile projectile = iterator.next();
			if (projectile.display.isRemoved()) {
				iterator.remove();
				continue;
			}
			ServerWorld world = (ServerWorld) projectile.display.getWorld();

			if (!projectile.expanded) {
				tickFlying(server, world, projectile);
			} else {
				tickExpanded(world, projectile, iterator);
			}
		}
	}

	private static void tickFlying(MinecraftServer server, ServerWorld world, Projectile projectile) {
		if (projectile.guided) {
			ServerPlayerEntity owner = server.getPlayerManager().getPlayer(projectile.ownerId);
			if (owner != null) {
				Vec3d targetDirection = owner.getRotationVec(1.0f);
				Vec3d currentDirection = projectile.velocity.normalize();
				Vec3d newDirection = currentDirection.multiply(1 - TURN_RATE)
						.add(targetDirection.multiply(TURN_RATE))
						.normalize();
				projectile.velocity = newDirection.multiply(GUIDED_SPEED);
			}
		} else {
			projectile.velocity = projectile.velocity.add(0, -GRAVITY_PER_TICK, 0);
		}

		Vec3d currentPos = projectile.display.getPos();
		Vec3d nextPos = currentPos.add(projectile.velocity);
		BlockPos nextBlockPos = BlockPos.ofFloored(nextPos);

		boolean hitBlock = !world.getBlockState(nextBlockPos).getCollisionShape(world, nextBlockPos).isEmpty();
		projectile.flightTicks++;

		if (hitBlock || projectile.flightTicks >= MAX_FLIGHT_TICKS) {
			expand(world, projectile);
		} else {
			projectile.display.setPosition(nextPos.x, nextPos.y, nextPos.z);
		}
	}

	private static void expand(ServerWorld world, Projectile projectile) {
		projectile.expanded = true;
		projectile.expandTick = world.getTime();
		projectile.display.setInterpolationDuration(GROW_TICKS);
		projectile.display.setStartInterpolation(0);
		projectile.display.setTransformation(centeredTransform(EXPANDED_SCALE, new Quaternionf()));
	}

	private static void tickExpanded(ServerWorld world, Projectile projectile, Iterator<Projectile> iterator) {
		long age = world.getTime() - projectile.expandTick;

		if (age > 0 && age % ROTATE_STEP_TICKS == 0) {
			projectile.rotationDegrees += ROTATE_STEP_DEGREES;
			projectile.display.setInterpolationDuration(ROTATE_STEP_TICKS);
			projectile.display.setStartInterpolation(0);
			projectile.display.setTransformation(centeredTransform(
					EXPANDED_SCALE, RotationAxis.POSITIVE_Y.rotationDegrees(projectile.rotationDegrees)));
		}

		if (age >= SMOKE_LIFETIME_TICKS) {
			projectile.display.discard();
			iterator.remove();
		}
	}

	/**
	 * A block display's model space is the unit cube [0,1] anchored at its corner, not its
	 * center. To have it grow/spin around the entity's actual world position (its visual
	 * center) rather than orbiting around that corner, the translation has to counter-rotate
	 * with whatever spin we apply - so it's recomputed here instead of being a fixed offset.
	 */
	private static AffineTransformation centeredTransform(float scale, Quaternionf rotation) {
		Vector3f halfExtent = new Vector3f(0.5f * scale, 0.5f * scale, 0.5f * scale);
		Vector3f rotatedHalfExtent = new Vector3f(halfExtent);
		rotation.transform(rotatedHalfExtent);
		Vector3f translation = new Vector3f(-rotatedHalfExtent.x, -rotatedHalfExtent.y, -rotatedHalfExtent.z);
		return new AffineTransformation(translation, rotation, new Vector3f(scale, scale, scale), new Quaternionf());
	}

	private static final class Projectile {
		final DisplayEntity.BlockDisplayEntity display;
		final UUID ownerId;
		Vec3d velocity = Vec3d.ZERO;
		boolean guided = false;
		int flightTicks = 0;
		boolean expanded = false;
		long expandTick = 0;
		float rotationDegrees = 0f;

		Projectile(DisplayEntity.BlockDisplayEntity display, UUID ownerId) {
			this.display = display;
			this.ownerId = ownerId;
		}
	}
}
