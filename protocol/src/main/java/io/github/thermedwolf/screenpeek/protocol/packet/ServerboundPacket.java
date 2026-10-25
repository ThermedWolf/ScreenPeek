package io.github.thermedwolf.screenpeek.protocol.packet;

/** Packets sent from a ScreenPeek client to the server relay. */
public sealed interface ServerboundPacket extends ScreenPeekPacket permits HelloPacket, ActivityUpdatePacket {
}
