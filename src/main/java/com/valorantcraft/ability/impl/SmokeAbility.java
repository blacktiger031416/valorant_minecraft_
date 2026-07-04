package com.valorantcraft.ability.impl;

import com.valorantcraft.ability.Ability;
import com.valorantcraft.ability.AbilityManager;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Places a temporary wall of white wool (blocks sightlines and bullets, like a real
 * Valorant smoke) at the raycast impact point, then restores the original blocks
 * after {@code durationTicks}.
 */
public record SmokeAbility(String id, String displayName, int chargesPerRound, int price, double range, int durationTicks) implements Ability {
	@Override
	public void activate(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d eyePos = player.getEyePos();
		Vec3d end = eyePos.add(player.getRotationVec(1.0f).multiply(range));
		BlockHitResult hit = world.raycast(new RaycastContext(eyePos, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		BlockPos center = (hit.getType() != HitResult.Type.MISS ? hit.getBlockPos() : BlockPos.ofFloored(end)).up();

		world.playSound(null, center, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 1.0f, 1.4f);
		world.spawnParticles(ParticleTypes.CLOUD, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 40, 1.5, 1.5, 1.5, 0.02);

		AbilityManager.get().spawnTemporaryWall(world, center, 3, 3, durationTicks, Blocks.WHITE_STAINED_GLASS.getDefaultState());
	}
}
