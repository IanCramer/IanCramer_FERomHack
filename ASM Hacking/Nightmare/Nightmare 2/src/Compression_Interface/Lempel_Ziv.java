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
 *  Contributions by others in this file
 *  - Nintenlord's NLZ GBA Advance application's LZ77 compression
 *    source code was ported
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
 *  <Description> This class supplies an interface for data processed by
 *  the algorithms for compression and decompression by Lempel and Ziv
 *  implemented for certain architectures
 */

package Compression_Interface;

import java.util.Arrays;
import java.util.Vector;
import Model.Util;

public class Lempel_Ziv {
	public static final int	BLOCK_SIZE = 0x00000008;
	public static final int READ_AHEAD_BUFFER_SIZE = 0x00000012;
	public static final int	SLIDING_WINDOW_SIZE = 0x00001000;

	// Decompresses data from a ROM or a byte array
	// Irrelevant argument should be null
	private static byte[] decompressionHelper(byte[] input) {
		if (input != null && input[0] != 0x10) { return null; }
		int size = (Util.bytesToInts(
			Arrays.copyOf(input, 4)
		)[0] >> 8) & 0xFFFFFF;

		int writeAddress = 0;
		int index = 4;
		byte[] output = new byte[size];

		int blockCount = 0;
		int isCompressed = 0;
		while (writeAddress < output.length) {
			if (blockCount == 0) {
				isCompressed = input[index++];
			} else {
				isCompressed <<= 1;
			}
			blockCount = (blockCount + 1) % BLOCK_SIZE;

			if ((isCompressed & 0x80) == 0) {
				byte tempByte = 0;
				tempByte = input[index++];
				output[writeAddress++] = tempByte;
			} else {
				int first = 0;
				first = input[index++] & 0xFF;
				int amountToCopy = 3 + (first >> 4);
				byte tempByte = 0;
				tempByte = input[index++];
				int copyOffset = ((first & 0x0F) << 8) | (tempByte & 0xFF);

				// System.arraycopy won't cut it here, because the copy region could
				// extend past the write point, in which case we want to keep repeating
				// data.
				for (int i = 0; i < amountToCopy; ++i, ++writeAddress) {
					output[writeAddress] = output[writeAddress - copyOffset - 1];
				}
			}
		}

		return output;
	}
	// decompressionHelper method; tested and working

	// Decompress a byte array representing GBA formatted LZ77 data
	public static byte[] decompress(byte[] input) {
		return decompressionHelper(input);
	}
	// decompress method; tested and working

	// Courtesy of NLZ GBA Advance, by Nintenlord
	// Returns info about the result of an LZ77 compression search
	// given the array to search through, the position to search from
	// and the size of array that is being compressed
	public static int[] search(byte[] input, int position, int length) {
		Vector<Integer> results = new Vector<Integer>();

		if (!(position < length))
			return new int[] {-1, 0};
		if ((position < 3) || ((length - position) < 3))
			return new int[] {0, 0};

		for (int i = 1; ((i < SLIDING_WINDOW_SIZE) && (i < position)); i++) {
			if (
				input[position - i - 1]
				== input[position]
			)
				results.add(i + 1);
		}
		if (results.size() == 0)
			return new int[] {0, 0};

		int amountOfBytes = 0;

		while (amountOfBytes < READ_AHEAD_BUFFER_SIZE) {
			amountOfBytes++;
			boolean searchComplete = false;
			for (int i = results.size() - 1; i >= 0; i--) {
				try {
					if (
						input[position + amountOfBytes]
						!=
						input[
							position - results.get(i)
							+ (
								amountOfBytes
								% results.get(i)
							)
						]
					) {
						if (results.size() > 1)
							results.removeElementAt(i);
						else
							searchComplete = true;
					}
				}
				catch (Exception error) {
					return new int[] {0, 0};
				}
			}
			if (searchComplete)
				break;
		}

		//Length of data is first, then position
		return new int[] { amountOfBytes, results.get(0) };
	}
	// search method; tested and working!

	// Courtesy of NLZ GBA Advance, by Nintenlord
	// Returns a byte array that contains data from a given byte array
	// compressed using the LZ77 compression algorithm
	public static byte[] compress(byte[] input) {
		int length = input.length;
		int position = 0;

		byte output[] = null;

		Vector<Byte> compressedData = new Vector<Byte>();
		compressedData.add((byte) 0x10);

		compressedData.add((byte) length);
		compressedData.add((byte) (length >> 8));
		compressedData.add((byte) (length >> 0x10));

		byte isCompressed = 0;
		int searchResult[] = null;
		Byte add = (byte) 0;
		Vector<Byte> tempVector = null;

		while (position < length) {
			isCompressed = 0;
			tempVector = new Vector<Byte>();

			for (int i = 0; i < BLOCK_SIZE; i++) {
				searchResult = search(
					input,
					position,
					length
				);

				if (searchResult[0] > 2) {
					add = (byte)(
						(((searchResult[0] - 3) & 0xF) << 4) + (((searchResult[1] - 1) >> 8) & 0xF)
					);
					tempVector.add(add);
					add = (byte)(
						(searchResult[1] - 1) & 0xFF
					);
					tempVector.add(add);
					position += searchResult[0];
					isCompressed |= (byte)(
						1 << (BLOCK_SIZE - (i + 1))
					);
				}
				else if (searchResult[0] >= 0) {
					tempVector.add(
						(byte) input[position++]
					);
				}
				else
					break;
			}

			compressedData.add(isCompressed);
			compressedData.addAll(tempVector);
		}

		while (compressedData.size() % 4 != 0)
			compressedData.add((byte) 0);

		output = new byte[compressedData.size()];
		int i = 0;
		for (Byte curr: compressedData)
			output[i++] = curr.byteValue();

		return output;
	}
	// compress method; tested and working!
}
