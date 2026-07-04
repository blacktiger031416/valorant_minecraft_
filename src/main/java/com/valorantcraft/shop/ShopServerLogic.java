package com.valorantcraft.shop;

import com.valorantcraft.match.MatchManager;
import com.valorantcraft.match.Phase;
import com.valorantcraft.registry.ModItems;
import com.valorantcraft.weapon.ModWeapons;
import com.valorantcraft.weapon.WeaponType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ShopServerLogic {
	private ShopServerLogic() {
	}

	public static void handleBuy(ServerPlayerEntity player, String itemId) {
		MatchManager match = MatchManager.get();
		if (match.getPhase() != Phase.BUY) {
			player.sendMessage(Text.literal("You can only buy during the buy phase.").formatted(Formatting.RED), true);
			return;
		}

		WeaponType type = ModWeapons.byId(itemId);
		if (type == null) {
			return;
		}
		Item item = ModItems.weaponItemForId(itemId);
		if (item == null) {
			return;
		}

		if (!match.spendCredits(player.getUuid(), type.price())) {
			player.sendMessage(Text.literal("Not enough credits.").formatted(Formatting.RED), true);
			return;
		}

		player.getInventory().insertStack(new ItemStack(item));
		player.sendMessage(Text.literal("Purchased " + itemId + ".").formatted(Formatting.GREEN), true);
	}
}
