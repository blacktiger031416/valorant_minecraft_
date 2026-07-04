package com.valorantcraft.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.valorantcraft.match.MatchManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ValorantCommand {
	private ValorantCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("valorant")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.literal("start").executes(context -> {
							MatchManager.get().start(context.getSource().getServer());
							return 1;
						}))
						.then(CommandManager.literal("stop").executes(context -> {
							MatchManager.get().stop(context.getSource().getServer());
							return 1;
						}))
						.then(CommandManager.literal("site")
								.then(CommandManager.literal("add")
										.then(CommandManager.argument("name", StringArgumentType.word())
												.then(CommandManager.argument("pos1", BlockPosArgumentType.blockPos())
														.then(CommandManager.argument("pos2", BlockPosArgumentType.blockPos())
																.executes(context -> {
																	String name = StringArgumentType.getString(context, "name");
																	BlockPos pos1 = BlockPosArgumentType.getBlockPos(context, "pos1");
																	BlockPos pos2 = BlockPosArgumentType.getBlockPos(context, "pos2");
																	Box box = new Box(Vec3d.of(pos1), Vec3d.of(pos2));
																	MatchManager.get().addBombSite(name, box);
																	context.getSource().sendFeedback(() -> Text.literal("Added bomb site '" + name + "'"), false);
																	return 1;
																})))))))
		);
	}
}
