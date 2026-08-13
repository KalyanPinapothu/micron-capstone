package com.micron.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.micron.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class FigureModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(FigureModel.class);
    }

    @Test
    void parsesGeneratedFigureHtmlIntoFigureParts() {
        final Resource resource = context.create().resource(
            "/content/figure",
            "sling:resourceType", FigureModel.RESOURCE_TYPE,
            "figureHtml", generatedFigureHtml()
        );

        final FigureModel model = resource.adaptTo(FigureModel.class);

        assertNotNull(model);
        assertFalse(model.isEmpty());
        assertEquals(
            "fig_9458D4A1F425423695A05FB088C79CDF",
            model.getSourceId()
        );
        assertEquals("orient-land", model.getOrientation());
        assertEquals("orient-land fig", model.getSourceClasses());
        assertEquals("inline", model.getPlacement());
        assertEquals(
            "/content/dam/sample-content/content/RD8_RD8_tCCD4_3ds_core.svg",
            model.getFileReference()
        );
        assertEquals("", model.getAlt());
        assertTrue(model.getCaptionHtml().contains("READ BL8 to READ BL8"));
        assertTrue(
            model.getCaptionHtml().contains(
                "<sup class=\"ph sup\">t</sup>CCD"
            )
        );
        assertNull(model.getDescriptionHtml());
        assertEquals(2, model.getNoteHtmlList().size());
        assertTrue(model.getNoteHtmlList().get(0).contains("BL8, RL = 5"));
        assertTrue(
            model.getNoteHtmlList().get(1).contains(
                "D<sub class=\"ph sub display-inline\">OUT</sub>"
            )
        );
    }

    @Test
    void prefersStructuredGeneratedPropertiesOverFigureHtml() {
        final Resource resource = context.create().resource(
            "/content/figure",
            "sling:resourceType", FigureModel.RESOURCE_TYPE,
            "sourceId", "fig_structured",
            "sourceClasses", "fig branded-figure unsafe/class",
            "outputClass", "orient-land",
            "captionHtml", "<span class=\"prefix-content\">Figure: </span>"
                + "Structured caption",
            "descriptionHtml", "<div class=\"footnote note\">Structured note</div>",
            "fileReference",
            "/content/dam/sample-content/content/structured.svg"
                + "/jcr:content/renditions/original",
            "alt", "Structured alt",
            "placement", "break"
        );

        final FigureModel model = resource.adaptTo(FigureModel.class);

        assertNotNull(model);
        assertFalse(model.isEmpty());
        assertEquals("fig_structured", model.getSourceId());
        assertEquals("fig branded-figure", model.getSourceClasses());
        assertEquals("orient-land", model.getOrientation());
        assertEquals(
            "<span class=\"prefix-content\">Figure: </span>Structured caption",
            model.getCaptionHtml()
        );
        assertEquals(
            "<div class=\"footnote note\">Structured note</div>",
            model.getDescriptionHtml()
        );
        assertTrue(model.getNoteHtmlList().isEmpty());
        assertEquals(
            "/content/dam/sample-content/content/structured.svg",
            model.getFileReference()
        );
        assertEquals("Structured alt", model.getAlt());
        assertEquals("break", model.getPlacement());
    }

    @Test
    void parsesRepresentativeGuidesFigureHtmlFallback() {
        final Resource resource = context.create().resource(
            "/content/figure",
            "sling:resourceType", FigureModel.RESOURCE_TYPE,
            "figureHtml", representativeGuidesFigureHtml()
        );

        final FigureModel model = resource.adaptTo(FigureModel.class);

        assertNotNull(model);
        assertFalse(model.isEmpty());
        assertEquals(
            "fig_3348114A2EB842938DB9E6989C7D6B5F",
            model.getSourceId()
        );
        assertEquals("orient-land", model.getOrientation());
        assertEquals("break", model.getPlacement());
        assertEquals(
            "/content/dam/sample-content/content/RD8_RD8_tCCD6_3ds_core.svg",
            model.getFileReference()
        );
        assertTrue(model.getCaptionHtml().contains("t</sup>CCD = 6"));
        assertEquals(2, model.getNoteHtmlList().size());
        assertTrue(model.getNoteHtmlList().get(0).contains("BL8, RL = 5"));
        assertTrue(model.getNoteHtmlList().get(1).contains("data-out from column"));
    }

    @Test
    void exposesVirtualImageResourceForCoreImageRendering() {
        final Resource resource = context.create().resource(
            "/content/figure",
            "sling:resourceType", FigureModel.RESOURCE_TYPE,
            "figureHtml", generatedFigureHtml()
        );

        final FigureModel model = resource.adaptTo(FigureModel.class);

        assertNotNull(model);
        assertEquals(
            "micron/components/image",
            model.getImageResourceType()
        );

        final Resource imageResource = model.getImageResource();

        assertNotNull(imageResource);
        assertEquals("/content/figure/image", imageResource.getPath());
        assertEquals(
            "micron/components/image",
            imageResource.getResourceType()
        );

        final ValueMap properties = imageResource.getValueMap();

        assertEquals(
            "/content/dam/sample-content/content/RD8_RD8_tCCD4_3ds_core.svg",
            properties.get("fileReference", String.class)
        );
        assertEquals("", properties.get("alt", String.class));
        assertEquals(Boolean.FALSE, properties.get("isDecorative", Boolean.class));
        assertEquals(Boolean.TRUE, properties.get("altValueFromDAM", Boolean.class));
        assertEquals(Boolean.FALSE, properties.get("disableLazyLoading", Boolean.class));
    }

    @Test
    void appliesAuthorOverridesAndVisibilityFlags() {
        final Resource resource = context.create().resource(
            "/content/figure",
            "sling:resourceType", FigureModel.RESOURCE_TYPE,
            "figureHtml", generatedFigureHtml(),
            "captionOverride", "Caption override",
            "descriptionOverride", "Description override",
            "fileReferenceOverride",
            "/content/dam/override.svg/jcr:content/renditions/original",
            "altOverride", "Override alt",
            "placementOverride", "break",
            "orientationOverride", "landscape",
            "hideCaption", true
        );

        final FigureModel model = resource.adaptTo(FigureModel.class);

        assertNotNull(model);
        assertFalse(model.isEmpty());
        assertNull(model.getCaptionHtml());
        assertEquals("Description override", model.getDescriptionHtml());
        assertTrue(model.getNoteHtmlList().isEmpty());
        assertEquals("/content/dam/override.svg", model.getFileReference());
        assertEquals("Override alt", model.getAlt());
        assertEquals("break", model.getPlacement());
        assertEquals("landscape", model.getOrientation());
    }

    @Test
    void isEmptyWithoutContent() {
        final Resource resource = context.create().resource(
            "/content/figure",
            "sling:resourceType", FigureModel.RESOURCE_TYPE
        );

        final FigureModel model = resource.adaptTo(FigureModel.class);

        assertNotNull(model);
        assertTrue(model.isEmpty());
        assertEquals("default", model.getPlacement());
        assertNull(model.getImageResource());
    }

    private String generatedFigureHtml() {
        return "<figure id=\"fig_9458D4A1F425423695A05FB088C79CDF\""
            + " class=\"orient-land fig\" data-attr-outputclass=\"orient-land\">"
            + "<figcaption class=\"orien-land title\">"
            + "<span class=\"prefix-content\">Figure: </span>"
            + "READ BL8 to READ BL8 (<sup class=\"ph sup\">t</sup>CCD = 4)"
            + " Example"
            + "</figcaption>"
            + "<span class=\"orient-land desc display-inline\">"
            + "<div id=\"note_1\" class=\"footnote note\">"
            + "<span class=\"prefix-content\">Note: </span>"
            + "<div id=\"p_1\" class=\"p\"> BL8, RL = 5 (CL = 5, AL = 0)."
            + " </div>"
            + "</div>"
            + "<div id=\"note_2\" class=\"footnote note\">"
            + "<span class=\"prefix-content\">Note: </span>"
            + "<div id=\"p_2\" class=\"p\">"
            + "D<sub class=\"ph sub display-inline\">OUT</sub> a,"
            + " b, or c = data-out from column a, b, or c. </div>"
            + "</div>"
            + "</span>"
            + "<span class=\"placement-inline align-\">"
            + "<img src=\"/content/dam/sample-content/content/"
            + "RD8_RD8_tCCD4_3ds_core.svg/jcr:content/renditions/original\""
            + " alt=\"\" style=\"width: 22cm; color: red\""
            + " class=\"image tcx-empty-element\"/>"
            + "</span>"
            + "</figure>";
    }

    private String representativeGuidesFigureHtml() {
        return "<figure id=\"fig_3348114A2EB842938DB9E6989C7D6B5F\""
            + " class=\"fig\">"
            + "<figcaption class=\"orien-land title\""
            + " data-attr-outputclass=\"orien-land\">"
            + "<span class=\"prefix-content\">Figure: </span>"
            + "READ BL8 to READ BL8 (<sup class=\"ph sup\">t</sup>CCD = 6)"
            + " Example </figcaption>"
            + "<span class=\"orient-land desc display-inline\""
            + " data-attr-outputclass=\"orient-land\">"
            + "<div id=\"note_07A5DC91BC444DFAA21754571B431124\""
            + " class=\"footnote note\" data-attr-outputclass=\"footnote\">"
            + "<span class=\"prefix-content\">Note: </span>"
            + "<div id=\"p_38F64E69AA664FF6AD5094AD152FEF40\" class=\"p\">"
            + " BL8, RL = 5 (CL = 5, AL = 0). </div>"
            + "</div>"
            + "<div id=\"note_F4791C23305547BC8D9D335E7E5F3A9F\""
            + " class=\"footnote note\" data-attr-outputclass=\"footnote\">"
            + "<span class=\"prefix-content\">Note: </span>"
            + "<div id=\"p_12FBD109789741938ACF0AC1008FCFAE\" class=\"p\">"
            + " D<sub class=\"ph sub display-inline\">OUT</sub> a,"
            + " b, or c = data-out from column a, b, or c. </div>"
            + "</div>"
            + "</span>"
            + "<span class=\"placement-break align-\">"
            + "<img src=\"/content/dam/sample-content/content/"
            + "RD8_RD8_tCCD6_3ds_core.svg/jcr:content/renditions/original\""
            + " alt=\"\" id=\"image_8F8E987296F0443CB9771E1824D6972B\""
            + " style=\"width: 22cm\" class=\"image tcx-empty-element\""
            + " data-attr-placement=\"break\"></span>"
            + "</figure>";
    }
}
