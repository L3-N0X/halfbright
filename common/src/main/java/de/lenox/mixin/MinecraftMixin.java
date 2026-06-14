package de.lenox.mixin;

import de.lenox.client.HalfbrightKeybinds;
import de.lenox.HalfbrightConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void onHandleKeybinds(CallbackInfo ci) {
        while (HalfbrightKeybinds.INSTANCE.getToggleKey().consumeClick()) {
            boolean newState = !HalfbrightConfig.INSTANCE.getEnabled();
            HalfbrightConfig.INSTANCE.setEnabled(newState);
            HalfbrightConfig.INSTANCE.save();
        }
        
        while (HalfbrightKeybinds.INSTANCE.getIncreaseKey().consumeClick()) {
            float current = HalfbrightConfig.INSTANCE.getMinLightLevel();
            float next = Math.min(15.0f, current + 0.5f);
            HalfbrightConfig.INSTANCE.setMinLightLevel(next);
            HalfbrightConfig.INSTANCE.save();
        }
        
        while (HalfbrightKeybinds.INSTANCE.getDecreaseKey().consumeClick()) {
            float current = HalfbrightConfig.INSTANCE.getMinLightLevel();
            float next = Math.max(0.0f, current - 0.5f);
            HalfbrightConfig.INSTANCE.setMinLightLevel(next);
            HalfbrightConfig.INSTANCE.save();
        }
    }
}
