package org.mcuosmipcuter.orcc.soundvis;

public class SessionToken {

	private final String fullPath;
	private final boolean named;
	private boolean changed;
	
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
	public boolean needsSave() {
		return isDefault() || isChanged();
	}

	public boolean isChanged() {
		return changed;
	}
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
}
