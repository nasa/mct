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

package org.acme.example.view;

import javax.swing.ButtonModel;

/** 
 * Records the state of borders around the perimeter.
 */
public class BorderState {
	
	private byte state = 0;
	/**
	 * Default state if no border attribute
	 */
	public static BorderEdge defaultState = BorderEdge.NONE;
   
	/** Border edges */
	public enum BorderEdge {
		/** Nemo. */      NONE((byte) 0), 
		/** sinistro. */  WEST((byte) 0x8), 
		/** summo. */     NORTH((byte) 0x1),
		/** dextro. */    EAST((byte) 0x4), 
		/** solum. */     SOUTH((byte) 0x2);
		
		private byte value;

		BorderEdge(byte value) {
	     this.value = value;
		}
		
		public byte value() {
			return value;
		}
	}

	/** Creates a new border state.
	 * @param state
	 */
	public BorderState(byte state) {
		this.state = state;
	}

	/** Creates a BorderState given a string representation. One or more edges may be set.
	 * @param value string representation
	 */
	public BorderState(String value) {

		state = BorderEdge.NONE.value();
		if (value.contains(BorderEdge.NONE.name())) {
			return;
		} else {
			if (value.contains(BorderEdge.WEST+"on")) {
				addBorderState(BorderEdge.WEST.value());
			}
			if (value.contains(BorderState.BorderEdge.NORTH+"on")) {
				addBorderState(BorderEdge.NORTH.value());
			}
			if (value.contains(BorderState.BorderEdge.EAST+"on")) {
				addBorderState(BorderEdge.EAST.value());
			}
			if (value.contains(BorderState.BorderEdge.SOUTH+"on")) {
				addBorderState(BorderEdge.SOUTH.value());
			}
		}
	}

	/**
	 * Creates a composite BorderState given all control button models. 
	 * @param west a button model
	 * @param north a button model
	 * @param east a button model
	 * @param south a button model
	 */
	public BorderState(ButtonModel west, ButtonModel north, ButtonModel east, ButtonModel south) {

		state = BorderEdge.NONE.value();
		if (west.isSelected()) {
			addBorderState(BorderEdge.WEST.value());
		}
		if (north.isSelected()) {
			addBorderState(BorderEdge.NORTH.value());
		}
		if (east.isSelected()) {
			addBorderState(BorderEdge.EAST.value());
		}
		if (south.isSelected()) {
			addBorderState(BorderEdge.SOUTH.value());
		}
	}
	
    private byte translateEdgeToState(BorderEdge t) {
    	
		state = BorderEdge.NONE.value();
    	if (t == BorderEdge.WEST) {
    		state = BorderEdge.WEST.value();
    	} else {
    		if (t == BorderState.BorderEdge.NORTH) {
    			state = BorderEdge.NORTH.value();
    		} else {
    			if (t == BorderState.BorderEdge.EAST) {
    				state = BorderEdge.EAST.value();
    			} else {
    				if (t == BorderState.BorderEdge.SOUTH) {
        				state = BorderEdge.SOUTH.value();
        			} 
    			}
    		}
    	}
		return state;
	}
    
	/**
	 * Returns true if the top border edge is to be drawn.
	 * @return result
	 */
	public boolean hasNorthBorder() {
			return (state & BorderEdge.NORTH.value()) != 0;
	}

	/**
	 * Returns true if the right border edge is to be drawn.
	 * @return result
	 */
	public boolean hasEastBorder() {
			return (state & BorderEdge.EAST.value()) != 0;
	}

	/**
	 * Returns true if the bottom border edge is to be drawn.
	 * @return result
	 */
	public boolean hasSouthBorder() {
			return (state & BorderEdge.SOUTH.value()) != 0;
	}

	/**
	 * Returns true if the left border edge is to be drawn.
	 * @return result
	 */
	public boolean hasWestBorder() {
			return (state & BorderEdge.WEST.value()) != 0;
	}
	
	/** Add an edge to the state.
	 * @param newBorder new edge
	 */
	public void addBorderState(byte newBorder) {
		this.state = (byte) (this.state | newBorder);
	}

	/**Remove an edge from the state.
	 * @param removeBorder edge to remove
	 */
	public void removeBorderState(byte removeBorder) {
		this.state = (byte) (this.state & ~removeBorder);
	}


	/**Add an edge to the state.
	 * @param edge new edge
	 */
	public void addBorderState(BorderEdge edge) {
		byte newBorder = translateEdgeToState(edge);
		this.state = (byte) (this.state | newBorder);
	}

    /**Remove an edge from the state.
     * @param edge to remove
     */
    public void removeBorderState(BorderEdge edge) {
    	byte removeBorder = translateEdgeToState(edge);
        this.state = (byte) (this.state & ~removeBorder);
    }

    /**
     * Removes all borders from view.
     */
    public void removeAllBorders() {
        this.state = 0;
    }
    
	@Override
	public String toString() {
		StringBuilder rv = new StringBuilder();
		if (hasWestBorder())  
			rv.append(BorderState.BorderEdge.WEST+"on:" ); 
		if (hasNorthBorder())  
			rv.append(BorderState.BorderEdge.NORTH+"on:" ); 
		if (hasEastBorder())  
			rv.append(BorderState.BorderEdge.EAST+"on:" ); 
		if (hasSouthBorder())  
			rv.append(BorderState.BorderEdge.SOUTH+"on:" ); 
		return rv.length() == 0 ?  BorderState.BorderEdge.NONE.name() : rv.toString();
	}
}