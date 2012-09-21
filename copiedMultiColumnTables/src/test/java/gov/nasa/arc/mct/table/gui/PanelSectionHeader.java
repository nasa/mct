/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.table.gui;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class PanelSectionHeader extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private final static ClassLoader CLASSLOADER = PanelSectionHeader.class.getClassLoader();
	
	private final static ImageIcon TOGGLE_OPEN =
		new ImageIcon(CLASSLOADER.getResource("images/controlToggleOpenDark.png"));
	
	private final static ImageIcon TOGGLE_CLOSED =
		new ImageIcon(CLASSLOADER.getResource("images/controlToggleClosedDark.png"));
	
	private Set<Component> toggledComponents = new HashSet<Component>();
	
	public PanelSectionHeader(String title) {
		setLayout(new GridBagLayout());
		
		ConstraintBuilder builder = new ConstraintBuilder(this);
		builder.hpad(0);
		
		JLabel titleLabel = new JLabel(title);
		JSeparator line = new JSeparator(SwingConstants.HORIZONTAL);

		JToggleButton button = new JToggleButton(TOGGLE_CLOSED, true);
		button.setBorder(new EmptyBorder(0,0,0,0));
		button.setSelectedIcon(TOGGLE_OPEN);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (Component c : toggledComponents) {
					c.setVisible(!c.isVisible());
				}
			}
		});
		
		builder.at(0,0).insets(0,0,0,5).add(titleLabel);
		builder.at(0,1).insets(0,0,0,5).add(button);
		builder.at(0,2).hfill().add(line);
	}
	
	public void addToggledComponents(Component...components) {
		for (Component c : components) {
			toggledComponents.add(c);
		}
	}

	public void removeToggledComponents(Component...components) {
		for (Component c : components) {
			toggledComponents.remove(c);
		}
	}

}
