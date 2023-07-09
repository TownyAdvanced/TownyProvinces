package io.github.townyadvanced.townyprovinces.jobs.map_display;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerIcon;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
		if (TownyProvincesSettings.getTownCostsIcon() == null) {
			TownyProvinces.severe("Error: Town Costs Icon is not valid. Unable to support Dynmap.");
			return;
		}
		
		DynmapAPI dynmapAPI = (DynmapAPI) TownyProvinces.getPlugin().getServer().getPluginManager().getPlugin("dynmap");

		final MarkerIcon oldMarkerIcon = dynmapAPI.getMarkerAPI().getMarkerIcon("provinces_costs_icon");
		if (oldMarkerIcon != null) {
			oldMarkerIcon.deleteIcon();
		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(TownyProvincesSettings.getTownCostsIcon(), "png", outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		MarkerIcon markerIcon = dynmapAPI.getMarkerAPI().createMarkerIcon("provinces_costs_icon", 
			"provinces_costs_icon", inputStream);
		
		if (markerIcon == null) {
			TownyProvinces.severe("Error registering Town Costs Icon on Dynmap! Unable to support Dynmap.");
		}
		
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
	
	public static void addMapDisplayAction(DisplayProvincesOnMapAction action) {
		mapDisplayActions.add(action);
	}
	
	static List<DisplayProvincesOnMapAction> getMapDisplayActions() {
		return mapDisplayActions;
	}

}
