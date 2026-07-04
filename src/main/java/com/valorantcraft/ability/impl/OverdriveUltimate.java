package com.valorantcraft.ability.impl;

import com.valorantcraft.ability.Ability;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

/** Ultimate: self speed + resistance buff, costing accumulated ultimate points. */
public record OverdriveUltimate(String id, String displayName, int durationTicks) implements Ability {
	@Override
	public int chargesPerRound() {
		return 0;
	}

	@Override
	public int price() {
		return 0;
	}

	@Override
	public void activate(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, durationTicks, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, durationTicks, 0));
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
		world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.5, 0.5, 0.05);
	}
}
