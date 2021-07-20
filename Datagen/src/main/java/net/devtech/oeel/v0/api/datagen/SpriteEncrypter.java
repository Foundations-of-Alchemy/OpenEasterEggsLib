package net.devtech.oeel.v0.api.datagen;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;
import net.devtech.oeel.v0.api.util.hash.HashKey;

public class SpriteEncrypter extends AbstractEncrypter<SpriteEncrypter> {
	/**
	 * @param key the hash encryptionKey of the sprite
	 */
	protected SpriteEncrypter(HashKey key, OutputStream out) throws IOException {
		super(key, out);
	}

	public SpriteEncrypter write(BufferedImage image, int offX, int offY, JsonObject object) throws IOException {
		this.writeInt(offX);
		this.writeInt(offY);
		this.writeInt(image.getWidth());
		this.writeInt(image.getHeight());
		if(object == null) {
			this.writeUTF("");
		} else {
			this.writeUTF(object.toString());
		}
		ImageIO.write(image, "png", this);
		return this;
	}
}
