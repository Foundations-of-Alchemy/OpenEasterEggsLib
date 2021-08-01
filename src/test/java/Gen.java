import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.devtech.oeel.v0.api.util.hash.SHA256Hasher;

public class Gen {
	public static void main(String[] args) throws IOException {
		Tags tags;
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		try(Reader reader = new InputStreamReader(Gen.class.getResourceAsStream("/tags.json"))) {
			tags = gson.fromJson(reader, Tags.class);
		}

		System.err.println("====================================== items ======================================");
		try(Writer writer = new FileWriter("src/main/resources/data/oeel/ihshr/standard.json")) {
			gson.toJson(create(tags.items,
			                   Set.of("veggies",
			                          "dye_gray",
			                          "dye_cyan",
			                          "dye_blue",
			                          "dye_white",
			                          "dye_magenta",
			                          "dye_light_blue",
			                          "dye_lime",
			                          "dye_red",
			                          "dye_purple",
			                          "dye_green",
			                          "dye_orange",
			                          "dye_black",
			                          "dye_yellow",
			                          "dye_pink",
			                          "dye_brown",
			                          "dye_light_gray",
			                          "gems",
			                          "wood_sticks",
			                          "dyes",
			                          "glass",
			                          "tea_ingredients/bitter/normal",
			                          "grain",
			                          "tea_ingredients/sweet/normal",
			                          "tea_ingredients/salty/weak",
			                          "ores",
			                          "tea_ingredients/shining/weak",
			                          "chest",
			                          "tea_ingredients/sour/normal",
			                          "tea_ingredients/gloopy/weak",
			                          "lapis_lazulis",
			                          "tea_ingredients/umami/weak",
			                          "tea_ingredients/sweet/strong",
			                          "cobblestones",
			                          "tea_ingredients/sweet/weak")), writer);
		}

		System.err.println("====================================== blocks ======================================");
		try(Writer writer = new FileWriter("src/main/resources/data/oeel/bhshr/standard.json")) {
			gson.toJson(create(tags.blocks, Set.of(
					"strip_command",
					"netherracks",
					"glass",
					"natural_stones",
					"yellow_sandstones"
			)), writer);
		}
	}

	public static JsonObject create(Tag[] arr, Set<String> ignore) {
		JsonObject json = new JsonObject();
		JsonObject functions = new JsonObject();
		json.add("functions", functions);
		functions.add("&oeel:default", JsonNull.INSTANCE);

		Multimap<String, TagData> minecrafts = HashMultimap.create();
		for(Tag item : arr) {
			if(ignore.contains(item.id)) {
				continue;
			}
			String str;
			try(SHA256Hasher hasher = SHA256Hasher.getPooled()) {
				hasher.putString(item.id, StandardCharsets.UTF_8);
				str = hasher.hashC().toString64();
			}

			Set<String> mods = new HashSet<>();
			for(Value value : item.content) {
				mods.addAll(Arrays.asList(value.sources));
				JsonElement val = value.value;
				String id;
				if(val.isJsonPrimitive()) {
					id = val.getAsString();
				} else {
					id = val.getAsJsonObject().getAsJsonPrimitive("id").toString();
				}
				if(id.startsWith("minecraft:")) {
					minecrafts.put(id, new TagData(mods, item.id));
					functions.addProperty(id, str);
				}
			}

			// dead mods or mods idc about
			mods.remove("cotton-resources");
			mods.removeIf(s -> s.startsWith("astromine"));
			mods.remove("flonters");
			mods.remove("ce_foodstuffs");

			functions.addProperty("#c:" + item.id, str);
		}

		minecrafts.asMap().forEach((s, strings) -> {
			if(strings.size() > 1) {
				TagData largest = new TagData(Collections.emptySet(), "");
				for(TagData i : strings) {
					if(i.mods.size() > largest.mods.size()) {
						largest = i;
					}
				}
				var strs = new ArrayList<>(strings);
				strs.remove(largest);
				System.err.printf("conflict %s in %s (recommend ignore: %s)\n", s, strings, strs);
			}
		});
		return json;
	}

	record TagData(Set<String> mods, String id) {
		@Override
		public String toString() {
			return this.id;
		}
	}

	static class Value {
		String[] sources;
		JsonElement value;
	}

	static class Tag {
		Value[] content;
		String id;
	}

	static class Tags {
		Tag[] blocks;
		Tag[] items;
		// Tag[] fluids;
	}
}
