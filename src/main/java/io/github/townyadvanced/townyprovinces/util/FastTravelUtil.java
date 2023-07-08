package io.github.townyadvanced.townyprovinces.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class FastTravelUtil {

	public static boolean isFastTravelSign(BlockState blockState) {
		return blockState instanceof Sign
			&& (((Sign) blockState).getLine(0).trim().equals(">>>"));
	}
}
