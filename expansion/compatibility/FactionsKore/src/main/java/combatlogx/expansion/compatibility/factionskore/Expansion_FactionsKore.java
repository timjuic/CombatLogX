package combatlogx.expansion.compatibility.factionskore;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionWithDependencies;

public final class Expansion_FactionsKore extends ExpansionWithDependencies {
    private static final String DEPENDENCY_NAME = "FactionsKore";

    private final Configuration configuration;
    private DeferredEnableListener deferredEnableListener;

    public Expansion_FactionsKore(@NotNull ICombatLogX plugin) {
        super(plugin);
        this.configuration = new Configuration();
    }

    @Override
    public boolean checkDependencies() {
        // Don't require FactionsKore to be enabled yet: it softdepends on CombatLogX,
        // which forces CombatLogX (and us) to load first. We register on PluginEnableEvent below.
        return checkDependency(DEPENDENCY_NAME, false);
    }

    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
    }

    @Override
    public void onCheckedEnable() {
        reloadConfig();

        Plugin dependency = Bukkit.getPluginManager().getPlugin(DEPENDENCY_NAME);
        if (dependency != null && dependency.isEnabled()) {
            registerTagger();
            return;
        }

        this.deferredEnableListener = new DeferredEnableListener();
        JavaPlugin javaPlugin = getPlugin().getPlugin();
        Bukkit.getPluginManager().registerEvents(this.deferredEnableListener, javaPlugin);
        getLogger().info(DEPENDENCY_NAME + " not yet enabled; will register listener once it enables.");
    }

    @Override
    public void onCheckedDisable() {
        if (this.deferredEnableListener != null) {
            HandlerList.unregisterAll(this.deferredEnableListener);
            this.deferredEnableListener = null;
        }
    }

    @Override
    public void reloadConfig() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.reload("config.yml");
        YamlConfiguration yaml = configurationManager.get("config.yml");
        this.configuration.load(yaml);
    }

    public @NotNull Configuration getConfiguration() {
        return this.configuration;
    }

    private void registerTagger() {
        StackedEntityDeathTagger tagger = new StackedEntityDeathTagger(this);
        tagger.tryRegister();
    }

    private final class DeferredEnableListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(@NotNull PluginEnableEvent event) {
            Plugin enabled = event.getPlugin();
            if (!DEPENDENCY_NAME.equals(enabled.getName())) return;

            HandlerList.unregisterAll(this);
            Expansion_FactionsKore.this.deferredEnableListener = null;

            getLogger().info("Detected " + DEPENDENCY_NAME + " enable; registering listener now.");
            registerTagger();
        }
    }
}
