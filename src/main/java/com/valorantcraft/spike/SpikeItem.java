package com.valorantcraft.spike;

import com.valorantcraft.match.MatchManager;
import com.valorantcraft.match.Phase;
import com.valorantcraft.match.Team;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/** Held by an attacker; hold right-click for {@link #PLANT_TICKS} while standing in a bomb site to plant. */
public class SpikeItem extends Item {
	public static final int PLANT_TICKS = 4 * 20;

	public SpikeItem(Settings settings) {
		super(settings);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public int getMaxUseTime(ItemStack stack, LivingEntity user) {
		return PLANT_TICKS;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			MatchManager match = MatchManager.get();
			if (match.getPhase() != Phase.ROUND || match.teamOf(user.getUuid()) != Team.ATTACKER) {
				user.sendMessage(Text.literal("You can only plant during the round as an Attacker."), true);
				return TypedActionResult.fail(stack);
			}
		}
		user.setCurrentHand(hand);
		return TypedActionResult.consume(stack);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (!world.isClient && user instanceof ServerPlayerEntity player) {
			BlockPos pos = player.getBlockPos();
			if (MatchManager.get().tryPlantSpike(player, pos)) {
				stack.decrement(1);
			}
		}
		return stack;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("Hold right-click inside a bomb site to plant."));
	}
}
