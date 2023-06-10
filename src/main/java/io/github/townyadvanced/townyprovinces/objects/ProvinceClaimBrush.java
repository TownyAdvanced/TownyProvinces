package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;

public class ProvinceClaimBrush {
	
	private final int squareRadius;
	private final TPCoord currentPosition;
	private final Province province;
	private boolean active;
	private int numChunksClaimed;
	
	public ProvinceClaimBrush(Province province, int squareRadius) {
		this.squareRadius = squareRadius;
		this.province = province;
		this.currentPosition = province.getHomeBlock();
		this.active = true;
		this.numChunksClaimed = 0;
	}
	
	public void moveBrushTo(int xCoord, int zCoord) {
		this.currentPosition.setValues(xCoord, zCoord);
	}

	public int getSquareRadius() {
		return squareRadius;
	}
	
	public TPCoord getCurrentPosition() {
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
	
	public void registerChunkClaimed() {
		numChunksClaimed++;
	}
	
	public int getNumChunksClaimed() {
		return numChunksClaimed;
	}
}
