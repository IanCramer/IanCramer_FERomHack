/*
 *  Nightmare 2.0 - General purpose file editor
 *
 *  Copyright (C) 2009 Hextator,
 *  hectorofchad (AIM) hectatorofchad@sbcglobal.net (MSN)
 * 
 *  Contributions by others in this file
 *  - Roedy Green of Canadian Mind Products and
 *  Thomas Fritsch provided/inspired this software.
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
 *  <Description> For having spinners function in base 16
 */

package Controls;

import javax.swing.text.DefaultFormatterFactory;

public class HexNumberFormatterFactory extends DefaultFormatterFactory {
	public HexNumberFormatterFactory(int width)  {
		super(new HexNumberFormatter(width));
	}
}
