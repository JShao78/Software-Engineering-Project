package de.pdbm.team18.business.shared.repository;

import java.util.Locale;

public final class RepositoryQueryUtils {
    public static final int DEFAULT_LIMIT = 25;
    public static final int MAX_LIMIT = 200;

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

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String toLikePattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    public static String normalizeLowercase(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
