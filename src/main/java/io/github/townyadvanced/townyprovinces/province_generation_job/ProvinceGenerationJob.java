package io.github.townyadvanced.townyprovinces.province_generation_job;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.land_validation_job.LandValidationJob;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.FileUtil;
import io.github.townyadvanced.townyprovinces.util.TownyProvincesMathUtil;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProvinceGenerationJob extends BukkitRunnable {
	
	public static ProvinceGenerationJob provinceGenerationJob = null;

	public static boolean startJob(String regionName) {
		if(provinceGenerationJob != null) {
			TownyProvinces.severe("Job In Progress. You must wait until the current job ends until you start a new one.");
			return false;
		} else {
			provinceGenerationJob = new ProvinceGenerationJob(regionName);
			provinceGenerationJob.runTaskAsynchronously(TownyProvinces.getPlugin());
			return true;
		}
	}
	
	public String regionName;
	
	public ProvinceGenerationJob(String regionName) {
		this.regionName = regionName;
	}
	
	@Override
	public void run() {
		TownyProvinces.info("Region regeneration Job Started");
		//Create region definitions folder and sample files if needed
		if(!FileUtil.createRegionDefinitionsFolderAndSampleFiles()) {
			TownyProvinces.info("Problem creation region definitions folder and sample files");
			return;
		}
		//Reload region definitions
		if(!TownyProvincesSettings.loadRegionDefinitions()) {
			TownyProvinces.info("Problem reloading region definitions");
			return;
		}
		//Paint region(s)
		boolean success;
		if(regionName.equalsIgnoreCase("ALL")) {
			//Clear all data maps
			TownyProvincesDataHolder.getInstance().getProvincesSet().clear();
			TownyProvincesDataHolder.getInstance().getCoordProvinceMap().clear();
			TownyProvincesDataHolder.getInstance().regenerateUnclaimedCoordsMap();
			success = paintAllRegions();
		} else {
			success = paintOneRegion(regionName);
		}
		if(!success) {
			TownyProvinces.info("Problem Painting Regions");
			return;
		}
		//Allocate unclaimed chunks to provinces.
		if(!assignUnclaimedCoordsToProvinces()) {
			TownyProvinces.info("Problem assigning unclaimed chunks to provinces");
			return;
		}
		//Delete empty provinces
		if(!deleteEmptyProvinces()) {
			TownyProvinces.info("Problem deleting empty provinces");
			return;
		}
		//Save data and ask for full dynmap refresh
		DataHandlerUtil.saveAllData();
		TownyProvinces.getPlugin().getDynmapIntegration().requestFullMapRefresh();
		//Messaging
		if(regionName.equalsIgnoreCase("ALL")) {
			TownyProvinces.info(Translatable.of("msg_successfully_regenerated_all_regions").translate(Locale.ROOT));
		} else {
			TownyProvinces.info(Translatable.of("msg_successfully_regenerated_one_regions", regionName).translate(Locale.ROOT));
		}
	}
	
	public boolean paintAllRegions() {
		//Paint all Provinces
		List<String> regionNames = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet());
		Collections.sort(regionNames); //Sort in alphabetical order
		for (String regionName: regionNames) {
			if(!paintProvincesInRegion(regionName)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Generate provinces in just one region
	 * @param regionName given region name
	 * @return true if success
	 */
	public static boolean paintOneRegion(String regionName) {
		//Paint region
		if(!paintProvincesInRegion(regionName)) {
			return false;
		}
		return true;
	}

	private static boolean deleteEmptyProvinces() {
		TownyProvinces.info("Now Deleting Empty Provinces.");
		for(Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			if(province.getCoordsInProvince().size() == 0) {
				TownyProvincesDataHolder.getInstance().deleteProvince(province);
			}
		}
		TownyProvinces.info("Empty Provinces Deleted.");
		return true;
	}

	private static boolean paintProvincesInRegion(String regionName) {
		TownyProvinces.info("Now Painting Provinces In Region: " + regionName);

		//Delete existing provinces which are mostly in the given area
		if(!deleteExistingProvincesWhichAreMostlyInSpecifiedArea(regionName)) {
			return false;
		}
		
		//Create province objects - empty except for the homeblocks
		if(!generateProvinceObjects(regionName)) {
			return false;
		}
		
		//Execute chunk claim competition
		if(!executeChunkClaimCompetition(regionName)) {
			return false;
		}

		TownyProvinces.info("Finished Painting Provinces In Region: " + regionName);
		return true;
	}

	private static boolean deleteExistingProvincesWhichAreMostlyInSpecifiedArea(String regionName) {
		TownyProvinces.info("Now deleting provinces which are mostly in the specified area.");
		int numProvincesDeleted = 0;
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		for(Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			List<TPCoord> coordsInProvince = province.getCoordsInProvince();
			int numProvinceBlocksInSpecifiedArea = 0;
			for (TPCoord coordInProvince : coordsInProvince) {
				if (coordInProvince.getX() < minX)
					continue;
				else if (coordInProvince.getX() > maxX)
					continue;
				else if (coordInProvince.getZ() < minZ)
					continue;
				else if (coordInProvince.getZ() > maxZ)
					continue;
				numProvinceBlocksInSpecifiedArea++;
			}
			if(numProvinceBlocksInSpecifiedArea > (coordsInProvince.size() / 2)) {
				TownyProvincesDataHolder.getInstance().deleteProvince(province);
				numProvincesDeleted++;
			}
		}
		TownyProvinces.info("" + numProvincesDeleted + " provinces deleted.");
		return true;
	}
	
	private static Set<TPCoord> findAllUnclaimedCoords() {
		//Pick first region as it should be the whole map
		String regionName = TownyProvincesSettings.getNameOfFirstRegion();
		Set<TPCoord> result = new HashSet<>();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		for(int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if (!TownyProvincesDataHolder.getInstance().isCoordClaimedByProvince(x, z)) {
					result.add(new TPCoord(x, z));
				}
			}
		}
		return result;	
	}
	
	/**
	 * Assign unclaimed coords to provinces where possible
	 * 
	 * @return list of unclaimable coords
	 */
	private static boolean assignUnclaimedCoordsToProvinces() {
		TownyProvinces.info("Now assigning unclaimed chunks to provinces.");
		//From the unclaimed coords set, select those eligible for province assignment
		Set<TPCoord> unclaimedCoords = findAllUnclaimedCoords();
		Map<TPCoord, Province> pendingCoordProvinceAssignmentMap = new HashMap<>();
		Province province;
		while(true) {
			TownyProvinces.info("Num Unclaimed Chunks: " + unclaimedCoords.size());
			//Exit loop if there are no more eligible coords
			rebuildPendingCoordProvinceAssignmentMap(unclaimedCoords, pendingCoordProvinceAssignmentMap);
			if(pendingCoordProvinceAssignmentMap.size() == 0) {
				break;
			}
			/*
			 * Cycle each eligible coord
			 * Assign it if it is still eligible when we get to it
			 */
			for(TPCoord coord: coordsEligibleForProvinceAssignment) {
				if((province = getProvinceIfCoordIsEligibleForProvinceAssignment(regionName, coord)) != null) {
					TownyProvincesDataHolder.getInstance().claimCoordForProvince(coord, province);
					unclaimedCoords.remove(coord);
				}
			}

		}
		
		while((coordsEligibleForProvinceAssignment = rebuildPendingCoordProvinceAssignmentMap(regionName, unclaimedCoords)).size() > 0) {
			TownyProvinces.info("Num Unclaimed Chunks: " + unclaimedCoords.size());
			/*
			 * Cycle each eligible coord
			 * Assign it if it is still eligible when we get to it
			 */
			for(Coord coord: coordsEligibleForProvinceAssignment) {
				if((province = getProvinceIfCoordIsEligibleForProvinceAssignment(regionName, coord)) != null) {
					TownyProvincesDataHolder.getInstance().claimCoordForProvince(coord, province);
					unclaimedCoords.remove(coord);
				}
			}
		}
		TownyProvinces.info("Finished assigning unclaimed chunks to provinces.");
		return true;
	}

	/**
	 * You might want to remove some
	 * 
	 * @param unclaimedCoords given list of unclaimed coords
	 */
	private static void rebuildSetOfUnclaimedCoords(Set<TPCoord> unclaimedCoords) {
		String regionName = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet()).get(0);
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		unclaimedCoords.clear();
		for(int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if (!TownyProvincesDataHolder.getInstance().isCoordClaimedByProvince(x, z)) {
					unclaimedCoords.add(new Coord(x, z));
				}
			}
		}
	}

	/**
	 * Some coords may now be eligible. Some may be ineligible. Rebuild
	 * 
	 * @param allUnclaimedCoords
	 */
	private static void rebuildPendingCoordProvinceAssignmentMap(Set<TPCoord> allUnclaimedCoords, Map<TPCoord, Province> pendingCoordProvinceAssignmentMap) {
		//Pick first region as it should be the whole map
		String regionName = TownyProvincesSettings.getNameOfFirstRegion();
		//Clear map
		pendingCoordProvinceAssignmentMap.clear();
		//Rebuild map
		for (TPCoord unclaimedCoord : allUnclaimedCoords) {
			Province province = getProvinceIfCoordIsEligibleForProvinceAssignment(regionName, unclaimedCoord);
			if(province != null) {
				pendingCoordProvinceAssignmentMap.put(unclaimedCoord, province);
			}
		}
	}

	private static Province getProvinceIfCoordIsEligibleForProvinceAssignment(String regionName, TPCoord candidateCoord) {
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();

		//Filter out chunk if it is at edge of map
		if(candidateCoord.getX() <= minX)
			return null;
		else if (candidateCoord.getX() >= maxX)
			return null;
		else if (candidateCoord.getZ() <= minZ)
			return null;
		else if (candidateCoord.getZ() >= maxZ)
			return null;

		//Check cardinal direction
		Province province;
		Province result = null;
		int[] x = new int[]{0,-1,1,0};
		int[] z = new int[]{-1,0,0,1};
		for(int i = 0; i < 4; i++) {
			province = TownyProvincesDataHolder.getInstance().getProvinceAt(x[i], z[i]);
			if (province != null) {
				if(result == null) {
					result = province;
				} else {
					return null; //Can't have 2 adjacent provinces
				}
			}
		}

		if(result == null)
			return null; //No province found cardinally
		
		//Check non-cardinal
		x = new int[]{-1,1,1,-1};
		z = new int[]{-1,-1,1,1};
		for(int i = 0; i < 4; i++) {
			province = TownyProvincesDataHolder.getInstance().getProvinceAt(x[i], z[i]);
			if (province != null) {
				return null; //A winner can't have any adjacent in non-cardinal
			}
		}
		return result;
	}
	
	public static Set<Coord> findAllAdjacentCoords(Coord targetCoord) {
		Set<Coord> result = new HashSet<>();
		int[] x = new int[]{-1,0,1,-1,1,-1,0,1};
		int[] z = new int[]{-1,-1,-1,0,0,1,1,1};
		for(int i = 0; i < 8; i++) {
			result.add(new Coord(targetCoord.getX() + x[i], targetCoord.getZ() + z[i]));
		}
		return result;
	}

	private static boolean executeChunkClaimCompetition(String regionName) {
		TownyProvinces.info("Chunk Claim Competition Started");
		
		//Create claim-brush objects
		List<ProvinceClaimBrush> provinceClaimBrushes = new ArrayList<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			provinceClaimBrushes.add(new ProvinceClaimBrush(province, TownyProvincesSettings.getProvinceCreatorBrushSquareRadiusInChunks(regionName)));
		}
		
		//First claim once around the homeblocks
		for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
			claimChunksCoveredByBrush(regionName, provinceClaimBrush);
		}
		
		//Execute province painting competition
		int numPaintingCycles =  TownyProvincesSettings.getNumberOfProvincePaintingCycles(regionName);
		for(int i = 0; i < numPaintingCycles; i++) {
			TownyProvinces.info("Painting Cycle: " + i + " / " + numPaintingCycles);
			for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
				//If inactive, do nothing
				if(!provinceClaimBrush.isActive())
					continue;
				//Generate random move delta
				int minMoveAmount = TownyProvincesSettings.getProvinceCreatorBrushMinMoveInChunks(regionName);
				int maxMoveAmount = TownyProvincesSettings.getProvinceCreatorBrushMaxMoveInChunks(regionName);
				int moveDeltaX = TownyProvincesMathUtil.generateRandomInteger(-maxMoveAmount, maxMoveAmount);
				int moveDeltaZ = TownyProvincesMathUtil.generateRandomInteger(-maxMoveAmount, maxMoveAmount);
				//Apply min move amount
				moveDeltaX = moveDeltaX > 0 ? Math.max(moveDeltaX,minMoveAmount) : Math.min(moveDeltaX,-minMoveAmount);
				moveDeltaZ = moveDeltaZ > 0 ? Math.max(moveDeltaZ,minMoveAmount) : Math.min(moveDeltaZ,-minMoveAmount);
				//Move brush if possible
				Coord destination = new Coord(provinceClaimBrush.getCurrentPosition().getX() + moveDeltaX, provinceClaimBrush.getCurrentPosition().getZ() + moveDeltaZ);
				moveBrushIfPossible(provinceClaimBrush, destination);
				//Claim chunks
				claimChunksCoveredByBrush(regionName, provinceClaimBrush);
				//Deactivate if too many chunks have been claimed
				if(hasBrushHitClaimLimit(regionName, provinceClaimBrush)) {
					provinceClaimBrush.setActive(false);
				}
			}
		}
		TownyProvinces.info("Chunk Claim Competition Complete. Total Chunks Claimed: " + TownyProvincesDataHolder.getInstance().getCoordProvinceMap().size());
		return true;
	}

	private static boolean hasBrushHitClaimLimit(String regionName, ProvinceClaimBrush provinceClaimBrush) {
		double chunkArea = Math.pow(TownyProvincesSettings.getProvinceBlockSideLength(), 2);
		double currentClaimArea = provinceClaimBrush.getProvince().getCoordsInProvince().size() * chunkArea; 
		double claimAreaLimit = TownyProvincesSettings.getProvinceCreatorBrushClaimLimitInSquareMetres(regionName);
		return currentClaimArea > claimAreaLimit;
	}

	/**
	 * Move the brush unless the new position would be too close to another province.
	 * 
	 * @param brush given brush
	 * @param destinationCoord destination coord
	 */
	private static void moveBrushIfPossible(ProvinceClaimBrush brush, Coord destinationCoord) {
		if(validateBrushPosition(destinationCoord, brush.getSquareRadius(), brush.getProvince())) {
			brush.moveBrush(destinationCoord);
		}
	}

	/**
	 * Validate that it is ok to put the brush at the given coord.
	 * - The only reason it might not be ok, is if another province is nearby.
	 * - Note that being at the edge of the map is not a problem. The brush will just paint what it can
	 * 
	 * @return true if it's ok
	 */
	public static boolean validateBrushPosition(Coord paintingPosition, int squareRadius, Province provinceBeingPainted) {
		int brushMinX = paintingPosition.getX() - squareRadius;
		int brushMaxX = paintingPosition.getX() + squareRadius;
		int brushMinZ = paintingPosition.getZ() - squareRadius;
		int brushMaxZ = paintingPosition.getZ() + squareRadius;
		Coord coord;
		Province province;
		for(int x = brushMinX -3; x <= (brushMaxX +3); x++) {
			for(int z = brushMinZ -3; z <= (brushMaxZ +3); z++) {
				//Don't move near different province
				coord = new Coord(x,z);
				province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord);
				if(province != null && province != provinceBeingPainted) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Claim chunks covered by brush, 
	 * except for those near the world border or covered by other provinces
	 * 
	 * @param brush the brush
	 */
	private static void claimChunksCoveredByBrush(String regionName, ProvinceClaimBrush brush) {
		int startX = brush.getCurrentPosition().getX() - brush.getSquareRadius();
		int endX = brush.getCurrentPosition().getX() + brush.getSquareRadius();
		int startZ = brush.getCurrentPosition().getZ() - brush.getSquareRadius();
		int endZ = brush.getCurrentPosition().getZ() + brush.getSquareRadius();
		for(int x = startX; x <= endX; x++) {
			for(int z = startZ; z <= endZ; z++) {
				//Claim chunk
				claimChunk(regionName, x, z, brush.getProvince());
			}
		}
	}

	/**
	 * Claim the given chunk, unless another province has already claimed it
	 * Or near edge of map, or near other province,
	 * @param province the province doing the claiming
	 */
	private static void claimChunk(String regionName, int coordX, int coordZ, Province province) {
		//Don't claim if already claimed
		if (TownyProvincesDataHolder.getInstance().getProvinceAt(coordX, coordZ) != null)
			return;

		
		
		
		
		//Don't claim near other province
		Set<Coord> adjacentCoords = findAllAdjacentCoords(coord);
		Province adjacentProvince;
		for(Coord adjacentCoord: adjacentCoords) {
			adjacentProvince = TownyProvincesDataHolder.getInstance().getProvinceAt(adjacentCoord);
			if(adjacentProvince != null &&adjacentProvince != province) {
				return;
			}
		}
		
		//Don't claim at edge of map
		int minX = (TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxX  = (TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		int minZ = (TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxZ  = (TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		if(coordX < minX)
			return;
		else if (coordX > maxX)
			return;
		else if (coordZ < minZ)
			return;
		else if (coordZ > maxZ)
			return;
		
		//Claim coord
		TownyProvincesDataHolder.getInstance().claimCoordForProvince(coord, province);
	}

	/**
	 * Generate province objects, including
	 * - Homeblocks
	 * - New town prices
	 * - Upkeep town prices
	 * 
	 * @return false if we failed to create sufficient province objects
	 */
	private static boolean generateProvinceObjects(String regionName) {
		TownyProvinces.info("Now generating province objects");
		
		Province province;
		int idealNumberOfProvinces = calculateIdealNumberOfProvinces(regionName);
		for (int provinceIndex = 0; provinceIndex < idealNumberOfProvinces; provinceIndex++) {
			province = generateProvinceObject(regionName);
			if(province != null) { 
				//Province object created successfully. Add to data holder
				TownyProvincesDataHolder.getInstance().addProvince(province);
			} else {
				//Could not generate a province homeblock. Ran out of space on the map
				double allowedVariance = TownyProvincesSettings.getMaxAllowedVarianceBetweenIdealAndActualNumProvinces(regionName);
				double minimumAllowedNumProvinces = ((double) idealNumberOfProvinces) * (1 - allowedVariance);
				int actualNumProvinces = TownyProvincesDataHolder.getInstance().getNumProvinces();
				if (actualNumProvinces < minimumAllowedNumProvinces) {
					TownyProvinces.severe("ERROR: Could not create the minimum number of provinces objects. Required: " + minimumAllowedNumProvinces + ". Actual: " + actualNumProvinces);
					return false;
				} else {
					TownyProvinces.info("" + actualNumProvinces + " province objects created.");
					return true;
				}
			}
		}
		TownyProvinces.info("" + TownyProvincesDataHolder.getInstance().getNumProvinces() + " province objects created.");
		return true;
	}

	/**
	 * Generate a single province object
	 * 
	 * @param regionName given region name
	 * @return the province on success, or null if you fail (usually due to map being full)
	 */
	private static @Nullable Province generateProvinceObject(String regionName) {
		double tpChunkSideLength = TownyProvincesSettings.getProvinceBlockSideLength();
		int newTownCost = TownyProvincesSettings.getNewTownCost(regionName);
		int upkeepTownCost = TownyProvincesSettings.getUpkeepTownCost(regionName);
		boolean isSea = false;
		boolean landValidationRequested = false;
		for(int i = 0; i < 100; i++) {
			
			//Establish boundaries of where it might be
			double xLowest = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() +1;
			double xHighest = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() -1;
			double zLowest = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() +1;
			double zHighest = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() -1;
			
			//Pick a random location
			double x = xLowest + (Math.random() * (xHighest - xLowest));
			double z = zLowest + (Math.random() * (zHighest - zLowest));
			int xCoord = (int)(x / tpChunkSideLength);
			int zCoord = (int)(z / tpChunkSideLength);
			Coord generatedHomeBlockCoord = new Coord(xCoord, zCoord);
			
			//Create province object
			Province province = new Province(generatedHomeBlockCoord, isSea, landValidationRequested, newTownCost, upkeepTownCost);
			
			//Validate province homeblock position
			if(validatePositionOfProvinceHomeBlock(province, regionName)) {
				return province;
			}
		}
		return null;
	}
	
	private static boolean validatePositionOfProvinceHomeBlock(Province newProvince, String regionName) {
		//Make sure it is far enough from other homeblocks
		Coord provinceHomeBlock = newProvince.getHomeBlock();
		int minAllowedDistanceInMetres = TownyProvincesSettings.getMinAllowedDistanceBetweenProvinceHomeBlocks(regionName);
		int minAllowedDistanceInChunks = minAllowedDistanceInMetres / TownyProvincesSettings.getProvinceBlockSideLength();
		List<Province> provinceList = TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList();
		for(Province province: provinceList) {
			if(MathUtil.distance(provinceHomeBlock, province.getHomeBlock()) < minAllowedDistanceInChunks) {
				return false;
			}
		}
		//Make sure that it is far enough from other provinces
		if(validateBrushPosition(provinceHomeBlock, TownyProvincesSettings.getProvinceCreatorBrushSquareRadiusInChunks(regionName), newProvince)) {
			return true;
		} else {
			return false;
		}
	}
	
	private static int calculateIdealNumberOfProvinces(String regionName) {
		double worldAreaSquareMetres = calculateWorldAreaSquareMetres(regionName);
		double averageProvinceAreaSquareMetres = TownyProvincesSettings.getProvinceSizeEstimateForPopulatingInSquareMetres(regionName);
		int idealNumberOfProvinces = (int)(worldAreaSquareMetres / averageProvinceAreaSquareMetres);
		TownyProvinces.info("Ideal num provinces: " + idealNumberOfProvinces);
		return idealNumberOfProvinces;
	}
	
	private static double calculateWorldAreaSquareMetres(String regionName) {
		Location topLeftCorner = TownyProvincesSettings.getTopLeftCornerLocation(regionName);
		Location bottomRightCorner = TownyProvincesSettings.getBottomRightCornerLocation(regionName);
		double sideLengthX = Math.abs(topLeftCorner.getX() - bottomRightCorner.getX());
		double sideLengthZ = Math.abs(topLeftCorner.getZ() - bottomRightCorner.getZ());
		double worldAreaSquareMetres = sideLengthX * sideLengthZ;
		TownyProvinces.info("World Area square metres: " + worldAreaSquareMetres);
		return worldAreaSquareMetres;
	}
}

 