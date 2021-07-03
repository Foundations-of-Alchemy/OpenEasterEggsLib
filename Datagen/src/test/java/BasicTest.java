import net.devtech.oeel.v0.api.datagen.LangBuilder;
import net.devtech.oeel.v0.api.datagen.RecipeBuilder;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BasicTest {
	public static void main(String[] args) {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();

		System.out.println(RecipeBuilder.itemOutput(Items.STONE).item(Items.STICK).item(Items.ITEM_FRAME).shapeless());
		System.out.println(RecipeBuilder.itemOutput(Items.STONE).item(Items.IRON_AXE).item(Items.ITEM_FRAME).shaped(1, 2));
		System.out.println(RecipeBuilder.itemOutput(Items.BUCKET)
				                   .direct("tin_ingot").item(Items.AIR).direct("tin_ingot")
				                   .item(Items.AIR).direct("tin_ingot").item(Items.AIR)
				                   .substCfg("oeel", "standard").shaped(3, 2));

		Item dummy = Registry.register(Registry.ITEM, new Identifier("oeel_test:test_item"), new Item(new Item.Settings()));
		System.out.println(new LangBuilder().item(dummy, "Test").build());
	}
}
