package net.devtech.oeel.v0.api.data;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.hash.HashCode;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.access.ByteDeserializer;
import net.devtech.oeel.v0.api.util.IdentifierPacker;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.OEELHashing;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ObfResourceManager extends SinglePreparationResourceReloader<Multimap<HashCode, byte[]>> {
	@Environment(EnvType.CLIENT)
	public static ObfResourceManager client;

	private Multimap<HashCode, byte[]> encryptedData;

	public ObfResourceManager() {
	}

	/**
	 * Does not cache the output, removes the data in the storage if found
	 */
	public byte[] decryptOnce(HashCode validationKey, Predicate<byte[]> dataPredicate, HashCode decryption)
			throws GeneralSecurityException, IOException {
		Collection<byte[]> data = this.encryptedData.get(validationKey);
		if(!data.isEmpty()) {
			Iterator<byte[]> iterator = data.iterator();
			while(iterator.hasNext()) {
				byte[] datum = iterator.next();
				if(dataPredicate.test(datum)) {
					iterator.remove();
					return OEELEncrypting.decrypt(decryption, datum);
				}
			}
		}
		return null;
	}

	public <T> Iterable<T> decryptOnce(HashCode validationKey, ByteDeserializer<T> deserializer) {
		long magic = IdentifierPacker.pack(deserializer.magic());
		return transform(filter(transform(this.encryptedData.get(validationKey), ByteBuffer::wrap), buf -> buf.getLong() == magic), input -> {
			T value = deserializer.newInstance();
			deserializer.read(value, input, validationKey);
			return value;
		});
	}

	@Override
	protected Multimap<HashCode, byte[]> prepare(ResourceManager manager, Profiler profiler) {
		Multimap<HashCode, byte[]> encryptedData = HashMultimap.create();
		for(Identifier resourceId : manager.findResources("obf_rss/", s -> s.endsWith(".data"))) {
			try {
				try(Resource resource = manager.getResource(resourceId)) {
					if(resource != null) {
						InputStream stream = resource.getInputStream();
						byte[] hash = new byte[OEELHashing.BITS / 8];
						Validate.isTrue(stream.read(hash) == hash.length, resourceId + " didn't contain " + OEELHashing.BITS + "-bit hash header!");
						encryptedData.put(HashCode.fromBytes(hash), stream.readAllBytes());
					} else {
						throw new FileNotFoundException(resourceId + "");
					}
				}
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}

		}
		return encryptedData;
	}

	@Override
	protected void apply(Multimap<HashCode, byte[]> prepared, ResourceManager manager, Profiler profiler) {
		this.encryptedData = prepared;
	}
}
