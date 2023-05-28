package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProvinceCreatorUtil {

	private static int estimatedNumLinesRequired = 0;
	/**
	 * Create all provinces in the world
	 */
	public static boolean createProvinces() {
	
		//Create province objects - empty except for the homeblocks
		if(!createProvinceObjects()) {
			return false;
		}
		
		//Execute chunk claim competition
		if(!executeChunkClaimCompetition()) {
			return false;
		}
		
		//Allocate unclaimed chunks to provinces. Do not create borders yet
		if(!assignUnclaimedChunksToProvinces()) {
			return false;
		}

		if(!createProvinceBorderBlocks()) {
			return false;
		}
		
		TownyProvinces.info("Provinces Created: " + TownyProvincesDataHolder.getInstance().getNumProvinces());
		TownyProvinces.info("Province Blocks Created: " + TownyProvincesDataHolder.getInstance().getProvinceBlocks().size());
		return true;
	}

	
	private static Set<Coord> findAllUnclaimedCoords(){
		Set<Coord> result = new HashSet<>();
		int minX = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		Coord coord;
		ProvinceBlock provinceBlock;
		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				coord = new Coord(x,z);
				provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
				if(provinceBlock == null) {
					//This means the chunk is unclaimed
					result.add(coord);
				}
			}
		}
		return result;
	}
	
	private static boolean createProvinceBorderBlocks() {
		//This is fairly simple. Just turn everything remaining in the target area, into a border bloc
		ProvinceBlock provinceBlock;
		for(Coord unclaimedCoord: findAllUnclaimedCoords()) {
			provinceBlock = new ProvinceBlock(unclaimedCoord, null, true);
			TownyProvincesDataHolder.getInstance().addProvinceBlock(unclaimedCoord, provinceBlock);
		}
		return true;
	}

	/**
	 * If an unclaimed coord is NOT on a border, allocate it to a province
	 * @return
	 */
	private static boolean assignUnclaimedChunksToProvinces() {
		List<Coord> coordsEligibleForProvinceAssignment;
		int indexOfCoordToAssign;
		Coord coordToAssign;
		Province province;
		ProvinceBlock newProvinceBlock;
		while((coordsEligibleForProvinceAssignment = findCoordsEligibleForProvinceAssignment()).size() > 0) {
			TownyProvinces.info("Num Coords Eligible For Province Assignment: " + coordsEligibleForProvinceAssignment.size());
			//Pick a random coord to assign
			indexOfCoordToAssign = (int)(Math.random() * coordsEligibleForProvinceAssignment.size());
			coordToAssign = coordsEligibleForProvinceAssignment.get(indexOfCoordToAssign);
			//Assign the cord to the (assumed) single adjacent province
			province = findAdjacentProvinces(coordToAssign).get(0);
			newProvinceBlock = new ProvinceBlock(coordToAssign, province, false);
			TownyProvincesDataHolder.getInstance().addProvinceBlock(coordToAssign, newProvinceBlock);
		}
		return true;
	}
	
	private static List<Coord> findCoordsEligibleForProvinceAssignment() {
		int minX = (TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxX  = (TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		int minZ = (TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxZ  = (TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		Set<Coord> resultSet = new HashSet<>();
		Set<Coord> allUnclaimedCoords = findAllUnclaimedCoords();
		
		for(Coord candidateCoord: allUnclaimedCoords) {
			
			//Filter out chunk if it is at edge of map
			if(candidateCoord.getX() <= minX)
				continue;
			else if (candidateCoord.getX() >= maxX)
				continue;
			else if (candidateCoord.getZ() <= minZ)
				continue;
			else if (candidateCoord.getZ() >= maxZ)
				continue;
			
			//Filter out chunk if it does not have exactly 1 adjacent province
			if(findAdjacentProvinces(candidateCoord).size() != 1)
				continue;
			
			//Filter out chunk if the adjacent province is NOT on a cardinal direction
			if(findCardinallyAdjacentProvinces(candidateCoord).size() != 1)
				continue;
			
			//Add candidate coord to result set
			resultSet.add(candidateCoord);
		}
		return new ArrayList<>(resultSet);
	}

	private static List<Province> findAdjacentProvinces(Coord givenCoord) {
		Set<Province> result = new HashSet<>();
		Province adjacentProvince;
		for (int x = -1; x <= 1; x++) {
			for(int z = -1; z <=1; z++) {
				if (x != 0 && z != 0) {
					adjacentProvince = findAdjacentProvince(givenCoord, x, z);
					if(adjacentProvince != null) {
						result.add(adjacentProvince);
					}
				}
			}
		}
		return new ArrayList<>(result);
	}
	
	private static List<Province> findCardinallyAdjacentProvinces(Coord givenCoord) {
		Set<Province> resultSet = new HashSet<>();
		Province[] cardinalResultArray = new Province[4];

		cardinalResultArray[0] = findAdjacentProvince(givenCoord, 0 , -1);
		cardinalResultArray[1] = findAdjacentProvince(givenCoord, 0 , 1);
		cardinalResultArray[2] = findAdjacentProvince(givenCoord, 1 , 0);
		cardinalResultArray[3] = findAdjacentProvince(givenCoord, -1 , 0);

		for(Province cardinalResult: cardinalResultArray) {
			if(cardinalResult != null) {
				resultSet.add(cardinalResult);
			}
		}
		return new ArrayList<>(resultSet);
	}

	private static Province findAdjacentProvince(Coord givenCoord, int xDelta, int zDelta) {
		Coord adjacentCoord = new Coord(givenCoord.getX() + xDelta, givenCoord.getZ() + zDelta);
		ProvinceBlock adjacentProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
		if(adjacentProvinceBlock == null || adjacentProvinceBlock.isProvinceBorder()) {
			return null;
		} else {
			return adjacentProvinceBlock.getProvince();
		}
	}


	/**
	 * 
	 
	 * 
	 * @param unclaimedCoords set of all unclaimed coords
	 * @return true if success
	 */
	private static boolean assignUnclaimedChunksToProvinces(Set<Coord> unclaimedCoords) {
		while (unclaimedCoords.size() > 0) {
			Coord coordToAssign = assignOneUnclaimedChunkToAProvince(unclaimedCoords);
			if(coordToAssign != null) {
				unclaimedCoords.remove(coordToAssign);
			}
		}
		return true;
	}

	/**
	 * 
	 * @param unclaimedCoords list of all unclaimed coords
	 */
	private static Coord assignOneUnclaimedChunkToAProvince(Set<Coord> unclaimedCoords) {
		//Select one unclaimed coord
		int indexOfCoordToAllocate = (int)(Math.random() * unclaimedCoords.size());
		//TownyProvinces.info("Index: "+ indexOfCoordToAllocate);
		Coord coordToAllocate = (new ArrayList<>(unclaimedCoords)).get(indexOfCoordToAllocate);
		
		//if(true) {
		//	ProvinceBlock newProvinceBlock = new ProvinceBlock(coordToAllocate, null, true);
	//		TownyProvincesDataHolder.getInstance().addProvinceBlock(coordToAllocate, newProvinceBlock);
	//		return coordToAllocate;
	//	}
		
		//Find all adjacent provinces
		Set<Coord> adjacentCoords = findAllAdjacentCoords(coordToAllocate);
		Set<Province> adjacentProvinces = new HashSet<>();
		ProvinceBlock provinceBlock;
		for(Coord adjacentCoord: adjacentCoords) {
			provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
			if(provinceBlock != null && !provinceBlock.isProvinceBorder()) {
				TownyProvinces.info("Adjacent Province found");
				adjacentProvinces.add(provinceBlock.getProvince());
			}
		}
		//Process the coord
		if(adjacentProvinces.size() == 1) {
			//Create block and assign to the adjacent province
			//ProvinceBlock newProvinceBlock = new ProvinceBlock(coordToAllocate, null, true);
			ProvinceBlock newProvinceBlock = new ProvinceBlock(coordToAllocate, new ArrayList<>(adjacentProvinces).get(0), false);
			TownyProvincesDataHolder.getInstance().addProvinceBlock(coordToAllocate, newProvinceBlock);
			return coordToAllocate;
		} else if (adjacentProvinces.size() > 1) {
			//Create  block and make it a border
			ProvinceBlock newProvinceBlock = new ProvinceBlock(coordToAllocate, null, true);
			TownyProvincesDataHolder.getInstance().addProvinceBlock(coordToAllocate, newProvinceBlock);
			return coordToAllocate;
		} else {
			//ProvinceBlock newProvinceBlock = new ProvinceBlock(coordToAllocate, null, true);
			//return coordToAllocate;
			return null;
		}
	}
	
	
	private static boolean setupProvinceBorders() {
		//Process all provinces, starting with the ones which have the least amount of processed neighbors
		Set<Province> allProvinces = new HashSet<>(TownyProvincesDataHolder.getInstance().getProvinces());
		Set<Province> unProcessedProvinces = new HashSet<>(allProvinces);
		Set<Province> processedProvinces = new HashSet<>();
		while(unProcessedProvinces.size() > 0) {
			//TownyProvinces.info("PROC_PROVINCES: " + processedProvinces.size());
			//TownyProvinces.info("UNPROC_PROVINCES: " + unProcessedProvinces.size());
			Province province = findUnProcessedProvinceWithLeastAmountOfProcessedNeighbors(unProcessedProvinces, processedProvinces);
			setupProvinceBorders(province);
			unProcessedProvinces.remove(province);
			processedProvinces.add(province);
		}
		
		TownyProvinces.info("PROVINCES PROCESSED: " + processedProvinces.size());

		//Fix invalid border blocks
		for(ProvinceBlock provinceBlock: TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
			//If a block is not a border between two regions, or a region and sea, cull it
			Set<Coord> adjacentCoords = findAllAdjacentCoords(provinceBlock.getCoord());
			Set<Province> borderingLandProvinces = new HashSet<>();
			int borderingSea = 0;
			for(Coord adjacentCoord: adjacentCoords) {
				ProvinceBlock borderBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
				if(borderBlock != null && !borderBlock.isProvinceBorder()) {
					borderingLandProvinces.add(borderBlock.getProvince());
				} else {
					borderingSea = 1;
				}
			}
			int numBorderingProvinces = borderingLandProvinces.size() + borderingSea;
			TownyProvinces.info("ProvinceBlock: x" + provinceBlock.getCoord().getX() + "_z" + provinceBlock.getCoord().getZ() + ". Bordering Provinces: " + numBorderingProvinces);

			if (numBorderingProvinces == 1) {
				TownyProvinces.info("Found block with only 1 bordering province");
				if(borderingSea == 1) {
					//assign it to the sea
					TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks().remove(provinceBlock);
				} else {
					//Assign the block to a nearby province
					provinceBlock.setProvince((new ArrayList<>(borderingLandProvinces)).get(0));
					provinceBlock.setProvinceBorder(false);
				}
				

			}
		}
		return true;
	}

	private static Province findUnProcessedProvinceWithLeastAmountOfProcessedNeighbors(Set<Province> unProcessedProvinces, Set<Province> processedProvinces) {
		Province winner = null;
		int winnerNumberOfProcessedNeighbors =  999999999;
		for(Province candidate: unProcessedProvinces) {
			int candidateNumberOfProcessedNeighbors = calculateNumberOfProcessedNeighbors(candidate, processedProvinces);
			if(winner == null || candidateNumberOfProcessedNeighbors < winnerNumberOfProcessedNeighbors) {
				winner = candidate;
				winnerNumberOfProcessedNeighbors = candidateNumberOfProcessedNeighbors;
			}
		}
		if(winnerNumberOfProcessedNeighbors == 0) {
			estimatedNumLinesRequired += 1;
		} else {
			estimatedNumLinesRequired += winnerNumberOfProcessedNeighbors;
			
		}
		return winner;
	}

	private static int calculateNumberOfProcessedNeighbors(Province province, Set<Province> processedProvinces) {
		int result = 0;
		Set<Province> neighboringProvinces = calculateNeigboringProvinces(province);
		for(Province neighborProvince: neighboringProvinces) {
			if(processedProvinces.contains(neighborProvince)) {
				result++;
			}
		}
		return result;
	}
	
	public static Set<Province> calculateNeigboringProvinces(Province province) {
		Set<Province> result = new HashSet<>();
		for(ProvinceBlock provinceBlock: province.getProvinceBlocks()) {
			result.addAll(calculateNeigboringProvinces(provinceBlock));
		}
		return result;
	}

	public static Set<Province> calculateNeigboringProvinces(ProvinceBlock givenProvinceBlock) {
		Coord givenProvinceBlockCoord = givenProvinceBlock.getCoord();
		Province givenProvince = givenProvinceBlock.getProvince();
		Coord adjacentCoord;
		ProvinceBlock adjacentProvinceBlock;
		Set<Province> result = new HashSet<>();
		for(int z = -1; z <=1; z++) {
			for(int x = -1; x <=1; x++) {
				if(x == 0 && z == 0) {
					continue;
				}
				adjacentCoord = new Coord(givenProvinceBlockCoord.getX() + x, givenProvinceBlockCoord.getZ() + z);
				adjacentProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
				if (adjacentProvinceBlock != null && adjacentProvinceBlock.getProvince() != givenProvince) {
					result.add(adjacentProvinceBlock.getProvince());
				}
			}
		}
		return result;
	}



	private static boolean setupProvinceBorders(Province province) {
		for (ProvinceBlock provinceBlock : province.getProvinceBlocks()) {
			if (shouldThisProvinceBlockBeAProvinceBorder(provinceBlock)) {
				provinceBlock.setProvinceBorder(true);
				provinceBlock.setProvince(null);
			}
		}
		return true;
	}

	private static Set<Coord> findAllAdjacentCoords(Coord targetCoord) {
		Set<Coord> result = new HashSet<>();
		for(int x = -1; x <= 1; x++) {
			for(int z = -1; z <= 1; z++) {
				if(x != 0 && z != 0) {
					result.add(new Coord(targetCoord.getX() + x, targetCoord.getZ() + z));
				}
			}
		}
		return result;
	}


	private static boolean shouldThisProvinceBlockBeAProvinceBorder(ProvinceBlock provinceBlock) {
		Coord provinceBlockCoord = provinceBlock.getCoord();
		Province province = provinceBlock.getProvince();
		Coord adjacentCoord;
		ProvinceBlock adjacentProvinceBlock;
		for(int z = -1; z <=1; z++) {
			for(int x = -1; x <=1; x++) {
				if(x == 0 && z == 0) {
					continue;
				}
				adjacentCoord = new Coord(provinceBlockCoord.getX() + x, provinceBlockCoord.getZ() + z);
				adjacentProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
				if(adjacentProvinceBlock == null) {
					return true;
				} else if (adjacentProvinceBlock.getProvince() != province && !adjacentProvinceBlock.isProvinceBorder()) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean executeChunkClaimCompetition() {
		TownyProvinces.info("Chunk Claim Competition Started");
		
		//Create claim-brush objects
		List<ProvinceClaimBrush> provinceClaimBrushes = new ArrayList<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getProvinces()) {
			provinceClaimBrushes.add(new ProvinceClaimBrush(province, 8));
		}
		
		//First claim once around the homeblocks
		for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
			claimChunksCoveredByBrush(provinceClaimBrush);
		}
		
		//Now Do claim competition
		//Loop each brush x times //TODO parameterize num loops
		for(int i = 0; i < 200; i++) {
			for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
				//Move brush
				//Todo - parameterize the move amount
				int moveAmountX = TownyProvincesMathUtil.generateRandomInteger(-4,4);
				int moveAmountZ = TownyProvincesMathUtil.generateRandomInteger(-4,4);
				Coord destination = new Coord(provinceClaimBrush.getCurrentPosition().getX() + moveAmountX, provinceClaimBrush.getCurrentPosition().getZ() + moveAmountZ);
				moveBrushIfPossible(provinceClaimBrush, destination);

				//Claim chunks
				claimChunksCoveredByBrush(provinceClaimBrush);
			}
		}
		
		TownyProvinces.info("Claim Competition Complete. Total Chunks Claimed: " + TownyProvincesDataHolder.getInstance().getProvinceBlocks().values().size());
		return true;
	}
	

	/**
	 * 
	 * @param claimQueue
	 * @return true if the queue was cleared
	 */
	private static boolean processClaimQueue(List<Coord> claimQueue) {
		TownyProvinces.info("Claim Queue Size: " + claimQueue.size());
		
		List<Coord> copyOfClaimQueue = new ArrayList<>(claimQueue);
		
		Province adjacentProvince;
		for(Coord coord: copyOfClaimQueue) {
			adjacentProvince = getAdjacentProvince(coord);
			if(adjacentProvince != null) {
				//Claim chunk and remove coord from queue
				ProvinceBlock provinceBlock = new ProvinceBlock();
				provinceBlock.setProvince(adjacentProvince);
				provinceBlock.setCoord(coord);
				TownyProvincesDataHolder.getInstance().addProvinceBlock(coord, provinceBlock);
				claimQueue.remove(coord);
				//TownyProvinces.info("Chunk Claimed");
			} else {
				//Send coord to back of queue
				claimQueue.remove(coord);
				claimQueue.add(coord);
			}
		}
		return claimQueue.size() == 0;
	}

	private static @Nullable Province getAdjacentProvince(Coord unclaimedChunkCoord) {
		Coord adjacentCoord;
		ProvinceBlock provinceBlock;
		for(int x = unclaimedChunkCoord.getX() -1; x <= unclaimedChunkCoord.getX() + 1; x++) {
			for (int z = unclaimedChunkCoord.getZ() - 1; z <= unclaimedChunkCoord.getZ() + 1; z++) {
				adjacentCoord = new Coord(x, z);
				if (adjacentCoord.equals(unclaimedChunkCoord)) {
					continue;
				}
				provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
				if (provinceBlock != null) {
					return provinceBlock.getProvince();	
				}
			}
		}
		return null;
	}


	//Don't move if any of the brush would overlap another province, or adjacent to it
	private static void moveBrushIfPossible(ProvinceClaimBrush brush, Coord destination) {
		
		int brushMinX = destination.getX() - brush.getSquareRadius() ;
		int brushMaxX = destination.getX() + brush.getSquareRadius() ;
		int brushMinZ = destination.getZ() - brush.getSquareRadius();
		int brushMaxZ = destination.getZ() + brush.getSquareRadius();

		int provinceWorldMinX = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMaxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMinZ = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMaxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		
		Coord coord;
		ProvinceBlock provinceBlock;
		for(int x = brushMinX -1; x <= brushMaxX +1; x++) {
			for(int z = brushMinZ -1; z <= brushMaxZ +1; z++) {
				
				//Don't move near edge of map
				if(x < provinceWorldMinX)
					return;
				else if (x > provinceWorldMaxX)
					return;
				else if (z < provinceWorldMinZ)
					return;
				else if (z > provinceWorldMaxZ)
					return;
				
				//Don't move near different province
				coord = new Coord(x,z);
				provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
				if(provinceBlock != null && provinceBlock.getProvince() != brush.getProvince()) {
					return;
				}
			}
		}
		
		brush.moveBrush(destination);
	}

	private static void claimChunkForNearestProvince(Coord chunkCoord) {
		Province nearestProvince = null;
		double nearestProvinceDistance = 9999999999999999d;
		double candidateProvinceDistance;
		for(Province province: TownyProvincesDataHolder.getInstance().getProvinces()) {
			candidateProvinceDistance = MathUtil.distance(chunkCoord, province.getHomeBlock());
			if(candidateProvinceDistance < nearestProvinceDistance) {
				nearestProvince = province;
				nearestProvinceDistance = candidateProvinceDistance;
			}
		}
		ProvinceBlock provinceBlock = new ProvinceBlock();
		provinceBlock.setProvince(nearestProvince);
		provinceBlock.setCoord(chunkCoord);
		TownyProvincesDataHolder.getInstance().addProvinceBlock(chunkCoord, provinceBlock);
	}

	/**
	 * Claim chunks covered by brush EXCEPT those near adjacent provinces or the map edge
	 * @param brush
	 */
	private static void claimChunksCoveredByBrush(ProvinceClaimBrush brush) {
		int startX = brush.getCurrentPosition().getX() - brush.getSquareRadius();
		int endX = brush.getCurrentPosition().getX() + brush.getSquareRadius();
		int startZ = brush.getCurrentPosition().getZ() - brush.getSquareRadius();
		int endZ = brush.getCurrentPosition().getZ() + brush.getSquareRadius();
		for(int x = startX; x <= endX; x++) {
			for(int z = startZ; z <= endZ; z++) {
				//Claim chunk
				claimChunk(new Coord(x,z), brush.getProvince());
			}
		}
	}

	/**
	 * Claim the given chunk, unless another province has already claimed it
	 * Or near edge of map, or near other province, 
	 * @param coord co-ord of chunk
	 * @param province the province doing the claiming
	 */
	private static void claimChunk(Coord coord, Province province) {
		//Don't claim if already claimed
		if (TownyProvincesDataHolder.getInstance().getProvinceBlock(coord) != null)
			return;

		//Don't claim near other province
		Set<Coord> adjacentCoords = findAllAdjacentCoords(coord);
		ProvinceBlock provinceBlock;
		for(Coord adjacentCoord: adjacentCoords) {
			provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
			if(provinceBlock != null && provinceBlock.getProvince() != province) {
				return;
			}
		}
		
		//Don't claim at edge of map
		int minX = (TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxX  = (TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		int minZ = (TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxZ  = (TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		if(coord.getX() < minX)
			return;
		else if (coord.getX() > maxX)
			return;
		else if (coord.getZ() < minZ)
			return;
		else if (coord.getZ() > maxZ)
			return;
		
		//Claim chunk
		ProvinceBlock newProvinceBlock = new ProvinceBlock();
		newProvinceBlock.setProvince(province);
		newProvinceBlock.setCoord(coord);
		TownyProvincesDataHolder.getInstance().addProvinceBlock(coord, newProvinceBlock);
	}

	/**
	 * Create province object - empty except for the homeblocks
	 * 
	 * @return false if we failed to create sufficient provinces
	 */
	private static boolean createProvinceObjects() {
		Coord provinceHomeBlock;
		int idealNumberOfProvinces = calculateIdealNumberOfProvinces();
		for (int provinceIndex = 0; provinceIndex < idealNumberOfProvinces; provinceIndex++) {
			provinceHomeBlock = generateProvinceHomeBlock();
			if(provinceHomeBlock != null) { 
				//Province homeblock generated. Now create province
				Province province = new Province();
				province.setHomeBlock(provinceHomeBlock);
				TownyProvincesDataHolder.getInstance().addProvince(province);
			} else {
				//Could not generate a province homeblock
				double allowedVariance = TownyProvincesSettings.getMaxAllowedVarianceBetweenIdealAndActualNumProvinces();
				double minimumAllowedNumProvinces = ((double) idealNumberOfProvinces) * (1 - allowedVariance);
				int actualNumProvinces = TownyProvincesDataHolder.getInstance().getNumProvinces();
				if (actualNumProvinces < minimumAllowedNumProvinces) {
					TownyProvinces.severe("ERROR: Could not create the minimum number of provinces. Required: " + minimumAllowedNumProvinces + ". Actual: " + actualNumProvinces);
					return false;
				} else {
					TownyProvinces.info("" + actualNumProvinces + " province objects created, each one containing just homeblock info.");
					return true;
				}
			}
		}
		return true;
	}

	/**
	 * Generate a new province homeBlock
	 * Return null if you fail - usually due to map being full up with provinces
	 */
	private static Coord generateProvinceHomeBlock() {
		double tpChunkSideLength = TownyProvincesSettings.getProvinceBlockSideLength();
		for(int i = 0; i < 100; i++) {
			double xLowest = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX();
			double xHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX();
			double zLowest = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ();
			double zHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ();
			double x = xLowest + (Math.random() * (xHighest - xLowest));
			double z = zLowest + (Math.random() * (zHighest - zLowest));
			int xCoord = (int)(x / tpChunkSideLength);
			int zCoord = (int)(z / tpChunkSideLength);
			Coord generatedHomeBlockCoord = new Coord(xCoord, zCoord);
			if(validateProvinceHomeBlock(generatedHomeBlockCoord)) {
				TownyProvinces.info("Province homeblock generated");
				return generatedHomeBlockCoord;
			}
		}
		TownyProvinces.info("Could not generate province homeblock");
		return null;
	}
	
	private static boolean validateProvinceHomeBlock(Coord coord) {
		int minAllowedDistanceInMetres = TownyProvincesSettings.getMinAllowedDistanceBetweenProvinceHomeBlocks();
		int minAllowedDistanceInChunks = minAllowedDistanceInMetres / TownyProvincesSettings.getProvinceBlockSideLength();
		List<Province> provinceList = TownyProvincesDataHolder.getInstance().getProvinces();
		for(Province province: provinceList) {
			if(MathUtil.distance(coord, province.getHomeBlock()) < minAllowedDistanceInChunks) {
				return false;
			}
		}
		return true;
	}
	
	private static int calculateIdealNumberOfProvinces() {
		double worldAreaSquareMetres = calculateWorldAreaSquareMetres();
		double averageProvinceAreaSquareMetres = TownyProvincesSettings.getAverageProvinceSizeInSquareMetres();
		int idealNumberOfProvinces = (int)(worldAreaSquareMetres / averageProvinceAreaSquareMetres);
		TownyProvinces.info("Ideal num provinces: " + idealNumberOfProvinces);
		return idealNumberOfProvinces;
	}
	
	private static double calculateWorldAreaSquareMetres() {
		Location topLeftCorner = TownyProvincesSettings.getTopLeftWorldCornerLocation();
		Location bottomRightCorner = TownyProvincesSettings.getBottomRightWorldCornerLocation();
		double sideLengthX = Math.abs(topLeftCorner.getX() - bottomRightCorner.getX());
		double sideLengthZ = Math.abs(topLeftCorner.getZ() - bottomRightCorner.getZ());
		double worldAreaSquareMetres = sideLengthX * sideLengthZ;
		TownyProvinces.info("World Area square metres: " + worldAreaSquareMetres);
		return worldAreaSquareMetres;

	}
}

 