package io.github.townyadvanced.townyprovinces.objects;

public class TPCoord {
	
	private int x;
	private int z;
	
	public TPCoord(int x, int z) {
		this.x= x;
		this.z = z;
	}

	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}

	public void setValues(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public int hashCode() {
		int result = 17;
		result = 27 * result + this.x;
		result = 27 * result + this.z;
		return result;
	}
	
	public boolean equals(Object o) {
		return o instanceof TPCoord
			&& ((TPCoord) o).getX() == this.x
			&& ((TPCoord) o).getZ() == this.z;
	}

}
