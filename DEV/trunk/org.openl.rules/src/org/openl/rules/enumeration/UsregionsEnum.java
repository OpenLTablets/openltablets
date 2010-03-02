package org.openl.rules.enumeration;

public enum UsregionsEnum {

	MW("Midwest"),
	NE("Northeast"),
	SE("Southeast"),
	SW("Southwest"),
	W("West");

	private final String displayName;

	private UsregionsEnum (String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}