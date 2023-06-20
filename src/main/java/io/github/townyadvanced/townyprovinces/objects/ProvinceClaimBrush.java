package io.github.townyadvanced.townyprovinces.objects;

public class ProvinceClaimBrush {
	
	private final int squareRadius;
	private final TPFreeCoord currentPosition;
	private final Province province;
	private boolean active;
	private int numChunksClaimed;
	
	public ProvinceClaimBrush(Province province, int squareRadius) {
		this.squareRadius = squareRadius;
		this.province = province;
		this.currentPosition = new TPFreeCoord(province.getHomeBlock().getX(), province.getHomeBlock().getZ());
		this.active = true;
		this.numChunksClaimed = 0;
	}
	
	public void moveBrushTo(int xCoord, int zCoord) {
		this.currentPosition.setValues(xCoord, zCoord);
	}

	public int getSquareRadius() {
		return squareRadius;
	}
	
	public TPFreeCoord getCurrentPosition() {
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
