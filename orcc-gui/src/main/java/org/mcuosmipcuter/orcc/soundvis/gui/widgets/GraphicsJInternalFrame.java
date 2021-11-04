/**
 * 
 */
package org.mcuosmipcuter.orcc.soundvis.gui.widgets;

import javax.swing.JInternalFrame;

/**
 * @author user
 *
 */
public class GraphicsJInternalFrame extends JInternalFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3579630003636142706L;
	private String realtimeTitle;
	private String inputTitle;
	private String outputTitle;
	
	
	
	public GraphicsJInternalFrame(String string, boolean b, boolean c, boolean d, boolean e) {
		super(string, b, c, d, e);
	}

	public String getRealtimeTitle() {
		return realtimeTitle;
	}
	private void setTitle() {
		StringBuilder sb = new StringBuilder();
		if(inputTitle != null) {
			sb.append("| " + inputTitle + " |");
		}
		if(outputTitle != null) {
			sb.append(" "  + outputTitle + " ");
		}
		if(realtimeTitle != null) {
			sb.append("| " + realtimeTitle + " |");
		}
		setTitle(sb.toString());
	}
	public void setRealtimeTitle(String realtimeTitle) {
		this.realtimeTitle = realtimeTitle;
		setTitle();
	}
	
	public String getOutputTitle() {
		return outputTitle;
	}
	public void setOutputTitle(String outputTitle) {
		this.outputTitle = outputTitle;
		setTitle();
	}
	
	public String getInputTitle() {
		return inputTitle;
	}
	public void setInputTitle(String inputTitle) {
		this.inputTitle = inputTitle;
		setTitle();
	}


}
