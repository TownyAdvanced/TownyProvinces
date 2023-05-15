package io.github.townyadvanced.townyprovinces.objects;

public class TPChunk {
	public Province province;
	public Region region;
	boolean border;
	
	public TPChunk() {
		this.province = null;
		this.region = null;
		this.border = false;
	}
}
