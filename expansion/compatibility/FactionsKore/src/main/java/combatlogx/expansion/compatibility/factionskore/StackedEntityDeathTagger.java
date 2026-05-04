package combatlogx.expansion.compatibility.factionskore;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.TagType;

/**
 * Bridges FactionsKore's custom {@code StackedEntityDeathEvent} into CombatLogX's tag system.
 * FactionsKore intercepts attacks via packets and never fires {@link org.bukkit.event.entity.EntityDamageByEntityEvent},
 * so the regular Mob Tagger flow never sees stacked-mob hits. We listen to the stacker's own death event instead
 * and tag the killer through {@link ICombatManager#tag}.
 *
 * <p>We pass {@code null} as the enemy and use {@link TagType#UNKNOWN} so CombatLogX's
 * {@code untag-on-enemy-death} path can't match the dying stack entity (which would cause tag/untag
 * spam on every kill). The dedicated language key {@code tagged.attacker.unknown} controls this
 * expansion's message; it isn't used elsewhere by default.
 */
public final class StackedEntityDeathTagger implements Listener {
    private static final String EVENT_CLASS_NAME = "com.golfing8.kore.event.StackedEntityDeathEvent";
    private static final String STACKED_ENTITY_CLASS_NAME = "com.golfing8.kore.object.StackedEntity";

    private final Expansion_FactionsKore expansion;
    private Method getKillerMethod;
    private Method getStackedEntityMethod;
    private Method getEntityTypeMethod;

    public StackedEntityDeathTagger(@NotNull Expansion_FactionsKore expansion) {
        this.expansion = expansion;
    }

    public void tryRegister() {
        Logger log = this.expansion.getLogger();
        Class<? extends Event> eventClass;
        try {
            Class<?> raw = Class.forName(EVENT_CLASS_NAME);
            if (!Event.class.isAssignableFrom(raw)) {
                log.warning(EVENT_CLASS_NAME + " is not a Bukkit Event; skipping registration.");
                return;
            }
            eventClass = raw.asSubclass(Event.class);

            this.getKillerMethod = eventClass.getMethod("getKiller");
            this.getStackedEntityMethod = eventClass.getMethod("getStackedEntity");
            this.getEntityTypeMethod = Class.forName(STACKED_ENTITY_CLASS_NAME).getMethod("getEntityType");
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            log.log(Level.WARNING, "FactionsKore stacker classes not found; expansion will be inactive.", ex);
            return;
        }

        ICombatLogX plugin = this.expansion.getPlugin();
        JavaPlugin javaPlugin = plugin.getPlugin();
        PluginManager pluginManager = Bukkit.getPluginManager();

        EventExecutor executor = (listener, event) -> handle(event);
        pluginManager.registerEvent(eventClass, this, EventPriority.NORMAL, executor, javaPlugin);
        log.info("Registered listener for " + EVENT_CLASS_NAME + ".");
    }

    private void handle(@NotNull Event event) {
        try {
            Object killerObj = this.getKillerMethod.invoke(event);
            if (!(killerObj instanceof Player)) return;
            Player killer = (Player) killerObj;

            Object stackedEntity = this.getStackedEntityMethod.invoke(event);
            if (stackedEntity == null) return;

            Object typeObj = this.getEntityTypeMethod.invoke(stackedEntity);
            if (!(typeObj instanceof EntityType)) return;
            EntityType entityType = (EntityType) typeObj;

            if (!this.expansion.getConfiguration().shouldTag(entityType)) return;

            ICombatLogX plugin = this.expansion.getPlugin();
            ICombatManager combatManager = plugin.getCombatManager();
            combatManager.tag(killer, null, TagType.UNKNOWN, TagReason.ATTACKER);
        } catch (ReflectiveOperationException ex) {
            this.expansion.getLogger().log(Level.WARNING, "Failed to handle " + EVENT_CLASS_NAME, ex);
        }
    }
}
