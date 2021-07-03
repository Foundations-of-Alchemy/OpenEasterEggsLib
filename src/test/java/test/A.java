package test;

import net.devtech.oeel.v0.api.util.BiHasher;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;

public class A implements ModInitializer {
	private static final Item ITEM = new Item(new Item.Settings()) {
		@Override
		public String getTranslationKey() {
			return this.getTranslationKey(new ItemStack(this));
		}

		@Override
		public String getTranslationKey(ItemStack stack) {
			BiHasher hasher = BiHasher.createDefault(false);
			hasher.putItem(stack);
			return "lang.hash." + hasher.hashA() + "." + hasher.hashB();
		}
	};

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("oeel_test:test_item"), ITEM);
	}
}
