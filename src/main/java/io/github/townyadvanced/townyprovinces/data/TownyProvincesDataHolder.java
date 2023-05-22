package io.github.townyadvanced.townyprovinces.data;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.Region;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;

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
		provinceBlocks = new HashMap<>();
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
	private Map<Coord, ProvinceBlock> provinceBlocks;

	public void addProvince(Province province) {
		provinces.add(province);
	}


	public int getNumProvinces() {
		return provinces.size();
	}

	public List<Province> getProvinces() {
		return new ArrayList<>(provinces);
	}

	public void addProvinceBlock(Coord coord, ProvinceBlock provinceBlock) {
		provinceBlocks.put(coord, provinceBlock);
	}

	public Map<Coord, ProvinceBlock> getProvinceBlocks() {
		return provinceBlocks;
	}

	public ProvinceBlock getProvinceBlock(Coord coord) {
		return provinceBlocks.get(coord);
	}
}
