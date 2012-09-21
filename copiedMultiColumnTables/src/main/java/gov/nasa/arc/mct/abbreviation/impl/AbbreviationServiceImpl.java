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
package gov.nasa.arc.mct.abbreviation.impl;

import gov.nasa.arc.mct.abbreviation.AbbreviationService;
import gov.nasa.arc.mct.abbreviation.Abbreviations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a service for finding an applying abbreviations for words
 * or phrases.
 */
public class AbbreviationServiceImpl implements AbbreviationService {
	
	private static final Logger logger = LoggerFactory.getLogger(AbbreviationServiceImpl.class);
	
	private static final String ABBREVIATIONS_FILE_PROPERTY = "abbreviations-file";
	
	private AbbreviationsManager manager;
	
	/**
	 * Activates the service implementation. A map of properties is
	 * used to configure the service.
	 * 
	 * @param context the component context for this service
	 */
	public void activate(ComponentContext context) {
		@SuppressWarnings("unchecked")
		Dictionary<String,String> properties = context.getProperties();
		
		// This property will always be present, according to OSGi 4.1 Compendium
		// Specification section 112.6.
		String componentName = properties.get("component.name");
		
		String abbreviationsFilePath = properties.get(ABBREVIATIONS_FILE_PROPERTY);
		Properties abbreviationsProperties = null;
		
		if (abbreviationsFilePath == null) {
			logger.warn("{}: no configuration value for {} - no abbreviations will be available.", componentName, ABBREVIATIONS_FILE_PROPERTY);
		} else {
			InputStream in = findFile(abbreviationsFilePath);
			if (in == null) {
				logger.warn("{}: abbreviations file <{}> not found - no abbreviations will be available.", componentName, abbreviationsFilePath);
			} else {
				try {
					abbreviationsProperties = new Properties();
					abbreviationsProperties.load(in);
				} catch (IOException ex) {
					logger.warn("{}: error loading abbreviations file <{}> - no abbreviations will be available.", componentName, abbreviationsFilePath);
					abbreviationsProperties = null;
				}
			}
		}
		
		if (abbreviationsProperties == null) {
			abbreviationsProperties = new Properties();
		}
		
		manager = new AbbreviationsManager(abbreviationsProperties);
	}

	/**
	 * Looks up a file given a path. The file is looked up first relative to the
	 * current directory. If not found, a matching resource within the bundle is
	 * tried. If neither method works, null is returned to indicate that the file
	 * could not be found.
	 * 
	 * @param path a relative or absolute pathname, or a resource name from within the bundle
	 * @return an input stream for reading from the file, or null if the file could not be found
	 */
	InputStream findFile(String path) {
		// 1. Try to find using the file path, which may be absolute or
		// relative to the current directory.
		File f = new File(path);
		if (f.isFile() && f.canRead()) {
			try {
				return new FileInputStream(f);
			} catch (Exception ex) {
				// ignore, for now
			}
		}
		
		// 2. Try to find a resource in the bundle. This return value may be null,
		// if no resource is found matching the path.
		return getClass().getResourceAsStream(path);
	}

	@Override
	public Abbreviations getAbbreviations(String s) {
		return manager.getAbbreviations(s);
	}

}
