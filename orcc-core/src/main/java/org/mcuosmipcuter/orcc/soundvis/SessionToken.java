package org.mcuosmipcuter.orcc.soundvis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.util.IOUtil;

public class SessionToken {

	private final String fullPath;
	private final boolean named;
	Map<String, ValueChanges> changes = new LinkedHashMap<>();
	
	public boolean isDefault() {
		return !named;
	}
	public boolean isNamed() {
		return named;
	}
	
	public SessionToken(String fullPath) {
		this.fullPath = fullPath;
		named = fullPath != null;
		IOUtil.log("##################### new session token ###################");
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
	public void changeOccurred(String propertyKey, Object oldValue, Object newValue) {
		if(oldValue instanceof SoundCanvas && newValue == null) {
			// remove all property changes for canvas
			String canvaskey = getSoundCanvasKey((SoundCanvas) oldValue);
			Iterator<String> iter = changes.keySet().iterator();
			while(iter.hasNext()) {
				String key = iter.next();
				if(key.startsWith(canvaskey)) {
					iter.remove();
				}
			}
		}
		ValueChanges vc = changes.get(propertyKey);
		if(vc == null) {
			vc = new ValueChanges(oldValue, newValue);
			changes.put(propertyKey, vc);
		}
		else {
			vc.addChangeValue(newValue);
		}
		for(Entry<String, ValueChanges> e : changes.entrySet()) {
			//System.err.println(e.getKey() + "=" + e.getValue());
		}
		
	}
	
	public static String getSoundCanvasKey(SoundCanvas soundCanvas) {
		return soundCanvas.getClass().getSimpleName() + "#" + System.identityHashCode(soundCanvas);
	}
	
	public static String getSoundCanvasWrapperKey(SoundCanvasWrapper soundCanvasWrapper) {
		return soundCanvasWrapper.getDisplayName() + "#" + System.identityHashCode(soundCanvasWrapper);
	}
	
	public List<String> getChangeLog(boolean includeReverted){
		List<String> result = new ArrayList<>();
		for(Entry<String, ValueChanges> e : changes.entrySet()) {
			ValueChanges vc = e.getValue();
			if(includeReverted || vc.isLogicallyChanged()) {
				result.add(e.getKey() + "=" + e.getValue());
			}
		}
		return result;
	}
}
