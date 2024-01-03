package ru.gasymov.foldercomparator.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import me.tongfei.progressbar.ConsoleProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import ru.gasymov.foldercomparator.MetaInfo;

import static ru.gasymov.foldercomparator.handler.Option.Value.*;

public class FilesHandler {

    private final Map<MetaInfo, File> files;
    private final OptionsContainer optionsContainer;
    private final String folder;

    public FilesHandler(Map<MetaInfo, File> files, String folder, OptionsContainer optionsContainer) {
        this.files = files;
        this.folder = new File(folder).getPath();
        this.optionsContainer = optionsContainer;
    }

    public void handle() {
        if (files.isEmpty()) {
            System.out.printf("Not found files present ONLY in '%s'\n\n", folder);
            return;
        }
        final boolean isParallelNeeded = optionsContainer.hasCommon(PARALLEL);
        final boolean isDetailsNeeded = optionsContainer.hasCommon(DETAILED_PRINT);
        final boolean isCopyNeeded = optionsContainer.has(folder, COPY);
        final boolean isDeleteNeeded = optionsContainer.has(folder, DELETE);

        Stream<Map.Entry<MetaInfo, File>> entryStream;
        if (isParallelNeeded) entryStream = files.entrySet().parallelStream();
        else entryStream = files.entrySet().stream();

        if (!isDetailsNeeded && (isCopyNeeded || isDeleteNeeded)) entryStream = wrapWithProgressBar(entryStream);

        final var print = String.format("Found %s files present ONLY in '%s'", files.size(), folder);
        if (isDetailsNeeded) System.out.println(print + ":");
        else System.out.println(print);

        runOptions(entryStream, isDetailsNeeded, isCopyNeeded, isDeleteNeeded);
    }

    private void runOptions(Stream<Map.Entry<MetaInfo, File>> entryStream, boolean isDetailsNeeded, boolean isCopyNeeded, boolean isDeleteNeeded) {
        entryStream.forEach(entry -> {
            final var metaInfo = entry.getKey();
            final var file = entry.getValue();

            try {
                if (isDetailsNeeded) {
                    System.out.printf("%s%s (size=%d)\n", folder, metaInfo.relativePath(), metaInfo.size());
                }
                if (isCopyNeeded) {
                    for (Option it : optionsContainer.get(folder, COPY)) {
                        var copyOption = (CopyOption) it;
                        FileUtils.copyFile(
                                file,
                                new File(copyOption.getDestination() + metaInfo.relativePath()),
                                StandardCopyOption.COPY_ATTRIBUTES
                        );
                    }
                }
                if (isDeleteNeeded) {
                    FileUtils.delete(file);
                }
            } catch (IOException e) {
                System.err.println(e);
                throw new RuntimeException(e);
            }
        });

        if (isCopyNeeded) {
            for (Option it : optionsContainer.get(folder, COPY)) {
                var copyOption = (CopyOption) it;
                System.out.printf("\nFiles found only in '%s' copied to '%s'\n", folder, copyOption.getDestination());
            }
        }

        if (isDeleteNeeded) {
            System.out.printf("\nFiles found ONLY in '%s' deleted\n", folder);
        }
        System.out.println();
    }

    private Stream<Map.Entry<MetaInfo, File>> wrapWithProgressBar(Stream<Map.Entry<MetaInfo, File>> entryStream) {
        return ProgressBar.wrap(
                entryStream,
                ProgressBar.builder()
                        .showSpeed()
                        .setStyle(ProgressBarStyle.ASCII)
                        .setConsumer(new ConsoleProgressBarConsumer(System.out, 120))
                        .setTaskName("Handling '" + folder + "': ")

        );
    }
}
