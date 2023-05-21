package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;

public class Province {
	
	private Coord homeBlock;
	
	public Province() {
		
	}
	
	public void setHomeBlock(Coord homeBlock) {
		this.homeBlock = homeBlock;
	}

	public Coord getHomeBlock() {
		return homeBlock;
	}
}
