package matejstastny.reputation.provider;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;

import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

import matejstastny.reputation.ReputationMod;
import matejstastny.reputation.nbt.ModNbtHelper;
import matejstastny.reputation.util.cache.IronGolemCache;

public class IronGolemProvider implements IServerDataProvider<EntityAccessor> {
    public static final IronGolemProvider INSTANCE = new IronGolemProvider();

    public static final Identifier IRON_GOLEM_IDENTIFIER = Identifier.fromNamespaceAndPath(ReputationMod.MOD_ID, "iron_golem");
    public static final String ANGRY_AT_KEY = "ReputationModAngryAt";

    @Override
    public Identifier getUid() {
        return IronGolemProvider.IRON_GOLEM_IDENTIFIER;
    }

    @Override
    public final void appendServerData(CompoundTag data, EntityAccessor accessor) {
        IronGolem golem = (IronGolem) accessor.getEntity();

        @Nullable
        EntityReference angryAtRef = golem.getPersistentAngerTarget();
        if (angryAtRef != null) {
            data.put(IronGolemProvider.ANGRY_AT_KEY, ModNbtHelper.fromUuid(angryAtRef.getUUID()));
        }
    }

    public static class Client implements IEntityComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public Identifier getUid() {
            return IronGolemProvider.IRON_GOLEM_IDENTIFIER;
        }

        @Override
        public int getDefaultPriority() {
            return TooltipPosition.BODY + 100;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            Player player = accessor.getPlayer();
            IronGolem golem = (IronGolem) accessor.getEntity();

            IronGolemCache.Data golemData = this.getIronGolemData(data, player, golem);

            @Nullable
            UUID angryAt = golemData.getAngryAt();

            if (player.getUUID().equals(angryAt)) {
                String angryTranslateKey = String.format("entity.%s.iron_golem.angry", ReputationMod.MOD_ID);
                MutableComponent text = Component.translatable(angryTranslateKey).withStyle(ChatFormatting.DARK_RED);
                tooltip.add(text);
            }
        }

        private IronGolemCache.Data getIronGolemData(CompoundTag data, Player player, IronGolem golem) {
            Cache<IronGolem, IronGolemCache.Data> golemCache = IronGolemCache.getOrCreate(player);
            IronGolemCache.Data golemData = Optional
                    .ofNullable(golemCache.getIfPresent(golem))
                    .orElse(new IronGolemCache.Data());

            @Nullable
            UUID angryAt = data.contains(IronGolemProvider.ANGRY_AT_KEY)
                    ? ModNbtHelper.toUuid(data.get(IronGolemProvider.ANGRY_AT_KEY))
                    : null;
            golemData.setAngryAt(angryAt);

            golemCache.put(golem, golemData);

            return golemData;
        }
    }
}
