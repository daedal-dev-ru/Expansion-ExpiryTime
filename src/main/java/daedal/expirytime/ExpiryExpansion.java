package daedal.expirytime;

import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExpiryExpansion extends PlaceholderExpansion implements Configurable {
    private final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    private final LuckPerms lp;
    {
        assert provider != null;
        lp = provider.getProvider();
    }
    private final UserManager userManager = lp.getUserManager();

    @Override
    public @NotNull String getIdentifier() {
        return "expirytime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Daedal";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2";
    }

    @Override
    public Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("numbers_color", "&7");
        defaults.put("never", "&6∞");
        defaults.put("days", "&6 дн.");
        defaults.put("hours", "&6 ч.");
        defaults.put("minutes", "&6 мин.");
        return defaults;
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String prompt) {
        if (p == null) return null;
        Collection<Node> nodes = Objects.requireNonNull(userManager.getUser(p.getUniqueId())).getNodes();
        String invalid_group = this.getString("invalid_group", "&cНЕИЗВЕСТНАЯ ГРУППА");
        String numbers_color = this.getString("numbers_color", "&7");
        String never = this.getString("never", "&6∞") ;
        String days = this.getString("days", "&6 дн.");
        String hours = this.getString("hours", "&6 ч.");
        String minutes = this.getString("minutes", "&6 мин.");
        String[] params = prompt.split("_");
        String permission;
        switch (params[0]) {
            case "auto":
                permission = "group." + Objects.requireNonNull(this.userManager.getUser(p.getUniqueId())).getPrimaryGroup();
                break;
            case "group":
                permission = "group." + params[1];
                if (!this.lp.getGroupManager().getLoadedGroups().contains(this.lp.getGroupManager().getGroup(params[1]))) {
                    return invalid_group;
                }
                break;
            case "permission":
                permission = params[1];
                break;
            default:
                permission = null;
        }
        for (Node node : nodes) {
            if (node.getKey().equals(permission)) {
                if (!node.hasExpiry()) {
                    return never;
                }
                Duration dur = node.getExpiryDuration();
                assert dur != null;
                double toDays = dur.toDays();
                double toHours = dur.toHours();
                double toMinutes = dur.toMinutes();
                if (toDays < 1.0) {
                    if (toHours < 1.0) {
                        return numbers_color + (int) toMinutes + minutes;
                    }
                    else {
                        return numbers_color + (int) toHours + hours;
                    }
                }
                else {
                    return numbers_color + (int) toDays + days;
                }
            }
        }
        return null;
    }
}
