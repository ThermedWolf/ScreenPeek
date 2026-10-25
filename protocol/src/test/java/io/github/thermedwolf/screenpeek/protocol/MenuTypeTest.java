package io.github.thermedwolf.screenpeek.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuTypeTest {

	@Test
	void knownWireIdsResolve() {
		assertEquals(MenuType.NONE, MenuType.fromWire(0));
		assertEquals(MenuType.INVENTORY, MenuType.fromWire(1));
		assertEquals(MenuType.CHEST, MenuType.fromWire(2));
		assertEquals(MenuType.CRAFTING, MenuType.fromWire(3));
		assertEquals(MenuType.FURNACE, MenuType.fromWire(4));
		assertEquals(MenuType.GENERIC, MenuType.fromWire(255));
	}

	@Test
	void unknownWireIdsDegradeToGeneric() {
		assertEquals(MenuType.GENERIC, MenuType.fromWire(5));
		assertEquals(MenuType.GENERIC, MenuType.fromWire(99));
	}
}
