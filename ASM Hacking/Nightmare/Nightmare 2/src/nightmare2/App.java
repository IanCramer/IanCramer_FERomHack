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
 *  <Description> This class provides the initialization of the
 *  application
 */

package nightmare2;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

public class App extends SingleFrameApplication {
	private static String initPath;
	private static View instanceDialog;

	@Override
	protected void startup() {
		instanceDialog = new View(this);
		show(instanceDialog);
	}

	public static String getInitialPath() { return initPath; }

	public static View getView() { return instanceDialog; }

	public static void setInitialPath(String path) { initPath = path; }

	/**
	 * NOTE: According to the doc for the method this is overriding:<br>
	 * By default the show methods inject resources before initializing
	 * the JFrame or JDialog's size, location, and restoring the window's
	 * session state. If the app is showing a window whose resources have
	 * already been injected, or that shouldn't be initialized via resource
	 * injection, this method can be overridden to defeat the default
	 * behavior.
	 */
	@Override
	protected void configureWindow(java.awt.Window root) {}

	public static App getApplication() {
		return Application.getInstance(App.class);
	}

	public static void main(String[] args) {
		if (args != null && args.length >= 1)
			initPath = args[0];

		launch(App.class, args);
	}
}
