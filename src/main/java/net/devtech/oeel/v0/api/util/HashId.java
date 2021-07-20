package net.devtech.oeel.v0.api.util;

import net.devtech.oeel.v0.api.util.hash.HashKey;

import net.minecraft.util.Identifier;

public class HashId extends Identifier {
	public final HashKey validation;
	public final byte[] encryption;
	protected HashId(String[] id, HashKey validation, byte[] encryption) {
		super(id);
		this.validation = validation;
		this.encryption = encryption;
	}

	public static HashId create(Identifier identifier, HashKey validation, byte[] encryption) {
		String value = identifier.getPath() + "/oeel/" + validation + "." + OEELEncrypting.encodeBase16(encryption);
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
			HashKey validation = new HashKey(path, index);
			byte[] encryption = OEELEncrypting.decodeBase16(path, seperator + 1, path.length());
			return new HashId(new String[] {id.getNamespace(), id.getPath()}, validation, encryption);
		}
	}
}
