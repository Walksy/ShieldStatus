package walksy.shieldstatus.manager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.concurrent.CopyOnWriteArrayList;

public class ShieldDataManager {
    
    public static ShieldDataManager INSTANCE = new ShieldDataManager();
    public final CopyOnWriteArrayList<PlayerStats> disabledShields = new CopyOnWriteArrayList<>();
    
    public void handlePlayerByteStatusEvent(byte status, Object castedClass) {
        if (LivingEntity.class.cast(castedClass) instanceof PlayerEntity player && status == 30) { //disable status
            disabledShields.add(new PlayerStats(player));
        }
    }

    public void tick() {
        disabledShields.forEach(playerStats -> {
            playerStats.ticks++;
            if (playerStats.ticks >= 100) {
                disabledShields.remove(playerStats);
            }
        });
    }


    public static class PlayerStats {
        public PlayerEntity player;
        public int ticks;

        public PlayerStats(PlayerEntity player) {
            this.player = player;
            this.ticks = 0;
        }
    }
}
