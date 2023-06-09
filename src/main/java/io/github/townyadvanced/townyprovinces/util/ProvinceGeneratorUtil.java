package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProvinceGeneratorUtil {
	
	/**
	 * Generate all provinces in the world
	 */
	public static boolean regenerateAllRegions() {
		//Create region definitions folder and sample files is needed
		FileUtil.createRegionDefinitionsFolderAndSampleFiles();
		//Reload region definitions
		TownyProvincesSettings.loadRegionDefinitions();
		//Delete all Provinces
		TownyProvincesDataHolder.getInstance().deleteAllProvinces();
		//Paint all Provinces
		List<String> regionNames = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet());
		Collections.sort(regionNames); //Sort in alphabetical order
		for (String regionName: regionNames) {
			if(!paintProvincesInRegion(regionName)) {
				return false;
			}
		}
		//Allocate unclaimed chunks to provinces.
		if(!assignUnclaimedChunksToProvinces()) {
			return false;
		}
		if(!deleteEmptyProvinces()) {
			return false;
		}
		return true;
	}

	/**
	 * Generate provinces in just one region
	 * @param regionName given region name
	 * @return true if success
	 */
	public static boolean regenerateOneRegion(String regionName) {
		//Create region definitions folder and sample files is needed
		FileUtil.createRegionDefinitionsFolderAndSampleFiles();
		//Reload region definitions
		TownyProvincesSettings.loadRegionDefinitions();
		//Paint region
		if(!paintProvincesInRegion(regionName)) {
			return false;
		}
		//Allocate unclaimed chunks
		if(!assignUnclaimedChunksToProvinces()) {
			return false;
		}
		if(!deleteEmptyProvinces()) {
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
			List<Coord> coordsInProvince = province.getCoordsInProvince();
			int numProvinceBlocksInSpecifiedArea = 0;
			for (Coord coordInProvince : coordsInProvince) {
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
	
	private static Set<Coord> findAllUnclaimedCoords() {
		String regionName = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet()).get(0);
		Set<Coord> result = new HashSet<>();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		Coord coord;
		Province province;
		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				coord = new Coord(x,z);
				province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord);
				if(province == null) {
					//This means the chunk is unclaimed
					result.add(coord);
				}
			}
		}
		return result;
	}
	
	/**
	 * Assign unclaimed chunks to provinces where possible
	 * 
	 * @return true on method success
	 */
	private static boolean assignUnclaimedChunksToProvinces() {
		TownyProvinces.info("Now assigning unclaimed chunks to provinces.");
		//Use the first region
		String regionName = TownyProvincesSettings.getNameOfFirstRegion();
		List<Coord> coordsEligibleForProvinceAssignment;
		Province province;
		Set<Coord> unclaimedCoords = findAllUnclaimedCoords();
		while((coordsEligibleForProvinceAssignment = findCoordsEligibleForProvinceAssignment(regionName, unclaimedCoords)).size() > 0) {
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
	
	private static List<Coord> findCoordsEligibleForProvinceAssignment(String regionName, Set<Coord> allUnclaimedCoords) {
		List<Coord> result = new ArrayList<>();

		Province province;
		for (Coord candidateCoord : allUnclaimedCoords) {
			province = getProvinceIfCoordIsEligibleForProvinceAssignment(regionName, candidateCoord);
			if(province != null) {
				result.add(candidateCoord);
			}
		}
		return result;

	}
	
	private static Province getProvinceIfCoordIsEligibleForProvinceAssignment(String regionName, Coord candidateCoord) {
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

		//Filter out chunk if it does not have exactly 1 adjacent province
		List<Province> adjacentProvinces = findAllAdjacentProvinces(candidateCoord);
		if(adjacentProvinces.size() != 1)
			return null;

		//Filter out chunk if the adjacent province is NOT on a cardinal direction
		//TODO - Improve me combine with above
		if(findCardinallyAdjacentProvinces(candidateCoord).size() == 0)
			return null;

		//Return province
		return adjacentProvinces.get(0);
	}
	
	private static List<Province> findAllAdjacentProvinces(Coord givenCoord) {
		Set<Province> result = new HashSet<>();
		Province adjacentProvince;
		for(Coord adjacentCoord: findAllAdjacentCoords(givenCoord)) {
			adjacentProvince = TownyProvincesDataHolder.getInstance().getProvinceAt(adjacentCoord);
			if(adjacentProvince != null) {
				result.add(adjacentProvince);
			}
		}
		return new ArrayList<>(result);
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

	private static List<Province> findCardinallyAdjacentProvinces(Coord givenCoord) {
		Set<Province> result = new HashSet<>();
		Province adjacentProvince;
		for(Coord adjacentCoord: findCardinallyAdjacentCoords(givenCoord)) {
			adjacentProvince = TownyProvincesDataHolder.getInstance().getProvinceAt(adjacentCoord);
			if(adjacentProvince != null) {
				result.add(adjacentProvince);
			}
		}
		return new ArrayList<>(result);
	}
	
	private static Set<Coord> findCardinallyAdjacentCoords(Coord targetCoord) {
		Set<Coord> result = new HashSet<>();
		int[] x = new int[]{0,-1,1,0};
		int[] z = new int[]{-1,0,0,1};
		for(int i = 0; i < 4; i++) {
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
				//Deactivate if too many chuns have been claimed
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
				claimChunk(regionName, new Coord(x,z), brush.getProvince());
			}
		}
	}

	/**
	 * Claim the given chunk, unless another province has already claimed it
	 * Or near edge of map, or near other province, 
	 * @param coord co-ord of chunk
	 * @param province the province doing the claiming
	 */
	private static void claimChunk(String regionName, Coord coord, Province province) {
		//Don't claim if already claimed
		if (TownyProvincesDataHolder.getInstance().getProvinceAt(coord) != null)
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
		if(coord.getX() < minX)
			return;
		else if (coord.getX() > maxX)
			return;
		else if (coord.getZ() < minZ)
			return;
		else if (coord.getZ() > maxZ)
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

 