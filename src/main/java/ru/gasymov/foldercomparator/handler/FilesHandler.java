package ru.gasymov.foldercomparator.handler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import me.tongfei.progressbar.ProgressBar;
import ru.gasymov.foldercomparator.MetaInfo;

import static ru.gasymov.foldercomparator.handler.Option.Value.*;

public class FilesHandler {

    private final Map<MetaInfo, File> files;
    private final Set<Option> options;

    public FilesHandler(Map<MetaInfo, File> files, Set<Option> options) {
        this.files = files;
        this.options = options;
    }

    public void handle(String folder) {
        if (files.isEmpty()) {
            System.out.printf("Not found files present ONLY in '%s'\n\n", folder);
            return;
        }

        final var optionsMap = options.stream().collect(Collectors.groupingBy(
                Option::getValue,
                Collectors.mapping(Function.identity(), Collectors.toList()))
        );
        final var optValues = optionsMap.keySet();

        final Stream<Map.Entry<MetaInfo, File>> entryStream;
        if (optValues.contains(PARALLEL)) entryStream = files.entrySet().parallelStream();
        else entryStream = files.entrySet().stream();

        final var print = String.format("Found %s files present ONLY in '%s'", files.size(), folder);
        if (optValues.contains(DETAILED_PRINT)) System.out.println(print + ":");
        else System.out.println(print);

        doOptions(folder, entryStream, optionsMap);
    }

    private static void doOptions(String folder,
                                  Stream<Map.Entry<MetaInfo, File>> entryStream,
                                  Map<Option.Value, List<Option>> optionsMap) {
        final var optValues = optionsMap.keySet();
        final boolean isDetailsNeeded = optValues.contains(DETAILED_PRINT);
        final boolean isCopyNeeded = optValues.contains(COPY);
        final boolean isDeleteNeeded = optValues.contains(DELETE);

        final Stream<Map.Entry<MetaInfo, File>> files;
        if (!isDetailsNeeded && (isCopyNeeded || isDeleteNeeded)) files = ProgressBar.wrap(entryStream, "Handling...");
        else files = entryStream;

        files.forEach(entry -> {
            final var metaInfo = entry.getKey();
            final var file = entry.getValue();

            try {
                if (isDetailsNeeded) {
                    System.out.printf("%s%s (size=%d)\n", folder, metaInfo.relativePath(), metaInfo.size());
                }
                if (isCopyNeeded) {
                    for (Option it : optionsMap.get(COPY)) {
                        var copyOption = (CopyOption) it;
                        FileUtils.copyFile(file, new File(copyOption.getDestination() + metaInfo.relativePath()));
                    }
                }
                if (isDeleteNeeded) {
                    FileUtils.delete(file);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (isCopyNeeded) {
            for (Option it : optionsMap.get(COPY)) {
                var copyOption = (CopyOption) it;
                System.out.printf("Files found only in '%s' copied to '%s'\n", folder, copyOption.getDestination());
            }
        }

        if (isDeleteNeeded) {
            System.out.printf("Files found ONLY in '%s' deleted\n", folder);
        }
        System.out.println();
    }
}
