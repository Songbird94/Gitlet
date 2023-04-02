package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/** The command class of Gitlet.
 * @author Xiaoru Zhao
 */
public class Command {

    /** Command Init. */
    public void init() throws IOException {
        if (_GITLET.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            return;
        }
        _GITLET.mkdir();
        _STAGINGAREA.mkdir();
        _ADDITION.mkdir();
        _REMOVAL.createNewFile();
        _COMMITS.mkdir();
        _BLOBS.mkdir();
        _BRANCH.mkdir();
        _HEAD.createNewFile();
        Commit initial = new Commit("initial commit", null);
        String initialID = initial.getIdentifier();

        File initialFile = new File(".gitlet/commits/" + initialID);
        initialFile.createNewFile();
        Utils.writeObject(initialFile, initial);

        String[] head = new String[2];
        head[0] = "master";
        head[1] = "unchanged";
        Utils.writeObject(_HEAD, head);

        File master = new File(".gitlet/branch/master");
        master.mkdir();
        File inMaster = new File(".gitlet/branch/master/branch");
        HashMap<String, String> masterBranch = new HashMap<String, String>();
        masterBranch.put("master", initialID);
        Utils.writeObject(inMaster, masterBranch);
        File headMaster = new File(".gitlet/branch/master/head");
        headMaster.createNewFile();
        Utils.writeContents(headMaster, initialID);

        HashMap<String, String> removed = new HashMap<>();
        Utils.writeObject(_REMOVAL, removed);
    }

    /** Command Add.
     *
     * @param file the file to be added.
     * @throws IOException
     */
    public void add(String file) throws IOException {
        File f = new File(_CWD, file);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        if (!_GITLET.exists()) {
            System.out.println("System not initiated.");
            return;
        }
        HashMap inRemove = Utils.readObject(_REMOVAL, HashMap.class);
        boolean notAdd = false;
        if (inRemove.containsKey(file)) {
            inRemove.remove(file);
            Utils.writeObject(_REMOVAL, inRemove);
            notAdd = true;
        }
        String branch = Utils.readObject(_HEAD, String[].class)[0];
        File head = new File(".gitlet/branch/" + branch + "/head");
        String id = Utils.readContentsAsString(head);
        if (id.length() == 0) {
            Commit initial = new Commit("initial commit", null);
            id = initial.getIdentifier();
        }
        Commit lastCommit = findCommit(id);
        HashMap<String, Blob> blobs = lastCommit.getBlobs();
        boolean yesAdd = true;
        Blob blob = new Blob(file);

        if (blobs == null) {
            yesAdd = false;
        } else {
            for (Blob temp : blobs.values()) {
                if (temp.getID().equals(blob.getID())) {
                    yesAdd = false;
                    break;
                }
            }
        }
        if (!yesAdd) {
            File addedBlob = Utils.join(_ADDITION, blob.getID());
            if (addedBlob.exists()) {
                addedBlob.delete();
            }
            return;
        }
        File saveblob = Utils.join(_BLOBS, blob.getID());
        saveblob.createNewFile();
        Utils.writeObject(saveblob, blob);
        File added = Utils.join(_ADDITION, file);
        if (!added.exists() && !notAdd) {
            added.createNewFile();
            Utils.writeObject(added, blob);
        }
    }

