package io.github.thermedwolf.screenpeek.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;

import io.github.thermedwolf.screenpeek.protocol.MenuType;

/**
 * Watches which container screen the local player has open and reports it as
 * the open-menu flag. Screen-based detection covers every way of opening a
 * menu (E, right-clicking containers, interacting with blocks).
 */
public final class OpenMenuTracker {

	private static MenuType lastMenu = MenuType.NONE;

	private OpenMenuTracker() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			MenuType menu = menuTypeOf(client.gui.screen());
			if (menu == lastMenu) {
				return;
			}
			lastMenu = menu;
			ClientActivity.reporter().setOpenMenu(menu);
		});
	}

	/** Maps a screen to the protocol's menu type; pure and exhaustive. */
	static MenuType menuTypeOf(Screen screen) {
		return switch (screen) {
			case InventoryScreen _ -> MenuType.INVENTORY;
			case CraftingScreen _, CrafterScreen _ -> MenuType.CRAFTING;
			case FurnaceScreen _, BlastFurnaceScreen _, SmokerScreen _ -> MenuType.FURNACE;
			case ContainerScreen _, ShulkerBoxScreen _, HopperScreen _, DispenserScreen _ -> MenuType.CHEST;
			case AbstractContainerScreen _ -> MenuType.GENERIC; // any other container: merchant, brewing, loom, ...
			case null, default -> MenuType.NONE; // no screen, or a non-container screen (chat, pause, ...)
		};
	}
}
