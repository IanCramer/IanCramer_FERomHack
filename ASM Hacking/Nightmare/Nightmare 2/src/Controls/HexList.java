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

import Model.Module;
import Model.Module.EntryComponent;
import Model.Target_File.AccessType;
import nightmare2.StructPane;

public class HexList extends DecimalList implements DependentControl {
	public HexList(
		final StructPane view, final Module module,
		final EntryComponent currComponent
	) { super(view, module, currComponent); }

	@Override
	protected int leadingDigits() { return 2; }

	@Override
	protected int base() { return 16; }

	@Override
	protected String formatString(Long asLong, AccessType accessType) {
		String validString = "";
		switch (accessType) {
			case BYTE:
			validString = String.format("0x%02X", asLong);
			break;

			case HALF:
			validString = String.format("0x%04X", asLong);
			break;

			case WORD:
			validString = String.format("0x%08X", asLong);
			break;

			case BIT:
			validString = String.format("0x%X", asLong);
			break;

			default:
			break;
		}
		return validString;
	}
}
