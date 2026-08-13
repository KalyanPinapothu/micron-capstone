package com.micron.core.models;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Model(adaptables = { Resource.class, SlingHttpServletRequest.class },
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GuidesMapMetadataModel {

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private SlingHttpServletRequest request;

    @SlingObject(injectionStrategy = InjectionStrategy.OPTIONAL)
    private ResourceResolver resourceResolver;

    @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
    private Page currentPage;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String mapPath;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(values = "CATEGORY")
    private String category;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(values = "Unlocked")
    private String accessLabel;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(values = "Version 3.2")
    private String versionLabel;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String lastUpdatedLabel;

    private String title;
    private String description;

    @PostConstruct
    protected void init() {
        if (resourceResolver == null && request != null) {
            resourceResolver = request.getResourceResolver();
        }
        if (currentPage == null && request != null) {
            PageManager pageManager = resourceResolver != null ? resourceResolver.adaptTo(PageManager.class) : null;
            currentPage = pageManager != null ? pageManager.getContainingPage(request.getResource()) : null;
        }

        ValueMap metadata = getMapMetadata();
        title = firstNotBlank(
                metadata.get("dc:title", String.class),
                currentPage != null ? currentPage.getTitle() : null,
                currentPage != null ? currentPage.getName() : null);
        description = firstNotBlank(
                metadata.get("dc:description", String.class),
                currentPage != null ? currentPage.getDescription() : null);
    }

    private ValueMap getMapMetadata() {
        if (StringUtils.isBlank(mapPath)) {
            return ValueMap.EMPTY;
        }

        if (resourceResolver == null) {
            return ValueMap.EMPTY;
        }

        Resource metadataResource = resourceResolver.getResource(mapPath + "/jcr:content/metadata");
        return metadataResource != null ? metadataResource.getValueMap() : ValueMap.EMPTY;
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    public String getCategory() {
        return category;
    }

    public String getAccessLabel() {
        return accessLabel;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getLastUpdatedLabel() {
        return StringUtils.defaultIfBlank(lastUpdatedLabel, "Last Updated");
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(title) && StringUtils.isBlank(description);
    }
}
