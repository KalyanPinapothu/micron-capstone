package com.micron.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.micron.core.testcontext.AppAemContext;
import com.micron.core.models.GuidesSideNavigationModel;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class GuidesSideNavigationModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(GuidesSideNavigationModel.class);
    }

    @Test
    void buildsNavigationFromConfiguredRootWithAnchorLinks() {
        context.create().page("/content/micron/us/en/kb", null, "jcr:title", "Knowledge Base");
        Page child = context.create().page("/content/micron/us/en/kb/accounts", null,
                "navTitle", "Accounts");
        context.create().page("/content/micron/us/en/kb/accounts/checking", null,
                "pageTitle", "Checking");
        context.create().page("/content/micron/us/en/kb/loans", null,
                "jcr:title", "Loans");
        context.create().page("/content/micron/us/en/kb/__references__", null,
                "jcr:title", "References");
        Page currentPage = context.create().page("/content/micron/us/en/kb/accounts/checking/details", null,
                "jcr:title", "Details");
        Resource component = context.create().resource(currentPage, "side-nav",
                "sling:resourceType", "micron/components/guidessidenavigation",
                "navigationRoot", "/content/micron/us/en/kb",
                "structureDepth", 2,
                "anchorLinks", true);
        context.currentPage(currentPage);
        context.currentResource(component);

        GuidesSideNavigationModel model = context.request().adaptTo(GuidesSideNavigationModel.class);
        List<GuidesSideNavigationModel.NavItem> items = model.getItems();

        assertEquals(2, items.size());
        assertEquals("Accounts", items.get(0).getTitle());
        assertEquals("#content-micron-us-en-kb-accounts", items.get(0).getHref());
        assertEquals(child.getPath() + ".html", items.get(0).getPageHref());
        assertTrue(items.get(0).isActive());
        assertEquals(1, items.get(0).getChildren().size());
        assertEquals("Checking", items.get(0).getChildren().get(0).getTitle());
        assertThrows(UnsupportedOperationException.class, () -> items.add(items.get(0)));
        assertThrows(UnsupportedOperationException.class, () -> items.get(0).getChildren().clear());
    }

    @Test
    void excludesGeneratedPagesAtEveryNavigationLevel() {
        context.create().page("/content/micron/us/en/kb", null, "jcr:title", "Knowledge Base");
        context.create().page("/content/micron/us/en/kb/features", null, "jcr:title", "Features");
        context.create().page("/content/micron/us/en/kb/features/__references__", null,
                "jcr:title", "References");
        Page currentPage = context.create().page("/content/micron/us/en/kb/features/general-description", null,
                "jcr:title", "General description");
        Resource component = context.create().resource(currentPage, "side-nav",
                "sling:resourceType", "micron/components/guidessidenavigation",
                "navigationRoot", "/content/micron/us/en/kb",
                "structureDepth", 3);
        context.currentPage(currentPage);
        context.currentResource(component);

        GuidesSideNavigationModel model = context.request().adaptTo(GuidesSideNavigationModel.class);

        assertEquals(1, model.getItems().size());
        assertEquals("Features", model.getItems().get(0).getTitle());
        assertEquals(1, model.getItems().get(0).getChildren().size());
        assertEquals("General description", model.getItems().get(0).getChildren().get(0).getTitle());
    }

    @Test
    void usesCurrentPageAsRootWhenConfiguredAncestorIsCurrentPageWithVisibleChildren() {
        Page currentPage = context.create().page("/content/micron/us/en/topic", null, "jcr:title", "Topic");
        context.create().page("/content/micron/us/en/topic/child", null, "jcr:title", "Child");
        Resource component = context.create().resource(currentPage, "side-nav",
                "sling:resourceType", "micron/components/guidessidenavigation",
                "navigationRootLevel", 7,
                "structureDepth", 1);
        context.currentPage(currentPage);
        context.currentResource(component);

        GuidesSideNavigationModel model = context.request().adaptTo(GuidesSideNavigationModel.class);

        assertEquals(1, model.getItems().size());
        assertEquals("/content/micron/us/en/topic/child.html", model.getItems().get(0).getHref());
    }
}
