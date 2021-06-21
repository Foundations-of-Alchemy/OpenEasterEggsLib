package net.devtech.oeel.impl.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public abstract class MultiJsonDataLoader extends SinglePreparationResourceReloader<Multimap<Identifier, JsonElement>> {
	private static final Gson GSON = new Gson();
	private final String dataType;
	private final Gson gson;

	public MultiJsonDataLoader(String dataType) {
		this(GSON, dataType);
	}

	protected MultiJsonDataLoader(Gson gson, String type) {
		this.dataType = type;
		this.gson = gson;
	}

	@Override
	protected Multimap<Identifier, JsonElement> prepare(ResourceManager manager, Profiler profiler) {
		Multimap<Identifier, JsonElement> map = HashMultimap.create();
		for(Identifier resource : manager.findResources(this.dataType, s -> s.endsWith(".json"))) {
			try {
				for(Resource rss : manager.getAllResources(resource)) {
					try(BufferedReader reader = new BufferedReader(new InputStreamReader(rss.getInputStream()))) {
						JsonElement element = JsonHelper.deserialize(this.gson, reader, JsonElement.class);
						map.put(rss.getId(), element);
					}
				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		return map;
	}
}
