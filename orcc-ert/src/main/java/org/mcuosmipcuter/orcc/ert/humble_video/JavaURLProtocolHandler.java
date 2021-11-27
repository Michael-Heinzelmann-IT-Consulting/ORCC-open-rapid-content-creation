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
package org.mcuosmipcuter.orcc.ert.humble_video;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.mcuosmipcuter.orcc.util.IOUtil;

import io.humble.video.customio.IURLProtocolHandler;
import io.humble.video.customio.IURLProtocolHandlerFactory;
import io.humble.video.customio.URLProtocolManager;

/**
 * @author Michael Heinzelmann
 *
 */
public class JavaURLProtocolHandler implements IURLProtocolHandler {
	private static boolean init;
	
	int size;
	ByteArrayInputStream in;
	/**
	 * 
	 */
	public JavaURLProtocolHandler() {
		init();
	}

	@Override
	public int open(String url, int flags) {
		IOUtil.log("open()" + url + " " + flags);
		try {
			URL url1 = new URL(url);
			InputStream is = url1.openStream();
			byte[]data = is.readAllBytes();
			size = data.length;
			in = new ByteArrayInputStream(data);
		} catch (Exception e) {
			IOUtil.log("error opening " + url + " " + e);
			return -1;
		}
		return 0;
	}

	@Override
	public int read(byte[] buf, int size) {
		try {
			int ret = in.read(buf, 0, size);
			return ret == -1 ? 0 : ret;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public int write(byte[] buf, int size) {
		IOUtil.log("write() " + size + " [" + (new String(buf)) + "]");
		// write request ignored
		return size;
	}

	@Override
	public long seek(long offset, int whence) {
		IOUtil.log("seek() " + offset + ", " + whence);
		if(whence == 0) {
			in.reset();
			return 0;
		}
		else if(whence == 65536) {
			return size;
		}
		return -1;
	}

	@Override
	public int close() {
		IOUtil.log("close()");
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean isStreamed(String url, int flags) {
		IOUtil.log("isStreamed() " + url);
		return false;
	}
	
	public static void init() {
		if(!init) {
			IURLProtocolHandlerFactory factory = new IURLProtocolHandlerFactory() {
				@Override
				public IURLProtocolHandler getHandler(String protocol, String url, int flags) {
					IOUtil.log("getHandler(" + protocol + ", " + url + ", " + flags + ")");
					return new JavaURLProtocolHandler();
				}};
			URLProtocolManager.getManager().registerFactory("jar", factory);
			init = true;
		}
	}

}
