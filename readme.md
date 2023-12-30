# Программа поиска различий между двумя директориями по именам файлов и их размерам

Осуществляет рекурсивное сравнение файлов по именам (используется относительный путь) и размерам в двух указанных директориях. Например имеем 2 такие директории:
1. ```C:\DIR_1```
   * ```\NESTED_DIR_1```
     * ```\file1.jpg```
     * ```\file2.jpg```
     * ```\file3.jpg```
   * ```\NESTED_DIR_2```
     * ```\file1.jpg```

1. ```D:\DIR_2```
   * ```\NESTED_DIR_1```
     * ```\file1.jpg```
     * ```\file2.jpg```
   * ```\NESTED_DIR_3```
     * ```\file1.jpg```

Результат выполнения `java -jar folder-comparator.jar "C:\DIR_1" "D:\DIR_2"` будет такой:
```
----------------------------------------------------
Файлы, присутствующие ТОЛЬКО в 'C:\DIR_1':
----------------------------------------------------
\NESTED_DIR_1\file3.jpg (size=4567)
\NESTED_DIR_2\file1.jpg (size=8765)

----------------------------------------------------
Файлы, оприсутствующие ТОЛЬКО в 'D:\DIR_2':
----------------------------------------------------
\NESTED_DIR_3\file1.jpg (size=1234)
```

### Обязательные аргументы
1. ```путь_к_директории_1```
1. ```путь_к_директории_2```

### Необязательные аргументы:
1. ```-c``` - скопировать найденные файлы в директории 'filesOnlyInFirstFolder' и 'filesOnlyInSecondFolder' рядом с вызываемым jar файлом
1. ```-r1``` - удалить файлы найденные только в директории 1
1. ```-r2``` - удалить файлы найденные только в директории 2

### Примеры запуска:
+ ```java -jar folder-comparator.jar "E:\Photo&Video\Main" "F:\Data - Ruslan\Photo&Video\Main"```
+ ```java -jar folder-comparator.jar "E:\Photo&Video" "F:\Data - Ruslan\Photo&Video" -c```
