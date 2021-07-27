package net.devtech.oeel.v0.api.datagen;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.devtech.oeel.impl.OEELImpl;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.func.UFunc;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class ClassEncrypter {
	Map<String, EncryptionEntry> classpath = new HashMap<>();
	Map<String, PreEncrypted> working = new HashMap<>();

	public void add(EncryptionEntry entry, byte[] data) {
		ClassReader reader = new ClassReader(data);
		Set<String> dependencies = new HashSet<>();
		Remapper remapper = new Remapper() {
			@Override
			public String map(String internalName) {
				dependencies.add(internalName);
				return super.map(internalName);
			}
		};
		reader.accept(new ClassRemapper(null, remapper), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		String name = reader.getClassName();
		dependencies.remove(name);
		this.working.put(name, new PreEncrypted(name, entry, data, dependencies));
		this.classpath.put(name, entry);
	}

	public void add(EncryptionEntry entry, Path path) throws IOException {
		this.add(entry, Files.readAllBytes(path));
	}

	public void add(EncryptionEntry entry, File file) throws IOException {
		this.add(entry, file.toPath());
	}

	public void addClasspath(String internalName, EncryptionEntry entry) {
		this.classpath.put(internalName, entry);
	}

	public void emit(Path directory) throws IOException {
		Files.walk(directory)
				.sorted(Comparator.reverseOrder())
				.map(UFunc.of(Files::deleteIfExists))
				.forEach(b -> {if(!b) throw new IllegalStateException("unable to delete a file!");});
		SecureRandom random = new SecureRandom();
		this.emit(s -> {
			Path resolved = directory.resolve(s);
			Path parent = resolved.getParent();
			Files.createDirectories(parent);
			while(Files.exists(resolved)) { // occasional conflicts cus we trim the hash key
				System.out.println("You are a lucky man!");
				resolved = parent.resolve(path(new HashKey(random)));
			}
			return Files.newOutputStream(resolved);
		});
	}

	public void emit(UFunc<String, OutputStream> output) throws IOException {
		for(PreEncrypted key : this.working.values()) {
			if(key.bytecode == null) {
				continue;
			}

			OutputStream stream = output.apply(path(key.entry.entryKey()));
			key.entry.entryKey().write(stream);

			key.dependencies.retainAll(this.classpath.keySet());

			int dependencies = key.dependencies.size();
			OutputStream encrypted = OEELImpl.encryptStream(key.entry.encryptionKey(), stream);
			try(DataOutputStream daos = new DataOutputStream(encrypted)) {
				daos.writeInt(dependencies);
				for(String dependency : key.dependencies) {
					EncryptionEntry dep = this.classpath.get(dependency);
					dep.write(daos);
				}
				daos.write(key.bytecode);
			}
		}
	}

	static String path(HashKey key) {
		return key.toString().substring(0, 16) + ".data";
	}

	record PreEncrypted(String internalName, EncryptionEntry entry, byte[] bytecode, Set<String> dependencies) {}
}
