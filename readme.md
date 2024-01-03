# Program to find differences between two directories by file names and sizes

Performs recursive comparison of files by names (using relative paths) and sizes in two specified directories. For example, let's have two directories:
1. ```C:\DIR_1```
   * ```\NESTED_DIR_1```
     * ```\file1.jpg```
     * ```\file2.jpg```
     * ```\file3.jpg```
   * ```\NESTED_DIR_2```
     * ```\file1.jpg```

2. ```D:\DIR_2```
   * ```\NESTED_DIR_1```
     * ```\file1.jpg```
     * ```\file2.jpg```
   * ```\NESTED_DIR_3```
     * ```\file1.jpg```

The result of executing `java -jar folder-comparator.jar "C:\DIR_1" "D:\DIR_2"` will be as follows:
```
----------------------------------------------------
Files present ONLY in 'C:\DIR_1':
----------------------------------------------------
\NESTED_DIR_1\file3.jpg (size=4567)
\NESTED_DIR_2\file1.jpg (size=8765)

----------------------------------------------------
Files present ONLY in 'D:\DIR_2':
----------------------------------------------------
\NESTED_DIR_3\file1.jpg (size=1234)
```

### Mandatory arguments
1. ```path_to_directory_1```
2. ```path_to_directory_2```

### Optional arguments:
1. ```-c``` - copy the found files to new directories 'filesOnlyInFirstFolder' and 'filesOnlyInSecondFolder' next to the invoked jar file
2. ```-c1``` - copy files found only in directory 1 to directory 2
3. ```-c2``` - copy files found only in directory 2 to directory 1
4. ```-d1``` - delete files found only in directory 1
5. ```-d2``` - delete files found only in directory 2
6. ```-l``` - print the name of each found file
7. ```-p``` - run in parallel to speed up (print will be non-ordered)

### Launch examples:
+ ```java -jar folder-comparator.jar "D:\Projects\folder-comparator\test\DIR1" "D:\Projects\folder-comparator\test\DIR2" -l```
+ ```java -jar folder-comparator.jar "E:\Photo&Video\Main" "F:\Data - Ruslan\Photo&Video\Main" -c -d1 -d2 -l```
+ ```java -jar folder-comparator.jar "E:\Photo&Video" "F:\Data - Ruslan\Photo&Video" -c1 -c2```
