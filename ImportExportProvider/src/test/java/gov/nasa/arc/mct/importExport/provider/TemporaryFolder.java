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
package gov.nasa.arc.mct.importExport.provider;

import java.io.File;
import java.io.IOException;

/**
 * Allows creation of files and folders that are guaranteed to be deleted when
 * the test method finishes (whether it passes or fails).
 * 
 * <pre>
 * public class MyTest {
 *     public TemporaryFolder tmpDir = new TemporaryFolder();
 * 
 *     &#064;BeforeMethod
 *     public void setup() throws Exception {
 *         tmpDir.create();
 *         // ...
 *     }
 * 
 *     &#064;AfterMethod
 *     public void tearDown() throws Exception {
 *         tmpDir.destroy();
 *     }
 * }
 * </pre>
 */
public class TemporaryFolder {

    private File folder;

    /**
     * Create a temporary folder for a unit test.
     */
    public TemporaryFolder() {
    }

    public void create() throws IOException {
        folder = File.createTempFile("testng", "", null);
        folder.delete();
        folder.mkdir();
    }

    public void destroy() {
        if (folder != null) {
            recursiveDelete(folder);
        }
    }

    public File getRoot() {
        if (folder == null) {
            throw new IllegalStateException(
                    "The temporary folder has not yet been created. Did you forget to call the create() method?");
        }
        return folder;
    }

    /**
     * Return a new fresh file with the given name under the temporary folder.
     * 
     * @param fileName the name of the file to be created
     * @return new file
     */
    public File newFile(String fileName) throws IOException {
        File file = new File(getRoot(), fileName);
        if (!file.createNewFile()) {
            new IOException("A file with the name '" + fileName
                    + "' already exists in the test folder");
        }

        return file;
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }
        file.delete();
    }

}