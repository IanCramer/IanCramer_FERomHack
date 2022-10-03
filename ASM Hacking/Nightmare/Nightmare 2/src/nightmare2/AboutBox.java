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
 *  <Description> This class is the dialog displayed by the application
 *  when the user requests more information from the Help menu item
 */

package nightmare2;

import org.jdesktop.application.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

public class AboutBox extends JDialog {
        private JButton closeButton;

	public AboutBox(java.awt.Frame parent) {
		super(parent);
		initComponents();
		getRootPane().setDefaultButton(closeButton);
	}

	@Action
	public void closeAboutBox() {
		dispose();
	}

	private void initComponents() {
		closeButton = new JButton();
                JLabel appTitleLabel = new JLabel();
                JLabel versionLabel = new JLabel();
                JLabel appVersionLabel = new JLabel();
                JLabel vendorLabel = new JLabel();
                JLabel appVendorLabel = new JLabel();
                JLabel appDescLabel = new JLabel();
                JLabel appDescLabel1 = new JLabel();

                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(nightmare2.App.class).getContext().getResourceMap(AboutBox.class);
                setTitle(resourceMap.getString("title"));
                setModal(true);
                setName("aboutBox");
                setResizable(false);

                ActionMap actionMap = org.jdesktop.application.Application.getInstance(nightmare2.App.class).getContext().getActionMap(AboutBox.class, this);
                closeButton.setAction(actionMap.get("closeAboutBox"));
                closeButton.setName("closeButton");

                appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD, appTitleLabel.getFont().getSize()+4));
                appTitleLabel.setText(resourceMap.getString("Application.title"));
                appTitleLabel.setName("appTitleLabel");

                versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));
                versionLabel.setText(resourceMap.getString("versionLabel.text"));
                versionLabel.setName("versionLabel");

                appVersionLabel.setText(resourceMap.getString("Application.version"));
                appVersionLabel.setName("appVersionLabel");

                vendorLabel.setFont(vendorLabel.getFont().deriveFont(vendorLabel.getFont().getStyle() | java.awt.Font.BOLD));
                vendorLabel.setText(resourceMap.getString("vendorLabel.text"));
                vendorLabel.setName("vendorLabel");

                appVendorLabel.setText(resourceMap.getString("Application.vendor"));
                appVendorLabel.setName("appVendorLabel");

                appDescLabel.setText(resourceMap.getString("appDescLabel.text"));
                appDescLabel.setName("appDescLabel");

                appDescLabel1.setText(resourceMap.getString("appDescLabel1.text"));
                appDescLabel1.setName("appDescLabel1");

                GroupLayout layout = new GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(appDescLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(appDescLabel, GroupLayout.PREFERRED_SIZE, 277, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(appTitleLabel)
                                        .addComponent(closeButton))
                                .addGap(40, 40, 40))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(vendorLabel)
                                        .addComponent(versionLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(appVendorLabel)
                                        .addComponent(appVersionLabel))
                                .addContainerGap(231, Short.MAX_VALUE))
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(appTitleLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(appDescLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(appDescLabel1)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(versionLabel)
                                                .addGap(6, 6, 6)
                                                .addComponent(vendorLabel))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(appVersionLabel)
                                                .addGap(6, 6, 6)
                                                .addComponent(appVendorLabel)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(closeButton)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                pack();
        }
}
