/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.mcuosmipcuter.orcc.soundvis.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

/**
 * Bean conforming class to delegate to/from others that are not
 * @author user
 *
 */
public class PersistentObject {
	
	private Class<?> delegate;
	private Map<String, Object> persistentProperties;

	/**
	 * 
	 */
	public PersistentObject() {
		// bean
	}

	public Class<?> getDelegate() {
		return delegate;
	}

	public void setDelegate(Class<?> delegate) {
		this.delegate = delegate;
	}

	public Map<String, Object> getPersistentProperties() {
		return persistentProperties;
	}

	public void setPersistentProperties(Map<String, Object> persistentProperties) {
		this.persistentProperties = persistentProperties;
	}
	
	public static PersistentObject createTo(Object object) throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> persistentProperties = new HashMap<String, Object>();
		PersistentObject po = new PersistentObject();
		po.setDelegate(object.getClass());
		for(Field field : object.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			Object value = field.get(object);
			if(field.isAnnotationPresent(UserProperty.class)) {
				persistentProperties.put(field.getName(), value);
			}
			if(field.isAnnotationPresent(NestedProperty.class)) {
				PersistentObject proxy = createTo(value);
				persistentProperties.put(field.getName(), proxy);
			}
		}
		po.setPersistentProperties(persistentProperties);
		return po;
	}

	public void mergeInto(Object object) throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		for(Map.Entry<String, Object> entry : persistentProperties.entrySet()) {
			Field field = object.getClass().getDeclaredField(entry.getKey());
			field.setAccessible(true);
			Object value;
			if(entry.getValue() instanceof PersistentObject) {
				PersistentObject persistentObject = (PersistentObject)entry.getValue();
				if(field.get(object) == null ) {
					value = delegate.getDeclaredConstructor((Class<?>[])null).newInstance((Object[])null);
					field.set(object, value);
				}

				persistentObject.mergeInto(field.get(object));
				
			}
			else {
				value = entry.getValue();
				field.set(object, value);
			}
		}
	}


}
