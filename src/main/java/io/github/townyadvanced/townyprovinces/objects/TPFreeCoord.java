package io.github.townyadvanced.townyprovinces.objects;

public class TPFreeCoord {
	
	private int x;
	private int z;

	public TPFreeCoord(int x, int z) {
		this.x= x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public int hashCode() {
		int result = 17;
		result = 27 * result + this.x;
		result = 27 * result + this.z;
		return result;
	}

	public boolean equals(Object o) {
		if (o instanceof TPCoord
			&& ((TPCoord) o).getX() == this.x
			&& ((TPCoord) o).getZ() == this.z) {
			return true;
		} else if (o instanceof TPFreeCoord
			&& ((TPFreeCoord) o).getX() == this.x
			&& ((TPFreeCoord) o).getZ() == this.z) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setValues(int x, int z) {
		this.x = x;
		this.z = z;
	}
}
