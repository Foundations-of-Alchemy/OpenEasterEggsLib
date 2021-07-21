package net.devtech.oeel.impl.mixin;

import net.devtech.oeel.v0.api.data.ObfResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin_ClientHook {
	@Shadow public abstract ResourceManager getResourceManager();

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;scanPacks()V"))
	public void atEnd(RunArgs args, CallbackInfo ci) {
		ReloadableResourceManager manager = (ReloadableResourceManager) this.getResourceManager();
		ObfResourceManager resourceManager = new ObfResourceManager();
		manager.registerReloader(resourceManager);
		ObfResourceManager.client = resourceManager;
	}
}
