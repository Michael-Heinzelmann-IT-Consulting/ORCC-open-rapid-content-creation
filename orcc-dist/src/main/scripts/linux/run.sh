echo "ORCC rapid content creation for entertainment, education and media production"
echo "Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting"
echo ""
echo "This program is free software: you can redistribute it and/or modify"
echo "it under the terms of the GNU General Public License as published by"
echo "the Free Software Foundation, either version 3 of the License, or"
echo "(at your option) any later version."
echo ""
echo "This program is distributed in the hope that it will be useful,"
echo "but WITHOUT ANY WARRANTY; without even the implied warranty of"
echo "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
echo "GNU General Public License for more details."
echo ""
echo "You should have received a copy of the GNU General Public License"
echo "along with this program.  If not, see <http://www.gnu.org/licenses/>."
echo "";
cd `dirname $0`;
echo "`date` starting in `pwd`";
if [ "$1" = "SNAP" ]
then
  JPREFS="-Djava.util.prefs.userRoot=$SNAP_USER_DATA"
echo "setting java preferences to snap $JPREFS"
fi

java $JPREFS -Dorg.mcuosmipcuter.orcc.util.IOUtil.log=true -cp orcc-gui-${pom.version}.jar:orcc-core-${pom.version}.jar:orcc-api-${pom.version}.jar:orcc-ert-${pom.version}.jar:slf4j-api-1.6.6.jar:humble-video-all-0.3.0.jar:humble-video-arch-i686-pc-linux-gnu6-0.3.0.jar:humble-video-arch-x86_64-pc-linux-gnu6-0.3.0.jar:humble-video-noarch-0.3.0.jar org.mcuosmipcuter.orcc.gui.Main $@;
