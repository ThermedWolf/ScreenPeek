package io.github.thermedwolf.screenpeek.client.render;

import io.github.thermedwolf.screenpeek.protocol.MenuType;

import net.minecraft.ChatFormatting;

/**
 * Text and colour of the floating menu panel, per menu type. Pure and
 * unit-tested; {@link MenuType#NONE} never reaches rendering (a relay only
 * reports menus alongside other activity) but keeps a defensive mapping.
 */
final class MenuLabels {

	private MenuLabels() {
	}

	static String text(MenuType menu) {
		return switch (menu) {
			case NONE -> "no menu";
			case INVENTORY -> "inventory";
			case CHEST -> "chest";
			case CRAFTING -> "crafting";
			case FURNACE -> "furnace";
			case GENERIC -> "menu";
		};
	}

	static ChatFormatting color(MenuType menu) {
		return switch (menu) {
			case NONE -> ChatFormatting.GRAY;
			case INVENTORY -> ChatFormatting.GREEN;
			case CHEST -> ChatFormatting.GOLD;
			case CRAFTING -> ChatFormatting.AQUA;
			case FURNACE -> ChatFormatting.RED;
			case GENERIC -> ChatFormatting.WHITE;
		};
	}
}
