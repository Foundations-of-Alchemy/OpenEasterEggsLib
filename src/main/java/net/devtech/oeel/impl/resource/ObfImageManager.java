package net.devtech.oeel.impl.resource;

import java.util.function.Consumer;

import net.minecraft.client.texture.TextureManager;

public class ObfImageManager implements Consumer<ObfResourceManager> {
	final TextureManager manager;

	public ObfImageManager(TextureManager manager) {
		this.manager = manager;
	}

	@Override
	public void accept(ObfResourceManager manager) {
		/**
		 * PngFile pngFile = new PngFile(resource.toString(), resource.getInputStream());
		 * 							AnimationResourceMetadata animationResourceMetadata = (AnimationResourceMetadata)resource.getMetadata(AnimationResourceMetadata.READER);
		 * 							if (animationResourceMetadata == null) {
		 * 								animationResourceMetadata = AnimationResourceMetadata.EMPTY;
		 *                                                        }
		 *
		 * 							Pair<Integer, Integer> pair = animationResourceMetadata.ensureImageSize(pngFile.x, pngFile.y);
		 * 							info3 = new Sprite.Info(identifier, (Integer)pair.getFirst(), (Integer)pair.getSecond(), animationResourceMetadata);
		 */
	}
}
