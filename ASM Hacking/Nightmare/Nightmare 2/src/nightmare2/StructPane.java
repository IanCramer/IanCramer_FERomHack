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
 *  <Description> GUI for Struct class
 */

package nightmare2;

import Controls.DecimalList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedList;
import javax.swing.border.LineBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import Controls.DependentControl;
import Controls.HexEditor;
import Controls.HexField;
import Controls.HexList;
import Controls.HexNumberEditor;
import Controls.SignedEditor;
import Controls.StructLoader;
import Controls.TextField;
import Controls.UnsignedEditor;
import Model.Module;
import Model.Module.EntryComponent;
import Model.Module.EntryType;
import Model.Struct;
import java.awt.event.MouseListener;

public class StructPane extends Box {
	protected ModuleFrame view;
	protected Module module;
	private JComponent headingPanel = new JPanel();

	protected JPanel dependentControlsPanel = new JPanel();
	protected JScrollPane dependentScrollPane;
	protected LinkedList<JLabel> entryComponentLabels = new LinkedList<JLabel>();

	protected int finalEntryIndex;
	protected int selectedIndex;

	protected LinkedList<EntryComponent> componentList =
		new LinkedList<EntryComponent>();
	protected LinkedList<Component> componentLinkedList =
		new LinkedList<Component>();

	private String toolTip(EntryComponent input) {
		if (!selectionInBounds())
			return "";

		int address = input.getAddress(selectedIndex);
		int amount = input.getNumBitsOffset();
		int bitOffset = amount % 8;
		String typeString = bitOffset == 0 ? "byte" : "bit";
		if (bitOffset == 0) amount >>= 3;
		if (amount != 1) typeString += "s";
		String toolTipString = String.format(
			"0x%08X:%d (%s) - %d bit",
			address,
			bitOffset,
			"offset by " + amount + " " + typeString,
			input.getBitCount()
		);
		if (input.getBitCount() != 1)
			toolTipString += "s";
		if (
			input.getEntryType() == EntryType.NEHU
			|| input.getEntryType() == EntryType.NDHU
			|| input.getEntryType() == EntryType.HEXA
			|| input.getEntryType() == EntryType.STRUCT
		)
			toolTipString += " (hex)";
		if (selectedIndex == finalEntryIndex)
			toolTipString = "";
		return toolTipString;
	}

	private void updateToolTips() {
		LinkedList<EntryComponent> tempList =
			new LinkedList<EntryComponent>();
		for (JLabel currComponentLabel: entryComponentLabels) {
			EntryComponent currEntryComponent =
				componentList.pollFirst();
			tempList.add(currEntryComponent);
			String toolTipString = toolTip(currEntryComponent);
			currComponentLabel.setToolTipText(toolTipString);
			currComponentLabel.setText(
				"    "
				+ currEntryComponent.getName()
				+ "            "
			);
		}
		componentList = tempList;
	}

