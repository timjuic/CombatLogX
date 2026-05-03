package combatlogx.expansion.compatibility.idisguise;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.MetadataValue;

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

        Player disguisedDamager = resolveDisguisedPlayer(e.getDamager());
        if (disguisedDamager != null) {
            printDebug("Resolved damager disguise to player " + disguisedDamager.getName());
            combatManager.tag(disguisedDamager, e.getEntity(), TagType.PLAYER, TagReason.ATTACKER);
        }

        Player disguisedTarget = resolveDisguisedPlayer(e.getEntity());
        if (disguisedTarget != null) {
            printDebug("Resolved damaged disguise to player " + disguisedTarget.getName());
            combatManager.tag(disguisedTarget, e.getDamager(), TagType.PLAYER, TagReason.ATTACKED);
        }
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
