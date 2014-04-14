package gov.nasa.arc.mct.defaults.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.PropertyDescriptor;
import gov.nasa.arc.mct.components.PropertyDescriptor.VisualControlDescriptor;
import gov.nasa.arc.mct.components.PropertyEditor;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.gui.CustomVisualControl;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.RoleAccess;
import gov.nasa.arc.mct.platform.spi.RoleService;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.User;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestInfoView {
    // This is just elaborate mocking to avoid NPEs
    User mockUser = Mockito.mock(User.class);
    RoleService mockRoleService = Mockito.mock(RoleService.class);   
    AbstractComponent comp = Mockito.mock(AbstractComponent.class);
    Platform mockPlatform = Mockito.mock(Platform.class);
    Platform oldPlatform;
    ViewInfo info = new ViewInfo(InfoView.class, "Info", ViewType.CENTER);    
    
    @BeforeMethod
    public void setup() {                    
        // Setup minimum expected environment for info view
        oldPlatform = PlatformAccess.getPlatform();
        Mockito.when(comp.getOwner()).thenReturn("*");
        Mockito.when(comp.getComponentTypeID()).thenReturn(""); 
        Mockito.when(mockUser.getUserId()).thenReturn("");
        Mockito.when(mockRoleService.getAllUsers()).thenReturn(Collections.singleton(""));
        Mockito.when(mockPlatform.getBootstrapComponents()).thenReturn(Collections.<AbstractComponent>emptyList());
        GlobalContext.getGlobalContext().switchUser(mockUser, Mockito.mock(Runnable.class));
        new RoleAccess().addRoleService(mockRoleService);
        new PlatformAccess().setPlatform(mockPlatform);
    }
    
    @AfterMethod
    public void teardown() {
        new PlatformAccess().setPlatform(oldPlatform);
    }
    
    @Test
    public void testUpdateMonitoredGUI() {
        /* This test ensures that updateMonitoredGUI triggers revalidate/repaint */
        
        final AtomicInteger repaintCalls = new AtomicInteger(0);
        final AtomicInteger revalidateCalls = new AtomicInteger(0);
        
       
        // We can't mock InfoView because we want to test its code, but we 
        // do want to verify that methods of its Swing superclass are invoked. 
        InfoView infoView = new InfoView(comp, info) {
            private static final long serialVersionUID = 1L;
            public void repaint() {
                super.repaint();
                repaintCalls.incrementAndGet();
            }
            public void revalidate() {
                super.revalidate();
                revalidateCalls.incrementAndGet();
            }
        };
        
        // The real part of the test: Check that repaint and revalidate really do get called.
        repaintCalls.set(0);
        revalidateCalls.set(0);
        infoView.updateMonitoredGUI();
        Assert.assertEquals(repaintCalls.get(), 1);
        Assert.assertEquals(revalidateCalls.get(), 1);        
    }
    
    @Test (dataProvider = "getExtendedFieldTestCases")
    public void testExtendedFieldRefresh(VisualControlDescriptor a, VisualControlDescriptor b) {
        // Verify that making a change to the second extended property
        // triggers an update in the first extended property.
        
        List<PropertyDescriptor> mockDescriptors = Arrays.asList(buildMockDescriptor(a), buildMockDescriptor(b));       
        
        Mockito.when(comp.getFieldDescriptors()).thenReturn(mockDescriptors);
        
        // Mocking Swing components tends to fail on different platforms, so stub
        CustomVisualControl mockControl = new CustomVisualControl() {
            private static final long serialVersionUID = -7026155085259171397L;
            private Object object = null;

            @Override
            public void setValue(Object value) { this.object = value; }

            @Override
            public Object getValue() { return object; }

            @Override
            public void setMutable(boolean mutable) {}
        };
        Mockito.when(comp.getAsset(CustomVisualControl.class)).thenReturn(mockControl);
        PropertyDescriptor lastPropertyDescriptor = mockDescriptors.get(mockDescriptors.size() - 1);        
        
        // Build a new info view
        InfoView infoView = new InfoView(comp, info);
        
        // First, verify that the appropriate get method has been called
        // (this implies that info view was populated with extended descriptors upon instantiation)
        for (PropertyDescriptor mockDescriptor : mockDescriptors) {
            switch (mockDescriptor.getVisualControlDescriptor()) {
            case CheckBox:
            case ComboBox:
                Mockito.verify(mockDescriptor.getPropertyEditor(), Mockito.times(1)).getValue();
                break;
            case Label:
            case TextField:
                Mockito.verify(mockDescriptor.getPropertyEditor(), Mockito.times(1)).getAsText();
                break;
            }
        }
        
        ActionEvent mockEvent = Mockito.mock(ActionEvent.class);
        
        // Act as if user changed a property
        switch (lastPropertyDescriptor.getVisualControlDescriptor()) {
        case CheckBox: {
            JCheckBox checkBox = findLastComponentOfType(infoView, JCheckBox.class);
            checkBox.doClick();
            break;
        }
        case ComboBox: {
            @SuppressWarnings("rawtypes")
            JComboBox comboBox = findLastComponentOfType(infoView, JComboBox.class);
            comboBox.setSelectedItem("other");
            break;            
        }
        case Label: {
            // Non-editable
            break;
        }
        case TextField: {
            JTextField textField = findLastComponentOfType(infoView, JTextField.class);
            textField.setText("changed");
            for (ActionListener listener : textField.getActionListeners()) {
                // Don't invoke listeners related to platform-specific Swing implementation
                // (may lead to inconsistent behavior when running tests on other platforms)
                if (listener.getClass().getName().startsWith("gov.nasa.arc.mct")) {
                    listener.actionPerformed(mockEvent);
                }
            }
            break;
        }
        case TextArea: {
            JTextArea textArea = findLastComponentOfType(infoView, JTextArea.class);
            textArea.setText("changed");
            for (FocusListener listener : textArea.getFocusListeners()) {
                // Don't invoke listeners related to platform-specific Swing implementation
                // (may lead to inconsistent behavior when running tests on other platforms)
                if (listener.getClass().getName().startsWith("gov.nasa.arc.mct")) {
                    listener.focusLost(Mockito.mock(FocusEvent.class));
                }
            }
            break;            
        }
        case Custom: {
            CustomVisualControl control = findLastComponentOfType(infoView, CustomVisualControl.class);
            control.setValue("changed");
            try {
                Method m = CustomVisualControl.class.getDeclaredMethod("fireChange");
                m.setAccessible(true);
                m.invoke(control);
            } catch (Exception e) {
                Assert.fail("Could not fire property change", e);
            }
            break;
        }
        }
        
        // Verify that some get method has been called on the OTHER mock
        // (this implies that changes to one field cause others to be refreshed)
        // Since labels cannot be edited, we don't expect this to occur 
        // if the property in the last position was a label. In that case, 
        // it is expected that get methods have not been called again.
        int expectedTimes = (lastPropertyDescriptor.getVisualControlDescriptor() == VisualControlDescriptor.Label) ?
                1 : 2;
        PropertyDescriptor mockDescriptor = mockDescriptors.get(0);
        switch (mockDescriptor.getVisualControlDescriptor()) {
        case CheckBox:
            Mockito.verify(mockDescriptor.getPropertyEditor(), Mockito.times(expectedTimes)).getValue();
            break;
        case ComboBox:
            Mockito.verify(mockDescriptor.getPropertyEditor(), Mockito.times(expectedTimes)).getValue();
            break;
        case Label:
            Mockito.verify(mockDescriptor.getPropertyEditor(), Mockito.times(expectedTimes)).getAsText();
            break;
        case TextField:
            Mockito.verify(mockDescriptor.getPropertyEditor(), Mockito.times(expectedTimes)).getAsText();
            break;
        case TextArea:
            Mockito.verify(mockDescriptor.getPropertyEditor(), Mockito.times(expectedTimes)).getAsText();
            break;            
        }
            
        
        
    }
    
    /*
     * Find the last, deepest component of a given type in the Swing hierarchy.
     * Used to identify a property field (which are placed after the default fields
     * in info view.)
     * If InfoView layout changes, this will need to change too.
     */
    private <T extends JComponent> T findLastComponentOfType(JComponent container, Class<T> componentClass) {
        T result = componentClass.isAssignableFrom(container.getClass()) ? componentClass.cast(container) : null;
        
        for (Component c : container.getComponents()) {
            if (c instanceof JComponent) {
                T candidate = findLastComponentOfType((JComponent) c, componentClass);
                result = candidate != null ? candidate : result;
            }
        }
        
        return result;        
    }
    
    @DataProvider
    public Object[][] getExtendedFieldTestCases() {
        // Test all combinations of two VCDs
        Object[][] testCases = new Object[VisualControlDescriptor.values().length * VisualControlDescriptor.values().length][2];
        int i = 0;
        for (VisualControlDescriptor a : VisualControlDescriptor.values()) {
            for (VisualControlDescriptor b : VisualControlDescriptor.values()) {
                testCases[i  ][0] = a;
                testCases[i++][1] = b;
            }            
        }
        return testCases;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private PropertyDescriptor buildMockDescriptor(VisualControlDescriptor vcd) {
        PropertyDescriptor mockDescriptor = Mockito.mock(PropertyDescriptor.class);
        PropertyEditor mockEditor = Mockito.mock(PropertyEditor.class);
        
        Mockito.when(mockDescriptor.getShortDescription()).thenReturn("mock");
        Mockito.when(mockDescriptor.getVisualControlDescriptor()).thenReturn(vcd);
        Mockito.when(mockDescriptor.getPropertyEditor()).thenReturn(mockEditor);
        Mockito.when(mockDescriptor.isFieldMutable()).thenReturn(true);
        
        // Change the result of get() method, to trigger refresh
        Mockito.when(mockEditor.getTags()).thenReturn(Arrays.asList("mock", "other"));
        Mockito.when(mockEditor.getAsText()).thenReturn("mock");
        Mockito.when(mockEditor.getValue()).thenReturn(vcd != VisualControlDescriptor.CheckBox ? "mock" : Boolean.TRUE);       
        
        return mockDescriptor;
    }
}
