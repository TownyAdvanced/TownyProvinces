package io.github.townyadvanced.townyprovinces.jobs.province_generation;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.objects.Region;
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
	private final Region region;
	private final Map<TPCoord, TPCoord> unclaimedCoordsMap;
	private final int mapMinXCoord;
	private final int mapMaxXCoord;
	private final int mapMinZCoord;
	private final int mapMaxZCoord;
	public final static double CHUNK_AREA_IN_SQUARE_METRES = Math.pow(TownyProvincesSettings.getChunkSideLength(), 2);
	public final TPFreeCoord searchCoord;
	
	public PaintRegionAction(Region region, Map<TPCoord,TPCoord> unclaimedCoordsMap) {
		this.region = region;
		this.unclaimedCoordsMap = unclaimedCoordsMap;
		this.mapMinXCoord = TownyProvincesSettings.getFirstRegion().getTopLeftRegionCorner().getBlockX() / TownyProvincesSettings.getChunkSideLength();
		this.mapMaxXCoord = TownyProvincesSettings.getFirstRegion().getBottomRightRegionCorner().getBlockX() / TownyProvincesSettings.getChunkSideLength();
		this.mapMinZCoord = TownyProvincesSettings.getFirstRegion().getTopLeftRegionCorner().getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		this.mapMaxZCoord = TownyProvincesSettings.getFirstRegion().getBottomRightRegionCorner().getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		this.searchCoord = new TPFreeCoord(0,0);
	}
	
	boolean executeAction(boolean deleteExistingProvincesInRegion) {
		TownyProvinces.info("-------------------------------");
		TownyProvinces.info("Now Painting Provinces In Region: " + region.getName());

		//Delete most provinces in the region, except those which are mostly outside
		if(deleteExistingProvincesInRegion) {
			if(!deleteExistingProvincesWhichAreMostlyInSpecifiedArea()) {
				return false;
			}
		}

		/*
		 * Create provinces including the initial claimed area
		 */
		if(!generateProvinces()) {
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

		TownyProvinces.info("Finished Painting Provinces In Region: " + region.getName());
		return true;
	}

	private boolean deleteExistingProvincesWhichAreMostlyInSpecifiedArea() {
		TownyProvinces.info("Now deleting provinces which are mostly in the specified area.");
		int numProvincesDeleted = 0;
		int minX = region.getTopLeftRegionCorner().getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int maxX  = region.getBottomRightRegionCorner().getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int minZ = region.getTopLeftRegionCorner().getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		int maxZ  = region.getBottomRightRegionCorner().getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		for(Province province: (new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet()))) {
			List<TPCoord> coordsInProvince = province.getListOfCoordsInProvince();
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
	 * Generate provinces, including
	 * - Homeblocks
	 * - Initial claimed area
	 *
	 * @return false if we failed to create sufficient province objects
	 */
	private boolean generateProvinces() {
		TownyProvinces.info("Now generating province objects");
		Province province;
		int maxNumProvinces = calculateMaxNumberOfProvinces();
		int maxNumRandomProvinces = maxNumProvinces - region.getProtectedLocations().size();
		int provincesCreated = 0;
		//Generate provinces at protected locations
		for(Map.Entry<String,Location> mapEntry: region.getProtectedLocations().entrySet()) {
			TownyProvinces.info("Now generating province at protected location: " + mapEntry.getKey());
			province = generateProtectedProvince(mapEntry.getValue());
			if(province == null) {
				TownyProvinces.severe("Could not generate province at protected location: " + mapEntry.getKey());
				return false;
			} else {
				TownyProvincesDataHolder.getInstance().addProvince(province);
				provincesCreated++;
			}
		}
		//Generate provinces at random locations
		for (int randomProvinceIndex = 0; randomProvinceIndex < maxNumRandomProvinces; randomProvinceIndex++) {
			province = generateRandomlyPlacedProvince();
			if(province == null) {
				break; //We created as many as we could in this region
			} else {
				//Province object created successfully. Add to data holder
				TownyProvincesDataHolder.getInstance().addProvince(province);
				provincesCreated++;
			}
		}
		TownyProvinces.info("" + provincesCreated + " province objects created.");
		return true;
	}
	
	private int calculateMaxNumberOfProvinces() {
		double regionAreaSquareMetres = calculateRegionAreaSquareMetres();
		int maxNumProvincesProvinces = (int)(regionAreaSquareMetres / region.getAverageProvinceSize());
		TownyProvinces.info("Max num provinces: " + maxNumProvincesProvinces);
		return maxNumProvincesProvinces;
	}

	private double calculateRegionAreaSquareMetres() {
		double sideLengthX = Math.abs(region.getTopLeftRegionCorner().getX() - region.getBottomRightRegionCorner().getX());
		double sideLengthZ = Math.abs(region.getTopLeftRegionCorner().getZ() - region.getBottomRightRegionCorner().getZ());
		double worldAreaSquareMetres = sideLengthX * sideLengthZ;
		TownyProvinces.info("Region Area square metres: " + worldAreaSquareMetres);
		return worldAreaSquareMetres;
	}
	
	/**
	 * Generate a protected province.
	 * 
	 * @return the province on success, or null if you fail (usually due to the province location being too close to an existing protected province location)
	 */
	private Province generateProtectedProvince(Location location) {
		Coord coord = Coord.parseCoord(location);
		TPCoord homeBlockCoord = new TPFinalCoord(coord.getX(), coord.getZ());
		Province province = new Province(homeBlockCoord);
		if(validateBrushPosition(homeBlockCoord.getX(), homeBlockCoord.getZ(), province)) {
			ProvinceClaimBrush brush = new ProvinceClaimBrush(province);
			claimChunksCoveredByBrush(brush);
			return province;
		} else {
			return null;
		}
	}

	/**
	 * Generate a randomly placed province
	 * 
	 * @return the province on success, or null if you fail (usually due to map being full)
	 */
	private @Nullable Province generateRandomlyPlacedProvince() {
		//Establish boundaries of where the homeblock might be placed
		double xLowest = region.getRegionMinX() + region.getBrushSquareRadiusInChunks() + 3;
		double xHighest = region.getRegionMaxX() - region.getBrushSquareRadiusInChunks() - 3;
		double zLowest = region.getRegionMinZ() + region.getBrushSquareRadiusInChunks() + 3;
		double zHighest = region.getRegionMaxZ() - region.getBrushSquareRadiusInChunks() - 3;
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
			Province province = new Province(homeBlockCoord);
			//Validate province position
			if(validateBrushPosition(homeBlockCoord.getX(), homeBlockCoord.getZ(), province)) {
				ProvinceClaimBrush brush = new ProvinceClaimBrush(province);
				claimChunksCoveredByBrush(brush);
				return province;
			}
		}
		return null;
	}
	
	private boolean executeChunkClaimCompetition() {
		TownyProvinces.info("Chunk Claim Competition Started");

		//Create claim-brush objects
		List<ProvinceClaimBrush> provinceClaimBrushes = new ArrayList<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			provinceClaimBrushes.add(new ProvinceClaimBrush(province));
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
		for(int i = 0; i < region.getMaxBrushMoves(); i++) {
			TownyProvinces.info("Painting Cycle: " + i + " / " + region.getMaxBrushMoves());
			for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
				//If inactive, do nothing
				if(!provinceClaimBrush.isActive())
					continue;
				//Generate random move delta
				moveDeltaX = TownyProvincesMathUtil.generateRandomInteger(-region.getMaxBrushMoveAmountInChunks(), region.getMaxBrushMoveAmountInChunks());
				moveDeltaZ = TownyProvincesMathUtil.generateRandomInteger(-region.getMaxBrushMoveAmountInChunks(), region.getMaxBrushMoveAmountInChunks());
				//Apply min move amount
				moveDeltaX = moveDeltaX > 0 ? Math.max(moveDeltaX, region.getMinBrushMoveAmountInChunks()) : Math.min(moveDeltaX,-region.getMinBrushMoveAmountInChunks());
				moveDeltaZ = moveDeltaZ > 0 ? Math.max(moveDeltaZ, region.getMinBrushMoveAmountInChunks()) : Math.min(moveDeltaZ,-region.getMinBrushMoveAmountInChunks());
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
		int brushMinCoordX = brushPositionCoordX - region.getBrushSquareRadiusInChunks();
		int brushMaxCoordX = brushPositionCoordX + region.getBrushSquareRadiusInChunks();
		int brushMinCoordZ = brushPositionCoordZ - region.getBrushSquareRadiusInChunks();
		int brushMaxCoordZ = brushPositionCoordZ + region.getBrushSquareRadiusInChunks();
		Province province;
		for(int x = brushMinCoordX -1; x <= (brushMaxCoordX +1); x++) {
			for(int z = brushMinCoordZ -1; z <= (brushMaxCoordZ +1); z++) {
				province = TownyProvincesDataHolder.getInstance().getProvinceAtCoord(x,z);
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
			
		int startX = brush.getCurrentPosition().getX() - region.getBrushSquareRadiusInChunks();
		int endX = brush.getCurrentPosition().getX() + region.getBrushSquareRadiusInChunks();
		int startZ = brush.getCurrentPosition().getZ() - region.getBrushSquareRadiusInChunks();
		int endZ = brush.getCurrentPosition().getZ() + region.getBrushSquareRadiusInChunks();
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

	private boolean hasBrushHitClaimLimit(ProvinceClaimBrush provinceClaimBrush) {
		double currentClaimArea = provinceClaimBrush.getNumChunksClaimed() * CHUNK_AREA_IN_SQUARE_METRES;
		return currentClaimArea > region.getClaimAreaLimitInSquareMetres();
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
			province = TownyProvincesDataHolder.getInstance().getProvinceAtCoord(unclaimedCoord.getX() + x[i], unclaimedCoord.getZ() + z[i]);
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
			province = TownyProvincesDataHolder.getInstance().getProvinceAtCoord(unclaimedCoord.getX() + x[i], unclaimedCoord.getZ() + z[i]);
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
			if(province.getListOfCoordsInProvince().size() == 0) {
				provincesToDelete.add(province);
			}
		}
		for(Province province: provincesToDelete) {
			TownyProvincesDataHolder.getInstance().deleteProvince(province, unclaimedCoordsMap);
			TownyProvinces.info("Province Deleted: " + province.getId());
		}
		TownyProvinces.info("Empty Provinces Deleted.");
		return true;
	}
	
}
