package io.github.townyadvanced.townyprovinces.util;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class FastTravelUtil {

	public static boolean isFastTravelSign(Block block) {
		return block.getState() instanceof Sign
			&& (((Sign) block.getState()).getLine(0).trim().equals(">>>"));
	}
}
