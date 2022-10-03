/*
 *  Nightmare 2.0 - General purpose file editor
 *
 *  Copyright (C) 2009 Hextator,
 *  hectorofchad (AIM) hectatorofchad@sbcglobal.net (MSN)
 *
 *  Major thanks to Zahlman (AIM/MSN: zahlman@gmail.com) for optimization,
 *  organization and modularity improvements.
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
 *  <Description> This class contains the logic for interfacing
 *  between the user and the editing components
 */

package nightmare2;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
//import org.jdesktop.application.TaskMonitor;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
//import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
//import javax.swing.Timer;
import Model.Target_File;
import Model.Util;

public class View extends FrameView {
	private App app;

	private JMenuBar menuBar;

	private JMenuItem saveMenuItem;
	private JMenuItem saveAsMenuItem;
	private JMenuItem closeMenuItem;

	private JMenuItem findMenuItem;
	private JMenuItem allocateMenuItem;

	private JMenuItem aboutMenuItem;

	private JPanel mainPanel;
	private JButton openButton;
	private JButton selectButton;
	private JButton quitButton;
	private JCheckBox endiannessCheckbox;

	/* NOTE: This has been removed because there is no status bar.
	 * If one is added, this should all be refactored to be cleaner.
	private final Timer messageTimer;
	private final Timer busyIconTimer;
	private final Icon idleIcon;
	private final Icon[] busyIcons = new Icon[15];
	private int busyIconIndex = 0;
	*/

	private JDialog aboutBox;

	private static String oldINIpath = null;

	public View(App app) {
		super(app);
		this.app = app;

		initComponents();

		getFrame().setResizable(false);

		/* NOTE: This has been removed because there is no status bar.
		 * If one is added, this should all be refactored to be cleaner.
		//Status bar initialization -
		//message timeout, idle icon and busy animation, etc
		ResourceMap resourceMap = getResourceMap();
		int messageTimeout =
			resourceMap.getInteger("StatusBar.messageTimeout");
		messageTimer = new Timer(
			messageTimeout,
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {}
			}
		);
		messageTimer.setRepeats(false);
		int busyAnimationRate =
			resourceMap.getInteger("StatusBar.busyAnimationRate");
		for (int i = 0; i < busyIcons.length; i++) {
			busyIcons[i] =
				resourceMap
				.getIcon("StatusBar.busyIcons[" + i + "]");
		}
		busyIconTimer = new Timer(
			busyAnimationRate,
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					busyIconIndex =
						(busyIconIndex + 1)
						% busyIcons.length;
				}
			}
		);
		// NOTE: Reinstate this if a status bar is added
		//idleIcon = resourceMap.getIcon("StatusBar.idleIcon");

		//Connecting action tasks to status bar via TaskMonitor
		TaskMonitor taskMonitor =
			new TaskMonitor(getApplication().getContext());
		taskMonitor.addPropertyChangeListener(
			new java.beans.PropertyChangeListener() {
				public void propertyChange(
					java.beans.PropertyChangeEvent evt
				) {
					String propertyName =
						evt.getPropertyName();
					if ("started".equals(propertyName)) {
						if (!busyIconTimer.isRunning()) {
							busyIconIndex = 0;
							busyIconTimer.start();
						}
					} else if ("done".equals(propertyName)) {
						busyIconTimer.stop();
					} else if ("message".equals(propertyName)) {
						String text =
							(String)
							(evt.getNewValue());
						messageTimer.restart();
					} else if ("progress".equals(propertyName)) {
						int value = (Integer)(evt.getNewValue());
					}
				}
			}
		);
		*/
	}

	private void unload() {
		Target_File.closeFile();
		ModuleFrame.resetOpenModules();

		endiannessCheckbox.setEnabled(true);
		openButton.setEnabled(false);
		saveMenuItem.setEnabled(false);
		saveAsMenuItem.setEnabled(false);
		closeMenuItem.setEnabled(false);
		allocateMenuItem.setEnabled(false);
		findMenuItem.setEnabled(false);
	}

	private void load() {
		endiannessCheckbox.setEnabled(false);
		openButton.setEnabled(true);
		saveMenuItem.setEnabled(true);
		saveAsMenuItem.setEnabled(true);
		closeMenuItem.setEnabled(true);
		allocateMenuItem.setEnabled(true);
		findMenuItem.setEnabled(true);
	}

