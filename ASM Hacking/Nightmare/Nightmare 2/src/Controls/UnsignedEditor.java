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

import Model.Module;
import Model.Module.EntryComponent;
import nightmare2.StructPane;

public class UnsignedEditor extends SignedEditor implements DependentControl {
	public UnsignedEditor(
		final StructPane view, final Module module,
		final EntryComponent currComponent
	) { super(view, module, currComponent); }

	@Override
	protected void setEditor() {
		setEditor(new UnsignedNumberEditor(
			this,
			getComponent().getBitCount()
		));
	}
}
