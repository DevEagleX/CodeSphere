package com.devcollab.util;

import java.util.Locale;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String toSlug(String input) {
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        String slug = normalized.replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
        return slug.isBlank() ? "workspace" : slug;
    }
}
