package io.github.townyadvanced.townyprovinces.objects;

import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

public enum ProvinceType {
	CIVILIZED,
	SEA,
	WASTELAND;

	public static ProvinceType parseProvinceType(String typeAsString) {
		if(typeAsString.equalsIgnoreCase("civilized")) {
			return CIVILIZED;
		} else if(typeAsString.equalsIgnoreCase("sea")) {
			return SEA;
		} else if(typeAsString.equalsIgnoreCase("wasteland")) {
			return WASTELAND;
		} else {
			throw new RuntimeException("Unknown province type");		
		}
	}

	public int getBorderColour() {
		switch(this) {
			case CIVILIZED:
				return TownyProvincesSettings.getCivilizedProvinceBorderColour();
			case SEA:
				return TownyProvincesSettings.getSeaProvinceBorderColour();
			case WASTELAND:
				return TownyProvincesSettings.getWastelandProvinceBorderColour();
			default:
				throw new RuntimeException("Unknown province type");
		}
	}

	public int getBorderWeight() {
		switch(this) {
			case CIVILIZED:
				return TownyProvincesSettings.getCivilizedProvinceBorderWeight();
			case SEA:
				return TownyProvincesSettings.getSeaProvinceBorderWeight();
			case WASTELAND:
				return TownyProvincesSettings.getWastelandProvinceBorderWeight();
			default:
				throw new RuntimeException("Unknown province type");
		}
	}

	public double getBorderOpacity() {
		switch(this) {
			case CIVILIZED:
				return TownyProvincesSettings.getCivilizedProvinceBorderOpacity();
			case SEA:
				return TownyProvincesSettings.getSeaProvinceBorderOpacity();
			case WASTELAND:
				return TownyProvincesSettings.getWastelandProvinceBorderOpacity();
			default:
				throw new RuntimeException("Unknown province type");
		}
	}
	
	public boolean canNewTownsBeCreated() {
		switch(this) {
			case CIVILIZED:
				return true;
			case SEA:
			case WASTELAND:
				return false;
			default:
				throw new RuntimeException("Unknown province type");
		}
	}

	public boolean canForeignOutpostsBeCreated() {
		switch(this) {
			case CIVILIZED:
				return false;
			case SEA:
				return TownyProvincesSettings.getSeaProvinceOutpostsAllowed();
			case WASTELAND:
				return TownyProvincesSettings.getWastelandProvinceOutpostsAllowed();
			default:
				throw new RuntimeException("Unknown province type");
		}
	}
}
