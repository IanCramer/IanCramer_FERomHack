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
import javax.swing.JTextField;
import Model.Module;
import Model.Module.EntryComponent;
import Model.Target_File;
import Model.Util;
import nightmare2.StructPane;

public class TextField extends JTextField implements DependentControl {
	private StructPane view;
	private Module module;
	private EntryComponent currComponent;

	public TextField(
		final StructPane view, final Module module,
		final EntryComponent currComponent
	) {
		super("Enter text here                    ");
		this.view = view;
		this.module = module;
		this.currComponent = currComponent;

		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!view.selectionInBounds())
					return;

				String loadedString = getText();
				loadedString = module.encode(Util.condenseCharacterCodes(loadedString));
				final int address = currComponent.getAddress(view.getSelectedIndex());
				try {
					int byteCount = currComponent.getBitCount() >> 3;
					while(loadedString.length() < byteCount)
						loadedString += "\u0000";
					loadedString = loadedString.substring(0, byteCount);
					Target_File.putString(Target_File.getData(), address, loadedString);
				} catch (Exception e) {}
				update();
			}
		});
	}

	public void update() {
		final int address = currComponent.getAddress(view.getSelectedIndex());
		try {
			final int length = currComponent.getBitCount() >> 3;
			String loaded = Target_File.pullString(
				new byte[length], 0, length
			);
			if (view.selectionInBounds()) {
				loaded = Target_File.pullString(
					Target_File.getData(),
					address,
					address + length
				);
			}
			setText(Util.expandCharacterCodes(module.translate(loaded)));
			view.update();
		} catch (Exception e) {}
	}
}
