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
 * Annotation to be used by canvas developers to inform soundvis about
 * meta data for annotated integer property.
 * 
 * @author Michael Heinzelmann
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface LimitedIntProperty {
	
	/**
	 * maximum value for the integer property
	 * @return the allowed maximum
	 */
	int maximum() default Integer.MAX_VALUE;
	/**
	 * minimum value for the integer property
	 * @return the allowed minimum
	 */
	int minimum() default Integer.MIN_VALUE;
	/**
	 * Step size to increment decrement the value 
	 * from the initial value (assignment in code)
	 * @return the step size to use (default 1)
	 */
	int stepSize() default 1;
	/**
	 * Describe here why we need the limitation
	 * @return the description
	 */
	String description();
}