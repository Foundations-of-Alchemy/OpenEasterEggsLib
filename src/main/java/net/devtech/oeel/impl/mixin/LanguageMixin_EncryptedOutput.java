package net.devtech.oeel.impl.mixin;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import net.devtech.oeel.impl.OEELInternal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.language.TranslationStorage;

@Mixin(value = TranslationStorage.class, targets = "net/minecraft/util/Language$1")
public abstract class LanguageMixin_EncryptedOutput {
	@Shadow(aliases = {"translations", "field_25308"}) @Final private Map<String, String> translations;

	@Inject(method = "get", at = @At("RETURN"), cancellable = true)
	public void onGet(String key, CallbackInfoReturnable<String> cir) throws GeneralSecurityException, IOException {
		String output = OEELInternal.decodeLang(key, this.translations::get);
		if(output != null) {
			cir.setReturnValue(output);
		}
	}
}
