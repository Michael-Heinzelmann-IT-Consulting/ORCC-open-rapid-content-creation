rem = ORCC rapid content creation for entertainment, education and media production
rem = Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting

rem = This program is free software: you can redistribute it and/or modify
rem = it under the terms of the GNU General Public License as published by
rem = the Free Software Foundation, either version 3 of the License, or
rem = (at your option) any later version.

rem = This program is distributed in the hope that it will be useful,
rem = but WITHOUT ANY WARRANTY; without even the implied warranty of
rem = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem = GNU General Public License for more details.

rem = You should have received a copy of the GNU General Public License
rem = along with this program.  If not, see <http://www.gnu.org/licenses/>.

java -cp orcc-gui-${pom.version}.jar;orcc-core-${pom.version}.jar;orcc-api-${pom.version}.jar;orcc-ert-${pom.version}.jar;slf4j-api-1.6.6.jar;xuggle-xuggler-5.4.jar org.mcuosmipcuter.orcc.gui.Main
