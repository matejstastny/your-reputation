package matejstastny.reputation.provider;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

import snownee.jade.Jade;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;

import matejstastny.reputation.ReputationMod;
import matejstastny.reputation.entity.passive.VillagerEntityInterface;
import matejstastny.reputation.util.cache.VillagerCache;

public class VillagerSnitchProvider implements IServerDataProvider<EntityAccessor> {
    public static final VillagerSnitchProvider INSTANCE = new VillagerSnitchProvider();

    public static final Identifier VILLAGER_SNITCH_IDENTIFIER = Identifier.fromNamespaceAndPath(ReputationMod.MOD_ID, "villager_snitch");
    public static final String IS_SNITCH_KEY = "ReputationModIsSnitch";

    @Override
    public Identifier getUid() {
        return VillagerSnitchProvider.VILLAGER_SNITCH_IDENTIFIER;
    }

    @Override
    public final void appendServerData(CompoundTag data, EntityAccessor accessor) {
        Player player = accessor.getPlayer();
        Villager villager = (Villager) accessor.getEntity();

        boolean isSnitch = ((VillagerEntityInterface) villager).isSnitch(player);
        data.putBoolean(VillagerSnitchProvider.IS_SNITCH_KEY, isSnitch);
    }

    public static class Client implements IEntityComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public Identifier getUid() {
            return VillagerSnitchProvider.VILLAGER_SNITCH_IDENTIFIER;
        }

        @Override
        public int getDefaultPriority() {
            return TooltipPosition.HEAD - 1;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            Player player = accessor.getPlayer();
            Villager villager = (Villager) accessor.getEntity();

            VillagerCache.Data villagerData = this.getVillagerData(data, player, villager);

            IWailaConfig wailaConfig = Jade.config();

            String name = Optional
                    .ofNullable(villager.getCustomName())
                    .orElse(villager.getType().getDescription())
                    .getString();

            Component text = wailaConfig.formatting().registryName(name);
            if (villagerData.isSnitch()) {
                String snitchTranslateKey = String.format("entity.%s.villager.snitch",
                        ReputationMod.MOD_ID);
                MutableComponent mText = Component.empty();
                mText = mText.append(text.copy().withStyle(ChatFormatting.STRIKETHROUGH));
                mText = mText.append(" ");
                mText = mText.append(Component.translatable(snitchTranslateKey).withStyle(ChatFormatting.DARK_RED));

                text = mText;
            }

            tooltip.add(text);
        }

        private VillagerCache.Data getVillagerData(CompoundTag data, Player player, Villager villager) {
            Cache<Villager, VillagerCache.Data> villagerCache = VillagerCache.getOrCreate(player);
            VillagerCache.Data villagerData = Optional
                    .ofNullable(villagerCache.getIfPresent(villager))
                    .orElse(new VillagerCache.Data());

            @Nullable
            Boolean isSnitch = data.contains(VillagerSnitchProvider.IS_SNITCH_KEY)
                    ? data.getBoolean(VillagerSnitchProvider.IS_SNITCH_KEY).orElse(null)
                    : null;
            if (isSnitch != null) {
                villagerData.setIsSnitch(isSnitch);
            }

            villagerCache.put(villager, villagerData);

            return villagerData;
        }
    }
}
