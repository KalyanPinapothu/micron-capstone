package com.micron.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.micron.core.testcontext.AppAemContext;
import com.micron.core.models.CustomBreadcrumbModel;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class CustomBreadcrumbModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(CustomBreadcrumbModel.class);
    }

    @Test
    void buildsBreadcrumbFromConfiguredStartLevel() {
        context.create().page("/content/micron", null, "jcr:title", "Micron");
        context.create().page("/content/micron/us", null, "navTitle", "United States");
        context.create().page("/content/micron/us/en", null, "jcr:title", "English");
        Page page = context.create().page("/content/micron/us/en/home", null,
                "pageTitle", "Home Page");
        Resource component = context.create().resource(page, "breadcrumb",
                "sling:resourceType", "micron/components/structure/custombreadcrumb",
                "startLevel", 2,
                "showCurrent", true);
        context.currentPage(page);
        context.currentResource(component);

        CustomBreadcrumbModel model = context.request().adaptTo(CustomBreadcrumbModel.class);
        List<CustomBreadcrumbModel.Item> items = model.getItems();

        assertFalse(model.isEmpty());
        assertEquals(3, items.size());
        assertEquals("United States", items.get(0).getTitle());
        assertEquals("/content/micron/us.html", items.get(0).getHref());
        assertEquals("English", items.get(1).getTitle());
        assertEquals("Home Page", items.get(2).getTitle());
        assertTrue(items.get(2).isCurrent());
    }

    @Test
    void excludesCurrentPageWhenConfigured() {
        context.create().page("/content/micron", null, "jcr:title", "Micron");
        context.create().page("/content/micron/us", null, "jcr:title", "United States");
        context.create().page("/content/micron/us/en", null, "jcr:title", "English");
        Page page = context.create().page("/content/micron/us/en/home", null, "jcr:title", "Home");
        Resource component = context.create().resource(page, "breadcrumb",
                "sling:resourceType", "micron/components/structure/custombreadcrumb",
                "startLevel", "2",
                "showCurrent", "false");
        context.currentPage(page);
        context.currentResource(component);

        CustomBreadcrumbModel model = context.request().adaptTo(CustomBreadcrumbModel.class);

        assertEquals(2, model.getItems().size());
        assertEquals("English", model.getItems().get(1).getTitle());
        assertTrue(model.getItems().get(1).isCurrent());
    }
}
