package dev.alexisreyna.artisanalbrew.application.common;

import java.util.Locale;
import java.util.regex.Pattern;

/** Port of {@code Common/SlugGenerator.cs}. */
public final class SlugGenerator {
    private static final Pattern INVALID_CHARACTERS = Pattern.compile("[^a-z0-9]+");
    private static final Pattern REPEATED_HYPHENS = Pattern.compile("-+");

    private SlugGenerator() {
    }

    public static String create(String value) {
        String slug = value.trim().toLowerCase(Locale.ROOT);
        slug = INVALID_CHARACTERS.matcher(slug).replaceAll("-");
        slug = REPEATED_HYPHENS.matcher(slug).replaceAll("-");
        return strip(slug, '-');
    }

    private static String strip(String value, char ch) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == ch) {
            start++;
        }
        while (end > start && value.charAt(end - 1) == ch) {
            end--;
        }
        return value.substring(start, end);
    }
}
