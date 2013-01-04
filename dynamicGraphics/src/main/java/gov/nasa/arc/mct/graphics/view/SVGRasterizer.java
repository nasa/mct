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

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.swing.gvt.GVTTreeRenderer;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.svg.GVTTreeBuilder;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.apache.batik.swing.svg.SVGDocumentLoader;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.w3c.dom.svg.SVGDocument;


/**
 * An SVGRasterizer renders SVG graphics to BufferedImages at 
 * requested resolutions (does not preserve aspect ratio)
 * 
 * @author vwoeltje
 *
 */
public class SVGRasterizer {
	private Dimension nextRenderingSize = null;
	private Dimension lastRenderingSize = null;
	
	private SVGDocument     svgDocument = null;
	private BufferedImage   latestImage = null;	
	
	private GraphicsNode    graphicsNode = null;
	
	private Runnable        callback     = null;
	
	private boolean isLoading = true;

	
	/**
	 * Create a new rasterizer to convert an SVG document 
	 * to BufferedImages, for display on a specific component
	 * Note that loading/parsing and rendering are both done 
	 * asynchronously.
	 * @param documentURL the URL of the document to rasterize

	 */
	public SVGRasterizer(String documentURL) {
		UserAgent userAgent = new UserAgentAdapter();
		SVGDocumentLoader loader = new SVGDocumentLoader(documentURL,
				new DocumentLoader(userAgent));		
		loader.addSVGDocumentLoaderListener(new SVGRasterizerListener());
		/* Note that Batik uses the context class loader for loading SAX Parser, etc. 
		 * Context class loader is inappropriate under the Felix implementation of OSGi, 
		 * so replace it with the regular class loader. */
		loader.setContextClassLoader(loader.getClass().getClassLoader());
		loader.start();
	}
	
	/**
	 * Inject behavior to be called when rendering is complete 
	 * @param callback the behavior to run after rendering
	 */
	public void setCallback(Runnable callback) {
		this.callback = callback;
	}

	/**
	 * Request a raster image of the loaded SVG document, 
	 * at a specified width and height.
	 * Note that rendering will be done asynchronously; 
	 * subsequent calls to getLatestImage() will return 
	 * the rendered image once complete (and the previously 
	 * rendered image until then)
	 * @param width the width in pixels at which to render
	 * @param height the height in pixels at which to render
	 */
	public void requestRender(int width, int height) {
		width = (width < 1) ? 1 : width;
		height = (height < 1) ? 1 : height;
		if (latestImage != null &&
			width  == latestImage.getWidth() && 
			height == latestImage.getHeight()) return;
		nextRenderingSize = new Dimension (width, height);
		renderIfReady();		
	}
	
	/**
	 * Get the most recently rendered raster image of this 
	 * SVG document. This may be null if no renders have 
	 * completed, and may not match the size of the last  
	 * rendering request if rendering is incomplete.
	 * @return
	 */
	public BufferedImage getLatestImage() {
		return latestImage;
	}
	
	/**
	 * Check to see if document loading failed
	 * @return true if the document could not be loaded/parsed;
	 *         false if it was successful, or is still loading
	 */
	public boolean hasFailed() {
		return graphicsNode == null && !isLoading;
	}

	/**
	 * Check to see if the document has been loaded & parsed
	 * @return true if loaded; otherwise false
	 */
	public boolean isLoaded() {
		return graphicsNode != null;
	}
	
	/**
	 * Check to see if any rendering operation has completed 
	 * (not necessarily the most recently requested one)
	 * @return true if an image is available; false if not
	 */
	public boolean isRendered() {
		return latestImage != null;
	}
	
	/**
	 * Check to see if all rendering options have completed	
	 * @return true if an up-to-date image is available
	 */
	public boolean isCurrent() {
		return latestImage != null && 
		       nextRenderingSize == null &&
		       lastRenderingSize == null;
	}
	
