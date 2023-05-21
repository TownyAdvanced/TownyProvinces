package io.github.townyadvanced.townyprovinces.integrations.dynmap;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DynmapIntegration {

	private static final String TEMP_ICON = "fire";
	private final MarkerAPI markerapi;
	private BukkitTask dynmapTask;
	private final Map<UUID, Marker> townUUIDToSiegeMarkerMap = new HashMap<>();
	private MarkerSet markerSet;

	public DynmapIntegration() {
		DynmapAPI dynmapAPI = (DynmapAPI) TownyProvinces.getPlugin().getServer().getPluginManager().getPlugin("dynmap");
		markerapi = dynmapAPI.getMarkerAPI();
		addMarkerSet();
		registerDynmapTownyListener();
		startDynmapTask();
		TownyProvinces.info("Dynmap support enabled.");
	}

	private void addMarkerSet() {
		TownyProvinces plugin = TownyProvinces.getPlugin();

		if (markerapi == null) {
			TownyProvinces.severe("Error loading Dynmap marker API!");
			return;
		}

		//Create marker set
		markerSet = markerapi.getMarkerSet("townyprovinces.markerset");
		if (markerSet == null) {
			markerSet = markerapi.createMarkerSet("townyprovinces.markerset", plugin.getName(), null, false);
		} else {
			markerSet.setMarkerSetLabel(plugin.getName());
		}

		if (markerSet == null) {
			TownyProvinces.severe("Error creating Dynmap marker set!");
			return;
		}

		//Create battle banner marker icon
		//markerapi.createMarkerIcon(BATTLE_BANNER_ICON_ID, "BattleBanner", plugin.getResource(Settings.BATTLE_BANNER_FILE_NAME));
	}

	private void registerDynmapTownyListener() {
		TownyProvinces plugin = TownyProvinces.getPlugin();
		PluginManager pm = plugin.getServer().getPluginManager();
		if (pm.isPluginEnabled("Dynmap-Towny")) {
			TownyProvinces.info("TownyProvinces found Dynmap-Towny plugin, enabling Dynmap-Towny support.");
			pm.registerEvents(new DynmapTownyListener(), plugin);
		} else {
			TownyProvinces.info("Dynmap-Towny plugin not found.");
		}
	}

	public void startDynmapTask() {
		dynmapTask = new DynmapTask(this).runTaskTimerAsynchronously(TownyProvinces.getPlugin(), 40, 300);
	}

	public void endDynmapTask() {
		dynmapTask.cancel();
	}

	/**
	 * Display all TownyProvinces items, including province boundaries, region boundaries etc.
	 */
	void displayTownyProvinces() {
		Map<UUID, Marker> townUUIDToSiegeMarkerMapCopy = new HashMap<>(townUUIDToSiegeMarkerMap);

		{
			//Cleanup markers ....... This would involve activating/de-activating roads etc.
			//Region/Province borders probably don't need to be re-done on a task
			/*
            UUID townUUID = null;
            Marker marker = null;
            for (Map.Entry<UUID, Marker> mapEntry : townUUIDToSiegeMarkerMapCopy.entrySet()) {
                try {
                    marker = null;
                    townUUID = null;
                    townUUID = mapEntry.getKey();
                    marker = mapEntry.getValue();
                    Siege siege = SiegeController.getSiegeByTownUUID(townUUID);

                    if (siege == null || siege.getStatus() != SiegeStatus.IN_PROGRESS) {
                        //Delete marker if siege is not in progress
                        marker.deleteMarker();
                        townUUIDToSiegeMarkerMap.remove(townUUID);

                    } else if (marker.getMarkerIcon().getMarkerIconID().equals(TEMP_ICON)) {
                        /*
                         * Change to battle icon if siege is active.
                         */
			/*
                        if(!isSiegeDormant(siege))
                            marker.setMarkerIcon(markerapi.getMarkerIcon(BATTLE_BANNER_ICON_ID));                             
                    } else if (marker.getMarkerIcon().getMarkerIconID().equals(BATTLE_BANNER_ICON_ID)) {
                        /*
                         * Change to peaceful icon if siege is dormant
                         */
			
			/*
                        if (isSiegeDormant(siege))
                            marker.setMarkerIcon(markerapi.getMarkerIcon(TEMP_ICON));                      
                    }
                } catch (Exception e) {
                    if (marker != null)
                        marker.deleteMarker();
                    townUUIDToSiegeMarkerMap.remove(townUUID);
                }
            }
        }
	*/
			{
				//TEMP - Add markers showing province homeblocks

				for (Province province : TownyProvincesDataHolder.getInstance().getProvinces()) {

					String name = "test name";
					try {

						Coord homeBlock = province.getHomeBlock();
						int realHomeBlockX = homeBlock.getX() * TownySettings.getTownBlockSize();
						int realHomeBlockZ = homeBlock.getZ() * TownySettings.getTownBlockSize();

						MarkerIcon homeBlockIcon = markerapi.getMarkerIcon(TEMP_ICON);
						String homeBlockMarkerId = "id" + homeBlock.getX() + "-" + homeBlock.getZ();
						Marker homeBlockMarker = markerSet.findMarker(homeBlockMarkerId);
						if (homeBlockMarker == null) {
							homeBlockMarker = markerSet.createMarker(
								homeBlockMarkerId, name, TownyProvincesSettings.getWorldName(),
								realHomeBlockX, 64, realHomeBlockZ, homeBlockIcon, false);
							homeBlockMarker.setLabel(name);
							homeBlockMarker.setDescription("test description");
						}
					} catch (Exception ex) {
						TownyProvinces.severe("Problem adding homeblock marker");
						ex.printStackTrace();
					}
				}
			}
		}
	}
}
