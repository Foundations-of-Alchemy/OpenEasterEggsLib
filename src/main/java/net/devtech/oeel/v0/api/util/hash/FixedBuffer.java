package net.devtech.oeel.v0.api.util.hash;

import net.devtech.oeel.impl.OEELInternal;
import org.jetbrains.annotations.NotNull;

public abstract class FixedBuffer<Self extends FixedBuffer<Self>> implements Comparable<Self> {
	public abstract byte getByte(int index);

	public abstract int bytes();

	public void append(StringBuilder builder) {
		builder.ensureCapacity(builder.capacity() + 128);
		for(int i = 0; i < 64; i++) {
			byte b = this.getByte(i);
			builder.append(OEELInternal.HEX_ARRAY_C[b >>> 4]);
			builder.append(OEELInternal.HEX_ARRAY_C[b & 0x0F]);
		}
	}

	@Override
	public int compareTo(@NotNull Self o) {
		for(int i = 0; i < this.bytes(); i++) {
			int comp = this.getByte(i) - o.getByte(i);
			if(comp != 0) {
				return comp;
			}
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		this.append(builder);
		return builder.toString();
	}
}
