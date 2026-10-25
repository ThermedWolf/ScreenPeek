package io.github.thermedwolf.screenpeek.protocol.packet;

/** Packets sent from the server relay to ScreenPeek clients. */
public sealed interface ClientboundPacket extends ScreenPeekPacket permits WelcomePacket, PlayerActivityPacket {
}
