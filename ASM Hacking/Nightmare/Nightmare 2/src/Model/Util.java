/*
 *  Nightmare 2.0 - General purpose file editor
 *
 *  From: FE Editor - GBA Fire Emblem (U) ROM editor
 *
 *  Copyright (C) 2008-2009 Hextator,
 *  hectorofchad (AIM) hectatorofchad@sbcglobal.net (MSN)
 *
 *  Major thanks to Zahlman (AIM/MSN: zahlman@gmail.com) for optimization,
 *  organization and modularity improvements.
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
 *  <Description> Unclassified utility functions are held in this class
 */

package Model;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

public class Util {
	public static String newline() {
		return System.getProperty("line.separator");
	}

	public static int parseInt(String input) {
		input = input.trim();
		int base = 10;
		if (input.indexOf("0x") == 0) {
			base = 16;
			input = input.substring(2);
		}

		return Integer.parseInt(input, base);
	}

	// Some routines for repacking arrays. Sadly, templates cannot be applied here.
	// I tried. Many approaches. -Zahlman
	// Streams won't work here, either, because we want to interpret the data
	// as little-endian.

	public static byte[] bytesToBytes(byte[]... arrays) {
		int size = 0;
		for (byte[] array: arrays) { size += array.length; }
		byte[] result = new byte[size];

		int position = 0;
		for (byte[] array: arrays) {
			System.arraycopy(array, 0, result, position, array.length);
			position += array.length;
		}

		return result;
	}

	public static byte[] shortsToBytes(short[]... arrays) {
		int size = 0;
		for (short[] array: arrays) { size += array.length; }
		byte[] result = new byte[size * 2];

		int position = 0;
		for (short[] array: arrays) {
			for (short value: array) {
				result[position++] = (byte)(value);
				result[position++] = (byte)(value >> 8);
			}
		}

		return result;
	}

	public static byte[] intsToBytes(int[]... arrays) {
		int size = 0;
		for (int[] array: arrays) { size += array.length; }
		byte[] result = new byte[size * 4];

		int position = 0;
		for (int[] array: arrays) {
			for (int value: array) {
				result[position++] = (byte)(value);
				result[position++] = (byte)(value >> 8);
				result[position++] = (byte)(value >> 16);
				result[position++] = (byte)(value >> 24);
			}
		}

		return result;
	}

	public static short[] bytesToShorts(byte[]... arrays) {
		int size = 0;
		for (byte[] array: arrays) { size += array.length; }
		short[] result = new short[(size + 1) / 2];

		int position = 0;
		int sub_position = 0;
		for (byte[] array: arrays) {
			for (byte value: array) {
				result[position] |= (value & 0xFF) << (sub_position * 8);
				sub_position++;
				if (sub_position == 2) { sub_position = 0; position++; }
			}
		}

		return result;
	}

	public static short[] shortsToShorts(short[]... arrays) {
		int size = 0;
		for (short[] array: arrays) { size += array.length; }
		short[] result = new short[size];

		int position = 0;
		for (short[] array: arrays) {
			System.arraycopy(array, 0, result, position, array.length);
			position += array.length;
		}

		return result;
	}

	public static short[] intsToShorts(int[]... arrays) {
		int size = 0;
		for (int[] array: arrays) { size += array.length; }
		short[] result = new short[size * 2];

		int position = 0;
		for (int[] array: arrays) {
			for (int value: array) {
				result[position++] = (short)(value);
				result[position++] = (short)(value >> 16);
			}
		}

		return result;
	}

	public static int[] bytesToInts(byte[]... arrays) {
		int size = 0;
		for (byte[] array: arrays) { size += array.length; }
		int[] result = new int[(size + 3) / 4];

		int position = 0;
		int sub_position = 0;
		for (byte[] array: arrays) {
			for (byte value: array) {
				result[position] |= (value & 0xFF) << (sub_position * 8);
				sub_position++;
				if (sub_position == 4) { sub_position = 0; position++; }
			}
		}

		return result;
	}

	public static int[] shortsToInts(short[]... arrays) {
		int size = 0;
		for (short[] array: arrays) { size += array.length; }
		int[] result = new int[(size + 1) / 2];

		int position = 0;
		int sub_position = 0;
		for (short[] array: arrays) {
			for (short value: array) {
				result[position] |= (value & 0xFFFF) << (sub_position * 16);
				sub_position++;
				if (sub_position == 2) { sub_position = 0; position++; }
			}
		}

		return result;
	}

	public static int[] intsToInts(int[]... arrays) {
		int size = 0;
		for (int[] array: arrays) { size += array.length; }
		int[] result = new int[size];

		int position = 0;
		for (int[] array: arrays) {
			System.arraycopy(array, 0, result, position, array.length);
			position += array.length;
		}

		return result;
	}

