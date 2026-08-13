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
public class CustomFooterModel {

    private static final String DEFAULT_LOGO_PATH =
        "https://assets.micron.com/adobe/assets/urn:aaid:aem:652506f6-3869-4e9b-a1bd-d5b4209f850c/as/Micron-logo-white.svg";
    private static final String DEFAULT_COPYRIGHT =
        "©2023 Micron Technology, Inc. All rights reserved.";
    private static final String DEFAULT_DISCLAIMER =
        "Information, products, and/or specifications are subject to change without notice. All information is provided on an \"AS IS\" basis without warranties of any kind. Micron, the Micron logo, and all other Micron trademarks are the property of Micron Technology, Inc.";

    @ValueMapValue
    private String logoFileReference;

    @ValueMapValue
    private String logoPath;

    @ValueMapValue
    private String logoAlt;

    @ValueMapValue
    private String logoLink;

    @ValueMapValue
    private String ctaText;

    @ValueMapValue
    private String ctaLink;

    @ValueMapValue
    private String copyright;

    @ValueMapValue
    private String copyrightText;

    @ValueMapValue
    private String disclaimerText;

    @ChildResource
    private Resource sections;

    @ChildResource
    private Resource primaryLinks;

    @ChildResource
    private Resource socialLinks;

    @ChildResource
    private Resource legalLinks;

    @SlingObject(injectionStrategy = InjectionStrategy.OPTIONAL)
    private ResourceResolver resourceResolver;

    private List<LinkItem> primaryItems = Collections.emptyList();
    private List<LinkItem> socialItems = Collections.emptyList();
    private List<LinkItem> legalItems = Collections.emptyList();
    private List<FooterSection> footerSections = Collections.emptyList();

    @PostConstruct
    protected void init() {
        primaryItems = readLinks(primaryLinks);
        socialItems = readLinks(socialLinks);
        legalItems = readLinks(legalLinks);
        footerSections = readSections(sections);
        if (footerSections.isEmpty()) {
            footerSections = defaultSections();
        }
    }

    public String getLogoPath() {
        return StringUtils.defaultIfBlank(
            StringUtils.defaultIfBlank(logoFileReference, logoPath),
            DEFAULT_LOGO_PATH
        );
    }

    public String getLogoAlt() {
        return StringUtils.defaultIfBlank(logoAlt, "Micron");
    }

    public String getLogoLink() {
        return StringUtils.defaultIfBlank(formatLink(logoLink), "/");
    }

    public String getContactLabel() {
        return StringUtils.defaultIfBlank(ctaText, "Contact us");
    }

    public String getCtaLink() {
        return StringUtils.defaultIfBlank(formatLink(ctaLink), "https://in.micron.com/about/contact-us");
    }

    public String getCopyrightText() {
        return StringUtils.defaultIfBlank(
            StringUtils.defaultIfBlank(copyright, copyrightText),
            DEFAULT_COPYRIGHT
        );
    }

    public String getDisclaimerText() {
        return StringUtils.defaultIfBlank(disclaimerText, DEFAULT_DISCLAIMER);
    }

    public List<FooterSection> getSections() {
        return footerSections;
    }

    public List<LinkItem> getPrimaryItems() {
        return primaryItems;
    }

    public List<LinkItem> getSocialItems() {
        return socialItems;
    }

    public List<LinkItem> getLegalItems() {
        return legalItems;
    }

