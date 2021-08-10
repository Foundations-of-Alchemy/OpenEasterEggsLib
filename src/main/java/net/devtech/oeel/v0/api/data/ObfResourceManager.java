package net.devtech.oeel.v0.api.data;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.access.OEELDeserializer;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.func.Iter;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ObfResourceManager extends SinglePreparationResourceReloader<Multimap<HashKey, byte[]>> {
	private static final Logger LOGGER = LogManager.getLogger(ObfResourceManager.class);
	@Environment(EnvType.CLIENT) public static ObfResourceManager client;
	private Multimap<HashKey, byte[]> encryptedData;

	public ObfResourceManager() {
	}

	public <T> Iterable<T> decryptOnce(EncryptionEntry entry, OEELDeserializer<T> deserializer) {
		return this.decryptOnce(entry.entryKey(), entry.encryptionKey(), deserializer);
	}

	public <T> Iterable<T> decryptOnce(HashKey validationKey, byte[] key, OEELDeserializer<T> deserializer) {
		List<T> list = new ArrayList<>();
		var iterator = this.encryptedData.get(validationKey).iterator();
		while(iterator.hasNext()) {
			this.extracted(validationKey, key, deserializer, list, iterator);
		}
		return list;
	}

	public static Map<Identifier, InputStream> findResources(ResourceType type,
			ResourceManager manager,
			String startingPath,
			Predicate<String> pathPredicate) {
		Map<Identifier, InputStream> map = new HashMap<>();
		for(ResourcePack pack : Iter.iter(manager::streamResourcePacks)) {
			for(String namespace : pack.getNamespaces(type)) {
				for(Identifier resource : pack.findResources(type, namespace, startingPath, Integer.MAX_VALUE, pathPredicate)) {
					try {
						map.put(resource, pack.open(type, resource));
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return map;
	}

	private <T> void extracted(HashKey validationKey, byte[] key, OEELDeserializer<T> deserializer, List<T> list, Iterator<byte[]> iterator) {
		try {
			byte[] data = iterator.next();
			DataInputStream decrypted = OEELEncrypting.decryptStream(key, new ByteArrayInputStream(data));
			T result = deserializer.read(decrypted, validationKey);
			if(result != null) {
				iterator.remove();
				list.add(result);
			}
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	@Override
	protected Multimap<HashKey, byte[]> prepare(ResourceManager manager, Profiler profiler) {
		Multimap<HashKey, byte[]> encryptedData = HashMultimap.create();
		for(InputStream resource : findResources(ResourceType.SERVER_DATA, manager, "obf_rss/", s -> s.endsWith(".data")).values()) {
			try {
				encryptedData.put(new HashKey(resource), resource.readAllBytes());
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}

		}
		return encryptedData;
	}

	@Override
	protected void apply(Multimap<HashKey, byte[]> prepared, ResourceManager manager, Profiler profiler) {
		this.encryptedData = prepared;
		LOGGER.warn("Found " + prepared.size() + " encrypted resources");
	}
}
