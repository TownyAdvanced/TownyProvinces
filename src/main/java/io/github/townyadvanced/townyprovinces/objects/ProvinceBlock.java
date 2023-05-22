package io.github.townyadvanced.townyprovinces.objects;

public class ProvinceBlock {
	public Province province;
	public Region region;
	boolean border;
	
	public ProvinceBlock() {
		this.province = null;
		this.region = null;
		this.border = false;
	}

	public void setProvince(Province province) {
		this.province = province;
	}
}
