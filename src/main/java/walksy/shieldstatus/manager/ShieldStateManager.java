package walksy.shieldstatus.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

public class ShieldStateManager {

    private final Map<PlayerEntity, Integer> shieldUseTicks = new HashMap<>();
    private final HashMap<PlayerEntity, Long> attackedPlayerEntries = new HashMap<>();
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final int SHIELD_BREAK_STATUS = 30;

    public void set(PlayerEntity player) {
        player.getItemCooldownManager().set(new ItemStack(Items.SHIELD), 100);
    }

    public void update() {
        long now = System.currentTimeMillis();
        for (PlayerEntity player : client.world.getPlayers()) {
            if (isHoldingUsableShield(player) && player.isUsingItem()) {
                int current = shieldUseTicks.getOrDefault(player, 0);
                shieldUseTicks.put(player, current + 1);
            } else {
                shieldUseTicks.put(player, 0);
            }
        }

        //Expire old entries if there's no response from the server within 1000ms
        //Assume wrong rotation (from target) or hit reg issues
        attackedPlayerEntries.entrySet().removeIf(entry -> now - entry.getValue() > 1000);
    }

    /**
     * For servers pre 1.21.5
     */
    public void handleEntityStatus(PlayerEntity player, byte status) {
        if (status == this.SHIELD_BREAK_STATUS) {
            this.set(player);
        }
    }

    /**
     * For servers post 1.21.4
     */
    public void handleBreakPacket(double x, double y, double z) {
        if (client.world == null || client.player == null) return;

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;

            final boolean bl = attackedPlayerEntries.containsKey(player);
            final int MAX_DIST = 5;

            if (!bl) continue;

            if (player.squaredDistanceTo(x, y, z) <= MAX_DIST * MAX_DIST) {
                this.set(player);
                attackedPlayerEntries.remove(player);
                break;
            }
        }
    }

    public void handlePlayerAttack(PlayerEntity target) {
        if (this.isHoldingUsableShield(target) && target.isUsingItem() && this.disablesShield(client.player)) {
            this.attackedPlayerEntries.put(target, System.currentTimeMillis());
        }
    }

    public boolean isCoolingDown(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        return player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.SHIELD));
    }

    public boolean isUsingShield(PlayerEntity player) {
        return shieldUseTicks.getOrDefault(player, 0) >= 5;
    }

    public boolean isHoldingUsableShield(PlayerEntity entity) {
        return (entity.getMainHandStack().isOf(Items.SHIELD) || entity.getOffHandStack().isOf(Items.SHIELD)) && !isHoldingAnimationItemMainHand(entity);
    }

    private boolean isHoldingAnimationItemMainHand(PlayerEntity entity) {
        return entity.getMainHandStack().getMaxUseTime(entity) != 0
            && !entity.getMainHandStack().isOf(Items.SHIELD);
    }

    public boolean disablesShield(PlayerEntity player) {
        return player.getWeaponStack().getItem() instanceof AxeItem;
    }
}
