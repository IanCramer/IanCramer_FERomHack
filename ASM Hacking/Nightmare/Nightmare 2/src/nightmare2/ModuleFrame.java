/*
 *  Nightmare 2.0 - General purpose file editor
 *
 *  Copyright (C) 2009 Hextator,
 *  hectorofchad (AIM) hectatorofchad@sbcglobal.net (MSN)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3
 *  as published by the Free Software Foundation
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  <Description> Singleton container for ModulePanes
 */

package nightmare2;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import org.jdesktop.application.FrameView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class ModuleFrame extends FrameView {
	private static App app = App.getApplication();
	private static ModuleFrame instance;
	private static LinkedList<File> openModules = new LinkedList<File>();

	// Set this to abort creating a new instance
	private static boolean problem;

	private JPanel moduleComboBoxPanel;
	private JComboBox moduleComboBox;
	private StructPane pane;

	// Set this to signal that init is complete
	private boolean ready = false;

	public void pack() {
		if (pane == null) return;

		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		int threeFourthsWidth = (screenWidth * 3)/4;
		int threeFourthsHeight = (screenHeight * 3)/4;
		JFrame frame = getFrame();
		frame.setTitle(pane.getModuleName());
		frame.setLocationRelativeTo(null);
		// Toss out invalid bounds; central component was replaced
		// and the old bounds have nothing to do with the new one
		frame.setMinimumSize(null);
		frame.setMaximumSize(null);
		frame.pack();
		int extraWidth = pane.extraWidth();
		int extraHeight = pane.extraHeight();
		int width = frame.getPreferredSize().width;
		int height = frame.getPreferredSize().height;
		width += extraWidth;
		height += extraHeight;
		if (height > threeFourthsHeight)
			height = threeFourthsHeight;
		if (width > threeFourthsWidth)
			width = threeFourthsWidth;
		frame.setSize(new Dimension(width, height));
		// Bound dimensions appropriately
		frame.setMinimumSize(frame.getSize());
		frame.setMaximumSize(new Dimension(
			screenWidth, frame.getSize().height
		));
		// Graphics need to be told to be updated by now due to
		// replacing the entire central component
		frame.repaint();
	}

	private void setModulePane(
		File moduleFile, Integer baseAddress, ModuleFrame instanceToUse
	) {
		if (instanceToUse == null)
			instanceToUse = instance;

		StructPane paneInstance = null;

		if (baseAddress == null) {
			// Create a ModulePane with a module file and give it the
			// module selection widget to display over the rest of the
			// controls
			paneInstance = new ModulePane(
				instance, instance.moduleComboBoxPanel, moduleFile
			);
		}
		else {
			// StructPane
			paneInstance = new StructPane(
				instanceToUse, moduleFile, baseAddress
			);
		}
		instanceToUse.setComponent(paneInstance);
		paneInstance.repaint();
		pane = paneInstance;

		pack();
	}

	protected static void instanceHelper(
		final boolean struct, final File input, final Integer baseAddress
	) {
		problem = false;
		SwingWorker<Void, Void> moduleThread =
			new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					if (!struct) {
						instance = new ModuleFrame(
							false, null,
							null
						);
						instance.setModulePane(openModules.peekFirst(), null, null);
						SwingUtilities.invokeAndWait(
							new Runnable() {
								@Override
								public void run() {
									app.show(instance);
									instance.pack();
								}	
							}
						);
					}
					else {
						final ModuleFrame instanceHandle = new ModuleFrame(
							true, input,
							baseAddress
						);
						instanceHandle.setModulePane(input, baseAddress, instanceHandle);
						SwingUtilities.invokeAndWait(
							new Runnable() {
								@Override
								public void run() {
									app.show(instanceHandle);
									instanceHandle.pack();
								}	
							}
						);
					}
				} catch (Exception e) {
					problem = true;
				}
				return null;
			}
		};
		moduleThread.execute();
		//while ((instance == null || !instance.ready) && !problem) {}
	}

	protected static void newInstance() {
		instanceHelper(false, null, null);
	}

	public static void newStruct(File input, int baseAddress) {
		instanceHelper(true, input, baseAddress);
	}

	private ModuleFrame(boolean struct, File input, Integer baseAddress) {
		super(app);

		final ModuleFrame curr = this;

		if (struct) {
			JFrame frame = getFrame();
			frame.addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {}

				@Override
				public void windowClosing(WindowEvent e) {
					app.hide(curr);
					curr.getFrame().dispose();
				}

				@Override
				public void windowClosed(WindowEvent e) {}

				@Override
				public void windowIconified(WindowEvent e) {}

				@Override
				public void windowDeiconified(WindowEvent e) {}

				@Override
				public void windowActivated(WindowEvent e) {}

				@Override
				public void windowDeactivated(WindowEvent e) {}
			});

			// This is just inappropers
			//setModulePane(input, baseAddress, this);

			// This is just inappropers
			//app.show(this);
			frame.setLocationRelativeTo(null);

			ready = true;
			return;
		}

		// Initialize module selection widget
		moduleComboBox = new JComboBox();
		moduleComboBox.setEditable(false);
		for (File currModule: openModules) {
			moduleComboBox.addItem(currModule.getName());
		}
		moduleComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					File curr;
					curr = openModules.get(
						moduleComboBox.getSelectedIndex()
					);
					setModulePane(curr, null, null);
				} catch (Exception e) {
					return;
				}
			}
		});
		moduleComboBoxPanel = new JPanel();
		moduleComboBoxPanel.add(moduleComboBox);

		JButton unloadButton = new JButton("Unload");
		unloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (openModules.size() <= 1) {
					resetOpenModules();
					return;
				}
				int selected = moduleComboBox.getSelectedIndex();
				openModules.remove(selected);
				moduleComboBox.removeItemAt(selected);
				setModulePane(openModules.peekFirst(), null, null);
				moduleComboBox.setSelectedIndex(0);
			}
		});
		moduleComboBoxPanel.add(unloadButton);

		JFrame frame = getFrame();
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}

			@Override
			public void windowClosing(WindowEvent e) {
				resetOpenModules();
			}

			@Override
			public void windowClosed(WindowEvent e) {}

			@Override
			public void windowIconified(WindowEvent e) {}

			@Override
			public void windowDeiconified(WindowEvent e) {}

			@Override
			public void windowActivated(WindowEvent e) {}

			@Override
			public void windowDeactivated(WindowEvent e) {}
		});

		// This is just inappropers
		//instance = this;
		//setModulePane(openModules.peekFirst(), null, null);
		moduleComboBox.setSelectedIndex(0);

		// This is just inappropers
		//app.show(this);
		frame.setLocationRelativeTo(null);

		ready = true;
	}

	public static File[] getOpenModules() {
		File[] output = new File[openModules.size()];
		int i = 0;
		for (File currFile: openModules)
			output[i++] = currFile;
		return output;
	}

	public static void resetOpenModules() {
		openModules = new LinkedList<File>();
		// No modules are open, so throw out the ModuleFrame
		if (instance != null) {
			app.hide(instance);
			instance.getFrame().dispose();
		}
		instance = null;
	}

	public static void addModule(File input, boolean update) {
		// Verify that it's actually a file and not a directory
		if (!input.isFile())
			return;

		// Check if the module is already loaded and pick it if so
		int index = -1;
		int tempIndex = -1;
		for (File curr: openModules) {
			tempIndex++;
			if (curr.getPath().equals(input.getPath())) {
				index = tempIndex;
				break;
			}
		}
		if (index != -1) {
			if (instance != null && update) {
				instance.moduleComboBox.setSelectedIndex(
					index
				);
				instance.setModulePane(input, null, null);
			}
		}

		// Add it if it's not already there
		openModules.add(input);

		// Display ModuleFrame if one wasn't present and we're
		// choosing the newly loaded module
		if (update && instance == null)
			newInstance();

		// Update the module frame accordingly if there is one
		if (instance != null) {
			instance.moduleComboBox.addItem(input.getName());
			if (update) {
				instance.moduleComboBox.setSelectedIndex(
					instance.moduleComboBox.getItemCount() - 1
				);
				instance.setModulePane(
					openModules.peekLast(), null, null
				);
			}
		}
	}

	public static void addModule(File input) {
		addModule(input, true);
	}

	public static void start() {
		// Create a ModuleFrame if there isn't one despite there
		// being at least one loaded module
		if (instance == null && openModules.size() > 0)
			newInstance();

		// Do nothing if there isn't even an open ModuleFrame
		if (instance == null || openModules.size() <= 0)
			return;

		instance.setModulePane(openModules.peekFirst(), null, null);
		instance.moduleComboBox.setSelectedIndex(0);
	}

	public static void openModule() {
		// Get a file from the user, but tell them they're choosing
		// the whole dir
		File dirFile = Common_Dialogs.showOpenFileDialog("module file");
		// Get the dir of the file they chose
		File dir = dirFile.getParentFile();
		if (dirFile == null || dir == null)
			return;

		// Get all the files in the dir for sifting
		File[] selectedFiles = dir.listFiles();
		if (selectedFiles == null)
			return;

		// Add all .nmm files from the dir, checking to see if one of them
		// was the file the user specifically chose
		int index = -1;
		int count = 0;
		for (File currFile: selectedFiles)
			if (
				currFile.getName().indexOf(".nmm")
				== currFile.getName().length() - 4
			) {
				ModuleFrame.addModule(currFile, false);
				if (currFile.getPath().equals(
					dirFile.getPath()
				))
					index = count;
				count++;
			}

		// If the exact file the user specified was indeed an .nmm file,
		// select it after updating the ModuleFrame accordingly
		selectedFiles = ModuleFrame.getOpenModules();
		if (selectedFiles != null && selectedFiles.length > 0 && index != -1) {
			if (instance == null)
				newInstance();
			instance.setModulePane(dirFile, null, null);
			instance.moduleComboBox.setSelectedIndex(
				instance.moduleComboBox.getItemCount() - count + index
			);
		}
	}

	public static ModuleFrame getInstance() { return instance; }
}
