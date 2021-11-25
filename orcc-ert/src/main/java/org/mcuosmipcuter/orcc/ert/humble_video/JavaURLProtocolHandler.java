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
	
	byte[] data;
	ByteArrayInputStream in;
	/**
	 * 
	 */
	public JavaURLProtocolHandler() {
		init();
	}

	@Override
	public int open(String url, int flags) {
		System.err.println("open()" + url + " " + flags);
		try {
			URL url1 = new URL(url);
			InputStream is = url1.openStream();
			data = is.readAllBytes();
			in = new ByteArrayInputStream(data);
		} catch (Exception e) {
			IOUtil.log("error opening " + url + " " + e);
			return -1;
		}
		return 0;
	}

	@Override
	public int read(byte[] buf, int size) {
		//System.err.println("read() " + size);
		try {
			int ret = in.read(buf, 0, size);
			//System.err.println("read() " + size + " ret=" + ret);
			return ret == -1 ? 0 : ret;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public int write(byte[] buf, int size) {
		//System.err.println("write() " + size + " [" + (new String(buf)) + "]" + Arrays.toString(buf));
		// write request ignored
		return size;
	}

	@Override
	public long seek(long offset, int whence) {
		System.err.println("seek() " + offset + ", " + whence);
		if(whence == 0) {
			in.reset();
			return 0;
		}
		else if(whence == 65536) {
			return data.length;
		}
		return -1;
	}

	@Override
	public int close() {
		System.err.println("close()");
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean isStreamed(String url, int flags) {
		System.err.println("isStreamed() " + url);
		return false;
	}
	
	public static void init() {
		if(!init) {
			IURLProtocolHandlerFactory factory = new IURLProtocolHandlerFactory() {
				@Override
				public IURLProtocolHandler getHandler(String protocol, String url, int flags) {
					System.err.println("getHandler(" + protocol + ", " + url + ", " + flags + ")");
					return new JavaURLProtocolHandler();
				}};
			URLProtocolManager.getManager().registerFactory("jar", factory);
			init = true;
		}
	}

}
