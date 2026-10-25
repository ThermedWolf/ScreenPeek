package io.github.thermedwolf.screenpeek.client.render;

import io.github.thermedwolf.screenpeek.protocol.MenuType;

import net.minecraft.ChatFormatting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuLabelsTest {

	@Test
	void everyMenuTypeHasDistinctText() {
		assertEquals("inventory", MenuLabels.text(MenuType.INVENTORY));
		assertEquals("chest", MenuLabels.text(MenuType.CHEST));
		assertEquals("crafting", MenuLabels.text(MenuType.CRAFTING));
		assertEquals("furnace", MenuLabels.text(MenuType.FURNACE));
		assertEquals("menu", MenuLabels.text(MenuType.GENERIC));
		assertEquals("no menu", MenuLabels.text(MenuType.NONE));
	}

	@Test
	void renderableMenusHaveDistinctColors() {
		assertEquals(ChatFormatting.GREEN, MenuLabels.color(MenuType.INVENTORY));
		assertEquals(ChatFormatting.GOLD, MenuLabels.color(MenuType.CHEST));
		assertEquals(ChatFormatting.AQUA, MenuLabels.color(MenuType.CRAFTING));
		assertEquals(ChatFormatting.RED, MenuLabels.color(MenuType.FURNACE));
		assertEquals(ChatFormatting.WHITE, MenuLabels.color(MenuType.GENERIC));
		assertEquals(ChatFormatting.GRAY, MenuLabels.color(MenuType.NONE));
	}
}
