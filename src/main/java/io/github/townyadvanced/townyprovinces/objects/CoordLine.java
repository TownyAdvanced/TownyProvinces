package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;

import java.util.ArrayList;
import java.util.List;

/**
 * A line of coords
 */
public class CoordLine {
	List<Coord> points;
	Coord lineHead;
	boolean active;

	/**
	 * Contructor used when we know only one point
	 * This could probably be always used in place of the one below
	 * but the one below makes for faster processing
	 */
	public CoordLine(Coord a) {
		points = new ArrayList<>();
		points.add(a);
		lineHead = a;
		active = true;
	}
	
	public CoordLine(Coord a, Coord b) {
		points = new ArrayList<>();
		points.add(a);
		points.add(b);
		lineHead = b;
		active = true;
	}

	public List<Coord> getPoints() {
		return points;
	}

	public Coord getLineHead() {
		return lineHead;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean b) {
		active = b;
	}

	public void setLineHead(Coord coord) {
		lineHead = coord;
	}

	public void addPoint(Coord linePoint) {
		points.add(linePoint);
	}
}
