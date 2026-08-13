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
import com.micron.core.models.CustomHeaderModel;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class CustomHeaderModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(CustomHeaderModel.class);
    }

    @Test
    void providesDefaultHeaderValuesWhenNothingIsAuthored() {
        Resource component = context.create().resource("/content/micron/us/en/home/jcr:content/root/header",
                "sling:resourceType", "micron/components/structure/customheader");

        CustomHeaderModel model = component.adaptTo(CustomHeaderModel.class);

        assertEquals("Site header", model.getAriaLabel());
        assertEquals("MENU", model.getMenuLabel());
        assertEquals("Open menu", model.getMenuAriaLabel());
        assertEquals("LINK TEXT", model.getLinkText());
        assertEquals("#", model.getLinkHref());
        assertEquals(null, model.getLogoReference());
        assertEquals("Micron", model.getLogoAlt());
        assertEquals("/", model.getLogoLink());
        assertEquals("micron", model.getBrandText());
        assertEquals("Micron home", model.getBrandAriaLabel());
        assertEquals("Mobile navigation", model.getMobileNavigationLabel());

        List<CustomHeaderModel.LinkItem> actions = model.getActions();
        assertEquals(3, actions.size());
        assertEquals("Search", actions.get(0).getLabel());
        assertEquals("#", actions.get(0).getHref());
        assertEquals("&#128269;", actions.get(0).getIcon());
        assertTrue(actions.get(0).isIconOnly());
        assertFalse(actions.get(0).isButton());
        assertFalse(actions.get(0).isDropdown());

        assertEquals("Select language", actions.get(1).getLabel());
        assertEquals("", actions.get(1).getHref());
        assertTrue(actions.get(1).isIconOnly());
        assertTrue(actions.get(1).isButton());

        assertEquals("SIGN IN", actions.get(2).getLabel());
        assertEquals("v", actions.get(2).getIcon());
        assertFalse(actions.get(2).isIconOnly());
        assertTrue(actions.get(2).isButton());
        assertTrue(actions.get(2).isDropdown());

        List<CustomHeaderModel.LinkItem> navigationItems = model.getNavigationItems();
        assertEquals(3, navigationItems.size());
        assertEquals("Products", navigationItems.get(0).getLabel());
        assertEquals("Sales & Support", navigationItems.get(1).getLabel());
        assertEquals("About", navigationItems.get(2).getLabel());
    }

    @Test
    void readsAuthoredFieldsAndMultifieldsFromRequestResource() {
        Page page = context.create().page("/content/micron/us/en/home");
        Resource component = context.create().resource(page, "header",
                "sling:resourceType", "micron/components/structure/customheader",
                "ariaLabel", "Primary header",
                "menuLabel", "Explore",
                "menuAriaLabel", "Open primary menu",
                "linkText", "Investors",
                "linkHref", "/content/micron/us/en/investors",
                "logoReference", "/content/dam/micron/logo.svg",
                "logoAlt", "Micron",
                "logoLink", "/content/micron/us/en",
                "brandText", "micron",
                "mobileNavigationLabel", "Primary mobile navigation");
        context.create().resource(component, "actionLinks");
        context.create().resource(component.getPath() + "/actionLinks/item0",
                "label", "Search",
                "href", "/content/micron/us/en/search",
                "icon", "&#128269;",
                "dropdown", false);
        context.create().resource(component.getPath() + "/actionLinks/item1",
                "label", "Sign in",
                "href", "",
                "icon", "v",
                "dropdown", true);
        context.create().resource(component.getPath() + "/actionLinks/item2",
                "label", "   ",
                "href", "/ignored",
                "icon", "x");
        context.create().resource(component, "mobileLinks");
        context.create().resource(component.getPath() + "/mobileLinks/item0",
                "label", "Cards",
                "href", "/content/micron/us/en/cards");
        context.create().resource(component.getPath() + "/mobileLinks/item1",
                "label", "Support",
                "href", "/content/micron/us/en/support");

        context.currentPage(page);
        context.currentResource(component);

        CustomHeaderModel model = context.request().adaptTo(CustomHeaderModel.class);

        assertEquals("Primary header", model.getAriaLabel());
        assertEquals("Explore", model.getMenuLabel());
        assertEquals("Open primary menu", model.getMenuAriaLabel());
        assertEquals("Investors", model.getLinkText());
        assertEquals("/content/micron/us/en/investors.html", model.getLinkHref());
        assertEquals("/content/dam/micron/logo.svg", model.getLogoReference());
        assertEquals("Micron", model.getLogoAlt());
        assertEquals("/content/micron/us/en.html", model.getLogoLink());
        assertEquals("micron", model.getBrandText());
        assertEquals("Micron", model.getBrandAriaLabel());
        assertEquals("Primary mobile navigation", model.getMobileNavigationLabel());

        List<CustomHeaderModel.LinkItem> actions = model.getActions();
        assertEquals(2, actions.size());
        assertEquals("Search", actions.get(0).getLabel());
        assertEquals("/content/micron/us/en/search.html", actions.get(0).getHref());
        assertEquals("&#128269;", actions.get(0).getIcon());
        assertTrue(actions.get(0).isIconOnly());
        assertFalse(actions.get(0).isButton());
        assertFalse(actions.get(0).isDropdown());
        assertEquals("Sign in", actions.get(1).getLabel());
        assertEquals("", actions.get(1).getHref());
        assertEquals("v", actions.get(1).getIcon());
        assertFalse(actions.get(1).isIconOnly());
        assertTrue(actions.get(1).isButton());
        assertTrue(actions.get(1).isDropdown());

        List<CustomHeaderModel.LinkItem> navigationItems = model.getNavigationItems();
        assertEquals(2, navigationItems.size());
        assertEquals("Cards", navigationItems.get(0).getLabel());
        assertEquals("/content/micron/us/en/cards.html", navigationItems.get(0).getHref());
        assertEquals("Support", navigationItems.get(1).getLabel());
        assertEquals("/content/micron/us/en/support.html", navigationItems.get(1).getHref());
    }

    @Test
    void fallsBackWhenAuthoredTextIsBlankAndReturnsImmutableLists() {
        Resource component = context.create().resource("/content/micron/us/en/home/jcr:content/root/header",
                "sling:resourceType", "micron/components/structure/customheader",
                "ariaLabel", " ",
                "menuLabel", "",
                "menuAriaLabel", " ",
                "linkText", "",
                "linkHref", " ",
                "logoAlt", "",
                "logoLink", " ",
                "brandText", "",
                "mobileNavigationLabel", " ");
        context.create().resource(component, "actionLinks");
        context.create().resource(component.getPath() + "/actionLinks/item0",
                "label", "",
                "href", "/ignored");
        context.create().resource(component, "mobileLinks");
        context.create().resource(component.getPath() + "/mobileLinks/item0",
                "label", " ",
                "href", "/ignored");

        CustomHeaderModel model = component.adaptTo(CustomHeaderModel.class);

        assertEquals("Site header", model.getAriaLabel());
        assertEquals("MENU", model.getMenuLabel());
        assertEquals("Open menu", model.getMenuAriaLabel());
        assertEquals("LINK TEXT", model.getLinkText());
        assertEquals("#", model.getLinkHref());
        assertEquals("Micron", model.getLogoAlt());
        assertEquals("/", model.getLogoLink());
        assertEquals("micron", model.getBrandText());
        assertEquals("Micron home", model.getBrandAriaLabel());
        assertEquals("Mobile navigation", model.getMobileNavigationLabel());

        assertEquals(3, model.getActions().size());
        assertEquals(3, model.getNavigationItems().size());
        assertThrows(UnsupportedOperationException.class,
                () -> model.getActions().add(new CustomHeaderModel.LinkItem("Help", "#", "", false)));
        assertThrows(UnsupportedOperationException.class,
                () -> model.getNavigationItems().clear());
    }

    @Test
    void linkItemNormalizesBlankHrefAndIcon() {
        CustomHeaderModel.LinkItem link = new CustomHeaderModel.LinkItem("Help", " ", null, false);

        assertEquals("Help", link.getLabel());
        assertEquals("", link.getHref());
        assertEquals("", link.getIcon());
        assertFalse(link.isIconOnly());
        assertTrue(link.isButton());
        assertFalse(link.isDropdown());
    }
}
