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
module orcc.core {
	exports org.mcuosmipcuter.orcc.soundvis.effects;
	exports org.mcuosmipcuter.orcc.util;
	exports org.mcuosmipcuter.orcc.soundvis.defaultcanvas;
	exports org.mcuosmipcuter.orcc.soundvis.util;
	exports org.mcuosmipcuter.orcc.soundvis.threads;
	exports org.mcuosmipcuter.orcc.soundvis.persistence;
	exports org.mcuosmipcuter.orcc.soundvis.defaultcanvas.model;
	exports org.mcuosmipcuter.orcc.soundvis.model;
	exports org.mcuosmipcuter.orcc.soundvis;

	requires java.desktop;
	requires java.xml;
	requires orcc.api;
	opens org.mcuosmipcuter.orcc.soundvis.defaultcanvas;
	opens org.mcuosmipcuter.orcc.soundvis.effects;
}