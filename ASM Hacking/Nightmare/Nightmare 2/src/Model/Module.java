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
 *  <Description> Wrapper class for text files following both the old format of
 *  Nightmare modules as well as the new format unique to this remix of the
 *  application which was designed to enhance the usefulness of it.
 */

package Model;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;
import Model.Target_File.AccessType;

public class Module {
	public static enum EntryType {
		TEXT,
		HEXA,
		NEHU,
		NEDS,
		NEDU,
		NDHU,
		NDDU,
		STRUCT
	}

	public final class EntryComponent {
		private boolean validComponent = true;

		private String name;
		private int numBitsOffset;
		private int bitCount;
		private EntryType type;
		private File associatedList;
		private AccessType accessType;

		private int parseVerboseInt(String input) {
			String loadedString = String.format("%s", input);
			boolean negative = false;
			int base = 10;
			int shift = 3;
			int retVal = -1;
			if (input.charAt(0) == '-') {
				negative = true;
				input = input.substring(1);
			}
			if (input.indexOf("0x") == 0) {
				base = 16;
				input = input.substring(2);
			}
			if (input.indexOf("b") == input.length() - 1) {
				shift = 0;
				input = input.substring(0, input.length() - 1);
			}
			try {
				retVal = Integer.parseInt(input, base);
			} catch (Exception e) {
				addGeneralError(loadedString);
				validComponent = false;
			}
			if (validComponent) {
				retVal <<= shift;
				if (negative)
					retVal = 0 - retVal;
			}
			return retVal;
		}

		public boolean isValid() { return validComponent; }

		public void setName(String input) {
			if (input == null)
				validComponent = false;
			else
				name = input;
		}

		public void setNumBitsOffset(String input) {
			if (input == null)
				validComponent = false;
			else
				numBitsOffset = parseVerboseInt(input);
			if (numBitsOffset < 0) {
				if (!numBitsOffsetErrorDisplayed)
					throw new IllegalArgumentException(
						"You can't have negative " +
						"offsets!"
					);
				numBitsOffsetErrorDisplayed = true;
				validComponent = false;
			}
		}

		public void setBitCount(String input) {
			if (input == null)
				validComponent = false;
			else
				bitCount = parseVerboseInt(input);
			if (bitCount <= 0) {
				if (!bitCountErrorDisplayed)
					throw new IllegalArgumentException(
						"You can't have absent or " +
						"negative data lengths!"
					);
				bitCountErrorDisplayed = true;
				validComponent = false;
			}
		}

		public void setType(String input) {
			if (input == null)
				validComponent = false;
			else {
				try {
					type = EntryType.valueOf(input);
				} catch (Exception e) {
					addGeneralError(input);
					validComponent = false;
				}
			}
			if (
				(
					type == EntryType.TEXT
					|| type == EntryType.HEXA
				)
				&& (
					numBitsOffset % 8 != 0
					|| bitCount % 8 != 0
				)
			) {
				if (!arrayTypeErrorDisplayed)
					throw new IllegalArgumentException (
						"Array editing components " +
						"must be byte aligned and\n" +
						"must have a length in bytes!"
					);
				arrayTypeErrorDisplayed = true;
				validComponent = false;
			}
			if (
				type != EntryType.TEXT
				&& type != EntryType.HEXA
				&& bitCount > 32
			) {
				if (!tooManyBitsErrorDisplayed)
					throw new IllegalArgumentException(
						"This application does not " +
						"support bit arrays with\n" +
						"more than 32 bits"
					);
				tooManyBitsErrorDisplayed = true;
				validComponent = false;
			}
			if (
				type == EntryType.STRUCT
				&& getAccessType(0) != AccessType.WORD) {
				if (!structErrorDisplayed)
					throw new IllegalArgumentException(
						"This application does not " +
						"support struct pointers\n" +
						"not aligned by 32 bits"
					);
				structErrorDisplayed = true;
				validComponent = false;
			}
		}

