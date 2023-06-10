package io.github.townyadvanced.townyprovinces.data;

import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
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
	 * Coord province map
	 * 
	 * To get a province at a given location, search the map
	 * To get a border at a given location, seach the map
	 *   If it doesn't have the given coord, then its a border
	 */
	private final Map<TPCoord, Province> coordProvinceMap;
	
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
	private final TPCoord coordSearchKey;
	
	private TownyProvincesDataHolder() {
		provincesSet = new HashSet<>();
		coordProvinceMap = new HashMap<>();
		coordSearchKey = new TPCoord(0,0);
	}
	
	public static TownyProvincesDataHolder getInstance() {
		return dataHolder;
	}
	
	public static boolean initialize() {
		dataHolder = new TownyProvincesDataHolder();
		return true;
	}

	public List<TPCoord> getCoordsInProvince(Province province) {
		List<TPCoord> result = new ArrayList<>();
		for(Map.Entry<TPCoord,Province> mapEntry: coordProvinceMap.entrySet()) {
			if(mapEntry.getValue() == province) {
				result.add(mapEntry.getKey());
			}
		}
		return result;
	}
	
	public void addProvince(Province province) {
		provincesSet.add(province);
	}
	
	public int getNumProvinces() {
		return provincesSet.size();
	}
	
	public List<Province> getCopyOfProvincesSetAsList() {
		return new ArrayList<>(provincesSet);
	}

	public void claimCoordForProvince(TPCoord coord, Province province) {
		coordProvinceMap.put(coord, province);
	}

	public Map<TPCoord, Province> getCoordProvinceMap() {
		return coordProvinceMap;
	}
	
	public Province getProvinceAt(int x, int z) {
		coordSearchKey.setValues(x,z);
		return coordProvinceMap.get(coordSearchKey);
	}

	public Set<Province> getProvincesSet() {
		return provincesSet;
	}


	public void deleteProvince(Province province) {
		List<TPCoord> coordsInProvince = province.getCoordsInProvince();
		for(TPCoord coord: coordsInProvince) {
			coordProvinceMap.remove(coord);
		}
		provincesSet.remove(province);
	}

	public void deleteAllProvinces() {
		provincesSet.clear();
		coordProvinceMap.clear();
	}

	public Set<TPCoord> findAdjacentBorderCoords(TPCoord targetCoord) {
		Set<TPCoord> result = new HashSet<>();
		int[] x = new int[]{-1,0,1,-1,1,-1,0,1};
		int[] z = new int[]{-1,-1,-1,0,0,1,1,1};
		TPCoord coord;
		for(int i = 0; i < 8; i++) {
			coord = getBorderCoord(targetCoord.getX() + x[i], targetCoord.getZ() + z[i]);
			if(coord != null)
				result.add(coord);
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
		coordSearchKey.setValues(x,z);
		return !coordProvinceMap.containsKey(coordSearchKey);
	}
	

	public Map<TPCoord, TPCoord> getAllUnclaimedCoordsInRegion(String regionName) {
		Map<TPCoord,TPCoord> result = new HashMap<>();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		//Add to the result, any coords in the area which are not claimed
		Map<TPCoord,Province> coordProvinceMap = TownyProvincesDataHolder.getInstance().getCoordProvinceMap();
		TPCoord searchKey = new TPCoord(0,0);
		TPCoord newCoord;
		for(int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				searchKey.setValues(x,z);
				if(!coordProvinceMap.containsKey(searchKey)) {
					newCoord = new TPCoord(x,z);
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
		Map<TPCoord,TPCoord> result = new HashMap<>();
		String regionName = TownyProvincesSettings.getNameOfFirstRegion();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		
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
		TPCoord searchKey = new TPCoord(0,0);
		TPCoord newCoord;
		for(int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				searchKey.setValues(x,z);
				if(!result.containsKey(searchKey)) {
					newCoord = new TPCoord(x,z);
					result.put(newCoord, newCoord);
				}
			}
		}
		
		//Return result
		return result;
	}
}
