package combatlogx.expansion.compatibility.idisguise;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionListener;

import de.luisagrether.idisguise.api.EventCancelledException;
import de.luisagrether.idisguise.iDisguise;

public final class DirectUndisguiseListener extends ExpansionListener {
    private final Expansion_iDisguise expansion;

    public DirectUndisguiseListener(@NotNull Expansion_iDisguise expansion) {
        super(expansion);
        this.expansion = expansion;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Configuration config = this.expansion.getConfiguration();
        if (!config.isUndisguiseOnDirectAttackMob() && !config.isUndisguiseOnDirectAttackedByMob()) {
            return;
        }

        Entity damager = e.getDamager();
        Entity damaged = e.getEntity();

        if (config.isUndisguiseOnDirectAttackMob()
                && damager instanceof Player
                && !(damaged instanceof Player)) {
            tryUndisguise((Player) damager);
        }

        if (config.isUndisguiseOnDirectAttackedByMob()
                && damaged instanceof Player
                && !(damager instanceof Player)) {
            tryUndisguise((Player) damaged);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnderpearlLand(PlayerTeleportEvent e) {
        if (!this.expansion.getConfiguration().isUndisguiseOnEnderpearlLand()) return;
        if (e.getCause() != TeleportCause.ENDER_PEARL) return;
        tryUndisguise(e.getPlayer());
    }

    private void tryUndisguise(@NotNull Player player) {
        DisguiseGraceTracker grace = this.expansion.getGraceTracker();
        if (grace.isInGracePeriod(player, this.expansion.getConfiguration().getGraceMillis())) {
            printDebug("Skipping direct undisguise for " + player.getName() + " (grace period active).");
            return;
        }

        iDisguise plugin = iDisguise.getInstance();
        if (!plugin.isDisguised(player)) return;

        try {
            plugin.undisguise(player);
            printDebug("Direct-undisguised " + player.getName() + ".");
            LanguageManager languageManager = getLanguageManager();
            languageManager.sendMessageWithPrefix(player, "expansion.disguise-compatibility.remove-disguise");
        } catch (EventCancelledException ex) {
            printDebug("Undisguise was cancelled for " + player.getName() + ".");
        }
    }
}
