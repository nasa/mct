package gov.nasa.arc.mct.fastplot.util;

import gov.nasa.arc.mct.fastplot.utils.MenuItemSpinner;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestMenuItemSpinner {	
	MenuItemSpinner spinner;
	JFrame f;
	JPopupMenu menu;	
	JMenu spinnerMenu;
	Robot robot;
	
	@BeforeClass
	public void setupFrame() {
		f = new JFrame();
		f.setVisible(true);			
	}
	
	@BeforeMethod
	public void setup() throws InterruptedException, InvocationTargetException {
		menu = new JPopupMenu();
		spinnerMenu = new JMenu("Test");
		spinner = new MenuItemSpinner(new SpinnerNumberModel(5,1,10,1), spinnerMenu);
		spinnerMenu.add(spinner);
		menu.add(spinnerMenu);
		
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				JLabel label = new JLabel("label");
				f.getContentPane().removeAll();
				f.getContentPane().add(label);
				f.pack();	
				menu.show(label, 10, 10);
			}
		});
		
		robot = BasicRobot.robotWithCurrentAwtHierarchy();
	}
	
	@AfterMethod
	public void hideMenu() {
		spinnerMenu.setVisible(false);
		menu.setVisible(false);
		robot.cleanUpWithoutDisposingWindows();
	}
	
	@AfterClass
	public void tearDown() {
		f.setVisible(false);
		f.dispose();
	}
	
	
	@Test
	public void testAppearsInSubMenu() {
		robot.click(spinnerMenu);
		robot.click(spinner); // Will cause exception if spinner isn't visible
	}
	
	
	@Test
	public void testTyping() {
		robot.click(spinnerMenu);
		robot.click(spinner);
		
		// Should still be 5
		Assert.assertEquals(((Integer)spinner.getValue()).intValue(), 5);
		
		// Changing to two and hitting enter should update value
		robot.click(spinner);
		robot.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE, '2', KeyEvent.VK_ENTER);
		Assert.assertEquals(((Integer)spinner.getValue()).intValue(), 2);

		// Empty box should be reset
		robot.click(spinner);
		robot.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE, KeyEvent.VK_ENTER);
		Assert.assertEquals(((Integer)spinner.getValue()).intValue(), 2);		
	}
	
	@Test
	public void testTabOut() {
		robot.click(spinnerMenu);
		robot.click(spinner);
		// Should still be 5
		Assert.assertEquals(((Integer)spinner.getValue()).intValue(), 5);
		robot.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE, '2', KeyEvent.VK_TAB);
		Assert.assertEquals(((Integer)spinner.getValue()).intValue(), 2);
	}
}
