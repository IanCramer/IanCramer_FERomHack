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
 *  <Description> Wrapper for the file to be edited by modules.
 *  Cannot be instantiated.
 */

package Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;

public class Target_File {
	public static enum ByteSex {
		LITTLE,
		BIG
	}

	public static enum AccessType {
		BYTE,
		HALF,
		WORD,
		BIT
	}

	class AlignmentException extends RuntimeException {}

	private static boolean open = false;
	private static boolean named = false;
	private static boolean saved = true;
	private static String fileName = "";
	private static ByteSex endianness = ByteSex.LITTLE;

	private static byte[] data;
	private static boolean originalLittleChecksumValid;
	private static boolean originalBigChecksumValid;
	private static int originalChecksum;

	private Target_File() {}

	public static boolean isOpen() { return open; }

	private static int checksum(byte[] data) {
		CRC32 crc32 = new CRC32();
		crc32.update(data);
		return (int)crc32.getValue();
	}

	public static int checksum() {
		return checksum(data);
	}

	public static String iniPath() {
		int checksum = checksum();
		return new File(Target_File.fileName).getParent()
			+ File.separator
			+ String.format("%08X.ini", checksum);
	}

	public static int size() { return data.length; }

	public static boolean isNamed() { return named; }

	public static boolean isSaved() { return saved; }

	public static String fileName() { return fileName; }

	public static ByteSex endianness() { return endianness; }

	public static void setLittleEndian() { endianness = ByteSex.LITTLE; }

	public static void setBigEndian() { endianness = ByteSex.BIG; }

	private static void verifyOpenFile() {
		if (!open) throw new RuntimeException("no file open");
	}

	public static AccessType calculateAccessType(
		int significand, boolean bitAddress
	) {
		int shiftAmt = 0;
		if (bitAddress) shiftAmt = 3;
		final int byteChk = 1 << shiftAmt;
		final int halfChk = 2 << shiftAmt;
		final int wordChk = 4 << shiftAmt;
		if (
			significand % wordChk == 0
		)
			return AccessType.WORD;
		else if (
			significand % halfChk == 0
		)
			return AccessType.HALF;
		else if (
			significand % byteChk == 0
		)
			return AccessType.BYTE;
		else
			return AccessType.BIT;
	}

	// The following six functions are for performing variable endian
	// data accesses on the file data; accesses supported are
	// 8, 16 and 32 bit read and write
	// All are tested and working

	public static byte getByte(int address) {
		verifyOpenFile();
		return data[address];
	}

	public static short getShort(int address) {
		verifyOpenFile();
		if (address % 2 != 0)
			throw (new Target_File()).new AlignmentException();
		short tempShort;
		if (endianness == ByteSex.LITTLE) {
			tempShort = (short) ((data[address + 1] & 0xFF) << 0x8);
			tempShort |= data[address] & 0xFF;
		}
		else {
			tempShort = (short) (data[address + 1] & 0xFF);
			tempShort |= (data[address] & 0xFF) << 0x8;
		}
		return tempShort;
	}

	public static int getInt(int address) {
		verifyOpenFile();
		if (address % 4 != 0)
			throw (new Target_File()).new AlignmentException();
		int tempInt;
		if (endianness == ByteSex.LITTLE) {
			tempInt = (data[address + 3] & 0xFF) << 0x18;
			tempInt |= (data[address + 2] & 0xFF) << 0x10;
			tempInt |= (data[address + 1] & 0xFF) << 0x8;
			tempInt |= data[address] & 0xFF;
		}
		else {
			tempInt = data[address + 3] & 0xFF;
			tempInt |= (data[address + 2] & 0xFF) << 0x8;
			tempInt |= (data[address + 1] & 0xFF) << 0x10;
			tempInt |= (data[address] & 0xFF) << 0x18;
		}
		return tempInt;
	}

	public static void putByte(int address, byte input) {
		verifyOpenFile();
		data[address] = input;
		saved = false;
	}

	public static void putShort(int address, short input) {
		verifyOpenFile();
		if (address % 2 != 0)
			throw (new Target_File()).new AlignmentException();
		if (endianness == ByteSex.LITTLE) {
			data[address + 1] = (byte) ((int) input >> 0x8);
			data[address] = (byte) ((int) input & 0xFF);
		}
		else {
			data[address] = (byte) ((int) input >> 0x8);
			data[address + 1] = (byte) ((int) input & 0xFF);
		}
		saved = false;
	}

	public static void putInt(int address, int input) {
		verifyOpenFile();
		if (address % 4 != 0)
			throw (new Target_File()).new AlignmentException();
		if (endianness == ByteSex.LITTLE) {
			data[address + 3] = (byte) (input >> 0x18);
			data[address + 2] =
				(byte) ((input >> 0x10) & 0xFF);
			data[address + 1] =
				(byte) ((input >> 0x8) & 0xFF);
			data[address] = (byte) (input & 0xFF);
		}
		else {
			data[address] = (byte) (input >> 0x18);
			data[address + 1] =
				(byte) ((input >> 0x10) & 0xFF);
			data[address + 2] =
				(byte) ((input >> 0x8) & 0xFF);
			data[address + 3] = (byte) (input & 0xFF);
		}
		saved = false;
	}

	// These two functions handle bit array reading and writing
	// Both are tested and working

	public static int getBits(
		int address, int offset, int length
	) {
		verifyOpenFile();
		if (length > 32 || length <= 0)
			throw new IllegalArgumentException(
				"invalid length"
			);

		int output = 0;
		while (length > 0) {
			if (offset == 8)
				address++;
			offset %= 8;
			output <<= 1;
			output |= (data[address] & (1 << (7 - offset)))
				>> (7 - offset);
			offset++;
			length--;
		}
		return output;
	}

