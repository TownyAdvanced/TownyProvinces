package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;

public class ProvinceBlock {
	private Province province;
	private Region region;
	private boolean provinceBorder;
	private boolean regionBorder;
	private Coord coord;
	
	public ProvinceBlock() {
		this.province = null;
		this.region = null;
		this.provinceBorder = false;
		this.coord = null;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public boolean isProvinceBorder() {
		return provinceBorder;
	}

	public void setProvinceBorder(boolean b) {
		this.provinceBorder = b;
	}

	public Province getProvince() {
		return province;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	public Coord getCoord() {
		return coord;
	}
}