		public void setAssociatedList(String input) {
			if (input == null)
				validComponent = false;
			else if (input.equals("NULL"))
				associatedList = null;
			else
				associatedList = new File(input);
		}

		public String getName() { return name; }

		public int getNumBitsOffset() { return numBitsOffset; }

		public int getAddress(int index) {
			return (numBitsOffset >> 3)
				+ (entrySize * index) + baseAddress;
		}

		public int getBitCount() { return bitCount; }

		private void calculateAccessType(int index) {
			final int significand = (getAddress(index) << 3) + (numBitsOffset % 8);
			final int bits = bitCount;
			final AccessType supposedType = Target_File.calculateAccessType(
				significand, true
			);
			if (supposedType != AccessType.BIT && bits == 8)
				accessType = AccessType.BYTE;
			else if ((
				supposedType == AccessType.HALF
				|| supposedType == AccessType.WORD)
				&& bits == 16
			)
				accessType = AccessType.HALF;
			else if (supposedType == AccessType.WORD && bits == 32)
				accessType = AccessType.WORD;
			else
				accessType = AccessType.BIT;
		}

		public AccessType getAccessType(int index) {
			if (accessType == null) calculateAccessType(index);
			return accessType;
		}

		public EntryType getEntryType() { return type; }

		public String getAssociatedListPath() {
			return associatedList.getPath();
		}
	}

	private final class TextTableEntry {
		private String find;
		private String replace;

		public int findLength = 0;
		public int replaceLength = 0;

		public TextTableEntry(String findString, String replaceString) {
			find = findString;
			if (find != null)
				findLength = find.length();
			replace = replaceString;
			if (replace != null)
				replaceLength = replace.length();
		}

		public String getFind() { return find; }

		public String getReplace() { return replace; }
	}

	private static boolean numBitsOffsetErrorDisplayed = false;
	private static boolean bitCountErrorDisplayed = false;
	private static boolean tooManyBitsErrorDisplayed = false;
	private static boolean arrayTypeErrorDisplayed = false;
	private static boolean structErrorDisplayed = false;

	protected File thisModule = null;

	private boolean valid = true;
	private int line = 1;
	private String errorString = "";

	protected boolean useBasePointer = false;
	protected boolean expanded = false;
	private int hardwareOffset = 0;

	private Integer checksum;
	private String IDstring;
	private Integer IDstringAddress;

	protected String version;
	protected String description;
	protected int baseAddress;
	protected int basePointer;
	protected int defaultEntryCount;
	protected int expandedEntryCount;
	protected int entrySize;
	private File entryList;
	private File textTable;

	private LinkedList<TextTableEntry> textTableEntryList =
		new LinkedList<TextTableEntry>();
	private TextTableEntry textTableTranslationEntries[];
	private TextTableEntry textTableEncodingEntries[];

	private LinkedList<EntryComponent> componentList =
		new LinkedList<EntryComponent>();

	private static final int partitionTableEntries(
		TextTableEntry[] array, int left, int right,
		boolean replaceElements
	) {
		TextTableEntry temp;
		int storeIndex = right;
		int leftComparator;
		int rightComparator;
		if (!replaceElements)
			rightComparator = array[left].findLength;
		else
			rightComparator = array[left].replaceLength;
		for (int i = right; i > left; i--) {
			if (!replaceElements)
				leftComparator = array[i].findLength;
			else
				leftComparator = array[i].replaceLength;

			if (leftComparator < rightComparator) {
				temp = array[i];
				array[i] = array[storeIndex];
				array[storeIndex--] = temp;
			}
		}
		temp = array[left];
		array[left] = array[storeIndex];
		array[storeIndex] = temp;
		return storeIndex;
	}

