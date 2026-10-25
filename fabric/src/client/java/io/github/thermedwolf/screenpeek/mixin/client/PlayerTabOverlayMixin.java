package io.github.thermedwolf.screenpeek.mixin.client;

import io.github.thermedwolf.screenpeek.client.ClientActivity;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Marks idle players in the tab player list: their name renders grey and
 * italic instead of the normal white. Grey+italic avoids gambling on special
 * glyphs in Minecraft's font while staying clearly distinct at a glance.
 */
@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {

	@Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
	private void screenpeek$styleIdlePlayers(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
		if (ClientActivity.PEERS.isIdle(info.getProfile().id())) {
			cir.setReturnValue(cir.getReturnValue().copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
	}
}
