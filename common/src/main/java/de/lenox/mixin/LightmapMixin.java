package de.lenox.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import de.lenox.HalfbrightConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.attribute.EnvironmentAttributes;
import java.util.OptionalInt;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class LightmapMixin {
    @Shadow @Final private com.mojang.blaze3d.textures.GpuTextureView textureView;
    @Shadow @Final private MappableRingBuffer ubo;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private GameRenderer renderer;
    @Shadow private boolean updateLightTexture;
    @Shadow private float blockLightRedFlicker;

    @Shadow
    private float calculateDarknessScale(net.minecraft.world.entity.LivingEntity livingEntity, float f, float g) {
        throw new AssertionError();
    }

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void onUpdateLightTexture(float f, CallbackInfo ci) {
        if (!this.updateLightTexture) return;
        if (!HalfbrightConfig.INSTANCE.getEnabled()) return;

        this.updateLightTexture = false;

        ClientLevel clientLevel = this.minecraft.level;
        if (clientLevel == null) return;

        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        int skyLightColorRGB = camera.attributeProbe().getValue(EnvironmentAttributes.SKY_LIGHT_COLOR, f);
        float ambientLightFactor = clientLevel.dimensionType().ambientLight();
        float skyFactor = camera.attributeProbe().getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, f);

        var endFlashState = clientLevel.endFlashState();
        Vector3f ambientColor;
        if (endFlashState != null) {
            ambientColor = new Vector3f(0.99F, 1.12F, 1.0F);
            if (!this.minecraft.options.hideLightningFlash().get()) {
                float flashIntensity = endFlashState.getIntensity(f);
                if (this.minecraft.gui.getBossOverlay().shouldCreateWorldFog()) {
                    skyFactor += flashIntensity / 3.0F;
                } else {
                    skyFactor += flashIntensity;
                }
            }
        } else {
            ambientColor = new Vector3f(1.0F, 1.0F, 1.0F);
        }

        float darknessEffectScale = this.minecraft.options.darknessEffectScale().get().floatValue();
        float darknessBlend = this.minecraft.player.getEffectBlendFactor(MobEffects.DARKNESS, f) * darknessEffectScale;
        float darknessScale = this.calculateDarknessScale(this.minecraft.player, darknessBlend, f) * darknessEffectScale;
        float nightVisionFactor;
        if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
            nightVisionFactor = GameRenderer.getNightVisionScale(this.minecraft.player, f);
        } else {
            float waterVision = this.minecraft.player.getWaterVision();
            if (waterVision > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
                nightVisionFactor = waterVision;
            } else {
                nightVisionFactor = 0.0F;
            }
        }

        float blockFactor = this.blockLightRedFlicker + 1.5F;
        float gamma = this.minecraft.options.gamma().get().floatValue();
        float darkenWorldAmount = this.renderer.getDarkenWorldAmount(f);

        float normalizedLevel = HalfbrightConfig.INSTANCE.getMinLightLevel() / 15.0f;
        float targetBrightness = halfbright$lerp(normalizedLevel, normalizedLevel * normalizedLevel, 0.5f);

        float brightnessFactor = Math.max(0.0F, gamma - darknessBlend + targetBrightness * 0.5f);
        skyFactor = skyFactor + targetBrightness * 0.3f;
        blockFactor = blockFactor + targetBrightness * 0.3f;

        Vector3f skyLightColor = ARGB.vector3fFromRGB24(skyLightColorRGB);

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.ubo.currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(mappedView.data())
                .putFloat(ambientLightFactor)
                .putFloat(skyFactor)
                .putFloat(blockFactor)
                .putFloat(nightVisionFactor)
                .putFloat(darknessScale)
                .putFloat(darkenWorldAmount)
                .putFloat(brightnessFactor)
                .putVec3(skyLightColor)
                .putVec3(ambientColor);
        }

        try (RenderPass renderPass = commandEncoder.createRenderPass(() -> "Update light (halfbright)", this.textureView, OptionalInt.empty())) {
            renderPass.setPipeline(RenderPipelines.LIGHTMAP);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("LightmapInfo", this.ubo.currentBuffer());
            renderPass.draw(0, 3);
        }

        this.ubo.rotate();
        ci.cancel();
    }

    @Unique
    private static float halfbright$lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
