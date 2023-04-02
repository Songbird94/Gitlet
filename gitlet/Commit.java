package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/** Commit class of Gitlet.
 * @author Xiaoru Zhao
 */
public class Commit implements Serializable {

    /** Constructor of Commit.
     *
     * @param msg message of the commit.
     * @param parentID parent of the commit.
     */
    public Commit(String msg, String parentID) {
        this.message = msg;
        this.parent = parentID;
        blobs = new HashMap<>();
        if (parent == null) {
            timestamp = new Date(0);
        } else {
            timestamp = new Date();
            File commits = new File(".gitlet/commits");
            File fileParent = Utils.join(commits, parent);
            Commit commitParent = Utils.readObject(fileParent, Commit.class);
        }
    }

    /** Get the message of commit.
     * @return the message.*/
    public String getMessage() {
        return this.message;
    }

    /** Get the timestamp of commit.
     * @return the timestamp.*/
    public Date getTimestamp() {
        return this.timestamp;
    }

    /** Get parent of commit.
     * @return the parent id.*/
    public String getParent() {
        return this.parent;
    }

    /** Get id of commit.
     * @return the id. */
    public String getIdentifier() {
        if (parent == null) {
            identifier = Utils.sha1(timestamp.toString());
            identifier += Utils.sha1(message);
            identifier = Utils.sha1(identifier);
            return identifier;
        } else {
            identifier = null;
            for (Blob x: blobs.values()) {
                String id = x.getID();
                identifier += Utils.sha1(id);
            }
            identifier += Utils.sha1(timestamp.toString());
            identifier += Utils.sha1(message);
            identifier = Utils.sha1(identifier);
            return this.identifier;
        }
    }

    /** Get blob of commit.
     * @return the blobs. */
    public HashMap<String, Blob> getBlobs() {
        return this.blobs;
    }

    /** Add blob in commit.
     *
     * @param filename filename of the blob.
     * @param blob the blob to be added.
     */
    public void setBlob(String filename, Blob blob) {
        blobs.put(filename, blob);
    }

    /** Remove blob from commit.
     *
     * @param filename the file to be removed.
     */
    public void removeBlob(String filename) {
        blobs.remove(filename);
    }

    /** Message of commit. */
    private final String message;

    /** Timestamp of commit. */
    private final Date timestamp;

    /** Parent of commit. */
    private final String parent;

    /** ID of commit. */
    private String identifier;

    /** Blobs of commit. */
    private HashMap<String, Blob> blobs;
}
