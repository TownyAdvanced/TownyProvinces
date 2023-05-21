package io.github.townyadvanced.townyprovinces.data;

import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.Region;
import io.github.townyadvanced.townyprovinces.objects.TPChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The In-Memory data holder class for TownyProvinces
 */
public class TownyProvincesDataHolder {
	
	//Singleton
	private static TownyProvincesDataHolder dataHolder = null;

	private TownyProvincesDataHolder() {
		provinces = new ArrayList<>();
		regions = new ArrayList<>();
		tpChunks = new HashMap<>();
	}
	
	public static TownyProvincesDataHolder getInstance() {
		return dataHolder;
	}
	
	public static boolean initialize() {
		dataHolder = new TownyProvincesDataHolder();
		return true;
	}
	
	private List<Province> provinces;
	private List<Region> regions;
	private Map<WorldCoord, TPChunk> tpChunks;

	public void addProvince(Province province) {
		provinces.add(province);
	}


	public int getNumProvinces() {
		return provinces.size();
	}

	public List<Province> getProvinces() {
		return new ArrayList<>(provinces);
	}
}
