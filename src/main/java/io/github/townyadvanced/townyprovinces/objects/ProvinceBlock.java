package io.github.townyadvanced.townyprovinces.objects;

public class ProvinceBlock {
	private Province province;
	private Region region;
	private boolean border;
	
	public ProvinceBlock() {
		this.province = null;
		this.region = null;
		this.border = false;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public boolean isBorder() {
		return border;
	}

	public void setBorder(boolean b) {
		this.border = b;
	}

	public Province getProvince() {
		return province;
	}
}
