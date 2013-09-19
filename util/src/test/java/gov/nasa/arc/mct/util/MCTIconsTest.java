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
package gov.nasa.arc.mct.util;

import static org.testng.Assert.assertNotNull;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MCTIconsTest {
	@Test
	public void testIcons() throws Exception {
		ImageIcon icon;
		
		icon = MCTIcons.getWarningIcon();
		assertNotNull(icon);
		
		icon = MCTIcons.getErrorIcon(100, 50);
		assertNotNull(icon);
		
		icon = MCTIcons.getComponent();
		assertNotNull(icon);
	}
	
	@Test
	public void testGeneratedIcons() {
	    // Verify that generated icons are unique up to first 8 bits
	    
	    // Use a hash set & wrapper to make comparison faster
	    Set<IconTester> iconTesters = new HashSet<IconTester>();
	    
	    for (int hash = 0 ; hash < (1 << 8); hash++) {
	        // Generate an icon
	        Icon icon = MCTIcons.generateIcon(hash, 14, Color.WHITE);
	        
	        // Set.add only returns true of set was changed
	        // (i.e. if a matching icon was not contained)
	        Assert.assertTrue(iconTesters.add(new IconTester(icon)));
	    }
	    
	}
	
	@Test
	public void testProcessIcon() {
	    // Verify that processing changes an image appropriately
	    // (Test may need to change or be made obsolete when look-feel changes)
	    ImageIcon original = MCTIcons.generateIcon(100, 14, Color.WHITE);
	    ImageIcon processed = MCTIcons.processIcon(original);
	    
	    IconTester originalTester = new IconTester(original);
	    IconTester processedTester = new IconTester(processed);
	    
	    // Image should have changed
	    Assert.assertFalse(originalTester.equals(processedTester));
	    
	    // New image should be more like the desired color (blue)
	    Assert.assertTrue(
	            processedTester.similarity(101, 131, 192) > 
	                originalTester.similarity(101, 131, 192));
	    
	    
	}
	
	@Test
	public void testProcessIconCaching() {
	    // Generate a base image (for convenience)
	    ImageIcon original = MCTIcons.generateIcon(0x1982, 16, Color.WHITE);
	    
	    // Processing it twice should give the same (pointer-identical) image
	    ImageIcon processed = MCTIcons.processIcon(original);
	    Assert.assertSame(MCTIcons.processIcon(original), processed);
	    
	    // Should also avoid re-processing icons
	    Assert.assertSame(MCTIcons.processIcon(processed), processed);  
	    
	    // A processed null should just be a null
	    Assert.assertNull(MCTIcons.processIcon(null));
	    
	    // Should not use default cache with processing instructions
	    ImageIcon colorized = MCTIcons.processIcon(original, Color.MAGENTA, true);
	    Assert.assertNotSame(colorized, processed);
	    
	    // Should cache for specific processing instructions
	    Assert.assertSame(MCTIcons.processIcon(original, Color.MAGENTA, true), colorized);
	    
	    // Should not use cache if processing instructions change
        Assert.assertNotSame(MCTIcons.processIcon(original, Color.MAGENTA, false), colorized);
        Assert.assertNotSame(MCTIcons.processIcon(original, Color.ORANGE, true), colorized);
        
        // Should also distinguish different gradient choices (and recognize same ones)
        ImageIcon gradient = MCTIcons.processIcon(original, Color.MAGENTA, Color.YELLOW, true);
        Assert.assertNotSame(gradient, colorized);
        Assert.assertSame(MCTIcons.processIcon(original, Color.MAGENTA, Color.YELLOW, true), gradient);
	}
	
	private static class IconTester {
	    private int hash;
	    private BufferedImage image;
	    
	    public IconTester(Icon icon) {
	        image = new BufferedImage(icon.getIconHeight(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
	        icon.paintIcon(null, image.createGraphics(), 0, 0);
	        
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
	        if (o instanceof IconTester) {
	            IconTester tester = (IconTester) o;
	            
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
	    
	    // Assess similarity to a given color
	    public float similarity(int r, int g, int b) {
	        int expected[] = { r , g , b };
	        int k = 0;
	        float similarity = 0;
	        for (int x = 0; x < image.getWidth(); x++) {
	            for (int y = 0; y < image.getHeight(); y++) {	                
	                // Get image r, g, b
	                int rgb = image.getRGB(x, y);
	                int actual[] = {0,0,0};
	                for (int i = 0; i < 3; i++) {
	                    actual[i] = rgb & 0xFF;
	                    rgb >>= 8;
	                }
	                // Compare with existing
	                for (int i = 0; i < 3; i++) {
	                    similarity += 255 - Math.abs(actual[i] - expected[i]);
	                    k++;
	                }
	            }
	        }
	        return similarity / (float) k;
	    }
	}
}
