package combatlogx.expansion.compatibility.idisguise;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.ConfigurationSection;

import com.github.sirblobman.api.configuration.IConfigurable;

public final class Configuration implements IConfigurable {
    private boolean undisguiseOnDirectAttackMob;
    private boolean undisguiseOnDirectAttackedByMob;
    private boolean undisguiseOnEnderpearlLand;
    private long graceMillis;

    public Configuration() {
        this.undisguiseOnDirectAttackMob = false;
        this.undisguiseOnDirectAttackedByMob = false;
        this.undisguiseOnEnderpearlLand = true;
        this.graceMillis = 0L;
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        this.undisguiseOnDirectAttackMob = config.getBoolean("undisguise-on-direct-attack-mob", false);
        this.undisguiseOnDirectAttackedByMob = config.getBoolean("undisguise-on-direct-attacked-by-mob", false);
        this.undisguiseOnEnderpearlLand = config.getBoolean("undisguise-on-enderpearl-land", true);
        long graceSeconds = Math.max(0L, config.getLong("grace-period-seconds", 0L));
        this.graceMillis = graceSeconds * 1000L;
    }

    public boolean isUndisguiseOnDirectAttackMob() {
        return this.undisguiseOnDirectAttackMob;
    }

    public boolean isUndisguiseOnDirectAttackedByMob() {
        return this.undisguiseOnDirectAttackedByMob;
    }

    public boolean isUndisguiseOnEnderpearlLand() {
        return this.undisguiseOnEnderpearlLand;
    }

    public long getGraceMillis() {
        return this.graceMillis;
    }
}
