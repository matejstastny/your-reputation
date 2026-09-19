package matejstastny.reputation.util.cache;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

public class VillagerCache {
    private static final Map<Player, Cache<Villager, VillagerCache.Data>> VILLAGER_CACHE_MAP = new HashMap<>();

    public static Cache<Villager, VillagerCache.Data> getOrCreate(Player player) {
        if (!VillagerCache.VILLAGER_CACHE_MAP.containsKey(player)) {
            Cache<Villager, VillagerCache.Data> cache = CacheBuilder
                    .newBuilder()
                    .maximumSize(CacheConfig.MAXIMUM_CACHE_SIZE)
                    .build();
            VillagerCache.VILLAGER_CACHE_MAP.put(player, cache);
        }

        return VillagerCache.VILLAGER_CACHE_MAP.get(player);
    }

    public static class Data {
        @Nullable
        private Integer reputation;
        private boolean isSnitch;

        public Data(@Nullable Integer reputation, boolean isSnitch) {
            this.reputation = reputation;
            this.isSnitch = isSnitch;
        }

        public Data() {
            this(null, false);
        }

        @Nullable
        public Integer getReputation() {
            return this.reputation;
        }

        public void setReputation(@Nullable Integer reputation) {
            this.reputation = reputation;
        }

        public boolean isSnitch() {
            return this.isSnitch;
        }

        public void setIsSnitch(boolean isSnitch) {
            this.isSnitch = isSnitch;
        }
    }
}
