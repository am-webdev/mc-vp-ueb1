package de.htw.mp.ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * A panel displaying a color
 */
public class ColorView extends JComponent {

	private static final long serialVersionUID = 3017702760660613243L;

	private Color color = null;
	
	//
	// painting
	//
	@Override
	public void paintComponent(Graphics g) {
		Dimension displaySize = this.getSize();	// display size
		
		// clear background
		g.clearRect(0, 0, displaySize.width, displaySize.height);
		// Set color and fill view
        g.setColor(this.color);
        g.fillRect(0, 0, displaySize.width, displaySize.height);
		
	}
	
	/**
	 * Change the image
	 * 
	 * @param image
	 */
	public void setColor(Color color) {
		this.color = color;
		
		// redraw the component
		invalidate();
		repaint();
	}
}