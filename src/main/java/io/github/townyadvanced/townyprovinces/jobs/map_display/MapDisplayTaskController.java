package io.github.townyadvanced.townyprovinces.jobs.map_display;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

import java.util.ArrayList;
import java.util.List;

public class MapDisplayTaskController {
	private static final List<DisplayProvincesOnMapAction> mapDisplayActions = new ArrayList<>();
	private static MapDisplayTask mapDisplayTask = null;
	private static boolean fullProvinceColoursRefreshRequested;
	private static boolean fullHomeBlockIconsRefreshRequest;
	public static boolean startTask() {
		if (mapDisplayTask != null) {
			TownyProvinces.severe("Map Display Job already started");
			return false;
		} else {
			TownyProvinces.info("Map Display Job Starting");
			fullProvinceColoursRefreshRequested = true;
			fullHomeBlockIconsRefreshRequest = true;
			mapDisplayTask = new MapDisplayTask();
			TownyProvinces.getPlugin().getScheduler().runAsyncRepeating(mapDisplayTask, 40, TownyProvincesSettings.getMapRefreshPeriodMilliseconds() * 20);
			TownyProvinces.info("Map Display Job Started");
			return true;
		}
	}
	
	public static void reloadIntegrations() {
		
		mapDisplayTask.cancel();
		synchronized (TownyProvinces.MAP_DISPLAY_JOB_LOCK) {
			synchronized (TownyProvinces.REGION_REGENERATION_JOB_LOCK) {
				synchronized (TownyProvinces.PRICE_RECALCULATION_JOB_LOCK) {
					for (DisplayProvincesOnMapAction mapDisplayAction : getMapDisplayActions()) {
						mapDisplayAction.reloadAction();
					}
				}
			}
		}
		requestFullMapRefresh();
		mapDisplayTask = new MapDisplayTask();
		TownyProvinces.getPlugin().getScheduler().runAsyncRepeating(mapDisplayTask, 40, TownyProvincesSettings.getMapRefreshPeriodMilliseconds() * 20);
	}

	public static void requestFullMapRefresh() {
		fullProvinceColoursRefreshRequested = true;
		fullHomeBlockIconsRefreshRequest = true;
	}

	public static void requestHomeBlocksRefresh() {
		fullHomeBlockIconsRefreshRequest = true;
	}

	public static void endTask() {
		if(mapDisplayTask != null) {
			mapDisplayTask.cancel();
			mapDisplayTask = null;
		}
	}

	static void setFullProvinceColoursRefreshRequested(boolean b) {
		fullProvinceColoursRefreshRequested = b;
	}

	static void setFullHomeBlockIconsRefreshRequest(boolean b) {
		fullHomeBlockIconsRefreshRequest = b;
	}
	
	static boolean getFullProvinceColoursRefreshRequested() {
		return fullProvinceColoursRefreshRequested;
	}

	static boolean getFullHomeBlockIconsRefreshRequest() {
		return fullHomeBlockIconsRefreshRequest;
	}
	
	public static boolean isMapSupported() {
		return mapDisplayActions.size() > 0;
	}
	
	public static void addMapDisplayAction(DisplayProvincesOnMapAction action) {
		mapDisplayActions.add(action);
	}
	
	static List<DisplayProvincesOnMapAction> getMapDisplayActions() {
		return mapDisplayActions;
	}

}
