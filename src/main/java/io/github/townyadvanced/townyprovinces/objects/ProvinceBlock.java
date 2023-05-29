package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;

public class ProvinceBlock {
	
	private final Province province;
	private final boolean provinceBorder;
	private final Coord coord;
	
	public ProvinceBlock(Coord coord, Province province, boolean provinceBorder) {
		this.coord = coord;
		this.province = province;
		this.provinceBorder = provinceBorder;
	}
	
	public boolean isProvinceBorder() {
		return provinceBorder;
	}

	public Province getProvince() {
		return province;
	}

	public Coord getCoord() {
		return coord;
	}
}
