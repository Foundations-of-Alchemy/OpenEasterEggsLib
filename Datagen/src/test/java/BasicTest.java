import net.devtech.oeel.v0.api.OEELHashing;
import net.devtech.oeel.v0.api.datagen.ItemRecipeBuilder;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Items;

public class BasicTest {
	public static void main(String[] args) {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();

		System.out.println(ItemRecipeBuilder.itemOutput(Items.STONE).item(Items.STICK).item(Items.ITEM_FRAME).shapeless());
		System.out.println(ItemRecipeBuilder.itemOutput(Items.STONE).item(Items.IRON_AXE).item(Items.ITEM_FRAME).shaped(1, 2));
		System.out.println(ItemRecipeBuilder.itemOutput(Items.BUCKET)
				                   .direct("tin_ingot").item(Items.AIR).direct("tin_ingot")
				                   .item(Items.AIR).direct("tin_ingot").item(Items.AIR)
				                   .substCfg("oeel", "standard").shaped(3, 2));
	}
}
