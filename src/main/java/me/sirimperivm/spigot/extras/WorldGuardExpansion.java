package me.sirimperivm.spigot.extras;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.other.Logger;

@SuppressWarnings("all")
public class WorldGuardExpansion {

    private Main plugin;
    private Logger log;
    private WorldGuard api;

    private StateFlag kingdom_access;

    public WorldGuardExpansion(Main plugin) {
        this.plugin = plugin;
        log = plugin.getLog();

        api = WorldGuard.getInstance();
        FlagRegistry registry = api.getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("kingdom-access", true);
            registry.register(flag);
            kingdom_access = flag;
            log.success("[UltimateKingdoms] La flag \"kingdom-access\" è stata registrata con successo.");
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("kingdom-access");
            if (existing instanceof StateFlag) {
                kingdom_access = (StateFlag) existing;
            } else {
                log.fail("[UltimateKingdoms] Non è stato possibile registrare la flag \"kingdom-access\".");
                e.printStackTrace();
            }
        }
    }

    public WorldGuard getApi() {
        return api;
    }

    public StateFlag getKingdom_access() {
        return kingdom_access;
    }
}
