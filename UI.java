import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class UI {
	private File[] obj_files;
	private File[] lm_files;
	private File[] bksb_files;
	
	public UI() {
		
	}
	
	public void createAndShowGUI() {
		JFrame f = new JFrame();
		
		JTabbedPane tabs = new JTabbedPane();
		
		JPanel toObjPanel = new JPanel();
		JButton selectBksbButton = new JButton("Select .bksb");
		JButton convertToObjButton = new JButton("Convert to .obj");
		convertToObjButton.setEnabled(false);
		JCheckBox lightmapCheckbox = new JCheckBox("Generate lightmap .obj");
		toObjPanel.add(selectBksbButton);
		toObjPanel.add(convertToObjButton);
		toObjPanel.add(lightmapCheckbox);
		tabs.add("To .obj", toObjPanel);
		
		selectBksbButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				FileFilter filter = new FileNameExtensionFilter("BKSB files", "bksb");
				fileChooser.setFileFilter(filter);
				try {
					fileChooser.setCurrentDirectory(new File(".").getCanonicalFile());
				}
				catch (Exception ex) {
					System.out.println("Error selecting bksb file.");
				}
				int result = fileChooser.showOpenDialog(fileChooser);
				if (result == JFileChooser.APPROVE_OPTION) {
				    bksb_files = fileChooser.getSelectedFiles();
				    for (File file : bksb_files) {
				    	System.out.println("Selected file: " + file.getPath());
				    }
				    convertToObjButton.setEnabled(true);
				}
			}
		});
		
		convertToObjButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (File file : bksb_files) {
					try {
						Bksb bksb = new Bksb(file);
						bksb.generateObj(lightmapCheckbox.isSelected());
					}
					catch (Exception ex) {
						System.out.println("Error converting bksb to obj.");
					}
				}
				
			}
		});
		
		
		JPanel toBksbPanel = new JPanel();
		JButton selectObjButton = new JButton("Select .obj");
		JButton addLightmapButton = new JButton("Add lightmap");
		JButton convertToBksbButton = new JButton("Convert to .bksb");
		convertToBksbButton.setEnabled(false);
		addLightmapButton.setEnabled(false);
		JCheckBox animatedCheckbox = new JCheckBox("Animated");
		toBksbPanel.add(selectObjButton);
		toBksbPanel.add(addLightmapButton);
		toBksbPanel.add(convertToBksbButton);
		toBksbPanel.add(animatedCheckbox);
		tabs.add("To .bksb", toBksbPanel);
		
		selectObjButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				FileFilter filter = new FileNameExtensionFilter("OBJ files", "obj");
				fileChooser.setFileFilter(filter);
				try {
					fileChooser.setCurrentDirectory(new File(".").getCanonicalFile());
				}
				catch (Exception ex) {
					System.out.println("Error selecting obj file.");
				}
				int result = fileChooser.showOpenDialog(fileChooser);
				if (result == JFileChooser.APPROVE_OPTION) {
				    obj_files = fileChooser.getSelectedFiles();
				    for (File file : obj_files) {
				    	System.out.println("Selected file: " + file.getPath());
				    }
				    convertToBksbButton.setEnabled(true);
				    addLightmapButton.setEnabled(true);
				    lm_files = null;
				}
			}
		});
		
		convertToBksbButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Obj obj;
				if (animatedCheckbox.isSelected()) {
					try {
						obj = new Obj(obj_files);
						if (lm_files != null) {
							if (!obj.addLightmap(lm_files)) {
								System.out.println("Error adding lightmap: geometry and/or number of UV coordinates do not match.");
							}
						}
						obj.generateBksb();
					}
					catch (Exception ex) {
						System.out.println("Error converting animated obj to bksb.");
					}
				}
				else {
					for (int i = 0; i < obj_files.length; i++) {
						try {
							obj = new Obj(obj_files[i]);
							if (lm_files != null) {
								if (!obj.addLightmap(lm_files[i])) {
									System.out.println("Error adding lightmap " + lm_files[i].getName() + " to " + obj_files[i].getName()
											+ ": geometry and/or number of UV coordinates do not match.");
								}
							}
							obj.generateBksb();
						}
						catch (Exception ex) {
							System.out.println("Error converting obj to bksb.");
							ex.printStackTrace();
						}
					}
				}
			}
		});
		
		addLightmapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				FileFilter filter = new FileNameExtensionFilter("OBJ files", "obj");
				fileChooser.setFileFilter(filter);
				try {
					fileChooser.setCurrentDirectory(new File(".").getCanonicalFile());
				}
				catch (Exception ex) {
					System.out.println("Error selecting obj file.");
				}
				int result = fileChooser.showOpenDialog(fileChooser);
				if (result == JFileChooser.APPROVE_OPTION) {
					if (fileChooser.getSelectedFiles().length != obj_files.length) {
						System.out.println("Number of lightmap obj files must match number of regular obj files.");
					}
					else {
					    lm_files = fileChooser.getSelectedFiles();
					    for (File file : lm_files) {
					    	System.out.println("Selected file: " + file.getPath());
					    }
					}
				}
			}
		});
		
		f.add(tabs);
		f.setTitle("BKSB Converter");
		f.setSize(525, 120);
		f.setLocationRelativeTo(null);
		f.setResizable(false);
		f.setLayout(new FlowLayout()); 
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
