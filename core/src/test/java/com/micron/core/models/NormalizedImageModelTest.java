package com.micron.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.micron.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NormalizedImageModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(NormalizedImageModel.class);
    }

    @Test
    void stripsDamRenditionPathBeforeDelegatingToCoreImage() {
        final Resource resource = context.create().resource(
            "/content/image",
            "sling:resourceType", NormalizedImageModel.RESOURCE_TYPE,
            "fileReference",
            "/content/dam/sample-content/content/RD8_RD8_tCCD5_3ds_core.svg"
                + "/jcr:content/renditions/original",
            "alt", "Timing diagram",
            "sourceClasses", "image hero-image unsafe/class"
        );

        final NormalizedImageModel model =
            resource.adaptTo(NormalizedImageModel.class);

        assertNotNull(model);
        assertEquals(
            "core/wcm/components/image/v3/image",
            model.getCoreImageResourceType()
        );

        final Resource normalizedResource = model.getResource();

        assertNotNull(normalizedResource);
        assertEquals(
            "core/wcm/components/image/v3/image",
            normalizedResource.getResourceType()
        );
        assertEquals(
            "/content/dam/sample-content/content/RD8_RD8_tCCD5_3ds_core.svg",
            normalizedResource.getValueMap().get("fileReference", String.class)
        );
        assertEquals(
            "Timing diagram",
            normalizedResource.getValueMap().get("alt", String.class)
        );
        assertEquals(
            "/content/dam/sample-content/content/RD8_RD8_tCCD5_3ds_core.svg",
            model.getFileReference()
        );
        assertEquals("Timing diagram", model.getAlt());
        assertEquals("image hero-image", model.getSourceClasses());
    }

    @Test
    void usesSrcAsFallbackWhenFileReferenceIsMissing() {
        final Resource resource = context.create().resource(
            "/content/image",
            "sling:resourceType", NormalizedImageModel.RESOURCE_TYPE,
            "src",
            "/content/dam/sample-content/content/WR8_WR8_tCCD4_3ds_core.svg"
                + "/jcr:content/renditions/original"
        );

        final NormalizedImageModel model =
            resource.adaptTo(NormalizedImageModel.class);

        assertNotNull(model);
        assertEquals(
            "/content/dam/sample-content/content/WR8_WR8_tCCD4_3ds_core.svg",
            model.getFileReference()
        );
    }
}
