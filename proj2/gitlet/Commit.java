package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  A Commit object records a snapshot of the files at a given point in time,
 *  along with metadata like its message, timestamp, and parent commits.
 */
public class Commit implements Serializable {

    private String outterdate;
    private String message;
    private String id;
    private String mergeId;
    private Commit parent;
    private Commit secondParent;

    private HashMap<String, String> files; // NO static

    public Commit(String message, Commit parent, Commit secondParent, String mergeId) {
        Date date = new Date();
        Formatter formatter = new Formatter();
        TimeZone.getDefault();
        formatter.format("%ta %tb %td %tT %tY %tz", date, date, date, date, date, date);
        outterdate = formatter.toString();
        this.message = message;
        this.parent = parent;
        this.secondParent = secondParent;
        this.mergeId = mergeId;
        this.files = new HashMap<>(); // Always initialize to prevent null
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setFiles(HashMap<String, String> files) {
        this.files = files; // Instance field, not static
    }

    public HashMap<String, String> getFiles() {
        return files;
    }

    public String getDate() {
        return outterdate;
    }

    public String getMessage() {
        return message;
    }

    public Commit getParent() {
        return parent;
    }

    public Commit getSecondParent() {
        return secondParent;
    }

    public String getMergeId() {
        return mergeId;
    }
}
