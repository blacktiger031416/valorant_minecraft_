package com.valorantcraft.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative weapon firing, hit detection, damage, and reload timing. */
public final class WeaponServerLogic {
	private static final Map<UUID, PlayerCombatState> STATES = new HashMap<>();
	private static final Random RANDOM = Random.create();

	private WeaponServerLogic() {
	}

	public static PlayerCombatState state(ServerPlayerEntity player) {
		return STATES.computeIfAbsent(player.getUuid(), id -> new PlayerCombatState());
	}

	public static void removeState(ServerPlayerEntity player) {
		STATES.remove(player.getUuid());
	}

	public static void handleFire(ServerPlayerEntity player) {
		ItemStack stack = player.getMainHandStack();
		if (!(stack.getItem() instanceof WeaponItem weaponItem)) {
			return;
		}
		WeaponType type = weaponItem.getWeaponType();
		PlayerCombatState combat = state(player);
		ServerWorld world = player.getServerWorld();
		long now = world.getTime();

		if (combat.reloadEndTick > 0) {
			return;
		}
		if (now - combat.lastShotTick < type.fireIntervalTicks()) {
			return;
		}

		WeaponAmmoData.initializeIfAbsent(stack, type);
		int magazine = WeaponAmmoData.getMagazine(stack, type);
		int reserve = WeaponAmmoData.getReserve(stack, type);

		if (magazine <= 0) {
			world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(), SoundCategory.PLAYERS, 0.5f, 1.6f);
			return;
		}

		if (now - combat.lastShotTick > type.fireIntervalTicks() * 6L) {
			combat.consecutiveShots = 0;
		}
		combat.lastShotTick = now;
		int shotIndex = combat.consecutiveShots++;

		WeaponAmmoData.set(stack, magazine - 1, reserve);

		int pellets = Math.max(1, type.burstCount());
		for (int i = 0; i < pellets; i++) {
			fireSinglePellet(player, world, type, shotIndex);
		}

