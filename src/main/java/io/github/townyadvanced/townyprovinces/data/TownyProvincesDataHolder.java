package io.github.townyadvanced.townyprovinces.data;

import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFinalCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The In-Memory data holder class for TownyProvinces
 */
public class TownyProvincesDataHolder {

	/**
	 * Singleton for this class
	 */
	private static TownyProvincesDataHolder dataHolder = null;
	
	/**
	 * Set of provinces
	 */
	private final Set<Province> provincesSet;


	/**
	 * 
	 *
	 1. townyProvincePortCoord, <Coord>

	 2. townyProvincesJumpHubCoord, <Coord>

	 3. townyProvincesRoadDestinations,  [<Coord>, <Coord>, <Coord>]

	 * 
	 * 
	 * 
	 */
	/**
	 * Coord province map
	 * 
	 * To get a province at a given location, search the map
	 * To get a border at a given location, seach the map
	 *   If it doesn't have the given coord, then its a border
	 */
	private final Map<TPCoord, Province> coordProvinceMap;

	/**
	 * Map of towns to their ports
	 * This map only lives in memory
	 */
	private final Map<Town, TPCoord> townPortMap;


	/**
	 * Map of towns to their jump hubs
	 * This map only lives in memory
	 */
	private final Map<Town, TPCoord> townJumpHubMap;

	/**
	 * Static Coord Search key
	 * 
	 * To use this
	 * 1. First ensure your thread is safe vis synchronization
	 * 2. Then modify the key with the coords you want
	 * 3. Then use the key to search the coord-province map
	 * 
	 * The advantage of this technique is that no new object
	 * needs to be created to do a search of the map
	 */
	private final TPFreeCoord searchCoord;
	
	private TownyProvincesDataHolder() {
		searchCoord = new TPFreeCoord(0,0);
		provincesSet = new HashSet<>();
		coordProvinceMap = new HashMap<>();
		townPortMap = new HashMap<>();
		townJumpHubMap = new HashMap<>();
	}
	
	public static TownyProvincesDataHolder getInstance() {
		return dataHolder;
	}
	
	public static boolean initialize() {
		dataHolder = new TownyProvincesDataHolder();
		return true;
	}

	public List<TPCoord> getListOfCoordsInProvince(Province province) {
		List<TPCoord> result = new ArrayList<>();
		for(Map.Entry<TPCoord,Province> mapEntry: coordProvinceMap.entrySet()) {
			if(mapEntry.getValue().equals(province)) {
				result.add(mapEntry.getKey());
			}
		}
		return result;
	}
	
	public void addProvince(Province province) {
		provincesSet.add(province);
	}
	
	public void claimCoordForProvince(TPCoord coord, Province province) {
		coordProvinceMap.put(coord, province);
	}

	public Map<TPCoord, Province> getCoordProvinceMap() {
		return coordProvinceMap;
	}
	
	public Province getProvinceAt(int x, int z) {
		searchCoord.setValues(x,z);
		return coordProvinceMap.get(searchCoord);
	}

	public Set<Province> getProvincesSet() {
		return provincesSet;
	}


	public void deleteProvince(Province province, Map<TPCoord,TPCoord> unclaimedCoordsMap) {
		List<TPCoord> coordsInProvince = province.getListOfCoordsInProvince();
		for(TPCoord coord: coordsInProvince) {
			coordProvinceMap.remove(coord);
			unclaimedCoordsMap.put(coord,coord);
		}
		provincesSet.remove(province);
	}

	public Set<TPCoord> findAdjacentBorderCoords(TPCoord targetCoord) {
		Set<TPCoord> result = new HashSet<>();
		int[] x = new int[]{-1,0,1,-1,1,-1,0,1};
		int[] z = new int[]{-1,-1,-1,0,0,1,1,1};
		for(int i = 0; i < 8; i++) {
			if(isCoordUnclaimed(targetCoord.getX() + x[i], targetCoord.getZ() + z[i])) {
				/*
				 * Adjacent border coord found
				 * If the result set does not already contain this the coord, create & add it
				 */
				searchCoord.setValues(targetCoord.getX() + x[i], targetCoord.getZ() + z[i]);
				if(!result.contains(searchCoord)) {
					result.add(new TPFinalCoord(targetCoord.getX() + x[i], targetCoord.getZ() + z[i]));
				}
			}
		}
		return result;
	}

	/**
	 * Check if the given coord is unclaimed
	 *
	 * @param x x
	 * @param z z
	 *             
	 * @return true if the coord is unclaimed
	 */
	public boolean isCoordUnclaimed(int x, int z) {
		searchCoord.setValues(x,z);
		return !coordProvinceMap.containsKey(searchCoord);
	}

	public Map<TPCoord, TPCoord> getAllUnclaimedCoordsOnMap() {
		String nameOfFirstRegion = TownyProvincesSettings.getNameOfFirstRegion();
		return getAllUnclaimedCoordsInRegion(nameOfFirstRegion);
	}
	
	private Map<TPCoord, TPCoord> getAllUnclaimedCoordsInRegion(String regionName) {
		Map<TPCoord,TPCoord> result = new HashMap<>();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		//Add to the result, any coords in the area which are not claimed
		Map<TPCoord,Province> coordProvinceMap = TownyProvincesDataHolder.getInstance().getCoordProvinceMap();
		TPCoord newCoord;
		for(int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				searchCoord.setValues(x,z);
				if(!coordProvinceMap.containsKey(searchCoord)) {
					newCoord = new TPFinalCoord(x,z);
					result.put(newCoord, newCoord);
				}
			}
		}
		//Return result
		return result;
	}

	/**
	 * Get all the coords on the map,
	 * assuming that region file 1 specifies the dimensions of the full map
	 *
	 * This method will re-use any required coord objects which are already in the coord-province map.
	 */
	public Map<TPCoord,TPCoord> getAllCoordsOnMap() {
		TownyProvinces.info("Now Getting all coords on map");
		Map<TPCoord,TPCoord> result = new HashMap<>();
		String regionName = TownyProvincesSettings.getNameOfFirstRegion();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		
		//First add any required coords which are already on the coord-province map
		for(TPCoord coord: coordProvinceMap.keySet()) {
			if(coord.getX() < minX)
				continue;
			if(coord.getX() > maxX)
				continue;
			if(coord.getZ() < minZ)
				continue;
			if(coord.getZ() > maxZ)
				continue;
			result.put(coord, coord);
		}
		
		//Now create any required coords which are not already in the result
		TPCoord newCoord;
		for(int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				searchCoord.setValues(x,z);
				if(!result.containsKey(searchCoord)) {
					newCoord = new TPFinalCoord(x,z);
					result.put(newCoord, newCoord);
					if(result.size() % 10000 == 0) {
						TownyProvinces.info("Num Coords Available: " + result.size());
					}
				}
			}
		}
		
		//Return result
		return result;
	}
}
