package ru.gasymov.foldercomparator;

public class Main {

    public static void main(String[] args) {
        System.out.println("Программа поиска различий между двумя директориями по именам файлов и их размерам");
        System.out.println(getArgsInfo());
        System.out.println("----------------------------------------------------");

        if (args.length < 2) {
            printErrorInfo();
            return;
        }
        final String folder1 = args[0];
        final String folder2 = args[1];
        var folderComparator = new FolderComparator(folder1, folder2);
        var comparingResult = folderComparator.compare();

        System.out.printf("Файлы, отсутствующие в '%s', но присутствующие в '%s':\n", folder1, folder2);
        if (comparingResult.filesAbsentInFirstFolder().isEmpty()) {
            System.out.println("Не найдено");
        } else {
            comparingResult.filesAbsentInFirstFolder().forEach(System.out::println);
        }
        System.out.println();

        System.out.printf("Файлы, отсутствующие в '%s', но присутствующие в '%s':\n", folder2, folder1);
        if (comparingResult.filesAbsentInSecondFolder().isEmpty()) {
            System.out.println("Не найдено");
        } else {
            comparingResult.filesAbsentInSecondFolder().forEach(System.out::println);
        }
        System.out.println("----------------------------------------------------");
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
                Пример запуска:
                    java -jar folder-comparator.jar [путь_к_директории_1] [путь_к_директории_2]
                """;
    }
}
