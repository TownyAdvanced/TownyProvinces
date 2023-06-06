package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.util.DataHandlerUtil;

import java.util.List;
import java.util.UUID;

public class Province {
	
	private final UUID uuid;
	private final Coord homeBlock;
	private int newTownPrice;
	private int townUpkeep;
	private boolean isSea;
	
	public boolean equals(Object object) {
		if(!(object instanceof Province))
			return false;
		return homeBlock.equals(((Province)object).getHomeBlock());
	}
	
	public Province(Coord homeBlock, UUID uuid) {
		this.uuid = uuid;
		this.homeBlock = homeBlock;
		this.isSea = false;
	}

	public Coord getHomeBlock() {
		return homeBlock;
	}
	
	public void setNewTownPrice(int i) {
		this.newTownPrice = i;
	}

	public void setTownUpkeep(int i) {
		this.townUpkeep = i;
	}

	public UUID getUuid() {
		return uuid;
	}

	public int getNewTownPrice() {
		return newTownPrice;
	}

	public int getTownUpkeep() {
		return townUpkeep;
	}
	
	public boolean isSea() { 
		return isSea; 
	}
	
	public void setSea(boolean b) {
		this.isSea = b;
	}

	public List<Coord> getCoordsInProvince() {
		return TownyProvincesDataHolder.getInstance().getCoordsInProvince(this);
	}

	public void saveData() {
		DataHandlerUtil.saveProvince(this);
	}
}
 