package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.util.DataHandlerUtil;

import java.util.List;

public class Province {
	
	private final Coord homeBlock;
	private int newTownPrice;
	private int townUpkeep;
	private boolean isSea;
	private String id; //convenience variable. In memory only. Used for dynmap and file operations
	private boolean landValidationRequested;

	public boolean equals(Object object) {
		if(!(object instanceof Province))
			return false;
		return homeBlock.equals(((Province)object).getHomeBlock());
	}
	
	public Province(Coord homeBlock) {
		this.homeBlock = homeBlock;
		this.isSea = false;
		this.newTownPrice = 0;
		this.townUpkeep = 0;
		this.id = "province_x" + homeBlock.getX() + "_y_" + homeBlock.getZ();
		this.landValidationRequested = false;
	}

	public String getId() {
		return id;
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

	public boolean isLandValidationRequested() {
		return landValidationRequested;
	}

	public void setLandValidationRequested(boolean landValidationRequested) {
		this.landValidationRequested = landValidationRequested;
	}
}
 