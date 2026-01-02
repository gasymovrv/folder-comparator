package ru.gasymov.foldercomparator.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
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
        final var progress = new Progress(files.size());

        Stream<Map.Entry<MetaInfo, File>> entryStream;
        if (isParallelNeeded) entryStream = files.entrySet().parallelStream();
        else entryStream = files.entrySet().stream();

        if (!isDetailsNeeded && (isCopyNeeded || isDeleteNeeded)) entryStream = wrapWithProgressBar(entryStream, progress);

        final var print = String.format("Found %s files present ONLY in '%s'", files.size(), folder);
        if (isDetailsNeeded) System.out.println(print + ":");
        else System.out.println(print);

        runOptions(entryStream, isDetailsNeeded, isCopyNeeded, isDeleteNeeded, progress);
    }

    private void runOptions(Stream<Map.Entry<MetaInfo, File>> entryStream,
                            boolean isDetailsNeeded,
                            boolean isCopyNeeded,
                            boolean isDeleteNeeded,
                            Progress progress) {
        entryStream.forEach(entry -> {
            final var metaInfo = entry.getKey();
            final var file = entry.getValue();

            if (isDetailsNeeded) {
                System.out.printf("%s%s (size=%d)\n", folder, metaInfo.relativePath(), metaInfo.size());
            }

            try {
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
            } catch (IOException e) {
                System.err.println("ERROR! Could not copy file " + file.getAbsolutePath() + ": " + e);
                progress.errors.incrementAndGet();
            }

            try {
                if (isDeleteNeeded) {
                    FileUtils.delete(file);
                }
            } catch (IOException e) {
                System.err.println("ERROR! Could not delete file " + file.getAbsolutePath() + ": " + e);
                progress.errors.incrementAndGet();
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

    private Stream<Map.Entry<MetaInfo, File>> wrapWithProgressBar(Stream<Map.Entry<MetaInfo, File>> entryStream, Progress progress) {
        final var counter = progress.processed;
        final int total = progress.total;

        // For parallel processing we can't show progress correctly
        if (optionsContainer.hasCommon(PARALLEL)) {
            System.out.println("Processing files...");
            return entryStream;
        } else {
            return entryStream.peek(entry -> {
                // Update progress
                int currentCount = counter.incrementAndGet();
                if (currentCount % 10 == 0 || currentCount == total) {
                    double percent = (currentCount * 100.0) / total;

                    // Clear the line and move cursor to start
                    System.out.print("\r");
                    System.out.printf("Processing files: %d/%d (%.1f%%), errors: %d", currentCount, total, percent, progress.errors.get());

                    if (currentCount == total) {
                        System.out.println(); // New line when done
                    }
                }
            });
        }
    }

    private static class Progress {
        final AtomicInteger processed = new AtomicInteger(0);
        final AtomicInteger errors = new AtomicInteger(0);
        final int total;

        Progress(int total) {
            this.total = total;
        }
    }
}
