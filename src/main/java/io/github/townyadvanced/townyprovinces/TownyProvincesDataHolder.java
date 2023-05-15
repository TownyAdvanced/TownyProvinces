package io.github.townyadvanced.townyprovinces;

import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.Region;
import io.github.townyadvanced.townyprovinces.objects.TPChunk;

import java.util.List;
import java.util.Map;

/**
 * The In-Memory data holder class for TownyProvinces
 */
public class TownyProvincesDataHolder {
	
	//Singleton
	private static TownyProvincesDataHolder dataHolder = null;

	private TownyProvincesDataHolder() {
	}
	
	public static TownyProvincesDataHolder getTownyProvincesDataHolder() {
		if(dataHolder == null) {
			dataHolder = new TownyProvincesDataHolder();
		}
		return dataHolder;
	}
	private List<Province> provincesList;
	private List<Region> regionsList;
	private Map<WorldCoord, TPChunk> tpChunksMap;
	
	
}
