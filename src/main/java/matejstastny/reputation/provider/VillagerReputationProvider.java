package matejstastny.reputation.provider;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

import matejstastny.reputation.ReputationMod;
import matejstastny.reputation.util.ReputationStatus;
import matejstastny.reputation.util.cache.VillagerCache;

public class VillagerReputationProvider implements IServerDataProvider<EntityAccessor> {
    public static final VillagerReputationProvider INSTANCE = new VillagerReputationProvider();

    public static final Identifier VILLAGER_REPUTATION_IDENTIFIER = Identifier.fromNamespaceAndPath(ReputationMod.MOD_ID,
            "villager_reputation");
    public static final String REPUTATION_KEY = "ReputationModReputation";

    @Override
    public Identifier getUid() {
        return VillagerReputationProvider.VILLAGER_REPUTATION_IDENTIFIER;
    }

    @Override
    public final void appendServerData(CompoundTag data, EntityAccessor accessor) {
        Player player = accessor.getPlayer();
        Villager villager = (Villager) accessor.getEntity();

        int reputation = villager.getPlayerReputation(player);
        data.putInt(VillagerReputationProvider.REPUTATION_KEY, reputation);
    }

    public static class Client implements IEntityComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public Identifier getUid() {
            return VillagerReputationProvider.VILLAGER_REPUTATION_IDENTIFIER;
        }

        @Override
        public int getDefaultPriority() {
            return TooltipPosition.BODY + 100;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            Player player = accessor.getPlayer();
            Villager villager = (Villager) accessor.getEntity();

            VillagerCache.Data villagerData = this.getVillagerData(data, player, villager);

            @Nullable
            Integer reputation = villagerData.getReputation();
            ReputationStatus status = ReputationStatus.getStatus(reputation);

            MutableComponent text = Component.translatable(status.getTranslateKey());

            if (reputation != null) {
                text = text.append(String.format(" (%d)", reputation));
            }

            text = text.withStyle(status.getFormatting());

            tooltip.add(text);
        }

        private VillagerCache.Data getVillagerData(CompoundTag data, Player player,
                Villager villager) {
            Cache<Villager, VillagerCache.Data> villagerCache = VillagerCache.getOrCreate(player);
            VillagerCache.Data villagerData = Optional
                    .ofNullable(villagerCache.getIfPresent(villager))
                    .orElse(new VillagerCache.Data());

            @Nullable
            Integer reputation = data.contains(VillagerReputationProvider.REPUTATION_KEY)
                    ? data.getInt(VillagerReputationProvider.REPUTATION_KEY).orElse(null)
                    : null;
            if (reputation != null) {
                villagerData.setReputation(reputation);
            }

            villagerCache.put(villager, villagerData);

            return villagerData;
        }
    }
}
