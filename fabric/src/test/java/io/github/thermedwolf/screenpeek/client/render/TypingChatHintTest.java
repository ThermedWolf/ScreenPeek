package io.github.thermedwolf.screenpeek.client.render;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypingChatHintTest {

	@Test
	void singlePlayer() {
		assertEquals("Alice is typing...", TypingChatHint.buildHint(List.of("Alice")));
	}

	@Test
	void twoPlayersJoinWithAnd() {
		assertEquals("Alice and Bob are typing...", TypingChatHint.buildHint(List.of("Alice", "Bob")));
	}

	@Test
	void threePlayersJoinWithCommasAndAnd() {
		assertEquals("Alice, Bob and Carol are typing...",
				TypingChatHint.buildHint(List.of("Alice", "Bob", "Carol")));
	}

	@Test
	void moreThanThreePlayersAreSummarized() {
		assertEquals("Alice, Bob and Carol and 1 more are typing...",
				TypingChatHint.buildHint(List.of("Alice", "Bob", "Carol", "Dave")));
	}
}
