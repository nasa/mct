package gov.nasa.arc.mct.launch;
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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class Startup {

	/**
     * A directory along the classpath where we expect to find base OSGi
     * bundles.
     */
    private static final String OSGI_BUNDLE_DIR = "osgi";
    private static final String OSGI_BUNDLE_SYS_PROPERTY = "osgiPluginsList";

    /** A directory along the classpath where we expect to find MCT plugins. */
    private static final String PLATFORM_BUNDLE_DIR = "platform";
    private static final String PLATFORM_BUNDLE_SYS_PROPERTY = "platformPluginsList";

	
    Startup() {
        try {
            startOSGI();
        } catch (Exception t) {
        	t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * Substitutes expression.
     * @param expression - the string.
     * @param evars - environment variables.
     * @return the substitute string.
     */
    private static String substitute(String expression, Properties evars) {
        assert evars != null && expression != null;
        final String regex = "([^\\)]*)(%\\()([\\w]+)(\\))"; // four groups
        final Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String evarReplacement = evars.getProperty(m.group(3));
            if (evarReplacement == null) {
                System.err.println(String.format("Expression: %s substituting empty string because evar %s is undefined", expression, m.group(3)));
                evarReplacement = "";
            } 
            String beforeReplacement = m.group(1);
            sb.append(beforeReplacement + evarReplacement);
            expression = expression.substring(m.end());
            m = pattern.matcher(expression);
        }
        return sb.toString() + expression;
    }
    
    private File getCacheDir() {
    	String filePath = substitute(MCTProperties.DEFAULT_MCT_PROPERTIES.getProperty("cacheDir"),System.getProperties());
        
        // Want to make sure that any old bundles are cleared out.
        File cacheDir = new File(filePath);

        if (cacheDir.exists()) {
            if (!deleteDir(cacheDir)) {
                System.err.println("Could not delete OSGi cache directory");
            }
        }

        // (Re)create the cache directory.
        if (!cacheDir.mkdirs()) {
            throw new RuntimeException("Could not create osgi cache dir (" + cacheDir
                    + "). Ensure that the directory is writable and executable.");
        }

        return cacheDir;
    }
    
    private boolean deleteDir(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                if (!deleteDir(child)) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return f.delete(); 
    }
    
    private void startOSGI() {
        Map<String, String> props = new HashMap<String, String>();
        
        // Start with a clean bundle cache.
        props.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        
        // Set the cache directory path.
        props.put(Constants.FRAMEWORK_STORAGE, getCacheDir().getAbsolutePath());
        
        // Bundles should have the extension classloader as their parent classloader.
        props.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_EXT);
        
        // felix specific properties
        props.put("org.osgi.framework.bootdelegation", MCTProperties.DEFAULT_MCT_PROPERTIES.getProperty("org.osgi.framework.bootdelegation", ""));
        //props.put("felix.log.level","4");

        // Add all system properties that seem to be OSGi property names.
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("osgi.") || key.startsWith("org.osgi.")) {
                props.put(key, System.getProperty(key));
            }
        }

        Framework framework = null;
        
        // Iterate over frameworks on the classpath to see if one matches. See the
        // Javadoc for org.osgi.framework.launch.FrameworkFactory for information
        // about using ServiceLoader to find the available framework implementations.
        for (FrameworkFactory factory : ServiceLoader.load(FrameworkFactory.class)) {
            framework = factory.newFramework(props);
        }

        if (framework == null) {
            throw new RuntimeException("Cannot find an OSGi framework");
        }
        try {
            framework.start();
        } catch (BundleException e) {
            throw new RuntimeException("Cannot start OSGi framework", e);
        }
        
        BundleContext bc = framework.getBundleContext();

        startOSGIBundles(bc);
        startPlatformBundles(bc);
    }

    
    public void startPlatformBundles(BundleContext bc) {
        List<String> platformBundles = getBundleList(new DirectoryBundlesListTracker(), PLATFORM_BUNDLE_SYS_PROPERTY,
                PLATFORM_BUNDLE_DIR, Collections.<String>emptySet());
        assert platformBundles != null && platformBundles.size() > 0;
        loadBundles(platformBundles,bc);
    }
    
    private void startOSGIBundles(BundleContext bc) {
        List<String> osgiBundleList = getBundleList(new DirectoryBundlesListTracker(), OSGI_BUNDLE_SYS_PROPERTY,
                OSGI_BUNDLE_DIR, Collections.<String>emptySet());
        loadBundles(osgiBundleList, bc);
    }
    
    private List<String> getBundleList(DirectoryBundlesListTracker bundleListTracker, String bundleListSysProperty,
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

        List<String> bundlesLoc = bundleListTracker.getBundlesLocation();
        if (bundlesLoc == null || bundlesLoc.isEmpty()) {
            URL bundleDirURL = getClass().getClassLoader().getResource(bundleListPath);
            if (bundleDirURL == null) {
                System.err.println("Bundle directory not found, skipped: " + bundleListPath);
                return Collections.emptyList();
            }

            File bundleDir;
            try {
                bundleDir = new File(bundleDirURL.toURI());
            } catch (URISyntaxException e) {
                System.err.println("Error getting path to bundle directory " + bundleListPath);
                return Collections.emptyList();
            }

            if (!bundleDir.isDirectory()) {
            	System.err.println("Bundle directory path not a directory: " + bundleListPath);
                return Collections.emptyList();
            }

            File[] bundles = bundleDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            for (File bundleFile : bundles) {
                if (!excludeBundles.contains(bundleFile.getName())) {
                    bundleListTracker.addBundle(bundleFile.getAbsolutePath());
                }
            }
        }

        return bundleListTracker.getBundlesLocation();
    }
    
    private void loadBundles(List<String> bundlesList, BundleContext bc) {
        List<Bundle> bundles = new ArrayList<Bundle>();

        for (String bundlePath : bundlesList) {
            File bundleFile = new File(bundlePath);
            if (!bundleFile.exists()) {
                throw new RuntimeException("Plugin path not found: " + bundlePath);
            } else if (!bundleFile.canRead()) {
            	throw new RuntimeException("Cannot read plugin file: " + bundlePath);
            } else {
                Bundle b = loadBundle(bundleFile, bc);
                if (b != null) {
                    bundles.add(b);
                }
            }
        }

        startBundles(bundles);
    }

    Bundle loadBundle(File bundleFile, BundleContext bc) {
        String bundleURL = null;
        try {
            if (bundleFile.isDirectory()) {
                bundleURL = "reference:" + bundleFile.toURI().toURL().toExternalForm();
            } else {
                bundleURL = bundleFile.toURI().toURL().toExternalForm();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad path to plugin bundle: " + bundleFile.getAbsolutePath());
        }

        try {
            Bundle b = bc.installBundle(URLDecoder.decode(bundleURL, "UTF8"));
            return b;
        } catch (Exception e) {
            throw new RuntimeException("Error installing bundle from " + bundleURL, e);
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
                } catch (BundleException ex) {
                    throw new RuntimeException("Error starting bundle " + bundle.getLocation(),ex);
                }
            }
        }
    }

    private boolean isFragment(Bundle bundle) {
        return (bundle.getHeaders().get("Fragment-Host") != null);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new Startup();
    }
}
