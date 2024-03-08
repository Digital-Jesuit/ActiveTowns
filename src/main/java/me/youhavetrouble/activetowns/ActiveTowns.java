package me.youhavetrouble.activetowns;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.time.dailytaxes.NewDayTaxAndUpkeepPreCollectionEvent;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ActiveTowns extends JavaPlugin implements Listener {

    private int daysInactive = 14;

    public final BooleanDataField safeFromDeletionMeta = new BooleanDataField("safeFromDeletion", false, "Is town safe from automatic deletion");

    @Override
    public void onEnable() {
        try {
            TownyAPI.getInstance().registerCustomDataField(safeFromDeletionMeta);
        } catch (KeyAlreadyRegisteredException e) {
            getLogger().severe("Could not register town metadata key. Disabling to prevent deleting towns marked as safe.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getCommandMap().register("activetowns", new ActiveTownsCommand(this));
    }

    private void reloadPlugin() {
        saveDefaultConfig();
        reloadConfig();
        this.daysInactive = Math.max(0, getConfig().getInt("days-inactive-to-remove", 14));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void newTownyDay(NewDayTaxAndUpkeepPreCollectionEvent event) {
        long oneDayTime = TownySettings.getNewDayTime();
        long inactiveTime = oneDayTime * daysInactive;
        long now = System.currentTimeMillis();
        Bukkit.getWorlds().forEach(world -> {
            TownyWorld townyWorld = TownyAPI.getInstance().getTownyWorld(world);
            if (townyWorld == null) return;
            getServer().getAsyncScheduler().runNow(this, (task -> {
                List<Town> townsToRemove = new ArrayList<>();
                for (Town town : townyWorld.getTowns().values()) {
                    if (!(town.getMetadata(safeFromDeletionMeta.getKey()) instanceof BooleanDataField isSafe)) continue; // invalid meta?
                    if (isSafe.getValue()) continue;
                    if (town.getResidents().isEmpty()) {
                        townsToRemove.add(town);
                        continue;
                    }
                    boolean townAbandoned = true;
                    for (Resident resident : town.getResidents()) {
                        UUID playerId = resident.getUUID();
                        OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(playerId);
                        long timeSinceLastSeen = now - offlinePlayer.getLastSeen();
                        if (timeSinceLastSeen > inactiveTime) continue;
                        townAbandoned = false;
                        break;
                    }
                    if (townAbandoned) {
                        townsToRemove.add(town);
                    }
                }
                townsToRemove.forEach(town -> {
                    try {
                        townyWorld.removeTown(town);
                    } catch (NotRegisteredException e) {
                        getLogger().warning("Could not remove town %s as it appears to be already removed".formatted(town.getName()));
                    }
                });
            }));
        });
    }

    public void makeTownImmuneFromDeletion(Town town, boolean immune) {
        if (!town.hasMeta("safeFromDeletion")) {
            town.addMetaData(safeFromDeletionMeta, true);
        }
        if (!(town.getMetadata(safeFromDeletionMeta.getKey()) instanceof BooleanDataField booleanDataField)) return;
        booleanDataField.setValue(immune);
    }

    public int getDaysInactive() {
        return daysInactive;
    }

}
