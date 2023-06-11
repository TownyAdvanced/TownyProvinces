package io.github.townyadvanced.townyprovinces.integrations.dynmap;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.province_generation.RegionRegenerateJob;
import org.bukkit.scheduler.BukkitRunnable;

public class DynmapTask extends BukkitRunnable {
    private final DynmapIntegration dynmapIntegration;

    DynmapTask(DynmapIntegration dynmapIntegration) {
        this.dynmapIntegration = dynmapIntegration;
    }

    public void run() {
		synchronized (RegionRegenerateJob.REGENERATION_JOB_LOCK) {
			dynmapIntegration.displayTownyProvinces();
		}
    }
}
