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
package gov.nasa.arc.mct.graphics.view;

import java.awt.image.BufferedImage;
import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SVGRasterizerTest {
	private static final String CHECKER  = new File("src/test/resources/checker.svg").toURI().toString();
	private static final String BAD_FILE = "src/test/resources/NONEXISTANT.svg";
	
	private static final int    MAXIMUM_WAIT = 5000;
	
	
	@Test
	public void testDocumentFailure() throws Exception {
		SVGRasterizer rasterizer = new SVGRasterizer(BAD_FILE);
		Assert.assertNull(rasterizer.getLatestImage());
		
		waitForLoad(rasterizer);
		
		Assert.assertTrue(rasterizer.hasFailed());
	}
	
	@Test
	public void testDocumentLoading() throws Exception {
		SVGRasterizer rasterizer = new SVGRasterizer(CHECKER);
		Assert.assertNull(rasterizer.getLatestImage());		
		waitForLoad(rasterizer);
		
		Assert.assertTrue(rasterizer.isLoaded());
	}
	
	@Test
	public void testDocumentRendering() throws Exception {
		SVGRasterizer rasterizer = new SVGRasterizer(CHECKER);
		
		Assert.assertNull(rasterizer.getLatestImage());
		rasterizer.requestRender(100,  100 );
		waitForCurrent(rasterizer);
		Assert.assertEquals(rasterizer.getLatestImage().getWidth(), 100);
		Assert.assertEquals(rasterizer.getLatestImage().getHeight(), 100);
		checkForCheckers(rasterizer.getLatestImage());
		
		BufferedImage stale;
		synchronized (rasterizer) {
			rasterizer.requestRender(1000, 1000);
			Assert.assertFalse(rasterizer.isCurrent()); // Not current...
			Assert.assertTrue(rasterizer.isRendered()); // ...but has something.
			stale = rasterizer.getLatestImage();			
			Assert.assertTrue(stale.getWidth() == 100);
		}
		checkForCheckers(stale);
		
		waitForCurrent(rasterizer);
		Assert.assertEquals(rasterizer.getLatestImage().getWidth(), 1000);
		Assert.assertEquals(rasterizer.getLatestImage().getHeight(), 1000);
		checkForCheckers(rasterizer.getLatestImage());	
		
		/* Using the same value shouldn't require a new render */
		rasterizer.requestRender(1000, 1000);
		Assert.assertTrue(rasterizer.isCurrent());
	}
		
	private boolean calledBack;
	
	@Test
	public void testCallback() throws Exception {
		calledBack = false;		
		SVGRasterizer rasterizer = new SVGRasterizer(CHECKER);
		rasterizer.setCallback(new Runnable() {
			public void run() { calledBack = true; }
		});
		
		// Instantiation doesn't call back
		Assert.assertFalse(calledBack);
		
		// Loading doesn't call back
		waitForLoad(rasterizer);
		Assert.assertFalse(calledBack);
		
		// Rendering does call back
		rasterizer.requestRender(10, 10);
		waitForCurrent(rasterizer);
		Assert.assertTrue(calledBack);		
	}
	
	private void checkForCheckers(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		
		// Start at 2 to give a couple of pixels of leeway
		for (int x = 2; x < w / 2 - 2; x++) {
			for (int y = 2; y < h / 2 - 2; y++) {
				/* Test for checkerboard pattern, or similar */
				
				// Get colors from four quadrants
				int[][] rgb = new int[2][2];
				rgb[0][0] = img.getRGB(  x,   y);
				rgb[0][1] = img.getRGB(  x, h-y);
				rgb[1][0] = img.getRGB(w-x,   y);
				rgb[1][1] = img.getRGB(w-x, h-y);
				
				//Not reflected across x	
				Assert.assertFalse(rgb[0][0] == rgb[1][0]);
				Assert.assertFalse(rgb[0][1] == rgb[1][1]);
				
				//Not reflected across y
				Assert.assertFalse(rgb[0][0] == rgb[0][1]);
				Assert.assertFalse(rgb[1][0] == rgb[1][1]);
				
				//But reflected diagonally
				Assert.assertTrue (rgb[0][0] == rgb[1][1]);
				Assert.assertTrue (rgb[1][0] == rgb[0][1]);
				
			}
		}
	}
	
	
	private void waitForLoad(SVGRasterizer rasterizer) throws Exception {
		int i = 0;
		while (rasterizer.isLoading() && i < MAXIMUM_WAIT) {
			Thread.sleep(100);
			i += 100;
		}
		Assert.assertTrue(i < MAXIMUM_WAIT); // Otherwise we timed out!
	}
	
	private void waitForCurrent(SVGRasterizer rasterizer) throws Exception {
		int i = 0;
		while (!rasterizer.isCurrent() && i < MAXIMUM_WAIT) {
			Thread.sleep(100);
			i += 100;
		}
		Assert.assertTrue(i < MAXIMUM_WAIT); // Otherwise we timed out!
	}	
}
