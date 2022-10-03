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
 *  <Description> For having spinners function in unsigned base 10
 */

package Controls;

import javax.swing.text.DefaultFormatter;
import java.text.ParseException;

@SuppressWarnings({"WeakerAccess"})
public class UnsignedNumberFormatter extends DefaultFormatter {
	private int pivotVal;

	public UnsignedNumberFormatter(int bits) {
		if (bits > 32)
			throw new IllegalArgumentException(
				"UnsignedNumberFormatter only works with 32 " +
				"bits or less"
			);
		else if (bits <= 0)
			throw new IllegalArgumentException(
				"UnsignedNumberFormatter can't function " +
				"without a valid specification of bit length"
			);
		pivotVal = 1 << bits;
	}

	@Override
	public Object stringToValue(String string) throws ParseException {
		try {
			Object value = getFormattedTextField().getValue();
			Long temp = Long.valueOf(string);
			if (temp >= (pivotVal >> 1))
				temp -= pivotVal;
			if (value instanceof Byte)
				return new Integer(temp.byteValue());
			else if (value instanceof Short)
				return new Integer(temp.shortValue());
			else if (value instanceof Integer)
				return new Integer(temp.intValue());
			else if (value instanceof Long)
				return Long.valueOf(string);
			else
				throw new IllegalArgumentException(
					"UnsignedNumberFormatter only works with "
					+ "wrappers of primitive numeric types"
				);
		}
		catch (NumberFormatException nfe) {
			throw new ParseException(string, 0);
		}
	}

	@Override
	public String valueToString(Object value) {
		//Treat as unsigned
		long valToPrint = ((Number) value).longValue();
		if (valToPrint < 0)
			valToPrint += pivotVal;
		return Long.toString(valToPrint);
	}
}