	@Action
	public void showAboutBox() {
		if (aboutBox == null) {
			JFrame mainFrame =
				App.getApplication().getMainFrame();
			aboutBox = new AboutBox(mainFrame);
			aboutBox.setLocationRelativeTo(mainFrame);
		}
		App.getApplication().show(aboutBox);
	}

	private void openModule() {
		ModuleFrame.openModule();
	}

	@Action
	public void openButtonExecute() {
		if (!openButton.isEnabled())
			return;
		try {
			openModule();
		} catch (Exception e) {
			//Common_Dialogs.showCatchErrorDialog(e);
		}
	}

	private void processINI(Scanner iniScanner) {
		String loadedString = iniScanner.nextLine();
		if (loadedString.equals("[Basis]"))
			oldINIpath = null;
		else
			ModuleFrame.addModule(new File(loadedString), false);
		while (iniScanner.hasNext())
			ModuleFrame.addModule(new File(iniScanner.nextLine()), false);
		iniScanner.close();
	}

	private void selectFile() {
		if (!Target_File.isSaved() && Target_File.isOpen())
			if (
				Common_Dialogs.showYesNoDialog(
					"Close the file?"
				)
				!= javax.swing.JOptionPane.YES_OPTION
			)
				return;
			else {
				try {
					Target_File.save(
						Common_Dialogs.showSaveFileDialog("file")
					);
					if (Target_File.isSaved()) writeINIfile();
				} catch (Exception e) {
					Common_Dialogs.showCatchErrorDialog(e);
				}
			}

		unload();
		File toOpen;
		String path = App.getInitialPath();
		if (path != null)
			toOpen = new File(path);
		else
			toOpen = Common_Dialogs.showOpenFileDialog("file");
		if (toOpen == null) return;
		Target_File.open(toOpen);
		if (Target_File.isOpen()) {
			load();
			App.setInitialPath(null);
			if (endiannessCheckbox.isSelected())
				Target_File.setBigEndian();
			else
				Target_File.setLittleEndian();
			Scanner iniScanner = null;
			try {
				oldINIpath = Target_File.iniPath();
				iniScanner = new Scanner(new File(oldINIpath));
				processINI(iniScanner);
				try {
					iniScanner.close();
				} catch (Exception e) {}
				ModuleFrame.start();
		} catch (Exception e) {
				// For debugging
				if (false) { }
			}
		}
	}

	@Action
	public void selectButtonExecute() {
		if (!selectButton.isEnabled())
			return;
		try {
			selectFile();
		} catch (Exception e) {
			Common_Dialogs.showCatchErrorDialog(e);
		}
	}

	private void writeINIfile() {
		FileWriter iniFile;
		Scanner oldINIfile = null;

		if (oldINIpath != null)
			try {
				(new File(oldINIpath)).delete();
			} catch (Exception e) {}

		oldINIpath = Target_File.iniPath();

		boolean write = true;
		try {
			oldINIfile = new Scanner(new File(oldINIpath));
			if (oldINIfile != null)
				if (oldINIfile.nextLine().equals("[Basis]"))
					write = false;
		} catch (Exception e) {}
		try {
			oldINIfile.close();
		} catch (Exception e) {}
		if (!write)
			return;

		try {
			iniFile = new FileWriter(new File(oldINIpath));
		} catch (Exception e) { return; }

		File[] openModules = ModuleFrame.getOpenModules();
		for (File currFile: openModules)
			try {
				iniFile.write(
					currFile.getPath() + Util.newline()
				);
			} catch (Exception e) {}

		try {
			iniFile.close();
		} catch (Exception e) {}
	}

	private void save(boolean saveAs) {
		try {
			if (!Target_File.isNamed() || saveAs)
				Target_File.save(
					Common_Dialogs.showSaveFileDialog("file")
				);
			else
				Target_File.save(new File(
					Target_File.fileName()
				));
			if (Target_File.isSaved()) writeINIfile();
		} catch (Exception e) {
			Common_Dialogs.showCatchErrorDialog(e);
		}
	}

	@Action
	public void saveMenuItemExecute() {
		if (!saveMenuItem.isEnabled())
			return;
		try {
			save(false);
		} catch (Exception e) {
			Common_Dialogs.showCatchErrorDialog(e);
		}
	}

	@Action
	public void saveAsMenuItemExecute() {
		if (!saveAsMenuItem.isEnabled())
			return;
		try {
			save(true);
		} catch (Exception e) {
			Common_Dialogs.showCatchErrorDialog(e);
		}
	}

