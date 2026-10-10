package walksy.shieldstatus.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class BannerQuad {
    private static final float X0 = -5f / 16f;
    private static final float X1 = 5f / 16f;
    private static final float Y0 = -10f / 16f;
    private static final float Y1 = 10f / 16f;
    private static final float Z = -2f / 16f - 0.0005f;

    private BannerQuad() {
    }

    public static void emit(final PoseStack.Pose pose, final VertexConsumer consumer, final int lightCoords, final int overlayCoords, final int stateColor) {
        final int color = stateColor | 0x00FFFFFF;
        vertex(pose, consumer, X1, Y0, 1f, 0f, color, lightCoords, overlayCoords);
        vertex(pose, consumer, X0, Y0, 0f, 0f, color, lightCoords, overlayCoords);
        vertex(pose, consumer, X0, Y1, 0f, 1f, color, lightCoords, overlayCoords);
        vertex(pose, consumer, X1, Y1, 1f, 1f, color, lightCoords, overlayCoords);
    }

    private static void vertex(final PoseStack.Pose pose, final VertexConsumer consumer, final float x, final float y, final float u, final float v, final int color, final int lightCoords, final int overlayCoords) {
        consumer.addVertex(pose, x, y, Z).setColor(color).setUv(u, v).setOverlay(overlayCoords).setLight(lightCoords).setNormal(pose, 0f, 0f, -1f);
    }
}
