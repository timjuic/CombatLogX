package combatlogx.expansion.compatibility.idisguise;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import com.github.sirblobman.combatlogx.api.expansion.ExpansionListener;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.TagType;

public final class DisguiseDamageListener extends ExpansionListener {
    public DisguiseDamageListener(@NotNull Expansion_iDisguise expansion) {
        super(expansion);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        ICombatManager combatManager = getCombatManager();
        Expansion_iDisguise expansion = (Expansion_iDisguise) getExpansion();
        DisguiseGraceTracker grace = expansion.getGraceTracker();
        long graceMillis = expansion.getConfiguration().getGraceMillis();

        Player disguisedDamager = resolveDisguisedPlayer(e.getDamager());
        if (disguisedDamager != null && !grace.isInGracePeriod(disguisedDamager, graceMillis)) {
            printDebug("Resolved damager disguise to player " + disguisedDamager.getName());
            combatManager.tag(disguisedDamager, e.getEntity(), TagType.PLAYER, TagReason.ATTACKER);
        }

        Player disguisedTarget = resolveDisguisedPlayer(e.getEntity());
        if (disguisedTarget != null && !grace.isInGracePeriod(disguisedTarget, graceMillis)) {
            printDebug("Resolved damaged disguise to player " + disguisedTarget.getName());
            combatManager.tag(disguisedTarget, e.getDamager(), TagType.PLAYER, TagReason.ATTACKED);

            // The base CombatLogX damage listener only tags player-vs-player. When the damaged
            // entity is a disguise mob, Bukkit sees a non-player on the receiving end, so the
            // attacker doesn't get tagged. Tag them ourselves so PvP feels symmetric.
            Player attackerPlayer = resolveAttackerPlayer(e.getDamager());
            if (attackerPlayer != null && !attackerPlayer.equals(disguisedTarget)) {
                printDebug("Tagging real attacker " + attackerPlayer.getName()
                        + " for hitting disguise of " + disguisedTarget.getName());
                combatManager.tag(attackerPlayer, disguisedTarget, TagType.PLAYER, TagReason.ATTACKER);
            }
        }
    }

    private @Nullable Player resolveAttackerPlayer(@NotNull Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }

    private @Nullable Player resolveDisguisedPlayer(@NotNull Entity entity) {
        if (!entity.hasMetadata("iDisguise")) {
            return null;
        }

        for (MetadataValue value : entity.getMetadata("iDisguise")) {
            Object raw = value.value();
            if (raw instanceof UUID) {
                Player player = Bukkit.getPlayer((UUID) raw);
                if (player != null && player.isOnline()) {
                    return player;
                }
            }
        }
        return null;
    }
}
