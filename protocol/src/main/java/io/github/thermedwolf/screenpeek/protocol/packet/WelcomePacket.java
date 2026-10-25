package io.github.thermedwolf.screenpeek.protocol.packet;

/**
 * Server handshake: "a ScreenPeek relay is here". The body is empty — the
 * relay's protocol version travels in the packet envelope, letting a client
 * detect version mismatches and disable itself gracefully.
 */
public record WelcomePacket() implements ClientboundPacket {
}
