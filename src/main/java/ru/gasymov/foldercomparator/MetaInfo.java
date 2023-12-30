package ru.gasymov.foldercomparator;

import java.util.Comparator;

public record MetaInfo(
        String relativePath,
        long size
) implements Comparable<MetaInfo> {

    @Override
    public int compareTo(MetaInfo o) {
        return Comparator.comparing(MetaInfo::relativePath)
              .thenComparingLong(MetaInfo::size)
              .compare(this, o);
    }

    @Override
    public String toString() {
        return String.format("%s (size=%d)", relativePath, size);
    }
}
