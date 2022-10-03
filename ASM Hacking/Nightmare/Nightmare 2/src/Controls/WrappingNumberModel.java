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
 *  <Description> Modified SpinnerNumberModel that causes exceeding
 *  minimum and maximum value boundaries to loop the set value around from
 *  the maximum to the minimum and vice versa
 */

package Controls;

import javax.swing.SpinnerNumberModel;

public class WrappingNumberModel extends SpinnerNumberModel {
	public WrappingNumberModel(
		Number value,
		Comparable minimum, Comparable maximum, Number stepSize
	) {
		super(value, minimum, maximum, stepSize);
	}

	public WrappingNumberModel(
		int value, int minimum, int maximum, int stepSize
	) {
		super(
			new Integer(value),
			new Integer(minimum),
			new Integer(maximum),
			new Integer(stepSize)
		);
	}

	public WrappingNumberModel(
		double value, double minimum, double maximum, double stepSize
	) {
		super(
			new Double(value),
			new Double(minimum),
			new Double(maximum),
			new Double(stepSize)
		);
	}

	public WrappingNumberModel() {
		super(new Integer(0), null, null, new Integer(1));
	}

	@Override
	public Object getNextValue() {
		Object value = super.getNextValue();
		if (value == null)
			return getMinimum();
		else
			return value;
	}

	@Override
	public Object getPreviousValue() {
		Object value = super.getPreviousValue();
		if (value == null)
			return getMaximum();
		else
			return value;
	}
}
