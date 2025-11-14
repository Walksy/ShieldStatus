package walksy.shieldstatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

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
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();

        NativeImage originalImage = null;

        AbstractTexture texture = textureManager.getTexture(original);
        if (texture instanceof NativeImageBackedTexture nativeTex) {
            try {
                NativeImage copy = new NativeImage(nativeTex.getImage().getWidth(), nativeTex.getImage().getHeight(), false);
                copy.copyFrom(nativeTex.getImage());
                originalImage = copy;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (originalImage == null) {
            try {
                Optional<Resource> optRes = client.getResourceManager().getResource(original);
                if (optRes.isPresent()) {
                    try (InputStream in = optRes.get().getInputStream()) {
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
                int rgba = originalImage.getColorArgb(x, y);

                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;

                int gray = (int) (r * 0.299f + g * 0.587f + b * 0.114f);
                grayscale.setColorArgb(x, y, (a << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }

        Identifier newId = Identifier.of(original.getNamespace(), "grayscale/" + original.getPath());
        textureManager.registerTexture(newId, new NativeImageBackedTexture(grayscale));

        return newId;
    }

}

