package yeet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.hash.HashCode;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.HashId;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import yeet.client.CubeData;
import yeet.client.CubeModel;
import yeet.client.TestModelProvider;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public class A implements ModInitializer {
	public static final List<Direction> DIRECTIONS = List.of(Direction.values());

	@Override
	public void onInitialize() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(r -> new TestModelProvider());
		HashKey key = new HashKey(0xABCEDF0123456789L, 0xFEDBAC1234567890L, 0xAAAAAAAAAAAAAAAAL, 0xFFFFFFFFFFFFFFFFL);
		EncryptionEntry entry = new EncryptionEntry(key, key.toByteArray());
		var spriteId = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, HashId.create(new Identifier("oeelt:default"), entry));
		TestModelProvider.add(new CubeModel(spriteId, CubeData.withAll(spriteId)), "item/test_item");

		Registry.register(Registry.ITEM, id("test_item"), new Item(new Item.Settings()));
	}

	public static Identifier id(String name) {
		return new Identifier("testmod", name);
	}

	public static void main(String[] args) {
	}
}
