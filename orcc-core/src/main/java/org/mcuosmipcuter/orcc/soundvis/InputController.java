/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2021 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.mcuosmipcuter.orcc.api.soundvis.InputEnabling;
import org.mcuosmipcuter.orcc.api.soundvis.InputEnabling.Controller;

/**
 * @author Michael Heinzelmann
 *
 */
public abstract class InputController implements Controller {

	private Map<String, InputEnabling> fieldEnablings = new HashMap<>();
	private boolean ready;

	@Override
	public void propertyWritten(Field field) {
		if(ready) {
			doFieldEnablings(fieldEnablings);
		}
	}

	@Override
	public void addEnablingReference(String propertyName, InputEnabling inputEnabling) {
		fieldEnablings.put(propertyName, inputEnabling);
	}

	@Override
	public void enablingsReady() {
		ready = true;
		doFieldEnablings(fieldEnablings);
	}
	protected abstract void doFieldEnablings(Map<String, InputEnabling> fieldEnablings);

}
