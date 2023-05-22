package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;

public class ProvinceClaimBrush {
	private final int squareRadius;
	private Coord currentPosition;
	private final Province province;
	public ProvinceClaimBrush(Province province, int squareRadius) {
		this.squareRadius = squareRadius;
		this.province = province;
		this.currentPosition = province.getHomeBlock();
	}

	public void moveBrush(int moveAmountX, int moveAmountZ) {
		this.currentPosition = new Coord(currentPosition.getX() + moveAmountX, currentPosition.getZ() + moveAmountZ);
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
}
