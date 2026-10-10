package walksy.shieldstatus.render;

import com.mojang.blaze3d.platform.NativeImage;
import main.walksy.lib.api.config.value.CroppedTexture;
import main.walksy.lib.api.render.AnimatedTextures;
import main.walksy.lib.api.render.CachedTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class BannerTextureCache {
    private static final int MAX_HEIGHT = 1024;
    private static final int MAX_ANIMATED_HEIGHT = 512;
    private static final Map<CroppedTexture, Entry> ENTRIES = new HashMap<>();
    private static int nextId;

    private static final class Entry {
        private final Map<Identifier, Identifier> ready = new HashMap<>();
        private final Set<Identifier> building = new HashSet<>();
        private Identifier lastReady;
        private boolean released;
    }

    private BannerTextureCache() {
    }

    public static void retain(final CroppedTexture first, final CroppedTexture second) {
        ENTRIES.entrySet().removeIf(entry -> {
            if (entry.getKey().equals(first) || entry.getKey().equals(second)) {
                return false;
            }
            release(entry.getValue());
            return true;
        });
    }

    public static @Nullable Identifier get(final CroppedTexture banner) {
        if (banner.isEmpty()) {
            return null;
        }
        final Identifier source = banner.texture();
        if (!CachedTextures.isReady(source)) {
            CachedTextures.ensureLoaded(source);
            return null;
        }
        final Entry entry = ENTRIES.computeIfAbsent(banner, key -> new Entry());
        final Identifier frame = AnimatedTextures.currentFrame(source);
        final Identifier ready = entry.ready.get(frame);
        if (ready != null) {
            entry.lastReady = ready;
            return ready;
        }
        if (entry.building.add(frame)) {
            build(entry, frame, banner, AnimatedTextures.isAnimated(source) ? MAX_ANIMATED_HEIGHT : MAX_HEIGHT);
        }
        return entry.lastReady;
    }

    private static void build(final Entry entry, final Identifier frame, final CroppedTexture banner, final int maxHeight) {
        final NativeImage source = copySource(frame);
        if (source == null) {
            return;
        }
        final Thread thread = new Thread(() -> {
            final NativeImage image = compose(source, banner, maxHeight);
            source.close();
            Minecraft.getInstance().execute(() -> register(entry, frame, image));
        }, "shieldstatus-banner");
        thread.setDaemon(true);
        thread.start();
    }

    private static void register(final Entry entry, final Identifier frame, final NativeImage image) {
        if (entry.released) {
            image.close();
            return;
        }
        entry.building.remove(frame);
        final Identifier id = Identifier.fromNamespaceAndPath("shieldstatus", "banner/" + nextId++);
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
        entry.ready.put(frame, id);
    }

    private static void release(final Entry entry) {
        entry.released = true;
        final TextureManager textures = Minecraft.getInstance().getTextureManager();
        for (final Identifier id : entry.ready.values()) {
            textures.release(id);
        }
        entry.ready.clear();
        entry.building.clear();
        entry.lastReady = null;
    }

    private static @Nullable NativeImage copySource(final Identifier frame) {
        final Minecraft minecraft = Minecraft.getInstance();
        final AbstractTexture texture = minecraft.getTextureManager().getTexture(frame);
        if (texture instanceof final DynamicTexture dynamic && dynamic.getPixels() != null) {
            final NativeImage pixels = dynamic.getPixels();
            final NativeImage copy = new NativeImage(pixels.getWidth(), pixels.getHeight(), false);
            copy.copyFrom(pixels);
            return copy;
        }
        final Optional<Resource> resource = minecraft.getResourceManager().getResource(frame);
        if (resource.isEmpty()) {
            return null;
        }
        try (final InputStream in = resource.get().open()) {
            return NativeImage.read(in);
        } catch (final IOException e) {
            return null;
        }
    }

    private static NativeImage compose(final NativeImage source, final CroppedTexture banner, final int maxHeight) {
        final int sourceW = source.getWidth();
        final int sourceH = source.getHeight();
        final float cropX = banner.u0() * sourceW;
        final float cropY = banner.v0() * sourceH;
        final float cropW = (banner.u1() - banner.u0()) * sourceW;
        final float cropH = (banner.v1() - banner.v0()) * sourceH;
        final int targetH = Math.max(1, Math.min(maxHeight, Math.round(cropH)));
        final int targetW = Math.max(1, Math.round(targetH * cropW / cropH));
        final NativeImage target = new NativeImage(targetW, targetH, true);
        final float stepX = cropW / targetW;
        final float stepY = cropH / targetH;
        for (int ty = 0; ty < targetH; ty++) {
            final int sy0 = Math.min(sourceH - 1, (int) (cropY + ty * stepY));
            final int sy1 = Math.max(sy0 + 1, Math.min(sourceH, (int) Math.ceil(cropY + (ty + 1) * stepY)));
            for (int tx = 0; tx < targetW; tx++) {
                final int sx0 = Math.min(sourceW - 1, (int) (cropX + tx * stepX));
                final int sx1 = Math.max(sx0 + 1, Math.min(sourceW, (int) Math.ceil(cropX + (tx + 1) * stepX)));
                target.setPixel(tx, ty, average(source, sx0, sy0, sx1, sy1));
            }
        }
        return target;
    }

    private static int average(final NativeImage source, final int x0, final int y0, final int x1, final int y1) {
        long alpha = 0;
        long c0 = 0;
        long c1 = 0;
        long c2 = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                final int pixel = source.getPixel(x, y);
                final int a = (pixel >>> 24) & 0xFF;
                alpha += a;
                c0 += (long) ((pixel >> 16) & 0xFF) * a;
                c1 += (long) ((pixel >> 8) & 0xFF) * a;
                c2 += (long) (pixel & 0xFF) * a;
            }
        }
        if (alpha == 0) {
            return 0;
        }
        final int count = (x1 - x0) * (y1 - y0);
        return (int) (alpha / count) << 24 | (int) (c0 / alpha) << 16 | (int) (c1 / alpha) << 8 | (int) (c2 / alpha);
    }
}