		world.playSound(null, player.getBlockPos(), type.fireSound(), SoundCategory.PLAYERS, 1.0f, fireSoundPitch(type));
	}

	private static float fireSoundPitch(WeaponType type) {
		if (type.range() >= 200.0) {
			return 0.6f;
		} else if (type.magazineSize() <= 8) {
			return 1.3f;
		}
		return 1.0f;
	}

	private static void fireSinglePellet(ServerPlayerEntity player, ServerWorld world, WeaponType type, int shotIndex) {
		Vec3d eyePos = player.getEyePos();
		Vec3d look = player.getRotationVec(1.0f);

		boolean moving = player.getVelocity().horizontalLength() > 0.06;
		double spreadDeg = type.baseSpreadDegrees() * (moving ? type.movingSpreadMultiplier() : 1.0);
		Vec3d direction = applySpread(look, spreadDeg);

		Vec3d end = eyePos.add(direction.multiply(type.range()));

		BlockHitResult blockHit = world.raycast(new RaycastContext(eyePos, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		double maxDistance = blockHit.getType() != HitResult.Type.MISS ? eyePos.distanceTo(blockHit.getPos()) : type.range();

		Box searchBox = player.getBoundingBox().stretch(direction.multiply(type.range())).expand(1.0);
		EntityHitResult entityHit = ProjectileUtil.raycast(player, eyePos, end, searchBox,
				candidate -> candidate instanceof LivingEntity && candidate.canHit() && candidate != player,
				maxDistance * maxDistance);

		spawnTracer(world, eyePos, blockHit.getType() != HitResult.Type.MISS ? blockHit.getPos() : end);

		if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
			applyDamage(player, target, type, entityHit.getPos());
		}
	}

	private static Vec3d applySpread(Vec3d look, double spreadDegrees) {
		if (spreadDegrees <= 0.0001) {
			return look;
		}
		float yaw = (float) (RANDOM.nextTriangular(0.0, spreadDegrees));
		float pitch = (float) (RANDOM.nextTriangular(0.0, spreadDegrees));
		return rotate(look, pitch, yaw);
	}

	private static Vec3d rotate(Vec3d look, float pitchDeg, float yawDeg) {
		double yawRad = Math.toRadians(yawDeg);
		double pitchRad = Math.toRadians(pitchDeg);
		Vec3d yawed = look.rotateY((float) yawRad);
		return yawed.rotateX((float) pitchRad);
	}

	private static void spawnTracer(ServerWorld world, Vec3d from, Vec3d to) {
		Vec3d diff = to.subtract(from);
		int steps = MathHelper.clamp((int) (diff.length() / 2.0), 1, 40);
		for (int i = 1; i <= steps; i++) {
			Vec3d point = from.add(diff.multiply((double) i / steps));
			world.spawnParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 1, 0, 0, 0, 0.0);
		}
	}

	private static void applyDamage(ServerPlayerEntity attacker, LivingEntity target, WeaponType type, Vec3d hitPos) {
		HitZone zone = classifyHit(target, hitPos);
		float damage = switch (zone) {
			case HEAD -> type.bodyDamage() * type.headMultiplier();
			case LEG -> type.bodyDamage() * type.legMultiplier();
			case BODY -> (float) type.bodyDamage();
		};

		DamageSource source = attacker.getWorld().getDamageSources().playerAttack(attacker);
		target.damage(source, damage);

		if (zone == HitZone.HEAD) {
			attacker.getServerWorld().playSound(null, attacker.getBlockPos(), SoundEvents.UI_TOAST_IN, SoundCategory.PLAYERS, 0.4f, 1.8f);
		}
	}

	private static HitZone classifyHit(LivingEntity target, Vec3d hitPos) {
		double relative = (hitPos.y - target.getY()) / Math.max(0.1, target.getHeight());
		if (relative >= 0.82) {
			return HitZone.HEAD;
		} else if (relative <= 0.28) {
			return HitZone.LEG;
		}
		return HitZone.BODY;
	}

	public static void handleReload(ServerPlayerEntity player) {
		ItemStack stack = player.getMainHandStack();
		if (!(stack.getItem() instanceof WeaponItem weaponItem)) {
			return;
		}
		WeaponType type = weaponItem.getWeaponType();
		PlayerCombatState combat = state(player);
		if (combat.reloadEndTick > 0) {
			return;
		}

		WeaponAmmoData.initializeIfAbsent(stack, type);
		int magazine = WeaponAmmoData.getMagazine(stack, type);
		int reserve = WeaponAmmoData.getReserve(stack, type);
		if (magazine >= type.magazineSize() || reserve <= 0) {
			return;
		}

		combat.reloadEndTick = player.getServerWorld().getTime() + type.reloadTicks();
		combat.reloadingWeaponId = type.id();
		player.getServerWorld().playSound(null, player.getBlockPos(), type.reloadSound(), SoundCategory.PLAYERS, 0.8f, 1.0f);
	}

	public static void tick(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			PlayerCombatState combat = STATES.get(player.getUuid());
			if (combat == null || combat.reloadEndTick <= 0) {
				continue;
			}
			if (player.getServerWorld().getTime() >= combat.reloadEndTick) {
				completeReload(player, combat);
			}
		}
	}

	private static void completeReload(ServerPlayerEntity player, PlayerCombatState combat) {
		combat.reloadEndTick = -1;
		ItemStack stack = player.getMainHandStack();
		if (stack.getItem() instanceof WeaponItem weaponItem && weaponItem.getWeaponType().id().equals(combat.reloadingWeaponId)) {
			WeaponType type = weaponItem.getWeaponType();
			int magazine = WeaponAmmoData.getMagazine(stack, type);
			int reserve = WeaponAmmoData.getReserve(stack, type);
			int needed = type.magazineSize() - magazine;
			int transferred = Math.min(needed, reserve);
			WeaponAmmoData.set(stack, magazine + transferred, reserve - transferred);
		}
		combat.reloadingWeaponId = null;
	}
}
