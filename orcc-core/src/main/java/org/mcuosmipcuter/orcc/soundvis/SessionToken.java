package org.mcuosmipcuter.orcc.soundvis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SessionToken {

	private final String fullPath;
	private final boolean named;
	private boolean changed;
	Map<String, ValueChanges> changes = new HashMap<>();
	
	public boolean isDefault() {
		return !named;
	}
	public boolean isNamed() {
		return named;
	}
	
	public SessionToken(String fullPath) {
		this.fullPath = fullPath;
		named = fullPath != null;
		System.err.println("##################### new session token ###################");
	}

	public String getFullPath() {
		return fullPath;
	}
	public boolean needsSave() {
		return isDefault() || isChanged();
	}

	public boolean isChanged() {
		for(ValueChanges vc : changes.values()) {
			if(vc.isLogicallyChanged()) {
				return true;
			}
		}
		return false;
	}
//	public void setChanged(boolean changed) {
//		this.changed = changed;
//	}
	public void changeOccurred(String propertyKey, Object oldValue, Object newValue) {
		ValueChanges vc = changes.get(propertyKey);
		if(vc == null) {
			vc = new ValueChanges(oldValue, newValue);
			changes.put(propertyKey, vc);
		}
		else {
			vc.addChangeValue(newValue);
		}
		for(Entry<String, ValueChanges> e : changes.entrySet()) {
			System.err.println(e.getKey() + "=" + e.getValue());
		}
		
	}
}
