package com.micron.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.micron.core.testcontext.AppAemContext;
import com.micron.core.models.GuidesMapMetadataModel;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class GuidesMapMetadataModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(GuidesMapMetadataModel.class);
    }

    @Test
    void readsTitleAndDescriptionFromDamMapMetadata() {
        Page page = context.create().page("/content/micron/us/en/home", null,
                "jcr:title", "Page title",
                "jcr:description", "Page description");
        context.create().resource("/content/dam/maps/sample.ditamap/jcr:content/metadata",
                "dc:title", "  DAM title  ",
                "dc:description", "DAM description");
        Resource component = context.create().resource(page, "metadata",
                "sling:resourceType", "micron/components/guidesmapmetadata",
                "mapPath", "/content/dam/maps/sample.ditamap",
                "category", "Guides",
                "accessLabel", "Locked",
                "versionLabel", "Version 1.0",
                "lastUpdatedLabel", "Updated today");

        GuidesMapMetadataModel model = component.adaptTo(GuidesMapMetadataModel.class);

        assertEquals("DAM title", model.getTitle());
        assertEquals("DAM description", model.getDescription());
        assertEquals("Guides", model.getCategory());
        assertEquals("Locked", model.getAccessLabel());
        assertEquals("Version 1.0", model.getVersionLabel());
        assertEquals("Updated today", model.getLastUpdatedLabel());
        assertFalse(model.isEmpty());
    }

    @Test
    void fallsBackToCurrentPageAndDefaultLabels() {
        Page page = context.create().page("/content/micron/us/en/home", null,
                "jcr:title", "Page title",
                "jcr:description", "Page description");
        Resource component = context.create().resource(page, "metadata",
                "sling:resourceType", "micron/components/guidesmapmetadata");
        context.currentPage(page);
        context.currentResource(component);

        GuidesMapMetadataModel model = context.request().adaptTo(GuidesMapMetadataModel.class);

        assertEquals("Page title", model.getTitle());
        assertEquals("Page description", model.getDescription());
        assertEquals("CATEGORY", model.getCategory());
        assertEquals("Unlocked", model.getAccessLabel());
        assertEquals("Version 3.2", model.getVersionLabel());
        assertEquals("Last Updated", model.getLastUpdatedLabel());
    }

    @Test
    void isEmptyWhenNoMetadataOrPageTextExists() {
        Resource component = context.create().resource("/content/orphan/metadata",
                "sling:resourceType", "micron/components/guidesmapmetadata");

        GuidesMapMetadataModel model = component.adaptTo(GuidesMapMetadataModel.class);

        assertTrue(model.isEmpty());
    }
}