    private List<FooterSection> readSections(Resource parent) {
        if (parent == null) {
            return Collections.emptyList();
        }

        List<FooterSection> items = new ArrayList<>();
        for (Resource child : parent.getChildren()) {
            ValueMap properties = child.getValueMap();
            String title = properties.get("title", String.class);
            if (StringUtils.isBlank(title)) {
                continue;
            }
            items.add(new FooterSection(
                title,
                formatLink(properties.get("titleUrl", String.class)),
                readLinks(child.getChild("links"))
            ));
        }
        return Collections.unmodifiableList(items);
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
                formatLink(properties.get("link", String.class)),
                Boolean.TRUE.equals(properties.get("openInNewTab", Boolean.class)),
                StringUtils.defaultIfBlank(
                    properties.get("icon", String.class),
                    properties.get("iconPath", String.class)
                )
            ));
        }
        return Collections.unmodifiableList(items);
    }

    private String formatLink(String link) {
        return LinkUtils.getFormattedLink(link, resourceResolver);
    }

    private List<FooterSection> defaultSections() {
        List<FooterSection> defaults = new ArrayList<>();
        defaults.add(new FooterSection("Products", "https://in.micron.com/products", defaultLinks(new String[][] {
            {"Memory", "https://in.micron.com/products/memory"},
            {"Storage", "https://in.micron.com/products/storage"},
            {"Multichip packages", "https://in.micron.com/products/multichip-packages"},
            {"Technology leadership", "https://in.micron.com/products/technology-leadership"},
            {"Design tools", "https://in.micron.com/support/tools-and-utilities"}
        })));
        defaults.add(new FooterSection("Markets & industries", "https://in.micron.com/solutions", defaultLinks(new String[][] {
            {"AI", "https://in.micron.com/solutions/artificial-intelligence"},
            {"Automotive", "https://in.micron.com/solutions/automotive"},
            {"Client PC", "https://in.micron.com/solutions/client"},
            {"Consumer", "https://in.micron.com/solutions/consumer"},
            {"Data center and servers", "https://in.micron.com/solutions/data-center"},
            {"Industrial IoT", "https://in.micron.com/solutions/industrial"}
        })));
        defaults.add(new FooterSection("Partners", "https://in.micron.com/partners", defaultLinks(new String[][] {
            {"Partners overview", "https://in.micron.com/partners"},
            {"Partner networks", "https://in.micron.com/partners/partner-networks"},
            {"Become a partner", "https://in.micron.com/partners/become-a-partner"},
            {"Sales network", "https://in.micron.com/sales-support/sales-network"},
            {"Suppliers", "https://in.micron.com/about/suppliers"}
        })));
        defaults.add(new FooterSection("Sales & Support", "https://in.micron.com/sales-support", defaultLinks(new String[][] {
            {"Sales and support overview", "https://in.micron.com/sales-support"},
            {"Customer support", "https://in.micron.com/sales-support/customer-support"},
            {"Sales", "https://in.micron.com/sales-support/sales"},
            {"Downloads and technical documentation", "https://in.micron.com/sales-support/downloads"}
        })));
        defaults.add(new FooterSection("About", "https://in.micron.com/about", defaultLinks(new String[][] {
            {"About overview", "https://in.micron.com/about"},
            {"Company", "https://in.micron.com/about/company"},
            {"Corporate governance", "https://in.micron.com/about/corporate-governance"},
            {"Micron blog", "https://in.micron.com/about/blogs"},
            {"Events", "https://in.micron.com/about/events"},
            {"Micron glossary", "https://in.micron.com/about/glossary"}
        })));
        defaults.add(new FooterSection("Investor Relations", "https://investors.micron.com/", defaultLinks(new String[][] {
            {"IR home", "https://investors.micron.com/"},
            {"Events and presentations", "https://investors.micron.com/events-and-presentations"},
            {"Latest news", "https://investors.micron.com/news-releases"},
            {"Quarterly financial results", "https://investors.micron.com/financial-information/quarterly-results"}
        })));
        return Collections.unmodifiableList(defaults);
    }

    private List<LinkItem> defaultLinks(String[][] values) {
        List<LinkItem> links = new ArrayList<>();
        for (String[] value : values) {
            links.add(new LinkItem(value[0], value[1], false, ""));
        }
        return Collections.unmodifiableList(links);
    }

    public static final class FooterSection {
        private final String title;
        private final String titleUrl;
        private final List<LinkItem> links;

        FooterSection(String title, String titleUrl, List<LinkItem> links) {
            this.title = title;
            this.titleUrl = StringUtils.defaultIfBlank(titleUrl, "#");
            this.links = links == null ? Collections.emptyList() : links;
        }

        public String getTitle() {
            return title;
        }

        public String getTitleUrl() {
            return titleUrl;
        }

        public List<LinkItem> getLinks() {
            return links;
        }
    }

    public static final class LinkItem {
        private final String label;
        private final String link;
        private final boolean openInNewTab;
        private final String icon;

        LinkItem(String label, String link, boolean openInNewTab, String icon) {
            this.label = label;
            this.link = StringUtils.defaultIfBlank(link, "#");
            this.openInNewTab = openInNewTab;
            this.icon = StringUtils.defaultIfBlank(icon, "");
        }

        public String getLabel() {
            return label;
        }

        public String getLink() {
            return link;
        }

        public boolean isOpenInNewTab() {
            return openInNewTab;
        }

        public String getIcon() {
            return icon;
        }
    }
}
