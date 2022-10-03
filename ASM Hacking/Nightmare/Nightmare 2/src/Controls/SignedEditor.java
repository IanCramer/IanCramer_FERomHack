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
 *  <Description> Modified JSpinner for letting StructPanes communicate
 *  with Modules
 */

package Controls;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import Model.Module;
import Model.Module.EntryComponent;
import Model.Target_File;
import Model.Target_File.AccessType;
import nightmare2.StructPane;

public class SignedEditor extends JSpinner implements DependentControl {
	private StructPane view;
	private Module module;
	private EntryComponent currComponent;

	public SignedEditor(
		final StructPane view, final Module module,
		final EntryComponent currComponent
	) {
		this.view = view;
		this.module = module;
		this.currComponent = currComponent;

		setModel();
		setEditor();
		addChangeListener (new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (!view.selectionInBounds())
					return;

				final int address = currComponent.getAddress(view.getSelectedIndex());
				final AccessType accessType = currComponent.getAccessType(view.getSelectedIndex());
				try {
					switch (accessType) {
						case BYTE:
						Target_File.putByte(address, ((Integer)getValue()).byteValue());
						break;

						case HALF:
						Target_File.putShort(address, ((Integer)getValue()).shortValue());
						break;

						case WORD:
						Target_File.putInt(address, (Integer)getValue());
						break;

						case BIT:
						int bitOffset = currComponent.getNumBitsOffset();
						int bits = currComponent.getBitCount();
						Target_File.putBits(address, bitOffset % 8, bits, (Integer)getValue());
						break;

						default:
						break;
					}
					update();
				} catch (Exception e) {}
			}
		});
	}

	protected final EntryComponent getComponent() { return currComponent; }

	protected void setModel() {
		final SpinnerModel model = new WrappingNumberModel(
			0x00,
			-(1 << (currComponent.getBitCount() - 1)),
			(1 << (currComponent.getBitCount() - 1)) - 1,
			1
		);
		setModel(model);
	}

	protected void setEditor() {
		setEditor(new JSpinner.NumberEditor(this, "#"));
	}

	public void update() {
		final int address = currComponent.getAddress(view.getSelectedIndex());
		final AccessType accessType = currComponent.getAccessType(view.getSelectedIndex());
		try {
			int val = 0;
			if (view.selectionInBounds()) { switch (accessType) {
				case BYTE:
				val = (int)Target_File.getByte(address);
				break;

				case HALF:
				val = (int)Target_File.getShort(address);
				break;

				case WORD:
				val = Target_File.getInt(address);
				break;

				case BIT:
				int bitOffset = currComponent.getNumBitsOffset();
				int bits = currComponent.getBitCount();
				int pivotVal = 1 << bits;
				val = Target_File.getBits(address, bitOffset % 8, bits);
				if (val >= (pivotVal >> 1)) val -= pivotVal;
				setValue(val);
				break;

				default:
				break;
			}}
			setValue(val);
		} catch (Exception e) {}
	}
}
