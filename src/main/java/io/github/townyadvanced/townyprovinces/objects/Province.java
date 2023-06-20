package io.github.townyadvanced.townyprovinces.objects;

import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;

import java.util.List;
import java.util.Set;

public class Province {
	
	private final TPCoord homeBlock;
	private int newTownCost;
	private int upkeepTownCost;
	private boolean isSea;
	private final String id; //convenience variable. In memory only. Used for dynmap and file operations
	private boolean landValidationRequested;

	public boolean equals(Object object) {
		if(!(object instanceof Province))
			return false;
		return homeBlock.equals(((Province)object).getHomeBlock());
	}
	
	public Province(TPCoord homeBlock, boolean isSea, boolean landValidationRequested, int newTownCost, int upkeepTownCost) {
		this.homeBlock = homeBlock;
		this.isSea = isSea;
		this.newTownCost = newTownCost;
		this.upkeepTownCost = upkeepTownCost;
		this.id = "province_x" + homeBlock.getX() + "_z_" + homeBlock.getZ();
		this.landValidationRequested = landValidationRequested;
	}

	public String getId() {
		return id;
	}
	
	public TPCoord getHomeBlock() {
		return homeBlock;
	}
	
	public void setNewTownCost(int i) {
		this.newTownCost = i;
	}

	public void setUpkeepTownCost(int i) {
		this.upkeepTownCost = i;
	}

	public int getNewTownCost() {
		return newTownCost;
	}

	public int getUpkeepTownCost() {
		return upkeepTownCost;
	}
	
	public boolean isSea() { 
		return isSea; 
	}
	
	public void setSea(boolean b) {
		this.isSea = b;
	}

	public List<TPCoord> getListOfCoordsInProvince() {
		return TownyProvincesDataHolder.getInstance().getListOfCoordsInProvince(this);
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

	public Set<TPCoord> getAdjacentBorderCoords(TPCoord targetCoord) {
		return TownyProvincesDataHolder.getInstance().findAdjacentBorderCoords(targetCoord);
	}
}
 