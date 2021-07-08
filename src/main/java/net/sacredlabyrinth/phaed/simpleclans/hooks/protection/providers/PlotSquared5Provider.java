package net.sacredlabyrinth.phaed.simpleclans.hooks.protection.providers;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import net.sacredlabyrinth.phaed.simpleclans.hooks.protection.Land;
import net.sacredlabyrinth.phaed.simpleclans.hooks.protection.ProtectionProvider;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class PlotSquared5Provider implements ProtectionProvider {

    private Method getPlot;
    private Method getPlots;
    private Method getPlotsUUID;

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Override
    public void setup() {
        try {
            getPlot = BukkitUtil.class.getMethod("getPlot", Location.class);
            getPlots = PlotSquared.class.getMethod("getPlots", String.class);
            getPlotsUUID = PlotSquared.class.getMethod("getPlots", String.class, UUID.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public @NotNull Set<Land> getLandsAt(@NotNull Location location) {
        Plot plot = getPlot(location);
        if (plot != null) {
            return Collections.singleton(getLand(plot));
        }
        return Collections.emptySet();
    }

    @Override
    public @NotNull Set<Land> getLandsOf(@NotNull OfflinePlayer player, @NotNull World world) {
        Set<Plot> plots = getPlots(world.getName(), player.getUniqueId());
        return plots.stream().map(this::getLand).collect(Collectors.toSet());
    }

    @Override
    public @NotNull String getIdPrefix() {
        return "ps";
    }

    @Override
    public void deleteLand(@NotNull String id, @NotNull World world) {
        id = id.replace(getIdPrefix(), "");
        for (Plot plot : getPlots(world.getName())) {
            if (plot.getId().toString().equals(id)) {
                plot.unclaim();
                break;
            }
        }
    }

    @Override
    public @Nullable Class<? extends Event> getCreateLandEvent() {
        return null;
    }

    @Override
    public @Nullable Player getPlayer(Event event) {
        return null;
    }

    @Override
    public @Nullable String getRequiredPluginName() {
        return "PlotSquared";
    }

    @NotNull
    private Land getLand(@NotNull Plot plot) {
        return new Land(getIdPrefix() + plot.getId(), plot.getOwners());
    }

    private @Nullable Plot getPlot(Location location) {
        try {
            return (Plot) getPlot.invoke(null, location);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private Set<Plot> getPlots(String world, UUID owner) {
        try {
            //noinspection unchecked
            return (Set<Plot>) getPlotsUUID.invoke(PlotSquared.get(), world, owner);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return Collections.emptySet();
        }
    }

    private Set<Plot> getPlots(String world) {
        try {
            //noinspection unchecked
            return (Set<Plot>) getPlots.invoke(PlotSquared.get(), world);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return Collections.emptySet();
        }
    }
}
