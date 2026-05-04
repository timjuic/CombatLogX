package combatlogx.expansion.compatibility.idisguise;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.luisagrether.idisguise.api.PlayerDisguiseEvent;
import de.luisagrether.idisguise.api.PlayerUndisguiseEvent;

public final class DisguiseGraceTracker implements Listener {
    private final Map<UUID, Long> disguiseStartMillis = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisguise(PlayerDisguiseEvent event) {
        Player player = event.getPlayer();
        this.disguiseStartMillis.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUndisguise(PlayerUndisguiseEvent event) {
        this.disguiseStartMillis.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.disguiseStartMillis.remove(event.getPlayer().getUniqueId());
    }

    public boolean isInGracePeriod(@NotNull Player player, long graceMillis) {
        if (graceMillis <= 0L) return false;
        Long start = this.disguiseStartMillis.get(player.getUniqueId());
        if (start == null) return false;
        return (System.currentTimeMillis() - start) < graceMillis;
    }
}
