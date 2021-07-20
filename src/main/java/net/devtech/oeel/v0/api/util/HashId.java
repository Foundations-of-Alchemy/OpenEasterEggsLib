package net.devtech.oeel.v0.api.util;

import com.google.common.hash.HashCode;

import net.minecraft.util.Identifier;

public class HashId extends Identifier {
	public final HashCode a, b;
	protected HashId(String[] id, HashCode a, HashCode b) {
		super(id);
		this.a = a;
		this.b = b;
	}

	public static HashId create(Identifier identifier, HashCode validation, HashCode encryption) {
		byte[] keyBytes = validation.asBytes(), encryptionBytes = encryption.asBytes();
		String value = identifier.getPath() + "/oeel/" + OEELEncrypting.encodeBase16(keyBytes) + "." + OEELEncrypting.encodeBase16(encryptionBytes);
		return new HashId(new String[] {identifier.getNamespace(), value}, validation, encryption);
	}

	public static HashId getKey(Identifier id) {
		if(id instanceof HashId h) {
			return h;
		} else {
			String path = id.getPath();
			int index = path.indexOf("/oeel/") + 6;
			if(index == 5) return null;
			int seperator = path.lastIndexOf('.');
			if(index > seperator) return null;
			HashCode validation = HashCode.fromString(path.substring(index, seperator));
			HashCode encryption = HashCode.fromString(path.substring(seperator + 1));
			return new HashId(new String[] {id.getNamespace(), id.getPath()}, validation, encryption);
		}
	}

	public static void main(String[] args) {
		HashId test = create(new Identifier("bru", "a"), HashCode.fromLong(384249), HashCode.fromLong(304390409));
		Identifier yeet = new Identifier(test.namespace, test.path);
		getKey(yeet);
	}
}
