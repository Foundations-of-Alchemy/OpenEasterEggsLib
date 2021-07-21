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

@SuppressWarnings("unchecked")
public class HashFunctionManager<T, E> extends MultiJsonDataLoader<JsonElement> {
	public static final HashFuncComp<ItemKey, Item> ITEM_COMP = new HashFuncComp<>(Registry.ITEM_KEY, ItemKey::getItem, OEEL.ITEM_HASHER);
	public static final HashFuncComp<Entity, EntityType<?>> ENTITY_COMP = new HashFuncComp<>(Registry.ENTITY_TYPE_KEY, Entity::getType, OEEL.ENTITY_HASHER);
	public static final HashFuncComp<BlockData, Block> BLOCK_COMP = new HashFuncComp<>(Registry.BLOCK_KEY, b -> b.getState().getBlock(), OEEL.BLOCK_HASHER);

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
