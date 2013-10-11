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