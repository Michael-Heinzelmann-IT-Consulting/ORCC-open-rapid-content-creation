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

/**
 * Thrown by a sound canvas if it is not able to work with the parameters given in prepare.
 * @author Michael Heinzelmann
 */
public class PrepareException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param msg
	 */
	public PrepareException(String msg) {
		super(msg);
	}

}