    /**Read from my computer the head commit object and the staging area
     * Clone the HEAD commit
     * Modify its message and timestamp according to user input
     * Use the staging area to modify the files tracked by the new commit
     * Write back any new object made or any modified objects read earlier.
     * @param message the Commit Message. */
    public void commit(String message) throws IOException {
        HashMap<String, String> removed = Utils.readObject(_REMOVAL,
                HashMap.class);
        if (_ADDITION.list().length == 0 && removed.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        String branch = Utils.readObject(_HEAD, String[].class)[0];
        File head = new File(".gitlet/branch/" + branch + "/head");
        String lastID = Utils.readContentsAsString(head);
        if (lastID.length() == 0) {
            Commit initial = new Commit("initial commit", null);
            lastID = initial.getIdentifier();
        }
        Commit lastCommit = findCommit(lastID);
        Commit newCommit = new Commit(message, lastCommit.getIdentifier());
        for (String x : lastCommit.getBlobs().keySet()) {
            newCommit.setBlob(x, lastCommit.getBlobs().get(x));
        }
        for (String x: _ADDITION.list()) {
            File f = Utils.join(_ADDITION, x);
            Blob blob = Utils.readObject(f, Blob.class);
            newCommit.setBlob(x, blob);
            f.delete();
        }
        for (String x : removed.keySet()) {
            newCommit.removeBlob(x);
            removed.remove(x);
        }
        Utils.writeObject(_REMOVAL, removed);
        String commitID = newCommit.getIdentifier();
        Utils.writeContents(head, commitID);

        String currBranch = Utils.readObject(_HEAD, String[].class)[0];
        File newBranch = new File(".gitlet/branch/" + currBranch
                + "/branch");
        HashMap<String, String> master = Utils.readObject(newBranch,
                HashMap.class);
        master.put(currBranch, commitID);
        Utils.writeObject(newBranch, master);

        File commit = Utils.join(_COMMITS, newCommit.getIdentifier());
        commit.createNewFile();
        Utils.writeObject(commit, newCommit);
    }

    /** Log command of gitlet. */
    public void log() {
        String curr = Utils.readObject(_HEAD, String[].class)[0];
        File headBranch = Utils.join(_BRANCH, curr);
        File inBranch = Utils.join(headBranch, "head");
        String headID = Utils.readContentsAsString(inBranch);
        Commit commit = findCommit(headID);
        boolean track = true;
        while (track) {
            System.out.println("===");
            System.out.println("commit " + commit.getIdentifier());
            SimpleDateFormat f = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z",
                    Locale.ENGLISH);
            System.out.println("Date: " + f.format(commit.getTimestamp()));
            System.out.println(commit.getMessage());
            System.out.println();
            if (commit.getParent() == null) {
                break;
            }
            File parent = Utils.join(_COMMITS, commit.getParent());
            commit = Utils.readObject(parent, Commit.class);
        }
    }

