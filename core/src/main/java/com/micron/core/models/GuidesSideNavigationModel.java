package com.micron.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GuidesSideNavigationModel {

    @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
    private Page currentPage;

    @SlingObject(injectionStrategy = InjectionStrategy.OPTIONAL)
    private ResourceResolver resourceResolver;

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private SlingHttpServletRequest request;

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private Resource resource;

    @ValueMapValue
    private String navigationRoot;

    @ValueMapValue
    @Default(intValues = 4)
    private int navigationRootLevel;

    @ValueMapValue
    @Default(intValues = 4)
    private int structureDepth;

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean anchorLinks;

    private List<NavItem> items = Collections.emptyList();

    @PostConstruct
    protected void init() {
        if (resource == null && request != null) {
            resource = request.getResource();
        }
        if (resourceResolver == null && request != null) {
            resourceResolver = request.getResourceResolver();
        }
        if (currentPage == null && request != null && resourceResolver != null) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            currentPage = pageManager != null ? pageManager.getContainingPage(request.getResource()) : null;
        }

        Page rootPage = getRootPage();
        if (rootPage == null) {
            return;
        }

        List<NavItem> children = new ArrayList<>();
        Iterator<Page> childPages = rootPage.listChildren(new PageFilter(false, true), false);
        while (childPages.hasNext()) {
            Page childPage = childPages.next();
            if (isGeneratedPage(childPage)) {
                continue;
            }
            children.add(toNavItem(childPage, 1));
        }
        items = children;
    }

    public List<NavItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private Page getRootPage() {
        if (resourceResolver == null) {
            return null;
        }

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        if (StringUtils.isNotBlank(navigationRoot) && pageManager != null) {
            Page configuredRoot = pageManager.getPage(navigationRoot);
            if (configuredRoot != null) {
                return configuredRoot;
            }
        }

        if (currentPage == null) {
            return null;
        }

        if (resource != null && "micron/components/guideshomereader".equals(resource.getResourceType())) {
            return currentPage;
        }

        Page ancestor = currentPage.getAbsoluteParent(navigationRootLevel);
        if (ancestor != null && (hasVisibleChildren(ancestor) || currentPage.equals(ancestor))) {
            return ancestor;
        }

        Page parent = currentPage.getParent();
        return parent != null ? parent : currentPage;
    }

    private boolean hasVisibleChildren(Page page) {
        return page.listChildren(new PageFilter(false, true), false).hasNext();
    }

    private NavItem toNavItem(Page page, int depth) {
        List<NavItem> children = new ArrayList<>();
        if (depth < structureDepth) {
            Iterator<Page> childPages = page.listChildren(new PageFilter(false, true), false);
            while (childPages.hasNext()) {
                Page childPage = childPages.next();
                if (isGeneratedPage(childPage)) {
                    continue;
                }
                children.add(toNavItem(childPage, depth + 1));
            }
        }

        return new NavItem(page.getPath(), getTitle(page), isActive(page), anchorLinks, children);
    }

    private boolean isGeneratedPage(Page page) {
        return page.getName().startsWith("__");
    }

    private boolean isActive(Page page) {
        return currentPage != null && (currentPage.equals(page) || currentPage.getPath().startsWith(page.getPath() + "/"));
    }

    private String getTitle(Page page) {
        if (StringUtils.isNotBlank(page.getNavigationTitle())) {
            return page.getNavigationTitle();
        }
        if (StringUtils.isNotBlank(page.getPageTitle())) {
            return page.getPageTitle();
        }
        if (StringUtils.isNotBlank(page.getTitle())) {
            return page.getTitle();
        }
        return page.getName();
    }

    public static class NavItem {
        private final String path;
        private final String title;
        private final boolean active;
        private final boolean anchorLinks;
        private final List<NavItem> children;

        NavItem(String path, String title, boolean active, boolean anchorLinks, List<NavItem> children) {
            this.path = path;
            this.title = title;
            this.active = active;
            this.anchorLinks = anchorLinks;
            this.children = children;
        }

        public String getPath() {
            return path;
        }

        public String getHref() {
            return anchorLinks ? "#" + GuidesAnchorUtil.fromPath(path) : path + ".html";
        }

        public String getPageHref() {
            return path + ".html";
        }

        public String getAnchorId() {
            return GuidesAnchorUtil.fromPath(path);
        }

        public String getTitle() {
            return title;
        }

        public boolean isActive() {
            return active;
        }

        public List<NavItem> getChildren() {
            return Collections.unmodifiableList(children);
        }
    }
}
