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
 *  <Description> Convenience class for abusing JOptionPane
 */

package nightmare2;

import java.io.File;
import javax.swing.JOptionPane;

public class Common_Dialogs {
	public static void showGenericErrorDialog(final String message) {
		/**
		 * This had to be removed due to Java version issues;
		 * it now prints to the standard out
		javax.swing.JTextArea eTextArea =
			new javax.swing.JTextArea();
		eTextArea.setText(message + "\n");
		eTextArea.setRows(16);
		eTextArea.setColumns(48);
		eTextArea.setEditable(false);
		javax.swing.JScrollPane eScrollPane =
			new javax.swing.JScrollPane(eTextArea);
		JOptionPane.showMessageDialog(
			null, eScrollPane, "Error",
			JOptionPane.ERROR_MESSAGE
		);
		**/
		System.out.println(message);
	}

	public static <E extends Exception> void showCatchErrorDialog(E e) {
		if (e == null) return;
		StackTraceElement[] stackTrace = e.getStackTrace();
		StackTraceElement exceptSource = stackTrace[0];
		String methodName = exceptSource.getClassName()
			+ "." + exceptSource.getMethodName();
		String exceptMessage = e.getMessage();
		if (exceptMessage == null) exceptMessage = "Unknown exception";
		exceptMessage = "\t" + exceptMessage.replaceAll("\n", "\n\t");
		String message = methodName + ":\n" + exceptMessage;
		showGenericErrorDialog(message);
	}

	public static void showGenericInformationDialog(String message) {
		JOptionPane.showMessageDialog(
			null, message, "Notice",
			JOptionPane.INFORMATION_MESSAGE
		);
	}

	public static int showYesNoDialog(String message) {
		return JOptionPane.showConfirmDialog(
			null, message, "Input",
			JOptionPane.YES_NO_OPTION
		);
	}

	private static File fileHelper(String title, int mode) {
		String inputFileName;

		java.awt.FileDialog chooser = new java.awt.FileDialog(
			new java.awt.Frame(), title, mode
		);
		chooser.setVisible(true);
		chooser.setLocationRelativeTo(null);
		if (
			chooser.getDirectory() == null
			|| chooser.getFile() == null
		)
			return null;
		inputFileName = chooser.getDirectory() + chooser.getFile();

		return new File(inputFileName);
	}

	public static File showOpenFileDialog(String what) {
		return fileHelper(
			"Select " + what + " for opening",
			java.awt.FileDialog.LOAD
		);
	}

	public static File showSaveFileDialog(String what) {
		return fileHelper(
			"Select path to save " + what,
			java.awt.FileDialog.SAVE
		);
	}
}
