package ru.gasymov.foldercomparator;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;

public class FolderComparator {

    private final String folder1;
    private final String folder2;

    public FolderComparator(String folder1, String folder2) {
        this.folder1 = folder1;
        this.folder2 = folder2;
    }

    public ComparingResult compare() {
        System.out.println("Comparing...");
        final var folder1Files = filesMapByMetaInfo(folder1);
        final var folder2Files = filesMapByMetaInfo(folder2);

        var result = new ComparingResult(
                leaveFilesOnlyInFirstSource(folder1Files, folder2Files.keySet()),
                leaveFilesOnlyInFirstSource(folder2Files, folder1Files.keySet())
        );
        System.out.println("Comparing completed");
        return result;
    }

    private Map<MetaInfo, File> filesMapByMetaInfo(String folder) {
        return FileUtils.listFiles(new File(folder), null, true)
                .stream()
                .collect(Collectors.toMap(
                        it -> new MetaInfo(it.getPath().replace(folder, ""), FileUtils.sizeOf(it)),
                        Function.identity())
                );
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

