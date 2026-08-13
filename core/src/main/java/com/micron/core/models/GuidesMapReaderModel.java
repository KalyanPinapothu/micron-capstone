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
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GuidesMapReaderModel {

    private static final List<String> RENDERABLE_RESOURCE_TYPES =
            java.util.Arrays.asList(
                    "micron/components/title",
                    "micron/components/text",
                    "micron/components/image",
                    "micron/components/table",
                    "micron/components/figure",
                    "core/wcm/components/title/v3/title",
                    "core/wcm/components/text/v2/text",
                    "core/wcm/components/image/v3/image"
            );

    private static final List<String> READER_RESOURCE_TYPES =
            java.util.Arrays.asList(
                    "micron/components/guidesmapreader",
                    "micron/components/guideshomereader",
                    "micron/components/guidessidenavigation"
            );

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
    @Default(intValues = 5)
    private int navigationRootLevel;

    @ValueMapValue
    @Default(intValues = 5)
    private int structureDepth;

    private List<TopicSection> sections = Collections.emptyList();

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

        List<TopicSection> topicSections = new ArrayList<>();
        Iterator<Page> childPages = rootPage.listChildren(new PageFilter(false, true), false);
        while (childPages.hasNext()) {
            collectSections(childPages.next(), 1, topicSections);
        }
        sections = topicSections;
    }

    public List<TopicSection> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public boolean isEmpty() {
        return sections.isEmpty();
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

    private void collectSections(Page page, int depth, List<TopicSection> topicSections) {
        List<Resource> contentResources = getContentResources(page);
        if (!contentResources.isEmpty()) {
            topicSections.add(new TopicSection(page, contentResources));
        }

        if (depth >= structureDepth) {
            return;
        }

        Iterator<Page> childPages = page.listChildren(new PageFilter(false, true), false);
        while (childPages.hasNext()) {
            collectSections(childPages.next(), depth + 1, topicSections);
        }
    }

    private List<Resource> getContentResources(Page page) {
        Resource contentResource = page.getContentResource();
        if (contentResource == null) {
            return Collections.emptyList();
        }

        Resource sourceContainer = findById(contentResource, "topic-source-content");
        if (sourceContainer != null) {
            return contentResourcesFromContainer(sourceContainer);
        }

        Resource topicContainer = findById(contentResource, "topic-container");
        if (topicContainer != null) {
            return contentResourcesFromContainer(topicContainer);
        }

        Resource injectedContainer = findByName(contentResource, "injected-container");
        if (injectedContainer != null) {
            return contentResourcesFromContainer(injectedContainer);
        }

        Resource root = contentResource.getChild("root");
        if (root != null) {
            List<Resource> mappedResources = mappedContentResources(root);
            return mappedResources.isEmpty()
                    ? visibleChildren(root)
                    : mappedResources;
        }

        return mappedContentResources(contentResource);
    }

    private Resource findById(Resource resource, String id) {
        if (id.equals(resource.getValueMap().get("id", String.class))) {
            return resource;
        }

        for (Resource child : resource.getChildren()) {
            Resource match = findById(child, id);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private Resource findByName(Resource resource, String name) {
        if (name.equals(resource.getName())) {
            return resource;
        }

        for (Resource child : resource.getChildren()) {
            Resource match = findByName(child, name);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private List<Resource> mappedContentResources(Resource resource) {
        List<Resource> resources = new ArrayList<>();
        collectMappedContentResources(resource, resources);
        return resources;
    }

    private List<Resource> contentResourcesFromContainer(Resource resource) {
        List<Resource> mappedResources = mappedContentResources(resource);
        return mappedResources.isEmpty()
                ? visibleChildren(resource)
                : mappedResources;
    }

    private void collectMappedContentResources(Resource resource, List<Resource> resources) {
        for (Resource child : resource.getChildren()) {
            if (isIgnoredResource(child)) {
                continue;
            }

            String resourceType = child.getResourceType();
            if (isRenderableResourceType(resourceType)) {
                resources.add(child);
            } else {
                collectMappedContentResources(child, resources);
            }
        }
    }

    private List<Resource> visibleChildren(Resource resource) {
        List<Resource> children = new ArrayList<>();
        for (Resource child : resource.getChildren()) {
            if (!isIgnoredResource(child)) {
                String resourceType = child.getResourceType();
                if (!READER_RESOURCE_TYPES.contains(resourceType)) {
                    children.add(child);
                }
            }
        }
        return children;
    }

    private boolean isIgnoredResource(Resource resource) {
        return resource.getName().startsWith("cq:")
                || "rep:policy".equals(resource.getName());
    }

    private boolean isRenderableResourceType(String resourceType) {
        return RENDERABLE_RESOURCE_TYPES.contains(resourceType);
    }

    public static class TopicSection {
        private final Page page;
        private final List<Resource> contentResources;

        TopicSection(Page page, List<Resource> contentResources) {
            this.page = page;
            this.contentResources = contentResources;
        }

        public String getAnchorId() {
            return GuidesAnchorUtil.fromPath(page.getPath());
        }

        public String getTitle() {
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

        public List<Resource> getContentResources() {
            return Collections.unmodifiableList(contentResources);
        }
    }
}
