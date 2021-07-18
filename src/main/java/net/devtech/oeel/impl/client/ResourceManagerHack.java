package net.devtech.oeel.impl.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

public record ResourceManagerHack(InputStream data) implements ResourceManager {
	@Override public Set<String> getAllNamespaces() {return Collections.emptySet();}
	@Override public boolean containsResource(Identifier id) {return false;}
	@Override public List<Resource> getAllResources(Identifier id) {return null;}
	@Override public Collection<Identifier> findResources(String startingPath, Predicate<String> pathPredicate) {return null;}
	@Override public Stream<ResourcePack> streamResourcePacks() {return null;}
	@Override public Resource getResource(Identifier id) {return new ResourceHack(this.data);}

	public record ResourceHack(InputStream data) implements Resource {
		@Override public Identifier getId() {return null;}
		@Override public InputStream getInputStream() {return this.data;}
		@Override public boolean hasMetadata() {return false;}
		@Nullable @Override public <T> T getMetadata(ResourceMetadataReader<T> metaReader) {return null;}
		@Override public String getResourcePackName() {return null;}
		@Override public void close() {}
	}
}
