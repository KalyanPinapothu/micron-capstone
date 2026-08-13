package com.micron.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.micron.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class CustomFooterModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(CustomFooterModel.class);
    }

    @Test
    void formatsAuthoredFooterLinks() {
        Page page = context.create().page("/content/micron/us/en/home");
        Resource component = context.create().resource(page, "footer",
            "sling:resourceType", "micron/components/structure/customfooter",
            "logoLink", "/content/micron/us/en",
            "ctaLink", "/content/micron/us/en/contact");
        context.create().resource(component, "primaryLinks");
        context.create().resource(component.getPath() + "/primaryLinks/item0",
            "label", "Products",
            "link", "/content/micron/us/en/home-page-content",
            "openInNewTab", false);
        context.create().resource(component.getPath() + "/primaryLinks/item1",
            "label", "External",
            "link", "www.google.com",
            "openInNewTab", true);
        context.create().resource(component, "socialLinks");
        context.create().resource(component.getPath() + "/socialLinks/item0",
            "label", "LinkedIn",
            "link", "linkedin.com/company/micron",
            "icon", "/content/dam/micron/linkedin.svg");
        context.create().resource(component, "legalLinks");
        context.create().resource(component.getPath() + "/legalLinks/item0",
            "label", "Privacy",
            "link", "/content/micron/us/en/privacy");

        context.currentPage(page);
        context.currentResource(component);

        CustomFooterModel model = context.request().adaptTo(CustomFooterModel.class);

        assertEquals("/content/micron/us/en.html", model.getLogoLink());
        assertEquals("/content/micron/us/en/contact.html", model.getCtaLink());
        assertEquals("Contact us", model.getContactLabel());
        assertEquals("Micron", model.getLogoAlt());
        assertEquals(6, model.getSections().size());

        List<CustomFooterModel.LinkItem> primaryItems = model.getPrimaryItems();
        assertEquals(2, primaryItems.size());
        assertEquals("Products", primaryItems.get(0).getLabel());
        assertEquals("/content/micron/us/en/home-page-content.html", primaryItems.get(0).getLink());
        assertEquals("https://www.google.com", primaryItems.get(1).getLink());
        assertEquals(true, primaryItems.get(1).isOpenInNewTab());

        List<CustomFooterModel.LinkItem> socialItems = model.getSocialItems();
        assertEquals(1, socialItems.size());
        assertEquals("https://linkedin.com/company/micron", socialItems.get(0).getLink());
        assertEquals("/content/dam/micron/linkedin.svg", socialItems.get(0).getIcon());

        List<CustomFooterModel.LinkItem> legalItems = model.getLegalItems();
        assertEquals(1, legalItems.size());
        assertEquals("/content/micron/us/en/privacy.html", legalItems.get(0).getLink());
    }

    @Test
    void providesDefaultsWhenLinksAreMissing() {
        Resource component = context.create().resource("/content/micron/us/en/home/jcr:content/root/footer",
            "sling:resourceType", "micron/components/structure/customfooter",
            "logoLink", " ");

        CustomFooterModel model = component.adaptTo(CustomFooterModel.class);

        assertEquals("/", model.getLogoLink());
        assertEquals("https://in.micron.com/about/contact-us", model.getCtaLink());
        assertEquals(6, model.getSections().size());
        assertEquals("Products", model.getSections().get(0).getTitle());
        assertEquals(0, model.getPrimaryItems().size());
        assertEquals(0, model.getSocialItems().size());
        assertEquals(0, model.getLegalItems().size());
    }
}
