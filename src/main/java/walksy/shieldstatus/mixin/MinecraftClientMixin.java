package walksy.shieldstatus.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.shieldstatus.config.Config;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "tick",
            at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (!Config.modEnabled) return;
        Config.tick();
    }
}