	public static void putBits(
		int address, int offset, int length, int input
	) {
		verifyOpenFile();
		if (length > 32 || length <= 0)
			throw new IllegalArgumentException(
				"invalid length"
			);

		int mask = 1 << (length - 1);
		while (length > 0) {
			if (offset == 8)
				address++;
			offset %= 8;
			data[address] = (byte)(
				(data[address] & ~(1 << (7 - offset)))
				| (((input & mask) >> (length - 1)) << (7 - offset))
			);
			saved = false;
			offset++;
			mask >>= 1;
			length--;
		}
	}

	public static byte[] getData() {
		verifyOpenFile();
		return data;
	}

	public static boolean isValid(int checksum) {
		verifyOpenFile();
		if (
			originalLittleChecksumValid
			&& endianness == ByteSex.LITTLE
		)
			return true;
		else if (
			originalBigChecksumValid
			&& endianness == ByteSex.BIG
		)
			return true;
		else
			return originalChecksum == checksum ? true : false;
	}

	public static String pullString(byte source[], int start, int end) {
		verifyOpenFile();
		String tempString = "";
		try {
			tempString = new String(java.util.Arrays.copyOfRange(
				source, start, end
			), "ASCII");
		} catch (UnsupportedEncodingException e) {}
		return tempString;
	}

	public static void putString(byte dest[], int start, String input) {
		verifyOpenFile();
		byte inputArray[] = new byte[input.length()];
		for (int i = 0; i < inputArray.length; i++)
			inputArray[i] = (byte) input.charAt(i);
		System.arraycopy(inputArray, 0, data, start, inputArray.length);
		if (dest == data)
			saved = false;
	}

	public static void open(File input) {
		// Either the file fails to load and nothing is there to save,
		// or the file loads successfully and thus has no changes
		// to it
		saved = true;

		// Sanity check
		if (input == null || !input.exists()) {
			open = false;
			throw new IllegalArgumentException("invalid file");
		}

		// Actually read the data
		FileInputStream inputFile;
		try {
			inputFile = new FileInputStream(input);
			data = new byte[(int)input.length()];
			inputFile.read(data);
		} catch (IOException e) {
			open = false;
			throw new IllegalArgumentException("stream error");
		}
		open = true;
		fileName = input.getPath();
		try {
			inputFile.close();
		} catch (IOException e) {}

		// Data should be a multiple of 4 bytes
		expand(0);

		// Verify checksums
		originalLittleChecksumValid = false;
		originalBigChecksumValid = false;
		originalChecksum = checksum();
		int modifiedChecksum = checksum(
			java.util.Arrays.copyOf(data, data.length - 4)
		);
		endianness = ByteSex.LITTLE;
		int originalLittleChecksum = getInt(data.length - 4);
		if (originalLittleChecksum == modifiedChecksum)
			originalLittleChecksumValid = true;
		endianness = ByteSex.BIG;
		int originalBigChecksum = getInt(data.length - 4);
		if (originalBigChecksum == modifiedChecksum)
			originalBigChecksumValid = true;
		endianness = ByteSex.LITTLE;
		if (originalLittleChecksumValid || originalBigChecksumValid) {
			expand(-4);
		}
	}

	public static void save(File where) {
		verifyOpenFile();

		// Get the checksum of the data
		int checksum = checksum();
		// Make room for the checksum
		expand(4);
		// Append checksum onto end of file for validation purposes
		putInt(data.length - 4, checksum);

		// Sanity check
		if (where == null || !where.exists()) {
			// Rip off checksum so further editing without reopening
			// doesn't generate a checksum chain
			expand(-4);
			return;
		}

		// Actually write the data
		FileOutputStream outputFile;
		try {
			outputFile = new FileOutputStream(where);
			outputFile.write(data);
		} catch (IOException e) {
			// Rip off checksum so further editing without reopening
			// doesn't generate a checksum chain
			expand(-4);
			throw new IllegalArgumentException("stream error");
		}
		saved = true;
		named = true;
		fileName = where.getPath();
		try {
			outputFile.close();
		} catch (IOException e) {}

		// Rip off checksum so further editing without reopening
		// doesn't generate a checksum chain
		expand(-4);
	}

	public static int expand(int amount) {
		verifyOpenFile();
		int originalSize = data.length;
		int size = originalSize + amount + 3;
		size >>= 2;
		size <<= 2;
		data = java.util.Arrays.copyOf(data, size);
		return size - originalSize;
	}

	public static int append(byte[] input) {
		verifyOpenFile();
		int address = data.length;
		int tempInt = expand(input.length);
		System.arraycopy(
			input, 0,
			data, data.length - tempInt,
			input.length
		);
		return address;
	}

	public static int writeToFile(byte[] input) {
		verifyOpenFile();
		int address = data.length;
		append(input);
		saved = false;
		return address;
	}

	public static void overwriteFile(byte input[], int address) {
		verifyOpenFile();
		System.arraycopy(input, 0, data, address, input.length);
		saved = false;
	}

	public static int writeAndRepoint(
		byte input[], int pointerAddress
	) {
		verifyOpenFile();
		int address = writeToFile(input);
		putInt(pointerAddress, address);
		address = pointerAddress;
		return address;
	}

	public static void findAndReplace(int oldVal, int newVal) {
		verifyOpenFile();
		for (int i = 0; i < data.length; i += 4)
			if (getInt(i) == oldVal)
				putInt(i, newVal);

		saved = false;
	}

	public static void closeFile() {
		open = false;
		data = null;
		named = false;
		saved = true;
		fileName = "";
		originalLittleChecksumValid = false;
		originalBigChecksumValid = false;
		originalChecksum = 0;
	}
}
