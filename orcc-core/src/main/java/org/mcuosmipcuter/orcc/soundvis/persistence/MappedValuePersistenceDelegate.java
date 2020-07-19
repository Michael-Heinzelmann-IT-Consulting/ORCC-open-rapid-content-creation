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

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;

import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;

/**
 * @author user
 *
 */
public class MappedValuePersistenceDelegate extends PersistenceDelegate {
	
	

	@Override
	protected boolean mutatesTo(Object oldInstance, Object newInstance) {
		// TODO Auto-generated method stub
		return oldInstance.equals(newInstance);
	}

	/**
	 * 
	 */
	public MappedValuePersistenceDelegate() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Expression instantiate(Object oldInstance, Encoder out) {
		MappedValue<?> mv = (MappedValue<?>)oldInstance;
		return new Expression(mv, mv.getFactoryClass(), mv.getFactoryMethod(), new Object[] {mv.getValue(), mv.getDisplayString()});
	}

}
