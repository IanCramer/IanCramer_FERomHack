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
 *  <Description> Class for modeling how a module should act if it is
 *  initialized by dereferencing an entry in another module
 */

package Model;

import java.io.File;

public class Struct extends Module {
	public Struct(File input, int baseAddress) {
		super(input);

		useBasePointer = false;
		expanded = false;

		this.baseAddress = baseAddress;
		basePointer = 0;
		defaultEntryCount = 1;
		expandedEntryCount = 1;
	}

	@Override
	public void expand() {
		throw new UnsupportedOperationException(
			"structs can't be expanded"
		);
	}

	@Override
	public boolean isExpanded() { return false; }

	@Override
	public boolean canExpand() { return false; }

	@Override
	public int getEntryCount() { return 1; }

	@Override
	public String toString() {
		String outputString = "\n";

		outputString += "Path: " + thisModule.getPath() + "\n";
		outputString += "Version: " + version + "\n";
		outputString += "Description: " + description + "\n";
		outputString +=
			"Entry size: "
			+ String.format("0x%X", entrySize)
			+ "\n"
		;

		return outputString;
	}
}
