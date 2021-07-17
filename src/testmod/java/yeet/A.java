package yeet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.hash.HashCode;
import net.devtech.oeel.impl.client.ObfSpriteAtlas;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import yeet.client.CubeData;
import yeet.client.CubeModel;
import yeet.client.TestModelProvider;

import net.minecraft.client.render.model.ModelLoader;
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
		SpriteIdentifier identifier = new SpriteIdentifier(ObfSpriteAtlas.OBF_SPRITE_ATLAS_ID, new Identifier("deadbeefcafebabe:bebafecaefbeadde"));
		TestModelProvider.add(new CubeModel(ModelLoader.FIRE_0, CubeData.withAll(identifier)), "item/test_item");

		Registry.register(Registry.ITEM, id("test_item"), new Item(new Item.Settings()));
	}

	public static Identifier id(String name) {
		return new Identifier("testmod", name);
	}

	public static void main(String[] args) throws IOException {
		byte[] inputPng = Files.readAllBytes(Path.of("src/testmod/resources/assets/testmod/obf_sprite/test_texture_1.png"));
		HashCode encryptionKey = HashCode.fromLong(0xDEADBEEF_CAFEBABEL);
		byte[] encrytedPng = OEELEncrypting.encrypt(inputPng, encryptionKey);
		Files.write(Path.of("src/testmod/resources/assets/testmod/obf_sprite/test_texture_1.data"), encrytedPng);
		System.out.println(encryptionKey);
	}
}