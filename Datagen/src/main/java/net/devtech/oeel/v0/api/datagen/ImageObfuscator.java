package net.devtech.oeel.v0.api.datagen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import net.devtech.oeel.v0.api.datagen.util.SecureVoronoi;

public class ImageObfuscator {
	public static void main(String[] args) throws IOException {
		int i = 0;
		for(BufferedImage image : obfuscateFull(ImageIO.read(new File("img.png")))) {
			ImageIO.write(image, "png", new File("o/" + Integer.toHexString(i++) + ".png"));
		}
	}

	/**
	 * obfuscate full textures (textures that fill up the entirety of
	 */
	public static BufferedImage[] obfuscateFull(BufferedImage image) {
		int xSize = image.getWidth(), ySize = image.getHeight();
		SecureVoronoi voronoi = new SecureVoronoi(image.hashCode(), xSize / 8, ySize / 8);
		BufferedImage[] images = new BufferedImage[10];
		for(int i = 0; i < images.length; i++) {
			images[i] = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_ARGB);
		}

		Random random = new Random(voronoi.seed());
		for(int x = 0; x < xSize; x++) {
			for(int y = 0; y < ySize; y++) {
				int imageIndex = voronoi.voronoi(x, y) % images.length;
				for(int i = 0; i < imageIndex; i++) { // since this bit will be overlayed anyways, we can put random giberish in here
					images[i].setRGB(x, y, getRGB(image, x + random.nextInt() % (xSize / 8), y + random.nextInt() % (xSize / 8)));
				}
				images[Math.abs(imageIndex)].setRGB(x, y, image.getRGB(x, y));
			}
		}

		return images;
	}

	private static int getRGB(BufferedImage image, int x, int y) {
		if(x >= image.getWidth()) {
			x = image.getWidth() - 1;
		} else if(x < 0) {
			x = 0;
		}
		if(y >= image.getHeight()) {
			y = image.getHeight() - 1;
		} else if(y < 0) {
			y = 0;
		}
		return image.getRGB(x, y);
	}
}
