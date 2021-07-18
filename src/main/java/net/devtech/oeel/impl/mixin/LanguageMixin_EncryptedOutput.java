package net.devtech.oeel.impl.mixin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.function.UnaryOperator;

import com.google.common.hash.HashCode;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Language;

@Mixin(value = TranslationStorage.class, targets = "net/minecraft/util/Language$1")
public abstract class LanguageMixin_EncryptedOutput extends Language {
	static final String OEEL_LANG_STARTER = "lang.info.";

	@Inject(method = "get(Ljava/lang/String;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
	public void onGet(String key, CallbackInfoReturnable<String> cir) throws GeneralSecurityException, IOException {
		String output = oeel_decodeLang(key, this::get);
		if(output != null) {
			cir.setReturnValue(output);
		}
	}

	static String oeel_decodeLang(String key, UnaryOperator<String> langGetter) throws GeneralSecurityException, IOException {
		if(key.startsWith(OEEL_LANG_STARTER)) {
			int index = key.lastIndexOf('.');
			if(index <= OEEL_LANG_STARTER.length()) {
				return null;
			}

			String langKey = key.substring(0, index); // the key in the en_us.json file
			String decryption = key.substring(index + 1); // the decryption key
			HashCode decryptionKey = HashCode.fromString(decryption);

			String encryptedOutput = langGetter.apply(langKey);
			if(encryptedOutput == null || encryptedOutput.equals(langKey)) {
				return null;
			}

			byte[] decryptedOutput = OEELEncrypting.decrypt(decryptionKey, OEELEncrypting.decodeBase16(encryptedOutput));
			return new String(decryptedOutput, StandardCharsets.UTF_8);
		}
		return null;
	}
}
