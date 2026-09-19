package matejstastny.reputation.entity.passive;

import net.minecraft.world.entity.player.Player;

public interface VillagerEntityInterface {
    public boolean isSnitch(Player player);

    public void setIsSnitch(Player player, boolean isSnitch);
}
