package io.github.townyadvanced.townyprovinces.metadata;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TownMetaDataController {
	
	private static final StringDataField jumpNodeCoord = new StringDataField("townyprovinces_jumpNodeCoord", "");
	private static final StringDataField jumpNodeSigns = new StringDataField("townyprovinces_jumpNodeSigns", "");
	private static final StringDataField portCoord = new StringDataField("townyprovinces_portCoord", "");
	private static final StringDataField portSigns = new StringDataField("townyprovinces_portSigns", "");

	////////////////////////////////////////////////////////////////

	@Nullable
	public static boolean hasJumpNode(Town town) {
		return hasTravelPlotWorldCoord(town, jumpNodeCoord);
	}

	@Nullable
	public static boolean hasPort(Town town) {
		return hasTravelPlotWorldCoord(town, portCoord);
	}

	@Nullable
	private static boolean hasTravelPlotWorldCoord(Town town, StringDataField stringDataField) {
		String coordAsString = getTravelPlotWorldCoordAsString(town, stringDataField);
		return coordAsString != null;
	}
	
	////////////////////////////////////////////////////////////////
	
	@Nullable
	public static WorldCoord getJumpNodeWorldCoord(Town town) {
		return getTravelPlotWorldCoord(town, jumpNodeCoord);
	}

	@Nullable
	public static WorldCoord getPortWorldCoord(Town town) {
		return getTravelPlotWorldCoord(town, portCoord);
	}
	
	@Nullable
	private static WorldCoord getTravelPlotWorldCoord(Town town, StringDataField stringDataField) {
		String coordAsString = getTravelPlotWorldCoordAsString(town, stringDataField);
		if(coordAsString == null)
			return null;
		String[] worldCoordArray = coordAsString.split(",");
		World world = Bukkit.getWorld(worldCoordArray[0]);
		int x = Integer.parseInt(worldCoordArray[1]);
		int z = Integer.parseInt(worldCoordArray[2]);
		WorldCoord result = new WorldCoord(world, x, z);
		return result;
	}

	////////////////////////////////////////////////////////////////
	
	public static void setJumpNodeCoord(Town town, WorldCoord worldCoord) {
		setTravelPlotCoord(town, worldCoord, jumpNodeCoord, "townyprovinces_jumpNodeCoord");
	}

	public static void setPortCoord(Town town, WorldCoord worldCoord) {
		setTravelPlotCoord(town, worldCoord, portCoord, "townyprovinces_portCoord");
	}

	private static void setTravelPlotCoord(Town town, WorldCoord worldCoord, StringDataField stringDataField, String metadataName) {
		int x = worldCoord.getX();
		int z = worldCoord.getZ();
		String worldName = worldCoord.getWorldName();
		String metadataValue = worldName + "," + x + "," + z;
		StringDataField sdf = (StringDataField) stringDataField.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, metadataValue, true);
		else
			town.addMetaData(new StringDataField(metadataName, metadataValue));
	}

	////////////////////////////////////////////////////////////////

	public static void removeJumpNodeCoord(Town town) {
		town.removeMetaData(jumpNodeCoord.clone());
	}

	public static void removePortCoord(Town town) {
		town.removeMetaData(portCoord.clone());
	}
	
	////////////////////////////////////////////////////////////////
	
	public static Map<String, Block> getJumpNodeSigns(Town town) {
		return getTravelPlotSigns(town, jumpNodeSigns);
	}
	
	public static Map<String, Block> getPortSigns(Town town) {
		return getTravelPlotSigns(town, portSigns);
	}

	private static Map<String, Block> getTravelPlotSigns(Town town, StringDataField stringDataField) {
		Map<String, Block> result = new HashMap<>();
		String signsAsString = getTravelPlotSignsAsString(town, stringDataField);
		if(signsAsString != null && signsAsString.length() > 0) {
			String[] signsAsArray = signsAsString.split("\\|");
			String[] signAsArray;
			World world;
			int x;
			int y;
			int z;
			String destinationTownName;
			Block block;
			for(String signAsString: signsAsArray) {
				signAsArray = signAsString.split(",");
				world = Bukkit.getWorld(signAsArray[0]);
				x = Integer.parseInt(signAsArray[1]);
				y = Integer.parseInt(signAsArray[2]);
				z = Integer.parseInt(signAsArray[3]);
				destinationTownName = signAsArray[4];
				block = (new Location(world,x,y,z)).getBlock();
				result.put(destinationTownName, block);
			}
		}
		return result;
	}

	////////////////////////////////////////////////////////////////

	public static void addJumpNodeSign(Town town, Block signBlock, String destinationTownName) {
		addTravelPlotSign(town, signBlock, destinationTownName, jumpNodeSigns, "townyprovinces_jumpNodeSigns");
	}

	public static void addPortSign(Town town, Block signBlock, String destinationTownName) {
		addTravelPlotSign(town, signBlock, destinationTownName, portSigns, "townyprovinces_portSigns");
	}

	private static void addTravelPlotSign(Town town, Block signBlock, String destinationTownName, StringDataField stringDataField, String metadataName) {
		StringBuilder builder;
		String value = getTravelPlotSignsAsString(town, stringDataField);
		//Remove data
		if(value != null) {
			removeJumpNodeSigns(town);
			builder = new StringBuilder(value);
			builder.append("|");
		} else {
			builder = new StringBuilder();
		}
		//Add the new values to the builder
		builder.append(signBlock.getWorld().getName());
		builder.append(",");
		builder.append(signBlock.getX());
		builder.append(",");
		builder.append(signBlock.getY());
		builder.append(",");
		builder.append(signBlock.getZ());
		builder.append(",");
		builder.append(destinationTownName);
		//Set the required metadata field
		StringDataField sdf = (StringDataField) stringDataField.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, builder.toString(), true);
		else
			town.addMetaData(new StringDataField(metadataName, builder.toString()));
	}

	////////////////////////////////////////////////////////////////

	@Nullable
	private static String getTravelPlotWorldCoordAsString(Town town, StringDataField stringDataField) {
		StringDataField sdf = (StringDataField) stringDataField.clone();
		if (town.hasMeta(sdf.getKey())) {
			return MetaDataUtil.getString(town, sdf);
		}
		return null;
	}

	/**
	 * worldName,x,y,z,destinationTown| 
	 */
	@Nullable
	private static String getTravelPlotSignsAsString(Town town, StringDataField stringDataField) {
		StringDataField sdf = (StringDataField) stringDataField.clone();
		if (town.hasMeta(sdf.getKey())) {
			return MetaDataUtil.getString(town, sdf);
		}
		return null;
	}


	////////////////////////////////////////////////////////////////
	
	public static void removeJumpNodeSigns(Town town) {
		town.removeMetaData(jumpNodeSigns.clone());
	}

	public static void removePortSigns(Town town) {
		town.removeMetaData(portSigns.clone());
	}

	////////////////////////////////////////////////////////////////

	public static void removeJumpNodeSign(Town town, Block block) {
		removeTravelPlotSign(town, block, jumpNodeSigns, "townyprovinces_jumpNodeSigns");
	}

	public static void removePortSign(Town town, Block block) {
		removeTravelPlotSign(town, block, portSigns, "townyprovinces_portSigns");
	}
	/**
	 * Remove the sign IF it is in the metadata
	 * @param block the sign
	 */
	public static void removeTravelPlotSign(Town town, Block block, StringDataField stringDataField, String metadataFieldName) {
		Map<String, Block> signs = getTravelPlotSigns(town, stringDataField);
		String signKey = null;
		for(Map.Entry<String,Block> mapEntry: signs.entrySet()) {
			if(mapEntry.getValue().equals(block)) {
				signKey = mapEntry.getKey();
				break;
			}
		}
		if(signKey != null) {
			signs.remove(signKey);
			setTravelPlotSigns(town, signs, stringDataField, metadataFieldName);
		}
	}

	///////////////////////////////////////////////
	
	private static void setTravelPlotSigns(Town town, Map<String,Block> newSigns, StringDataField stringDataField, String metaDataname) {
		removeJumpNodeSigns(town);
		for(Map.Entry<String,Block> mapEntry: newSigns.entrySet()) {
			addTravelPlotSign(town, mapEntry.getValue(), mapEntry.getKey(), stringDataField, metaDataname);
		}
	}

	public static void removeAllJumpNodeMetadata(Town town) {
		removeJumpNodeCoord(town);
		removeJumpNodeSigns(town);
	}

	public static void removeAllPortMetadata(Town town) {
		removePortCoord(town);
		removePortSigns(town);
	}

	///////////////////////////////////////////////

}
