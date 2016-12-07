package de.htw.mp;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JFrame;

import de.htw.mp.ui.DatasetViewer;

/**
 * Program entrance point of the DatasetViewer template.
 * 
 * @author Nico Hezel
 */
public class MP_CV_Ue01_Swing {

	/**
	 * Main method. 
	 * @param args - ignored. No arguments are used by this application.
	 */
	public static void main(String[] args) {
				
        // schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	createAndShowGUI();
            }
        });
	}
	
	/**
	 * Set up and show the main frame.
	 */
	public static void createAndShowGUI() {
		// create and setup the window
		JFrame frame = new JFrame("DatasetViewer - Template");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent contentPane = new DatasetViewer();
        contentPane.setOpaque(true);	//content panes must be opaque
        frame.setContentPane(contentPane);

        // display the window centered on screen
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}
}
