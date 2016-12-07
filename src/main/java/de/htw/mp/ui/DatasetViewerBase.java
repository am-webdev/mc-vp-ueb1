package de.htw.mp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import de.htw.mp.ui.component.ColorView;
import de.htw.mp.ui.component.ImageView;

/**
 * Simple data set viewer. Categorizes and lists all image files in a directory.
 * The UI provides an image viewer and mean color calculation.
 * 
 * @author Nico Hezel
 */
public abstract class DatasetViewerBase extends JPanel {

	private static final long serialVersionUID = 8420613964694777857L;

	// spacing and border size
	private static final int border = 10;
	
	/**
	 * For each image category there exists a file list
	 */
	private Map<String, List<File>> categoryToFileList = new HashMap<>();
	
	/**
	 * Content of the left list
	 */
	private JList<String> categoryList = null;
	private DefaultListModel<String> categoryListModel = null;
	
	/**
	 * Content of the right list
	 */
	private JList<String> imageFileList = null;
	private DefaultListModel<String> imageFileListModel = null;

	/**
	 * Image display on the bottom left
	 */
	private ImageView imageDisplay = null;
	
	/**
	 * Color display on the bottom right
	 */
	private ColorView colorDisplay = null;

	/**
	 * Constructor. Constructs the layout of the GUI components and loads the
	 * initial image.
	 */
	public DatasetViewerBase() {
		super(new BorderLayout(border, border));

		// add the menu
		{
			JPanel menuPanel = new JPanel(new GridLayout(1, 4, border, border));
			menuPanel.setPreferredSize(new Dimension(800, 200));
			add(menuPanel, BorderLayout.NORTH);

			// add open folder button
			{
				JButton openDirectoryBtn = new JButton("Open Folder");
				openDirectoryBtn.addActionListener(this::onOpenDirectoryClick); // click event handler
				menuPanel.add(openDirectoryBtn);
			}

			// add category combo box and a describing label
			{
				JPanel categoryMenuPanel = new JPanel(new GridBagLayout());
				menuPanel.add(categoryMenuPanel);

				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				c.weighty = .2; // request any extra vertical space
				c.weightx = 1.0; // request any extra vertical space

				JLabel categoryLabel = new JLabel("Category");
				categoryMenuPanel.add(categoryLabel, c);

				// model containing all elements of the list
				categoryListModel = new DefaultListModel<>();
				categoryList = new JList<>(categoryListModel);
				categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				categoryList.addListSelectionListener(this::onCategoryListChange); // selection change  handler
				JScrollPane listScroller = new JScrollPane(categoryList);
				c.weighty = .8; // request any extra vertical space
				c.gridy = 1;
				categoryMenuPanel.add(listScroller, c);
			}

			// add category combo box and a describing label
			{
				JPanel categoryMenuPanel = new JPanel(new GridBagLayout());
				menuPanel.add(categoryMenuPanel);

				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				c.weighty = .2; // request any extra vertical space
				c.weightx = 1.0; // request any extra vertical space

				JLabel categoryLabel = new JLabel("Image Files");
				categoryMenuPanel.add(categoryLabel, c);

				imageFileListModel = new DefaultListModel<>();
				imageFileList = new JList<>(imageFileListModel);				
				imageFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				imageFileList.addListSelectionListener(this::onImageFileListChange); // selection change handler
				JScrollPane listScroller = new JScrollPane(imageFileList);
				c.weighty = .8; // request any extra vertical space
				c.gridy = 1;
				categoryMenuPanel.add(listScroller, c);
			}
		}

		// setup the image display
		imageDisplay = new ImageView();
		imageDisplay.setPreferredSize(new Dimension(400, 400));
		add(imageDisplay, BorderLayout.WEST);
		
		// setup the image display
		colorDisplay = new ColorView();
		colorDisplay.setPreferredSize(new Dimension(400, 400));
		add(colorDisplay, BorderLayout.EAST);
		
		// load the initial image
		try {
			URL res = getClass().getResource("/Hummel.jpg");
			File imageFile = Paths.get(res.toURI()).toFile();
			updateMeanColorAndImage(imageFile);
			imageDisplay.setImage(ImageIO.read(imageFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	};

	/**
	 * Analysis all images inside the selected category and paint their mean
	 * color in the color panel. Lists all image files of the category in the
	 * image file list view.
	 * 
	 * @param event
	 */
	private void onCategoryListChange(ListSelectionEvent event) {
		if (event.getValueIsAdjusting() == false) {
			
			String categoryName = categoryList.getSelectedValue();
			if(categoryName != null) {				
				List<File> imageFiles = categoryToFileList.get(categoryName);
				
				// update the mean color pane and the mean image pane
				updateMeanColorAndImage(imageFiles.toArray(new File[0]));			
				
				// list all the image file names
				imageFileListModel.clear();
				for (File file : imageFiles)
					imageFileListModel.addElement(file.toPath().getFileName().toString());
			}			
		}
	}

	/**
	 * Loads and displays the image from the selected image file.
	 * 
	 * @param event
	 */
	private void onImageFileListChange(ListSelectionEvent event) {
		if (event.getValueIsAdjusting() == false) {

			String categoryName = categoryList.getSelectedValue();
			String filename = imageFileList.getSelectedValue();
			if(categoryName != null && filename != null) {
				File[] imageFiles = categoryToFileList.get(categoryName)
													  .stream()
													  .filter(file -> file.toPath().getFileName().toString().equals(filename))
													  .toArray(File[]::new);
				
				// update the mean color pane and the mean image pane
				updateMeanColorAndImage(imageFiles);
			}
		}
	}
	
	/**
	 * Calculate a new mean color and mean image and displays them on the UI.
	 * 
	 * @param imageFiles
	 */
	private void updateMeanColorAndImage(File ... imageFiles) {
		
		// calculate the mean color of the category
		Color meanColor = getMeanColor(imageFiles);
		colorDisplay.setColor(meanColor);
		
		// calculate the mean image of the category
		BufferedImage image = getMeanImage(imageFiles);
		imageDisplay.setImage(image);
	}

	/**
	 * Opens a dialog to select a data directory. All image files inside the
	 * directory will be filtered and categorized bases on their names. The
	 * resulting categories are listed in the category list view.
	 * 
	 * @param event
	 */
	private void onOpenDirectoryClick(ActionEvent event) {

		// open the directory chooser
		JFileChooser dirChooser = new JFileChooser("dataset");
		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (dirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File dir = dirChooser.getSelectedFile();

			// abort
			if(dir == null) return;

			// read all image files from the directory
			categoryToFileList.clear();
			try (DirectoryStream<Path> files = Files.newDirectoryStream(dir.toPath(), "*.{jpg,jpeg,png}")) {
				for (Path imageFile : files) {
					String name = imageFile.getFileName().toString().split("_")[0];
					List<File> cat = categoryToFileList.getOrDefault(name, new ArrayList<File>());
					cat.add(imageFile.toFile());
					categoryToFileList.putIfAbsent(name, cat);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			// -------------------- Build Category List -------------------------
			// add an "All" category
			List<File> all = categoryToFileList.values()
											   .stream()
											   .flatMap(files -> files.stream())
											   .collect(Collectors.toList());

			// list all category names
			resetAll();
			categoryListModel.addElement("All");
			categoryToFileList.keySet().stream().sorted().forEach(name -> categoryListModel.addElement(name));
			
			// now add the All category
			categoryToFileList.put("All", all);
		}
	}
	
	/**
	 * Clears all lists and displays
	 */
	private void resetAll() {
		categoryListModel.clear();
		imageFileListModel.clear();
		imageDisplay.setImage(null);
		colorDisplay.setColor(Color.WHITE);
	}
	
	/**
	 * Calculate the mean color of all given images. Or return PINK if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public abstract Color getMeanColor(File ... imageFiles);
	
	/**
	 * Calculate the mean images of all given images. Or return NULL if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public abstract BufferedImage getMeanImage(File ... imageFiles);
}