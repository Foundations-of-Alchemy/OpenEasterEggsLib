package net.devtech.oeel.v0.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.hash.HashCode;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.util.OEELEncrypting;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class ObfResourceManager extends SinglePreparationResourceReloader<Map<Identifier, byte[]>> {
	private static final List<Consumer<ObfResourceManager>> ON_RELOAD = new ArrayList<>();
	public static ObfResourceManager active;
	private Map<Identifier, byte[]> encryptedData;

	public ObfResourceManager() {
		active = this;
		for(Consumer<ObfResourceManager> consumer : ON_RELOAD) {
			consumer.accept(this);
		}
	}

	public static void registerOnReplaced(Consumer<ObfResourceManager> consumer) {
		ON_RELOAD.add(consumer);
	}

	/**
	 * Does not cache the output, removes the data in the storage if found
	 */
	public byte[] decryptOnce(Identifier key, HashCode decryption) throws GeneralSecurityException, IOException {
		byte[] data = this.encryptedData.remove(key);
		if(data == null) {
			return null;
		} else {
			return OEELEncrypting.decrypt(decryption, data);
		}
	}

	@Override
	protected Map<Identifier, byte[]> prepare(ResourceManager manager, Profiler profiler) {
		Map<Identifier, byte[]> encryptedData = new HashMap<>();
		for(Identifier resourceId : manager.findResources("obf_rss/", s -> s.endsWith(".data"))) {
			try {
				try(Resource resource = manager.getResource(resourceId)) {
					if(resource != null) {
						Identifier id = new Identifier(resourceId.getNamespace(), resourceId.getPath().substring(8));
						encryptedData.put(id, resource.getInputStream().readAllBytes());
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
	protected void apply(Map<Identifier, byte[]> prepared, ResourceManager manager, Profiler profiler) {
		this.encryptedData = prepared;
	}
}
