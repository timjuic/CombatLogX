package combatlogx.expansion.compatibility.idisguise;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;

import com.github.sirblobman.combatlogx.api.expansion.disguise.DisguiseHandler;

import de.luisagrether.idisguise.api.EventCancelledException;
import de.luisagrether.idisguise.iDisguise;

public final class DisguiseHandler_iDisguise extends DisguiseHandler<Expansion_iDisguise> {
    public DisguiseHandler_iDisguise(@NotNull Expansion_iDisguise expansion) {
        super(expansion);
    }

    @Override
    public boolean hasDisguise(@NotNull Player player) {
        iDisguise plugin = iDisguise.getInstance();
        if (!plugin.isDisguised(player)) return false;

        Expansion_iDisguise expansion = getExpansion();
        DisguiseGraceTracker grace = expansion.getGraceTracker();
        long graceMillis = expansion.getConfiguration().getGraceMillis();
        if (grace != null && grace.isInGracePeriod(player, graceMillis)) {
            return false;
        }

        return true;
    }

    @Override
    public void removeDisguise(@NotNull Player player) {
        iDisguise plugin = iDisguise.getInstance();
        try {
            plugin.undisguise(player);
        } catch (EventCancelledException ex) {
            // Another listener cancelled the undisguise; nothing to do.
        }
    }
}
