package io.github.townyadvanced.townyprovinces.settings;

/**
 * 
 * @author LlmDl
 *
 */
public enum TownyProvincesPermissionNodes {
	
	TOWNYPROVINCES_ADMIN("townyprovinces.admin");

	private String value;

	/**
	 * Constructor
	 * 
	 * @param permission - Permission.
	 */
	TownyProvincesPermissionNodes(String permission) {

		this.value = permission;
	}

	/**
	 * Retrieves the permission node
	 * 
	 * @return The permission node
	 */
	public String getNode() {

		return value;
	}

	/**
	 * Retrieves the permission node
	 * replacing the character *
	 * 
	 * @param replace - String
	 * @return The permission node
	 */
	public String getNode(String replace) {

		return value.replace("*", replace);
	}

	public String getNode(int replace) {

		return value.replace("*", replace + "");
	}

}
