import java.nio.file.Path;

import net.devtech.oeel.v0.api.datagen.SpriteAtlasBuilder;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.hash.HashKey;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.texture.SpriteAtlasTexture;

public class BasicTest {
	public static void main(String[] args) {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();

		HashKey key = new HashKey(0xABCEDF0123456789L, 0xFEDBAC1234567890L, 0xAAAAAAAAAAAAAAAAL, 0xFFFFFFFFFFFFFFFFL);
		EncryptionEntry entry = new EncryptionEntry(key, key.toByteArray());
		SpriteAtlasBuilder builder = new SpriteAtlasBuilder()
				.add(entry, BasicTest.class, "/test_texture_1.png", null);
		builder.buildSprites(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Path.of("src/testmod/resources/data"), "default", 4);
	}
}
