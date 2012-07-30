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
/**
 * MCTProperties.java Aug 18, 2008
 * 
 * This code is the property of the National Aeronautics and Space
 * Administration and was produced for the Mission Control Technologies (MCT)
 * project.
 * 
 */
package gov.nasa.arc.mct.launch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * get the MCT system wide properties
 */
@SuppressWarnings("serial")
public class MCTProperties extends Properties {
    public static final MCTProperties DEFAULT_MCT_PROPERTIES;

    static {
        try {
            DEFAULT_MCT_PROPERTIES = new MCTProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public MCTProperties(String propertyFileName) throws IOException {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(propertyFileName);

        if (is == null)
            throw new IOException("MCT properties util:: Unable to get mct property file: " + propertyFileName);
        load(is);
    }

    public MCTProperties() throws IOException {
        String defaultProperties = "properties/mct.properties";

        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(defaultProperties);

        if (is == null)
            throw new IOException("MCT properties util:: Unable to get mct property file: " + defaultProperties);
        load(is);
    }

    public void load(InputStream is) throws IOException {
        try {
            super.load(is);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                // ignore exception
            }
        }
    }
    
    /*
     * get a set of properties named by your class
     */
    public MCTProperties(Class<?> c) throws IOException {

        String className = getClassName(c);
        String pkgProperties = "properties/" + className + ".properties";

        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(pkgProperties);

        if (is == null)
            throw new IOException("MCT properties util:: Unable to get mct property file: " + pkgProperties);
        load(is);
    }
    
    

    /*
     * returns the class (without the package if any)
     */

    private String getClassName(Class<?> c) {
        String FQClassName = c.getName();
        int firstChar;
        firstChar = FQClassName.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            FQClassName = FQClassName.substring(firstChar);
        }
        return FQClassName;
    }
    
    // Normally we could 
    @Override
    public String getProperty(String key) {
        String value = System.getProperty(key);
        if (value != null) {
        	return trim(value);
        } else {
            return trim(super.getProperty(key));
        }
    }
    
    @Override
    public String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null) {
        	return trim(value);
        } else {
            return trim(super.getProperty(key,defaultValue));
        }
    }

    /**
     * Return a string value trimmed of leading and trailing spaces, or null if the
     * string is null.
     * 
     * @param s the string to trim
     * @return the trimmed value, or null
     */
    protected static String trim(String s) {
    	if (s == null) {
    		return null;
    	} else {
    		return s.trim();
    	}
    }

}
