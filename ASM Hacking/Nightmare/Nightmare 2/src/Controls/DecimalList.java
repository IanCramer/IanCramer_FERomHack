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
 *  <Description> Modified JComboBox for letting StructPanes communicate
 *  with Modules
 */

package Controls;

import javax.swing.JComboBox;
import Model.Module;
import Model.Module.EntryComponent;
import Model.Target_File;
import Model.Target_File.AccessType;
import Model.Util;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;
import javax.swing.DefaultComboBoxModel;
import nightmare2.StructPane;

public class DecimalList extends JComboBox implements DependentControl {
	private StructPane view;
	private Module module;
	private EntryComponent currComponent;

	public DecimalList(
		final StructPane view, final Module module,
		final EntryComponent currComponent
	) {
		this.view = view;
		this.module = module;
		this.currComponent = currComponent;

		LinkedList<String> loadedStringsList = new LinkedList<String>();

		Scanner listScanner = null;
		try {
			listScanner = new Scanner(new File(
				currComponent.getAssociatedListPath()
			));
		} catch (Exception e) {}

		if (listScanner != null) {
			try {
				listScanner.nextLine();
			} catch (Exception e) {}
			String loadedString;
			Long asLong;
			String tempString = null;
			final AccessType accessType = currComponent.getAccessType(view.getSelectedIndex());
			while (listScanner.hasNext()) {
				try {
					loadedString = listScanner.nextLine();
				} catch (Exception e) { break; }
				asLong = null;
				try {
					tempString = loadedString .substring(
						2, loadedString.indexOf(" ")
					);
					asLong = Long.parseLong(
						tempString,
						16
					);
				} catch (Exception e) {}
				if (asLong != null) {
					loadedStringsList.add(
						formatString(asLong, accessType)
						+ loadedString.substring(loadedString.indexOf(" "))
					);
				}
			}

			try {
				listScanner.close();
			} catch (Exception e) {}
		}

		String loadedStrings[] =
			new String[loadedStringsList.size()];
		int i = 0;
		for (String currString : loadedStringsList)
			loadedStrings[i++] = currString;

		setModel(new DefaultComboBoxModel(loadedStrings));
		setEditable(true);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!view.selectionInBounds())
					return;

				final int address = currComponent.getAddress(view.getSelectedIndex());
				final AccessType accessType = currComponent.getAccessType(view.getSelectedIndex());
				boolean valid = true;
				if (getSelectedItem() == null)
					valid = false;
				String loadedString = (String)getSelectedItem();
				if (loadedString.length() == 0)
					valid = false;
				int numberOffset = leadingDigits();
				int base = base();
				int spaceIndex = loadedString.indexOf(" ");
				if (spaceIndex == -1)
					spaceIndex = loadedString.length();
				Long asLong = null;
				try {
					asLong = Long.parseLong(loadedString.substring(numberOffset, spaceIndex), base);
				} catch (Exception e) {}
				try {
					if (!valid || asLong == null) {
						throw new RuntimeException(
							"Failed to load"
						);
					}
					else {
						switch (accessType) {
							case BYTE:
							Target_File.putByte(address, asLong.byteValue());
							break;

							case HALF:
							Target_File.putShort(address, asLong.shortValue());
							break;

							case WORD:
							Target_File.putInt(address, asLong.intValue());
							break;

							case BIT:
							int bitOffset = currComponent.getNumBitsOffset();
							int bits = currComponent.getBitCount();
							Target_File.putBits(address, bitOffset % 8, bits, asLong.intValue());
							break;

							default:
							break;
						}
					}
				} catch (Exception e) {}
				update();
			}
		});
	}

	protected int leadingDigits() { return 0; }

	protected int base() { return 10; }

	protected String formatString(Long asLong, AccessType accessType) {
		return "" + asLong;
	}

	public void update() {
		final int address = currComponent.getAddress(view.getSelectedIndex());
		final AccessType accessType = currComponent.getAccessType(view.getSelectedIndex());
		try {
			String validString = "";
			Long asLong = null;
			if (view.selectionInBounds()) { switch (accessType) {
				case BYTE:
				asLong = Long.valueOf(Target_File.getByte(address) & 0xFF);
				break;

				case HALF:
				asLong = Long.valueOf(Target_File.getShort(address) & 0xFFFF);
				break;

				case WORD:
				asLong = Long.valueOf(Target_File.getInt(address) & 0xFFFFFFFF);
				break;

				case BIT:
				int bitOffset = currComponent.getNumBitsOffset();
				int bits = currComponent.getBitCount();
				long val = (long)Target_File.getBits(address, bitOffset % 8, bits);
				asLong = val & Util.mask(bits);
				break;

				default:
				break;
			}}
			else asLong = (long)0;
			validString = formatString(asLong, accessType);
			boolean found = false;
			for (int i = 0; i < getItemCount(); i++) {
				if (((String)getItemAt(i)).indexOf(validString) == 0) {
					validString = (String)getItemAt(i);
					found = true;
					break;
				}
			}
			if (!found)
				validString += String.format(
					" Default value for entry 0x%X",
					view.getSelectedIndex()
				);
			setSelectedItem(validString);
		} catch (Exception e) {}
	}
}
