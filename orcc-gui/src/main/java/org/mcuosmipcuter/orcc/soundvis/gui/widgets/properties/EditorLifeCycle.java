package org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties;

import org.mcuosmipcuter.orcc.api.soundvis.InputEnabling;

public interface EditorLifeCycle extends InputEnabling{

	void activate();

	void passivate();

}