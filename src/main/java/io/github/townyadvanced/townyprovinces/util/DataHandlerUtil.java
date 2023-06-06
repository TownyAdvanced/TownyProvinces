package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHandlerUtil {
	public static final String dataFolderPath = "data";
	public static final String provinceGeneratorsFolderPath = "province_generators";
	public static final String provincesFolderPath = "data/provinces";

	public static boolean setupPluginSubFoldersIfRequired() {
		try {
			FileUtil.setupFolderIfRequired(dataFolderPath);
			FileUtil.setupFolderIfRequired(provincesFolderPath);
			FileUtil.setupFolderIfRequired(provinceGeneratorsFolderPath);
		} catch (Exception e) {
			TownyProvinces.severe("Problem Setting up plugin sub-folders");
		}
		return true;
	}

	public static boolean loadAllData() {
		//loadProvinces();
		//loadProvinceBlocks();
		return true;
	}

	public static boolean saveAllData() {
		saveAllProvinces();
		//saveProvinceBlocks();
		return true;
	}


	private static void saveAllProvinces() {
		TownyProvinces.info("Now Saving Provinces");
		//Delete existing files
		List<File> provinceFiles = FileUtil.readListOfFiles(provincesFolderPath);
		for (File file : provinceFiles) {
			file.delete();
		}
		//Save all provinces
		for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			saveProvince(province);
		}
		TownyProvinces.info("All Provinces Saved");
	}

	public static void saveProvince(Province province) {
		String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(provincesFolderPath).toString();
		String fileName = folderPath + "/" + province.getId() + ".yml";
		Map<String, String> fileEntries = new HashMap<>();
		fileEntries.put("home_block", "" + province.getHomeBlock().getX() + "," + province.getHomeBlock().getZ());
		fileEntries.put("sea", "" + province.isSea());
		fileEntries.put("new_town_price", "" + province.getNewTownPrice());
		fileEntries.put("town_upkeep", "" + province.getTownUpkeep());
		fileEntries.put("coords", "" + getCoordsAsWriteableString(province));
		FileUtil.saveHashMapIntoFile(fileEntries, fileName);
	}

	/**
	 * 
	 * @param province province
	 * @return
	 */
	private static String getCoordsAsWriteableString(Province province) {
		StringBuilder result = new StringBuilder();
		boolean firstCoord = true;
		for(Coord coord: province.getCoordsInProvince()) {
			if(firstCoord) {
				firstCoord = false;
			} else {
				result.append("|");
			}
			result.append(coord.getX()).append(",").append(coord.getZ());
		}
		return  result.toString();
	}
	/*

	private static void loadProvinces() {
		TownyProvinces.info("Now Loading Provinces");
		List<File> provinceFiles = FileUtil.readListOfFiles(provincesFolderPath);
		for(File provinceFile: provinceFiles) {
			//Load province file
			Map<String,String> fileEntries = FileMgmt.loadFileIntoHashMap(provinceFile);
			Coord homeBlock = unpackCoord(fileEntries.get("home_block"));
			UUID uuid = UUID.fromString(fileEntries.get("uuid"));
			Province province = new Province(homeBlock, uuid);
			province.setNewTownPrice(Integer.parseInt(fileEntries.get("new_town_price")));
			province.setTownUpkeep(Integer.parseInt(fileEntries.get("town_upkeep")));
			//Add province to TP universe
			TownyProvincesDataHolder.getInstance().addProvince(province);
		}
		TownyProvinces.info("Provinces Loaded");
	}

*/
	 
	/*
	private static void saveProvinceBlocks() {
		TownyProvinces.info("Now Saving Province Blocks");
		String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(provinceBlocksFolderPath).toString();
		
		//Delete existing files
		List<File> provinceBlockFiles = FileUtil.readListOfFiles(provinceBlocksFolderPath);
		for(File file: provinceBlockFiles) {
			file.delete();
		}

		//Save all province block files
		for(Map.Entry<Coord,ProvinceBlock> provinceBlockEntry: TownyProvincesDataHolder.getInstance().getProvinceBlocks().entrySet()) {
			//Make file name
			Coord coord = provinceBlockEntry.getKey();
			ProvinceBlock provinceBlock = provinceBlockEntry.getValue();
			String fileName = folderPath + "/province_block_x" + coord.getX() + "_z" + coord.getZ() + ".yml";
			//Make content
			Map<String,String> fileEntries = new HashMap<>();
			fileEntries.put("coord", "" + coord.getX() + "," + coord.getZ());
			fileEntries.put("province_uuid", provinceBlock.getProvince() == null ? "" : provinceBlock.getProvince().getUuid().toString());
			fileEntries.put("border", "" + provinceBlock.isProvinceBorder());
			
			if(!provinceBlock.isProvinceBorder() && provinceBlock.getProvince() == null) {
				throw new RuntimeException("WARNING: Province block is not right + x_" + coord.getX() + "_z_" + coord.getZ());
			}
			
			FileUtil.saveHashMapIntoFile(fileEntries, fileName);
		}
		TownyProvinces.info("Province Blocks Saved");
	}

	private static void loadProvinceBlocks() {
		TownyProvinces.info("Now Loading Province Blocks");
		List<File> provinceBlockFiles = FileUtil.readListOfFiles(provinceBlocksFolderPath);
		Map<String,String> fileEntries;
		Coord coord;
		boolean border;
		Province province;
		UUID provinceUuid;
		ProvinceBlock provinceBlock;
		for(File provinceBlockFile: provinceBlockFiles) {
			//Load file
			fileEntries = FileMgmt.loadFileIntoHashMap(provinceBlockFile);
			coord = unpackCoord(fileEntries.get("coord"));
			border = Boolean.parseBoolean(fileEntries.get("border"));
			if(border) {
				province = null;
			} else {
				//TownyProvinces.info("Now loading province: x_" + coord.getX() + "_z_" + coord.getZ());
				provinceUuid = UUID.fromString(fileEntries.get("province_uuid"));
				province = TownyProvincesDataHolder.getInstance().getProvince(provinceUuid);
			}
			//Create province block
			provinceBlock = new ProvinceBlock(coord, province, border);
			//Add province block to TP universe
			TownyProvincesDataHolder.getInstance().claimCoordForProvince(coord, provinceBlock);
			
			if(!provinceBlock.isProvinceBorder() && provinceBlock.getProvince() == null) {
				throw new RuntimeException("WARNING: Province block is not right + x_" + coord.getX() + "_z_" + coord.getZ() + " --- UUID in file: " + fileEntries.get("province_uuid"));
			}
			
			
		}
		TownyProvinces.info("Province Blocks Loaded");
	}


	private static Coord unpackCoord(String coordAsString) {
		String[] xz = coordAsString.split(",");
		int x = Integer.parseInt(xz[0]);
		int z = Integer.parseInt(xz[1]);
		return new Coord(x,z);
	}

	*/
}
