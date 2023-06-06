package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	 * @return true if folder already existed
	 */
	public static boolean createFolderIfRequired(String relativeFolderPath) {
		Path folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(relativeFolderPath);
		return FileMgmt.checkOrCreateFolder(folderPath.toString());
	}

	public static List<File> readListOfFiles(String relativeFolderPath) {
		String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(relativeFolderPath).toString();
		File folder = new File(folderPath);
		return Arrays.asList(Objects.requireNonNull(folder.listFiles()));
	}

	public static void saveHashMapIntoFile(Map<String, String> fileEntries, String filePath) {
		try {
			List<String> linesToWrite = transformFileEntriesMapToListOfLines(fileEntries);
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

	private static List<String> transformFileEntriesMapToListOfLines(Map<String, String> fileEntriesMap) {
		List<String> result = new ArrayList<>();
		for(Map.Entry<String,String> mapEntry: fileEntriesMap.entrySet()) {
			result.add(mapEntry.getKey() + ": " + mapEntry.getValue());
		}
		return result;
	}

	public static List<File> readRegionDefinitionFiles() {
		return readListOfFiles(REGION_DEFINITIONS_FOLDER_PATH);
	}

	public static boolean createResourceDefinitionsFolderAndSampleFiles() {
		String fileName = "???";
		try {
			boolean folderAlreadyExisted = FileUtil.createFolderIfRequired(REGION_DEFINITIONS_FOLDER_PATH);
			if (folderAlreadyExisted) {
				//Sample file 1
				fileName = "Region_1_Earth.yml";
				List<String> fileEntries = new ArrayList<>();
				fileEntries.add("region_name", "Earth");
				fileEntries.add("top_left_corner_location", "-2434,-2049");
				fileEntries.add("bottom_right_corner_location", "4064,2056");
				fileEntries.add("province_size_estimate_for_populating_in_square_metres", "80000");
				fileEntries.add("min_allowed_distance_between_province_home_blocks", "160");
				fileEntries.add("max_allowed_variance_between_ideal_and_actual_num_provinces", "0.1");
				fileEntries.add("province_creator_brush_square_radius_in_chunks", "4");
				fileEntries.add("province_creator_brush_min_move_in_chunks", "4");
				fileEntries.put("province_creator_brush_max_move_in_chunks", "2");
				fileEntries.put("province_creator_brush_claim_limit_in_square_metres", "10000");
				fileEntries.put("number_of_province_painting_cycles", "100");
				String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(FileUtil.REGION_DEFINITIONS_FOLDER_PATH).toString();
				String filePath = folderPath + "/" + fileName;
				saveHashMapIntoFile(fileEntries, filePath);}
			
			
			
			
			return true;
		} catch (Exception e) {
			TownyProvinces.severe("Problem creating sample resource definition file: " + fileName + ". " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
