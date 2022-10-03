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
 *  <Description> Modified JTextField for letting StructPanes communicate
 *  with Modules
 */

package Controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JTextField;
import Model.Module;
import Model.Module.EntryComponent;
import Model.Target_File;
import Model.Util;
import nightmare2.StructPane;

public class HexField extends JTextField implements DependentControl {
	private StructPane view;
	private Module module;
	private EntryComponent currComponent;

	public HexField(
		final StructPane view, final Module module,
		final EntryComponent currComponent
	) {
		super("Enter hex info here                ");
		this.view = view;
		this.module = module;
		this.currComponent = currComponent;

		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!view.selectionInBounds())
					return;

				String loadedString = getText();
				byte input[] = Util.stringToByteArray(loadedString, currComponent.getBitCount() >> 3);
				final int address = currComponent.getAddress(view.getSelectedIndex());
				try {
					if (input == null) {
						setText(Util.byteArrayToString(java.util.Arrays.copyOfRange(
							Target_File.getData(),
							address,
							address + (currComponent.getBitCount() >> 3)
						)));
					}
					else
						Target_File.overwriteFile(input, address);
				} catch (Exception e) {}
				update();
			}
		});
	}

	public void update() {
		final int address = currComponent.getAddress(view.getSelectedIndex());
		try {
			byte[] loaded = new byte[currComponent.getBitCount() >> 3];
			if (view.selectionInBounds()) {
				loaded = Arrays.copyOfRange(
					Target_File.getData(),
					address,
					address + loaded.length
				);
			}
			setText(Util.byteArrayToString(loaded));
		} catch (Exception e) {}
	}
}
