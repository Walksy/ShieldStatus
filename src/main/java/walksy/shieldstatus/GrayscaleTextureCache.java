package walksy.shieldstatus;


import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GrayscaleTextureCache {
    private static final Map<Identifier, Identifier> cache = new HashMap<>();

    public static Identifier get(final Identifier original) {
        return cache.computeIfAbsent(original, GrayscaleTextureCache::convert);
    }

    private static Identifier convert(final Identifier original) {
        final Minecraft client = Minecraft.getInstance();
        final TextureManager textureManager = client.getTextureManager();
        NativeImage originalImage = null;
        final AbstractTexture texture = textureManager.getTexture(original);
        if (texture instanceof DynamicTexture nativeTex) {
            try {
                final NativeImage copy = new NativeImage(nativeTex.getPixels().getWidth(), nativeTex.getPixels().getHeight(), false);
                copy.copyFrom(nativeTex.getPixels());
                originalImage = copy;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (originalImage == null) {
            try {
                final Optional<Resource> optRes = client.getResourceManager().getResource(original);
                if (optRes.isPresent()) {
                    try (final InputStream in = optRes.get().open()) {
                        originalImage = NativeImage.read(in);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (originalImage == null) {
            return original;
        }
        final NativeImage grayscale = new NativeImage(originalImage.getWidth(), originalImage.getHeight(), false);
        for (int y = 0; y < grayscale.getHeight(); y++) {
            for (int x = 0; x < grayscale.getWidth(); x++) {
                final int rgba = originalImage.getPixel(x, y);
                final int a = (rgba >> 24) & 0xFF;
                final int r = (rgba >> 16) & 0xFF;
                final int g = (rgba >> 8) & 0xFF;
                final int b = rgba & 0xFF;
                final int gray = (int) (r * 0.299f + g * 0.587f + b * 0.114f);
                grayscale.setPixel(x, y, (a << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }
        final Identifier newId = Identifier.fromNamespaceAndPath(original.getNamespace(), "grayscale/" + original.getPath());
        textureManager.register(newId, new DynamicTexture(newId::toString, grayscale));
        return newId;
    }

}