    /** The global log command. */
    public void globalLog() {
        for (File commitF : _COMMITS.listFiles()) {
            String id = commitF.getName();
            Commit temp = findCommit(id);
            if (temp.getParent() != null && temp.getParent().length() > 1) {
                System.out.println("===");
                System.out.println("commit " + id);
                SimpleDateFormat f = new SimpleDateFormat(
                        "E MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
                System.out.println("Date: " + f.format(temp.getTimestamp()));
                System.out.println(temp.getMessage());
                System.out.println();
            } else {
                System.out.println("===");
                System.out.println("commit " + id);
                SimpleDateFormat f = new SimpleDateFormat(
                        "E MMM d HH:mm:ss yyyy Z");
                System.out.println("Date: " + f.format(temp.getTimestamp()));
                System.out.println(temp.getMessage());
                System.out.println();
            }
        }
    }

    /** The Status command. */
    public void status() {
        if (!_GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        String[] branches = _BRANCH.list();
        Arrays.sort(branches);
        for (String x : branches) {
            if (Utils.readObject(_HEAD, String[].class)[0].equals(x)) {
                System.out.println("*" + x);
            } else {
                System.out.println(x);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        String[] staged = _ADDITION.list();
        Arrays.sort(staged);
        for (String x : staged) {
            System.out.println(x);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        HashMap<String, String> removal = Utils.readObject(_REMOVAL,
                HashMap.class);
        String[] removed = removal.keySet().toArray(new String[0]);
        Arrays.sort(removed);
        for (String x : removed) {
            System.out.println(x);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** The check out command.
     *
     * @param filename the name of the file to be checkout.
     * @throws IOException
     */
    public void checkout(String filename) throws IOException {
        String branch = Utils.readObject(_HEAD, String[].class)[0];
        File head = new File(".gitlet/branch/" + branch + "/head");
        String id = Utils.readContentsAsString(head);
        checkout(id, filename);
    }

    /** The checkout command.
     *
     * @param id The ID of the file.
     * @param filename The name of the file.
     * @throws IOException
     */
    public void checkout(String id, String filename) throws IOException {
        File f = new File(".gitlet/commits/" + id);
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = findCommit(id);
        if (!commit.getBlobs().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String iD = commit.getBlobs().get(filename).getID();
        File blobFile = Utils.join(_BLOBS, iD);
        Blob blob = Utils.readObject(blobFile, Blob.class);
        String content = blob.getContent();
        File cwdFile = Utils.join(_CWD, filename);
        if (!cwdFile.exists()) {
            cwdFile.createNewFile();
        }
        Utils.writeContents(cwdFile, content);
    }

    /** Checkout command for branch.
     *
     * @param branchName The name of the branch.
     * @throws IOException
     */
    public void checkBranch(String branchName) throws IOException {
        boolean exist = false;
        String branch = Utils.readObject(_HEAD, String[].class)[0];
        File head = new File(".gitlet/branch/" + branch + "/head");
        String headID = Utils.readContentsAsString(head);
        if (headID.length() == 0) {
            Commit initial = new Commit("initial commit", null);
            headID = initial.getIdentifier();
        }
        Commit curr = findCommit(headID);
        for (String file : _CWD.list()) {
            if (Utils.join(_CWD, file).isDirectory()) {
                continue;
            }
            Blob blob = new Blob(file);
            if (!curr.getBlobs().containsKey(file)
                    || !curr.getBlobs().get(file).getID().
                    equals(blob.getID())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        for (String x : _BRANCH.list()) {
            if (x.equals(branchName)) {
                exist = true;
            }
        }
        if (!exist) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        for (File x : _CWD.listFiles()) {
            if (x.isDirectory()) {
                continue;
            }
            if (!x.getName().equals(".gitlet")) {
                if (!Utils.restrictedDelete(x)) {
                    System.out.println("Can not delete file " + x.getName());
                    return;
                }
            }
        }
        checkHelper(branchName);
        for (File x : _ADDITION.listFiles()) {
            x.delete();
        }
        HashMap<String, String> removed = new HashMap<>();
        Utils.writeObject(_REMOVAL, removed);
        String[] inHead = new String[2];
        inHead[0] = branchName;
        inHead[1] = "unchanged";
        Utils.writeObject(_HEAD, inHead);
    }

    /** Helper for checkout branch.
     *
     * @param branch Name of the branch.
     * @throws IOException
     */
    public void checkHelper(String branch) throws IOException {
        File currBranch = Utils.join(_BRANCH, branch);
        File currHead = Utils.join(currBranch, "head");
        String currID = Utils.readContentsAsString(currHead);
        if (currID.length() == 0) {
            return;
        }
        Commit currCommit = findCommit(currID);
        HashMap<String, Blob> blob = currCommit.getBlobs();
        if (blob != null) {
            for (Blob x : blob.values()) {
                File blobs = Utils.join(_BLOBS, x.getID());
                File file = new File(x.getFilename());
                Blob b = Utils.readObject(blobs, Blob.class);
                String contents = b.getContent();
                Utils.writeContents(file, contents);
                file.createNewFile();
            }
        }
    }

    /** Find command.
     *
     * @param message To find the commit id with MESSAGE.
     */
    public void find(String message) {
        boolean found = false;
        for (File x : _COMMITS.listFiles()) {
            String id = x.getName();
            Commit commit = findCommit(id);
            if (commit.getMessage().equals(message)) {
                System.out.println(id);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    /** The branch command. To create a new branch.
     *
     * @param name The new branch's name.
     * @throws IOException
     */
    public void branch(String name) throws IOException {
        String[] branches = _BRANCH.list();
        for (String x : branches) {
            if (x.equals(name)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }
        File newBranch = new File(".gitlet/branch/" + name);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        newBranch.mkdir();
        File branchInside = Utils.join(newBranch, "branch");
        HashMap<String, String> branch = new HashMap<>();
        Utils.writeObject(branchInside, branch);
        File newHead = Utils.join(newBranch, "head");
        newHead.createNewFile();
        String[] inHead = Utils.readObject(_HEAD, String[].class);
        inHead[1] = name;
        Utils.writeObject(_HEAD, inHead);
    }

    /** Un-stage the file FILENAME if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory
     * if the user has not already done so.
     * @param filename The name of the file to rm.
     */
    public void rm(String filename) {
        File file = new File(filename);
        String branch = Utils.readObject(_HEAD, String[].class)[0];
        File head = new File(".gitlet/branch/" + branch + "/head");
        String headID = Utils.readContentsAsString(head);
        if (headID.length() == 0) {
            File newhead = new File(".gitlet/branch/master/head");
            headID = Utils.readContentsAsString(newhead);
        }
        Commit commit = findCommit(headID);
        HashMap<String, Blob> tracked = commit.getBlobs();
        boolean stagedOrTracked = false;
        if (tracked != null) {
            for (String key : tracked.keySet()) {
                Blob blob = tracked.get(key);
                if (blob.getFilename().equals(filename)) {
                    HashMap<String, String> removed = Utils.readObject(_REMOVAL,
                            HashMap.class);
                    removed.put(filename, filename);
                    Utils.writeObject(_REMOVAL, removed);
                    File inCWD = Utils.join(_CWD, filename);
                    if (inCWD.exists()) {
                        inCWD.delete();
                    }
                    stagedOrTracked = true;
                }
            }
            if (!file.exists() && !stagedOrTracked) {
                System.out.println("File does not exist.");
                return;
            }
        }
        String[] added = _ADDITION.list();
        for (String x : added) {
            if (x.equals(filename)) {
                File toRemove = new File(".gitlet/staging/addition/"
                        + filename);
                toRemove.delete();
                stagedOrTracked = true;
            }
        }
        if (!stagedOrTracked) {
            System.out.println("No reason to remove the file.");
        }
    }

    /** The rm command for branch.
     *
     * @param branchName the name of the branch.
     */
    public void rmBranch(String branchName) {
        String[] branches = _BRANCH.list();
        boolean exists = false;
        for (String x : branches) {
            if (x.equals(branchName)) {
                exists = true;
            }
        }
        if (!exists) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File branch = Utils.join(Utils.join(_BRANCH, branchName), "branch");
        HashMap<String, String> inBranch = Utils.readObject(branch,
                HashMap.class);
        String currbranch = Utils.readObject(_HEAD, String[].class)[0];
        File head = new File(".gitlet/branch/" + currbranch + "/head");
        String headID = Utils.readContentsAsString(head);
        if (inBranch.containsValue(headID)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File other = Utils.join(_BRANCH, branchName);
        branch.delete();
        File deleteHead = Utils.join(other, "head");
        deleteHead.delete();
        other.delete();
    }

    /** The reset command.
     *
     * @param id the id of file to be reset.
     * @throws IOException
     */
    public void reset(String id) throws IOException {
        File f = new File(".gitlet/commits/" + id);
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = findCommit(id);
        HashMap<String, Blob> blobs = commit.getBlobs();
        String branch = Utils.readObject(_HEAD, String[].class)[0];
        File head = new File(".gitlet/branch/" + branch + "/head");
        String headID = Utils.readContentsAsString(head);
        if (headID.length() == 0) {
            Commit initial = new Commit("initial commit", null);
            headID = initial.getIdentifier();
        }
        Commit curr = findCommit(headID);
        for (String file : _CWD.list()) {
            if (Utils.join(_CWD, file).isDirectory()) {
                continue;
            }
            Blob blob = new Blob(file);
            if (!curr.getBlobs().containsKey(file)
                    || !curr.getBlobs().get(file).getID().
                    equals(blob.getID())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
            if (!commit.getBlobs().containsKey(file)) {
                Utils.join(_CWD, file).delete();
            }
        }
        for (Blob x : blobs.values()) {
            checkout(id, x.getFilename());
        }
        for (String name : _ADDITION.list()) {
            if (!commit.getBlobs().containsKey(name)) {
                Utils.join(_ADDITION, name).delete();
            }
        }
        Utils.writeContents(head, id);
    }

    /** A helper to find the commit with given id.
     *
     * @param id the id of the commit.
     * @return
     */
    public Commit findCommit(String id) {
        File f = new File(".gitlet/commits/" + id);
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            throw new GitletException();
        } else {
            return Utils.readObject(f, Commit.class);
        }
    }

    /** Current Working Directory.*/
    private File _CWD = new File(".");

    /** The Gitlet Directory. */
    private File _GITLET = new File(".gitlet");

    /** The staging Directory. */
    private File _STAGINGAREA = new File(".gitlet/staging");

    /** The addition Directory. */
    private File _ADDITION = new File(".gitlet/staging/addition");

    /** The Removal Directory. */
    private File _REMOVAL = new File(".gitlet/staging/removal");

    /** The Commit Directory. */
    private File _COMMITS = new File(".gitlet/commits");

    /** The Blob Directory. */
    private File _BLOBS = new File(".gitlet/blobs");

    /** The Branch Directory. */
    private File _BRANCH = new File(".gitlet/branch");

    /** The Head Pointer. */
    private File _HEAD = new File(".gitlet/head");
}
