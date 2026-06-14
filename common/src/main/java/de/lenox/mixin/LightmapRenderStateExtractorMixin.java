package de.lenox.mixin;

import de.lenox.HalfbrightConfig;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {
    @Inject(method = "extract", at = @At("TAIL"))
    private void onExtract(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (HalfbrightConfig.INSTANCE.getEnabled()) {
            float normalBrightness = renderState.brightness;
            float minLightLevel = HalfbrightConfig.INSTANCE.getMinLightLevel();
            
            int normalInt = Math.round(normalBrightness * 100.0f);
            int minInt = Math.round(minLightLevel * 10.0f);
            
            float encoded = normalInt * 1000.0f + minInt;
            renderState.brightness = -(encoded + 1.0f);
        }
    }
}
