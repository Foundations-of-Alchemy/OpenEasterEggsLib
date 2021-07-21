package yeet;

import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.HashId;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import yeet.client.CubeData;
import yeet.client.CubeModel;
import yeet.client.TestModelProvider;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public class AClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(r -> new TestModelProvider());
		HashKey key = new HashKey(0xABCEDF0123456789L, 0xFEDBAC1234567890L, 0xAAAAAAAAAAAAAAAAL, 0xFFFFFFFFFFFFFFFFL);
		EncryptionEntry entry = new EncryptionEntry(key, key.toByteArray());
		var spriteId = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, HashId.create(new Identifier("oeelt:default"), entry));
		TestModelProvider.add(new CubeModel(spriteId, CubeData.withAll(spriteId)), "item/test_item");
	}
}
