package io.github.townyadvanced.townyprovinces.jobs.province_generation;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFinalCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.TownyProvincesMathUtil;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to paint a single region
 */
public class PaintRegionAction {
	private final String regionName;  //Name of real region (not "All")
	private final Map<TPCoord, TPCoord> unclaimedCoordsMap;
	private final int regionMinX;
	private final int regionMaxX;
	private final int regionMinZ;
	private final int regionMaxZ;
	private final int mapMinXCoord;
	private final int mapMaxXCoord;
	private final int mapMinZCoord;
	private final int mapMaxZCoord;
	private final int minBrushMoveAmountInChunks;
	private final int maxBrushMoveAmountInChunks;
	private final int brushSquareRadiusInChunks;
	private final int claimAreaLimitInSquareMetres;
	public final static double CHUNK_AREA_IN_SQUARE_METRES = Math.pow(TownyProvincesSettings.getChunkSideLength(), 2);
	public final TPFreeCoord searchCoord;
	private final int minimumStartDistanceBetweenBrushesInChunks;
	private final Location topLeftRegionCorner;
	private final Location bottomRightRegionCorner;
	private final int averageProvinceSize;
	private final int maxBrushMoves;
	private final double tpChunkSideLength;
	private final int newTownCost;
	private final int upkeepTownCost;
	private final double allowedVarianceBetweenIdealAndActualNumProvinces;

	public PaintRegionAction(String regionName, Map<TPCoord,TPCoord> unclaimedCoordsMap) {
		this.regionName = regionName;
		this.unclaimedCoordsMap = unclaimedCoordsMap;
		this.minBrushMoveAmountInChunks = TownyProvincesSettings.getMinBrushMoveAmountInChunks(regionName);
		this.maxBrushMoveAmountInChunks = TownyProvincesSettings.getMaxBrushMoveAmountInChunks(regionName);
		this.brushSquareRadiusInChunks = TownyProvincesSettings.getBrushSquareRadiusInChunks(regionName);
		this.claimAreaLimitInSquareMetres = TownyProvincesSettings.getMaxAreaClaimedPerBrush(regionName);
		String nameOfFirstRegion = TownyProvincesSettings.getNameOfFirstRegion();
		this.mapMinXCoord = TownyProvincesSettings.getTopLeftCornerLocation(nameOfFirstRegion).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		this.mapMaxXCoord = TownyProvincesSettings.getBottomRightCornerLocation(nameOfFirstRegion).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		this.mapMinZCoord = TownyProvincesSettings.getTopLeftCornerLocation(nameOfFirstRegion).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		this.mapMaxZCoord = TownyProvincesSettings.getBottomRightCornerLocation(nameOfFirstRegion).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		this.searchCoord = new TPFreeCoord(0,0);
		int minimumStartDistanceBetweenBrushes = TownyProvincesSettings.getMinimumStartDistanceBetweenBrushes(regionName);
		this.minimumStartDistanceBetweenBrushesInChunks = minimumStartDistanceBetweenBrushes / TownyProvincesSettings.getChunkSideLength();
		this.regionMinX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX();
		this.regionMaxX = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX();
		this.regionMinZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ();
		this.regionMaxZ = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ();
		this.topLeftRegionCorner = TownyProvincesSettings.getTopLeftCornerLocation(regionName);
		this.bottomRightRegionCorner = TownyProvincesSettings.getBottomRightCornerLocation(regionName);
		this.averageProvinceSize = TownyProvincesSettings.getAverageProvinceSize(regionName);
		this.maxBrushMoves = TownyProvincesSettings.getMaxBrushMoves(regionName);
		this.tpChunkSideLength = TownyProvincesSettings.getChunkSideLength();
		this.newTownCost = TownyProvincesSettings.getNewTownCost(regionName);
		this.upkeepTownCost = TownyProvincesSettings.getUpkeepTownCost(regionName);
		this.allowedVarianceBetweenIdealAndActualNumProvinces = TownyProvincesSettings.getMaxAllowedVarianceBetweenIdealAndActualNumProvinces(regionName);
	}
	