	private static final void quickSortTableEntries(
		TextTableEntry[] array, int left, int right,
		boolean replaceElements
	) {
		if (array == null)
			return;

		if (right <= left)
			return;

		int pivotNewIndex = partitionTableEntries(array, left, right, replaceElements);
		quickSortTableEntries(array, left, pivotNewIndex - 1, replaceElements);
		quickSortTableEntries(array, pivotNewIndex + 1, right, replaceElements);
	}

	private final String nextRelevantLine(Scanner input) {
		String tempString;
		try {
			tempString = input.findWithinHorizon(".", 1);
			while (
				(tempString == null && input.hasNext())
				|| tempString.equals("#")
			) {
				input.nextLine();
				line++;
				tempString = input.findWithinHorizon(".", 1);
			}
			if (!input.hasNext())
				throw new RuntimeException(
					"unexpected end of file"
				);
		} catch (Exception e) {
			throw new RuntimeException(
				"unknown processing error"
			);
		}
		return tempString + input.nextLine();
	}

	private final int getInt(Scanner input) { return getInt(input, false); }

	private final int getInt(Scanner input, Boolean silentlyFail) {
		String originalString = nextRelevantLine(input);
		int retVal = -1;
		String loadedString;
		boolean negative = false;
		try {
			loadedString = originalString;
			if (loadedString.charAt(0) == '-') {
				loadedString = loadedString
					.substring(1);
				negative = true;
			}
			int base = 10;
			if (loadedString.indexOf("0x") == 0) {
				base = 16;
				loadedString = loadedString.substring(2);
			}
			retVal = Integer
				.parseInt(loadedString, base);
		} catch (Exception e) {
			if (!silentlyFail || !input.hasNext()) {
				addGeneralError(originalString);
				valid = false;
				retVal = -1;
			}
			silentlyFail = true;
		}
		line++;
		if (valid && negative)
			retVal = 0 - retVal;
		return retVal;
	}

	private final void addToErrorString(String input, boolean general) {
		if (general) {
			errorString +=
				"Line " + line
				+ ": Error parsing \""
				+ input + "\"";
		}
		else
			errorString += input + "\n";
	}

	private final void addToErrorString(String input) {
		addToErrorString(input, false);
	}

	private final void addGeneralError(String input) {
		addToErrorString(input, true);
	}

	private final void processChecksum(Scanner theModule) {
		if (theModule.findWithinHorizon("#0x", 3) != null) {
			long checksumVal = -1;
			String checksumString = theModule.nextLine();
			try {
				checksumVal =
					Long.parseLong(checksumString, 16);
				if ((checksumVal & 0xFFFFFFFF00000000L) == 0)
					checksum = (int) checksumVal;
				else
					throw new Exception();
			} catch (Exception e) {
				throw new RuntimeException(
					"checksum line is invalid; no "
					+ "checksum is being used."
				);
			}

			if (
				!Target_File.isValid(checksum)
			) {
				valid = false;
				throw new RuntimeException(
					"File has invalid "
					+ "checksum for use "
					+ "with this module."
				);
			}
			line++;
		}
	}

	private final void processIDstring(Scanner theModule) {
		if (theModule.findWithinHorizon("#0x", 3) != null) {
			long IDaddress = -1;
			String loadedString = theModule.nextLine();
			try {
				IDaddress =
					Long.parseLong(loadedString, 16);
				if (
					IDaddress >= 0
					&& IDaddress < Target_File.size()
				)
					IDstringAddress = (int) IDaddress;
				else
					throw new Exception();
			} catch (Exception e) {
				line++;
				throw new RuntimeException(
					"ID string address line is invalid; "
					+ "no ID string is being used."
				);
			}
			line++;
			IDstring = theModule.nextLine();
			IDstring = IDstring.substring(1);
			IDstring = Util.condenseCharacterCodes(IDstring);
			if (
				IDstring.length() + IDstringAddress
				> Target_File.size()
			) {
				throw new RuntimeException(
					"ID string line is too long; "
					+ "no ID string is being used."
				);
			}
			if (
				!Target_File.pullString(
					Target_File.getData(),
					IDstringAddress,
					IDstringAddress + IDstring.length()
				).equals(IDstring)
			) {
				valid = false;
				throw new RuntimeException(
					"ID string line doesn't match "
					+ "ID string in file;\ncancelling "
					+ "module loading."
				);
			}
			line++;
		}
	}

