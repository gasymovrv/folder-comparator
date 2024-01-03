package ru.gasymov.foldercomparator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.gasymov.foldercomparator.handler.OptionsContainer;

class MainTest {
    private final static String temp = "src/test/resources/temp";
    private final static String folder1 = new File(temp + "/DIR1").getPath();
    private final static String folder2 = new File(temp + "/DIR2").getPath();

    @BeforeAll
    static void globalSetUp() {
        OptionsContainer.newDestination1 = new File(temp + "/" + OptionsContainer.newDestination1).getPath();
        OptionsContainer.newDestination2 = new File(temp + "/" + OptionsContainer.newDestination2).getPath();
    }

    @BeforeEach
    void setUp() throws IOException {
        FileUtils.copyDirectory(new File("src/test/resources/example"), new File(temp));
    }

    @AfterEach
    void destroy() throws IOException {
        FileUtils.deleteDirectory(new File(temp));
    }

    @Test
    void should_copy_from_dir1_and_from_dir2() throws InterruptedException {
        Main.main(new String[]{folder1, folder2, "-c1", "-c2"});

        Assertions.assertTrue(directoriesAreEqual());
    }

    @Test
    void should_copy_from_dir1_and_from_dir2_parallel() throws InterruptedException {
        Main.main(new String[]{folder1, folder2, "-p", "-c1", "-c2"});

        Assertions.assertTrue(directoriesAreEqual());
    }

    @Test
    void should_copy_from_dir1_and_from_dir2_and_to_new_dirs() throws InterruptedException {
        Main.main(new String[]{folder1, folder2, "-c1", "-c2", "-c"});

        Assertions.assertTrue(directoriesAreEqual());

        var expected = Stream.of(
                        new File(folder1 + "/NESTED_DIR_1/file3.txt"),
                        new File(folder1 + "/NESTED_DIR_2/file4.txt")
                )
                .map(MainTest::convertToMetaInfo)
                .toList();
        Assertions.assertEquals(expected, getFilesInfos(OptionsContainer.newDestination1));

        expected = Stream.of(new File(folder2 + "/NESTED_DIR_3/file4.txt"))
                .map(MainTest::convertToMetaInfo)
                .toList();
        Assertions.assertEquals(expected, getFilesInfos(OptionsContainer.newDestination2));
    }

    @Test
    void should_copy_from_dir1_and_from_dir2_and_to_new_dirs_parallel() throws InterruptedException {
        Main.main(new String[]{folder1, folder2, "-p", "-c1", "-c2", "-c"});

        Assertions.assertTrue(directoriesAreEqual());
        var expected = Stream.of(
                        new File(folder1 + "/NESTED_DIR_1/file3.txt"),
                        new File(folder1 + "/NESTED_DIR_2/file4.txt")
                )
                .map(MainTest::convertToMetaInfo)
                .toList();
        Assertions.assertEquals(expected, getFilesInfos(OptionsContainer.newDestination1));

        expected = Stream.of(new File(folder2 + "/NESTED_DIR_3/file4.txt"))
                .map(MainTest::convertToMetaInfo)
                .toList();
        Assertions.assertEquals(expected, getFilesInfos(OptionsContainer.newDestination2));
    }

    @Test
    void should_copy_from_dir1() throws InterruptedException {
        Main.main(new String[]{folder1, folder2, "-p", "-c1"});
        final var actual = getFilesInfos(folder2);

        var expected = Stream.of(
                        new File(folder2 + "/NESTED_DIR_1/file1.txt"),
                        new File(folder2 + "/NESTED_DIR_1/file2.txt"),
                        new File(folder1 + "/NESTED_DIR_1/file3.txt"), // must be copied
                        new File(folder1 + "/NESTED_DIR_2/file4.txt"), // must be copied
                        new File(folder2 + "/NESTED_DIR_3/file4.txt")
                )
                .map(MainTest::convertToMetaInfo)
                .toList();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void should_copy_from_dir2() throws InterruptedException {
        Main.main(new String[]{folder1, folder2, "-p", "-c2"});
        final var actual = getFilesInfos(folder1);

        var expected = Stream.of(
                        new File(folder1 + "/NESTED_DIR_1/file1.txt"),
                        new File(folder1 + "/NESTED_DIR_1/file2.txt"),
                        new File(folder1 + "/NESTED_DIR_1/file3.txt"),
                        new File(folder1 + "/NESTED_DIR_2/file4.txt"),
                        new File(folder2 + "/NESTED_DIR_3/file4.txt") // must be copied
                )
                .map(MainTest::convertToMetaInfo)
                .toList();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void should_throw_error_when_copy_already_existing_file_from_dir1() throws IOException {
        // Create file with already existing name in folder2
        FileUtils.touch(new File(folder2 + "/NESTED_DIR_1/file3.txt"));

        Assertions.assertThrows(Exception.class, () -> Main.main(new String[]{folder1, folder2, "-c1", "-l"}));
    }

    @Test
    void should_throw_error_when_copy_already_existing_file_from_dir2() throws IOException {
        // Create file with already existing name in folder1
        FileUtils.touch(new File(folder1 + "/NESTED_DIR_3/file4.txt"));

        Assertions.assertThrows(Exception.class, () -> Main.main(new String[]{folder1, folder2, "-c2", "-l"}));
    }

    @Test
    void should_delete_from_dir1() throws InterruptedException {
        Main.main(new String[]{folder1, folder2, "-p", "-d1"});

        final var actual = getFilesInfos(folder1);

        var expected = Stream.of(
                        new File(folder1 + "/NESTED_DIR_1/file1.txt"),
                        new File(folder1 + "/NESTED_DIR_1/file2.txt")
                )
                .map(MainTest::convertToMetaInfo)
                .toList();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void should_delete_from_dir2() throws InterruptedException {
        Main.main(new String[]{folder1, folder2, "-p", "-d2"});

        final var actual = getFilesInfos(folder2);

        var expected = Stream.of(
                        new File(folder2 + "/NESTED_DIR_1/file1.txt"),
                        new File(folder2 + "/NESTED_DIR_1/file2.txt")
                )
                .map(MainTest::convertToMetaInfo)
                .toList();
        Assertions.assertEquals(expected, actual);
    }

    private boolean directoriesAreEqual() {
        var dir1 = getFilesInfos(folder1);
        var dir2 = getFilesInfos(folder2);
        return dir1.equals(dir2);
    }

    private List<MetaInfo> getFilesInfos(String folder) {
        return FileUtils.listFiles(new File(folder), null, true)
                .stream()
                .map(it -> new MetaInfo(it.getPath().replace(folder, ""), FileUtils.sizeOf(it)))
                .toList();
    }

    private static MetaInfo convertToMetaInfo(File it) {
        return new MetaInfo(
                it.getPath().replace(folder1, "").replace(folder2, ""),
                FileUtils.sizeOf(it)
        );
    }
}
