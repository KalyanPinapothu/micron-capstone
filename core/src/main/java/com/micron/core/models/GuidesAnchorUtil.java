package com.micron.core.models;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

final class GuidesAnchorUtil {

    private GuidesAnchorUtil() {
    }

    static String fromPath(String path) {
        if (StringUtils.isBlank(path)) {
            return "topic";
        }

        String anchor = path.toLowerCase(Locale.ENGLISH)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+|-+$)", "");

        return StringUtils.defaultIfBlank(anchor, "topic");
    }
}
