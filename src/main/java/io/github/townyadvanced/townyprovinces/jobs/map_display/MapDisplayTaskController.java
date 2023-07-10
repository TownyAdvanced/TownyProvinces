package io.github.townyadvanced.townyprovinces.jobs.map_display;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapDisplayTaskController {
	private static final List<DisplayProvincesOnMapAction> mapDisplayActions = new ArrayList<>();
	private static MapDisplayTask mapDisplayTask = null;
	private static boolean fullProvinceColoursRefreshRequested;
	private static boolean fullHomeBlockIconsRefreshRequest;
	private static Set<Province> provinceColourRefreshRequests = new HashSet<>(); //Reset the colours of individual provinces
	private static Set<Province> homeBlockIconRefreshRequests = new HashSet<>(); //Reset the home block icons of individual provinces

	public static boolean startTask() {
		if (mapDisplayTask != null) {
			TownyProvinces.severe("Map Display Job already started");
			return false;
		} else {
			TownyProvinces.info("Map Display Job Starting");
			fullProvinceColoursRefreshRequested = true;
			fullHomeBlockIconsRefreshRequest = true;
			provinceColourRefreshRequests = new HashSet<>();
			homeBlockIconRefreshRequests = new HashSet<>();
			mapDisplayTask = new MapDisplayTask();
			mapDisplayTask.runTaskTimerAsynchronously(TownyProvinces.getPlugin(), 40, 300);
			TownyProvinces.info("Map Display Job Started");
			return true;
		}
	}

	public static void requestFullMapRefresh() {
		fullProvinceColoursRefreshRequested = true;
		fullHomeBlockIconsRefreshRequest = true;
	}

	public static void requestProvinceColourRefreshForTown(Town town) {
		if(!town.hasHomeBlock()) {
			return;
		}
		WorldCoord worldCoord = town.getHomeBlockOrNull().getWorldCoord();
		if(!TownyProvincesSettings.getWorld().equals(worldCoord)) {
			return;
		}
		Province province = TownyProvincesDataHolder.getInstance().getProvinceAtWorldCoord(worldCoord);
		provinceColourRefreshRequests.add(province);
	}

	public static void requestProvinceColourResetsForNation(Nation nation) {
		for(Town town: nation.getTowns()) {
			requestProvinceColourRefreshForTown(town);
		}
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