	boolean executeAction(boolean deleteExistingProvincesInRegion) {
		TownyProvinces.info("Now Painting Provinces In Region: " + regionName);

		//Delete most provinces in the region, except those which are mostly outside
		if(deleteExistingProvincesInRegion) {
			if(!deleteExistingProvincesWhichAreMostlyInSpecifiedArea()) {
				return false;
			}
		}

		/*
		 * Create province objects - empty except for the homeblocks
		 * This is the start point for the paint brushes
		 */
		if(!generateProvinceObjects()) {
			return false;
		}

		//Execute chunk claim competition
		if(!executeChunkClaimCompetition()) {
			return false;
		}

		//Allocate unclaimed chunks to provinces.
		if(!assignUnclaimedCoordsToProvinces()) {
			TownyProvinces.info("Problem assigning unclaimed chunks to provinces");
			return false;
		}

		//Delete empty provinces
		if(!deleteEmptyProvinces()) {
			TownyProvinces.info("Problem deleting empty provinces");
			return false;
		}

		TownyProvinces.info("Finished Painting Provinces In Region: " + regionName);
		return true;
	}

	private boolean deleteExistingProvincesWhichAreMostlyInSpecifiedArea() {
		TownyProvinces.info("Now deleting provinces which are mostly in the specified area.");
		int numProvincesDeleted = 0;
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		for(Province province: (new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet()))) {
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
				TownyProvincesDataHolder.getInstance().deleteProvince(province, unclaimedCoordsMap);
				numProvincesDeleted++;
			}
		}
		TownyProvinces.info("" + numProvincesDeleted + " provinces deleted.");
		return true;
	}

	/**
	 * Generate province objects, including
	 * - Homeblocks
	 * - New town costs
	 * - Upkeep town costs
	 *
	 * @return false if we failed to create sufficient province objects
	 */
	private boolean generateProvinceObjects() {
		TownyProvinces.info("Now generating province objects");
		Province province;
		int idealNumberOfProvinces = calculateIdealNumberOfProvinces();
		int provincesCreated = 0;
		for (int provinceIndex = 0; provinceIndex < idealNumberOfProvinces; provinceIndex++) {
			province = generateProvinceObject();
			if(province != null) {
				//Province object created successfully. Add to data holder
				TownyProvincesDataHolder.getInstance().addProvince(province);
				provincesCreated++;
			} else {
				//Could not generate a province homeblock. Ran out of space on the map
				double minimumAllowedNumProvinces = ((double) idealNumberOfProvinces) * (1 - allowedVarianceBetweenIdealAndActualNumProvinces);
				//int actualNumProvinces = TownyProvincesDataHolder.getInstance().getNumProvinces();
				if (provincesCreated < minimumAllowedNumProvinces) {
					TownyProvinces.severe("ERROR: Could not create the minimum number of provinces objects. Required: " + minimumAllowedNumProvinces + ". Actual: " + provincesCreated);
					return false;
				} else {
					TownyProvinces.info("" + provincesCreated + " province objects created.");
					return true;
				}
			}
		}
		TownyProvinces.info("" + provincesCreated + " province objects created.");
		return true;
	}
	
	private int calculateIdealNumberOfProvinces() {
		double regionAreaSquareMetres = calculateRegionAreaSquareMetres();
		int idealNumberOfProvinces = (int)(regionAreaSquareMetres / averageProvinceSize);
		TownyProvinces.info("Ideal num provinces: " + idealNumberOfProvinces);
		return idealNumberOfProvinces;
	}

	private double calculateRegionAreaSquareMetres() {
		double sideLengthX = Math.abs(topLeftRegionCorner.getX() - bottomRightRegionCorner.getX());
		double sideLengthZ = Math.abs(topLeftRegionCorner.getZ() - bottomRightRegionCorner.getZ());
		double worldAreaSquareMetres = sideLengthX * sideLengthZ;
		TownyProvinces.info("World Area square metres: " + worldAreaSquareMetres);
		return worldAreaSquareMetres;
	}
	
	/**
	 * Generate a single province object
	 *
	 * @return the province on success, or null if you fail (usually due to map being full)
	 */
	private @Nullable Province generateProvinceObject() {
		boolean isSea = false;
		boolean landValidationRequested = false;
		//Establish boundaries of where the homeblock might be placed
		double xLowest = regionMinX + brushSquareRadiusInChunks + 3;
		double xHighest = regionMaxX - brushSquareRadiusInChunks - 3;
		double zLowest = regionMinZ + brushSquareRadiusInChunks + 3;
		double zHighest = regionMaxZ - brushSquareRadiusInChunks - 3;
		//Try a few times to place the homeblock
		for(int i = 0; i < 100; i++) {
			//Pick a random location
			double x = xLowest + (Math.random() * (xHighest - xLowest));
			double z = zLowest + (Math.random() * (zHighest - zLowest));
			Coord coord = Coord.parseCoord((int)x,(int)z);
			int xCoord = coord.getX();
			int zCoord = coord.getZ();
			TPCoord homeBlockCoord = new TPFinalCoord(xCoord, zCoord);
			//Create province object
			Province province = new Province(homeBlockCoord, isSea, landValidationRequested, newTownCost, upkeepTownCost);
			//Validate province homeblock position
			if(validatePositionOfProvinceHomeBlock(province)) {
				return province;
			}
		}
		return null;
	}
	
	private boolean validatePositionOfProvinceHomeBlock(Province provinceTovalidate) {
		//Make sure it is far enough from other homeblocks
		TPCoord homeBlockToValidate = provinceTovalidate.getHomeBlock();
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			/* 
			* The minimum allowed distance is:
			* - (The configured min distance), or
			* - (twice the brush paint radius + 1)
			* - Whichever is larger			 
			 */
			int minAllowedDistance = minimumStartDistanceBetweenBrushesInChunks + (brushSquareRadiusInChunks * 2) + 1;
			if(TownyProvincesMathUtil.minecraftDistance(homeBlockToValidate, province.getHomeBlock()) < minAllowedDistance) {
				return false;
			}
		}
		//Make sure that it doesn't overlap existing claims, or go off the map border
		if(validateBrushPosition(homeBlockToValidate.getX(), homeBlockToValidate.getZ(), provinceTovalidate)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean executeChunkClaimCompetition() {
		TownyProvinces.info("Chunk Claim Competition Started");

		//Create claim-brush objects
		List<ProvinceClaimBrush> provinceClaimBrushes = new ArrayList<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			provinceClaimBrushes.add(new ProvinceClaimBrush(province, brushSquareRadiusInChunks));
		}
		
		/*
		 * First claim once around the homeblocks
		 * Note: We assume that the homeblocks have all been put in valid positoins
		 */
		for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
			claimChunksCoveredByBrush(provinceClaimBrush);
		}

		//Execute province painting competition
		int moveDeltaX;
		int moveDeltaZ;
		int newX;
		int newZ;
		for(int i = 0; i < maxBrushMoves; i++) {
			TownyProvinces.info("Painting Cycle: " + i + " / " + maxBrushMoves);
			for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
				//If inactive, do nothing
				if(!provinceClaimBrush.isActive())
					continue;
				//Generate random move delta
				moveDeltaX = TownyProvincesMathUtil.generateRandomInteger(-maxBrushMoveAmountInChunks, maxBrushMoveAmountInChunks);
				moveDeltaZ = TownyProvincesMathUtil.generateRandomInteger(-maxBrushMoveAmountInChunks, maxBrushMoveAmountInChunks);
				//Apply min move amount
				moveDeltaX = moveDeltaX > 0 ? Math.max(moveDeltaX, minBrushMoveAmountInChunks) : Math.min(moveDeltaX,-minBrushMoveAmountInChunks);
				moveDeltaZ = moveDeltaZ > 0 ? Math.max(moveDeltaZ, minBrushMoveAmountInChunks) : Math.min(moveDeltaZ,-minBrushMoveAmountInChunks);
				//Move brush if possible
				newX = provinceClaimBrush.getCurrentPosition().getX() + moveDeltaX;
				newZ = provinceClaimBrush.getCurrentPosition().getZ() + moveDeltaZ;
				
				//If new position is good, move and claim
				if(validateBrushPosition(newX, newZ, provinceClaimBrush.getProvince())) {
					provinceClaimBrush.moveBrushTo(newX, newZ);
					claimChunksCoveredByBrush(provinceClaimBrush);
					//Deactivate if too many chunks have been claimed
					if(hasBrushHitClaimLimit(provinceClaimBrush)) {
						provinceClaimBrush.setActive(false);
					}
				}
			}
		}
		TownyProvinces.info("Chunk Claim Competition Complete.");
		TownyProvinces.info("Num Chunks Claimed: " + TownyProvincesDataHolder.getInstance().getCoordProvinceMap().size());
		TownyProvinces.info("Num Chunks Unclaimed: " + unclaimedCoordsMap.size());
		return true;
	}
	
	/**
	 * Validate that it is ok to put the brush at the given coord.
	 * It is not ok if:
	 * - It would paint off the map
	 * - It would paint on another province
	 *
	 * @return true if it's ok
	 */
	public boolean validateBrushPosition(int brushPositionCoordX, int brushPositionCoordZ, Province provinceBeingPainted) {
		//First check that the centre point would not be off the map
		if (brushPositionCoordX < mapMinXCoord)
			return false;
		else if (brushPositionCoordX > mapMaxXCoord)
			return false;
		else if (brushPositionCoordZ < mapMinZCoord)
			return false;
		else if (brushPositionCoordZ > mapMaxZCoord)
			return false;
		//Now check that none of the painting points would be on or near a different province
		int brushMinCoordX = brushPositionCoordX - brushSquareRadiusInChunks;
		int brushMaxCoordX = brushPositionCoordX + brushSquareRadiusInChunks;
		int brushMinCoordZ = brushPositionCoordZ - brushSquareRadiusInChunks;
		int brushMaxCoordZ = brushPositionCoordZ + brushSquareRadiusInChunks;
		Province province;
		for(int x = brushMinCoordX -1; x <= (brushMaxCoordX +1); x++) {
			for(int z = brushMinCoordZ -1; z <= (brushMaxCoordZ +1); z++) {
				province = TownyProvincesDataHolder.getInstance().getProvinceAt(x,z);
				if(province != null && province != provinceBeingPainted) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Claim chunks covered by brush
	 * Assume that all the checks related to other provinces
	 * and the edge of the map, have already been done
	 *
	 * @param brush the brush
	 */
	private void claimChunksCoveredByBrush(ProvinceClaimBrush brush) {
		if(!validateBrushPosition(brush.getCurrentPosition().getX(), brush.getCurrentPosition().getZ(), brush.getProvince()))
			return;
			
		int startX = brush.getCurrentPosition().getX() - brush.getSquareRadius();
		int endX = brush.getCurrentPosition().getX() + brush.getSquareRadius();
		int startZ = brush.getCurrentPosition().getZ() - brush.getSquareRadius();
		int endZ = brush.getCurrentPosition().getZ() + brush.getSquareRadius();
		for(int x = startX; x <= endX; x++) {
			for(int z = startZ; z <= endZ; z++) {
				//Claim chunk if not already claimed by the province
				searchCoord.setValues(x,z);
				if(unclaimedCoordsMap.containsKey(searchCoord)) {
					TownyProvincesDataHolder.getInstance().claimCoordForProvince(unclaimedCoordsMap.get(searchCoord), brush.getProvince());
					brush.registerChunkClaimed();
					unclaimedCoordsMap.remove(searchCoord);
				}
			}
		}
	}

	/**
	 * Claim the given chunk, unless another province has already claimed it
	 * Or near edge of map, or near other province,
	 */
	private void claimUnclaimedChunk(int coordX, int coordZ, ProvinceClaimBrush brush) {
		//Don't claim if already claimed
		searchCoord.setValues(coordX,coordZ);
		if(!unclaimedCoordsMap.containsKey(searchCoord))
			return;

		//TODO - I'm not sure why I have to do this....
		//Don't claim near other provinces
		Province province = brush.getProvince();
		Province adjacentProvince;
		int[] x = new int[]{-1,0,1,-1,1,-1,0,1};
		int[] z = new int[]{-1,-1,-1,0,0,1,1,1};
		for(int i = 0; i < 8; i++) {
			adjacentProvince = TownyProvincesDataHolder.getInstance().getProvinceAt(coordX + x[i], coordZ + z[i]);
			if(adjacentProvince != null && adjacentProvince != province) {
				return;
			}
		}

		//Claim chunk
		TownyProvincesDataHolder.getInstance().claimCoordForProvince(unclaimedCoordsMap.get(searchCoord), province);
		brush.registerChunkClaimed();
		unclaimedCoordsMap.remove(searchCoord);
	}


	private boolean hasBrushHitClaimLimit(ProvinceClaimBrush provinceClaimBrush) {
		double currentClaimArea = provinceClaimBrush.getNumChunksClaimed() * CHUNK_AREA_IN_SQUARE_METRES;
		return currentClaimArea > claimAreaLimitInSquareMetres;
	}


	/**
	 * Assign unclaimed coords to provinces, until you can assign no more
	 */
	private boolean assignUnclaimedCoordsToProvinces() {
		TownyProvinces.info("Now assigning unclaimed chunks to provinces.");
		Map<TPCoord, Province> pendingCoordProvinceAssignments = new HashMap<>();
		double totalChunksOnMap = (mapMaxXCoord - mapMinXCoord) * (mapMaxZCoord - mapMinZCoord);
		while(true) {
			//Rebuild the map of pending coord-province assignments
			rebuildPendingCoordProvinceAssignmentMap(pendingCoordProvinceAssignments);
			double totalClaimedChunks = totalChunksOnMap - unclaimedCoordsMap.size();
			int percentageChunksClaimed = (int)((totalClaimedChunks / totalChunksOnMap) * 100);
			TownyProvinces.info("Assigning Unclaimed Chunks. Progress: " + percentageChunksClaimed + "%");
			//Exit loop if there are no more pending assignments
			if(pendingCoordProvinceAssignments.size() == 0) {
				break;
			}
			/*
			 * Do all the pending assignments
			 * Except those which are no longer valid when you get to them
			 */
			for(Map.Entry<TPCoord,Province> mapEntry: pendingCoordProvinceAssignments.entrySet()) {
				if(verifyCoordEligibilityForProvinceAssignment(mapEntry.getKey())) {
					TownyProvincesDataHolder.getInstance().claimCoordForProvince(mapEntry.getKey(), mapEntry.getValue());
					unclaimedCoordsMap.remove(mapEntry.getKey());
				}
			}
		}
		TownyProvinces.info("Assigning Unclaimed Chunks. Progress: 100%");
		TownyProvinces.info("Finished assigning unclaimed chunks to provinces.");
		return true;
	}

	/**
	 * Some coords may now be eligible. Some may be ineligible. Rebuild
	 */
	private void rebuildPendingCoordProvinceAssignmentMap(Map<TPCoord, Province> pendingCoordProvinceAssignmentMap) {
		//Clear map
		pendingCoordProvinceAssignmentMap.clear();
		//Rebuild map
		for (TPCoord unclaimedCoord : unclaimedCoordsMap.values()) {
			Province province = getProvinceIfUnclaimedCoordIsEligibleForProvinceAssignment(unclaimedCoord);
			if(province != null) {
				pendingCoordProvinceAssignmentMap.put(unclaimedCoord, province);
			}
		}
	}

	private boolean verifyCoordEligibilityForProvinceAssignment(TPCoord coord) {
		Province province = getProvinceIfUnclaimedCoordIsEligibleForProvinceAssignment(coord);
		return province != null;
	}

	/**
	 * Eligibility rules:
	 * 1. At least one claimed chunk must be found cardinally
	 * 2. If any adjacent claimed chunks are found, they must all belong to the same province.
	 *
	 * @param unclaimedCoord the unclaimed coord
	 * @return the province to assign it to
	 */
	private Province getProvinceIfUnclaimedCoordIsEligibleForProvinceAssignment(TPCoord unclaimedCoord) {
		//Filter out chunk if it is at edge of map
		if(unclaimedCoord.getX() < mapMinXCoord)
			return null;
		else if (unclaimedCoord.getX() > mapMaxXCoord)
			return null;
		else if (unclaimedCoord.getZ() < mapMinZCoord)
			return null;
		else if (unclaimedCoord.getZ() > mapMaxZCoord)
			return null;

		//Check cardinal direction
		Province result = null;
		Province province;
		int[] x = new int[]{0,0,1,-1};
		int[] z = new int[]{-1,1,0,0};
		for(int i = 0; i < 4; i++) {
			province = TownyProvincesDataHolder.getInstance().getProvinceAt(unclaimedCoord.getX() + x[i], unclaimedCoord.getZ() + z[i]);
			if (province != null) {
				if(result == null) {
					result = province;
				} else if (province != result) {
					return null; //2 different adjacent provinces found. Return null, as this chunk will be a border.
				}
			}
		}

		if(result == null)
			return null; //No province found cardinally

		//Check non-cardinal
		x = new int[]{-1,1,1,-1};
		z = new int[]{-1,-1,1,1};
		for(int i = 0; i < 4; i++) {
			province = TownyProvincesDataHolder.getInstance().getProvinceAt(unclaimedCoord.getX() + x[i], unclaimedCoord.getZ() + z[i]);
			if (province != null && province != result) {
				return null; //2 different adjacent provinces found. Return null, as this chunk will be a border.
			}
		}
		return result;
	}


	private  boolean deleteEmptyProvinces() {
		TownyProvinces.info("Now Deleting Empty Provinces.");
		Set<Province> provincesToDelete = new HashSet<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			if(province.getCoordsInProvince().size() == 0) {
				provincesToDelete.add(province);
			}
		}
		for(Province province: provincesToDelete) {
			TownyProvincesDataHolder.getInstance().deleteProvince(province, unclaimedCoordsMap);
		}
		TownyProvinces.info("Empty Provinces Deleted.");
		return true;
	}


}
