package com.micron.core.models;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.micron.core.support.NormalizedImageResource;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Model(
    adaptables = Resource.class,
    resourceType = NormalizedImageModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class NormalizedImageModel {

    public static final String RESOURCE_TYPE =
        "micron/components/image";

    private static final String CORE_IMAGE_RESOURCE_TYPE =
        "core/wcm/components/image/v3/image";

    private static final String RENDITION_SEGMENT =
        "/jcr:content/renditions/";

    private static final Pattern SAFE_CLASS_NAME =
        Pattern.compile("[A-Za-z0-9_-]+");

    @Self
    private Resource resource;

    private Resource normalizedResource;
    private String fileReference;
    private String alt;
    private String title;
    private String sourceClasses;

    @PostConstruct
    protected void init() {
        if (resource == null) {
            return;
        }

        final ValueMap sourceProperties = resource.getValueMap();
        final Map<String, Object> properties = new HashMap<>(sourceProperties);
        fileReference = normalizeAssetPath(firstNonBlank(
            sourceProperties.get("fileReference", String.class),
            sourceProperties.get("src", String.class)
        ));
        alt = StringUtils.defaultString(sourceProperties.get("alt", String.class));
        title = StringUtils.defaultString(sourceProperties.get("jcr:title", String.class));
        sourceClasses = normalizeClassNames(
            sourceProperties.get("sourceClasses", String.class)
        );

        properties.put(
            "fileReference",
            fileReference
        );

        normalizedResource = new NormalizedImageResource(
            resource,
            CORE_IMAGE_RESOURCE_TYPE,
            properties
        );
    }

    private @Nullable String normalizeAssetPath(final @Nullable String sourcePath) {
        final String path = StringUtils.trimToNull(sourcePath);

        if (path == null) {
            return null;
        }

        final int renditionIndex =
            path.indexOf(RENDITION_SEGMENT);

        return renditionIndex >= 0
            ? path.substring(0, renditionIndex)
            : path;
    }

    public @NotNull String getCoreImageResourceType() {
        return CORE_IMAGE_RESOURCE_TYPE;
    }

    public @Nullable Resource getResource() {
        return normalizedResource;
    }

    public @Nullable String getFileReference() {
        return fileReference;
    }

    public @NotNull String getAlt() {
        return alt;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public @Nullable String getSourceClasses() {
        return sourceClasses;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(fileReference);
    }

    private @Nullable String firstNonBlank(
            final @Nullable String first,
            final @Nullable String second) {

        return StringUtils.isNotBlank(first) ? first : second;
    }

    private @Nullable String normalizeClassNames(final @Nullable String value) {
        final String classNames = StringUtils.trimToNull(value);

        if (classNames == null) {
            return null;
        }

        final StringBuilder safeClassNames = new StringBuilder();

        for (String className : classNames.split("\\s+")) {
            if (SAFE_CLASS_NAME.matcher(className).matches()) {
                if (safeClassNames.length() > 0) {
                    safeClassNames.append(' ');
                }

                safeClassNames.append(className);
            }
        }

        return safeClassNames.length() == 0
            ? null
            : safeClassNames.toString();
    }
}
