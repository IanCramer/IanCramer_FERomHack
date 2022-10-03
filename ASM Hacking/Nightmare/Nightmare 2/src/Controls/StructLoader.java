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

import javax.swing.JPanel;
import Model.Module;
import Model.Module.EntryComponent;
import Model.Target_File;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import nightmare2.ModuleFrame;
import nightmare2.StructPane;

public class StructLoader extends JPanel implements DependentControl {
	private StructPane view;
	private Module module;
	private EntryComponent currComponent;

	private HexEditor editor;

	public StructLoader(
		final StructPane view, final Module module,
		final EntryComponent currComponent
	) {
		this.view = view;
		this.module = module;
		this.currComponent = currComponent;

		setLayout(new BorderLayout());

		editor = new HexEditor(view, module, currComponent);
		final JButton editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				int baseAddress = (Integer)editor.getValue();
				if (baseAddress == 0) return;
				baseAddress -= module.getHardwareOffset();
				if (baseAddress < 0 || baseAddress >= Target_File.size())
					return;
				ModuleFrame.newStruct(
					new File(currComponent.getAssociatedListPath()),
					baseAddress
				);
			}
		});
		add(editor, BorderLayout.EAST);
		add(editButton, BorderLayout.WEST);
	}

	public void update() { editor.update(); }
}
