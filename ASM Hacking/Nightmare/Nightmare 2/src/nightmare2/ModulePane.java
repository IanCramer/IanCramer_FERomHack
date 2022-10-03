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
 *  <Description> GUI for Module class
 */

package nightmare2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import Model.Util;

public class ModulePane extends StructPane {
	private JPanel entryListPanel = new JPanel();
	private JComboBox entryListComboBox = new JComboBox();

	private JButton expandButton = new JButton("Expand");

	public ModulePane(
		ModuleFrame view, JComponent heading, File input
	) {
		super(view, input, null);
		addToHeading(heading);

		finalEntryIndex = 1;
		int entryCount = module.getEntryCount();
		LinkedList<String> entryNamesList = new LinkedList<String>();
		Scanner entryListScanner = null;
		try {
			entryListScanner = new Scanner(module.getEntryList());
		} catch (Exception e) {}
		while (entryListScanner != null && entryListScanner.hasNext())
			entryNamesList.add(entryListScanner.nextLine());
		try {
			entryListScanner.close();
		} catch (Exception e) {}

		String[] entryNames = new String[entryNamesList.size()];
		int index = 0;
		for (String curr: entryNamesList)
			entryNames[index++] = curr;
		Util.renameDuplicates(entryNames);

		entryListComboBox.setEditable(false);
		for (int i = 0; i < entryCount; i++) {
			if (i < entryNames.length)
				entryListComboBox.addItem(entryNames[i]);
			else
				entryListComboBox.addItem(String.format("0x%02X", i));
		}
		if (entryCount != 1) {
			entryListComboBox.addItem("--- Select an Entry ---");
			selectedIndex = finalEntryIndex = entryListComboBox.getItemCount() - 1;
			entryListComboBox.setSelectedIndex(finalEntryIndex);
		}
		else {
			selectedIndex = 0;
			finalEntryIndex = 1;
			entryListComboBox.setSelectedIndex(selectedIndex);
		}
		entryListComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedIndex = entryListComboBox.getSelectedIndex();
				int count = entryListComboBox.getItemCount();
				if (
					selectedIndex != finalEntryIndex
					&& count == finalEntryIndex + 1
				) {
					int width = entryListComboBox.getSize().width;
					entryListComboBox.removeItemAt(finalEntryIndex);
					entryListComboBox.setMinimumSize(new Dimension(
						width,
						entryListComboBox.getSize().height
					));
				}
				updateAllEntryComponents();
			}
		});
		entryListPanel.add(entryListComboBox);

		expandButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				expandButton.setEnabled(false);
				int originalCount = module.getEntryCount();
				module.expand();
				int expandedCount = module.getEntryCount();
				selectedIndex++;
				try {
					entryListComboBox.removeItemAt(finalEntryIndex);
				} catch (Exception e) {}
				for (int i = originalCount; i < expandedCount; i++)
					entryListComboBox.addItem(String.format("0x%02X", i));
				entryListComboBox.addItem("--- Select an Entry ---");
				finalEntryIndex = entryListComboBox.getItemCount() - 1;
				entryListComboBox.setSelectedIndex(finalEntryIndex);
				updateAllEntryComponents();
			}
		});
		if (!module.canExpand())
			expandButton.setEnabled(false);
		entryListPanel.add(expandButton);
		addToHeading(entryListPanel);

		if (entryCount == 1) updateAllEntryComponents();
	}
}
