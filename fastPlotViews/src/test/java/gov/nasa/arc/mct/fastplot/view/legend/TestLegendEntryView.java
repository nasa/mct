package gov.nasa.arc.mct.fastplot.view.legend;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotLine;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.ComponentDragAndDrop;
import org.fest.swing.core.Robot;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestLegendEntryView {
	private static final String TEST_ID = "test:id";
	private static final String TEST_NAME = "Test component";
	private static final String OTHER_NAME = "Supplied name";
	private static final Color  TEST_COLOR = Color.PINK;
	
	@Mock AbstractComponent comp;
	@Mock FeedProvider fp;
	@Mock RenderingInfo ri;

	LegendEntryView view;
	JFrame f;
	
	
	@BeforeClass
	public void setupFrame() {
		f = new JFrame();
		f.setVisible(true);			
	}
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(comp.getDisplayName()).thenReturn(TEST_NAME);
		Mockito.when(comp.getCapability(FeedProvider.class)).thenReturn(fp);
		Mockito.when(fp.getRenderingInfo(Mockito.anyMap())).thenReturn(ri);
		Mockito.when(fp.getSubscriptionId()).thenReturn(TEST_ID);
		Mockito.when(ri.getStatusText()).thenReturn("");
		Mockito.when(ri.isPlottable()).thenReturn(true);
		view = new LegendEntryView(comp, LegendEntryView.VIEW_INFO);
		
		f.getContentPane().removeAll();
		f.getContentPane().add(view);
		f.pack();	
	}
	
	@AfterClass
	public void tearDown() {
		f.setVisible(false);
		f.dispose();
	}
	
	@Test
	public void testComponentName() {
		Assert.assertEquals(view.getDisplayedName(), TEST_NAME);
		
		view.setBaseDisplayName(OTHER_NAME);
		
		Assert.assertEquals(view.getDisplayedName(), OTHER_NAME);
	}
	
	@Test
	public void testSetAppearanceColor() {
		Assert.assertFalse(containsColor(drawComponent(view), TEST_COLOR));		
		view.setAppearance(TEST_COLOR, null);
		Assert.assertTrue (containsColor(drawComponent(view), TEST_COLOR));
	}
	
	@Test
	public void testAttachPlotLine() {
		// Attaching a plot line should update the color of the legend to match
		AbstractPlotLine mockPlotLine = Mockito.mock(AbstractPlotLine.class);
		Mockito.when(mockPlotLine.getColor()).thenReturn(TEST_COLOR);		
		Assert.assertFalse(containsColor(drawComponent(view), TEST_COLOR));		
		view.attachPlotLine(mockPlotLine);
		Assert.assertTrue (containsColor(drawComponent(view), TEST_COLOR));
	}
	
	@Test
	public void testShowsStatus() {
		// Draw the same view with multiple rendering infos; confirm they are all drawn uniquely 
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		
		Map<String, List<Map<String,String>>> data = new HashMap<String,List<Map<String,String>>>();
		List<Map<String,String>> points = new ArrayList<Map<String,String>>();
		points.add(new HashMap<String,String>()); // Rendering info, which is mocked, should decode this, so empty map should be fine
		data.put(TEST_ID, points);		
				
		for (boolean plottable : new boolean[]{true,false}) {
			for (String status : new String[]{"", "A", "B"}) {
				Mockito.when(ri.isPlottable()).thenReturn(plottable);
				Mockito.when(ri.getStatusText()).thenReturn(status);
				view.updateFromFeed(data);
				images.add(drawComponent(view));
			}
		}
		
		for (int i = 0; i < images.size(); i++) {
			for (int j = i + 1; j < images.size(); j++) {
				Assert.assertFalse(imagesEqual(images.get(i), images.get(j)));
			}
		}
		
		Assert.assertTrue(imagesEqual(images.get(0), images.get(0)));		
	}
	
	@Test
	public void testVisibleFeedProviders() {
		Assert.assertTrue(view.getVisibleFeedProviders().contains(fp));
	}
	
	@Test
	public void testDragAndDrop() {
		TestDropTarget dt = new TestDropTarget();
		
		f.getContentPane().removeAll();
				
		JPanel dest = new JPanel(); // Drop target
		dest.setBackground(Color.YELLOW);
		dest.setDropTarget(dt);
		JPanel p = new JPanel(new BorderLayout());
		p.add(view, BorderLayout.NORTH);
		p.add(dest, BorderLayout.CENTER);
		f.getContentPane().add(p);
		f.pack();
		
		Assert.assertNull(dt.v); // Make sure we're starting clean
		
		Robot robot = BasicRobot.robotWithCurrentAwtHierarchy();
		
		ComponentDragAndDrop d = new ComponentDragAndDrop(robot);
				
		d.drag(view, new Point(5,5));
		d.drop(view, new Point(5,5));
		
		Assert.assertNull(dt.v); // Nothing should have happened
		
		d.drag(view, new Point(5,5));
		d.drop(dest, new Point(5,5));		
		
		Assert.assertEquals(dt.v, view); // Make sure the right view got dropped
	}
	
	
	// Test disabled pending alternative approach (open is final, can't be mocked)
	@Test (enabled = false)
	public void testDoubleClick() {
		Robot robot = BasicRobot.robotWithCurrentAwtHierarchy();
		
		Mockito.verify(comp, Mockito.never()).open();
		robot.doubleClick(view);
		Mockito.verify(comp, Mockito.times(1)).open();		
	}
	
	@Test (enabled = false)
	public void testSetAppearanceIcon() {
		
	}
	
	private BufferedImage drawComponent(JComponent comp) {
		BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);		
		comp.paint(img.createGraphics());		
		return img;
	}
	
	private boolean imagesEqual(BufferedImage a, BufferedImage b) {
		if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
			return false;
		}
		
		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
			}
		}
		
		return true;		
	}

	private boolean containsColor(BufferedImage im, Color c) {
		for (int y = 0; y < im.getHeight(); y++) {
			for (int x = 0; x < im.getWidth(); x++) {
				int rgb = im.getRGB(x, y);
				int r   = ((rgb & 0xFF0000) >> 16) - c.getRed();
			    int g   = ((rgb & 0x00FF00) >>  8) - c.getGreen();
			    int b   = ((rgb & 0x0000FF) >>  0) - c.getBlue();
			    if (r <  4 && g <  4 && b <  4 && 
			        r > -4 && g > -4 && b > -4) { // Give some leeway, since font may be antialiased
			    	return true;
			    }
			}
		}
		return false;
	}
	
	private static class TestDropTarget extends DropTarget {
		private static final long serialVersionUID = -7167420468257291174L;

		public View v = null;
		
		public void drop (DropTargetDropEvent event) {
			if (event.getTransferable().isDataFlavorSupported(View.DATA_FLAVOR)) {
				try {
					for (View view : (View[]) event.getTransferable().getTransferData(View.DATA_FLAVOR)) {
						v = view;
						break; // Only take the first!
					}
				} catch (UnsupportedFlavorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
