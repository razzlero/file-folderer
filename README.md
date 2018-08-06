# File Folderer
File Folderer is a tool for grouping files and placing them into folders based on a provided regular expression.

### Technology

File Folderer is a relatively simple file and was made using Kotlin + Tornadofx.

For testing JUnit + JIMFS is used which allows testing with virtual files and directories.

## Usage

  * First select the directory which contains the files that you want to group.
  * Then enter a regular expression to group the files.
    * The regular expression should contain at least 1 capturing group.
    * Files matching the regular expression will be considered for grouping.
    * Files will be placed into folders based on what text the capturing groups match.
    * If there are multiple capturing groups the name will use all capturing groups separated by underscores.
  * Press the 'Preview' button.
  * You may then choose which files you would like to ignore.
    * Any files which exist in the destination are ignored by default. You may uncheck ignore to overwrite the existing file.
  * Then finally press 'Process' and your filles will be grouped according to the preview.

### Regular Expression Example
Let's say we have a folder containing the following files:
```
Account 1.txt
Account 2.txt
Account 3.txt
Notes 1.txt
Notes 2.txt
Picture 1.jpg
```

And let's say that we want to group all the "Account" files into 1 folder and we want to grop all the "Notes" files into another folder.
We could do that with the following regular expression:
```
(.+)\s+\d+\.txt
```
That would give use the following output:
```
Account/Account 1.txt
Account/Account 2.txt
Account/Account 3.txt
Notes/Notes 1.txt
Notes/Notes 2.txt
Picture 1.jpg
```

You'll notice that 'Picture 1.jpg' was not affected because it did not match the regex which specified '.txt' files.

The account files were places in an "Account" directory because the regular expression capturing group matched "Account" in the file names. Similarly the notes files were placed in a "Notes" directory because that's the text that match the capturing group for those files.

License
----

MIT
