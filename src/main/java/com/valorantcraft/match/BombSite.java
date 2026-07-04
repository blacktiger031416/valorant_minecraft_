package com.valorantcraft.match;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public record BombSite(String name, Box box) {
	public boolean contains(Vec3d pos) {
		return box.contains(pos.x, pos.y, pos.z);
	}
}
