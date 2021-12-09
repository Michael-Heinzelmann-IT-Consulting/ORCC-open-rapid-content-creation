package org.mcuosmipcuter.orcc.gui.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionsFileFilter extends FileFilter {

	private final String extension;
	
	public ExtensionsFileFilter(String extension) {
		this.extension = extension;
	}

	@Override
	public boolean accept(File f) {
		return f.isDirectory() || f.getAbsolutePath().endsWith(extension);
	}

	@Override
	public String getDescription() {
		return extension;
	}

	public String getExtension() {
		return extension;
	}

}
