package com.micron.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

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

@Model(
    adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MicronHeader {

    private static final String DEFAULT_LOGO_PATH =
        "https://assets.micron.com/adobe/assets/urn:aaid:aem:652506f6-3869-4e9b-a1bd-d5b4209f850c/as/Micron-logo-white.svg";

    @ValueMapValue private String logoPath;
    @ValueMapValue private String logoAlt;
    @ValueMapValue private String homeUrl;
    @ValueMapValue private String searchAction;
    @ValueMapValue private String searchPlaceholder;
    @ValueMapValue private String signInLabel;
    @ValueMapValue private String signInUrl;
    @ValueMapValue private String regionLabel;

    @ChildResource(name = "utilityLinks")
    private Resource utilityLinksResource;

    @ChildResource(name = "mainNavigation")
    private Resource mainNavigationResource;

    @SlingObject(injectionStrategy = InjectionStrategy.OPTIONAL)
    private ResourceResolver resourceResolver;

    private List<LinkItem> utilityLinks;
    private List<MenuItem> mainNavigation;

    @PostConstruct
    protected void init() {
        utilityLinks = readLinks(utilityLinksResource);
        mainNavigation = readMenus(mainNavigationResource);
    }

    public String getLogoPath() { return defaultString(logoPath, DEFAULT_LOGO_PATH); }
    public String getLogoAlt() { return defaultString(logoAlt, "Micron"); }
    public String getHomeUrl() { return defaultString(formatLink(homeUrl), "/"); }
    public String getSearchAction() { return defaultString(formatLink(searchAction), "/search"); }
    public String getSearchPlaceholder() { return defaultString(searchPlaceholder, "Search Micron Technology"); }
    public String getSignInLabel() { return defaultString(signInLabel, "Sign in"); }
    public String getSignInUrl() { return defaultString(formatLink(signInUrl), "#"); }
    public String getRegionLabel() { return defaultString(regionLabel, "India - English"); }
    public List<LinkItem> getUtilityLinks() { return utilityLinks; }
    public List<MenuItem> getMainNavigation() { return mainNavigation; }

    private List<MenuItem> readMenus(Resource root) {
        if (root == null) return Collections.emptyList();
        List<MenuItem> result = new ArrayList<>();
        for (Resource item : root.getChildren()) {
            ValueMap vm = item.getValueMap();
            result.add(new MenuItem(
                vm.get("title", String.class),
                formatLink(vm.get("url", String.class)),
                vm.get("featuredLabel", String.class),
                formatLink(vm.get("featuredUrl", String.class)),
                readColumns(item.getChild("columns"))
            ));
        }
        return Collections.unmodifiableList(result);
    }

    private List<MenuColumn> readColumns(Resource root) {
        if (root == null) return Collections.emptyList();
        List<MenuColumn> result = new ArrayList<>();
        for (Resource item : root.getChildren()) {
            ValueMap vm = item.getValueMap();
            result.add(new MenuColumn(
                vm.get("heading", String.class),
                formatLink(vm.get("headingUrl", String.class)),
                readLinks(item.getChild("links"))
            ));
        }
        return Collections.unmodifiableList(result);
    }

    private List<LinkItem> readLinks(Resource root) {
        if (root == null) return Collections.emptyList();
        List<LinkItem> result = new ArrayList<>();
        for (Resource item : root.getChildren()) {
            ValueMap vm = item.getValueMap();
            result.add(new LinkItem(
                vm.get("label", String.class),
                formatLink(vm.get("url", String.class)),
                Boolean.TRUE.equals(vm.get("newTab", Boolean.class))
            ));
        }
        return Collections.unmodifiableList(result);
    }

    private static String defaultString(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String formatLink(String link) {
        return LinkUtils.getFormattedLink(link, resourceResolver);
    }

    public static final class LinkItem {
        private final String label;
        private final String url;
        private final boolean newTab;
        LinkItem(String label, String url, boolean newTab) {
            this.label = label;
            this.url = url;
            this.newTab = newTab;
        }
        public String getLabel() { return label; }
        public String getUrl() { return url; }
        public boolean isNewTab() { return newTab; }
    }

    public static final class MenuColumn {
        private final String heading;
        private final String headingUrl;
        private final List<LinkItem> links;
        MenuColumn(String heading, String headingUrl, List<LinkItem> links) {
            this.heading = heading;
            this.headingUrl = headingUrl;
            this.links = links;
        }
        public String getHeading() { return heading; }
        public String getHeadingUrl() { return headingUrl; }
        public List<LinkItem> getLinks() { return links; }
    }

    public static final class MenuItem {
        private final String title;
        private final String url;
        private final String featuredLabel;
        private final String featuredUrl;
        private final List<MenuColumn> columns;
        MenuItem(String title, String url, String featuredLabel, String featuredUrl, List<MenuColumn> columns) {
            this.title = title;
            this.url = url;
            this.featuredLabel = featuredLabel;
            this.featuredUrl = featuredUrl;
            this.columns = columns;
        }
        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getFeaturedLabel() { return featuredLabel; }
        public String getFeaturedUrl() { return featuredUrl; }
        public List<MenuColumn> getColumns() { return columns; }
        public boolean isHasMegaMenu() { return columns != null && !columns.isEmpty(); }
    }
}
