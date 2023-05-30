package io.github.townyadvanced.townyprovinces.data;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The In-Memory data holder class for TownyProvinces
 */
public class TownyProvincesDataHolder {
	
	//Singleton
	private static TownyProvincesDataHolder dataHolder = null;
	//Attributes
	private Set<Province> provinces;
	private Map<Coord, ProvinceBlock> provinceBlocks;
	
	private TownyProvincesDataHolder() {
		provinces = new HashSet<>();
		provinceBlocks = new HashMap<>();
	}
	
	public static TownyProvincesDataHolder getInstance() {
		return dataHolder;
	}
	
	public static boolean initialize() {
		dataHolder = new TownyProvincesDataHolder();
		return true;
	}

	public Set<ProvinceBlock> getProvinceBorderBlocks() {
		Set<ProvinceBlock> result = new HashSet<>();
		for(ProvinceBlock provinceBlock: getProvinceBlocks().values()) {
			if(provinceBlock.isProvinceBorder()) {
				result.add(provinceBlock);
			}
		}
		return result;
	}
	
	public void addProvince(Province province) {
		provinces.add(province);
	}


	public int getNumProvinces() {
		return provinces.size();
	}

	public List<Province> getCopyOfProvincesSetAsList() {
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

	public void deleteProvince(Province province) {
		provinces.remove(province);
	}

	public Province getProvince(UUID provinceUuid) {
		for(Province province: provinces) {
			if(province.getUuid().equals(provinceUuid)) {
				return province;
			}
		}
		return null;
	}

	public Set<Province> getProvinces() {
		return provinces;
	}
}