	private final void processBasePointerExpected(Scanner theModule) {
		if (
			theModule.findWithinHorizon("#BASEPOINTER", 12) != null
		) {
			useBasePointer = true;
			theModule.nextLine();
			line++;
		}
	}

	private final void processTextTableEntries() {
		if (textTable == null)
			return;

		Scanner textTableScanner;
		try {
			textTableScanner = new Scanner(textTable);
		} catch (Exception e) {
			throw new RuntimeException(
				"Text table file failed to load;\n" +
				"text tokens will not be translated"
			);
		}

		String loadedString;
		int assignmentOperatorIndex;
		int count = 0;
		while (textTableScanner.hasNext()) {
			loadedString = textTableScanner.nextLine();
			assignmentOperatorIndex = loadedString.indexOf(" = ");
			if (assignmentOperatorIndex == -1)
				continue;
			textTableEntryList.add(new TextTableEntry(
				Util.condenseCharacterCodes(
					loadedString.substring(
						0, assignmentOperatorIndex
					)
				),
				Util.condenseCharacterCodes(
					loadedString.substring(
						assignmentOperatorIndex + 3
					)
				)
			));
			if (
				textTableEntryList.peekLast().findLength == 0
				|| textTableEntryList.peekLast().replaceLength == 0
			)
				textTableEntryList.removeLast();
			else
				count++;
		}

		try {
			textTableScanner.close();
		} catch (Exception e) {}

		textTableTranslationEntries = new TextTableEntry[count];
		textTableEncodingEntries = new TextTableEntry[count];
		int i = 0;
		for (TextTableEntry currEntry: textTableEntryList) {
			textTableTranslationEntries[i] = currEntry;
			textTableEncodingEntries[i++] = currEntry;
		}
		quickSortTableEntries(textTableTranslationEntries, 0, i - 1, true);
		quickSortTableEntries(textTableEncodingEntries, 0, i - 1, false);
	}

	private final void processHeader(Scanner theModule) {
		String loadedString = nextRelevantLine(theModule);
		if (loadedString == null) {
			valid = false;
			return;
		}
		version = loadedString;
		line++;
		loadedString = nextRelevantLine(theModule);
		if (loadedString == null) {
			valid = false;
			return;
		}
		description = loadedString;
		line++;
		baseAddress = getInt(theModule);
		if (
			baseAddress < 0
			|| baseAddress > Target_File.size()
		) {
			valid = false;
			throw new IllegalArgumentException(
				"invalid base address"
			);
		}
		if (theModule.findWithinHorizon("#", 1) != null)
			hardwareOffset = getInt(theModule, true);
		if (useBasePointer) {
			if (theModule.findWithinHorizon("#", 1) == null)
				basePointer = -1;
			else
				basePointer = getInt(theModule);
			if (
				basePointer < 0
				|| basePointer > Target_File.size()
			) {
				valid = false;
				throw new IllegalArgumentException(
					"invalid base pointer"
				);
			}

			int tempInt = Target_File.getInt(
				basePointer
			) - hardwareOffset;
			if (
				useBasePointer
				&& tempInt != baseAddress
			)
				expanded = true;
			baseAddress = tempInt;
		}
		defaultEntryCount = getInt(theModule);
		if (defaultEntryCount <= 256)
			expandedEntryCount = 256;
		else if (defaultEntryCount < 65536)
			expandedEntryCount = 65536;
		else if (defaultEntryCount >= 65536) {
			addToErrorString(
				"Expansion cannot be supported:\n"
				+ "maximum value of indexing data type is "
				+ "too large"
			);
			expandedEntryCount = defaultEntryCount;
		}
		if (defaultEntryCount < 0) {
			valid = false;
			throw new IllegalArgumentException(
				"invalid default entry count"
			);
		}
		entrySize = getInt(theModule);
		if (entrySize <= 0) {
			valid = false;
			throw new IllegalArgumentException(
				"invalid entry size"
			);
		}
		loadedString = nextRelevantLine(theModule);
		if (loadedString == null) {
			valid = false;
			return;
		}
		if (loadedString.equals("NULL"))
			textTable = null;
		else
			entryList = new File(
				thisModule.getParent()
				+ File.separator
				+ loadedString
			);
		line++;
		loadedString = nextRelevantLine(theModule);
		if (loadedString == null) {
			valid = false;
			return;
		}
		if (loadedString.equals("NULL"))
			textTable = null;
		else
			textTable = new File(
				thisModule.getParent()
				+ File.separator
				+ loadedString
			);
		line++;
		processTextTableEntries();
		return;
	}

