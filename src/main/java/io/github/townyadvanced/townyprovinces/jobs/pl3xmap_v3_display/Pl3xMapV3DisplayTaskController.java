package io.github.townyadvanced.townyprovinces.jobs.pl3xmap_v3_display;
import io.github.townyadvanced.townyprovinces.TownyProvinces;

public class Pl3xMapV3DisplayTaskController {

	private static Pl3xMapV3DisplayTask pl3xMapDisplayTask = null;
	private static boolean bordersRefreshRequested;
	private static boolean homeBlocksRefreshRequested;
	public static boolean startTask() {
		if (pl3xMapDisplayTask != null) {
			TownyProvinces.severe("Pl3xMap Display Job already started");
			return false;
		} else {
			TownyProvinces.info("Pl3xMap Display Job Starting");
			bordersRefreshRequested = true;
			homeBlocksRefreshRequested = true;
			pl3xMapDisplayTask = new Pl3xMapV3DisplayTask();
			pl3xMapDisplayTask.runTaskTimerAsynchronously(TownyProvinces.getPlugin(), 40, 300);
			TownyProvinces.info("Pl3xMap Display Job Started");
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
		if(pl3xMapDisplayTask != null) {
			pl3xMapDisplayTask.cancel();
			pl3xMapDisplayTask = null;
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
