package de.htw.mp.ui.component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * Copyright (C) 2016 by Klaus Jung
 * All rights reserved.
 * Date: 2016-09-17
 */
public class ImageView extends JComponent {

	private static final long serialVersionUID = 3017702760660613243L;

	private BufferedImage image = null;
	
	//
	// painting
	//
	@Override
	public void paintComponent(Graphics g) {
		
		Graphics2D g2 = (Graphics2D)g;		// context with extended capabilities
		Dimension displaySize = this.getSize();	// display size
		
		// clear background
		g.clearRect(0, 0, displaySize.width, displaySize.height);
		
		// draw image
		if(image != null) {
			
			// calculate drawing rectangle, keep aspect ratio of image
			Dimension drawingSize = new Dimension();
			double scaleX = (double)displaySize.width / image.getWidth();
			double scaleY = (double)displaySize.height / image.getHeight();
			if(scaleX < scaleY) {
				drawingSize.width = displaySize.width;
				drawingSize.height = (int)(scaleX * image.getHeight() + 0.5);
			} else {
				drawingSize.width = (int)(scaleY * image.getWidth() + 0.5);
				drawingSize.height = displaySize.height;
			}

			// center the image
			int offsetX = Math.max((displaySize.width - drawingSize.width) / 2, 0);
			int offsetY = Math.max((displaySize.height - drawingSize.height) / 2, 0);
			
		    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		    //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		    //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.drawImage(image, offsetX, offsetY, drawingSize.width, drawingSize.height, this);
		}
	}
	
	/**
	 * Change the image
	 * 
	 * @param image
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
		
		// redraw the component
		invalidate();
		repaint();
	}	
}
