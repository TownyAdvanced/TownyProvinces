package io.github.townyadvanced.townyprovinces.integrations.dynmap;

import org.bukkit.scheduler.BukkitRunnable;

public class DynmapTask extends BukkitRunnable {
    private final DynmapIntegration dynmapIntegration;

    DynmapTask(DynmapIntegration dynmapIntegration) {
        this.dynmapIntegration = dynmapIntegration;
    }

    public void run() {
        dynmapIntegration.displayTownyProvinces();
    }
}
