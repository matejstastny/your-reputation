package matejstastny.reputation.entity.passive;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

public interface IronGolemEntityInterface {
    public boolean addReport(Player player, Villager villager);

    public void clearReports();
}
