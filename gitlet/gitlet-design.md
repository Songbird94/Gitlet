# Gitlet Design Document

**Name**: Xiaoru Zhao

## Classes and Data Structures

### Commit

#### Instance Variables

* Message: Contains the message of a commit.
* Timestamp / date (Assigned by the constructor)
* Parent (String)
* Identifier (SHA1): the unique id of a commit.

### Repository

### Command

#### Instance Variables

* header: a pointer indicating the current commit
* master

### Main

## Algorithms

### Commit class

1. commit(String message, Commit parent): The class constructor, which creates a new commit object with its date and time created, log message, and id
2. commit(): set the field "blobs" with the blobs inside the staging area.
3. getMessage(): return the private field message
4. getParent(): return the private field parent
5. getTimestamp(): return the private field timestamp

### Command class

* Init(): initialize a commit.
  * start a commit that contains no files
  * commit message: initial commit
  * branch: master
  * timestamp: 00:00:00 UTC, Thursday, 1 January 1970
  * failure cases: if the directory is already initialized
* Add (String filename): add the file to staging area
  * add the copy of the file to the staging area
  * edge case: if the file with same file name is already in the staging area, update the content of the file
  * failure cases: if the file doesn't exit in the current working directory
* Commit (String message): saves tracked (or unchanged) files and the files in staging area in a new commit that constructed with the message.
  * saves a snapshot of tracked files in the current commit and staging area
  * a commit will automatically save the same files as its parent's snapshot
  * a commit will only update the contents of files in the staging area with different version from its parent's
  * something about removal command
  * failure cases:
    * if the staging area for adding has no files
    * if the message is blank
* rm(String filename): put the file into the stage of removal area and remove that file
  * if the file is in the staging area for addition, remove that file from the staging area
  * if the file is tracked in the current commit, move the file to the staging area for removal and remove tha file from the working directory
  * failure cases: if the file is not in the staging area for addition nor tracked by the head commit 
* log(): print out the commit history of the header commit branch.
  * start at the current head commit, print out the information about the commit and then repeat the process for the parent of the commit until the parent of the commit is null
  * failure cases: none
* Global-log: 
  * Displays information about all commits ever made.
* find: 
  * Prints out the ids of all commits that have the given commit 
  message, one per line. The commit message is a single operand; 
  to indicate a multiword message, put the operand in 
  quotation marks.
* status:
  * Displays what branches currently exist, and marks the current 
  branch with a *. Also displays what files have been staged for 
  addition or removal.
* checkout:
  * checkout -- [file name]
    * Takes the version of the file in the head commit, 
    the front of the current branch, and puts it in the working 
    directory, overwriting the version of the file that's already 
    there if there is one. The new version of the file is not staged.
  * checkout [commit id] -- [file name]
    * Takes the version of the file in the commit with the given id, 
    puts it in the working directory, ...
  * checkout [branch name]
    * Takes all files in the commit at the head of the given branch, ...
    * The given branch will now be considered the current branch (HEAD).
    * Files that are tracked in the current branch but not present in 
    the checked-out branch are deleted, the staging area cleared.
* branch(String name)
* rm-branch(String name)
* reset(id)
* merge(String name)

## Persistence

While making a commit, we only commit the changed files to the new commit and the other unchanged part is automatically inherit from the parent commit. Therefore
we need to make sure that when creating a new commit, the parent commit is recorded and its linked files is not changed.

In order to make sure each time we create a new commit, the last commit we created will not lose, I introduce the field of parent in commit class.
This variable can record the unique id of the parent commit. In this case, we indirectly link the commit object with its parent without actually creating
a linked list.

For each commit that is made, create a new object with the stage of
the tracked files and store it as a new file in the .gitlet directory,
so that the history of each commit can be recorded.

A directory called "staging area" inside .gitlet