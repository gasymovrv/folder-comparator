package ru.gasymov.foldercomparator.handler;

import java.util.Objects;

public class CopyOption extends Option {
    private final String destination;

    public CopyOption(String destination) {
        super(Value.COPY);
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CopyOption that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), destination);
    }

    @Override
    public String toString() {
        return "CopyOption { " +
                "value=" + super.getValue() +
                ", destination='" + destination +
                "' }";
    }
}
