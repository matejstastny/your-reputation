package matejstastny.reputation.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;

import matejstastny.reputation.ReputationMod;

public enum ReputationStatus {
    FRIENDLY("friendly", ChatFormatting.DARK_GREEN),
    TRUSTWORTHY("trustworthy", ChatFormatting.GREEN),
    NEUTRAL("neutral", ChatFormatting.GRAY),
    SUSPICIOUS("suspicious", ChatFormatting.RED),
    HOSTILE("hostile", ChatFormatting.DARK_RED),
    UNKNOWN("unknown", ChatFormatting.DARK_GRAY);

    private final String translateKey;
    private final ChatFormatting formatting;

    private ReputationStatus(String status, ChatFormatting formatting) {
        this.translateKey = String.format("entity.%s.villager.reputation.%s", ReputationMod.MOD_ID, status);
        this.formatting = formatting;
    }

    public static ReputationStatus getStatus(@Nullable Integer reputation) {
        if (reputation == null) {
            return UNKNOWN;
        } else if (reputation <= -100) {
            return HOSTILE;
        } else if (reputation < 0) {
            return SUSPICIOUS;
        } else if (reputation == 0) {
            return NEUTRAL;
        } else if (reputation < 100) {
            return TRUSTWORTHY;
        } else {
            return FRIENDLY;
        }
    }

    public String getTranslateKey() {
        return this.translateKey;
    }

    public ChatFormatting getFormatting() {
        return this.formatting;
    }
}
