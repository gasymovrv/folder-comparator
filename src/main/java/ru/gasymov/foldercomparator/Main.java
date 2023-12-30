package ru.gasymov.foldercomparator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class Main {

    public static void main(String[] args) {
        System.out.println("Программа поиска различий между двумя директориями по именам файлов и их размерам");
        System.out.println(getArgsInfo());

        if (args.length < 2) {
            printErrorInfo();
            return;
        }
        final var folder1 = args[0];
        final var folder2 = args[1];
        final var optionalArgs = Arrays.stream(Arrays.copyOfRange(args, 2, args.length)).toList();
        var isCopyNeeded = optionalArgs.contains("-c");
        var isRemove1Needed = optionalArgs.contains("-r1");
        var isRemove2Needed = optionalArgs.contains("-r2");
        final var folderComparator = new FolderComparator(folder1, folder2);
        final var comparingResult = folderComparator.compare();

        System.out.println("====================================================");
        System.out.println();

        System.out.println("----------------------------------------------------");
        System.out.printf("Файлы, присутствующие ТОЛЬКО в '%s':\n", folder1);
        System.out.println("----------------------------------------------------");
        handleResult(comparingResult.filesOnlyInFirstFolder(), "filesOnlyInFirstFolder", isCopyNeeded, isRemove1Needed);

        System.out.println();

        System.out.println("----------------------------------------------------");
        System.out.printf("Файлы, присутствующие ТОЛЬКО в '%s':\n", folder2);
        System.out.println("----------------------------------------------------");
        handleResult(comparingResult.filesOnlyInSecondFolder(), "filesOnlyInSecondFolder", isCopyNeeded, isRemove2Needed);

        System.out.println();
        System.out.println("====================================================");
    }

    private static void handleResult(Map<MetaInfo, File> absentFiles, String newDirName, boolean isCopyNeeded, boolean isRemoveNeeded) {
        if (absentFiles.isEmpty()) {
            System.out.println("Не найдено");
            return;
        }
        absentFiles.forEach((k, v) -> {
            System.out.println(k);
            try {
                if (isCopyNeeded) {
                    FileUtils.copyFile(v, new File(newDirName + k.relativePath()));
                }
                if (isRemoveNeeded) {
                    FileUtils.delete(v);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        if (isCopyNeeded) {
            System.out.printf("\nСкопированы в '%s'\n", newDirName);
        }
        if (isRemoveNeeded) {
            System.out.println("\nУдалены");
        }
    }

    private static void printErrorInfo() {
        System.out.println("Неверные параметры запуска.");
        System.out.println(getArgsInfo());
    }

    private static String getArgsInfo() {
        return """
                Обязательные аргументы:
                    1.[путь_к_директории_1]
                    2.[путь_к_директории_2]
                Необязательные аргументы:
                    1.[-c] - скопировать найденные файлы в директории 'filesOnlyInFirstFolder' и 'filesOnlyInSecondFolder' рядом с вызываемым jar файлом
                    1.[-r1] - удалить файлы найденные только в директории 1
                    1.[-r2] - удалить файлы найденные только в директории 2
                Пример запуска:
                    java -jar folder-comparator.jar [путь_к_директории_1] [путь_к_директории_2]
                """;
    }
}
