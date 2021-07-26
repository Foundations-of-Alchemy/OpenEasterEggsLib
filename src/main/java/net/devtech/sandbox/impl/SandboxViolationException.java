package net.devtech.sandbox.impl;

public class SandboxViolationException extends IllegalAccessError {
	public SandboxViolationException() {
	}

	public SandboxViolationException(String s) {
		super(s);
	}
}
