package net.devtech.oeel.v0.api.datagen;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;

/**
 * sprite: [u32 hash] {[u8 magic] [u4 offX] [u4 offY] [u4 data] [u1 xData]}
 * recipe: [u32 hash] {}
 * @param <Self>
 */
public abstract class AbstractEncrypter<Self extends AbstractEncrypter<Self>> extends DataOutputStream {
	public AbstractEncrypter(OutputStream out) {
		super(out);
	}

}
