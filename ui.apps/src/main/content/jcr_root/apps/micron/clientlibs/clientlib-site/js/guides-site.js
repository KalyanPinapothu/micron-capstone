(function () {
    "use strict";

    var actionIcons = {
        download: "&#x21E9;",
        print: "&#x2399;",
        notify: "&#x1F514;"
    };
    var maxTopicRequests = 6;
    var topicRequestTimeout = 45000;

    function closestItem(element) {
        while (element && !element.hasAttribute("data-guides-side-nav-item")) {
            element = element.parentElement;
        }
        return element;
    }

    function setExpanded(button, expanded) {
        var list = button.parentElement.nextElementSibling;
        button.setAttribute("aria-expanded", expanded ? "true" : "false");
        if (list) {
            list.hidden = !expanded;
        }
    }

    function bindPanelToggle(button, layout, sidePanel) {
        if (!button || button.dataset.guidesBound) {
            return;
        }
        button.dataset.guidesBound = "true";
        button.addEventListener("click", function () {
            var collapsed = layout.classList.toggle("guides-layout--panel-collapsed");
            sidePanel.hidden = collapsed;
            button.setAttribute("aria-expanded", collapsed ? "false" : "true");
            button.querySelector("span:last-child").textContent = collapsed ? "Show Side Panel" : "Hide Side Panel";
            button.querySelector("span:first-child").innerHTML = collapsed ? "&rsaquo;" : "&lsaquo;";
        });
    }

    function createPanelToggle(layout, sidePanel) {
        var button = document.createElement("button");
        button.className = "guides-layout__panel-toggle";
        button.type = "button";
        button.setAttribute("aria-expanded", "true");
        button.innerHTML = "<span aria-hidden=\"true\">&lsaquo;</span><span>Hide Side Panel</span>";
        bindPanelToggle(button, layout, sidePanel);
        return button;
    }

    function createActionButton(action, label) {
        var button = document.createElement("button");
        button.className = "guides-document-actions__button";
        button.type = "button";
        button.dataset.guidesAction = action;
        button.setAttribute("aria-label", label);
        button.title = label;
        button.innerHTML = "<span aria-hidden=\"true\">" + actionIcons[action] + "</span>";
        return button;
    }

    function createActions() {
        var actions = document.createElement("div");
        actions.className = "guides-document-actions";
        actions.setAttribute("aria-label", "Document actions");
        actions.appendChild(createActionButton("download", "Download"));
        actions.appendChild(createActionButton("print", "Print"));
        actions.appendChild(createActionButton("notify", "Notify"));
        bindActions(actions);

        return actions;
    }

    function bindActions(actions) {
        if (!actions || actions.dataset.guidesBound) {
            return;
        }
        actions.dataset.guidesBound = "true";
        actions.addEventListener("click", function (event) {
            var button = event.target.closest("[data-guides-action]");
            if (!button) {
                return;
            }

            if (button.dataset.guidesAction === "print") {
                window.print();
            }
            if (button.dataset.guidesAction === "download") {
                window.location.href = window.location.href.replace(/#.*$/, "") + ".pdf";
            }
            if (button.dataset.guidesAction === "notify") {
                window.alert("Notifications are not configured for this document yet.");
            }
        });
    }

    function ensureReaderShell() {
        var layout = document.getElementById("topic-page");
        var content = document.getElementById("topic-container");
        var existingPanel = document.getElementById("table-of-contents");
        var sidePanelColumn = existingPanel ? existingPanel.closest(".aem-GridColumn, .container.responsivegrid") : null;
        var contentColumn = content ? content.closest(".aem-GridColumn, .container.responsivegrid") : null;

        if (!layout) {
            layout = sidePanelColumn ? sidePanelColumn.parentElement : null;
            if (!layout) {
                var root = document.querySelector("main .cmp-container, .root.container, .cmp-container");
                if (!root) {
                    return;
                }
                layout = root;
            }
            layout.id = "topic-page";
        }

        if (existingPanel && existingPanel.parentElement !== layout) {
            layout.insertBefore(existingPanel, sidePanelColumn || layout.firstChild);
        }

        if (!content) {
            content = layout.querySelector(":scope > #topic-container, :scope > .guides-layout__content");
            if (!content && existingPanel) {
                content = document.createElement("div");
                content.className = "container responsivegrid";
                var anchor = existingPanel;
                layout.insertBefore(content, anchor.nextSibling);
            }
            if (!content) {
                return;
            }
            content.id = "topic-container";
        }

        if (content.parentElement !== layout) {
            layout.insertBefore(content, existingPanel ? existingPanel.nextSibling : layout.firstChild);
        } else if (existingPanel && content.previousElementSibling !== existingPanel) {
            layout.insertBefore(content, existingPanel.nextSibling);
        }

        if (sidePanelColumn || contentColumn) {
            Array.prototype.slice.call(layout.children).forEach(function (child) {
                if (child !== existingPanel && child !== content) {
                    child.classList.add("guides-layout__legacy-hidden");
                }
            });
        }

        if (!existingPanel) {
            var sidePanel = document.createElement("aside");
            sidePanel.id = "table-of-contents";
            sidePanel.className = "guides-layout__side-panel";
            sidePanel.appendChild(createFallbackNavigation(content));
            layout.insertBefore(sidePanel, content);
        } else {
            if (!existingPanel.querySelector("[data-guides-side-nav]")) {
                existingPanel.appendChild(createFallbackNavigation(content));
            }
        }
    }

    function createFallbackNavigation(content) {
        var nav = document.createElement("nav");
        nav.className = "guides-side-nav";
        nav.dataset.guidesSideNav = "";
        nav.setAttribute("aria-label", "Topic navigation");
        nav.innerHTML = "<div class=\"guides-side-nav__search\"><input class=\"guides-side-nav__search-input\" data-guides-side-nav-search type=\"search\" placeholder=\"Search by keyword\" aria-label=\"Search by keyword\"><span class=\"guides-side-nav__search-icon\" aria-hidden=\"true\"></span></div><div class=\"guides-side-nav__tabs\" aria-label=\"Navigation views\"><button class=\"guides-side-nav__tab guides-side-nav__tab--active\" type=\"button\" data-guides-side-nav-tab=\"content\" aria-selected=\"true\">Content</button><button class=\"guides-side-nav__tab\" type=\"button\" data-guides-side-nav-tab=\"links\" aria-selected=\"false\">Links</button></div>";

        var list = document.createElement("ol");
        list.className = "guides-side-nav__list guides-side-nav__list--root";
        list.dataset.guidesSideNavPanel = "content";
        var headings = content.querySelectorAll("h1, h2, h3");

        headings.forEach(function (heading, index) {
            if (!heading.id) {
                heading.id = "topic-heading-" + index;
            }
            var item = document.createElement("li");
            item.className = index === 0 ? "guides-side-nav__item guides-side-nav__item--active" : "guides-side-nav__item";
            item.dataset.guidesSideNavItem = "";
            item.innerHTML = "<div class=\"guides-side-nav__row\"><a class=\"guides-side-nav__link\" href=\"#" + heading.id + "\">" + heading.textContent + "</a></div>";
            list.appendChild(item);
        });

        if (!headings.length) {
            var item = document.createElement("li");
            item.className = "guides-side-nav__item guides-side-nav__item--active";
            item.dataset.guidesSideNavItem = "";
            item.innerHTML = "<div class=\"guides-side-nav__row\"><a class=\"guides-side-nav__link\" href=\"#topic-container\">Overview</a></div>";
            list.appendChild(item);
        }

        nav.appendChild(list);
        return nav;
    }

    function enhanceNavigation(nav) {
        if (nav.dataset.guidesNavigationBound) {
            return;
        }
        nav.dataset.guidesNavigationBound = "true";

        nav.addEventListener("click", function (event) {
            var tab = event.target.closest("[data-guides-side-nav-tab]");
            if (tab) {
                event.preventDefault();
                setActiveSideNavTab(nav, tab.dataset.guidesSideNavTab);
                return;
            }

            var toggle = event.target.closest(".guides-side-nav__toggle");
            if (!toggle) {
                return;
            }

            event.preventDefault();
            setExpanded(toggle, toggle.getAttribute("aria-expanded") !== "true");
        });

        var search = nav.querySelector("[data-guides-side-nav-search]");
        if (search) {
            search.addEventListener("input", function () {
                var query = search.value.trim().toLowerCase();
                var items = nav.querySelectorAll("[data-guides-side-nav-item]");

                items.forEach(function (item) {
                    var link = item.querySelector(".guides-side-nav__link");
                    var matches = !query || (link && link.textContent.toLowerCase().indexOf(query) !== -1);
                    var childMatches = Array.prototype.some.call(item.querySelectorAll("[data-guides-side-nav-item]"), function (child) {
                        var childLink = child.querySelector(".guides-side-nav__link");
                        return child !== item && childLink && childLink.textContent.toLowerCase().indexOf(query) !== -1;
                    });

                    item.hidden = !(matches || childMatches);

                    if (query && childMatches) {
                        var toggle = item.querySelector(".guides-side-nav__toggle");
                        if (toggle) {
                            setExpanded(toggle, true);
                        }
                    }
                });
            });
        }
    }

    function setActiveSideNavTab(nav, selectedName) {
        if (!nav.querySelector("[data-guides-side-nav-panel=\"" + selectedName + "\"]")) {
            return;
        }

        nav.querySelectorAll("[data-guides-side-nav-tab]").forEach(function (tab) {
            var active = tab.dataset.guidesSideNavTab === selectedName;
            tab.classList.toggle("guides-side-nav__tab--active", active);
            tab.setAttribute("aria-selected", active ? "true" : "false");
        });

        nav.querySelectorAll("[data-guides-side-nav-panel]").forEach(function (panel) {
            panel.hidden = panel.dataset.guidesSideNavPanel !== selectedName;
        });
    }

    function enhanceLayout(nav) {
        var sidePanel = nav.closest("#table-of-contents");
        var content = document.getElementById("topic-container");
        var layout = document.getElementById("topic-page");

        if (!sidePanel || !content || !layout) {
            return;
        }

        layout.classList.add("guides-layout");
        sidePanel.classList.add("guides-layout__side-panel");
        content.classList.add("guides-layout__content");
        var toolbar = content.querySelector(":scope > .guides-layout__toolbar");
        if (!toolbar) {
            toolbar = document.createElement("div");
            toolbar.className = "guides-layout__toolbar";
            toolbar.appendChild(createPanelToggle(layout, sidePanel));
            toolbar.appendChild(createActions());
            content.insertBefore(toolbar, content.firstChild);
        } else {
            bindPanelToggle(toolbar.querySelector(".guides-layout__panel-toggle"), layout, sidePanel);
            bindActions(toolbar.querySelector(".guides-document-actions"));
        }
        enhanceReaderAnchors(nav, content);
        hydrateClientSideReader(nav, content);
    }

    function maybeBuildHomeReaderNavigation(nav) {
        var layout = nav.closest("[data-guides-home-reader]");
        var list = nav.querySelector("[data-guides-home-toc]");

        if (!layout || !list || nav.querySelector(".guides-side-nav__link[href]")) {
            return null;
        }

        var pagePath = window.location.pathname.replace(/\.html$/, "");
        var jsonPath = pagePath + ".infinity.json";

        return fetch(jsonPath, { credentials: "same-origin" })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Unable to load page tree");
                }
                return response.json();
            })
            .then(function (data) {
                var items = pageChildren(data, pagePath);
                list.innerHTML = "";

                if (!items.length) {
                    list.appendChild(createStaticNavItem({
                        title: "No topics found",
                        path: pagePath,
                        children: []
                    }, false));
                    return;
                }

                items.forEach(function (item, index) {
                    list.appendChild(createStaticNavItem(item, index === 0));
                });
            })
            .catch(function () {
                list.innerHTML = "<li class=\"guides-side-nav__item\"><div class=\"guides-side-nav__row\"><span class=\"guides-side-nav__link\">Unable to load content</span></div></li>";
            });
    }

    function pageChildren(node, parentPath) {
        return Object.keys(node || {}).reduce(function (items, name) {
            var child = node[name];
            if (!child || child["jcr:primaryType"] !== "cq:Page") {
                return items;
            }

            var content = child["jcr:content"] || {};
            if (name.indexOf("__") === 0 || content.hideInNav === true || content.hideInNav === "true") {
                return items;
            }

            var path = parentPath + "/" + name;
            items.push({
                title: content.navTitle || content.pageTitle || content["jcr:title"] || name,
                path: path,
                children: pageChildren(child, path)
            });
            return items;
        }, []);
    }

    function createStaticNavItem(item, active) {
        var listItem = document.createElement("li");
        listItem.className = active ? "guides-side-nav__item guides-side-nav__item--active" : "guides-side-nav__item";
        listItem.dataset.guidesSideNavItem = "";

        var row = document.createElement("div");
        row.className = "guides-side-nav__row";

        var link = document.createElement("a");
        link.className = "guides-side-nav__link";
        link.href = "#" + anchorFromHref(item.path + ".html");
        link.dataset.guidesTopicUrl = item.path + ".html";
        link.dataset.guidesReaderTopicLink = "";
        link.textContent = item.title;
        row.appendChild(link);

        if (item.children.length) {
            var button = document.createElement("button");
            button.className = "guides-side-nav__toggle";
            button.type = "button";
            button.setAttribute("aria-expanded", "true");
            button.setAttribute("aria-label", "Toggle " + item.title);
            button.innerHTML = "<span aria-hidden=\"true\">v</span>";
            row.appendChild(button);
        }

        listItem.appendChild(row);

        if (item.children.length) {
            var childList = document.createElement("ol");
            childList.className = "guides-side-nav__list";
            item.children.forEach(function (child) {
                childList.appendChild(createStaticNavItem(child, false));
            });
            listItem.appendChild(childList);
        }

        return listItem;
    }

    function enhanceReaderAnchors(nav, content) {
        if (!nav.closest("[data-guides-home-reader]") || nav.dataset.guidesAnchorScrollBound) {
            return;
        }
        nav.dataset.guidesAnchorScrollBound = "true";
        nav.addEventListener("click", function (event) {
            var link = event.target.closest(".guides-side-nav__link[href^='#']");
            if (!link) {
                return;
            }

            var id = decodeURIComponent(link.getAttribute("href").substring(1));
            var target = id ? content.querySelector("#" + cssEscape(id)) : null;
            event.preventDefault();
            if (!target) {
                return;
            }

            content.scrollTo({
                top: target.offsetTop - content.offsetTop - 52,
                behavior: "smooth"
            });
            setActiveNavItem(nav, link);
            history.replaceState(null, "", "#" + id);
        });
    }

    function setActiveNavItem(nav, activeLink) {
        nav.querySelectorAll(".guides-side-nav__item--active").forEach(function (item) {
            item.classList.remove("guides-side-nav__item--active");
        });
        var item = closestItem(activeLink);
        if (item) {
            item.classList.add("guides-side-nav__item--active");
        }
    }

    function hydrateClientSideReader(nav, content) {
        var existingReader = content.querySelector("[data-guides-map-reader]");
        if (existingReader) {
            rewriteNavLinksToAnchors(nav);
            buildHomeReaderLinks(nav, collectReaderLinks(existingReader, window.location.href));
            return;
        }

        var links = Array.prototype.slice.call(nav.querySelectorAll(".guides-side-nav__link[href]")).filter(function (link) {
            return link.dataset.guidesTopicUrl || link.getAttribute("href").charAt(0) !== "#";
        });

        if (!links.length) {
            return;
        }

        var reader = content.querySelector("[data-guides-client-reader]");
        if (!reader) {
            reader = document.createElement("div");
            reader.className = "guides-map-reader";
            reader.dataset.guidesClientReader = "";
            content.appendChild(reader);
        }
        reader.innerHTML = "";
        var status = document.createElement("p");
        status.className = "guides-map-reader__status";
        status.textContent = "Loading document...";
        reader.appendChild(status);

        var homeReader = !!nav.closest("[data-guides-home-reader]");
        var placeholders = links.map(function () {
            var placeholder = document.createElement("div");
            placeholder.className = "guides-map-reader__placeholder";
            reader.appendChild(placeholder);
            return placeholder;
        });

        rewriteNavLinksToAnchors(nav);
        loadTopicsInBatches(links, homeReader, function (result, loaded, total) {
            placeholders[result.index].parentElement.replaceChild(result.section, placeholders[result.index]);
            status.textContent = "Loading document... " + loaded + " of " + total;
        }).then(function (results) {
            var collectedLinks = [];
            results.forEach(function (result) {
                collectedLinks = collectedLinks.concat(result.links);
            });
            if (status.parentElement) {
                status.parentElement.removeChild(status);
            }
            buildHomeReaderLinks(nav, collectedLinks);
        });
    }

    function loadTopicsInBatches(links, homeReader, onTopicLoaded) {
        var total = links.length;
        var results = new Array(total);
        var nextIndex = 0;
        var loaded = 0;
        var workers = Math.min(maxTopicRequests, total);
        var tasks = [];

        function loadNext() {
            if (nextIndex >= total) {
                return Promise.resolve();
            }

            var index = nextIndex;
            nextIndex += 1;

            return loadTopic(links[index], homeReader)
            .then(function (result) {
                    result.index = index;
                    results[index] = result;
                    loaded += 1;
                    onTopicLoaded(result, loaded, total);
                })
                .then(loadNext);
        }

        while (workers > 0) {
            tasks.push(loadNext());
            workers -= 1;
        }

        return Promise.all(tasks).then(function () {
            return results;
        });
    }

    function loadTopic(link, homeReader) {
        var topicUrl = link.dataset.guidesTopicUrl || link.href;
        var fetchUrl = homeReader ? renderedTopicUrl(topicUrl) : topicUrl;

        return fetchTextWithTimeout(fetchUrl)
            .then(function (html) {
                return createClientTopicResult(link, topicUrl, html, fetchUrl);
            })
            .catch(function () {
                return {
                    section: createErrorSection(link, topicUrl),
                    links: []
                };
            });
    }

    function fetchTextWithTimeout(url) {
        var controller = window.AbortController ? new AbortController() : null;
        var timeoutId = controller ? window.setTimeout(function () {
            controller.abort();
        }, topicRequestTimeout) : null;

        return fetch(url, {
            credentials: "same-origin",
            signal: controller ? controller.signal : undefined
        }).then(function (response) {
            if (timeoutId) {
                window.clearTimeout(timeoutId);
            }
            if (!response.ok) {
                throw new Error("Unable to load " + url);
            }
            return response.text();
        }).catch(function (error) {
            if (timeoutId) {
                window.clearTimeout(timeoutId);
            }
            throw error;
        });
    }

    function renderedTopicUrl(topicUrl) {
        var parser = document.createElement("a");
        parser.href = topicUrl;
        parser.search = "";
        parser.hash = "";
        if (!/\.html$/.test(parser.pathname)) {
            parser.pathname = parser.pathname.replace(/\/$/, "") + ".html";
        }
        parser.search = "wcmmode=disabled";
        return parser.href;
    }

    function rewriteNavLinksToAnchors(nav) {
        nav.querySelectorAll(".guides-side-nav__link[href]").forEach(function (link) {
            if (!isReaderTopicLink(link)) {
                return;
            }
            if (link.dataset.guidesTopicUrl) {
                link.setAttribute("href", "#" + anchorFromHref(link.dataset.guidesTopicUrl));
            } else if (link.getAttribute("href").charAt(0) !== "#") {
                link.setAttribute("href", "#" + anchorFromHref(link.href));
            }
        });
    }

    function isReaderTopicLink(link) {
        return link.dataset.guidesReaderTopicLink !== undefined ||
            link.dataset.guidesTopicUrl ||
            link.closest("[data-guides-home-toc]") ||
            !link.closest("[data-guides-home-links]");
    }

    function buildHomeReaderLinks(nav, links) {
        var linksPanel = nav.querySelector("[data-guides-home-links]");
        if (!linksPanel || !nav.closest("[data-guides-home-reader]")) {
            return;
        }

        var grouped = groupReaderLinks(links || []);
        var total = grouped.external.length + grouped.internal.length;

        linksPanel.innerHTML = "";
        var count = document.createElement("p");
        count.className = "guides-side-nav__status";
        count.textContent = total + " " + (total === 1 ? "link" : "links");
        linksPanel.appendChild(count);

        var list = document.createElement("ol");
        list.className = "guides-side-nav__list guides-side-nav__list--root";
        list.appendChild(createLinksGroup("Internal Links", grouped.internal, "internal", true));
        list.appendChild(createLinksGroup("External Links", grouped.external, "external", true));
        linksPanel.appendChild(list);
    }

    function groupReaderLinks(links) {
        var grouped = {
            internal: [],
            external: []
        };
        var seen = {};

        links.forEach(function (link) {
            var href = link.href;
            var label = link.title;
            if (!href || !label) {
                return;
            }

            var key = href + "|" + label;
            if (seen[key]) {
                return;
            }
            seen[key] = true;

            var item = {
                title: label,
                href: href,
                external: isExternalUrl(href)
            };
            grouped[item.external ? "external" : "internal"].push(item);
        });

        return grouped;
    }

    function collectReaderLinks(source, baseUrl) {
        var links = [];

        Array.prototype.slice.call(source.querySelectorAll("a[href], [data-href], [data-link], [xlink\\:href]")).forEach(function (element) {
            if (element.closest("#table-of-contents, header, footer, nav, script, style, .guides-layout__toolbar")) {
                return;
            }

            var href = element.getAttribute("href") ||
                element.getAttribute("data-href") ||
                element.getAttribute("data-link") ||
                element.getAttribute("xlink:href");
            var label = linkLabel(element);
            if (!href || !label || isIgnoredCollectedLink(href)) {
                return;
            }

            links.push({
                title: label,
                href: absolutizeImportedUrl(href, baseUrl)
            });
        });

        return links;
    }

    function linkLabel(element) {
        return (element.textContent ||
            element.getAttribute("aria-label") ||
            element.getAttribute("title") ||
            element.getAttribute("href") ||
            "").trim();
    }

    function isIgnoredCollectedLink(href) {
        return /^(mailto:|tel:|javascript:)/i.test(href);
    }

    function createLinksGroup(title, links, type, expanded) {
        var group = document.createElement("li");
        group.className = "guides-side-nav__item guides-side-nav__item--group";
        group.dataset.guidesSideNavItem = "";

        var row = document.createElement("div");
        row.className = "guides-side-nav__row";

        var label = document.createElement("span");
        label.className = "guides-side-nav__link";
        label.textContent = title;
        row.appendChild(label);

        var button = document.createElement("button");
        button.className = "guides-side-nav__toggle";
        button.type = "button";
        button.setAttribute("aria-expanded", expanded ? "true" : "false");
        button.setAttribute("aria-label", "Toggle " + title);
        button.innerHTML = "<span aria-hidden=\"true\">v</span>";
        row.appendChild(button);
        group.appendChild(row);

        var childList = document.createElement("ol");
        childList.className = "guides-side-nav__list";
        childList.hidden = !expanded;

        if (links.length) {
            links.forEach(function (link) {
                childList.appendChild(createLinkNavItem(link, type));
            });
        } else {
            childList.appendChild(createEmptyLinkItem("No links found"));
        }

        group.appendChild(childList);
        return group;
    }

    function createLinkNavItem(item, type) {
        var listItem = document.createElement("li");
        listItem.className = "guides-side-nav__item guides-side-nav__item--link guides-side-nav__item--" + type;
        listItem.dataset.guidesSideNavItem = "";

        var row = document.createElement("div");
        row.className = "guides-side-nav__row";

        var link = document.createElement("a");
        link.className = "guides-side-nav__link guides-side-nav__link--" + type;
        link.href = item.href;
        link.textContent = item.title;
        if (item.external) {
            link.target = "_blank";
            link.rel = "noopener noreferrer";
        }
        row.appendChild(link);
        listItem.appendChild(row);
        return listItem;
    }

    function createEmptyLinkItem(message) {
        var listItem = document.createElement("li");
        listItem.className = "guides-side-nav__item guides-side-nav__item--empty";

        var row = document.createElement("div");
        row.className = "guides-side-nav__row";

        var text = document.createElement("span");
        text.className = "guides-side-nav__link";
        text.textContent = message;
        row.appendChild(text);
        listItem.appendChild(row);
        return listItem;
    }

    function isExternalUrl(url) {
        if (/^www\./i.test(url)) {
            return true;
        }

        try {
            return new URL(url, window.location.href).origin !== window.location.origin;
        } catch (error) {
            return false;
        }
    }

    function createClientTopicResult(link, topicUrl, html, fetchUrl) {
        var doc = new DOMParser().parseFromString(html, "text/html");
        var source = getClientContentSource(doc);

        return {
            section: createClientSectionFromSource(link, topicUrl, source, fetchUrl),
            links: collectReaderLinks(source, fetchUrl || topicUrl)
        };
    }

    function createClientSection(link, topicUrl, html, fetchUrl) {
        var doc = new DOMParser().parseFromString(html, "text/html");
        return createClientSectionFromSource(link, topicUrl, getClientContentSource(doc), fetchUrl);
    }

    function getClientContentSource(doc) {
        return doc.querySelector("#topic-source-content") ||
            doc.querySelector("#topic-container") ||
            doc.querySelector("main") ||
            doc.body;
    }

    function createClientSectionFromSource(link, topicUrl, source, fetchUrl) {
        var section = document.createElement("section");
        section.className = "guides-map-reader__section";
        section.id = anchorFromHref(topicUrl);

        appendClientContent(section, source, link, topicUrl, fetchUrl);
        if (!section.querySelector("h1, h2, h3, .cmp-title")) {
            section.insertBefore(createSectionHeading(link), section.firstChild);
        }

        return section;
    }

    function appendClientContent(section, source, link, topicUrl, fetchUrl) {
        var contentNodes = Array.prototype.slice.call(source.children).filter(function (child) {
            return isRenderableClientContent(child) && !isDuplicateTopicTitle(child, link, topicUrl);
        });
        if (!contentNodes.length) {
            contentNodes = Array.prototype.slice.call(source.querySelectorAll([
                ".cmp-title",
                ".cmp-text",
                ".cmp-image",
                ".cmp-dita-table",
                ".cmp-dita-figure",
                ".micron-guides-table",
                "figure"
            ].join(","))).filter(function (node) {
                return !node.closest("#table-of-contents, .guides-layout__toolbar, [data-guides-map-reader], [data-guides-client-reader]") &&
                    !isDuplicateTopicTitle(node, link, topicUrl);
            });
        }

        contentNodes.forEach(function (child) {
            var importedChild = document.importNode(child, true);
            hydrateImportedImages(importedChild, fetchUrl || topicUrl);
            section.appendChild(importedChild);
        });
    }

    function createSectionHeading(link) {
        var heading = document.createElement("h2");
        heading.className = "guides-map-reader__title";
        heading.textContent = link.textContent.trim();
        return heading;
    }

    function isDuplicateTopicTitle(node, link, topicUrl) {
        if (!node.matches(".cmp-title, .title, h1, h2, h3") && !node.querySelector(".cmp-title, .title, h1, h2, h3")) {
            return false;
        }

        var heading = node.matches(".cmp-title, .title, h1, h2, h3") ?
            node :
            node.querySelector(".cmp-title, .title, h1, h2, h3");
        var headingText = normalizeTitleText(heading.textContent);
        if (!headingText) {
            return false;
        }

        var navigationText = normalizeTitleText(link.textContent);
        var topicSlug = normalizeTitleText(anchorFromHref(topicUrl));
        var isNavigationTitle = headingText === navigationText || headingText === topicSlug;
        var hasFollowingContentTitle = hasLaterContentTitle(node);
        return isNavigationTitle && hasFollowingContentTitle;
    }

    function hasLaterContentTitle(node) {
        var next = node.nextElementSibling;
        while (next && !isRenderableClientContent(next)) {
            next = next.nextElementSibling;
        }
        while (next) {
            if (next.matches(".cmp-title, .title, h1, h2, h3") || next.querySelector(".cmp-title, .title, h1, h2, h3")) {
                return true;
            }
            next = next.nextElementSibling;
        }
        return false;
    }

    function normalizeTitleText(value) {
        return (value || "").toLowerCase().replace(/[^a-z0-9]+/g, "");
    }

    function hydrateImportedImages(root, baseUrl) {
        var images = root.matches && root.matches("img") ? [root] : [];
        images = images.concat(Array.prototype.slice.call(root.querySelectorAll ? root.querySelectorAll("img") : []));
        images.forEach(function (image) {
            [
                "src",
                "data-src",
                "data-cmp-src",
                "data-lazy-src"
            ].forEach(function (attribute) {
                var value = image.getAttribute(attribute);
                if (value) {
                    image.setAttribute(attribute, normalizeImportedImageUrl(value, baseUrl, image));
                }
            });
            ["srcset", "data-srcset", "data-cmp-srcset"].forEach(function (attribute) {
                var value = image.getAttribute(attribute);
                if (value) {
                    image.setAttribute(attribute, normalizeImportedSrcset(value, baseUrl, image));
                }
            });

            if (!image.getAttribute("src")) {
                var fallbackSource = image.getAttribute("data-cmp-src") ||
                    image.getAttribute("data-src") ||
                    image.getAttribute("data-lazy-src");
                if (fallbackSource) {
                    image.setAttribute("src", fallbackSource);
                }
            }
            image.removeAttribute("data-cmp-is");
            image.removeAttribute("data-cmp-lazy");
        });

        var sources = root.matches && root.matches("source") ? [root] : [];
        sources = sources.concat(Array.prototype.slice.call(root.querySelectorAll ? root.querySelectorAll("source") : []));
        sources.forEach(function (source) {
            ["srcset", "data-srcset"].forEach(function (attribute) {
                var value = source.getAttribute(attribute);
                if (value) {
                    source.setAttribute(attribute, normalizeImportedSrcset(value, baseUrl));
                }
            });
        });
    }

    function normalizeImportedImageUrl(value, baseUrl, image) {
        var concreteUrl = concreteCoreImageUrl(value, image)
            .replace(/\/jcr:content\/renditions\/original(?=($|[?#\s,]))/g, "");
        return absolutizeImportedUrl(concreteUrl, baseUrl);
    }

    function normalizeImportedSrcset(value, baseUrl, image) {
        return value.split(",").map(function (entry) {
            var parts = entry.trim().split(/\s+/);
            if (!parts[0]) {
                return "";
            }
            parts[0] = normalizeImportedImageUrl(parts[0], baseUrl, image);
            return parts.join(" ");
        }).filter(Boolean).join(", ");
    }

    function concreteCoreImageUrl(value, image) {
        if (value.indexOf("{.width}") === -1) {
            return value;
        }

        var width = preferredCoreImageWidth(image);
        return value.replace(/\{\.width\}/g, width ? "." + width : "");
    }

    function preferredCoreImageWidth(image) {
        if (!image) {
            return "";
        }

        var widths = image.getAttribute("data-cmp-widths");
        if (!widths) {
            return "";
        }

        try {
            widths = JSON.parse(widths);
        } catch (error) {
            widths = widths.replace(/[\[\]]/g, "").split(",");
        }

        widths = widths.map(function (width) {
            return parseInt(width, 10);
        }).filter(function (width) {
            return !isNaN(width) && width > 0;
        }).sort(function (left, right) {
            return left - right;
        });

        return widths.length ? widths[widths.length - 1] : "";
    }

    function absolutizeImportedUrl(value, baseUrl) {
        if (/^www\./i.test(value)) {
            return "https://" + value;
        }

        if (/^(https?:|data:|blob:|#|\/)/i.test(value)) {
            return value;
        }

        try {
            return new URL(value, baseUrl || window.location.href).href;
        } catch (error) {
            return value;
        }
    }

    function isRenderableClientContent(child) {
        return !child.matches(".guides-layout__toolbar, #table-of-contents, #topic-source-content, script, style") &&
            !child.hasAttribute("data-guides-map-reader") &&
            !child.hasAttribute("data-guides-client-reader");
    }

    function createJsonSection(link, topicUrl, json) {
        var section = document.createElement("section");
        section.className = "guides-map-reader__section";
        section.id = anchorFromHref(topicUrl);

        var heading = document.createElement("h2");
        heading.className = "guides-map-reader__title";
        heading.textContent = link.textContent.trim();
        section.appendChild(heading);

        var fragments = [];
        collectTextFragments(json, fragments);

        if (!fragments.length) {
            section.appendChild(document.createTextNode("No content found for this topic."));
            return section;
        }

        fragments.forEach(function (html) {
            var fragment = document.createElement("div");
            fragment.className = "guides-map-reader__fragment";
            fragment.innerHTML = html;
            section.appendChild(fragment);
        });

        return section;
    }

    function collectTextFragments(node, fragments) {
        if (!node || typeof node !== "object") {
            return;
        }

        if (typeof node.text === "string" && node.text.trim()) {
            fragments.push(node.text);
        }

        Object.keys(node).forEach(function (key) {
            var value = node[key];
            if (value && typeof value === "object") {
                collectTextFragments(value, fragments);
            }
        });
    }

    function createErrorSection(link, topicUrl) {
        var section = document.createElement("section");
        section.className = "guides-map-reader__section";
        section.id = anchorFromHref(topicUrl || link.dataset.guidesTopicUrl || link.href);
        section.innerHTML = "<h2 class=\"guides-map-reader__title\">" + link.textContent.trim() + "</h2><p>Unable to load this topic.</p>";
        return section;
    }

    function anchorFromHref(href) {
        var parser = document.createElement("a");
        parser.href = href;
        return parser.pathname.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "") || "topic";
    }

    function cssEscape(value) {
        if (window.CSS && typeof window.CSS.escape === "function") {
            return window.CSS.escape(value);
        }
        return value.replace(/([ #;?%&,.+*~':"!^$[\]()=>|/@])/g, "\\$1");
    }

    function isTemplateEditor() {
        var path = window.location.pathname;
        return path.indexOf("/editor.html/conf/") !== -1 ||
            (path.indexOf("/conf/") !== -1 && path.indexOf("/settings/wcm/templates/") !== -1);
    }

    function initGuidesReader() {
        if (isTemplateEditor()) {
            document.documentElement.classList.add("micron-template-editor");
            return;
        }

        hideUnresolvedCategoryPlaceholder();
        ensureReaderShell();
        document.querySelectorAll("[data-guides-side-nav]").forEach(function (nav) {
            enhanceNavigation(nav);
            var navigationReady = maybeBuildHomeReaderNavigation(nav);
            if (navigationReady) {
                navigationReady.then(function () {
                    enhanceLayout(nav);
                });
            } else {
                enhanceLayout(nav);
            }
        });
    }

    function hideUnresolvedCategoryPlaceholder() {
        document.querySelectorAll("p, div").forEach(function (element) {
            if (element.children.length || element.textContent.trim() !== "$category.html$") {
                return;
            }
            element.hidden = true;
            var wrapper = element.closest(".text, .cmp-text");
            if (wrapper) {
                wrapper.hidden = true;
            }
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initGuidesReader);
    } else {
        initGuidesReader();
    }
}());
