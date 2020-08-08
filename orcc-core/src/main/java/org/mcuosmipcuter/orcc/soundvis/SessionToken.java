package org.mcuosmipcuter.orcc.soundvis;

public class SessionToken {

	private final String fullPath;
	private final boolean named;
	
	public boolean isDefault() {
		return !named;
	}
	public boolean isNamed() {
		return named;
	}
	
	public SessionToken(String fullPath) {
		this.fullPath = fullPath;
		named = fullPath != null;
	}

	public String getFullPath() {
		return fullPath;
	}
}
