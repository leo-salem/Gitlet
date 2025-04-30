package gitlet;

import java.io.File;
import static gitlet.RepositoryHelperMethods.*;
import static gitlet.Utils.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.io.Serializable;
import java.util.*;
/** Represents a gitlet repository.
 *
 *
 *  @author leo-salem
 */
public class Repository {


    private static final File CWD = new File(System.getProperty("user.dir"));
    private static final File GITLET_DIR = join(CWD, ".gitlet");
    private static File BLOB_DIR = join(GITLET_DIR, "blob");
    private static File COMMIT_DIR = join(GITLET_DIR, "commit");
    private static File BRANCH_DIR = join(GITLET_DIR, "branch");
    private static File Addition_STAGGING_AREA = join(GITLET_DIR, "stagging_add");
    private static File removal_STAGGING_AREA = join(GITLET_DIR, "stagging_remove");
    private static File currentMap = join(GITLET_DIR, "currentFiles");
    private static File head = join(GITLET_DIR, "head");
    private static File AllBranches = join(GITLET_DIR, "AllBranches");
    private static File UntouchableFiles= join(GITLET_DIR, "UntouchableFiles");
    public static void CheckInitialization(File check) {
        if (!check.exists() ||
                !currentMap.exists() ||
                !Addition_STAGGING_AREA.exists() ||
                !removal_STAGGING_AREA.exists() ||
                !head.exists() ||
                !AllBranches.exists() ||
                !BRANCH_DIR.exists() ||
                !COMMIT_DIR.exists()) {

            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
    public static void InitializeForbidenArea() {
        makeNewFile(UntouchableFiles);
        HashMap<String, String> noFiles = new HashMap<>();
        noFiles.put("pom.xml", "UnTouchableFiles");
        noFiles.put("Makefile", "UnTouchableFiles");
        noFiles.put("gitlet-design.md", "UnTouchableFiles");  // just temporary ;)
        noFiles.put("testing", "UnTouchableFiles");
        noFiles.put("target", "UnTouchableFiles");
        noFiles.put("gitlet", "UnTouchableFiles");
        noFiles.put(".idea", "UnTouchableFiles");
        noFiles.put(".gitlet", "UnTouchableFiles");
        writeObject(UntouchableFiles, noFiles);
    }
    /*********************************************************************/

    public static void init() {
        if (GITLET_DIR.exists()) {
            String s = "A Gitlet version-control system already exists in the current directory.";
            System.out.println(s);
            System.exit(0);
        }
        //setup presistence
        GITLET_DIR.mkdir();
        BLOB_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BRANCH_DIR.mkdir();
        InitializeForbidenArea();
        makeNewFile(removal_STAGGING_AREA);
        makeNewFile(Addition_STAGGING_AREA);
        makeNewFile(currentMap);
        makeNewFile(head);
        makeNewFile(AllBranches);
        writeObject(Addition_STAGGING_AREA, new HashMap<String, String>());
        writeObject(removal_STAGGING_AREA, new HashMap<String, String>());
        writeObject(currentMap, new HashMap<String, String>());


        //intial commit
        String message = "initial commit";
        HashMap<String, String> commitFiles = new HashMap<>();
        Commit commit = new Commit(message, null,null,"none");
        commit.setFiles(commitFiles);
        String hashValue = sha1(serialize(commit));
        commit.setId(hashValue);
        File c = new File(COMMIT_DIR, hashValue);
        makeNewFile(c);
        writeObject(c, commit);
        Branch Default = new Branch("master", hashValue);
        File branch = new File(BRANCH_DIR, Default.getName());
        makeNewFile(branch);
        writeObject(branch, Default);
        HashSet<String> All = new HashSet<>();
        All.add(Default.getName());
        writeObject(AllBranches, All);
        writeContents(head, Default.getName());
    }

    /***********************************************************************/

    public static void add(String fileName) {
        CheckInitialization(GITLET_DIR);
        File checkfile = join(CWD, fileName);
        if (!checkfile.exists()) {
            HashMap<String, String> Readcurrent =
                    currentMap.exists() ? readObject(currentMap, HashMap.class) : new HashMap<>();
            if (Readcurrent.containsKey(fileName)) {
                //user delete it manually
                //now we had to delete it from current
                HashMap<String, String> ReadAddStage =
                        Addition_STAGGING_AREA.exists() ? readObject(Addition_STAGGING_AREA, HashMap.class) : new HashMap<>();
                HashMap<String, String> ReadRemoveStage =
                        removal_STAGGING_AREA.exists() ? readObject(removal_STAGGING_AREA, HashMap.class) : new HashMap<>();
                ReadRemoveStage.put(fileName, Readcurrent.get(fileName));//get it first from current
                ReadAddStage.remove(fileName);// delete it if it's had staged
                Readcurrent.remove(fileName);//delete from current after gave value to both stages
                writeObject(currentMap, Readcurrent);
                writeObject(Addition_STAGGING_AREA, ReadAddStage);
                writeObject(removal_STAGGING_AREA, ReadRemoveStage);
            } else {
                System.out.println("File does not exist.");
            }
        } else {
            HashMap<String, String> Readcurrent =
                    currentMap.exists() ? readObject(currentMap, HashMap.class) : new HashMap<>();
            HashMap<String, String> ReadAddStage =
                    Addition_STAGGING_AREA.exists() ? readObject(Addition_STAGGING_AREA, HashMap.class) : new HashMap<>();
            HashMap<String, String> ReadRemoveStage =
                    removal_STAGGING_AREA.exists() ? readObject(removal_STAGGING_AREA, HashMap.class) : new HashMap<>();
            String hashValue = Hashing(checkfile);
            if (Readcurrent.containsKey(fileName)) {
                if (ReadAddStage.containsKey(fileName)) {
                    if (!ReadAddStage.get(fileName).equals(hashValue)) {
                        File delteBloob = join(BLOB_DIR, ReadAddStage.get(fileName));
                        delteBloob.delete();
                        File Blob = join(BLOB_DIR, hashValue);
                        writeContents(Blob, readContents(checkfile));
                        Readcurrent.replace(fileName, hashValue);
                        ReadAddStage.replace(fileName, hashValue);
                        writeObject(currentMap, Readcurrent);
                        writeObject(Addition_STAGGING_AREA, ReadAddStage);
                    }
                } else {
                    if (!Readcurrent.get(fileName).equals(hashValue)) {
                        File Blob = join(BLOB_DIR, hashValue);
                        writeContents(Blob, readContents(checkfile));
                        Readcurrent.replace(fileName, hashValue);
                        ReadAddStage.put(fileName, hashValue);
                        writeObject(currentMap, Readcurrent);
                        writeObject(Addition_STAGGING_AREA, ReadAddStage);
                    }
                }
            } else {
                //check if this file in removalStage
                if (ReadRemoveStage.containsKey(fileName)) {
                    ReadRemoveStage.remove(fileName);
                } else {
                    ReadAddStage.put(fileName, hashValue);
                }
                File newBlob = join(BLOB_DIR, hashValue);
                writeContents(newBlob, readContents(checkfile));
                Readcurrent.put(fileName, hashValue);
                writeObject(currentMap, Readcurrent);
                writeObject(Addition_STAGGING_AREA, ReadAddStage);
                writeObject(removal_STAGGING_AREA, ReadRemoveStage);
            }

        }
    }


    /*******************************************************************/

    public static void commit(String message) {
        CheckInitialization(GITLET_DIR);
        HashMap<String, String> Readcurrent =
                currentMap.exists() ? readObject(currentMap, HashMap.class) : new HashMap<>();
        HashMap<String, String> ReadAddStage =
                Addition_STAGGING_AREA.exists() ? readObject(Addition_STAGGING_AREA, HashMap.class) : new HashMap<>();
        HashMap<String, String> ReadRemoveStage =
                removal_STAGGING_AREA.exists() ? readObject(removal_STAGGING_AREA, HashMap.class) : new HashMap<>();
        if (ReadAddStage.isEmpty() && ReadRemoveStage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        String CurrentBranchName = readContentsAsString(head);
        Branch currentBranch = readObject(join(BRANCH_DIR, CurrentBranchName), Branch.class);
        Commit parent = readObject(join(COMMIT_DIR, currentBranch.getCurrentcommit()), Commit.class);
        Commit com = new Commit(message, parent,null,"none");
        com.setFiles(Readcurrent);
        String hash = sha1(serialize(com));
        com.setId(hash);
        currentBranch.setCurrentcommit(hash);
        writeObject(join(BRANCH_DIR, CurrentBranchName), currentBranch);
        File c = join(COMMIT_DIR, hash);
        makeNewFile(c);
        writeObject(c, com);
        ReadAddStage.clear();
        ReadRemoveStage.clear();
        writeObject(Addition_STAGGING_AREA, ReadAddStage);
        writeObject(removal_STAGGING_AREA, ReadRemoveStage);
    }

    /*******************************************************************/

    public static void rm(String fileName) {
        CheckInitialization(GITLET_DIR);
        File checkfile = join(CWD, fileName);

        HashMap<String, String> readCurrent =
                currentMap.exists() ? readObject(currentMap, HashMap.class) : new HashMap<>();
        HashMap<String, String> readAddStage =
                Addition_STAGGING_AREA.exists() ? readObject(Addition_STAGGING_AREA, HashMap.class) : new HashMap<>();
        HashMap<String, String> readRemoveStage =
                removal_STAGGING_AREA.exists() ? readObject(removal_STAGGING_AREA, HashMap.class) : new HashMap<>();

        if (!checkfile.exists()) {
            if (readCurrent.containsKey(fileName)) {
                // File manually deleted, now update the internal state
                readRemoveStage.put(fileName, readCurrent.get(fileName));
                readAddStage.remove(fileName);
                readCurrent.remove(fileName);
                writeObject(currentMap, readCurrent);
                writeObject(Addition_STAGGING_AREA, readAddStage);
                writeObject(removal_STAGGING_AREA, readRemoveStage);
            } else {
                System.out.println("File does not exist.");
            }
        } else {
            String hashValue = Hashing(checkfile);

            if (readAddStage.containsKey(fileName) && hashValue.equals(readAddStage.get(fileName))) {
                // If file is staged for addition but now needs to be unstaged
                readAddStage.remove(fileName);
                readCurrent.remove(fileName);
                writeObject(currentMap, readCurrent);
                writeObject(Addition_STAGGING_AREA, readAddStage);
            } else {
                String currentBranchName = readContentsAsString(head);
                Branch currentBranch = readObject(join(BRANCH_DIR, currentBranchName), Branch.class);
                Commit com = readObject(join(COMMIT_DIR, currentBranch.getCurrentcommit()), Commit.class);

                String committedHash = com.getFiles() != null ? com.getFiles().get(fileName) : null;

                if (committedHash != null && committedHash.equals(hashValue)) {
                    readAddStage.remove(fileName);
                    readRemoveStage.put(fileName, readCurrent.get(fileName));
                    readCurrent.remove(fileName);
                    restrictedDelete(checkfile);
                    writeObject(currentMap, readCurrent);
                    writeObject(Addition_STAGGING_AREA, readAddStage);
                    writeObject(removal_STAGGING_AREA, readRemoveStage);
                } else {
                    System.out.println("No reason to remove the file.");
                }
            }
        }
    }

    /*******************************************************************/

    public static void log() {

        CheckInitialization(GITLET_DIR);
        String CurrentBranchName = readContentsAsString(head);
        Branch currentBranch = readObject(join(BRANCH_DIR, CurrentBranchName), Branch.class);
        Commit com = readObject(join(COMMIT_DIR, currentBranch.getCurrentcommit()), Commit.class);
        while (com != null) {
            System.out.println("===");
            System.out.println("commit " + com.getId());
            if (!com.getMergeId().equals("none")) {
                System.out.println("Merge: " + com.getMergeId());
            }
            System.out.println("Date: " + com.getDate());
            System.out.println(com.getMessage());
            System.out.println();
            com = com.getParent();
        }
    }

    /*******************************************************************/

    public static void global_log() {

        CheckInitialization(GITLET_DIR);
        File[] files = join(COMMIT_DIR).listFiles();
        for (File file : files) {
            Commit com = readObject(file, Commit.class);
            System.out.println("===");
            System.out.println("commit " + com.getId());
            if (!com.getMergeId().equals("none")) {
                System.out.println("Merge: " + com.getMergeId());
            }
            System.out.println("Date: " + com.getDate());
            System.out.println(com.getMessage() + "\n");
        }
    }

    /*******************************************************************/

    public static void find(String message) {
        CheckInitialization(GITLET_DIR);
        File[] files = join(COMMIT_DIR).listFiles();
        boolean check = true;
        for (File it : files) {
            Commit com = readObject(it, Commit.class);
            if (com.getMessage().equals(message)) {
                check = false;
                System.out.println(com.getId());
            }
        }
        if (check) {
            System.out.println("Found no commit with that message.");
        }
    }

    /*******************************************************************/
    public static void status() {
        CheckInitialization(GITLET_DIR);
        System.out.println("=== Branches ===");
        HashSet<String> All = readObject(AllBranches, HashSet.class);
        String CurrentBranchName = readContentsAsString(head);
        System.out.println("*" + CurrentBranchName);
        for (String it : All) {
            if (!it.equals(CurrentBranchName)) {
                System.out.println(it);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        HashMap<String, String> ReadAddStage =
                Addition_STAGGING_AREA.exists() ? readObject(Addition_STAGGING_AREA, HashMap.class) : new HashMap<>();
        for (Map.Entry<String, String> set : ReadAddStage.entrySet()) {
            System.out.println(set.getKey());
        }
        System.out.println("\n=== Removed Files ===");
        HashMap<String, String> ReadRemoveStage =
                removal_STAGGING_AREA.exists() ? readObject(removal_STAGGING_AREA, HashMap.class) : new HashMap<>();
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : ReadRemoveStage.entrySet()){
            File test = join(entry.getKey());
            if (test.exists()) {
                if (entry.getValue().equals(Hashing(test))) {
                    list.add(entry.getKey());
                }
            }
        }
        for (String it : list) {
            ReadRemoveStage.remove(it);
        }
        for (String it : ReadRemoveStage.keySet()) {
            System.out.println(it);
        }
        HashMap<String, String> AllCombinationFiles = new HashMap<>();
        HashMap<String, String> ReadCurrentMap=readObject(currentMap,HashMap.class);
        getFiles(AllCombinationFiles, CWD, join(CWD, ".gitlet"));
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (Map.Entry<String, String> entry : ReadCurrentMap.entrySet()){
            if (AllCombinationFiles.containsKey(entry.getKey())) {
                if (!entry.getValue().equals(AllCombinationFiles.get(entry.getKey()))) {
                    System.out.println(entry.getKey() + " (modified)");
                }
                AllCombinationFiles.remove(entry.getKey());
            } else {
                System.out.println(entry.getKey() + " (deleted)");
            }
        }
        System.out.println("\n=== Untracked Files ===");
        for (Map.Entry<String, String> entry : AllCombinationFiles.entrySet()){
            System.out.println(entry.getKey());
        }
    }

    /*******************************************************************/
    public static void branch(String BranchName) {
        CheckInitialization(GITLET_DIR);
        HashSet<String> All = readObject(AllBranches, HashSet.class);
        if (All.contains(BranchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch currentBranch= readObject(join(BRANCH_DIR, readContentsAsString(head)), Branch.class);
        Branch branch = new Branch(BranchName, currentBranch.getCurrentcommit());
        All.add(BranchName);
        File file = join(BRANCH_DIR, BranchName);
        makeNewFile(file);
        writeObject(file, branch);
        writeObject(AllBranches, All);
    }
    /*******************************************************************/

    public static void checkoutWithFileName(String filename ) {
        CheckInitialization(GITLET_DIR);
        String CurrentBranchName = readContentsAsString(head);
        Branch currentBranch = readObject(join(BRANCH_DIR, CurrentBranchName), Branch.class);
        Commit com = readObject(join(COMMIT_DIR, currentBranch.getCurrentcommit()), Commit.class);
        if (com.getFiles().containsKey(filename) ) {
            File blob = join(BLOB_DIR, com.getFiles().get(filename));
            File cwdFile = join(CWD, filename);
            createPathIfNotExists(filename);
            writeContents(cwdFile, readContents(blob));
            HashMap<String, String> stagedAdd = readObject(Addition_STAGGING_AREA, HashMap.class);
            stagedAdd.remove(filename);
            writeObject(Addition_STAGGING_AREA, stagedAdd);
        }
        else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void checkoutCommitId(String id,String filename ) {
        CheckInitialization(GITLET_DIR);
        File commitFile = join(COMMIT_DIR, id);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit com = readObject(join(COMMIT_DIR, id), Commit.class);
        if (com.getFiles().containsKey(filename) ) {
            File blob = join(BLOB_DIR, com.getFiles().get(filename));
            File cwdFile = join(CWD, filename);
            createPathIfNotExists(filename);
            writeContents(cwdFile, readContents(blob));
            HashMap<String, String> stagedAdd = readObject(Addition_STAGGING_AREA, HashMap.class);
            stagedAdd.remove(filename);
            writeObject(Addition_STAGGING_AREA, stagedAdd);
        }
        else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    public static void checkoutBranch(String BranchName ) {
        CheckInitialization(GITLET_DIR);
        File checkBranch=join(BRANCH_DIR, BranchName);
        if (!checkBranch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String CurrentBranchName = readContentsAsString(head);
        Branch currentBranch = readObject(join(BRANCH_DIR,CurrentBranchName), Branch.class);
        Commit CurrentCommit = readObject(join(COMMIT_DIR, currentBranch.getCurrentcommit()), Commit.class);
        if(CurrentBranchName.equals(BranchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        HashMap <String, String> AllCurrentFiles = new HashMap<>();
        getFiles(AllCurrentFiles, CWD, join(CWD, ".gitlet"));
        HashMap <String, String> ReadCurrentMap=readObject(currentMap, HashMap.class);
        HashMap <String, String>CurrentCommitFiles=CurrentCommit.getFiles();
        Branch TargetBranch = readObject(join(BRANCH_DIR, BranchName), Branch.class);
        Commit TargetCommit = readObject(join(COMMIT_DIR, TargetBranch.getCurrentcommit()), Commit.class);
        HashMap <String, String>TargetCommitFiles=TargetCommit.getFiles();
        AllCurrentFiles.forEach((key, value) -> {
            if (TargetCommitFiles.containsKey(key)) {
                if (!value.equals(CurrentCommitFiles.get(key))) {
                    String s = "There is an untracked file in the way;"
                            + " delete it, or add and commit it first.";
                    System.out.println(s);
                    System.exit(0);
                }
            }
        });
        deleteFiles(CWD);
        ReadCurrentMap.clear();
        TargetCommitFiles.forEach((key, value) -> {
            ReadCurrentMap.put(key, value);
            createPathIfNotExists(key);
            File cwdFile = join(CWD, key);
            File blobFile = join(BLOB_DIR, value);
            makeNewFile(cwdFile);
            writeContents(cwdFile, readContents(blobFile));
        });
        writeObject(currentMap, ReadCurrentMap);
        writeContents(head, BranchName);
    }

    /*******************************************************************/
    public static void rmBranch(String BranchName) {
        CheckInitialization(GITLET_DIR);
        HashSet<String> All=readObject(AllBranches, HashSet.class);
        if (!All.contains(BranchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String CurrentBranchName = readContentsAsString(head);
        if(CurrentBranchName.equals(BranchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        All.remove(BranchName);
        File DeletedBranch= join(BRANCH_DIR,BranchName);
        DeletedBranch.delete();
        writeObject(AllBranches,All);
    }
    /*******************************************************************/
    public static void reset(String TargetCommitId){
        CheckInitialization(GITLET_DIR);
        File checkCommit=join(COMMIT_DIR, TargetCommitId);
        if (!checkCommit.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        String CurrentBranchName = readContentsAsString(head);
        Branch currentBranch = readObject(join(BRANCH_DIR,CurrentBranchName), Branch.class);
        Commit CurrentCommit = readObject(join(COMMIT_DIR, currentBranch.getCurrentcommit()), Commit.class);
        HashMap <String, String> AllCurrentFiles = new HashMap<>();
        getFiles(AllCurrentFiles, CWD, join(CWD, ".gitlet"));
        HashMap <String, String> ReadCurrentMap=readObject(currentMap, HashMap.class);
        HashMap <String, String>CurrentCommitFiles=CurrentCommit.getFiles();
        Commit TargetCommit = readObject(join(COMMIT_DIR, TargetCommitId), Commit.class);
        HashMap <String, String>TargetCommitFiles=TargetCommit.getFiles();
        AllCurrentFiles.forEach((key, value) -> {
            if (TargetCommitFiles.containsKey(key)) {
                if (!value.equals(CurrentCommitFiles.get(key))) {
                    String s = "There is an untracked file in the way;"
                            + " delete it, or add and commit it first.";
                    System.out.println(s);
                    System.exit(0);
                }
            }
        });
        deleteFiles(CWD);
        ReadCurrentMap.clear();
        TargetCommitFiles.forEach((key, value) -> {
            ReadCurrentMap.put(key, value);
            createPathIfNotExists(key);
            File cwdFile = join(CWD, key);
            File blobFile = join(BLOB_DIR, value);
            makeNewFile(cwdFile);
            writeContents(cwdFile, readContents(blobFile));
        });
        currentBranch.setCurrentcommit(TargetCommitId);
        writeObject(join(BRANCH_DIR,readContentsAsString(head)),currentBranch);
        writeObject(currentMap, ReadCurrentMap);
    }
    /*******************************************************************/
    public static void merge(String BranchName) {
        CheckInitialization(GITLET_DIR);
        File MergedBranchFile = join(BRANCH_DIR, BranchName);
        if (!MergedBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String CurrentBranchName = readContentsAsString(head);
        Branch currentBranch = readObject(join(BRANCH_DIR, CurrentBranchName), Branch.class);
        Commit CurrentCommit = readObject(join(COMMIT_DIR, currentBranch.getCurrentcommit()), Commit.class);

        if (CurrentBranchName.equals(BranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        HashMap<String, String> CurrentCommitFiles = CurrentCommit.getFiles();
        Branch MergedBranch = readObject(join(BRANCH_DIR, BranchName), Branch.class);
        Commit MergedCommit = readObject(join(COMMIT_DIR, MergedBranch.getCurrentcommit()), Commit.class);
        HashMap<String, String> MergedFiles = MergedCommit.getFiles();

        // Check for uncommitted changes
        HashMap<String, String> ReadAddStage = readObject(Addition_STAGGING_AREA, HashMap.class);
        HashMap<String, String> ReadRemoveStage = readObject(removal_STAGGING_AREA, HashMap.class);
        if (!ReadAddStage.isEmpty() || !ReadRemoveStage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        // Check for untracked files
        HashMap<String, String> AllCurrentFiles = new HashMap<>();
        getFiles(AllCurrentFiles, CWD, join(CWD, ".gitlet"));
        HashMap<String, String> ReadCurrentMap = readObject(currentMap, HashMap.class);
        if (!ReadCurrentMap.equals(AllCurrentFiles)) {
            String s = "There is an untracked file in the way; delete it, or add and commit it first.";
            System.out.println(s);
            System.exit(0);
        }

        // Collect commits from the head to root
        HashSet<String> CommitsFromHeadToRoot = new HashSet<>();
        Commit TempCurrentCommit = readObject(join(COMMIT_DIR, currentBranch.getCurrentcommit()), Commit.class);
        while (TempCurrentCommit != null) {
            CommitsFromHeadToRoot.add(TempCurrentCommit.getId());
            TempCurrentCommit = TempCurrentCommit.getParent();
        }

        Commit TempMergedCommit = readObject(join(COMMIT_DIR, MergedBranch.getCurrentcommit()), Commit.class);
        Commit SplitCommit = null;
        while (true) {
            if (CommitsFromHeadToRoot.contains(TempMergedCommit.getId())) {
                SplitCommit = readObject(join(COMMIT_DIR, TempMergedCommit.getId()), Commit.class);
                break;
            }
            TempMergedCommit = TempMergedCommit.getParent();
        }

        // Handle special cases based on commit relationships
        if (SplitCommit.getId().equals(MergedCommit.getId())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        if (SplitCommit.getId().equals(CurrentCommit.getId())) {
            checkoutBranch(BranchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        // Conflict detection and resolution
        boolean conflict = false;
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(SplitCommit.getFiles().keySet());
        allFiles.addAll(CurrentCommit.getFiles().keySet());
        allFiles.addAll(MergedCommit.getFiles().keySet());

        for (String filename : allFiles) {
            String splitBlob = SplitCommit.getFiles().get(filename);
            String currentBlob = CurrentCommit.getFiles().get(filename);
            String givenBlob = MergedCommit.getFiles().get(filename);
            boolean inSplit = splitBlob != null;
            boolean inCurrent = currentBlob != null;
            boolean inGiven = givenBlob != null;

            boolean splitEqCurrent = Objects.equals(splitBlob, currentBlob);
            boolean splitEqGiven = Objects.equals(splitBlob, givenBlob);
            boolean currentEqGiven = Objects.equals(currentBlob, givenBlob);

            // Handle the different conflict cases
            if (inSplit && inCurrent && inGiven) {
                if (splitEqCurrent && !splitEqGiven) {
                    checkoutAndStage(givenBlob, filename);
                } else if (!splitEqCurrent && splitEqGiven) {
                    // No action required for identical versions
                } else if (splitEqCurrent && splitEqGiven) {
                    // No action required for unchanged versions
                } else {
                    handleConflict(currentBlob, givenBlob, filename);
                    conflict = true;
                }
            } else if (!inSplit && inCurrent && inGiven) {
                if (currentEqGiven) {
                    // No action required for unchanged files
                } else {
                    handleConflict(currentBlob, givenBlob, filename);
                    conflict = true;
                }
            } else if (!inSplit && !inCurrent && inGiven) {
                checkoutAndStage(givenBlob, filename);
            } else if (!inSplit && inCurrent && !inGiven) {
                // No action for files added only in current
            } else if (inSplit && inCurrent && !inGiven) {
                if (splitEqCurrent) {
                    restrictedDelete(join(CWD, filename));
                    ReadCurrentMap.remove(filename);
                    ReadRemoveStage.put(filename, splitBlob);
                } else {
                    handleConflict(currentBlob, null, filename);
                    conflict = true;
                }
            } else if (inSplit && !inCurrent && inGiven) {
                if (splitEqGiven) {
                    // No action required for identical versions
                } else {
                    handleConflict(null, givenBlob, filename);
                    conflict = true;
                }
            }
        }

        // Finalize merge
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }

        // Write changes to disk
        writeObject(Addition_STAGGING_AREA, ReadAddStage);
        writeObject(removal_STAGGING_AREA, ReadRemoveStage);
        writeObject(currentMap, ReadCurrentMap);

        // Create new commit after merge
        String MergeId = CurrentCommit.getId().substring(0, 7) + " " + MergedCommit.getId().substring(0, 7);
        String message = "Merged " + BranchName + " into " + CurrentBranchName + ".";
        Commit newCom = new Commit(message, CurrentCommit, MergedCommit, MergeId);
        newCom.setFiles(ReadCurrentMap);
        String hash = sha1(serialize(newCom));
        newCom.setId(hash);
        currentBranch.setCurrentcommit(hash);

        File commitFile = join(COMMIT_DIR, newCom.getId());
        writeObject(commitFile, newCom);
        writeObject(join(BRANCH_DIR, CurrentBranchName), currentBranch);
    }


    static void checkoutAndStage(String blobId, String filename) {
        HashMap<String, String> ReadCurrentMap = readObject(currentMap, HashMap.class);
        HashMap<String, String> ReadAddStage = readObject(Addition_STAGGING_AREA, HashMap.class);
        File blob = join(BLOB_DIR, blobId);
        File fileInCWD = join(CWD, filename);
        createPathIfNotExists(filename);
        makeNewFile(fileInCWD);
        writeContents(fileInCWD, readContents(blob));
        ReadAddStage.put(filename, blobId);
        ReadCurrentMap.put(filename, blobId);
    }

    static void handleConflict(String currentBlob, String givenBlob, String filename) {
        HashMap<String, String> ReadAddStage = readObject(Addition_STAGGING_AREA, HashMap.class);
        HashMap<String, String> ReadCurrentMap = readObject(currentMap, HashMap.class);
        String currentContent = (currentBlob == null) ? "" : readContentsAsString(join(BLOB_DIR, currentBlob));
        String givenContent = (givenBlob == null) ? "" : readContentsAsString(join(BLOB_DIR, givenBlob));
        String conflictContent = "<<<<<<< HEAD\n" + currentContent + "=======\n" + givenContent + ">>>>>>>\n";
        File fileInCWD = join(CWD, filename);
        createPathIfNotExists(filename);
        makeNewFile(fileInCWD);
        writeContents(fileInCWD, conflictContent);
        String hash = Hashing(fileInCWD);
        ReadAddStage.put(filename, hash);
        ReadCurrentMap.put(filename, hash);
        writeContents(join(BLOB_DIR, hash), conflictContent);
    }

    /*******************************************************************/

}