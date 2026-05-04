package combatlogx.expansion.compatibility.factionskore;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import com.github.sirblobman.api.configuration.IConfigurable;

import static com.github.sirblobman.api.utility.ConfigurationHelper.parseEnums;

public final class Configuration implements IConfigurable {
    private final Set<EntityType> mobTypeSet;
    private boolean mobListInverted;

    public Configuration() {
        this.mobTypeSet = EnumSet.noneOf(EntityType.class);
        this.mobListInverted = false;
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        this.mobListInverted = config.getBoolean("mob-list-inverted", false);

        List<String> names = config.getStringList("mob-list");
        Set<EntityType> parsed = parseEnums(names, EntityType.class);
        this.mobTypeSet.clear();
        this.mobTypeSet.addAll(parsed);
    }

    public boolean shouldTag(@NotNull EntityType type) {
        boolean contains = this.mobTypeSet.contains(type);
        return this.mobListInverted != contains; // XOR: contained vs. inverted
    }
}
