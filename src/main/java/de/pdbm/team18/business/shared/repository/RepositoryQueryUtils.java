package de.pdbm.team18.business.shared.repository;

public class RepositoryQueryUtils {
    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT = 100;
    public static final int SEARCH_QUERY_LIMIT = 10;

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
