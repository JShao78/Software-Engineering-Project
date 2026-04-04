package de.pdbm.team18.business.shared.repository;

public class RepositoryQueryUtils {
    public static final int DEFAULT_LIMIT = 25;
    public static final int MAX_LIMIT = 10_000; //TODO

    private RepositoryQueryUtils() {
    }

    public static int sanitizeOffset(int offset) {
        return Math.max(offset, 0);
    }

    public static int sanitizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
