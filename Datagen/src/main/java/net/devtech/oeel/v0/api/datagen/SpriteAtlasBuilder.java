package net.devtech.oeel.v0.api.datagen;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELImpl;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.func.UFunc;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import net.devtech.oeel.v0.api.util.hash.Hasher;
import net.devtech.oeel.v0.api.util.hash.SHA256Hasher;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Sprite.Info;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.util.Identifier;

/**
 * This is how you encrypt sprites, the way this works is you create a 'mini atlas' where all your mods textures can go in. The total size has to be
 * computed at build time so the appropriate image area can be allocated in the minecraft atlas. Then, each of your images stores it's position in
 * this mini atlas in it's encrypted data, so when the image with the sprite is found, it is decrypted, uploaded to the minecraft atlas and rendered.
 *
 * This class also handles caching,
 */
public class SpriteAtlasBuilder {
	private static final byte[] UNIQUER = "SpriteAtlasBuilder".getBytes(StandardCharsets.UTF_8);

	private static final Identifier ID = new Identifier("aaa:aaa");
	protected final List<Sprt<?>> images = new ArrayList<>();
	protected final SHA256Hasher spriteCacheHelper = SHA256Hasher.getPooled();

	public SpriteAtlasBuilder() {
		this.spriteCacheHelper.putBytes(UNIQUER);
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, File file, @Nullable JsonObject meta) {
		this.spriteCacheHelper.putLong(file.lastModified());
		this.spriteCacheHelper.putString(file.getPath(), StandardCharsets.UTF_8);
		this.images.add(new Sprt<>(key, file, ImageIO::read, meta));
		return this;
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, Path file, @Nullable JsonObject meta) {
		try {
			this.spriteCacheHelper.putLong(Files.getLastModifiedTime(file).toMillis());
			this.spriteCacheHelper.putString(file.toString(), StandardCharsets.UTF_8);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}

		this.images.add(new Sprt<>(key, file, i -> {
			try(InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
				return ImageIO.read(stream);
			}
		}, meta));
		return this;
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, BufferedImage image, @Nullable JsonObject meta) {
		return this.add(key, image, meta, h -> {
			h.putInt(image.getWidth());
			h.putInt(image.getHeight());
			for(int x = 0; x < image.getWidth(); x++) {
				for(int y = 0; y < image.getHeight(); y++) {
					h.putInt(image.getRGB(x, y));
				}
			}
		});
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, Class<?> cls, String resource, @Nullable JsonObject meta) {
		try {
			return this.add(key, Objects.requireNonNull(cls.getResource(resource), "no resource found for " + resource), meta);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, URL file, @Nullable JsonObject meta) throws IOException {
		URLConnection connection = file.openConnection();
		return this.add(key, connection.getInputStream(), meta, hasher -> {
			hasher.putString(file.toString(), StandardCharsets.UTF_8);
			hasher.putLong(connection.getLastModified());
		});
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, InputStream input, @Nullable JsonObject meta) {
		return this.add(key, input, meta, h -> h.putLong(System.currentTimeMillis()));
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, BufferedImage image, @Nullable JsonObject meta, Consumer<Hasher> hasher) {
		hasher.accept(this.spriteCacheHelper);
		this.images.add(new Sprt<>(key, null, $ -> image, meta));
		return this;
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, InputStream input, @Nullable JsonObject meta, Consumer<Hasher> hasher) {
		hasher.accept(this.spriteCacheHelper);
		this.images.add(new Sprt<>(key, input, in -> {
			try(in) {
				return ImageIO.read(in);
			}
		}, meta));
		return this;
	}

	public SpriteAtlasBuilder add(EncryptionEntry key, ImageInputStream file, @Nullable JsonObject meta, Consumer<Hasher> hasher) {
		hasher.accept(this.spriteCacheHelper);
		this.images.add(new Sprt<>(key, file, ImageIO::read, meta));
		return this;
	}

	HashKey key;
	public HashKey getHash() {
		if(this.key == null) this.key = this.spriteCacheHelper.hashC();
		return this.key;
	}

	/**
	 * To get a sprite in code, the id is at `[atlasId] [modid]:[spriteGroup]/oeel/[validation hash]/[encryption key]`
	 *
	 * @param spriteGroupName your sprite group name
	 * @param modDataDirectory all unified obfuscated data (atm, everything except lang files) goes in the mods data directory
	 * @param mipmapLevels default is 4 in mc I think
	 */
	public List<Path> buildSprites(Identifier atlasId, Path modDataDirectory, String spriteGroupName, int mipmapLevels) {
		Path spriteAtlasPath = modDataDirectory.resolve("obf_atlas").resolve(spriteGroupName + ".json");
		try {
			JsonObject currentAtlasMeta = from(spriteAtlasPath);

			Path obfDataDir = modDataDirectory.resolve("obf_rss");
			AtomicInteger uniqueId = new AtomicInteger();
			List<Path> paths = new ArrayList<>();
			JsonObject replacement = this.buildSprites(e -> {
				SHA256Hasher hasher = SHA256Hasher.getPooled();
				hasher.putIdentifier(atlasId);
				hasher.putString(spriteGroupName, StandardCharsets.UTF_8);
				hasher.putInt(uniqueId.incrementAndGet());
				String str = hasher.hashC().toString();
				Path unique = obfDataDir.resolve(str.substring(0, 16) + ".data");
				paths.add(unique);
				Files.createDirectories(unique.getParent());
				return Files.newOutputStream(unique);
			}, mipmapLevels);
			currentAtlasMeta.add(atlasId.toString(), replacement);
			write(currentAtlasMeta, spriteAtlasPath);
			return paths;
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	/**
	 * @param mipMapLevel default is 4 in mc I think
	 * @return the atlas data, this should be combined into a sigle atlas info json, which is documented somewhere tm
	 */
	public JsonObject buildSprites(UFunc<EncryptionEntry, OutputStream> dataOutput, int mipMapLevel) {
		final int max = Integer.MAX_VALUE;
		TextureStitcher stitcher = new TextureStitcher(max, max, mipMapLevel);
		Map<Info, ESprt> map = new HashMap<>();
		for(Sprt<?> sprite : this.images) {
			this.build(map, stitcher, sprite);
		}
		stitcher.stitch();
		stitcher.getStitchedSprites((info, atlasWidth, atlasHeight, x, y) -> {
			try {
				ESprt sprt = map.get(info);
				try(SpriteEncrypter encrypter = new SpriteEncrypter(sprt.entry.entryKey(), dataOutput.apply(sprt.entry))) {
					encrypter.startEncryptedData(sprt.entry.encryptionKey());
					encrypter.writeMagic("oeel:tex");
					encrypter.write(sprt.image, x, y, sprt.meta);
				}
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}
		});
		JsonObject object = new JsonObject();
		object.addProperty("atlasWidth", stitcher.getWidth());
		object.addProperty("atlasHeight", stitcher.getHeight());
		return object;
	}

	private static void delet(Path f) {
		try {
			Files.delete(f);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	protected <T> void build(Map<Info, ESprt> map, TextureStitcher stitcher, Sprt<T> sprite) {
		BufferedImage image = sprite.image.apply(sprite.val);
		Info info = new Info(ID, image.getWidth(), image.getHeight(), AnimationResourceMetadata.EMPTY);
		stitcher.add(info);
		map.put(info, new ESprt(sprite.key, image, sprite.meta));
	}

	record Sprt<T>(EncryptionEntry key, T val, UFunc<T, BufferedImage> image, JsonObject meta) {}

	record ESprt(EncryptionEntry entry, BufferedImage image, JsonObject meta) {}

	public static JsonObject from(Path path) {
		try(BufferedReader reader = Files.newBufferedReader(path)) {
			return OEELImpl.GSON.fromJson(reader, JsonObject.class);
		} catch(IOException e) {
			return new JsonObject();
		}
	}

	public static void write(JsonObject object, Path path) throws IOException {
		Files.createDirectories(path.getParent());
		try(BufferedWriter writer = Files.newBufferedWriter(path)) {
			OEELImpl.GSON.toJson(object, writer);
		}
	}
}
