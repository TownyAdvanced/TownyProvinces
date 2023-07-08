package io.github.townyadvanced.townyprovinces.jobs.pl3xmap_v3_display;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import org.bukkit.scheduler.BukkitRunnable;

public class Pl3xMapV3DisplayTask extends BukkitRunnable {
    private final DisplayProvincesOnPl3xMapV3Action pl3xMapDisplayMapAction;
	private boolean jobRunning;

    Pl3xMapV3DisplayTask() {
        this.pl3xMapDisplayMapAction = new DisplayProvincesOnPl3xMapV3Action();
		this.jobRunning = false;
    }

    public void run() {
		try {
			if(jobRunning) {
				return;
			} else {
				jobRunning = true;
			}
			synchronized (TownyProvinces.PL3XMAP_DISPLAY_LOCK) {
				pl3xMapDisplayMapAction.executeAction(Pl3xMapV3DisplayTaskController.getBordersRefreshRequested(), Pl3xMapV3DisplayTaskController.getHomeBlocksRefreshRequested());
			}
		} finally {
			//Reset refresh flags
			Pl3xMapV3DisplayTaskController.setBordersRefreshRequested(false);
			Pl3xMapV3DisplayTaskController.setHomeBlocksRefreshRequested(false);
			jobRunning = false;
		}
	}
}