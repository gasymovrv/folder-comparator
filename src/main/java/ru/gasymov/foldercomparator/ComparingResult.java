package ru.gasymov.foldercomparator;

import java.util.List;

public record ComparingResult(
        List<MetaInfo> filesAbsentInFirstFolder,
        List<MetaInfo> filesAbsentInSecondFolder
) {

}
