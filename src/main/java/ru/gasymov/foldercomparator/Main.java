package ru.gasymov.foldercomparator;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ru.gasymov.foldercomparator.handler.FilesHandler;
import ru.gasymov.foldercomparator.handler.Option;
import ru.gasymov.foldercomparator.handler.OptionsContainer;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            printErrorInfo();
            return;
        }
        final var folder1 = args[0];
        final var folder2 = args[1];
        System.out.printf("Got mandatory arguments: folder1 = '%s', folder2 = '%s'\n", folder1, folder2);

        final var optionalArgs = Arrays.stream(Arrays.copyOfRange(args, 2, args.length)).toList();
        final OptionsContainer optionsContainer = OptionsContainer.create(folder1, folder2)
                .setOptions(optionalArgs);
        optionsContainer.print();
        System.out.println();

        System.out.println("======================== COMPARING ============================");
        final var folderComparator = new FolderComparator(folder1, folder2);
        final var comparingResult = folderComparator.compare();
        System.out.println();

        System.out.println("======================== HANDLING ============================");

        final Runnable handleFirst = () ->
                new FilesHandler(comparingResult.filesOnlyInFirstFolder(), folder1, optionsContainer).handle();

        final Runnable handleSecond = () ->
                new FilesHandler(comparingResult.filesOnlyInSecondFolder(), folder2, optionsContainer).handle();

        if (optionsContainer.hasCommon(Option.Value.PARALLEL)) {
            try (var executor = Executors.newFixedThreadPool(2)) {
                executor.submit(handleFirst);
                executor.submit(handleSecond);
                executor.shutdown();
                if (!executor.awaitTermination(10, TimeUnit.MINUTES))
                    throw new RuntimeException("Main executor terminated by timeout");
            }
        } else {
            if (optionsContainer.has(folder2, Option.Value.DELETE) && !optionsContainer.has(folder1, Option.Value.DELETE)) {
                handleSecond.run();
                handleFirst.run();
            } else {
                handleFirst.run();
                handleSecond.run();
            }
        }

        System.out.println("======================== COMPLETED ============================");
    }

    private static void printErrorInfo() {
        System.out.println("Incorrect launch arguments");
        System.out.println(getArgsInfo());
    }

    private static String getArgsInfo() {
        return """
                Mandatory arguments:
                    1. [path_to_directory_1]
                    2. [path_to_directory_2]
                Optional arguments:
                    1. [-c] - copy the found files to directories 'filesOnlyInFirstFolder' and 'filesOnlyInSecondFolder' next to the invoked jar file
                    2. [-c1] - copy files found only in directory 1 to directory 2
                    3. [-c2] - copy files found only in directory 2 to directory 1
                    4. [-d1] - delete files found only in directory 1
                    5. [-d2] - delete files found only in directory 2
                    6. [-l] - print the name of each found file
                    7. [-p] - run in parallel to speed up (print will be non-ordered)
                Example execution:
                    java -jar folder-comparator.jar [path_to_directory_1] [path_to_directory_2] -p -c -l
                """;
    }
}
