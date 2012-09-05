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
package org.mcuosmipcuter.orcc.api.soundvis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User property annotation to be used by canvas developers to inform soundvis
 * that the annotated property is read/writable for the user, soundvis will use reflection
 * to access the property, so getter/setter are not required.<br/>
 * Supported base types: boolean, java.awt.Color, int, String<br/>
 * Consult the latest editor documentation whether other types are supported 
 * @author Michael Heinzelmann
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface UserProperty {
	
	/**
	 * Describe to the user the meaning of the property and how it is intended to be used
	 * @return the description of the property
	 */
	String description();
}