package de.lenox.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import de.lenox.HalfbrightConfig;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public class LightmapMixin {
    @Shadow @Final private GpuTexture texture;
    @Shadow @Final private MappableRingBuffer ubo;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(LightmapRenderState renderState, CallbackInfo ci) {
        if (renderState.needsUpdate) {
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

            // 1. Maintain UBO update so other subsystems stay happy
            try (GpuBuffer.MappedView view = commandEncoder.mapBuffer(this.ubo.currentBuffer(), false, true)) {
                Std140Builder.intoBuffer(view.data())
                    .putFloat(renderState.skyFactor)
                    .putFloat(renderState.blockFactor)
                    .putFloat(renderState.nightVisionEffectIntensity)
                    .putFloat(renderState.darknessEffectScale)
                    .putFloat(renderState.bossOverlayWorldDarkening)
                    .putFloat(renderState.brightness)
                    .putVec3(renderState.blockLightTint)
                    .putVec3(renderState.skyLightColor)
                    .putVec3(renderState.ambientColor)
                    .putVec3(renderState.nightVisionColor);
            }

            // 2. Perform CPU-based lightmap rendering
            NativeImage image = new NativeImage(16, 16, false);
            
            float minLevel = HalfbrightConfig.INSTANCE.getEnabled() ? HalfbrightConfig.INSTANCE.getMinLightLevel() / 15.0f : 0.0f;

            for (int y = 0; y < 16; y++) {
                float skyLevel = y / 15.0f;
                if (HalfbrightConfig.INSTANCE.getEnabled()) {
                    skyLevel = minLevel + (1.0f - minLevel) * skyLevel;
                }
                float skyBrightness = getBrightness(skyLevel) * renderState.skyFactor;

                for (int x = 0; x < 16; x++) {
                    float blockLevel = x / 15.0f;
                    if (HalfbrightConfig.INSTANCE.getEnabled()) {
                        blockLevel = minLevel + (1.0f - minLevel) * blockLevel;
                    }
                    float blockBrightness = getBrightness(blockLevel) * renderState.blockFactor;

                    // Calculate ambient color with or without night vision
                    float nightVisionR = renderState.nightVisionColor.x() * renderState.nightVisionEffectIntensity;
                    float nightVisionG = renderState.nightVisionColor.y() * renderState.nightVisionEffectIntensity;
                    float nightVisionB = renderState.nightVisionColor.z() * renderState.nightVisionEffectIntensity;

                    float r = Math.max(renderState.ambientColor.x(), nightVisionR);
                    float g = Math.max(renderState.ambientColor.y(), nightVisionG);
                    float b = Math.max(renderState.ambientColor.z(), nightVisionB);

                    // Add sky light
                    r += renderState.skyLightColor.x() * skyBrightness;
                    g += renderState.skyLightColor.y() * skyBrightness;
                    b += renderState.skyLightColor.z() * skyBrightness;

                    // Add block light
                    float mixFactor = 0.9f * parabolicMixFactor(blockLevel);
                    float blockLightR = lerp(renderState.blockLightTint.x(), 1.0f, mixFactor);
                    float blockLightG = lerp(renderState.blockLightTint.y(), 1.0f, mixFactor);
                    float blockLightB = lerp(renderState.blockLightTint.z(), 1.0f, mixFactor);

                    r += blockLightR * blockBrightness;
                    g += blockLightG * blockBrightness;
                    b += blockLightB * blockBrightness;

                    // Apply boss overlay darkening effect
                    r = lerp(r, r * 0.7f, renderState.bossOverlayWorldDarkening);
                    g = lerp(g, g * 0.6f, renderState.bossOverlayWorldDarkening);
                    b = lerp(b, b * 0.6f, renderState.bossOverlayWorldDarkening);

                    // Apply darkness effect scale
                    r -= renderState.darknessEffectScale;
                    g -= renderState.darknessEffectScale;
                    b -= renderState.darknessEffectScale;

                    // Apply brightness option (gamma)
                    r = clamp(r, 0.0f, 1.0f);
                    g = clamp(g, 0.0f, 1.0f);
                    b = clamp(b, 0.0f, 1.0f);

                    float maxComponent = Math.max(Math.max(r, g), b);
                    float notGammaR = r;
                    float notGammaG = g;
                    float notGammaB = b;
                    if (maxComponent > 0.0f) {
                        float maxInverted = 1.0f - maxComponent;
                        float maxScaled = 1.0f - maxInverted * maxInverted * maxInverted * maxInverted;
                        float scale = maxScaled / maxComponent;
                        notGammaR = r * scale;
                        notGammaG = g * scale;
                        notGammaB = b * scale;
                    }

                    r = lerp(r, notGammaR, renderState.brightness);
                    g = lerp(g, notGammaG, renderState.brightness);
                    b = lerp(b, notGammaB, renderState.brightness);

                    r = clamp(r, 0.0f, 1.0f);
                    g = clamp(g, 0.0f, 1.0f);
                    b = clamp(b, 0.0f, 1.0f);

                    int ri = Math.max(0, Math.min(255, Math.round(r * 255.0f)));
                    int gi = Math.max(0, Math.min(255, Math.round(g * 255.0f)));
                    int bi = Math.max(0, Math.min(255, Math.round(b * 255.0f)));

                    int pixelARGB = (255 << 24) | (ri << 16) | (gi << 8) | bi;
                    image.setPixel(x, y, pixelARGB);
                }
            }

            commandEncoder.writeToTexture(this.texture, image);
            image.close();

            // 3. Rotate UBO
            this.ubo.rotate();

            renderState.needsUpdate = false;
            ci.cancel();
        }
    }

    private static float getBrightness(float level) {
        return level / (4.0f - 3.0f * level);
    }

    private static float parabolicMixFactor(float level) {
        float term = 2.0f * level - 1.0f;
        return term * term;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
