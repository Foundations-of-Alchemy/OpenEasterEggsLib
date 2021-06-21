package net.devtech.oeel.impl.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Id;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.access.ItemHashSubstitution;

import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class ItemHashSubstitutionManager extends MultiJsonDataLoader {
	private static final Map<Identifier, ItemHashSubstitution> HASH_FUNCTIONS = new HashMap<>();
	private static final Gson GSON = new Gson();

	public ItemHashSubstitutionManager() {
		super(GSON, "item_subst");
	}

	public static ItemHashSubstitution forId(Identifier id) {
		return HASH_FUNCTIONS.computeIfAbsent(id, ItemHasherRef::new);
	}

	@Override
	protected void apply(Multimap<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
		HASH_FUNCTIONS.clear();
		for(Map.Entry<Identifier, Collection<JsonElement>> entry : prepared.asMap().entrySet()) {
			Identifier identifier = entry.getKey();
			Collection<JsonElement> elements = entry.getValue();

			ArrayList<ItemHashSubstitution> hashers = new ArrayList<>();
			for(JsonElement element : elements) {
				JsonObject o = element.getAsJsonObject();
				if(o.getAsJsonPrimitive("replace").getAsBoolean()) {
					hashers.clear();
				}
				hashers.ensureCapacity(hashers.size() + o.size());
				this.compile(o.getAsJsonObject("replacements"), hashers);
			}

			ItemHashSubstitution sub = ItemHashSubstitution.COMBINE.combine(hashers);
			if(HASH_FUNCTIONS.put(identifier, sub) instanceof ItemHasherRef i) {
				i.hasher = sub;
			}
		}
	}

	protected void compile(JsonObject element, List<ItemHashSubstitution> hashers) {
		for(Map.Entry<String, JsonElement> entry : element.entrySet()) { // this is actually ordered
			String key = entry.getKey();
			String replacement = entry.getValue().getAsString();
			if(key.startsWith("#")) {
				TagManager manager = ServerTagManagerHolder.getTagManager();
				Tag<Item> tag = manager.getTag(Registry.ITEM_KEY, Id.create(key.substring(1)).to(), (i) -> new JsonSyntaxException("Unknown item tag '" + i + "'"));
				hashers.add((i, k) -> {
					if(tag.contains(k.getItem())) {
						return replacement;
					}
					return i;
				});
			} else if(key.startsWith("$")) {
				hashers.add(forId(Id.create(key.substring(1)).to()));
			} else {
				int i = key.indexOf(':');
				if(i != -1) {
					Item item = Registry.ITEM.get(new Identifier(key.substring(0, i), key.substring(i + 1)));
					hashers.add((incoming, val) -> {
						if(val.getItem() == item) {
							return replacement;
						}
						return incoming;
					});
				} else {
					hashers.add((incoming, val) -> {
						if(incoming.equals(key)) {
							return replacement;
						}
						return incoming;
					});
				}
			}
		}
	}

	private static class ItemHasherRef implements ItemHashSubstitution {
		private final Identifier id;
		private ItemHashSubstitution hasher;

		public ItemHasherRef(Identifier id) {
			this.id = id;
		}

		@Override
		public String substitute(String incoming, ItemKey val) {
			ItemHashSubstitution hash = this.hasher;
			if(hash == null) {
				this.hasher = hash = Validate.notNull(HASH_FUNCTIONS.get(this.id), "no item hash substitute found for id " + this.id);
			}
			return hash.substitute(incoming, val);
		}
	}
}
