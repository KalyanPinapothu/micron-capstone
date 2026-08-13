package com.micron.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.micron.core.testcontext.AppAemContext;
import com.micron.core.models.GuidesMapReaderModel;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class GuidesMapReaderModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(GuidesMapReaderModel.class);
    }

    @Test
    void collectsVisibleTopicSourceContentFromConfiguredRoot() {
        context.create().page("/content/micron/us/en/kb", null, "jcr:title", "Knowledge Base");
        Page topic = context.create().page("/content/micron/us/en/kb/accounts", null,
                "navTitle", "Accounts");
        context.create().resource(topic.getContentResource(), "root/topic-source",
                "sling:resourceType", "core/wcm/components/container/v1/container",
                "id", "topic-source-content");
        Resource text = context.create().resource(topic.getContentResource(), "root/topic-source/text",
                "sling:resourceType", "core/wcm/components/text/v2/text",
                "text", "Account content");
        context.create().resource(topic.getContentResource(), "root/topic-source/reader",
                "sling:resourceType", "micron/components/guidesmapreader");
        context.create().resource(topic.getContentResource(), "root/topic-source/cq:responsive",
                "sling:resourceType", "nt:unstructured");
        Page currentPage = context.create().page("/content/micron/us/en/kb/current", null,
                "jcr:title", "Current");
        Resource component = context.create().resource(currentPage, "map-reader",
                "sling:resourceType", "micron/components/guidesmapreader",
                "navigationRoot", "/content/micron/us/en/kb",
                "structureDepth", 1);
        context.currentPage(currentPage);
        context.currentResource(component);

        GuidesMapReaderModel model = context.request().adaptTo(GuidesMapReaderModel.class);
        List<GuidesMapReaderModel.TopicSection> sections = model.getSections();

        assertFalse(model.isEmpty());
        assertEquals(1, sections.size());
        assertEquals("Accounts", sections.get(0).getTitle());
        assertEquals("content-micron-us-en-kb-accounts", sections.get(0).getAnchorId());
        assertEquals(1, sections.get(0).getContentResources().size());
        assertEquals(text.getPath(), sections.get(0).getContentResources().get(0).getPath());
        assertThrows(UnsupportedOperationException.class, () -> sections.clear());
        assertThrows(UnsupportedOperationException.class, () -> sections.get(0).getContentResources().clear());
    }

    @Test
    void recursivelyCollectsMappedContentInsideKnownTopicContainer() {
        context.create().page("/content/micron/us/en/kb", null, "jcr:title", "Knowledge Base");
        Page topic = context.create().page("/content/micron/us/en/kb/features", null,
                "navTitle", "Features");
        context.create().resource(topic.getContentResource(), "root/topic-source",
                "sling:resourceType", "core/wcm/components/container/v1/container",
                "id", "topic-source-content");
        Resource title = context.create().resource(topic.getContentResource(), "root/topic-source/title",
                "sling:resourceType", "micron/components/title",
                "text", "Feature title");
        Resource text = context.create().resource(topic.getContentResource(), "root/topic-source/text",
                "sling:resourceType", "micron/components/text",
                "text", "Feature content");
        Resource image = context.create().resource(topic.getContentResource(), "root/topic-source/nested/media/image",
                "sling:resourceType", "micron/components/image",
                "fileReference", "/content/dam/feature.svg");
        Resource table = context.create().resource(topic.getContentResource(), "root/topic-source/nested/table",
                "sling:resourceType", "micron/components/table",
                "tableHtml", "<table><tbody><tr><td>Feature</td></tr></tbody></table>");
        context.create().resource(topic.getContentResource(), "root/topic-source/nested/reader",
                "sling:resourceType", "micron/components/guidesmapreader");
        Resource component = context.create().resource("/content/components/map-reader",
                "sling:resourceType", "micron/components/guidesmapreader",
                "navigationRoot", "/content/micron/us/en/kb",
                "structureDepth", 1);

        GuidesMapReaderModel model = component.adaptTo(GuidesMapReaderModel.class);

        assertEquals(1, model.getSections().size());
        assertEquals(4, model.getSections().get(0).getContentResources().size());
        assertEquals(title.getPath(), model.getSections().get(0).getContentResources().get(0).getPath());
        assertEquals(text.getPath(), model.getSections().get(0).getContentResources().get(1).getPath());
        assertEquals(image.getPath(), model.getSections().get(0).getContentResources().get(2).getPath());
        assertEquals(table.getPath(), model.getSections().get(0).getContentResources().get(3).getPath());
    }

    @Test
    void fallsBackToMappedContentResourcesWhenNoKnownContainerExists() {
        context.create().page("/content/micron/us/en/kb", null, "jcr:title", "Knowledge Base");
        Page topic = context.create().page("/content/micron/us/en/kb/cards", null,
                "pageTitle", "Cards");
        Resource title = context.create().resource(topic.getContentResource(), "root/container/title",
                "sling:resourceType", "micron/components/title",
                "text", "Card title");
        Resource text = context.create().resource(topic.getContentResource(), "root/container/text",
                "sling:resourceType", "micron/components/text",
                "text", "Card content");
        Resource image = context.create().resource(topic.getContentResource(), "root/container/media/image",
                "sling:resourceType", "micron/components/image",
                "fileReference", "/content/dam/card.svg");
        Resource table = context.create().resource(topic.getContentResource(), "root/container/table",
                "sling:resourceType", "micron/components/table",
                "tableHtml", "<table><tbody><tr><td>Card</td></tr></tbody></table>");
        Resource component = context.create().resource("/content/components/map-reader",
                "sling:resourceType", "micron/components/guidesmapreader",
                "navigationRoot", "/content/micron/us/en/kb",
                "structureDepth", 1);

        GuidesMapReaderModel model = component.adaptTo(GuidesMapReaderModel.class);

        assertEquals(1, model.getSections().size());
        assertEquals("Cards", model.getSections().get(0).getTitle());
        assertEquals(4, model.getSections().get(0).getContentResources().size());
        assertEquals(title.getPath(), model.getSections().get(0).getContentResources().get(0).getPath());
        assertEquals(text.getPath(), model.getSections().get(0).getContentResources().get(1).getPath());
        assertEquals(image.getPath(), model.getSections().get(0).getContentResources().get(2).getPath());
        assertEquals(table.getPath(), model.getSections().get(0).getContentResources().get(3).getPath());
    }

    @Test
    void staysEmptyWhenNoRootCanBeResolved() {
        Resource component = context.create().resource("/content/components/map-reader",
                "sling:resourceType", "micron/components/guidesmapreader");

        GuidesMapReaderModel model = component.adaptTo(GuidesMapReaderModel.class);

        assertTrue(model.isEmpty());
    }
}