	private void close() {
		if (
			JOptionPane.showConfirmDialog(
				null,
				"Are you sure you want to close "
				+ "the file?",
				"Confirm",
				javax.swing.JOptionPane.YES_NO_OPTION
			)
			== javax.swing.JOptionPane.YES_OPTION
		) {
			if (!Target_File.isSaved())
				Target_File.save(
					Common_Dialogs.showSaveFileDialog("file")
				);
			if (Target_File.isSaved()) writeINIfile();
			unload();
		}
	}

	@Action
	public void closeMenuItemExecute() {
		if (!closeMenuItem.isEnabled())
			return;
		try {
			close();
		} catch (Exception e) {
			Common_Dialogs.showCatchErrorDialog(e);
		}
	}

	public void allocate() throws Exception {
		String result = JOptionPane.showInputDialog(
			null,
			"Enter the numer of bytes to allocate:",
			0
		);

		int memToAllocate = Util.parseInt(result);
		final int megabyte = 0x00100000;
		if (memToAllocate > megabyte)
			throw new IllegalArgumentException(
				"Can't allocate more than a megabyte at a time."
			);

		int addressToReport = Target_File.size();
		memToAllocate = Target_File.expand(memToAllocate);
		Common_Dialogs.showGenericInformationDialog(
			String.format("0x%08X", memToAllocate)
			+ " bytes allocated to "
			+ String.format("0x%08X", addressToReport)
		);
	}

	@Action
	public void allocateMenuItemExecute() {
		if (!allocateMenuItem.isEnabled() || !Target_File.isOpen())
			return;
		try {
			allocate();
		} catch (Exception e) {
			//Common_Dialogs.showCatchErrorDialog(e);
		}
	}

	public void find() throws Exception {
		String result = JOptionPane.showInputDialog(
			null,
			"Enter value of the byte to search for strings of:",
			0
		);

		byte byteToFind = (byte)Util.parseInt(result);

		result = JOptionPane.showInputDialog(
			null,
			"Enter the number of bytes needed:",
			0
		);

		int memToFind = Util.parseInt(result);

		int resultAddress = Util.findByteString(
			Target_File.getData(), byteToFind, memToFind
		);
		if (resultAddress == -1)
			Common_Dialogs.showGenericErrorDialog(
				"The string was not found."
			);
		else
			Common_Dialogs.showGenericInformationDialog(
				"String found at "
				+ String.format("0x%08X", resultAddress)
			);
	}

	@Action
	public void findMenuItemExecute() {
		if (!findMenuItem.isEnabled())
			return;
		try {
			find();
		} catch (Exception e) {
			//Common_Dialogs.showCatchErrorDialog(e);
		}
	}

	// Helpers for initialization.
	// Hextator sez: Thanks, Zahlman.

	private JMenu createMenu(
		ResourceMap resourceMap,
		String name, char mnemonic,
		JMenuItem[] items
	) {
		JMenu menu = new JMenu();
		menu.setMnemonic(mnemonic);
		menu.setText(resourceMap.getString(name + ".text"));
		menu.setName(name);

		for (JMenuItem item: items) {
			menu.add(item);
		}

		return menu;
	}

	private JMenuItem createMenuItem(
		ActionMap actionMap, ResourceMap resourceMap,
		String name, KeyStroke accelerator,
		Integer mnemonic, boolean enabled
	) {
		JMenuItem item = new JMenuItem();
		item.setAction(actionMap.get(name + "Execute"));
		if (accelerator != null) { item.setAccelerator(accelerator); }
		if (mnemonic != null) { item.setMnemonic(mnemonic); }
		item.setText(resourceMap.getString(name + ".text"));
		item.setToolTipText(resourceMap.getString(name + ".toolTipText"));
		item.setName(name);
		item.setEnabled(enabled);
		return item;
	}

	private JButton createButton(
		ActionMap actionMap, ResourceMap resourceMap,
		String name, boolean tooltip, boolean enabled
	) {
		JButton button = new JButton();
		button.setAction(actionMap.get(name + "Execute"));
		button.setText(resourceMap.getString(name + ".text"));
		button.setName(name);
		if (tooltip) button.setToolTipText(resourceMap.getString(name + ".toolTipTest"));
		button.setEnabled(enabled);
		return button;
	}