	public static int findByteString(byte[] data, byte byteToFind, int length) {
		if (data == null || length < 0 || length > data.length)
			return -1;
		if (length == 0)
			return 0;

		int index = -1;
		final int areaNeeded = length;
		int areaFound = 0;

Search:		for (int i = 0; i < data.length - areaNeeded; i += 4) {
			if (areaFound != 0) {
				i += areaFound - 1;
				i >>= 2;
				i <<= 2;
				if (i >= data.length)
					break;
			}
			areaFound = 0;
			if (data[i] == byteToFind)
				while (data[i + areaFound] == byteToFind) {
					areaFound++;
					if (areaFound >= areaNeeded) {
						index = i;
						break Search;
					}
				}
		}

		return index;
	}

	public static String condenseCharacterCodes(String input) {
		for (int i = 0; i < 32; i++)
			input = input.replace(
				String.format("\\u%04X", i),
				String.format("%c", (char)i)
			);
		return input;
	}

	public static String expandCharacterCodes(String input) {
		for (int i = 0; i < 32; i++)
			input = input.replace(
				String.format("%c", (char)i),
				String.format("\\u%04X", i)
			);
		return input;
	}

	public static byte[] stringToByteArray(String input, int byteCount) {
		if (input == null || byteCount <= 0)
			return null;

		input = input.trim();
		LinkedList<Byte> outputList = new LinkedList<Byte>();
		String tempString;
		int spaceIndex;
		int tempInt;
		while (input.length() > 0) {
			spaceIndex = input.indexOf(" ");
			if (spaceIndex != -1) {
				tempString = input.substring(0, spaceIndex);
				input = input.substring(spaceIndex + 1);
			}
			else {
				tempString = input;
				input = "";
			}
			try {
				tempInt = Integer.parseInt(tempString, 16);
				if (tempInt < 0 || tempInt > 255)
					return null;
				outputList.add(new Integer(tempInt).byteValue());
			} catch (Exception e) { return null; }
		}

		if (outputList.size() != byteCount)
			return null;
		byte output[] = new byte[outputList.size()];
		int i = 0;
		for (Byte currByte : outputList)
			output[i++] = currByte;
		return output;
	}

	public static String byteArrayToString(byte[] input) {
		if (input == null || input.length == 0)
			return null;

		String outputString = " ";
		for (int i = 0; i < input.length; i++)
			outputString += String.format("%02X ", input[i] & 0xFF);
		return outputString.substring(0, outputString.length() - 1);
	}

	// Appends numbers onto Strings that are reused in the input
	// Each group of repeated strings has its own "counter"
	public static void renameDuplicates(String[] input) {
		HashSet<String> dejaVu = new HashSet<String>();
		Hashtable<String, Integer> duplicates =
			new Hashtable<String, Integer>();
		for (String s: input) {
			if (dejaVu.contains(s)) {
				duplicates.put(s, 1);
			}
			else { dejaVu.add(s); }
		}
		for (int i = 0; i < input.length; i++) {
			if (duplicates.get(input[i]) != null) {
				int ID = duplicates.get(input[i]);
				duplicates.put(input[i], ID + 1);
				input[i] += " " + ID;
			}
		}
	}

	// Returns a long with a number of the low bits set specified
	public static long mask(int bits) {
		return (((long)1 << (long)bits) - 1);
	}

	public static void main(String[] args) {
		byte[] byte1 = new byte[] { 0x01 };
		byte[] byte2 = new byte[] { 0x02, 0x03 };
		byte[] byte3 = new byte[] { 0x04, 0x05, 0x06, 0x07 };

		short[] short1 = new short[] { 0x7F01 };
		short[] short2 = new short[] { 0x7E02, 0x7D03 };
		short[] short3 = new short[] { 0x7C04, 0x7B05, 0x7A06, 0x7907 };

		int[] int1 = new int[] { 0x7F000001 };
		int[] int2 = new int[] { 0x7E000002, 0x7D000003 };
		int[] int3 = new int[] { 0x7C000004, 0x7B000005, 0x7A000006, 0x79000007 };

		for (byte v: bytesToBytes(byte1, byte2, byte3)) {
			System.out.print(String.format("%02X ", v));
		}
		System.out.println();
		for (short v: bytesToShorts(byte1, byte2, byte3)) {
			System.out.print(String.format("%04X ", v));
		}
		System.out.println();
		for (int v: bytesToInts(byte1, byte2, byte3)) {
			System.out.print(String.format("%08X ", v));
		}
		System.out.println();
		for (byte v: shortsToBytes(short1, short2, short3)) {
			System.out.print(String.format("%02X ", v));
		}
		System.out.println();
		for (short v: shortsToShorts(short1, short2, short3)) {
			System.out.print(String.format("%04X ", v));
		}
		System.out.println();
		for (int v: shortsToInts(short1, short2, short3)) {
			System.out.print(String.format("%08X ", v));
		}
		System.out.println();
		for (byte v: intsToBytes(int1, int2, int3)) {
			System.out.print(String.format("%02X ", v));
		}
		System.out.println();
		for (short v: intsToShorts(int1, int2, int3)) {
			System.out.print(String.format("%04X ", v));
		}
		System.out.println();
		for (int v: intsToInts(int1, int2, int3)) {
			System.out.print(String.format("%08X ", v));
		}
	}
}
