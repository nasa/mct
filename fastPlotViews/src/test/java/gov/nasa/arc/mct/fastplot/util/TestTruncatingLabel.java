package gov.nasa.arc.mct.fastplot.util;

import gov.nasa.arc.mct.fastplot.utils.TruncatingLabel;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTruncatingLabel {
	private int width = 10;
	
	private static final String TEXT = "This is some text.";
	
	@Test
	public void testPaint() {
		JLabel truncatingLabel = new TruncatingLabel();
		JLabel plainLabel      = new JLabel();
		
		truncatingLabel.setText(TEXT);
		plainLabel.setText(TEXT);
		
    @SuppressWarnings("serial")//Harleigh108: this removes the warning received when we build with respect to java 7:for JPanel
		JPanel testPanel = new JPanel() {
			public int getWidth() {
				return width;
			}
		};

		testPanel.add(truncatingLabel);
		testPanel.add(plainLabel);
		
		// We need a JFrame to make the panel do any layout
		JFrame testFrame = new JFrame();
		testFrame.getContentPane().add(testPanel);
		testFrame.pack();
		
		width = 1000; // Plenty of room, should draw the same
		Assert.assertEquals(true,  matches( draw(truncatingLabel), draw(plainLabel) ));		
		
		width = 20;   // Very narrow, should truncate
		Assert.assertEquals(false, matches( draw(truncatingLabel), draw(plainLabel) ));

	}
	
	private boolean matches (BufferedImage a, BufferedImage b) {
		if (a.getWidth () != b.getWidth ()) throw new IllegalArgumentException("Images must have same dimensions");
		if (a.getHeight() != b.getHeight()) throw new IllegalArgumentException("Images must have same dimensions");
		
		for (int x = 0; x < a.getWidth(); x++) {
			for (int y = 0; y < a.getHeight(); y++) {
				if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
			}
		}
		
		return true;
	}
	
	private BufferedImage draw(JLabel label) {
		BufferedImage image = new BufferedImage(label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		label.paint(g);		
		return image;
	}
	
}
