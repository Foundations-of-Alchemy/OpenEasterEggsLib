package net.devtech.oeel.v0.api.event;

import io.github.astrarre.access.v0.api.Access;
import net.devtech.oeel.impl.OEELImpl;

import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ServerResourceManager;

public interface ServerResourceManagerLoadEvent {
	Access<ServerResourceManagerLoadEvent> POST_TAG = new Access<>(OEELImpl.id2("server_resource_manager_load_event"), arr -> (s, m) -> {
		for(ServerResourceManagerLoadEvent e : arr) {
			e.onLoad(s, m);
		}
	});

	void onLoad(ServerResourceManager serverResourceManager, ReloadableResourceManager manager);
}
