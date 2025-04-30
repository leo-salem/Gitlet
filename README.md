# Gitlet
## Overview
• A distributed version control system similar to Git, command line program made in Java, used SHA1 hashing, data structures and DAG (Directed Acyclic Graph) traversal techniques such as
 BFS(Breadth First Search) to get the LCA (latest common ancestor). Retained persistence of files by saving different versions of them in a hidden directory.
and traverse the commits tree.
## Dependencies
1- Install Java : https://adoptium.net
<br>
2- Install Git :  http://git-scm.com/download/
## How to run
1- Download the repo and unzip it.
<br>
2- Open gitlet-master then open gitlet folder.
<br>
3- Open git bash and navigate to the path in step 3, use (cd ..) to go one directory to the back, (cd folder_name) to enter the specified folder and use (pwd) to know where you currently are. <ins>(NOTE:use the previous commands without the braces)</ins>
<br>
4- Compile the program by typing javac gitlet/*.java
<br>
5- Run the program by typing java gitlet.Main [THE COMMANDS GO HERE WITHOUT THE SQUARE BRACKETS EACH ARGUMENT SHOULD BE SEPARTED BY A SPACE OR CAPSULATE THE DESIRED ARGUMENT IN DOUBLE QUOTES]
<br>
6- Supported commands are listed below.
<br>
## Short demo:




https://github.com/user-attachments/assets/b44180fc-6bab-41bd-83ec-b4dc8df65cec


couldn't show all the cases and commands because it would take too long, you can try it yourself ISA everything will work fine.


## Methods
### init 
Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit.
### add
Adds a copy of the file as it currently exists to the staging area.
### commit
Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit.
### rm
Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).
### log 
Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits.
### global-log
Like log, except displays information about all commits ever made. The order of the commits does not matter.
### find
Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines.
### status
Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal.
### checkout -- fileName
Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
### checkout commitId -- fileName
Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
### checkout branchName
Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch.
### branch
Creates a new branch with the given name, and points it at the current head commit.
### rm-branch
Deletes the branch with the given name (This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that).
### reset
Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node.
### merge
Merges files from the given branch into the current branch.

:)
