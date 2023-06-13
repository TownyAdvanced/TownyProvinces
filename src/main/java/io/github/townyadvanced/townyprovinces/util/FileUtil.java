package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileUtil {

	public static final String DATA_FOLDER_PATH = "data";
	public static final String REGION_DEFINITIONS_FOLDER_PATH = "region_definitions";
	public static final String PROVINCES_FOLDER_PATH = "data/provinces";

	public static boolean setupPluginDataFoldersIfRequired() {
		try {
			TownyProvinces.info("Now setting up plugin sub-folders");
			createFolderIfRequired(DATA_FOLDER_PATH);
			createFolderIfRequired(PROVINCES_FOLDER_PATH);
		} catch (Exception e) {
			TownyProvinces.severe("Problem setting up plugin sub-folders: " + e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @param relativeFolderPath given folder path
	 * @return true if folder was created
	 */
	public static boolean createFolderIfRequired(String relativeFolderPath) {
		Path folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(relativeFolderPath);
		boolean folderWasCreated = !(new File(folderPath.toString()).exists());
		FileMgmt.checkOrCreateFolder(folderPath.toString());
		return folderWasCreated;
	}

	public static List<File> readListOfFiles(String relativeFolderPath) {
		String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(relativeFolderPath).toString();
		File folder = new File(folderPath);
		return Arrays.asList(Objects.requireNonNull(folder.listFiles()));
	}

	public static void saveHashMapIntoFile(Map<String, String> fileEntries, String filePath) {
		List<String> linesToWrite = new ArrayList<>();
		for(Map.Entry<String,String> mapEntry: fileEntries.entrySet()) {
			linesToWrite.add(mapEntry.getKey() + ": " + mapEntry.getValue());
		}
		saveListIntoFile(linesToWrite, filePath);
	}

	public static void saveListIntoFile(List<String> linesToWrite, String filePath) {
		try {
			File file = new File(filePath);
			if(file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileMgmt.listToFile(linesToWrite, filePath);
		} catch (IOException e) {
			TownyProvinces.severe("Problem Saving Hash Map to File." + e.getMessage());
			e.printStackTrace();
		}
	}

	public static List<File> readRegionDefinitionFiles() {
		return readListOfFiles(REGION_DEFINITIONS_FOLDER_PATH);
	}

	public static boolean createRegionDefinitionsFolderAndSampleFiles() {
		String fileName = "???";
		try {
			boolean folderWasCreated = FileUtil.createFolderIfRequired(REGION_DEFINITIONS_FOLDER_PATH);
			if (folderWasCreated) {
				
				//Sample file 1
				fileName = "Region_1_Earth.yml";
				List<String> fileEntries = new ArrayList<>();
				fileEntries.add("region_name : Earth");
				fileEntries.add("top_left_corner_location: -2434,-2049");
				fileEntries.add("bottom_right_corner_location: 4064,2056");
				fileEntries.add("province_size_estimate_for_populating_in_square_metres: 80000");
				fileEntries.add("min_allowed_distance_between_province_home_blocks: 120");
				fileEntries.add("max_allowed_variance_between_ideal_and_actual_num_provinces: 0.1");
				fileEntries.add("province_creator_brush_square_radius_in_chunks: 4");
				fileEntries.add("province_creator_brush_min_move_in_chunks: 1");
				fileEntries.add("province_creator_brush_max_move_in_chunks: 4");
				fileEntries.add("province_creator_brush_claim_limit_in_square_metres: 70000");
				fileEntries.add("number_of_province_painting_cycles: 100");
				fileEntries.add("new_town_cost: 250");
				fileEntries.add("upkeep_town_cost: 10");
				String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(FileUtil.REGION_DEFINITIONS_FOLDER_PATH).toString();
				String filePath = folderPath + "/" + fileName;
				saveListIntoFile(fileEntries, filePath);
				
				//Sample file 2
				fileName = "Region_2_Europe.yml";
				fileEntries.clear();
				fileEntries.add("region_name: Europe");
				fileEntries.add("top_left_corner_location: -570,-1515");
				fileEntries.add("bottom_right_corner_location: 900,-630");
				fileEntries.add("province_size_estimate_for_populating_in_square_metres: 20000");
				fileEntries.add("min_allowed_distance_between_province_home_blocks: 80");
				fileEntries.add("max_allowed_variance_between_ideal_and_actual_num_provinces: 0.1");
				fileEntries.add("province_creator_brush_square_radius_in_chunks: 2");
				fileEntries.add("province_creator_brush_min_move_in_chunks: 1");
				fileEntries.add("province_creator_brush_max_move_in_chunks: 2");
				fileEntries.add("province_creator_brush_claim_limit_in_square_metres: 18000");
				fileEntries.add("number_of_province_painting_cycles: 100");
				fileEntries.add("new_town_cost: 300");
				fileEntries.add("upkeep_town_cost: 12");
				folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(FileUtil.REGION_DEFINITIONS_FOLDER_PATH).toString();
				filePath = folderPath + "/" + fileName;
				saveListIntoFile(fileEntries, filePath);
			}
			return true;
		} catch (Exception e) {
			TownyProvinces.severe("Problem creating sample resource definition file: " + fileName + ". " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
