package gov.nasa.arc.mct.gui;

import java.awt.image.BufferedImage;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestContextAwareButton {

    private static final String ACTION_NAME = "Mock";
    
    @Mock private ContextAwareAction action;
    @Mock private ActionContext context;
    
    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(action.getValue(Action.NAME)).thenReturn(ACTION_NAME);
    }
    
    @Test
    public void testNoContext() {
        // Button should not be visible if no context has been provided 
        ContextAwareButton button = new ContextAwareButton(action);
        Assert.assertFalse(button.isEnabled());
        Assert.assertFalse(button.isVisible());
        
        // Should behave similarly if context is explicitly null
        button = new ContextAwareButton(action, null);
        Assert.assertFalse(button.isEnabled());
        Assert.assertFalse(button.isVisible());
    }
    
    @Test
    public void testContext() {
        // Should invoke canHandle for the specified context
        new ContextAwareButton(action, context);
        Mockito.verify(action).canHandle(context);
        Mockito.verify(action, Mockito.atLeastOnce()).isEnabled();
    }
    
    @Test
    public void testSetContext() {
        // Should invoke canHandle for the specified context
        new ContextAwareButton(action).setContext(context);
        Mockito.verify(action).canHandle(context);
        Mockito.verify(action, Mockito.atLeastOnce()).isEnabled();
    }
    
    @Test (dataProvider="generateStates")
    public void testState(boolean canHandle, boolean isEnabled, boolean useConstructor) {
        // Set up states
        Mockito.when(action.canHandle(context)).thenReturn(canHandle);
        Mockito.when(action.isEnabled()).thenReturn(isEnabled);
        
        // Make the button
        ContextAwareButton button = useConstructor ?
                        new ContextAwareButton(action, context) :
                        new ContextAwareButton(action);
        if (!useConstructor) {
            button.setContext(context);
        }
             
        // Verify visibility/enabled states match what the action reports
        Assert.assertEquals(button.isVisible(), canHandle);
        Assert.assertEquals(button.isEnabled(), isEnabled);
    }
    
    @Test
    public void testToolTip() {
        // Tool tip should show action name if text set to something else
        ContextAwareButton button = new ContextAwareButton(action);
        
        // Should not have a tool tip by default
        Assert.assertTrue(button.getToolTipText() == null || button.getToolTipText().isEmpty());
        
        // Setting text to same action name should not change tool tip
        button.setText(ACTION_NAME);
        Assert.assertTrue(button.getToolTipText() == null || button.getToolTipText().isEmpty());
        
        // Setting text to some other name should change tool tip
        button.setText("Something else");
        Assert.assertEquals(button.getToolTipText(), ACTION_NAME);        
    }
    
    @Test
    public void testSetIcon() {
        // Should not have an icon by default
        Assert.assertNull(new ContextAwareButton(action).getIcon());
               
        // Should customize (change) icon if it can (only for ImageIcons)
        Icon icon = new ImageIcon(new BufferedImage(12,12,BufferedImage.TYPE_INT_ARGB));
        Mockito.when(action.getValue(Action.SMALL_ICON)).thenReturn(icon);
        Mockito.when(action.getValue(Action.LARGE_ICON_KEY)).thenReturn(icon);
        Assert.assertNotNull(new ContextAwareButton(action).getIcon());
        Assert.assertNotSame(new ContextAwareButton(action).getIcon(), icon);
        
        // Should use (but not customize) regular icon
        icon = Mockito.mock(Icon.class);
        Mockito.when(action.getValue(Action.SMALL_ICON)).thenReturn(icon);
        Mockito.when(action.getValue(Action.LARGE_ICON_KEY)).thenReturn(icon);
        Assert.assertEquals(new ContextAwareButton(action).getIcon(), icon);
    }
    
    @DataProvider
    public Object[][] generateStates() {
        Object[][] result = new Object[8][];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Object[]{
                            (i & 1) != 0, // canHandle
                            (i & 2) != 0, // isEnabled
                            (i & 4) != 0  // useConstructor
            };
        }
        return result;
    }
}
