package ru.gasymov.foldercomparator;

import java.io.File;
import java.util.List;
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
        final Map<MetaInfo, File> folder1Files = filesMapByMetaInfo(folder1);
        final Map<MetaInfo, File> folder2Files = filesMapByMetaInfo(folder2);

        final Set<MetaInfo> folder1MetaInfos = folder1Files.keySet();
        final Set<MetaInfo> folder2MetaInfos = folder2Files.keySet();

        final List<MetaInfo> absentInFolder1 = filter(folder2MetaInfos, folder1MetaInfos);
        final List<MetaInfo> absentInFolder2 = filter(folder1MetaInfos, folder2MetaInfos);

        return new ComparingResult(absentInFolder1, absentInFolder2);
    }

    private Map<MetaInfo, File> filesMapByMetaInfo(String folder) {
        return FileUtils.listFiles(new File(folder), null, true)
                .stream()
                .collect(Collectors.toMap(
                        it -> new MetaInfo(it.getPath().replace(folder, ""), FileUtils.sizeOf(it)),
                        Function.identity())
                );
    }

    private List<MetaInfo> filter(Set<MetaInfo> folder1MetaInfos, Set<MetaInfo> folder2MetaInfos) {
        return folder1MetaInfos
                .stream()
                .filter((it) -> !folder2MetaInfos.contains(it))
                .sorted()
                .toList();
    }
}

