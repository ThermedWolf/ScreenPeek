package io.github.thermedwolf.screenpeek.protocol.packet;

/**
 * A ScreenPeek wire packet. Every packet is either {@link ServerboundPacket}
 * (client to server) or {@link ClientboundPacket} (server to client); the
 * direction is carried by the channel it travels on, and each direction has
 * its own codec entry point in {@code ScreenPeekCodec}.
 */
public sealed interface ScreenPeekPacket permits ServerboundPacket, ClientboundPacket {
}