	private final void processValidComponents(Scanner theModule) {
		while (true) {
			EntryComponent currentComponent = new EntryComponent();
			String loadedString = nextRelevantLine(theModule);
			if (loadedString == null)
				break;
			currentComponent.setName(loadedString);
			line++;
			if (!currentComponent.isValid()) {
				for (int i = 0; i < 4; i++)
					loadedString = nextRelevantLine(theModule);
				if (loadedString == null)
					break;
				continue;
			}
			loadedString = nextRelevantLine(theModule);
			if (loadedString == null)
				break;
			currentComponent.setNumBitsOffset(loadedString);
			line++;
			if (!currentComponent.isValid()) {
				for (int i = 0; i < 3; i++)
					loadedString = nextRelevantLine(theModule);
				if (loadedString == null)
					break;
				continue;
			}
			loadedString = nextRelevantLine(theModule);
			if (loadedString == null)
				break;
			currentComponent.setBitCount(loadedString);
			if (
				currentComponent.getNumBitsOffset() + currentComponent.getBitCount()
				> entrySize << 3
			) {
				addToErrorString(
					"Line " + line
					+ ": Warning: Size and/or offset of "
					+ "component extends\nbeyond the entry "
					+ "size for this module"
				);
			}
			line++;
			if (!currentComponent.isValid()) {
				for (int i = 0; i < 2; i++)
					loadedString = nextRelevantLine(theModule);
				if (loadedString == null)
					break;
				continue;
			}
			loadedString = nextRelevantLine(theModule);
			if (loadedString == null)
				break;
			currentComponent.setType(loadedString);
			line++;
			if (!currentComponent.isValid()) {
				loadedString = nextRelevantLine(theModule);
				if (loadedString == null)
					break;
				continue;
			}
			loadedString = nextRelevantLine(theModule);
			if (loadedString == null)
				break;
			if (loadedString.equals("NULL"))
				currentComponent.setAssociatedList(
					loadedString
				);
			else
				currentComponent.setAssociatedList(
					thisModule.getParent()
					+ File.separator
					+ loadedString
				);
			line++;

			if (currentComponent.isValid())
				componentList.add(currentComponent);

			if (!theModule.hasNext())
				break;
		}
	}

	public Module(File input) {
		if (input == null) throw new RuntimeException("invalid path");

		Scanner theModule = null;
		try {
			theModule = new Scanner(input);
		} catch (Exception e) { valid = false; }
		thisModule = input;

		try {
			processChecksum(theModule);
			if (valid)
				processIDstring(theModule);
			if (valid)
				processBasePointerExpected(theModule);
			if (valid)
				processHeader(theModule);
			if (valid)
				processValidComponents(theModule);
		} catch (Exception e) {
			errorString += e.getMessage() + "\n";
		}

		try {
			theModule.close();
		} catch (Exception e) {}

		if (!valid) throw new RuntimeException(errorString);
	}

