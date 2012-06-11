package plotter;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;


/**
 * Factory for common data point marker shapes.
 * Each takes a <code>scale</code> parameter and fits the shape within a square bounding box with side length of <code>2 * scale</code>.
 * @author Adam Crume
 */
public class Shapes {
	/**
	 * Creates a square with side length <code>2 * scale</code>.
	 * @param scale scale of the shape
	 * @return square with side length <code>2 * scale</code>
	 */
	public static Shape square(double scale) {
		GeneralPath shape = new GeneralPath();
		shape.moveTo(-scale, -scale);
		shape.lineTo(-scale, scale);
		shape.lineTo(scale, scale);
		shape.lineTo(scale, -scale);
		shape.lineTo(-scale, -scale);
		return shape;
	}


	/**
	 * Creates a circle with radius <code>scale</code>.
	 * @param scale scale of the shape
	 * @return circle with radius <code>scale</code>
	 */
	public static Shape circle(double scale) {
		return new Ellipse2D.Double(-scale, -scale, scale * 2, scale * 2);
	}


	/**
	 * Creates an upward-pointing triangle.
	 * @param scale scale of the shape
	 * @return upward-pointing triangle
	 */
	public static Shape triangleUp(double scale) {
		GeneralPath shape = new GeneralPath();
		shape.moveTo(0, -scale);
		double alpha = -Math.PI / 6;
		double y = -scale * Math.sin(alpha);
		double x = scale * Math.cos(alpha);
		shape.lineTo(x, y);
		shape.lineTo(-x, y);
		shape.lineTo(0, -scale);
		return shape;
	}


	/**
	 * Creates an downward-pointing triangle.
	 * @param scale scale of the shape
	 * @return downward-pointing triangle
	 */
	public static Shape triangleDown(double scale) {
		GeneralPath shape = new GeneralPath();
		shape.moveTo(0, scale);
		double alpha = -Math.PI / 6;
		double y = scale * Math.sin(alpha);
		double x = scale * Math.cos(alpha);
		shape.lineTo(x, y);
		shape.lineTo(-x, y);
		shape.lineTo(0, scale);
		return shape;
	}


	/**
	 * Creates a space shuttle.
	 * @param scale scale of the shape
	 * @return space shuttle
	 */
	public static Shape shuttle(double scale) {
		double engineX = .15;
		double engineY = .82;
		double engineY2 = .2;
		double wingX = .69;
		double wingY = engineY - .1;
		double wing2X = .91 * wingX;
		double wing2Y = wingY - .24;
		double wing3X = .45 * wing2X;
		double wing3Y = .15;
		double fuselageX = .15;
		double cockpitY = -.6;
		GeneralPath shape = new GeneralPath();

		// engine
		shape.moveTo(-engineX, 1);
		shape.lineTo(engineX, 1);
		shape.lineTo(engineY2, engineY);

		// right wing
		shape.lineTo(wingX, wingY);
		shape.lineTo(wing2X, wing2Y);
		shape.lineTo(wing3X, wing3Y);
		shape.lineTo(fuselageX, cockpitY);

		// cockpit
		shape.curveTo(fuselageX, cockpitY - .2, .05, -1, 0, -1);
		shape.curveTo(-.05, -1, -fuselageX, cockpitY - .2, -fuselageX, cockpitY);

		// left wing
		shape.lineTo(-wing3X, wing3Y);
		shape.lineTo(-wing2X, wing2Y);
		shape.lineTo(-wingX, wingY);

		// engine
		shape.lineTo(-engineY2, engineY);
		shape.lineTo(-engineX, 1);
		shape.transform(AffineTransform.getScaleInstance(scale, scale));
		return shape;
	}


	/**
	 * Creates an asterisk.
	 * @param scale scale of the shape
	 * @return asterisk
	 */
	public static Shape star(double scale) {
		GeneralPath shape = new GeneralPath();
		for(int i = 0; i < 6; i++) {
			shape.moveTo(0, 0);
			shape.lineTo(scale * Math.cos(i * Math.PI / 3), scale * Math.sin(i * Math.PI / 3));
		}
		return shape;
	}


	/**
	 * Creates a plus or cross.
	 * @param scale scale of the shape
	 * @return plus
	 */
	public static Shape plus(double scale) {
		GeneralPath shape = new GeneralPath();
		for(int i = 0; i < 4; i++) {
			shape.moveTo(0, 0);
			shape.lineTo(scale * Math.cos(i * Math.PI / 2), scale * Math.sin(i * Math.PI / 2));
		}
		return shape;
	}


	/**
	 * Creates an 'x'.
	 * @param scale scale of the shape
	 * @return 'x' shape
	 */
	public static Shape x(double scale) {
		GeneralPath shape = new GeneralPath();
		for(int i = 0; i < 4; i++) {
			shape.moveTo(0, 0);
			double alpha = i * Math.PI / 2 + Math.PI / 4;
			shape.lineTo(scale * Math.cos(alpha), scale * Math.sin(alpha));
		}
		return shape;
	}
}
