/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.osgi.platform;

import gov.nasa.arc.mct.util.logging.MCTLogger;
import gov.nasa.arc.mct.util.property.MCTProperties;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OSGIRuntimeImpl implements OSGIRuntime {
    private static final MCTLogger logger = MCTLogger.getLogger(OSGIRuntimeImpl.class);

    /**
     * A directory along the classpath where we expect to find additional
     * bundles
     */
    private static final String EXTERNAL_BUNDLE_DIR = "plugins";
    private static final String EXTERNAL_BUNDLE_SYS_PROPERTY = "externalPluginsList";
    private static final String EXCLUDE_BUNDLES_SYS_PROPERTY = "excludePluginsList";
    
    /** Amount of time to wait for framework to stop when stopping the framework. */
    private static final long FRAMEWORK_STOP_WAIT_TIME = 5000;

    private static OSGIRuntimeImpl osgiRuntime = new OSGIRuntimeImpl();

    public static OSGIRuntimeImpl getOSGIRuntime() {
        return osgiRuntime;
    }

    private BundleContext bc = null;
    
    public void setBundleContext(BundleContext context) {
        bc = context;
    }

    private Set<String> getExcludeBundles() {
        String propertyVal = MCTProperties.DEFAULT_MCT_PROPERTIES.getProperty(EXCLUDE_BUNDLES_SYS_PROPERTY);
        Set<String> excludeBundles = new HashSet<String>();
        if (propertyVal != null) {
            String[] bundles = propertyVal.split("[ \\t]*,[ \\t]*");
            for (String bundlePath : bundles) {
                excludeBundles.add(bundlePath);
            }
        }
        return excludeBundles;
    }

    private List<String> getBundleList(BundlesListTracker bundleListTracker, String bundleListSysProperty,
            String bundleListPath, Collection<String> excludeBundles) {
        String propertyVal = MCTProperties.DEFAULT_MCT_PROPERTIES.getProperty(bundleListSysProperty);
        if (propertyVal != null) {
            String[] bundles = propertyVal.split("[ \\t]*,[ \\t]*");
            for (String bundlePath : bundles) {
                if (!excludeBundles.contains(bundlePath)) {
                    bundleListTracker.addBundle(bundlePath);
                }
            }
        }

        logger.debug("Looking for bundles in location {0}", bundleListPath);
        List<String> bundlesLoc = bundleListTracker.getBundlesLocation();
        if (bundlesLoc == null || bundlesLoc.isEmpty()) {
            URL bundleDirURL = getResourceURL(bundleListPath);
            if (bundleDirURL == null) {
                logger.error("Bundle directory not found, skipped: " + bundleListPath);
                return Collections.emptyList();
            }

            File bundleDir;
            try {
                bundleDir = getFile(bundleDirURL);
            } catch (URISyntaxException e) {
                logger.error("Error getting path to bundle directory " + bundleListPath, e);
                return Collections.emptyList();
            }

            if (!bundleDir.isDirectory()) {
                logger.error("Bundle directory path not a directory: " + bundleListPath);
                return Collections.emptyList();
            }

            logger.debug("Loading bundles from directory {0}", bundleDir.getAbsolutePath());
            File[] bundles = bundleDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            for (File bundleFile : bundles) {
                if (!excludeBundles.contains(bundleFile.getName())) {
                    bundleListTracker.addBundle(bundleFile.getAbsolutePath());
                    logger.debug("Found a bundle: {0}", bundleFile.getAbsolutePath());
                }
            }
        }

        return bundleListTracker.getBundlesLocation();
    }

    private void loadBundles(List<String> bundlesList) {
        List<Bundle> bundles = new ArrayList<Bundle>();

        for (String bundlePath : bundlesList) {
            logger.info("loadBundles path : " + bundlePath);

            File bundleFile = getFile(bundlePath);
            if (!bundleFile.exists()) {
                logger.error("Plugin path not found: " + bundlePath);
            } else if (!bundleFile.canRead()) {
                logger.error("Cannot read plugin file: " + bundlePath);
            } else {
                Bundle b = loadBundle(bundleFile);
                if (b != null) {
                    bundles.add(b);
                }
            }
        }

        startBundles(bundles);
    }

    Bundle loadBundle(File bundleFile) {
        String bundleURL = null;
        try {
            if (bundleFile.isDirectory()) {
                bundleURL = "reference:" + bundleFile.toURI().toURL().toExternalForm();
            } else {
                bundleURL = bundleFile.toURI().toURL().toExternalForm();
            }
        } catch (MalformedURLException e) {
            logger.error("Bad path to plugin bundle: " + bundleFile.getAbsolutePath());
            return null;
        }

        try {
            Bundle b = bc.installBundle(URLDecoder.decode(bundleURL, "UTF8"));
            logger.debug("Loaded plugin from path: {0}", bundleURL);
            return b;
        } catch (Exception e) {
            logger.error(e, "Error installing bundle from {0}", bundleURL);
            return null;
        }
    }

    /**
     * Starts an ordered list of bundles.
     * 
     * @param bundles
     *            the list of bundles to start
     */
    private void startBundles(List<Bundle> bundles) {
        for (Bundle bundle : bundles) {
            if (!isFragment(bundle)) {
                try {
                    bundle.start();
                    logger.debug("Started bundle {0}", bundle.getLocation());
                } catch (BundleException ex) {
                    logger.error(ex, "Error starting bundle {0}", bundle.getLocation());
                }
            }
        }
    }

    private boolean isFragment(Bundle bundle) {
        return (bundle.getHeaders().get("Fragment-Host") != null);
    }

    @Override
    public void startExternalBundles() {
        Set<String> excludeBundles = getExcludeBundles();
        List<String> externalBundles = getBundleList(new DirectoryBundlesListTracker(), EXTERNAL_BUNDLE_SYS_PROPERTY,
                EXTERNAL_BUNDLE_DIR, excludeBundles);
        loadBundles(externalBundles);
    }

    @Override
    public synchronized BundleContext getBundleContext() {
        return bc;
    }

    public <T> T getService(Class<T> serviceClass, String filter) {
        BundleContext bc = getBundleContext();
        if (bc == null) { return null; }
        ServiceReference[] srs;
        try {
            srs = bc.getServiceReferences(serviceClass.getName(), filter);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }

        if (srs != null && srs.length > 0) {
            ServiceReference sr = srs[0];
            return serviceClass.cast(bc.getService(sr));

        }
        return null;
    }

    @Override
    public void stopOSGI() throws BundleException, InterruptedException {
        // Stop all active bundles
        if (bc != null) {
            Bundle[] bundles = bc.getBundles();
            // Stop bundles in reverse order (plugins, platform, osgi)
            // Doing so in the opposite order will generate errors
            // (due to stopping SCR before other bundles.)
            for (int i = bundles.length - 1; i >= 0; i--) {
                Bundle bundle = bundles[i];
                // Fragments do not participate in bundle lifecycle
                if (!isFragment(bundle) && bundle.getState() == Bundle.ACTIVE) {
                    try {
                        bundle.stop();
                        logger.debug("Stopped bundle " + bundle.getLocation());
                    } catch (BundleException ex) {
                        logger.error(ex, "Error stopping bundle {0}", bundle.getLocation());
                    }
                }
            
            }
        }
        bc = null;
    }

    private URL getResourceURL(String path) {
        System.err.println("getting resource for " + path);
        System.err.println(ClassLoader.getSystemResource(path));
        return ClassLoader.getSystemResource(path);
    }

    private File getFile(String path) {
        return new File(path);
    }

    private File getFile(URL url) throws URISyntaxException {
        return new File(url.toURI());
    }

}
