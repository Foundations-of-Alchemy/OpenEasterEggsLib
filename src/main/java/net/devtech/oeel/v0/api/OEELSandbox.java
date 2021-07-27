package net.devtech.oeel.v0.api;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	private static final Logger LOGGER = LogManager.getLogger(OEELSandbox.class);
	protected final Sandbox sandbox = new SandboxImpl();

	final Map<HashKey, Class<?>> loadedClasses = new HashMap<>();
	final Map<HashKey, byte[]> data = new HashMap<>();
	final Set<HashKey> loading = new HashSet<>();

	private OEELSandbox() {
		Set<String> allExplicitAllowed = new HashSet<>();
		for(ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			try {
				Path root = mod.getPath("sandboxed");
				this.populate(root);
				for(String line : Files.readAllLines(mod.getPath("requires.txt"))) {
					if(line.isBlank() || line.charAt(0) == '#') continue;
					this.sandbox.allow(line);
					if(allExplicitAllowed.add(line)) {
						LOGGER.info("Whitelisting " + line);
					}
				}
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

			DataInputStream buffer = new DataInputStream(new ByteArrayInputStream(decryptedClass));
			int dependencies = buffer.readInt();

			try {
				this.loading.add(k);
				for(int i = 0; i < dependencies; i++) {
					String internalName = buffer.readUTF();
					this.sandbox.allow(internalName);
					EncryptionEntry dep = new EncryptionEntry(buffer);
					if(!this.loading.contains(dep.entryKey())) {
						this.getFor(dep);
					}
				}
			} finally {
				this.loading.remove(k);
			}

			byte[] remaining = buffer.readAllBytes();
			return this.sandbox.defineValidatedClass(remaining, 0, remaining.length);
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
