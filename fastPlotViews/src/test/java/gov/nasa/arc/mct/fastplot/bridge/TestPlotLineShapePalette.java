package gov.nasa.arc.mct.fastplot.bridge;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;


public class TestPlotLineShapePalette {
	// The number of samples to be taken along each axis
	private static final double RESOLUTION = 250.0;
	
	// The number of distinct angles (radial slices) to consider
	private static final int    ANGLES     = 8;
	
	// The ratio at which two slices are considered similar
	private static final double SIMILAR    = 0.75;
	
	
	
	@Test
	public void testShapeCount() {
		Assert.assertTrue(PlotLineShapePalette.getShapeCount() >= PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT);
		try {
			PlotLineShapePalette.getShape(PlotLineShapePalette.getShapeCount() + 1);
			Assert.fail("Expected to hit IllegalArgumentException for excess shapes");
		} catch (IllegalArgumentException e) {
			// Expected!
		}
	}
		
	
	/*
	 * To ensure that shapes are visually unique, shapes are compared using a similarity metric.
	 * The shape is considered along ANGLES directions (up, up-right, right, etc) relative to its center of mass. 
	 * Samples are taken and assigned to their nearest slice to accumulate counts for each slice.
	 * Shapes are considered dissimilar if the ratio between corresponding slices is less than SIMILAR.
	 * Note that all shapes are sampled at the same resolution, effectively normalizing the accumulated counts 
	 * (making them directly comparable between shapes).
	 */
	
	@Test
	public void testShapeUniqueness() {
		int count = PlotLineShapePalette.getShapeCount();
		List<ShapeProfile> profiles = new ArrayList<ShapeProfile>();
		
		for (int i = 0; i < count; i++) {
			profiles.add(new ShapeProfile(PlotLineShapePalette.getShape(i)));
		}
		
		// Try all combinations to ensure all shapes are distinct
		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				Assert.assertFalse(same(profiles.get(i), profiles.get(j)));
			}
		}
	}
	
	private boolean same(ShapeProfile a, ShapeProfile b) {
		for (int i = 0; i < ANGLES; i++) {
			double low  = Math.min(a.concentration[i], b.concentration[i]);
			double high = Math.max(a.concentration[i], b.concentration[i]);
			if (low / high < SIMILAR) {
				return false; // Dissimilar within some slice of interest
			}
		}
		return true;
	}
	
	private static class ShapeProfile {
		private double xMin;
		private double xMax;
		private double yMin;
		private double yMax;
		private double xAvg;
		private double yAvg;
		
		private int    concentration[] = new int[ANGLES];
		
		public ShapeProfile(Shape shape) {
			for (int i = 0; i < ANGLES; i++) concentration[i] = 0; // Initialize
			
			// These may not be realistic min and max bounds
			double x1 = shape.getBounds().getMinX();
			double y1 = shape.getBounds().getMinY();
			double x2 = shape.getBounds().getMaxX();
			double y2 = shape.getBounds().getMaxY();
			
			double xSum  = 0.0;
			double ySum  = 0.0;
			double count = 0.0;
			
			// Find real min, max, and average
			xMin = x2;
			xMax = x1;
			yMin = y2;
			yMax = y1;
			for (double x = x1; x < x2; x += (x2-x1)/RESOLUTION) {
				for (double y = y1; y < y2; y += (y2-y1)/RESOLUTION) {
					if (shape.contains(x, y)) {
						xMin  = Math.min(x, xMin);
						yMin  = Math.min(y, yMin);
						xMax  = Math.max(x, xMax);
						yMax  = Math.max(y, yMax);
						xSum  += x;
						ySum  += y;
						count += 1.0;
					}
				}
			}			
			
			// Center of mass
			xAvg = xSum / count;
			yAvg = ySum / count;
			
			// Another pass to accumulate the density at various angles
			for (double x = xMin; x < xMax; x += (xMax-xMin)/RESOLUTION) {
				for (double y = yMin; y < yMax; y += (yMax-yMin)/RESOLUTION) {
					if (shape.contains(x, y)) {
						if (x != xAvg || y != yAvg) {
							double radians   = Math.atan2(y-yAvg,x-xAvg);
							double rotations = radians / (Math.PI * 2);
							while (rotations < 0) rotations += 1.0; //No negatives!
							int    slice = ((int) Math.round(rotations * ANGLES)) % ANGLES;
							concentration[slice]++;
						}
					}
				}
			}

		}
	}
}
