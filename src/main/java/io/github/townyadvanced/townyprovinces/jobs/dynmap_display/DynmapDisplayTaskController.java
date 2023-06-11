package io.github.townyadvanced.townyprovinces.jobs.dynmap_display;
import io.github.townyadvanced.townyprovinces.TownyProvinces;

public class DynmapDisplayTaskController {

	private static DynmapDisplayTask dynmapDisplayTask = null;
	private static boolean bordersRefreshRequested;
	private static boolean homeBlocksRefreshRequested;
	public static boolean startTask() {
		if (dynmapDisplayTask != null) {
			TownyProvinces.severe("Dynmap Display Job already started");
			return false;
		} else {
			TownyProvinces.info("Dynmap Display Job Starting");
			bordersRefreshRequested = true;
			homeBlocksRefreshRequested = true;
			dynmapDisplayTask = new DynmapDisplayTask();
			dynmapDisplayTask.runTaskTimerAsynchronously(TownyProvinces.getPlugin(), 40, 300);
			TownyProvinces.info("Dynmap Display Job Started");
			return true;
		}
	}

	public static void requestFullMapRefresh() {
		bordersRefreshRequested = true;
		homeBlocksRefreshRequested = true;
	}

	public static void requestHomeBlocksRefresh() {
		homeBlocksRefreshRequested = true;
	}

	public static void endTask() {
		if(dynmapDisplayTask != null) {
			dynmapDisplayTask.cancel();
			dynmapDisplayTask = null;
		}
	}

	static void setBordersRefreshRequested(boolean b) {
		bordersRefreshRequested = b;
	}

	static void setHomeBlocksRefreshRequested(boolean b) {
		homeBlocksRefreshRequested = b;
	}
	
	static boolean getBordersRefreshRequested() {
		return bordersRefreshRequested;
	}

	static boolean getHomeBlocksRefreshRequested() {
		return homeBlocksRefreshRequested;
	}

}
