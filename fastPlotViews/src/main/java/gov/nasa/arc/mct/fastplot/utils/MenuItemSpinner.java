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
package gov.nasa.arc.mct.fastplot.utils;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 * A JSpinner that can be used as a menu item. (Includes key listeners to allow tabbing in, etc)
 */
public class MenuItemSpinner extends JSpinner {
	private static final long serialVersionUID = 1571587288826156261L;

	public MenuItemSpinner(SpinnerModel model, final JMenu menu) {
		super(model);
		final JSpinner spinner = this;
		spinner.setBorder(new EmptyBorder(2,2,2,2));
		
		 final JFormattedTextField myTextField = ((NumberEditor) spinner
			        .getEditor()).getTextField();
		 final Document doc = myTextField.getDocument();
		 if (doc instanceof PlainDocument) {
			 AbstractDocument abstractDoc = new PlainDocument() {

                 /**
				 * 
				 */
				private static final long serialVersionUID = 6648357467446359739L;

				@Override
                 public void setDocumentFilter(DocumentFilter filter) {
                     if (filter instanceof NumberFilter) {
                         super.setDocumentFilter(filter);
                     }
                 }
             };
             abstractDoc.setDocumentFilter(new NumberFilter());
             try {
				abstractDoc.insertString(0, spinner.getValue().toString(), null);
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
             myTextField.setDocument(abstractDoc);
             myTextField.setValue(spinner.getValue());
		 }
		
		spinner.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE ) {
					((NumberEditor) spinner.getEditor()).getTextField().setText("");
				} 
				myTextField.grabFocus();
			}
			
		});  
			 
		 myTextField.addFocusListener(new FocusListener()
			    {
			 @Override
			 public void focusGained(FocusEvent e) {
				 SwingUtilities.invokeLater(new Runnable() {
			            public void run() {
			            	myTextField.selectAll();
			            }
			     });
			 }

			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
			}
		});
		 
		 final NumberEditor numberEditor = (NumberEditor) spinner.getEditor();
		 numberEditor.setFocusable(true);
		 numberEditor.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_LEFT && 
						numberEditor.getTextField().getCaretPosition() == 0) {
					menu.setSelected(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		 
		 myTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_LEFT && 
						numberEditor.getTextField().getCaretPosition() == 0) {
					menu.setSelected(true);
					menu.grabFocus();
					((JPopupMenu) spinner.getParent()).setSelected(menu);
				} 
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
			
		});
		 
		menu.addMenuKeyListener(new MenuKeyListener() {

			@Override
			public void menuKeyTyped(MenuKeyEvent e) {
			}

			@Override
			public void menuKeyPressed(MenuKeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_RIGHT && menu.isSelected()) {
					spinner.setVisible(true);
					spinner.requestFocusInWindow();
					((NumberEditor) spinner.getEditor()).requestFocusInWindow();
					myTextField.selectAll();
				} 
			}

			@Override
			public void menuKeyReleased(MenuKeyEvent e) {
			}
			
		});
		 
		
	}
	
	class NumberFilter extends DocumentFilter {
		private StringBuilder insertBuilder;
		private StringBuilder replaceBuilder;
		
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {
			insertBuilder = new StringBuilder(string);
			for (int k = insertBuilder.length() - 1; k >= 0; k--) {
				int cp = insertBuilder.codePointAt(k);
				if (! Character.isDigit(cp)) {
					insertBuilder.deleteCharAt(k);
					if (Character.isSupplementaryCodePoint(cp)) {
						k--;
						insertBuilder.deleteCharAt(k);
					}
				}
			}
			if (insertBuilder.length() + fb.getDocument().getLength() < 3) {
				super.insertString(fb, offset, insertBuilder.toString(), attr);
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr)
				throws BadLocationException {
			replaceBuilder = new StringBuilder(string);
			for (int k = replaceBuilder.length() - 1; k >= 0; k--) {
				int cp = replaceBuilder.codePointAt(k);
				if (! Character.isDigit(cp)) {
					replaceBuilder.deleteCharAt(k);
					if (Character.isSupplementaryCodePoint(cp)) {
						k--;
						replaceBuilder.deleteCharAt(k);
					}
				}
			}
			if ((replaceBuilder.length() - length + fb.getDocument().getLength()) < 3 ) {
				if (replaceBuilder.length() - length + fb.getDocument().getLength() == 0) {
					super.replace(fb, offset, length, "2", attr);
				} else {
					super.replace(fb, offset, length, replaceBuilder.toString(), attr);
				}
			}
		}

	}
}
