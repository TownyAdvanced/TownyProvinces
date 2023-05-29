package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Province {
	
	private UUID uuid;
	private final Coord homeBlock;
	private int newTownCost = 0;
	private int townUpkeep = 0;
	
	public boolean equals(Object object) {
		if(!(object instanceof Province))
			return false;
		return homeBlock.equals(((Province)object).getHomeBlock());
	}
	
	public Province(Coord homeBlock) {
		this.uuid = UUID.randomUUID();
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
 