	public void expand() {
		if (!canExpand())
			throw new RuntimeException(
				"attempt to expand non-expandable data"
			);

		int originalSize = (entrySize * defaultEntryCount);
		int expandedSize = (entrySize * expandedEntryCount);

		byte[] thisArray = java.util.Arrays.copyOfRange(
			Target_File.getData(),
			baseAddress,
			baseAddress + originalSize
		);
		thisArray = java.util.Arrays.copyOf(thisArray, expandedSize);
		int cleanBaseAddress = baseAddress;
		baseAddress = Target_File.writeToFile(thisArray);
		Target_File.findAndReplace(
			cleanBaseAddress + hardwareOffset,
			baseAddress + hardwareOffset
		);
		expanded = true;
		byte[] wipedTable = new byte[originalSize];
		Target_File.overwriteFile(wipedTable, cleanBaseAddress);
	}

	public final String translate(String input) {
		if (input == null)
			return null;

		if (
			textTable == null
			|| textTableTranslationEntries.length == 0
		)
			return input;

		String outputString = "";
		boolean match;
		while (input.length() > 0) {
			match = false;
			for (TextTableEntry currEntry: textTableTranslationEntries)
				if (input.indexOf(currEntry.getFind()) == 0) {
					outputString += currEntry.getReplace();
					input = input.substring(
						currEntry
						.getFind()
						.length()
					);
					match = true;
					break;
				}
			if (!match) {
				outputString += input.substring(0, 1);
				input = input.substring(1);
			}
		}
		return outputString;
	}

	public final String encode(String input) {
		if (input == null)
			return null;

		if (
			textTable == null
			|| textTableEncodingEntries.length == 0
		)
			return input;

		String outputString = "";
		if (input.length() == 0)
			return outputString;
		boolean match;
		while (input.length() > 0) {
			match = false;
			for (TextTableEntry currEntry: textTableEncodingEntries)
				if (input.indexOf(currEntry.getReplace()) == 0) {
					outputString += currEntry.getFind();
					input = input.substring(
						currEntry.getReplace().length()
					);
					match = true;
					break;
				}
			if (!match) {
				outputString += input.substring(0, 1);
				input = input.substring(1);
			}
		}
		return outputString;
	}

	public final String getErrors() { return errorString; }

	public boolean isExpanded() { return expanded; }

	public boolean canExpand() {
		return !expanded
			&& (expandedEntryCount != defaultEntryCount)
			&& useBasePointer;
	}

	public final String getDescription() { return description; }

	public final int getHardwareOffset() { return hardwareOffset; }

	public int getEntryCount() {
		return expanded ? expandedEntryCount : defaultEntryCount;
	}

	public final File getEntryList() { return entryList; }

	public final LinkedList<EntryComponent> getComponentList() {
		return componentList;
	}

	public final void setHardwareOffset(int newOffset) {
		hardwareOffset = newOffset;
	}

	@Override
	public String toString() {
		String outputString = "\n";

		outputString += "Path: " + thisModule.getPath() + "\n";
		outputString += "Version: " + version + "\n";
		outputString += "Description: " + description + "\n";
		outputString +=
			"Base address: "
			+ String.format("0x%08X", baseAddress)
			+ "\n"
		;
		outputString +=
			"Base pointer: "
			+ String.format("0x%08X", basePointer)
			+ "\n"
		;
		outputString +=
			"Default entry count: "
			+ String.format("0x%X", defaultEntryCount)
			+ "\n"
		;
		outputString +=
			"Expanded entry count: "
			+ String.format("0x%X", expandedEntryCount)
			+ "\n"
		;
		outputString +=
			"Entry size: "
			+ String.format("0x%X", entrySize)
			+ "\n"
		;
		if (entryList != null)
			outputString += "Entry list path: " + entryList.getPath() + "\n";
		else
			outputString += "No entry list\n";
		if (textTable != null)
			outputString += "Text table path: " + textTable.getPath();
		else
			outputString += "No text table\n";

		return outputString;
	}
}
