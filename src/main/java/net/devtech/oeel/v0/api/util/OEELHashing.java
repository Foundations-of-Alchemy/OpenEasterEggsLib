package net.devtech.oeel.v0.api.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtNull;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;

public class OEELHashing {
	public static final HashFunction FUNCTION = Hashing.sha256();
	public static final String ALGORITHM = "AES";
	/**
	 * if true, unrecognized NbtElement subclasses can be hashed via a default implementation
	 */
	public static boolean COMPAT_MODE = Boolean.getBoolean("compat_mode");

	public static void hash(Hasher hasher, BlockState state) {
		hash(hasher, Registry.BLOCK, state.getBlock());
		state.getProperties()
				.stream()
				.sorted(Comparator.comparing(Property::getName))
				.forEachOrdered(property -> hashProperty(hasher, state, property));
	}

	public static <T> void hash(Hasher hasher, Registry<T> registry, T item) {
		hasher.putString(registry.getId(item).toString(), StandardCharsets.US_ASCII);
	}

	public static <T extends Comparable<T>> void hashProperty(Hasher hasher, BlockState state, Property<T> property) {
		T val = state.get(property);
		hasher.putString(property.getName(), StandardCharsets.UTF_8);
		hasher.putString(property.name(val), StandardCharsets.UTF_8);
	}

	public static HashCode hashWithCount(ItemStack key) {
		Hasher hasher = FUNCTION.newHasher();
		hash(hasher, Registry.ITEM, key.getItem());
		hash(hasher, key.getTag());
		hasher.putInt(key.getCount());
		return hasher.hash();
	}

	public static HashCode hash(ItemKey key) {
		Hasher hasher = FUNCTION.newHasher();
		hash(hasher, Registry.ITEM, key.getItem());
		hash(hasher, key.getTag().toTag());
		return hasher.hash();
	}

	public static HashCode hash(ItemStack key) {
		Hasher hasher = FUNCTION.newHasher();
		hash(hasher, Registry.ITEM, key.getItem());
		hash(hasher, key.getTag());
		return hasher.hash();
	}

	/**
	 *
	 */
	public static HashCode hashExact(Item item) {
		Hasher hasher = FUNCTION.newHasher();
		hash(hasher, Registry.ITEM, item);
		hash(hasher, (NbtElement) null);
		return hasher.hash();
	}

	public static void hash(Hasher hasher, @Nullable NbtElement element) {
		if(element == null) {
			hasher.putString("empty", StandardCharsets.US_ASCII);
		} else if(element instanceof NbtCompound n) {
			if(n.isEmpty()) {
				hasher.putString("empty", StandardCharsets.US_ASCII);
			} else {
				for(String key : (Iterable<String>) () -> n.getKeys().stream().sorted().iterator()) {
					hasher.putString(key, StandardCharsets.US_ASCII);
					hash(hasher, n.get(key));
				}
			}
		} else if(element instanceof NbtByteArray bs) {
			for(int i = 0; i < bs.size(); i++) {
				hasher.putInt(i);
				hasher.putByte(bs.getByteArray()[i]);
			}
		} else if(element instanceof NbtIntArray is) {
			for(int i = 0; i < is.size(); i++) {
				hasher.putInt(i);
				hasher.putInt(is.getIntArray()[i]);
			}
		} else if(element instanceof NbtLongArray ls) {
			for(int i = 0; i < ls.size(); i++) {
				hasher.putInt(i);
				hasher.putLong(ls.getLongArray()[i]);
			}
		} else if(element instanceof AbstractNbtList<?> l) {
			for(int i = 0, size = l.size(); i < size; i++) {
				NbtElement e = l.get(i);
				hasher.putInt(i);
				hash(hasher, e);
			}
		} else if(element instanceof NbtByte b) {
			hasher.putByte(b.byteValue());
		} else if(element instanceof NbtShort s) {
			hasher.putShort(s.shortValue());
		} else if(element instanceof NbtInt i) {
			hasher.putInt(i.intValue());
		} else if(element instanceof NbtFloat f) {
			hasher.putFloat(f.floatValue());
		} else if(element instanceof NbtLong l) {
			hasher.putLong(l.longValue());
		} else if(element instanceof NbtDouble d) {
			hasher.putDouble(d.doubleValue());
		} else if(element instanceof NbtNull) {
			hasher.putString("null", StandardCharsets.US_ASCII);
		} else if(element instanceof NbtString s) {
			hasher.putString(s.asString(), StandardCharsets.UTF_8);
		} else if(COMPAT_MODE) {
			OEELInternal.HashingOutput output = new OEELInternal.HashingOutput(hasher);
			try {
				element.write(output);
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}
		} else {
			throw new UnsupportedOperationException("unknown type " + element.getClass());
		}
	}
}
