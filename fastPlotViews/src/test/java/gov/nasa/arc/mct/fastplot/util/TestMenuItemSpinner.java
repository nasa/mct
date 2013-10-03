package gov.nasa.arc.mct.fastplot.util;

import gov.nasa.arc.mct.fastplot.utils.MenuItemSpinner;

import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestMenuItemSpinner {	
	MenuItemSpinner spinner;
	JTextField spinnerField;
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
			
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				menu = new JPopupMenu();
				spinnerMenu = new JMenu("Test");
				spinner = new MenuItemSpinner(new SpinnerNumberModel(5,1,10,1), spinnerMenu);
				spinnerField = ((NumberEditor)spinner.getEditor()).getTextField();
				spinnerMenu.add(spinner);
				menu.add(spinnerMenu);

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
		robot.click(spinnerField); // Will cause exception if spinner isn't visible
	}
	
	
	@Test
	public void testTyping() {
		robot.click(spinnerMenu);
		robot.click(spinnerField);
		
		// Should still be 5
		Assert.assertEquals(getSpinnerValue(), 5);
		
		// Changing to two and hitting enter should update value
		robot.click(spinnerField);
		robot.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE, '2', KeyEvent.VK_ENTER);
		Assert.assertEquals(getSpinnerValue(), 2);

		// Empty box should be reset
		robot.click(spinnerField);
		robot.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE, KeyEvent.VK_ENTER);
		Assert.assertEquals(getSpinnerValue(), 2);		
	}
	
	@Test
	public void testTabOut() {
		robot.click(spinnerMenu);
		robot.click(spinnerField);
		// Should still be 5
		Assert.assertEquals(getSpinnerValue(), 5);
		robot.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE, '2', KeyEvent.VK_TAB);
		Assert.assertEquals(getSpinnerValue(), 2);
	}
	
	// Retrieve spinner value from EDT to avoid intermittent test failures.
	private int getSpinnerValue() {
		robot.waitForIdle();
		Integer i = GuiActionRunner.execute(new GuiQuery<Integer>() {
			@Override
			protected Integer executeInEDT() throws Throwable {
				return (Integer) spinner.getValue();
			}			
		});
		robot.waitForIdle();
		return i;
	}
}
