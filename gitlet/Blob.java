package gitlet;

import java.io.File;
import java.io.Serializable;

/** Class Blob for Gitlet.
 * @author Xiaoru Zhao
 */
public class Blob implements Serializable {

    /** Constructor of Blob.
     *
     * @param name the name of the file represented by Blob.
     */
    public Blob(String name) {
        this.filename = name;
        File cwd = new File(".");
        File blob = Utils.join(cwd, filename);
        content = Utils.readContentsAsString(blob);
        String id = filename + content;
        _ID = Utils.sha1(id);
    }

    /** To get the FILENAME of Blob.
     * @return the filename. */
    public String getFilename() {
        return this.filename;
    }

    /** To get the content of Blob.
     * @return the content. */
    public String getContent() {
        return content;
    }

    /** To get the ID of Blob.
     * @return the ID. */
    public String getID() {
        return _ID;
    }

    /** Name of the file in Blob.*/
    private String filename;

    /** Content of the Blob.*/
    private String content;

    /** ID of the Blob.*/
    private String _ID;
}
