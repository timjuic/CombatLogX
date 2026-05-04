package combatlogx.expansion.compatibility.idisguise;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.disguise.DisguiseExpansion;
import com.github.sirblobman.combatlogx.api.expansion.disguise.DisguiseHandler;

public final class Expansion_iDisguise extends DisguiseExpansion {
    private final Configuration configuration;
    private DisguiseHandler<?> disguiseHandler;
    private DisguiseGraceTracker graceTracker;

    public Expansion_iDisguise(@NotNull ICombatLogX plugin) {
        super(plugin);
        this.configuration = new Configuration();
        this.disguiseHandler = null;
        this.graceTracker = null;
    }

    @Override
    public boolean checkDependencies() {
        return checkDependency("iDisguise", true, "6");
    }

    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
    }

    @Override
    public @NotNull DisguiseHandler<?> getDisguiseHandler() {
        if (this.disguiseHandler == null) {
            this.disguiseHandler = new DisguiseHandler_iDisguise(this);
        }
        return this.disguiseHandler;
    }

    @Override
    public void reloadConfig() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.reload("config.yml");

        YamlConfiguration yaml = configurationManager.get("config.yml");
        this.configuration.load(yaml);
    }

    @Override
    public void afterEnable() {
        this.graceTracker = new DisguiseGraceTracker();
        ICombatLogX combatLogX = getPlugin();
        JavaPlugin javaPlugin = combatLogX.getPlugin();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this.graceTracker, javaPlugin);

        new DisguiseDamageListener(this).register();
        new DirectUndisguiseListener(this).register();
    }

    public @NotNull Configuration getConfiguration() {
        return this.configuration;
    }

    public DisguiseGraceTracker getGraceTracker() {
        return this.graceTracker;
    }
}
