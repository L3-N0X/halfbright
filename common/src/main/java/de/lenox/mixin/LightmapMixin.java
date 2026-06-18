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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public class LightmapMixin {
    @Shadow @Final private GpuTexture texture;
    @Shadow @Final private MappableRingBuffer ubo;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(LightmapRenderState renderState, CallbackInfo ci) {
        if (!HalfbrightConfig.INSTANCE.getEnabled()) {
            return;
        }

        if (renderState.needsUpdate) {
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

            float brightness = 1.0f;

            // 1. Maintain UBO update so other subsystems stay happy
            try (GpuBuffer.MappedView view = commandEncoder.mapBuffer(this.ubo.currentBuffer(), false, true)) {
                Std140Builder.intoBuffer(view.data())
                    .putFloat(renderState.skyFactor)
                    .putFloat(renderState.blockFactor)
                    .putFloat(renderState.nightVisionEffectIntensity)
                    .putFloat(renderState.darknessEffectScale)
                    .putFloat(renderState.bossOverlayWorldDarkening)
                    .putFloat(brightness)
                    .putVec3(renderState.blockLightTint)
                    .putVec3(renderState.skyLightColor)
                    .putVec3(renderState.ambientColor)
                    .putVec3(renderState.nightVisionColor);
            }

            // Perform CPU-based lightmap rendering
            // (If you are reading this and you know how a way to optimize this
            // or make it run faster on the gpu, please contact me!)
            NativeImage image = new NativeImage(16, 16, false);

            float targetBrightness = 0.0f;
            if (HalfbrightConfig.INSTANCE.getEnabled()) {
                float normalizedLevel = HalfbrightConfig.INSTANCE.getMinLightLevel() / 15.0f;
                float quadraticCurve = normalizedLevel * normalizedLevel;
                targetBrightness = halfbright$lerp(normalizedLevel, quadraticCurve, 0.5f);
            }

            for (int y = 0; y < 16; y++) {
                // Keep vanilla unextended sky level logic
                float skyLevel = y / 15.0f;
                float skyBrightness = halfbright$getBrightness(skyLevel) * renderState.skyFactor;

                for (int x = 0; x < 16; x++) {
                    // Keep vanilla unextended block level logic
                    float blockLevel = x / 15.0f;
                    float blockBrightness = halfbright$getBrightness(blockLevel) * renderState.blockFactor;

                    // Calculate ambient color with or without night vision
                    float nightVisionR = renderState.nightVisionColor.x() * renderState.nightVisionEffectIntensity;
                    float nightVisionG = renderState.nightVisionColor.y() * renderState.nightVisionEffectIntensity;
                    float nightVisionB = renderState.nightVisionColor.z() * renderState.nightVisionEffectIntensity;

                    float r = Math.max(renderState.ambientColor.x(), nightVisionR);
                    float g = Math.max(renderState.ambientColor.y(), nightVisionG);
                    float b = Math.max(renderState.ambientColor.z(), nightVisionB);

                    // Add vanilla sky light (Environment Base)
                    r += renderState.skyLightColor.x() * skyBrightness;
                    g += renderState.skyLightColor.y() * skyBrightness;
                    b += renderState.skyLightColor.z() * skyBrightness;

                    // Apply floor boost to the environment only
                    if (HalfbrightConfig.INSTANCE.getEnabled()) {
                        // Calculate perceived luminance
                        float currentEnvBrightness = 0.2126f * r + 0.7152f * g + 0.0722f * b;
                        currentEnvBrightness = Math.clamp(currentEnvBrightness, 0.0f, 1.0f);

                        // Proportional boost to maintain contrast
                        float boost = targetBrightness * (1.0f - currentEnvBrightness);

                        if (boost > 0.0f) {
                            // Extract the current ambient sky color
                            float skyR = renderState.skyLightColor.x();
                            float skyG = renderState.skyLightColor.y();
                            float skyB = renderState.skyLightColor.z();
                            float maxSky = Math.max(skyR, Math.max(skyG, skyB));

                            float tintR = 1.0f, tintG = 1.0f, tintB = 1.0f;

                            if (maxSky > 0.0f) {
                                // Mix the pure sky color with white (60% mix).
                                // This prevents the shadows from becoming TOO blue, keeping them natural.
                                tintR = halfbright$lerp(1.0f, skyR / maxSky, 0.6f);
                                tintG = halfbright$lerp(1.0f, skyG / maxSky, 0.6f);
                                tintB = halfbright$lerp(1.0f, skyB / maxSky, 0.6f);
                            }

                            // Normalize the tint using perceptual luminance.
                            // This guarantees that even if the tint is blue, it adds the EXACT amount of brightness our slider demands.
                            float tintLum = 0.2126f * tintR + 0.7152f * tintG + 0.0722f * tintB;
                            if (tintLum > 0.0f) {
                                tintR /= tintLum;
                                tintG /= tintLum;
                                tintB /= tintLum;
                            }

                            // Apply the color-corrected boost
                            r += boost * tintR;
                            g += boost * tintG;
                            b += boost * tintB;
                        }
                    }

                    // Add the block light tint on top.
                    // Because the floor is already locked in, the block light strictly adds brightness.
                    float tintR = renderState.blockLightTint.x();
                    float tintG = renderState.blockLightTint.y();
                    float tintB = renderState.blockLightTint.z();

                    if (HalfbrightConfig.INSTANCE.getEnabled()) {
                        // Apply a desaturation effect to the block light tint to remove some of the yellowish tint.
                        tintR = halfbright$lerp(tintR, 1.0f, targetBrightness);
                        tintG = halfbright$lerp(tintG, 1.0f, targetBrightness);
                        tintB = halfbright$lerp(tintB, 1.0f, targetBrightness);
                    }

                    float mixFactor = 0.9f * halfbright$parabolicMixFactor(blockLevel);
                    float blockLightR = halfbright$lerp(tintR, 1.0f, mixFactor);
                    float blockLightG = halfbright$lerp(tintG, 1.0f, mixFactor);
                    float blockLightB = halfbright$lerp(tintB, 1.0f, mixFactor);

                    r += blockLightR * blockBrightness;
                    g += blockLightG * blockBrightness;
                    b += blockLightB * blockBrightness;

                    // Apply boss overlay darkening effect
                    r = halfbright$lerp(r, r * 0.7f, renderState.bossOverlayWorldDarkening);
                    g = halfbright$lerp(g, g * 0.6f, renderState.bossOverlayWorldDarkening);
                    b = halfbright$lerp(b, b * 0.6f, renderState.bossOverlayWorldDarkening);

                    // Apply darkness effect scale
                    r -= renderState.darknessEffectScale;
                    g -= renderState.darknessEffectScale;
                    b -= renderState.darknessEffectScale;

                    // Apply brightness option (gamma)
                    r = Math.clamp(r, 0.0f, 1.0f);
                    g = Math.clamp(g, 0.0f, 1.0f);
                    b = Math.clamp(b, 0.0f, 1.0f);

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

                    r = halfbright$lerp(r, notGammaR, brightness);
                    g = halfbright$lerp(g, notGammaG, brightness);
                    b = halfbright$lerp(b, notGammaB, brightness);

                    r = Math.clamp(r, 0.0f, 1.0f);
                    g = Math.clamp(g, 0.0f, 1.0f);
                    b = Math.clamp(b, 0.0f, 1.0f);

                    int ri = Math.clamp(Math.round(r * 255.0f), 0, 255);
                    int gi = Math.clamp(Math.round(g * 255.0f), 0, 255);
                    int bi = Math.clamp(Math.round(b * 255.0f), 0, 255);

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

    @Unique
    private static float halfbright$getBrightness(float level) {
        return level / (4.0f - 3.0f * level);
    }

    @Unique
    private static float halfbright$parabolicMixFactor(float level) {
        float term = 2.0f * level - 1.0f;
        return term * term;
    }

    @Unique
    private static float halfbright$lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