	private JCheckBox createCheckBox(
		ResourceMap resourceMap,
		String name, Integer mnemonic,
		boolean tooltip, boolean enabled
	) {
		JCheckBox checkbox = new JCheckBox();
		if (mnemonic != null) checkbox.setMnemonic(mnemonic);
		checkbox.setText(resourceMap.getString(name + ".text"));
		checkbox.setName(name);
		if (tooltip) checkbox.setToolTipText(resourceMap.getString(name+ ".toolTipText"));
		checkbox.setEnabled(enabled);
		return checkbox;
	}

	private KeyStroke controlKeyStroke(int key) {
		return KeyStroke.getKeyStroke(key, InputEvent.CTRL_MASK);
	}

	/* Keeping this around just in case
	private KeyStroke plainKeyStroke(int key) {
		return KeyStroke.getKeyStroke(key, 0);
	}
	*/

        private void initComponents() {
		// Get ActionMap and ResourceMap that will be used
		// for most of the rest of the initialization
		ActionMap actionMap = Application.getInstance(
			nightmare2.App.class
		).getContext().getActionMap(View.class, this);
		ResourceMap resourceMap = Application.getInstance(
			nightmare2.App.class
		).getContext().getResourceMap(View.class);

		// File menu
		saveMenuItem = createMenuItem(
			actionMap, resourceMap,
			"saveMenuItem", controlKeyStroke(KeyEvent.VK_S),
			null, false
		);
		saveAsMenuItem = createMenuItem(
			actionMap, resourceMap,
			"saveAsMenuItem", null,
			KeyEvent.VK_S, false
		);
		closeMenuItem = createMenuItem(
			actionMap, resourceMap,
			"closeMenuItem", controlKeyStroke(KeyEvent.VK_W),
			KeyEvent.VK_C, false
		);

		// Tools menu
		allocateMenuItem = createMenuItem(
			actionMap, resourceMap,
			"allocateMenuItem", null,
			KeyEvent.VK_A, false
		);
		findMenuItem = createMenuItem(
			actionMap, resourceMap,
			"findMenuItem", controlKeyStroke(KeyEvent.VK_F),
			KeyEvent.VK_F, false
		);

		// Help menu
		aboutMenuItem = new JMenuItem();
		aboutMenuItem.setAction(actionMap.get("showAboutBox"));
		aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(
			java.awt.event.KeyEvent.VK_F1,
			java.awt.event.InputEvent.SHIFT_MASK
		));
		aboutMenuItem.setName("aboutMenuItem");

		// Menu bar initialization
		menuBar = menuBar = new JMenuBar();
		menuBar.setName("menuBar");
		for (JMenu menu: new JMenu[] {
			createMenu(resourceMap, "fileMenu", 'f', new JMenuItem[] {
				saveMenuItem, saveAsMenuItem, closeMenuItem
			}),
			createMenu(resourceMap, "toolsMenu", 't', new JMenuItem[] {
				findMenuItem, allocateMenuItem
			}),
			createMenu(resourceMap, "helpMenu", 'h', new JMenuItem[] {
				aboutMenuItem
			})
		}) {
			menuBar.add(menu);
		}
		menuBar.setName("menuBar");
		setMenuBar(menuBar);

		// Center component initialization
		mainPanel = new JPanel();

		openButton = createButton(actionMap, resourceMap, "openButton", false, false);
		selectButton = createButton(actionMap, resourceMap, "selectButton", true, true);

		quitButton = new JButton();
		quitButton.setAction(actionMap.get("quit"));
		quitButton.setName("quitButton");

		endiannessCheckbox = createCheckBox(
			resourceMap,
			"endiannessCheckbox", KeyEvent.VK_E,
			true, true
		);

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(
			mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(mainPanelLayout.createSequentialGroup()
				.addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(mainPanelLayout.createSequentialGroup()
						.addGap(73, 73, 73)
						.addComponent(quitButton))
					.addGroup(mainPanelLayout.createSequentialGroup()
						.addGap(20, 20, 20)
						.addComponent(endiannessCheckbox)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(selectButton))
					.addGroup(mainPanelLayout.createSequentialGroup()
						.addGap(52, 52, 52)
						.addComponent(openButton)))
				.addContainerGap(25, Short.MAX_VALUE))
		);
		mainPanelLayout.setVerticalGroup(
			mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(endiannessCheckbox)
					.addComponent(selectButton))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(openButton)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
				.addComponent(quitButton)
				.addContainerGap())
		);

		mainPanel.setMaximumSize(null);
		mainPanel.setName("mainPanel");
		mainPanel.setPreferredSize(new java.awt.Dimension(203, 114));

		setComponent(mainPanel);
        }
}
