package net.devtech.oeel.v0.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.func.UFunc;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import net.devtech.sandbox.impl.SandboxImpl;
import net.devtech.sandbox.v0.api.Sandbox;
import org.jetbrains.annotations.ApiStatus;
import oshi.util.tuples.Pair;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * @deprecated this is almost guaranteed to break in the future
 */
@Deprecated
@ApiStatus.Experimental
public final class OEELSandbox {
	public static final OEELSandbox INSTANCE = new OEELSandbox();
	protected final Sandbox sandbox = new SandboxImpl();

	final Map<HashKey, Class<?>> loadedClasses = new HashMap<>();
	final Map<HashKey, byte[]> data = new HashMap<>();
	final Set<HashKey> loading = new HashSet<>();

	private OEELSandbox() {
		for(ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			Path root = mod.getPath("sandboxed");
			try {
				this.populate(root);
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}
		}
	}

	public Class<?> getFor(EncryptionEntry entry) {
		return this.loadedClasses.computeIfAbsent(entry.entryKey(), UFunc.of(k -> {
			byte[] encryptedClass = this.data.get(k);
			if(encryptedClass == null) {
				return null;
			}
			byte[] decryptedClass = OEELEncrypting.decrypt(entry.encryptionKey(), encryptedClass);

			ByteBuffer buffer = ByteBuffer.wrap(decryptedClass);
			int dependencies = buffer.getInt();

			try {
				this.loading.add(k);
				for(int i = 0; i < dependencies; i++) {
					EncryptionEntry dep = new EncryptionEntry(buffer);
					if(!this.loading.contains(dep.entryKey())) {
						this.getFor(dep);
					}
				}
			} finally {
				this.loading.remove(k);
			}


			return this.sandbox.defineValidatedClass(decryptedClass, buffer.arrayOffset(), buffer.remaining());
		}));
	}

	static <A, B> Pair<A, B> pair(A a, B b) {
		return new Pair<>(a, b);
	}

	private void populate(Path root) throws IOException {
		Files.walk(root)
				.filter(Files::isRegularFile)
				.map(UFunc.of(Files::newInputStream))
				.map(UFunc.of(BufferedInputStream::new))
				.map(UFunc.of(i -> pair(new HashKey(i), i.readAllBytes())))
				.forEach(p -> this.data.put(p.getA(), p.getB()));
	}
}
