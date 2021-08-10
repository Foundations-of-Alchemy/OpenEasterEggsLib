package net.devtech.oeel.v0.api.event;

import io.github.astrarre.access.v0.api.Access;
import net.devtech.oeel.impl.OEELImpl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ClientResourceManagerLoadEvent {
	Access<ClientResourceManagerLoadEvent> POST_ALL = new Access<>(OEELImpl.id2("client_resource_manager_load_event"), arr -> (s, m) -> {
		for(ClientResourceManagerLoadEvent e : arr) {
			e.onLoad(s, m);
		}
	});

	@Environment(EnvType.CLIENT)
	void onLoad(MinecraftClient client, ReloadableResourceManager manager);
}
