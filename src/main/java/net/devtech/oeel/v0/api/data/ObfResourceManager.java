package net.devtech.oeel.v0.api.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.access.ByteDeserializer;
import net.devtech.oeel.v0.api.util.IdentifierPacker;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.func.UFunc;
import net.devtech.oeel.v0.api.util.func.UPred;
import net.devtech.oeel.v0.api.util.hash.HashKey;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ObfResourceManager extends SinglePreparationResourceReloader<Multimap<HashKey, byte[]>> {
	@Environment(EnvType.CLIENT) public static ObfResourceManager client;

	private Multimap<HashKey, byte[]> encryptedData;

	public ObfResourceManager() {
	}

	/**
	 * Does not cache the output, removes the data in the storage if found
	 */
	public byte[] decryptOnce(HashKey validationKey, byte[] decryption, Predicate<byte[]> dataPredicate) throws GeneralSecurityException {
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

	public <T> Iterable<T> decryptOnce(HashKey validationKey, byte[] key, ByteDeserializer<T> deserializer) {
		long magic = IdentifierPacker.pack(deserializer.magic());
		return () -> this.encryptedData.get(validationKey)
				             .stream()
				             .map(b -> OEELEncrypting.decryptStream(b, key)) // decrypt
				             .filter(UPred.of(s -> s.readLong() == magic)) // validate magic
				             .map(UFunc.of(i -> {
					             T value = deserializer.newInstance();
					             deserializer.read(value, i, validationKey);
					             return value;
				             }))
				             .iterator();
	}

	@Override
	protected Multimap<HashKey, byte[]> prepare(ResourceManager manager, Profiler profiler) {
		Multimap<HashKey, byte[]> encryptedData = HashMultimap.create();
		for(Identifier resourceId : manager.findResources("obf_rss/", s -> s.endsWith(".data"))) {
			try {
				try(Resource resource = manager.getResource(resourceId)) {
					if(resource != null) {
						InputStream stream = resource.getInputStream();
						encryptedData.put(new HashKey(stream), stream.readAllBytes());
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
	protected void apply(Multimap<HashKey, byte[]> prepared, ResourceManager manager, Profiler profiler) {
		this.encryptedData = prepared;
	}
}
