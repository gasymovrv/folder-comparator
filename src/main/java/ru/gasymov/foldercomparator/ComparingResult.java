package ru.gasymov.foldercomparator;

import java.io.File;
import java.util.Map;

public record ComparingResult(
        Map<MetaInfo, File> filesOnlyInFirstFolder,
        Map<MetaInfo, File> filesOnlyInSecondFolder
) {
}
