package net.devtech.oeel.v0.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.github.astrarre.util.v0.api.Validate;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

public class OEELSerializing {
	public static ItemStack deserializeItem(byte[] data) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis);
			return ItemStack.fromNbt(NbtIo.read(dis));
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	public static byte[] serializeItem(ItemStack stack) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			NbtIo.write(stack.writeNbt(new NbtCompound()), dos);
			dos.flush();
			return bos.toByteArray();
		} catch(Throwable t) {
			throw Validate.rethrow(t);
		}
	}
}