	/**
	 * Check to see if the document is in the process of loading 
	 * & parsing.
	 * @return true if loading & parsing; otherwise false
	 */
	public boolean isLoading() {
		return isLoading;
	}
	
	
	private synchronized void renderIfReady() {
		if (graphicsNode == null) return;
		if (nextRenderingSize == null) return;
		if (lastRenderingSize != null) return; // Still waiting

		Dimension size = nextRenderingSize;
		Rectangle2D bounds = graphicsNode.getPrimitiveBounds();
		lastRenderingSize = nextRenderingSize;
		nextRenderingSize = null;
		// start renderer...
		double widthScale = size.getWidth() / bounds.getWidth();
		double heightScale = size.getHeight() / bounds.getHeight();

		AffineTransform renderTransform = new AffineTransform();		
		renderTransform.scale(widthScale, heightScale);
		renderTransform.translate(-bounds.getMinX(), -bounds.getMinY());	
		
		ImageRenderer renderer     = new ConcreteImageRendererFactory().createStaticImageRenderer();
		renderer.setTree(graphicsNode);		
		GVTTreeRenderer gvtRenderer = new GVTTreeRenderer(
				renderer, 
				renderTransform, 
				true, 
				bounds, 
				size.width, size.height);
		
		gvtRenderer.addGVTTreeRendererListener(new SVGRasterizerListener());
		gvtRenderer.setContextClassLoader(gvtRenderer.getClass().getClassLoader());
		gvtRenderer.start();
	}

	private class SVGRasterizerListener implements  SVGDocumentLoaderListener,
													GVTTreeBuilderListener,
													GVTTreeRendererListener {
		/* SVGDocumentLoaderListener */
		@Override
		public void documentLoadingCancelled(SVGDocumentLoaderEvent arg0) {
			isLoading = false;
			if (callback != null) callback.run();
		}

		@Override
		public void documentLoadingCompleted(SVGDocumentLoaderEvent arg0) {
			svgDocument = arg0.getSVGDocument();
			GVTTreeBuilder gvtBuilder;
			gvtBuilder = new GVTTreeBuilder(svgDocument, new BridgeContext(new UserAgentAdapter()));
			gvtBuilder.addGVTTreeBuilderListener(this);
			gvtBuilder.setContextClassLoader(gvtBuilder.getClass().getClassLoader());
			gvtBuilder.start();		
		}

		@Override
		public void documentLoadingFailed(SVGDocumentLoaderEvent arg0) {	
			isLoading = false;
			if (callback != null) callback.run();
		}

		@Override
		public void documentLoadingStarted(SVGDocumentLoaderEvent arg0) {		
		}


		/* GVTTreeBuilderListener */
		@Override
		public void gvtBuildCancelled(GVTTreeBuilderEvent arg0) {
			isLoading = false;
			if (callback != null) callback.run();
		}

		@Override
		public void gvtBuildCompleted(GVTTreeBuilderEvent arg0) {
			graphicsNode = arg0.getGVTRoot();		
			renderIfReady();
			isLoading = false;
		}

		@Override
		public void gvtBuildFailed(GVTTreeBuilderEvent arg0) {
			isLoading = false;
			if (callback != null) callback.run();
		}

		@Override
		public void gvtBuildStarted(GVTTreeBuilderEvent arg0) {
		}


		/* GVTTreeRendererListener */
		@Override
		public void gvtRenderingCancelled(GVTTreeRendererEvent arg0) {
		}

		@Override
		public synchronized void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {

			BufferedImage img = new BufferedImage(lastRenderingSize.width, 
					lastRenderingSize.height, BufferedImage.TYPE_INT_ARGB);
			img.getGraphics().drawImage(arg0.getImage(),
					0, 0, lastRenderingSize.width, lastRenderingSize.height,
					0, 0, lastRenderingSize.width, lastRenderingSize.height,
					null);		
			latestImage = img;		
			lastRenderingSize = null;		 

			if (callback != null) callback.run();

		}

		@Override
		public void gvtRenderingFailed(GVTTreeRendererEvent arg0) {
			if (callback != null) callback.run();
		}

		@Override
		public void gvtRenderingPrepare(GVTTreeRendererEvent arg0) {
		}

		@Override
		public void gvtRenderingStarted(GVTTreeRendererEvent arg0) {
		}
	}
	
	
	
}
