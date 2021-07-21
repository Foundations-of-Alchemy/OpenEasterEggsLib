package net.devtech.oeel.v0.api.event;

import java.util.function.Function;

import io.github.astrarre.access.v0.api.FunctionAccess;
import io.github.astrarre.access.v0.fabric.helper.ItemAccessHelper;
import net.devtech.oeel.impl.OEELImpl;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

public interface ItemLangOverrideEvent {
	FunctionAccess<ItemStack, String> EVENT = new FunctionAccess<>(OEELImpl.id2("item_lang_override"));
	ItemAccessHelper<Function<ItemStack, String>> ITEM_FILTER = ItemAccessHelper.create(EVENT, function -> stack -> function.apply(stack).apply(stack), ItemStack::getItem, s -> null);

	Void _STATIC = Util.make(() -> {
		EVENT.addProviderFunction();
		return null;
	});
}
