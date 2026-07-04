package com.valorantcraft.ability.impl;

import com.valorantcraft.ability.Ability;
import com.valorantcraft.match.MatchManager;
import com.valorantcraft.match.Team;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Reveals every enemy within {@code radius} of the raycast impact point with Glowing. */
public record ReconAbility(String id, String displayName, int chargesPerRound, int price, double range, double radius, int glowTicks) implements Ability {
	@Override
	public void activate(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d eyePos = player.getEyePos();
		Vec3d end = eyePos.add(player.getRotationVec(1.0f).multiply(range));
		BlockHitResult hit = world.raycast(new RaycastContext(eyePos, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		Vec3d impact = hit.getType() != HitResult.Type.MISS ? hit.getPos() : end;

		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_SPYGLASS_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
		world.spawnParticles(ParticleTypes.SONIC_BOOM, impact.x, impact.y, impact.z, 1, 0, 0, 0, 0.0);

		Team casterTeam = MatchManager.get().teamOf(player.getUuid());
		Box area = Box.of(impact, radius * 2, radius * 2, radius * 2);
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, area, e -> e != player)) {
			if (entity instanceof ServerPlayerEntity target && MatchManager.get().teamOf(target.getUuid()) == casterTeam) {
				continue;
			}
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, glowTicks, 0));
		}
	}
}
