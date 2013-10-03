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

package gov.nasa.arc.mct.gui;


import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

/*
 * Settings button is essentially just a toggle button, 
 * but doesn't contain some custom painting code, so 
 * that is tested here.
 */
public class TestSettingsButton {

    @Test
    public void testSelectedEnabled() {
        // Ensure that button looks unique when selected and/or focused
        // (and combinations thereof)
        
        boolean truths[] = { false , true };
        Set<ImageTester> testSet = new HashSet<ImageTester>();
        
        for (boolean selected : truths) {
            for (boolean focused : truths) {
                final boolean s = selected;
                final boolean f = focused;
                
                // Only interested in testing behavior not derived from
                // JToggleButton, and focus is not easily controlled in a 
                // test environment, so fake it.
                SettingsButton button = new SettingsButton() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public boolean isSelected() {
                        return s;
                    }
                    @Override
                    public boolean hasFocus() {
                        return f;
                    }
                };
                
                button.setSize(button.getPreferredSize());
                               
                BufferedImage image = new BufferedImage(button.getWidth(), button.getHeight(), BufferedImage.TYPE_INT_ARGB);
                button.paint(image.getGraphics());
                
                // Adding to set should return true (i.e. set did not contain this)
                Assert.assertTrue(testSet.add(new ImageTester(image)));
            }
        }
        
    }

    private static class ImageTester {
        private int hash;
        private BufferedImage image;
        
        public ImageTester(BufferedImage image) {
            this.image = image;
            
            // Generate hash based on pixel data
            int prior = 0;
            for (int i = 0; i < 32 && i < image.getWidth() * image.getHeight(); i++) {
                hash <<= 1;
                int rgb = image.getRGB(i % image.getWidth(), i / image.getWidth());
                if (rgb > prior) {
                    hash |= 1;
                }
                prior = rgb;
            }
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override
        public boolean equals(Object o) {           
            if (o instanceof ImageTester) {
                ImageTester tester = (ImageTester) o;
                
                // Ensure same size
                if (tester.image.getWidth() != image.getWidth() || 
                    tester.image.getHeight() != image.getHeight()) {
                    return false;
                }
                
                // Ensure same pixel data
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        if (image.getRGB(x, y) != tester.image.getRGB(x, y)) {
                            return false;
                        }
                    }
                }
                
                return true;
            } else {
                return false;
            }
        }
    }
}
