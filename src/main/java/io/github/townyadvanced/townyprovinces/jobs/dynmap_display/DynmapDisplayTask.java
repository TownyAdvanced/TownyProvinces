package io.github.townyadvanced.townyprovinces.jobs.dynmap_display;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import org.bukkit.scheduler.BukkitRunnable;

public class DynmapDisplayTask extends BukkitRunnable {
    private final DisplayProvincesOnDynmapAction dynmapDisplayMapAction;

    DynmapDisplayTask() {
        this.dynmapDisplayMapAction = new DisplayProvincesOnDynmapAction();
    }

    public void run() {
		try {
			synchronized (TownyProvinces.MAP_CHANGE_LOCK) {
				dynmapDisplayMapAction.executeAction(DynmapDisplayTaskController.getBordersRefreshRequested(), DynmapDisplayTaskController.getHomeBlocksRefreshRequested());
			}
		} finally {
			//Reset refresh flags
			DynmapDisplayTaskController.setBordersRefreshRequested(false);
			DynmapDisplayTaskController.setHomeBlocksRefreshRequested(false);
		}
	}
}