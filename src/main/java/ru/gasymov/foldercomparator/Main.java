package ru.gasymov.foldercomparator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ru.gasymov.foldercomparator.handler.CopyOption;
import ru.gasymov.foldercomparator.handler.FilesHandler;
import ru.gasymov.foldercomparator.handler.Option;

public class Main {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Program to find differences between two directories by file names and sizes");
        System.out.println(getArgsInfo());

        if (args.length < 2) {
            printErrorInfo();
            return;
        }
        final var folder1 = args[0];
        final var folder2 = args[1];

        System.out.println("======================== COMPARING ============================");
        final var folderComparator = new FolderComparator(folder1, folder2);
        final var comparingResult = folderComparator.compare();
        System.out.println();

        final var optionalArgs = Arrays.stream(Arrays.copyOfRange(args, 2, args.length)).toList();
        final var commonOptions = new HashSet<Option>();
        if (optionalArgs.contains("-l")) {
            commonOptions.add(new Option(Option.Value.DETAILED_PRINT));
        }
        final boolean isParallel = optionalArgs.contains("-p");
        if (isParallel) {
            commonOptions.add(new Option(Option.Value.PARALLEL));
        }

        System.out.println("======================== HANDLING ============================");

        final Runnable handleFirst = () -> {
            final var options = new HashSet<>(commonOptions);
            if (optionalArgs.contains("-c")) {
                options.add(new CopyOption("filesOnlyInFirstFolder"));
            }
            if (optionalArgs.contains("-c1")) {
                options.add(new CopyOption(folder2));
            }
            if (optionalArgs.contains("-d1")) {
                options.add(new Option(Option.Value.DELETE));
            }
            final var files = comparingResult.filesOnlyInFirstFolder();
            new FilesHandler(files, options).handle(folder1);
        };
        if (isParallel) EXECUTOR_SERVICE.submit(handleFirst);
        else handleFirst.run();

        final Runnable handleSecond = () -> {
            final var options = new HashSet<>(commonOptions);
            if (optionalArgs.contains("-c")) {
                options.add(new CopyOption("filesOnlyInSecondFolder"));
            }
            if (optionalArgs.contains("-c2")) {
                options.add(new CopyOption(folder1));
            }
            if (optionalArgs.contains("-d2")) {
                options.add(new Option(Option.Value.DELETE));
            }
            final var files = comparingResult.filesOnlyInSecondFolder();
            new FilesHandler(files, options).handle(folder2);
        };
        if (isParallel) EXECUTOR_SERVICE.submit(handleSecond);
        else handleSecond.run();

        EXECUTOR_SERVICE.shutdown();
        var isTerminated = EXECUTOR_SERVICE.awaitTermination(5, TimeUnit.MINUTES);
        if (isTerminated) {
            System.out.println("======================== COMPLETED ============================");
        } else {
            System.out.println("======================== TIMEOUT ============================");
        }
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
                    6. [-l] - print the name of each found file
                    7. [-p] - run in parallel to speed up
                Example execution:
                    java -jar folder-comparator.jar [path_to_directory_1] [path_to_directory_2]
                """;
    }
}
