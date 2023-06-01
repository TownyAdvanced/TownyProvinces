package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;

public class ProvinceClaimBrush {
	
	private final int squareRadius;
	private Coord currentPosition;
	private final Province province;
	private boolean active;
	
	public ProvinceClaimBrush(Province province, int squareRadius) {
		this.squareRadius = squareRadius;
		this.province = province;
		this.currentPosition = province.getHomeBlock();
		this.active = true;
	}
	
	public void moveBrush(Coord destination) {
		this.currentPosition = destination;
	}

	public int getSquareRadius() {
		return squareRadius;
	}
	
	public Coord getCurrentPosition() {
		return currentPosition;
	}

	public Province getProvince() {
		return province;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
