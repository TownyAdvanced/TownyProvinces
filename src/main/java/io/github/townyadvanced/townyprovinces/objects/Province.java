package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;

import java.util.ArrayList;
import java.util.List;

public class Province {
	
	private Coord homeBlock;
	
	public Province() {
		
	}
	
	public void setHomeBlock(Coord homeBlock) {
		this.homeBlock = homeBlock;
	}

	public Coord getHomeBlock() {
		return homeBlock;
	}

	public List<ProvinceBlock> getProvinceBlocks() {
		List<ProvinceBlock> result = new ArrayList<>();
		for(ProvinceBlock provinceBlock: TownyProvincesDataHolder.getInstance().getProvinceBlocks().values()) {
			if(provinceBlock.getProvince() == this) {
				result.add(provinceBlock);
			}
		}
		return result;
	}
}
 