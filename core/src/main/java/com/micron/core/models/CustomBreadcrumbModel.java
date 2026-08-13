package com.micron.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Model(adaptables = SlingHttpServletRequest.class,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CustomBreadcrumbModel {

    @Self
    private SlingHttpServletRequest request;

    @ScriptVariable
    private Page currentPage;

    private List<Item> items = Collections.emptyList();

    @PostConstruct
    protected void init() {
        if (currentPage == null) {
            PageManager pageManager = request != null ? request.getResourceResolver().adaptTo(PageManager.class) : null;
            currentPage = pageManager != null ? pageManager.getContainingPage(request.getResource()) : null;
        }

        if (currentPage == null) {
            return;
        }

        List<Item> breadcrumbItems = new ArrayList<>();
        ValueMap properties = request != null ? request.getResource().getValueMap() : ValueMap.EMPTY;
        int resolvedStartLevel = toInt(properties.get("startLevel"), 3);
        boolean resolvedShowCurrent = toBoolean(properties.get("showCurrent"), true);
        int currentLevel = currentPage.getDepth() - 1;
        int endLevel = resolvedShowCurrent ? currentLevel : currentLevel - 1;

        for (int level = resolvedStartLevel; level <= endLevel; level++) {
            Page page = currentPage.getAbsoluteParent(level);
            if (page != null && !page.isHideInNav()) {
                breadcrumbItems.add(new Item(page, level == endLevel));
            }
        }
        items = breadcrumbItems;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    private int toInt(Object value, int defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = value != null ? value.toString() : "";
        String digits = StringUtils.defaultString(text).replaceAll("[^0-9-]", "");
        if (StringUtils.isBlank(digits)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean toBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String text = value != null ? value.toString() : "";
        if (StringUtils.isBlank(text)) {
            return defaultValue;
        }
        return text.toLowerCase().contains("true");
    }

    public static class Item {
        private final String title;
        private final String href;
        private final boolean current;

        Item(Page page, boolean current) {
            this.title = firstNotBlank(page.getNavigationTitle(), page.getPageTitle(), page.getTitle(), page.getName());
            this.href = page.getPath() + ".html";
            this.current = current;
        }

        private String firstNotBlank(String... values) {
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    return value.trim();
                }
            }
            return "";
        }

        public String getTitle() {
            return title;
        }

        public String getHref() {
            return href;
        }

        public boolean isCurrent() {
            return current;
        }
    }
}
