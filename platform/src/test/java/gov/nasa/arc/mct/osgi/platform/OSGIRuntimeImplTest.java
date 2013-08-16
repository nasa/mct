package gov.nasa.arc.mct.osgi.platform;

import java.util.Dictionary;

import org.mockito.InOrder;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OSGIRuntimeImplTest {
    private Bundle[] mockBundles = new Bundle[5];
    private BundleContext mockBundleContext = Mockito.mock(BundleContext.class);
    private OSGIRuntime runtimeImpl;
    
    // Some tests need to skip stopping a bundle - do this in the middle
    private final int skipIndex = mockBundles.length / 2;
    
    @BeforeMethod
    public void setup() {
        Dictionary<String,String> mockDictionary = Mockito.mock(Dictionary.class);
        for (int i = 0; i < mockBundles.length; i++) {
            mockBundles[i] = Mockito.mock(Bundle.class);
            Mockito.when(mockBundles[i].getHeaders()).thenReturn(mockDictionary);
            Mockito.when(mockBundles[i].getState()).thenReturn(Bundle.ACTIVE);
        }
        Mockito.when(mockBundleContext.getBundles()).thenReturn(mockBundles);
        runtimeImpl = OSGIRuntimeImpl.getOSGIRuntime();
        OSGIRuntimeImpl.getOSGIRuntime().setBundleContext(mockBundleContext);
    }

    @Test
    public void testStop() throws Exception { // Any exception should be a failure here
        // This should stop all bundles, in reverse order
        // (in practice, osgi bundles are at the start of the list, and should stop last)
        runtimeImpl.stopOSGI();        
       
        // Verify that bundles were all stopped in reverse order
        verifyInOrder(false);
    }

    @Test
    public void testStopSkipInactive() throws Exception { // Any exception should be a failure here             
        // Bundles that are already inactive should not be stopped
        Mockito.when(mockBundles[skipIndex].getState()).thenReturn(Bundle.STOPPING);
        
        // Stop OSGi (this is the method we are testing)
        runtimeImpl.stopOSGI();
        
        // Verify that bundles were all stopped in reverse order
        // except for the inactive one, which should be skipped
        verifyInOrder(true);    
    }
    
    @Test
    public void testStopClearsBundleContext() throws Exception {
        // Stopping should nullify the bundle context
        
        // Verify expected precondition
        Assert.assertEquals(runtimeImpl.getBundleContext(), mockBundleContext);
        
        // Stop OSGi (this is the method we are testing)
        runtimeImpl.stopOSGI();        
        
        // Bundle context should have been cleared
        Assert.assertNull(runtimeImpl.getBundleContext());
    }
    
    @Test
    public void testNullBundleContext() throws Exception {
        // Stopping should nullify the bundle context
        
        // Verify expected precondition
        Assert.assertEquals(runtimeImpl.getBundleContext(), mockBundleContext);
        
        // Stop OSGi (this is the method we are testing)
        runtimeImpl.stopOSGI();        
        
        // BundleContext is known to be null at this point (per previous test)
        // Stop it again. Should not throw any exception.
        runtimeImpl.stopOSGI();
    }
    
    @Test
    public void testStopSkipsFragments() throws Exception {
        // OSGi fragments should not participate in lifecycle
        Dictionary<String,String> mockFragmentDictionary =
                Mockito.mock(Dictionary.class);
        Mockito.when(mockFragmentDictionary.get("Fragment-Host"))
                .thenReturn("host");
        Mockito.when(mockBundles[skipIndex].getHeaders())
                .thenReturn(mockFragmentDictionary);
        
        // Stop OSGi (this is the method we are testing)
        runtimeImpl.stopOSGI();
        
        // Verify that bundles were all stopped in reverse order,
        // except for the fragment, which should be skipped
        verifyInOrder(true);             
    }
    
    @Test
    public void testStopHandlesBundleExceptions() throws Exception {
        // If one bundle fails to stop (throws BundleException),
        // this should not prevent other bundles from being stopped.
        Mockito.doThrow(Mockito.mock(BundleException.class))
                .when(mockBundles[skipIndex]).stop();
        
        // Stop OSGi (this is the method we are testing)
        runtimeImpl.stopOSGI();
        
        // Verify that bundles were all stopped in reverse order,
        // including the one that threw the exception
        verifyInOrder(false);
    }
    
    /*
     * Verify that bundles were stopped in reverse order. 
     * Argument indicates whether or not we expect one bundle to 
     * be skipped for some reason
     */
    private void verifyInOrder(boolean expectSkipped) throws Exception {
        InOrder inOrder = Mockito.inOrder((Object[]) mockBundles);
        for (int i = mockBundles.length - 1 ; i >= 0; i--) {
            if (expectSkipped && i == skipIndex) {
                Mockito.verify(mockBundles[i], Mockito.never()).stop();
            } else {
                inOrder.verify(mockBundles[i]).stop();
            }
        }     
    }
}
