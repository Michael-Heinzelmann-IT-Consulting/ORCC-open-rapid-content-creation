package org.mcuosmipcuter.orcc.gui.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionsFileFilter extends FileFilter {

	private final String[] extensions;
	private final String description;
	public ExtensionsFileFilter(String ...  extensions) {
		this.extensions = extensions;
		StringBuilder stringBuilder = new StringBuilder();
		for(int i= 0; i < extensions.length; i++) {
			if(i > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(extensions[i]);
		}
		description = stringBuilder.toString();
	}

	@Override
	public boolean accept(File f) {
		if(f.isDirectory()) {
			return true;
		}
		for(String ext : extensions) {
			if(f.getAbsolutePath().endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
