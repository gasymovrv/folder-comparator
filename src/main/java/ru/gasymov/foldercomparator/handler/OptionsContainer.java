package ru.gasymov.foldercomparator.handler;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OptionsContainer {
    public static String newDestination1 = "filesOnlyInFirstFolder";
    public static String newDestination2 = "filesOnlyInSecondFolder";
    private final String folder1;
    private final String folder2;
    private Set<Option> commonOptions = new HashSet<>();
    private final Map<String, Map<Option.Value, Set<Option>>> optionsByFolders = new HashMap<>();

    private OptionsContainer(String folder1, String folder2) {
        this.folder1 = new File(folder1).getPath();
        this.folder2 = new File(folder2).getPath();
    }

    public static OptionsContainer create(String folder1, String folder2) {
        return new OptionsContainer(folder1, folder2);
    }

    public OptionsContainer setOptions(List<String> optionalArgs) {
        this.commonOptions = parseCommonOptions(optionalArgs);
        this.optionsByFolders.put(folder1, groupOptionsByValue(parseFolder1Options(optionalArgs)));
        this.optionsByFolders.put(folder2, groupOptionsByValue(parseFolder2Options(optionalArgs)));
        return this;
    }

    public boolean hasCommon(Option.Value value) {
        return commonOptions.stream().anyMatch(option -> option.matches(value));
    }

    public boolean has(String folder, Option.Value value) {
        return optionsByFolders.get(folder).containsKey(value);
    }

    public Set<Option> get(String folder, Option.Value value) {
        return optionsByFolders.get(folder).get(value);
    }

    public void print() {
        System.out.println("Got optional arguments:");
        System.out.printf("\tCommon: %s\n", commonOptions);
        System.out.printf("\tFolder 1 options: %s\n", optionsByFolders.get(folder1).values().stream().flatMap(Collection::stream).toList());
        System.out.printf("\tFolder 2 options: %s\n", optionsByFolders.get(folder2).values().stream().flatMap(Collection::stream).toList());
    }

    private Set<Option> parseFolder1Options(List<String> optionalArgs) {
        final var options = new HashSet<Option>();
        if (optionalArgs.contains("-c")) {
            options.add(new CopyOption(newDestination1));
        }
        if (optionalArgs.contains("-c1")) {
            options.add(new CopyOption(folder2));
        }
        if (optionalArgs.contains("-d1")) {
            options.add(new Option(Option.Value.DELETE));
        }
        return options;
    }

    private Set<Option> parseFolder2Options(List<String> optionalArgs) {
        final var options = new HashSet<Option>();
        if (optionalArgs.contains("-c")) {
            options.add(new CopyOption(newDestination2));
        }
        if (optionalArgs.contains("-c2")) {
            options.add(new CopyOption(folder1));
        }
        if (optionalArgs.contains("-d2")) {
            options.add(new Option(Option.Value.DELETE));
        }
        return options;
    }

    private Set<Option> parseCommonOptions(List<String> optionalArgs) {
        final var commonOptions = new HashSet<Option>();
        if (optionalArgs.contains("-l")) {
            commonOptions.add(new Option(Option.Value.DETAILED_PRINT));
        }
        final boolean isParallel = optionalArgs.contains("-p");
        if (isParallel) {
            commonOptions.add(new Option(Option.Value.PARALLEL));
        }
        return commonOptions;
    }

    private Map<Option.Value, Set<Option>> groupOptionsByValue(Set<Option> options) {
        return options.stream().collect(Collectors.groupingBy(
                Option::getValue,
                Collectors.mapping(Function.identity(), Collectors.toSet()))
        );
    }
}
