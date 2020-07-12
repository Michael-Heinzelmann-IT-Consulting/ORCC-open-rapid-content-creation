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

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.util.IOUtil;

public class Session {
	public static void saveSession() throws IllegalArgumentException, IllegalAccessException {
		List<SoundCanvasWrapper> appObjects = Context.getSoundCanvasList();
		
		List<PersistentObject> persistentObjects = new ArrayList<PersistentObject>();
		
		for(SoundCanvasWrapper sw : appObjects) {
			SoundCanvas sc = sw.getSoundCanvas();
			PersistentObject po = new PersistentObject();
			Map<String, Object> persistentProperties = new HashMap<String, Object>();
			po.setDelegate(sc.getClass());
			for(Field field : sc.getClass().getDeclaredFields()) {
				if(field.isAnnotationPresent(UserProperty.class)) {
					field.setAccessible(true);
					Object value = field.get(sc);
					persistentProperties.put(field.getName(), value);
				}
			}
			po.setPersistentProperties(persistentProperties);
			persistentObjects.add(po);
		}

		
		File file = new File("latest_session.xml");

		try (FileOutputStream out = new FileOutputStream(file); 
				XMLEncoder encoder = new XMLEncoder(out)) {
			encoder.writeObject(persistentObjects);
			encoder.flush();
			IOUtil.log("saved: " + file.getAbsolutePath());
		} catch (IOException e) {
			IOUtil.log(e.getMessage());
		}
	}

}
