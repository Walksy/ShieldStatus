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

    public static Identifier get(Identifier original) {
        return cache.computeIfAbsent(original, GrayscaleTextureCache::convert);
    }

    private static Identifier convert(Identifier original) {
        Minecraft client = Minecraft.getInstance();
        TextureManager textureManager = client.getTextureManager();

        NativeImage originalImage = null;

        AbstractTexture texture = textureManager.getTexture(original);
        if (texture instanceof DynamicTexture nativeTex) {
            try {
                NativeImage copy = new NativeImage(nativeTex.getPixels().getWidth(), nativeTex.getPixels().getHeight(), false);
                copy.copyFrom(nativeTex.getPixels());
                originalImage = copy;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (originalImage == null) {
            try {
                Optional<Resource> optRes = client.getResourceManager().getResource(original);
                if (optRes.isPresent()) {
                    try (InputStream in = optRes.get().open()) {
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

        NativeImage grayscale = new NativeImage(originalImage.getWidth(), originalImage.getHeight(), false);
        for (int y = 0; y < grayscale.getHeight(); y++) {
            for (int x = 0; x < grayscale.getWidth(); x++) {
                int rgba = originalImage.getPixel(x, y);

                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;

                int gray = (int) (r * 0.299f + g * 0.587f + b * 0.114f);
                grayscale.setPixel(x, y, (a << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }

        Identifier newId = Identifier.fromNamespaceAndPath(original.getNamespace(), "grayscale/" + original.getPath());
        textureManager.register(newId, new DynamicTexture(newId::toString, grayscale));

        return newId;
    }

}

