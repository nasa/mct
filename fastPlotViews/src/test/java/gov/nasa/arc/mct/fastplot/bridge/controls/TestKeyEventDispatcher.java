package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.PlotAbstraction;
import gov.nasa.arc.mct.fastplot.bridge.PlotViewActionListener;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JPanel;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestKeyEventDispatcher {


	@Mock PlotAbstraction abstraction;
	@Mock AbstractPlottingPackage plot;
	@Mock PlotViewActionListener listener;
	@Mock AbstractPlotLocalControlsManager controlManager;
	
	@BeforeTest
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(abstraction.getSubPlots()).thenReturn(Arrays.asList(plot));
		Mockito.when(plot.getPlotActionListener()).thenReturn(listener);
		Mockito.when(plot.getLocalControlsManager()).thenReturn(controlManager);
		Mockito.when(listener.isMouseOutsideOfPlotArea()).thenReturn(false);
	}
	
	@Test
	public void testKeyEventDispatcher() {
		JPanel p = new JPanel();
		KeyEventDispatcher d = new LocalControlKeyEventDispatcher(abstraction);
		
		Mockito.when(listener.isMouseOutsideOfPlotArea()).thenReturn(true);		
		KeyEvent ctrlDown = new KeyEvent(p, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL,
				KeyEvent.CHAR_UNDEFINED);
		KeyEvent ctrlUp = new KeyEvent(p, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_CONTROL,
				KeyEvent.CHAR_UNDEFINED);
		
		// Pressing when mouse is not over should do nothing
		d.dispatchKeyEvent(ctrlDown);	
		Mockito.verifyZeroInteractions(controlManager);
		
		// Now, try it with mouse in the plot area
		Mockito.when(listener.isMouseOutsideOfPlotArea()).thenReturn(false);		
		
		// Now, it should inform the control manager of the key press
		d.dispatchKeyEvent(ctrlDown);
		Mockito.verify(controlManager, Mockito.atLeastOnce()).informKeyState(KeyEvent.VK_CONTROL, true);
		d.dispatchKeyEvent(ctrlUp);
		Mockito.verify(controlManager, Mockito.atLeastOnce()).informKeyState(KeyEvent.VK_CONTROL, false);
	}
	
}
