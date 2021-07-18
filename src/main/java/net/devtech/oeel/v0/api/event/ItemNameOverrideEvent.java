package net.devtech.oeel.v0.api.event;

import java.util.function.Function;

import io.github.astrarre.access.v0.api.FunctionAccess;
import io.github.astrarre.access.v0.fabric.helper.ItemAccessHelper;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public interface ItemNameOverrideEvent {

	/**
	 * return null
	 */
	FunctionAccess<ItemStack, Text> EVENT = new FunctionAccess<>(OEELInternal.id2("item_lang_override"));
	ItemAccessHelper<Function<ItemStack, Text>> ITEM_FILTER = ItemAccessHelper.create(EVENT, function -> stack -> function.apply(stack).apply(stack), ItemStack::getItem, s -> null);

	Void _STATIC = Util.make(() -> {
		EVENT.addProviderFunction();
		EVENT.dependsOn(ItemLangOverrideEvent.EVENT, event -> stack -> Validate.transform(event.apply(stack), TranslatableText::new));
		return null;
	});
}
