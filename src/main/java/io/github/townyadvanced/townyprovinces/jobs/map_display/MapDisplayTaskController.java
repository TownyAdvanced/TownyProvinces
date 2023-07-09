package io.github.townyadvanced.townyprovinces.jobs.map_display;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;

import java.util.ArrayList;
import java.util.List;

public class MapDisplayTaskController {
	private static final List<DisplayProvincesOnMapAction> mapDisplayActions = new ArrayList<>();
	private static MapDisplayTask mapDisplayTask = null;
	private static boolean bordersRefreshRequested;
	private static boolean homeBlocksRefreshRequested;
	public static boolean startTask() {
		if (mapDisplayTask != null) {
			TownyProvinces.severe("Map Display Job already started");
			return false;
		} else {
			TownyProvinces.info("Map Display Job Starting");
			bordersRefreshRequested = true;
			homeBlocksRefreshRequested = true;
			mapDisplayTask = new MapDisplayTask();
			mapDisplayTask.runTaskTimerAsynchronously(TownyProvinces.getPlugin(), 40, 300);
			TownyProvinces.info("Map Display Job Started");
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
		if(mapDisplayTask != null) {
			mapDisplayTask.cancel();
			mapDisplayTask = null;
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
	
	public static boolean isMapSupported() {
		return mapDisplayActions.size() > 0;
	}
	
	public static void enableDynmap() {
		//TODO: Register the icon as an OPTION, let the class check settings to use or not
		mapDisplayActions.add(new DisplayProvincesOnDynmapAction());
	}
	
	public static void enablePl3xMapV3() {
		if (TownyProvincesSettings.getTownCostsIcon() == null) {
			TownyProvinces.severe("Error: Town Costs Icon is not valid. Unable to support Pl3xMap V3.");
			return;
		}

		Pl3xMap.api().getIconRegistry().register(new IconImage(
			"provinces_costs_icon", TownyProvincesSettings.getTownCostsIcon(), "png"));
		
		mapDisplayActions.add(new DisplayProvincesOnPl3xMapV3Action());
	}
	
	static List<DisplayProvincesOnMapAction> getMapDisplayActions() {
		return mapDisplayActions;
	}

}
