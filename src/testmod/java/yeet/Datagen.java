package yeet;

import java.io.IOException;
import java.nio.file.Path;

import net.devtech.oeel.v0.api.datagen.RecipeBuild;
import net.devtech.oeel.v0.api.datagen.SpriteAtlasBuilder;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import techreborn.init.TRContent;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class Datagen {
	public void onInitialize() throws IOException {
		Path dir = Path.of("").toAbsolutePath().getParent().resolve("src/testmod/resources/data/oeelt");

		HashKey key = new HashKey(-1L, -1L, -1L, -1L);
		EncryptionEntry entry = new EncryptionEntry(key, key.toByteArray());
		SpriteAtlasBuilder builder = new SpriteAtlasBuilder()
				                             .add(entry, Datagen.class, "/test_texture_1.png", null);
		// todo remember to document that u need a assets directory even if u don't use it
		builder.buildSprites(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, dir, "default", 4);

		Item tin = TRContent.Ingots.TIN.item;
		Path rss = dir.resolve("obf_rss");
		HashKey k = new RecipeBuild()
				.item(tin).item(Items.IRON_INGOT)
				.empty()  .item(Items.SMITHING_TABLE)
				.setHash(new Identifier("oeel:standard"))
				.shaped(2)
				.writeInDir(rss, RecipeBuild.of(new ItemStack(Items.IRON_AXE)));
		System.out.println(k);
	}
}
