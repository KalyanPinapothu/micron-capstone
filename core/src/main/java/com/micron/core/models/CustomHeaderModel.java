package com.micron.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.micron.core.utils.LinkUtils;

@Model(adaptables = { Resource.class, SlingHttpServletRequest.class },
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CustomHeaderModel {

    @ValueMapValue
    private String ariaLabel;

    @ValueMapValue
    private String menuLabel;

    @ValueMapValue
    private String menuAriaLabel;

    @ValueMapValue
    private String linkText;

    @ValueMapValue
    private String linkHref;

    @ValueMapValue
    private String logoReference;

    @ValueMapValue
    private String logoAlt;

    @ValueMapValue
    private String logoLink;

    @ValueMapValue
    private String brandText;

    @ValueMapValue
    private String mobileNavigationLabel;

    @ChildResource
    private Resource actionLinks;

    @ChildResource
    private Resource mobileLinks;

    @SlingObject(injectionStrategy = InjectionStrategy.OPTIONAL)
    private ResourceResolver resourceResolver;

    private List<LinkItem> actions = Collections.emptyList();
    private List<LinkItem> navigationItems = Collections.emptyList();

    @PostConstruct
    protected void init() {
        actions = readLinks(actionLinks);
        if (actions.isEmpty()) {
            List<LinkItem> defaultActions = new ArrayList<>();
            defaultActions.add(new LinkItem("Search", "#", "&#128269;", false));
            defaultActions.add(new LinkItem("Select language", "", "&#9678;", false));
            defaultActions.add(new LinkItem("SIGN IN", "", "v", true));
            actions = defaultActions;
        }

        navigationItems = readLinks(mobileLinks);
        if (navigationItems.isEmpty()) {
            List<LinkItem> defaultNavigation = new ArrayList<>();
            defaultNavigation.add(new LinkItem("Products", "#", "", false));
            defaultNavigation.add(new LinkItem("Sales & Support", "#", "", false));
            defaultNavigation.add(new LinkItem("About", "#", "", false));
            navigationItems = defaultNavigation;
        }
    }

    private List<LinkItem> readLinks(Resource parent) {
        if (parent == null) {
            return Collections.emptyList();
        }

        List<LinkItem> items = new ArrayList<>();
        for (Resource child : parent.getChildren()) {
            ValueMap properties = child.getValueMap();
            String label = properties.get("label", String.class);
            if (StringUtils.isBlank(label)) {
                continue;
            }
            items.add(new LinkItem(
                    label,
                    formatLink(properties.get("href", String.class)),
                    properties.get("icon", String.class),
                    properties.get("dropdown", Boolean.FALSE)));
        }
        return items;
    }

    public String getAriaLabel() {
        return defaultIfBlank(ariaLabel, "Site header");
    }

    public String getMenuLabel() {
        return defaultIfBlank(menuLabel, "MENU");
    }

    public String getMenuAriaLabel() {
        return defaultIfBlank(menuAriaLabel, "Open menu");
    }

    public String getLinkText() {
        return defaultIfBlank(linkText, "LINK TEXT");
    }

    public String getLinkHref() {
        return defaultIfBlank(formatLink(linkHref), "#");
    }

    public String getLogoReference() {
        return logoReference;
    }

    public String getLogoAlt() {
        return defaultIfBlank(logoAlt, "Micron");
    }

    public String getLogoLink() {
        return defaultIfBlank(formatLink(logoLink), "/");
    }

    public String getBrandText() {
        return defaultIfBlank(brandText, "micron");
    }

    public String getBrandAriaLabel() {
        return defaultIfBlank(logoAlt, "Micron home");
    }

    public String getMobileNavigationLabel() {
        return defaultIfBlank(mobileNavigationLabel, "Mobile navigation");
    }

    public List<LinkItem> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public List<LinkItem> getNavigationItems() {
        return Collections.unmodifiableList(navigationItems);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.defaultIfBlank(value, defaultValue);
    }

    private String formatLink(String link) {
        return LinkUtils.getFormattedLink(link, resourceResolver);
    }

    public static class LinkItem {
        private final String label;
        private final String href;
        private final String icon;
        private final boolean dropdown;

        LinkItem(String label, String href, String icon, boolean dropdown) {
            this.label = label;
            this.href = StringUtils.defaultIfBlank(href, "");
            this.icon = StringUtils.defaultIfBlank(icon, "");
            this.dropdown = dropdown;
        }

        public String getLabel() {
            return label;
        }

        public String getHref() {
            return href;
        }

        public String getIcon() {
            return icon;
        }

        public boolean isIconOnly() {
            return StringUtils.isNotBlank(icon) && !dropdown;
        }

        public boolean isButton() {
            return StringUtils.isBlank(href);
        }

        public boolean isDropdown() {
            return dropdown;
        }
    }
}
