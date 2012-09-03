/**
* Copyright 2012 Michael Heinzelmann IT-Consulting
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
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