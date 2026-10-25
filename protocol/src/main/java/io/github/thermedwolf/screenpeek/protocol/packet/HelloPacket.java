package io.github.thermedwolf.screenpeek.protocol.packet;

/**
 * Client handshake: "I speak ScreenPeek". The body is empty — the protocol
 * version travels in the packet envelope, so this packet only announces
 * presence. The relay answers with a {@link WelcomePacket}.
 */
public record HelloPacket() implements ServerboundPacket {
}
