package net.devtech.oeel.v0.api.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public abstract class MultiJsonDataLoader<T> extends SinglePreparationResourceReloader<ListMultimap<Identifier, T>> {
	protected static final Gson GSON = new Gson();
	private final Gson gson;
	private final String dataType;
	private final Class<T> type;

	public MultiJsonDataLoader(String dataType, Class<T> type) {
		this(GSON, dataType, type);
	}

	public MultiJsonDataLoader(Gson gson, String dataType, Class<T> type) {
		this.gson = gson;
		this.dataType = dataType;
		this.type = type;
	}

	@Override
	protected ListMultimap<Identifier, T> prepare(ResourceManager manager, Profiler profiler) {
		ListMultimap<Identifier, T> map = ArrayListMultimap.create();
		for(Identifier resource : manager.findResources(this.dataType, s -> s.endsWith(".json"))) {
			try {
				for(Resource rss : manager.getAllResources(resource)) {
					try(BufferedReader reader = new BufferedReader(new InputStreamReader(rss.getInputStream()))) {
						T element = JsonHelper.deserialize(this.gson, reader, this.type);
						Identifier id = rss.getId();
						String path = id.getPath();
						int index = path.indexOf('/'), endIndex = path.lastIndexOf(".json");
						map.put(new Identifier(id.getNamespace(), path.substring(index+1, endIndex)), element);
					}
				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		return map;
	}

	/**
	 * bridge to help maintain forwards compatibility
	 */
	@Override
	protected void apply(ListMultimap<Identifier, T> prepared, ResourceManager manager, Profiler profiler) {
		this.apply(prepared);
	}

	protected abstract void apply(ListMultimap<Identifier, T> prepared);
}
