package com.devcollab.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MentionUtil {

    // Matches @username where username follows the same charset as our own
    // username validation (Feature 1): letters, digits, at least 3 chars.
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]{3,50})");

    private MentionUtil() {
    }

    public static Set<String> extractMentionedUsernames(String text) {
        Set<String> usernames = new LinkedHashSet<>();
        if (text == null) return usernames;

        Matcher matcher = MENTION_PATTERN.matcher(text);
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }
        return usernames;
    }
}
