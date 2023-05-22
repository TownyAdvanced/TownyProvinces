package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;

public class ProvinceClaimBrush {
	private final int sideLength;
	private Coord currentPosition;
	private final Province province;
	public ProvinceClaimBrush(Province province, int sideLength) {
		this.sideLength = sideLength;
		this.province = province;
		this.currentPosition = province.getHomeBlock();
	}

	public void moveBrush(int moveAmountX, int moveAmountZ) {
		this.currentPosition = new Coord(currentPosition.getX() + moveAmountX, currentPosition.getZ() + moveAmountZ);
	}
}
