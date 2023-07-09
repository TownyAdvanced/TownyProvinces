package io.github.townyadvanced.townyprovinces.objects;

import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

public enum ProvinceType {
	CIVILISED,
	SEA,
	WASTELAND;

	public static ProvinceType parseProvinceType(String typeAsString) {
		if(typeAsString.equalsIgnoreCase("civilized")) {
			return CIVILISED;
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
			case CIVILISED:
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
			case CIVILISED:
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
			case CIVILISED:
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
			case CIVILISED:
				return TownyProvincesSettings.getCivilizedProvinceNewTownsAllowed();
			case SEA:
				return TownyProvincesSettings.getSeaProvinceNewTownsAllowed();
			case WASTELAND:
				return TownyProvincesSettings.getWastelandProvinceNewTownsAllowed();
			default:
				throw new RuntimeException("Unknown province type");
		}
	}

}
