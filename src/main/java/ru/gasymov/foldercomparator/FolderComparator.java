package ru.gasymov.foldercomparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.io.file.PathUtils;
import ru.gasymov.foldercomparator.handler.Option;
import ru.gasymov.foldercomparator.handler.OptionsContainer;

public class FolderComparator {
    private final String folder1;
    private final String folder2;
    private final boolean verbose;

    public FolderComparator(String folder1, String folder2, OptionsContainer optionsContainer) {
        this.folder1 = new File(folder1).getPath();
        this.folder2 = new File(folder2).getPath();
        this.verbose = optionsContainer.hasCommon(Option.Value.DETAILED_PRINT);
    }

    public ComparingResult compare() {
        if (verbose) {
            System.out.println(LocalDateTime.now() + " - Reading...");
        } else {
            System.out.println("Reading and comparing...");
        }
        final var folder1Files = readAndMapFilesByMetaInfo(folder1);
        final var folder2Files = readAndMapFilesByMetaInfo(folder2);
        if (verbose) {
            System.out.println(LocalDateTime.now() + " - Reading completed");
            System.out.println(LocalDateTime.now() + " - Comparing...");
        }
        var result = new ComparingResult(
                leaveFilesOnlyInFirstSource(folder1Files, folder2Files.keySet()),
                leaveFilesOnlyInFirstSource(folder2Files, folder1Files.keySet())
        );
        if (verbose) {
            System.out.println(LocalDateTime.now() + " - Comparing completed");
        }
        return result;
    }

    private Map<MetaInfo, File> readAndMapFilesByMetaInfo(String folder) {
        if (verbose) {
            System.out.println("Start reading folder: " + folder);
        }
        final long start = System.currentTimeMillis();
        var map = new ConcurrentHashMap<MetaInfo, File>();

        try (var paths = Files.find(Paths.get(folder), Integer.MAX_VALUE, (path, basicFileAttributes) -> Files.isRegularFile(path));
             var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            paths.forEach(path -> executor.submit(() -> {
                try {
                    map.put(new MetaInfo(path.toString().replace(folder, ""), PathUtils.sizeOf(path)), path.toFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            executor.shutdown();

            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                throw new RuntimeException("File-read executor terminated by timeout");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        final long stop = System.currentTimeMillis();
        if (verbose) {
            System.out.println("Completed reading folder: " + folder + ", duration: " + (stop - start) + " ms");
        }
        return map;
    }

    /**
     * Leave files that are found in the first source, but not in the second source.
     *
     * @param folder1FilesMap  first source files map.
     * @param folder2MetaInfos second source files meta info set.
     * @return files map with files that are found in the first source, but not in the second source.
     */
    private Map<MetaInfo, File> leaveFilesOnlyInFirstSource(Map<MetaInfo, File> folder1FilesMap, Set<MetaInfo> folder2MetaInfos) {
        return folder1FilesMap
                .entrySet()
                .stream()
                .filter(it -> !folder2MetaInfos.contains(it.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry<MetaInfo, File>::getKey,
                        Map.Entry<MetaInfo, File>::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new)
                );
    }
}

