package de.htw.mp.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Simple data set viewer. Categorizes and lists all image files in a directory.
 * The UI provides an image viewer and mean color calculation.
 * 
 * @author Nico Hezel
 */
public class DatasetViewer extends DatasetViewerBase {
	
	private static final long serialVersionUID = -6288314471660252417L;

	/**
	 * Calculate the mean color of all given images. Or return PINK if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public Color getMeanColor(File ... imageFiles) {
		int avrRed = 0;
		int avrGreen = 0;
		int avrBlue = 0;
		
		if(imageFiles.length == 0) {
			return Color.PINK;	// no images? return PINK
		} else {
			for (File imgFile : imageFiles) {
				BufferedImage bufferdImg = null;
				
				int red = 0;
				int green = 0;
				int blue = 0;
				
				try {
					// Read Image from file system
					bufferdImg = ImageIO.read(imgFile);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// ensure color spectrum is in a correct RGB
				bufferdImg = ensureCorrectColorSpectrum(bufferdImg);
				
				int width = bufferdImg.getWidth();
				int height = bufferdImg.getHeight();
				int[] pixels = new int[width * height];
				bufferdImg.getRGB(0, 0, width, height, pixels, 0, width);
				//System.out.println("Length:\t"+pixels.length+"\nPath:\t"+imgFile.getPath());				
				
				// sum up color per channel per pixel (pos)
				for(int y = 0; y < height; y++) { for(int x = 0; x < width; x++) {
					int pos = y * width + x;
						int rgb 	= pixels[pos];
						red 	+= (rgb >> 16) & 0xff; 
						green 	+= (rgb >> 8) & 0xff;
						blue 	+= rgb & 0xff;
					}
				}
				avrRed 		= (red 		/ (width*height));
				avrGreen 	= (green 	/ (width*height));
				avrBlue		= (blue 	/ (width*height));
				
//				System.out.println("File:\t"+imgFile.getName() +
//						"\n\tred:\t"+avrRed+
//						"\n\tgreen:\t"+avrGreen+
//						"\n\tblue:\t"+avrBlue);
			}
		}
		
		return new Color((avrRed), (avrGreen), (avrBlue));
	}
	
	/**
	 * Calculate the mean image of all given images. Or return NULL if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public BufferedImage getMeanImage(File ... imageFiles) {
		
		BufferedImage images[] = new BufferedImage[imageFiles.length];
		for (int i = 0; i < imageFiles.length; i++) {
			BufferedImage currentImg = null;
			try {
				// Read Image from file system
				currentImg = ImageIO.read(imageFiles[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// ensure color spectrum is in a correct RGB
			currentImg = ensureCorrectColorSpectrum(currentImg);
			images[i] = currentImg;
		}
		
		// Prepare new bufferdImage that can be used to add the avr Color per pixel later on
		BufferedImage average = new BufferedImage(images[0].getWidth(), images[0].getHeight(), BufferedImage.TYPE_INT_RGB);
		int width = average.getWidth();
		int height = average.getHeight();		
		int avrPixels[] = new int[width * height];

		//calculate mean color per pixel
		for (int pixelPointer = 0; pixelPointer < avrPixels.length; pixelPointer++) {
			
			int avrRed = 0;
			int avrGreen = 0;
			int avrBlue = 0;

			int currentRed = 0;
			int currentGreen = 0;
			int currentBlue = 0;
			
			for (BufferedImage currentImage : images) {
				
				int currentWidth = currentImage.getWidth();
				int currenHeight = currentImage.getHeight();	
				int crntPixels[] = new int[currentWidth * currenHeight];
				
				/*
				 * getRGB() ist zu teuer für jedes Pixel
				 */
				currentImage.getRGB(0, 0, currentWidth, currenHeight, crntPixels, 0, currentWidth); 
				
				int crntRGB = crntPixels[pixelPointer];
				int r 	= (crntRGB >> 16) & 0xff; 
				int g	= (crntRGB >> 8) & 0xff;
				int b	= (crntRGB >> 0) & 0xff;
				
				currentRed += r;
				currentGreen += g;
				currentBlue += b;
			}
			
			avrRed = currentRed/imageFiles.length;
			avrGreen = currentGreen/imageFiles.length;
			avrBlue = currentBlue/imageFiles.length;
				
			// Calculate average per pixel
			avrRed 		= preventColorOverflow(avrRed);
			avrGreen 	= preventColorOverflow(avrGreen);
			avrBlue 	= preventColorOverflow(avrBlue);
						
			avrPixels[pixelPointer] =  (avrRed << 16) | (avrGreen << 8) | avrBlue;
		}
		
		average.setRGB(0, 0, width, height, avrPixels, 0, width); 	// very slow performance on MacBook Air
		
		return average;
	}
	
	private int preventColorOverflow(int singleColor) {
		int fixedColor = singleColor;
		if (singleColor > 255) {
			fixedColor = 255;
			System.out.println("Given Color("+singleColor+") greater than 255, set to 255");
		} else if(singleColor<0) {
			fixedColor = 0;
			System.out.println("Given Color("+singleColor+") smaller than 0, set to 0");
		}
		return fixedColor;
	}
	
	private BufferedImage ensureCorrectColorSpectrum(BufferedImage bufferedImage) {
		// ensure color spectrum is in a correct RGB
		if(bufferedImage.getType() != BufferedImage.TYPE_INT_RGB && bufferedImage.getType() != BufferedImage.TYPE_INT_ARGB) {
			
			BufferedImage biRGB = new BufferedImage(bufferedImage.getWidth(),
					bufferedImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = biRGB.createGraphics();
			g.drawImage(bufferedImage, 0, 0, null);
			g.dispose();
			bufferedImage = biRGB;
		}
		return bufferedImage;
	}
}