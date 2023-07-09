package io.github.townyadvanced.townyprovinces.jobs.map_display;

public abstract class DisplayProvincesOnMapAction {

	/**
	 * Display all TownyProvinces items
	 */
	abstract void executeAction(boolean bordersRefreshRequested, boolean homeBlocksRefreshRequested);
	 
}
