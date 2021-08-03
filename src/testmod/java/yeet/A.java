package yeet;

import java.io.IOException;
import java.util.List;

import io.github.astrarre.util.v0.api.Validate;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class A implements ModInitializer {

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			if(Boolean.getBoolean("oeel.datagen")) {
				try {
					new Datagen().onInitialize();
					System.exit(0);
					System.exit(0);
					System.exit(0);
				} catch(IOException e) {
					throw Validate.rethrow(e);
				}
			}
		});


		Registry.register(Registry.ITEM, id("test_item"), new Item(new Item.Settings()));
	}

	public static Identifier id(String name) {
		return new Identifier("testmod", name);
	}

	public static void main(String[] args) {
	}
}
