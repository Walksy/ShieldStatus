package walksy.shieldstatus.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class ShieldStateManager {

    private final long ATTACK_ENTRY_TTL_MS = 1000;
    private final Map<PlayerEntity, Integer> shieldUseTicks = new HashMap<>();
    private final Map<PlayerEntity, AttackEntry> attackedPlayerEntries = new HashMap<>();
    private final MinecraftClient client = MinecraftClient.getInstance();

    private record AttackEntry(Vec3d attackPos, Vec3d targetPos, long time, boolean wasBlocking) { }

    private boolean clientShieldDisabled = false;
    private long lastClientShieldDisabledAt = 0L;

    private final ShieldCooldownManager cooldownManager = new ShieldCooldownManager();

    public void set(PlayerEntity player) {
        if (player == null) return;
        cooldownManager.setCooldown(player);
        if (client != null && client.player != null && player == client.player) {
            clientShieldDisabled = true;
            lastClientShieldDisabledAt = System.currentTimeMillis();
        }
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (client.world == null) return;

        for (PlayerEntity player : client.world.getPlayers()) {
            if (isHoldingUsableShield(player) && player.isUsingItem()) {
                int current = shieldUseTicks.getOrDefault(player, 0);
                shieldUseTicks.put(player, current + 1);
            } else {
                shieldUseTicks.put(player, 0);
            }
        }

        attackedPlayerEntries.entrySet().removeIf(e -> now - e.getValue().time > ATTACK_ENTRY_TTL_MS);

        cooldownManager.tick();

        if (client.player != null) {
            boolean nowDisabled = cooldownManager.isCoolingDown(client.player);
            if (nowDisabled && !clientShieldDisabled) {
                lastClientShieldDisabledAt = System.currentTimeMillis();
            }
            clientShieldDisabled = nowDisabled;
        } else {
            clientShieldDisabled = false;
        }
    }

    public void handleBreakPacket(double x, double y, double z) {
        if (client == null || client.world == null || client.player == null) return;

        PlayerEntity local = client.player;
        long now = System.currentTimeMillis();

        long CLIENT_DISABLE_GRACE_MS = 250L;
        //if (now - lastClientShieldDisabledAt <= CLIENT_DISABLE_GRACE_MS) return;

        int MATCH_RADIUS = 5;
        final double matchRadiusSq = (double) MATCH_RADIUS * MATCH_RADIUS;
        PlayerEntity best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double bestClientSq = Double.POSITIVE_INFINITY;

        for (Map.Entry<PlayerEntity, AttackEntry> e : attackedPlayerEntries.entrySet()) {
            PlayerEntity candidate = e.getKey();
            AttackEntry ae = e.getValue();
            if (candidate == null || candidate == local) continue;
            if (now - ae.time > ATTACK_ENTRY_TTL_MS) continue;

            double apx = ae.attackPos.x, apy = ae.attackPos.y, apz = ae.attackPos.z;
            double tpx = ae.targetPos.x, tpy = ae.targetPos.y, tpz = ae.targetPos.z;

            double clientDx = apx - x, clientDy = apy - y, clientDz = apz - z;
            double clientSq = clientDx * clientDx + clientDy * clientDy + clientDz * clientDz;

            double dx = tpx - x, dy = tpy - y, dz = tpz - z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > matchRadiusSq) continue;

            double score = Math.max(0.0, (matchRadiusSq - distSq)) / matchRadiusSq;
            if (ae.wasBlocking) score += 0.30;
            if (!clientShieldDisabled && clientSq + 0.01 < distSq) score -= 0.35;

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
                bestClientSq = clientSq;
            }
        }

        double threshold = clientShieldDisabled ? 0.05 : 0.25;
        if (best == null || bestScore <= threshold) return;

        AttackEntry bestEntry = attackedPlayerEntries.get(best);
        if (bestEntry == null) return;

        double dx = bestEntry.targetPos.x - x, dy = bestEntry.targetPos.y - y, dz = bestEntry.targetPos.z - z;
        double bestDistSq = dx * dx + dy * dy + dz * dz;

        double clientScore = Math.max(0.0, (matchRadiusSq - bestClientSq)) / matchRadiusSq;

        double candDx = best.getX() - x, candDy = best.getY() - y, candDz = best.getZ() - z;
        double candidateCurrentDistSq = candDx * candDx + candDy * candDy + candDz * candDz;

        final double EPS = 0.05;
        final double CLOSE_DIST_SQ = 0.5;

        boolean accept = false;

        if (bestEntry.wasBlocking) {
            if (bestDistSq <= bestClientSq + EPS || candidateCurrentDistSq <= bestClientSq + EPS) accept = true;
            if (bestDistSq <= CLOSE_DIST_SQ && bestClientSq <= CLOSE_DIST_SQ) accept = true;
        }

        if (!accept) {
            double relativeFactor, deltaThreshold;
            if (clientShieldDisabled) {
                relativeFactor = 1.02;
                deltaThreshold = 0.05;
            } else {
                relativeFactor = 1.05;
                deltaThreshold = 0.08;
            }
            if (bestScore > clientScore * relativeFactor) accept = true;
            else if (bestScore - clientScore > deltaThreshold) accept = true;
        }

        if (accept) {
            set(best);
            attackedPlayerEntries.remove(best);
        }
    }

    public void handleEntityStatus(PlayerEntity player, byte status) {
        if (status == 30 && player != client.player) {
            this.set(player);
        }
    }

    public void handlePlayerAttack(PlayerEntity target) {
        boolean estBlocking = shieldUseTicks.getOrDefault(target, 0) >= 3;
        if (client == null || client.player == null) return;
        if (disablesShield(client.player)) {
            attackedPlayerEntries.put(
                target,
                new AttackEntry(
                    client.player.getPos(),
                    target.getPos(),
                    System.currentTimeMillis(),
                    estBlocking
                )
            );
        }
    }

    public boolean isCoolingDown(PlayerEntity player) {
        if (player == client.player) {
            return client.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.SHIELD));
        }
        return cooldownManager.isCoolingDown(player);
    }

    public float getCooldownProgress(PlayerEntity player) {
        if (player == null) return 0.0f;
        if (player == client.player) {
            return client.player.getItemCooldownManager().getCooldownProgress(new ItemStack(Items.SHIELD), 0);
        }
        int remaining = cooldownManager.getRemainingTicks(player);
        if (remaining <= 0) return 0.0f;
        float frac = remaining / (float) 100;
        return Math.max(0f, Math.min(1f, frac));
    }

    public boolean isUsingShield(PlayerEntity player) {
        return shieldUseTicks.getOrDefault(player, 0) >= 5;
    }

    public boolean isHoldingUsableShield(PlayerEntity entity) {
        return (entity.getMainHandStack().isOf(Items.SHIELD)
            || entity.getOffHandStack().isOf(Items.SHIELD))
            && !isHoldingAnimationItemMainHand(entity);
    }

    private boolean isHoldingAnimationItemMainHand(PlayerEntity entity) {
        return entity.getMainHandStack().getMaxUseTime(entity) != 0
            && !entity.getMainHandStack().isOf(Items.SHIELD);
    }

    public boolean disablesShield(PlayerEntity player) {
        return player.getWeaponStack().getItem() instanceof AxeItem;
    }
}
