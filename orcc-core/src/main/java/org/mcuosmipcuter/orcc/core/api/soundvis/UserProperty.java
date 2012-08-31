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
package org.mcuosmipcuter.orcc.core.api.soundvis;

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
	 * @return
	 */
	String description();
}