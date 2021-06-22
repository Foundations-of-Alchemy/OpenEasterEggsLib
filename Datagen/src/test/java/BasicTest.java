import net.devtech.oeel.v0.api.datagen.CraftingDatagen;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Items;

public class BasicTest {
	public static void main(String[] args) {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
		System.out.println(CraftingDatagen.builder(Items.STONE).addItem(Items.STICK).addItem(Items.ITEM_FRAME).shapeless());
		System.out.println(CraftingDatagen.builder(Items.STONE).addItem(Items.IRON_AXE).addItem(Items.ITEM_FRAME).shaped(1, 2));
		System.out.println(CraftingDatagen.builder(Items.BUCKET)
				                   .hashSubstitutionConfig("oeel", "standard")
				                   .addDirect("tin_ingot_replacement")
				                   .addItem(Items.AIR)
				                   .addDirect("tin_ingot_replacement")

				                   .addItem(Items.AIR)
				                   .addDirect("tin_ingot_replacement")
				                   .addItem(Items.AIR)
				                   .shaped(3, 2));
	}
}
