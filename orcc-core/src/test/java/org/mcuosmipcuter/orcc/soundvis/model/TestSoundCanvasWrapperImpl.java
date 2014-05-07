/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2013 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import junit.framework.TestCase;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;

/**
 * @author Michael Heinzelmann
 *
 */
public class TestSoundCanvasWrapperImpl extends TestCase {

	private SoundCanvasWrapper soundCanvasWrapperImpl;
	private MyInvocationHandler myInvocationHandler;
	private final int SAMPLESIZE_BITS = 16;

	@Override
	protected void setUp() throws Exception {
		AudioFormat audioFormat = new AudioFormat(44100, SAMPLESIZE_BITS, 2, false, false);
		AudioInputInfo audioInputInfo = new AudioInputInfoImpl(audioFormat, 1000L);
		VideoOutputInfo videoOutputInfo = new VideoOutputInfoImpl(30, 480, 600);
		myInvocationHandler = new MyInvocationHandler();
		SoundCanvas mockSoundCanvas = createMock(myInvocationHandler);
		soundCanvasWrapperImpl = new SoundCanvasWrapperImpl(mockSoundCanvas);
		soundCanvasWrapperImpl.prepare(audioInputInfo, videoOutputInfo);
	}
	private static class Invocation {
		private final String methodName;
		private final Object[] args;

		public Object[] getArgs() {
			return args;
		}

		public Invocation(String methodName, Object[] args) {
			this.methodName = methodName;
			this.args = args;
		}

		@Override
		public String toString() {
			return "Invocation: " + methodName + "("
					+ Arrays.toString(args) + ")";
		}
		
	}
	private static class MyInvocationHandler implements InvocationHandler {
		private List<Invocation> invocations = new ArrayList<Invocation>();
		public List<Invocation> getInvocations(String name) {
			List<Invocation> result = new ArrayList<Invocation>();
			for(Invocation invocation : invocations) {
				if(invocation.methodName.equals(name)) {
					result.add(invocation);
				}
			}
			return result;
		}
		@Override
		public Object invoke(Object proxy, Method method, Object[] args){
			//System.err.println(method + " : " + args);
			Invocation invocation = new Invocation(method.getName(), args);
			System.err.println(invocation);
			invocations.add(invocation);
			return null;
		}
	}
	private SoundCanvas createMock(InvocationHandler invocationHandler) {
		return (SoundCanvas) Proxy.newProxyInstance(TestSoundCanvasWrapperImpl.class.getClassLoader(), new Class[] {SoundCanvas.class}, invocationHandler);
	}
	
	public void testPrepare() {
		assertEquals(1, myInvocationHandler.getInvocations(("prepare")).size());
	}
	public void testEnabled() {
		soundCanvasWrapperImpl.newFrame(4, null);
		soundCanvasWrapperImpl.setVisible(false);
		soundCanvasWrapperImpl.newFrame(5, null);
		Iterator<Invocation> iterator = myInvocationHandler.getInvocations(("newFrame")).iterator();
		assertNull("expected local null graphics", iterator.next().getArgs()[1]);
		assertNotNull("expected dummy graphics", iterator.next().getArgs()[1]);
	}
	
	public void testFromTo() {
		
		soundCanvasWrapperImpl.setFrameFrom(5);
		soundCanvasWrapperImpl.setFrameTo(6);
		
		soundCanvasWrapperImpl.newFrame(4, null);
		soundCanvasWrapperImpl.newFrame(5, null);
		soundCanvasWrapperImpl.newFrame(6, null);
		soundCanvasWrapperImpl.newFrame(7, null);
		Iterator<Invocation> iterator = myInvocationHandler.getInvocations(("newFrame")).iterator();
		
		assertNotNull("expected dummy graphics", iterator.next().getArgs()[1]);
		assertNull("expected local null graphics", iterator.next().getArgs()[1]);
		assertNull("expected local null graphics", iterator.next().getArgs()[1]);
		assertNotNull("expected dummy graphics", iterator.next().getArgs()[1]);
	}
	public void testFirstFrameLogic() {
		
		soundCanvasWrapperImpl.setRepaintThreshold(10); //  user setting
		
		int[] amplitudes = new int[] {((int) Math.pow(2, SAMPLESIZE_BITS)) / 2}; //  amplitude= 0
		soundCanvasWrapperImpl.nextSample(amplitudes);
		soundCanvasWrapperImpl.newFrame(1, null);
		
		amplitudes = new int[] {(int) Math.pow(2, SAMPLESIZE_BITS)}; // maximum amplitude
		soundCanvasWrapperImpl.nextSample(amplitudes);
		soundCanvasWrapperImpl.newFrame(4, null);
		
		Iterator<Invocation> iterator = myInvocationHandler.getInvocations(("newFrame")).iterator();
		assertNotNull("expected dummy graphics", iterator.next().getArgs()[1]);
		assertNull("expected local null graphics", iterator.next().getArgs()[1]);

	}
	
	public void testThresholdExceeded() {
		
		int[] amplitudes = new int[] {(int) Math.pow(2, SAMPLESIZE_BITS)}; // maximum amplitude
		
		soundCanvasWrapperImpl.setRepaintThreshold(100); // maximum threshold user setting
		soundCanvasWrapperImpl.nextSample(amplitudes);
		soundCanvasWrapperImpl.newFrame(5, null);
		
		soundCanvasWrapperImpl.setRepaintThreshold(9); // lowering user setting
		soundCanvasWrapperImpl.nextSample(amplitudes);
		soundCanvasWrapperImpl.newFrame(6, null);
		
		amplitudes = new int[] {((int) Math.pow(2, SAMPLESIZE_BITS)) / 2}; //  amplitude= 0
		
		soundCanvasWrapperImpl.nextSample(amplitudes);
		soundCanvasWrapperImpl.newFrame(7, null);
		
		amplitudes = new int[] {((int) Math.pow(2, SAMPLESIZE_BITS)) / 5}; //  amplitude= 20 %
		
		soundCanvasWrapperImpl.nextSample(amplitudes);
		soundCanvasWrapperImpl.newFrame(8, null);
		
		
		Iterator<Invocation> iterator = myInvocationHandler.getInvocations(("newFrame")).iterator();
		assertNotNull("expected dummy graphics", iterator.next().getArgs()[1]);
		assertNotNull("expected dummy graphics", iterator.next().getArgs()[1]);
		assertNotNull("expected dummy graphics", iterator.next().getArgs()[1]);
		assertNull("expected local null graphics", iterator.next().getArgs()[1]);
	}
	
}
