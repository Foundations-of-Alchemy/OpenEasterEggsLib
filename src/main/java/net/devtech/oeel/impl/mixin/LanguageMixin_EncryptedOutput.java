package net.devtech.oeel.impl.mixin;

import java.io.IOException;
import java.security.GeneralSecurityException;

import net.devtech.oeel.impl.OEELInternal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Language;

@Mixin(value = TranslationStorage.class, targets = "net/minecraft/util/Language$1")
public abstract class LanguageMixin_EncryptedOutput extends Language {
	@Inject(method = "get(Ljava/lang/String;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
	public void onGet(String key, CallbackInfoReturnable<String> cir) throws GeneralSecurityException, IOException {
		String output = OEELInternal.decodeLang(key, this::get);
		if(output != null) {
			cir.setReturnValue(output);
		}
	}
}
