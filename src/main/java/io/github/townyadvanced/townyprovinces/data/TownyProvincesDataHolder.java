package io.github.townyadvanced.townyprovinces.data;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

import javax.annotation.Nullable;
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
	
	//Singleton
	private static TownyProvincesDataHolder dataHolder = null;
	private final Set<Province> provincesSet;
	private final Map<TPCoord, Province> coordProvinceMap;
	private final Map<TPCoord, TPCoord> unclaimedCoordsMap;
	private static final TPCoord coordSearchKey = new TPCoord(0,0);
	
	private TownyProvincesDataHolder() {
		provincesSet = new HashSet<>();
		coordProvinceMap = new HashMap<>();
		unclaimedCoordsMap = new HashMap<>();
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

	public synchronized Province getProvinceAt(Coord coord) {
		coordSearchKey.setValues(coord.getX(),coord.getZ());
		return coordProvinceMap.get(coordSearchKey);
	}
	
	public synchronized Province getProvinceAt(int x, int z) {
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

	public boolean isCoordClaimedByProvince(int x, int z) {
		coordSearchKey.setValues(x,z);
		return coordProvinceMap.containsKey(coordSearchKey);
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
	 * Get the required border cord
	 * @param x x
	 * @param z z
	 * @return the border coord if it is a border coord
	 */
	public synchronized @Nullable TPCoord getBorderCoord(int x, int z) {
		coordSearchKey.setValues(x,z);
		return unclaimedCoordsMap.getOrDefault(coordSearchKey, null);
	}

	/**
	 * This method is called 
	 * - after load
	 * - after reload,
	 * - At the start of a regenerate process
	 *
	 * It clears the unclaimed coords map,
	 * and then generates any coords in the map 
	 * which are not already in the coord-province map
	 *
	 */
	public boolean regenerateUnclaimedCoordsMap() {
		unclaimedCoordsMap.clear();
		String regionName = TownyProvincesSettings.getNameOfFirstRegion();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		TPCoord tpCoord;
		for(int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if (!isCoordClaimedByProvince(x, z)) {
					tpCoord = new TPCoord(x,z);
					unclaimedCoordsMap.put(tpCoord,tpCoord);
				}
			}
		}
		return true;
	}
	

	public Map<TPCoord, TPCoord> getUnclaimedCoordsMap() {
		return unclaimedCoordsMap;
	}
}
