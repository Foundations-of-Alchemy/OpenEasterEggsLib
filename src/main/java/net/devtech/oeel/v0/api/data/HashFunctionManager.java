package net.devtech.oeel.v0.api.data;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.gson.JsonElement;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.v0.api.OEEL;
import net.devtech.oeel.v0.api.util.BlockData;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class HashFunctionManager<T, E> extends MultiJsonDataLoader<JsonElement> {
	/**
	 * Mainly used for recipes, when a yeet.client joins a single player world, or when a dedicated server starts up, this is server
	 *
	 * otherwise, when in multiplayer or not in a screen, it is yeet.client, the real solution is to synchronize, but i'm too lazy so meh
	 */
	public static Functions active;

	public static final Functions SERVER = new Functions();
	@Environment(EnvType.CLIENT)
	public static final Functions CLIENT = new Functions();

	public static final class Functions {
		public final HashFuncComp<ItemKey, Item> itemComp = new HashFuncComp<>(Registry.ITEM_KEY, ItemKey::getItem, OEEL.ITEM_HASHER);
		public final HashFuncComp<Entity, EntityType<?>> entityComp = new HashFuncComp<>(Registry.ENTITY_TYPE_KEY, Entity::getType, OEEL.ENTITY_HASHER);
		public final HashFuncComp<BlockData, Block> blockComp = new HashFuncComp<>(Registry.BLOCK_KEY, b -> b.getState().getBlock(), OEEL.BLOCK_HASHER);
	}

	static {
		active = CLIENT;
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			active = SERVER;
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			SERVER.itemComp.clear();
			SERVER.blockComp.clear();
			SERVER.entityComp.clear();
			active = CLIENT;
		});
	}

	private final HashFuncComp<T, E> compiler;

	public HashFunctionManager(String dataType, HashFuncComp<T, E> compiler) {
		super(dataType, JsonElement.class);
		this.compiler = compiler;
	}

	@Override
	protected void apply(ListMultimap<Identifier, JsonElement> prepared) {
		this.compiler.clear();
		for(Map.Entry<Identifier, Collection<JsonElement>> entry : prepared.asMap().entrySet()) {
			Identifier identifier = entry.getKey();
			Collection<JsonElement> elements = entry.getValue();
			this.compiler.addTo(identifier, Iterables.transform(elements, JsonElement::getAsJsonObject));
		}
	}
}
