package com.micron.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.micron.core.support.FigureImageResource;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Model(
    adaptables = Resource.class,
    resourceType = FigureModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class FigureModel {

    public static final String RESOURCE_TYPE =
        "micron/components/figure";

    private static final String IMAGE_RESOURCE_TYPE =
        "micron/components/image";

    private static final String RENDITION_SEGMENT =
        "/jcr:content/renditions/";

    private static final Pattern SAFE_CLASS_NAME =
        Pattern.compile("[A-Za-z0-9_-]+");

    @Self
    private Resource resource;

    @ValueMapValue
    private String figureHtml;

    @ValueMapValue
    private String sourceId;

    @ValueMapValue
    private String outputClass;

    @ValueMapValue
    private String sourceClasses;

    @ValueMapValue
    private String captionHtml;

    @ValueMapValue
    private String descriptionHtml;

    @ValueMapValue
    private String fileReference;

    @ValueMapValue
    private String alt;

    @ValueMapValue
    private String placement;

    // Author overrides
    @ValueMapValue
    private String captionOverride;

    @ValueMapValue
    private String descriptionOverride;

    @ValueMapValue
    private String fileReferenceOverride;

    @ValueMapValue
    private String altOverride;

    @ValueMapValue
    private String placementOverride;

    @ValueMapValue
    private String orientationOverride;

    @ValueMapValue
    private Boolean hideCaption;

    @ValueMapValue
    private Boolean hideDescription;

    @ValueMapValue
    private Boolean decorativeOverride;

    private String parsedCaptionHtml;
    private final List<String> parsedNoteHtmlList = new ArrayList<>();
    private String parsedFileReference;
    private String parsedAlt;
    private String parsedPlacement = "default";
    private String parsedOrientation;
    private String parsedSourceClasses;

    private Resource imageResource;

    @PostConstruct
    protected void init() {
        parseFigureHtml();
        buildImageResource();
    }

    private void parseFigureHtml() {
        if (StringUtils.isBlank(figureHtml)) {
            return;
        }

        final Document document = Jsoup.parseBodyFragment(figureHtml);
        final Element figure = document.selectFirst("figure");

        if (figure == null) {
            return;
        }

        if (StringUtils.isBlank(sourceId)) {
            sourceId = StringUtils.trimToNull(figure.id());
        }

        parsedSourceClasses = normalizeClassNames(figure.className());

        final Element outputClassElement =
            figure.selectFirst("[data-attr-outputclass]");

        parsedOrientation = normalizeOrientation(firstNonBlank(
            outputClass,
            figure.attr("data-attr-outputclass"),
            figure.hasClass("orient-land") ? "orient-land" : null,
            figure.hasClass("orient-portrait") ? "orient-portrait" : null,
            figure.selectFirst(".orient-land") != null ? "orient-land" : null,
            figure.selectFirst(".orient-portrait") != null
                ? "orient-portrait"
                : null,
            outputClassElement != null
                ? outputClassElement.attr("data-attr-outputclass")
                : null
        ));

        final Element caption = figure.selectFirst("figcaption");

        if (caption != null) {
            parsedCaptionHtml =
                StringUtils.trimToNull(caption.html());
        }

        for (Element note : figure.select("div.note")) {
            final String noteHtml = StringUtils.trimToNull(note.outerHtml());

            if (noteHtml != null) {
                parsedNoteHtmlList.add(noteHtml);
            }
        }

        Element image = figure.selectFirst("img.image");

        if (image == null) {
            image = figure.selectFirst("img");
        }

        if (image == null) {
            return;
        }

        parsedFileReference =
            normalizeAssetPath(image.attr("src"));

        parsedAlt =
            StringUtils.defaultString(image.attr("alt"));

        final Element placementWrapper = image.closest(
            ".placement-inline, .placement-break"
        );

        if (placementWrapper != null) {
            if (placementWrapper.hasClass("placement-break")) {
                parsedPlacement = "break";
            } else if (
                placementWrapper.hasClass("placement-inline")
            ) {
                parsedPlacement = "inline";
            }
        } else {
            parsedPlacement = StringUtils.defaultIfBlank(
                image.attr("data-attr-placement"),
                "default"
            );
        }
    }

    private void buildImageResource() {
        final String fileReference = getFileReference();

        if (resource == null || StringUtils.isBlank(fileReference)) {
            imageResource = null;
            return;
        }

        final Map<String, Object> properties = new HashMap<>();

        properties.put(
            "sling:resourceType",
            IMAGE_RESOURCE_TYPE
        );

        properties.put(
            "fileReference",
            fileReference
        );

        /*
         * Core Image supports alt text through the alt property.
         */
        properties.put(
            "alt",
            StringUtils.defaultString(getAlt())
        );

        /*
         * Let Image v3 resolve its normal behavior. The image is treated
         * as decorative only when explicitly configured.
         */
        properties.put(
            "isDecorative",
            Boolean.TRUE.equals(decorativeOverride)
        );

        /*
         * Disable automatic alt extraction only when an explicit authored
         * alt value is available.
         */
        properties.put(
            "altValueFromDAM",
            StringUtils.isBlank(getAlt())
        );

        properties.put(
            "disableLazyLoading",
            Boolean.FALSE
        );

        imageResource = new FigureImageResource(
            resource,
            IMAGE_RESOURCE_TYPE,
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

    private @Nullable String firstNonBlank(final String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return StringUtils.trim(value);
            }
        }

        return null;
    }

    private @Nullable String normalizeOrientation(final @Nullable String value) {
        final String orientation = StringUtils.trimToNull(value);

        if ("orien-land".equals(orientation)) {
            return "orient-land";
        }

        if ("orien-portrait".equals(orientation)) {
            return "orient-portrait";
        }

        return orientation;
    }

    private @Nullable String normalizeClassNames(final @Nullable String value) {
        final String classNames = StringUtils.trimToNull(value);

        if (classNames == null) {
            return null;
        }

        final List<String> safeClassNames = new ArrayList<>();

        for (String className : classNames.split("\\s+")) {
            if (SAFE_CLASS_NAME.matcher(className).matches()) {
                safeClassNames.add(className);
            }
        }

        return safeClassNames.isEmpty()
            ? null
            : StringUtils.join(safeClassNames, " ");
    }

    public @NotNull String getImageResourceType() {
        return IMAGE_RESOURCE_TYPE;
    }

    public @Nullable String getCaptionHtml() {
        if (Boolean.TRUE.equals(hideCaption)) {
            return null;
        }

        return StringUtils.defaultIfBlank(
            captionOverride,
            firstNonBlank(
                captionHtml,
                parsedCaptionHtml
            )
        );
    }

    public @Nullable String getDescriptionHtml() {
        if (Boolean.TRUE.equals(hideDescription)) {
            return null;
        }

        return firstNonBlank(
            descriptionOverride,
            descriptionHtml
        );
    }

    public @NotNull List<String> getNoteHtmlList() {
        if (
            Boolean.TRUE.equals(hideDescription)
                || StringUtils.isNotBlank(descriptionOverride)
                || StringUtils.isNotBlank(descriptionHtml)
        ) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(parsedNoteHtmlList);
    }

    public @Nullable String getFileReference() {
        return normalizeAssetPath(
            firstNonBlank(
                fileReferenceOverride,
                fileReference,
                parsedFileReference
            )
        );
    }

    public @NotNull String getAlt() {
        return StringUtils.defaultString(firstNonBlank(
            altOverride,
            alt,
            parsedAlt
        ));
    }

    public @NotNull String getPlacement() {
        return StringUtils.defaultIfBlank(
            placementOverride,
            firstNonBlank(
                placement,
                parsedPlacement
            )
        ).toLowerCase(Locale.ROOT);
    }

    public @Nullable String getOrientation() {
        return normalizeOrientation(firstNonBlank(
            orientationOverride,
            outputClass,
            parsedOrientation
        ));
    }

    public @Nullable String getSourceId() {
        return sourceId;
    }

    public @Nullable String getSourceClasses() {
        return normalizeClassNames(firstNonBlank(
            sourceClasses,
            parsedSourceClasses
        ));
    }

    /**
     * Virtual child resource rendered by the Image v3 proxy component.
     */
    public @Nullable Resource getImageResource() {
        return imageResource;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(getCaptionHtml())
            && StringUtils.isBlank(getDescriptionHtml())
            && getNoteHtmlList().isEmpty()
            && StringUtils.isBlank(getFileReference());
    }
}
