package net.devtech.oeel.impl;

import java.io.OutputStream;

public final class NullOutputStream extends OutputStream {
	public static final NullOutputStream NULL = new NullOutputStream();
	private NullOutputStream() {}

	// @formatter:off
	@Override public void write(byte[] b) {}
	@Override public void write(byte[] b, int off, int len) {}
	@Override public void flush() {}
	@Override public void close() {}
	@Override public void write(int b) {}
	@Override public String toString() {return "NullOutputStream";}
	@Override public Object clone() {return NULL;}
	@Override public int hashCode() {return 1;}
}