	private void addDependentControls() {
		dependentControlsPanel.setLayout(new BoxLayout(
			dependentControlsPanel,
			BoxLayout.PAGE_AXIS
		));
		LinkedList<EntryComponent> tempList =
			new LinkedList<EntryComponent>();
		boolean first = false;
		JPanel spacePanel;
		for (final Component currComponent: componentLinkedList) {
			final EntryComponent currEntryComponent =
				componentList.pollFirst();
			tempList.add(currEntryComponent);
			JLabel currentComponentLabel = new JLabel(
				"    "
				+ currEntryComponent.getName()
				+ "            "
			);
			JPanel currComponentPanel = new JPanel();
			currComponentPanel.setLayout(new BorderLayout());
			currComponentPanel.add(
				currentComponentLabel, BorderLayout.WEST
			);
			entryComponentLabels.add(currentComponentLabel);
			if (!first) {
				spacePanel = new JPanel();
				spacePanel.setSize(currComponentPanel.getSize());
				dependentControlsPanel.add(spacePanel);
				first = true;
			}
			JPanel editPanel = new JPanel();
			editPanel.add(currComponent);
			currComponentPanel.add(editPanel, BorderLayout.EAST);
			JLabel guideLabel = new JLabel();
			class AutoSizeLine extends LineBorder {
				private boolean visible = false;

				public AutoSizeLine(Color color) { super(color); }

				@Override
				public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
					if (!visible) return;
					Color oldColor = g.getColor();
					g.setColor(lineColor);
					g.drawRect(x, y + (height/2), width, 1);
					g.setColor(oldColor);
				}

				public boolean isVisible() { return visible; }

				public void setVisible(boolean b) {
					visible = b;
				}
			}
			final AutoSizeLine line = new AutoSizeLine(Color.BLACK);
			guideLabel.setBorder(line);
			guideLabel.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {}

				@Override
				public void mousePressed(MouseEvent e) {}

				@Override
				public void mouseReleased(MouseEvent e) {}

				@Override
				public void mouseEntered(MouseEvent e) {
					line.setVisible(true);
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					line.setVisible(false);
					repaint();
				}
			});
			currComponentPanel.add(guideLabel, BorderLayout.CENTER);
			dependentControlsPanel.add(currComponentPanel);
		}
		componentList = tempList;
		dependentScrollPane = new JScrollPane(dependentControlsPanel);
		add(dependentScrollPane);
	}

	private void createDependentControls() {
		componentList = module.getComponentList();
		for (final EntryComponent currComponent: componentList) {
			// TEXT handling code; tested and working!
			if (currComponent.getEntryType() == EntryType.TEXT) {
				componentLinkedList.add(new TextField(
					this, module, currComponent
				));
			}
			// End TEXT handling code; tested and working!
			// HEXA handling code; tested and working!
			else if (currComponent.getEntryType() == EntryType.HEXA) {
				componentLinkedList.add(new HexField(
					this, module, currComponent
				));
			}
			// End HEXA handling code; tested and working!
			// NEHU handling code; tested and working!
			else if (currComponent.getEntryType() == EntryType.NEHU) {
				componentLinkedList.add(new HexEditor(
					this, module, currComponent
				));
			}
			// End NEHU handling code; tested and working!
			// STRUCT handling code; tested and working!
			else if (currComponent.getEntryType() == EntryType.STRUCT) {
				componentLinkedList.add(new StructLoader(
					this, module, currComponent
				));
			}
			// End STRUCT handling code; tested and working!
			// NEDS handling code; tested and working!
			else if (currComponent.getEntryType() == EntryType.NEDS) {
				componentLinkedList.add(new SignedEditor(
					this, module, currComponent
				));
			}
			// End NEDS handling code; tested and working!
			// NEDU handling code; tested and working!
			else if (currComponent.getEntryType() == EntryType.NEDU) {
				componentLinkedList.add(new UnsignedEditor(
					this, module, currComponent
				));
			}
			// End NEDU handling code; tested and working!
			// NDDU handling code; tested and working!
			else if (currComponent.getEntryType() == EntryType.NDDU) {
				componentLinkedList.add(new DecimalList(
					this, module, currComponent
				));
			}
			// End NDDU handling code; tested and working!
			// NDHU handling code; tested and working!
			else if (currComponent.getEntryType() == EntryType.NDHU) {
				componentLinkedList.add(new HexList(
					this, module, currComponent
				));
			}
			// End NDHU handling code; tested and working!
		}
		addDependentControls();
	}

	protected final void updateAllEntryComponents() {
		for (final Component currControl: componentLinkedList) {
			try {
				((DependentControl)currControl).update();
				continue;
			} catch (Exception e) {
				Common_Dialogs.showCatchErrorDialog(e);
			}
		}
		updateToolTips();
	}

	private void displayErrors() {
		String errorString = module.getErrors();
		if (errorString.equals(""))
			return;
		Common_Dialogs.showGenericErrorDialog(errorString);
	}

	public StructPane(ModuleFrame view, File input, Integer baseAddress) {
		super(BoxLayout.PAGE_AXIS);
		this.view = view;

		selectedIndex = 0;
		finalEntryIndex = 1;
		final boolean struct = baseAddress != null
			&& this instanceof StructPane;
		try {
			if (struct)
				module = new Struct(input, baseAddress);
			else
				module = new Module(input);
		} catch (Exception e) {
			Common_Dialogs.showCatchErrorDialog(e);
			throw new RuntimeException("module failed to load");
		}

		headingPanel.setLayout(new BoxLayout(
			headingPanel,
			BoxLayout.PAGE_AXIS
		));
		add(headingPanel);

		JPanel hardwareOffsetPanel = new JPanel();
		JLabel hardwareOffsetLabel = new JLabel("Hardware offset: ");
		final SpinnerModel hardwareOffsetModel = new SpinnerNumberModel(
			0x00,
			Integer.MIN_VALUE,
			Integer.MAX_VALUE,
			1
		);
		final JSpinner hardwareOffsetSpinner =
			new JSpinner(hardwareOffsetModel);

		hardwareOffsetSpinner.setEditor(
			new HexNumberEditor(hardwareOffsetSpinner, 8)
		);
		hardwareOffsetSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				module.setHardwareOffset(
					(Integer)hardwareOffsetSpinner.getValue()
				);
			}
		});
		hardwareOffsetSpinner.setValue(module.getHardwareOffset());
		hardwareOffsetPanel.add(hardwareOffsetLabel);
		hardwareOffsetPanel.add(hardwareOffsetSpinner);
		add(hardwareOffsetPanel);

		createDependentControls();
		if (struct) updateAllEntryComponents();
		displayErrors();
	}

	public final void addToHeading(JComponent comp) { headingPanel.add(comp); }

	public final void update() { view.pack(); }

	public final String getModuleName() { return module.getDescription(); }

	public final int extraWidth() {
		return dependentScrollPane.getVerticalScrollBar().getWidth();
	}

	public final int extraHeight() {
		return dependentScrollPane.getHorizontalScrollBar().getHeight();
	}

	public final boolean selectionInBounds() {
		return selectedIndex >= 0 && selectedIndex < finalEntryIndex;
	}

	public final int getSelectedIndex() { return selectedIndex; }
}
