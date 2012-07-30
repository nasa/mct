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
package gov.nasa.arc.mct.gui.menu.actions;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.GroupAction;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class DoTheseAction extends GroupAction {

	private static final String CHOOSE_THAT = "Choose That";
	private static final String CHOOSE_THIS = "Choose This";
	
	private String currentChoice;

	public DoTheseAction() {
		this("Do These Instead");
	}

	protected DoTheseAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	@Override
	public boolean canHandle(ActionContext context) {
	    ActionContextImpl actionContext = (ActionContextImpl) context;
        AbstractComponent component = actionContext.getTargetComponent();
        
        if (component == null)
        	return false;

        if ("My Model B".equals(component.getDisplayName())) {
	        List<MySpecificAction> actions = new ArrayList<MySpecificAction>();
	        actions.add(new MySpecificAction(actionContext, CHOOSE_THIS));
	        actions.add(new MySpecificAction(actionContext, CHOOSE_THAT));
	        setActions(actions.toArray(new RadioAction[actions.size()]));
	
	        return true;
        }
        else
        	return false;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	private class MySpecificAction extends GroupAction.RadioAction {
		
		// TODO: Should we be looking at "context" to decide how
		// to handle the action? Right now the action chosen is
		// context independent.
		@SuppressWarnings("unused")
		private ActionContextImpl context;
		private String choice;
		public MySpecificAction(ActionContextImpl context, String choice) {
			this.context = context;
			this.choice = choice;
		}

		@Override
        public boolean isMixed() {
            return false;
        }
		
		@Override
		public boolean isSelected() {
			return (choice.equals(currentChoice));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (CHOOSE_THIS.equals(currentChoice))
				currentChoice = CHOOSE_THAT;
			else
				currentChoice = CHOOSE_THIS;
		}
		
	}
}
