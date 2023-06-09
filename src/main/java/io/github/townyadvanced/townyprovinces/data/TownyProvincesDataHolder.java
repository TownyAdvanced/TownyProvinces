package io.github.townyadvanced.townyprovinces.data;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.objects.Province;

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
	//Attributes
	private Set<Province> provincesSet;
	private Map<Coord, Province> coordProvinceMap;
	
	private TownyProvincesDataHolder() {
		provincesSet = new HashSet<>();
		coordProvinceMap = new HashMap<>();
	}
	
	public static TownyProvincesDataHolder getInstance() {
		return dataHolder;
	}
	
	public static boolean initialize() {
		dataHolder = new TownyProvincesDataHolder();
		return true;
	}

	public List<Coord> getCoordsInProvince(Province province) {
		List<Coord> result = new ArrayList<>();
		for(Map.Entry<Coord,Province> mapEntry: coordProvinceMap.entrySet()) {
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

	public void claimCoordForProvince(Coord coord, Province province) {
		coordProvinceMap.put(coord, province);
	}

	public Map<Coord, Province> getCoordProvinceMap() {
		return coordProvinceMap;
	}

	public Province getProvinceAt(Coord coord) {
		return coordProvinceMap.get(coord);
	}

	public Set<Province> getProvincesSet() {
		return provincesSet;
	}


	public void deleteProvince(Province province) {
		List<Coord> coordsInProvince = province.getCoordsInProvince();
		for(Coord coord: coordsInProvince) {
			getCoordProvinceMap().remove(coord);
		}
		provincesSet.remove(province);
	}

	public void deleteAllProvinces() {
		provincesSet.clear();
		coordProvinceMap.clear();
	}

	public boolean isCoordClaimedByProvince(int x, int z) {
		for(Coord coord: coordProvinceMap.keySet()) {
			if(coord.getX() == x && coord.getZ() == z) {
				return true;
			}
		}
		return false;
	}
}
