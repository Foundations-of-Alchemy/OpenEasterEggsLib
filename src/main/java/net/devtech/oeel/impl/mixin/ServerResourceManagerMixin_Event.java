package net.devtech.oeel.impl.mixin;

import net.devtech.oeel.v0.api.data.ServerResourceManagerLoadEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.registry.DynamicRegistryManager;

@Mixin(ServerResourceManager.class)
public class ServerResourceManagerMixin_Event {
	@Shadow @Final private ReloadableResourceManager resourceManager;

	@Inject(method = "<init>",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/resource/ReloadableResourceManager;registerReloader(Lnet/minecraft/resource/ResourceReloader;)V",
					ordinal = 1))
	public void onInit(DynamicRegistryManager registryManager,
			CommandManager.RegistrationEnvironment commandEnvironment,
			int functionPermissionLevel,
			CallbackInfo ci) {
		ServerResourceManagerLoadEvent.POST_TAG.get().onLoad((ServerResourceManager) (Object) this, this.resourceManager);
	}
}
