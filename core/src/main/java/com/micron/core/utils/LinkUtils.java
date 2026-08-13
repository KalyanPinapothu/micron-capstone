package com.micron.core.utils;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;

public final class LinkUtils {

    public static final String HTML_EXTENSION = ".html";
    public static final String HASH = "#";
    public static final String QUESTION_MARK = "?";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String HTTPS_SCHEME = "https://";
    public static final String CONTENT_ROOT = "/content/micron";
    public static final String CONTENT_ROOT_DAM = "/content/dam";
    private static final Pattern BARE_EXTERNAL_LINK = Pattern.compile(
        "(?i)^(www\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+(/.*|\\?.*|#.*)?$"
    );

    private LinkUtils() {
    }

    public static String getFormattedLink(String link, ResourceResolver resolver) {
        if (StringUtils.isBlank(link)) {
            return link;
        }

        String formattedLink = StringUtils.trim(link);
        if (StringUtils.equals(formattedLink, HASH)) {
            return HASH;
        } else if (isBareExternalLink(formattedLink)) {
            return HTTPS_SCHEME + formattedLink;
        } else if (isDam(formattedLink) || isExternalLink(formattedLink)) {
            return formattedLink;
        } else if (Objects.nonNull(resolver) && StringUtils.contains(formattedLink, CONTENT_ROOT)) {
            return getResolvedContentURL(formattedLink, resolver);
        }

        return link;
    }

    private static String getResolvedContentURL(String link, ResourceResolver resolver) {
        if (!link.contains(HTML_EXTENSION)
                && !link.contains(HASH)
                && !link.contains(QUESTION_MARK)) {
            return resolver.map(link + HTML_EXTENSION);
        } else if (!link.contains(HTML_EXTENSION) && link.contains(HASH)) {
            String[] hashString = StringUtils.split(link, HASH);
            return resolver.map(hashString[0] + HTML_EXTENSION) + HASH + hashString[1];
        } else if (!link.contains(HTML_EXTENSION) && link.contains(QUESTION_MARK)) {
            String[] queryString = StringUtils.split(link, QUESTION_MARK);
            return resolver.map(queryString[0] + HTML_EXTENSION) + QUESTION_MARK + queryString[1];
        } else {
            return resolver.map(link);
        }
    }

    public static boolean isExternalLink(String link) {
        return link.startsWith(HTTP) || link.startsWith(HTTPS);
    }

    public static boolean isBareExternalLink(String link) {
        return BARE_EXTERNAL_LINK.matcher(link).matches();
    }

    public static boolean isDam(String link) {
        return StringUtils.contains(link, CONTENT_ROOT_DAM);
    }
}
