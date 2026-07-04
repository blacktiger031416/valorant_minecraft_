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

/**
 * Simplified flash: raycasts forward (stopping at the first wall) and blinds every
 * enemy within a radius of the impact point. Real Valorant flashes are thrown
 * projectiles with an arc and facing-angle check; that is left as a follow-up.
 */
public record FlashAbility(String id, String displayName, int chargesPerRound, int price, double range, double radius, int blindTicks) implements Ability {
	@Override
	public void activate(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d eyePos = player.getEyePos();
		Vec3d end = eyePos.add(player.getRotationVec(1.0f).multiply(range));

		BlockHitResult hit = world.raycast(new RaycastContext(eyePos, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		Vec3d impact = hit.getType() != HitResult.Type.MISS ? hit.getPos() : end;

		world.spawnParticles(ParticleTypes.FLASH, impact.x, impact.y, impact.z, 1, 0, 0, 0, 0.0);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 2.0f, 1.8f);

		Team casterTeam = MatchManager.get().teamOf(player.getUuid());
		Box area = Box.of(impact, radius * 2, radius * 2, radius * 2);
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, area, e -> e != player)) {
			if (entity instanceof ServerPlayerEntity target && MatchManager.get().teamOf(target.getUuid()) == casterTeam) {
				continue;
			}
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, blindTicks, 0));
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, blindTicks, 0));
		}
	}
}
