package net.devtech.oeel.impl.mixin;

import net.devtech.oeel.v0.api.event.ItemLangOverrideEvent;
import net.devtech.oeel.v0.api.event.ItemNameOverrideEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Mixin(value = ItemStack.class, priority = 10)
public class ItemStackMixin_OverrideTranslation {
	@Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
	public void onGetTranslationKey(CallbackInfoReturnable<String> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		String lang = ItemLangOverrideEvent.EVENT.get().apply(stack);
		if(lang != null) {
			cir.setReturnValue(lang);
		}
	}

	@Inject(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"), cancellable = true)
	public void onGetName(CallbackInfoReturnable<Text> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		Text name = ItemNameOverrideEvent.EVENT.get().apply(stack);
		if(name != null) {
			cir.setReturnValue(name);
		}

		String lang = ItemLangOverrideEvent.EVENT.get().apply(stack);
		if(lang != null) {
			cir.setReturnValue(new TranslatableText(lang));
		}
	}
}
