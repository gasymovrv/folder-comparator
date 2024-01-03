package ru.gasymov.foldercomparator.handler;

import java.util.Objects;


public class Option {
    private final Value value;

    public Option(Value value) {
        this.value = value;
    }

    public enum Value {
        COPY,
        DELETE,
        DETAILED_PRINT,
        PARALLEL
    }

    public boolean matches(Value value) {
        return this.value == value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Option option)) return false;
        return value == option.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
