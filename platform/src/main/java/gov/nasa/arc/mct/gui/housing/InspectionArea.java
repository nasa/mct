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
package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.util.LafColor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial") 
public class InspectionArea extends View implements SelectionProvider {
        public static final String INSPECTION_AREA_VIEW_PROP_KEY = "InspectionArea";
        public static String SELECTION_WILL_CHANGE_PROP = "selectionWillChange";

        private static final Color BACKGROUND_COLOR = LafColor.WINDOW_BORDER.darker();
        private static final Color FOREGROUND_COLOR = LafColor.WINDOW.brighter();

        private final PropertyChangeListener selectionChangeListener = new PropertyChangeListener() {
            
            @SuppressWarnings("unchecked")
            @Override
            public void propertyChange(PropertyChangeEvent event) {
               firePropertyChange(SELECTION_WILL_CHANGE_PROP, event.getOldValue(), event.getNewValue());
               updateInspectionArea((Collection<View>) event.getNewValue());
               firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, event.getOldValue(), event.getNewValue());
            }
        };
        
        public InspectionArea(AbstractComponent ac, ViewInfo vi) {
            setManifestedComponent(ac);
            setLayout(new BorderLayout());
            inspectorView = new EmptyInspector();
            add(inspectorView, BorderLayout.CENTER);
            
            addAncestorListener(new AncestorListener() {
                private MCTHousing housing;
                
                @Override
                public void ancestorAdded(AncestorEvent event) {
                    housing = findHousing();
                    housing.getSelectionProvider().addSelectionChangeListener(selectionChangeListener);
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                    
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    if (housing != null) {
                        housing.getSelectionProvider().removeSelectionChangeListener(selectionChangeListener);
                    }
                }
                
                private MCTHousing findHousing() {
                    return (MCTHousing) SwingUtilities.getAncestorOfClass(MCTHousing.class, InspectionArea.this);
                }
                
            });
        }
        
        private View inspectorView;
        
        @Override
        public View getHousedViewManifestation() {
            return inspectorView.getHousedViewManifestation();
        }
        
        private void updateInspectionArea(Collection<View> selectedViews) {            
            if (inspectorView != null) {
                remove(inspectorView);
                inspectorView = null;
            }
            
            if (selectedViews.isEmpty() || selectedViews.size() > 1) {
                inspectorView = new EmptyInspector();
            } else {            
                View v = (View) SwingUtilities.getAncestorOfClass(View.class, selectedViews.iterator().next());
                if (v != null) {
                    if (v.isContentOwner()) {
                        inspectorView = v.getManifestedComponent().getViewInfos(ViewType.CENTER_OWNED_INSPECTOR).iterator().next().createView(getManifestedComponent());
                    }
                }
                if (inspectorView == null)
                    inspectorView = getManifestedComponent().getViewInfos(ViewType.INSPECTOR).iterator().next().createView(getManifestedComponent());
            }
            if (isLocked())
                exitLockedState();
            add(inspectorView, BorderLayout.CENTER);
            inspectorView.requestFocusInWindow();
            revalidate();
        }

        @Override
        public void addSelectionChangeListener(PropertyChangeListener listener) {
            addPropertyChangeListener(SELECTION_CHANGED_PROP, listener);
            addPropertyChangeListener(SELECTION_WILL_CHANGE_PROP, listener);
        }

        @Override
        public void removeSelectionChangeListener(PropertyChangeListener listener) {
            removePropertyChangeListener(SELECTION_CHANGED_PROP, listener);
            removePropertyChangeListener(SELECTION_WILL_CHANGE_PROP, listener);
        }

        @Override
        public Collection<View> getSelectedManifestations() {
            return Collections.emptySet();
        }

        @Override
        public void clearCurrentSelections() {
        }
        
        @Override
        public void enterLockedState() {
            super.enterLockedState();
            inspectorView.enterLockedState();
        }
        
        @Override
        public void exitLockedState() {
            super.exitLockedState();
            inspectorView.exitLockedState();
        }
                
        private static final class EmptyInspector extends View {
            public EmptyInspector() {
                setLayout(new BorderLayout());
                JPanel titlebar = new JPanel();
                titlebar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                JLabel titleLabel = new JLabel("Inspector");
                titlebar.add(titleLabel);
                titleLabel.setForeground(FOREGROUND_COLOR);
                titlebar.setBackground(BACKGROUND_COLOR);
                add(titlebar, BorderLayout.NORTH);
            }
            
            @Override
            public AbstractComponent getManifestedComponent() {
                return AbstractComponent.NULL_COMPONENT;
            }
        }        
    }