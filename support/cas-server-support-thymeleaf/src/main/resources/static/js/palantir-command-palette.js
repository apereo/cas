/**
 * The Palantir command palette: one search box, opened with Ctrl/⌘+K or "/", that reaches every page,
 * the sections inside a page, the registered applications, the common actions and the user lookup.
 *
 * It is a jQuery UI dialog holding a jQuery UI autocomplete; the entries are collected when the palette
 * opens, from what the dashboard shows at that moment, so pages and sections hidden because their
 * endpoints are off never show up.
 */
class PalantirCommandPalette {
    static MAX_RESULTS = 40;

    static CATEGORY_ORDER = ["Pages", "Actions"];

    /**
     * Whether a sidebar or tab element is hidden by processNavigationTabs or a disabled feature.
     */
    static isHidden(element) {
        const $element = $(element);
        return $element.hasClass("hide") || $element.hasClass("d-none") || $element.css("display") === "none";
    }

    /**
     * Whether what a tab or section declares it needs is available: <code>data-palantir-requires</code> lists
     * endpoints, by the name of their <code>CasActuatorEndpoints</code> accessor, and features, as
     * <code>feature:Name</code>, all of which must be present.
     *
     * @param element the tab or section
     * @returns {boolean|null} whether it is available, or null when it declares nothing
     */
    static requirementsMet(element) {
        const requires = String($(element).data("palantir-requires") ?? "").trim();
        if (requires === "") {
            return null;
        }
        return requires.split(/\s+/).every(requirement => requirement.startsWith("feature:")
            ? CAS_FEATURES.includes(requirement.substring("feature:".length))
            : typeof CasActuatorEndpoints[requirement] === "function" && Boolean(CasActuatorEndpoints[requirement]()));
    }

    /**
     * Whether a tab or section is left out of the search. A page shows some of its tabs only once it opens, after
     * checking what they need; until the page has been opened, such a tab is offered when what it declares it
     * needs is available.
     *
     * @param element the tab or section
     * @param pageIndex the page it belongs to
     * @returns {boolean} true to leave it out
     */
    static isUnavailable(element, pageIndex) {
        const requirements = PalantirCommandPalette.requirementsMet(element);
        if (requirements === false) {
            return true;
        }
        if (requirements === true && !isDashboardTabInitialized(pageIndex)) {
            return false;
        }
        return PalantirCommandPalette.isHidden(element);
    }

    static navigationItems() {
        return $("nav.sidebar-navigation ul li[data-tab-index]")
            .toArray()
            .filter(li => !PalantirCommandPalette.isHidden(li))
            .map(li => {
                const $li = $(li);
                return {
                    index: Number($li.data("tab-index")),
                    element: li,
                    label: ($li.find(".sidebar-navigation-label").first().text()
                        || $li.find("button").attr("title") || "").trim(),
                    description: $li.find(".sidebar-navigation-description").first().text().trim(),
                    group: $li.closest(".palantir-nav-group").find(".palantir-nav-group-name").first().text().trim(),
                    icon: ($li.find(".mdi").first().attr("class") || "")
                        .split(/\s+/).find(name => name.startsWith("mdi-")) || "mdi-chevron-right"
                };
            })
            .filter(item => item.label && item.index < 100);
    }

    static async goTo(index) {
        $(`nav.sidebar-navigation ul li[data-tab-index=${index}]`).trigger("click");
        await initializeDashboardTab(index);
    }

    static pageEntries(navigation) {
        return navigation.map(item => ({
            category: "Pages",
            label: item.label,
            detail: [item.group, item.description].filter(Boolean).join(" · "),
            icon: item.icon,
            keywords: `${item.group} ${item.description}`,
            run: () => PalantirCommandPalette.goTo(item.index)
        }));
    }

    /**
     * The tabs that sit directly in a container: tab widgets inside it that are not themselves inside another tab
     * widget of the same container.
     *
     * @param container the page panel or a tab panel
     * @returns {jQuery} the tab widgets
     */
    static tabsIn(container) {
        return $(container).find(".jqueryui-tabs").filter((index, tabs) => $(tabs).parentsUntil(container, ".jqueryui-tabs").length === 0);
    }

    /**
     * The sections of every page, found from the page's markup rather than listed by hand: each tab of a tab
     * widget, at any depth, is a section reached by opening the page and every tab on the way to it, and any
     * element marked with <code>data-palantir-search-section</code> is a section reached the same way and scrolled
     * into view. A tab or section is left out while it is hidden, and so is everything under a tab or element
     * marked <code>data-palantir-search="false"</code> or kept in a dialog; <code>data-palantir-search-label</code>
     * renames an entry and
     * <code>data-palantir-search-keywords</code> adds words it is found by. A tab its page reveals only when it opens
     * declares what it needs with <code>data-palantir-requires</code>, so it can be found before the page is opened.
     *
     * @param navigation the pages
     * @returns {Array<object>} the section entries
     */
    static sectionEntries(navigation) {
        const entries = [];
        const excluded = element => $(element).closest("[data-palantir-search=false], .ui-dialog-content, .mdc-dialog, .modal, [role=dialog], [role=alertdialog], .hide").length > 0;
        const nameOf = (element, fallback) => String($(element).data("palantir-search-label") ?? fallback ?? "").replace(/\s+/g, " ").trim();
        const keywordsOf = element => String($(element).data("palantir-search-keywords") ?? "");
        const add = (item, trail, path, target, keywords) => entries.push({
            category: "Sections",
            label: [item.label, ...trail].join(" › "),
            detail: item.group,
            icon: item.icon,
            keywords: `${trail.join(" ")} ${keywords}`,
            run: () => PalantirCommandPalette.openSection(item.index, path, target)
        });
        const walk = (item, container, trail, path) => {
            $(container).find("[data-palantir-search-section]")
                .filter((index, element) => $(element).parentsUntil(container, ".jqueryui-tabs").length === 0)
                .each((index, element) => {
                    const label = nameOf(element, $(element).data("palantir-search-section"));
                    if (label && !excluded(element) && !PalantirCommandPalette.isUnavailable(element, item.index)) {
                        add(item, [...trail, label], path, element, keywordsOf(element));
                    }
                });
            PalantirCommandPalette.tabsIn(container).each((index, tabs) => {
                if (excluded(tabs) || PalantirCommandPalette.isHidden(tabs)) {
                    return;
                }
                $(tabs).find("> ul > li").each((position, tab) => {
                    const anchor = $(tab).find("a").first();
                    const label = nameOf(tab, nameOf(anchor, anchor.text()));
                    if (!label || excluded(tab) || PalantirCommandPalette.isUnavailable(tab, item.index) || PalantirCommandPalette.isHidden(anchor)) {
                        return;
                    }
                    const step = {tabs: tabs, tab: tab};
                    add(item, [...trail, label], [...path, step], null, `${keywordsOf(tab)} ${keywordsOf(anchor)}`);
                    const href = String(anchor.attr("href") ?? "");
                    const panel = href.startsWith("#") ? document.getElementById(href.slice(1)) : null;
                    if (panel && tabs.contains(panel)) {
                        walk(item, panel, [...trail, label], [...path, step]);
                    }
                });
            });
        };
        for (const item of navigation) {
            const panel = document.getElementById(`attribute-tab-${item.index}`);
            if (panel && !excluded(panel)) {
                walk(item, panel, [], []);
            }
        }
        return entries;
    }

    /**
     * Open a page, then each tab on the way to a section, waiting for tab widgets the page builds when it opens,
     * and bring the section into view.
     *
     * @param index the page index
     * @param path the tab widgets and the tab to choose in each, outermost first
     * @param target an element to scroll to once its tabs are open, or null to show the last tab
     */
    static async openSection(index, path, target) {
        await PalantirCommandPalette.goTo(index);
        for (const step of path) {
            for (let attempt = 0; attempt < 40 && !$(step.tabs).data("ui-tabs"); attempt++) {
                await new Promise(resolve => setTimeout(resolve, 50));
            }
            if (!$(step.tabs).data("ui-tabs")) {
                return;
            }
            const position = $(step.tabs).find("> ul > li").index(step.tab);
            if (position >= 0) {
                $(step.tabs).tabs("option", "active", position);
            }
        }
        const destination = target ?? path.at(-1)?.tab ?? null;
        if (destination) {
            destination.scrollIntoView({block: target ? "start" : "nearest", behavior: "smooth"});
        }
    }

    static applicationEntries(navigation) {
        if (!navigation.some(item => item.index === Tabs.APPLICATIONS.index)
            || typeof registeredServicesById === "undefined") {
            return [];
        }
        return [...registeredServicesById.values()].map(service => {
            const name = service.name ?? String(service.id);
            return {
                category: "Applications",
                label: name,
                detail: [`#${service.id}`, service.serviceId].filter(Boolean).join(" · "),
                icon: "mdi-application-outline",
                keywords: [service.id, service.serviceId, service.clientId, describePalantirDescription(service.description)]
                    .filter(value => value !== undefined && value !== null).join(" "),
                run: () => PalantirCommandPalette.showApplication(name)
            };
        });
    }

    static async showApplication(name) {
        await PalantirCommandPalette.goTo(Tabs.APPLICATIONS.index);
        const tabs = $("#applications-tabs");
        if (tabs.data("ui-tabs")) {
            tabs.tabs("option", "active", 0);
        }
        if ($.fn.dataTable.isDataTable("#applicationsTable")) {
            $("#applicationsTable").DataTable().search(name).draw();
            $("#applications-tab .dt-search input").val(name);
        }
    }

    static actionEntries(navigation) {
        const visible = index => navigation.some(item => item.index === index);
        const actions = [];
        if (visible(Tabs.APPLICATIONS.index) && typeof openRegisteredServiceWizardDialog === "function") {
            actions.push({
                label: "New application",
                detail: "Open the application wizard",
                icon: "mdi-plus-circle-outline",
                keywords: "add create register service wizard",
                run: async () => {
                    await PalantirCommandPalette.goTo(Tabs.APPLICATIONS.index);
                    openRegisteredServiceWizardDialog();
                }
            });
        }
        if (typeof PalantirTheme !== "undefined") {
            const dark = PalantirTheme.current() === "dark";
            actions.push({
                label: dark ? "Switch to light theme" : "Switch to dark theme",
                detail: "Theme",
                icon: dark ? "mdi-white-balance-sunny" : "mdi-weather-night",
                keywords: "theme appearance dark light mode",
                run: () => PalantirTheme.toggle()
            });
        }
        if (typeof PalantirZenMode !== "undefined") {
            const zen = PalantirZenMode.enabled();
            actions.push({
                label: zen ? "Leave zen mode" : "Zen mode",
                detail: zen ? "Show the navigation bar and footer again" : "Hide the navigation bar and footer (Shift+Z)",
                icon: zen ? "mdi-arrow-expand-vertical" : "mdi-arrow-collapse-vertical",
                keywords: "zen focus distraction fullscreen hide navigation header footer",
                run: () => PalantirZenMode.toggle()
            });
        }
        actions.push({
            label: "Keyboard shortcuts",
            detail: "Every shortcut, also shown with ?",
            icon: "mdi-keyboard-outline",
            keywords: "keys hotkeys help accessibility",
            run: () => openPalantirShortcuts()
        });
        const settings = $("#palantirSettingsTabButton");
        if (settings.length > 0 && !PalantirCommandPalette.isHidden(settings)) {
            actions.push({
                label: "Settings",
                detail: "Palantir preferences",
                icon: "mdi-cog-outline",
                keywords: "preferences options polling",
                run: () => settings.trigger("click")
            });
        }
        const logout = $("#palantirLogoutButton");
        if (logout.length > 0) {
            actions.push({
                label: "Sign out",
                detail: "End this Palantir session",
                icon: "mdi-logout-variant",
                keywords: "logout log out exit",
                run: () => logout.trigger("click")
            });
        }
        return actions.map(action => ({...action, category: "Actions"}));
    }

    static entries() {
        const navigation = PalantirCommandPalette.navigationItems();
        return [
            ...PalantirCommandPalette.pageEntries(navigation),
            ...PalantirCommandPalette.sectionEntries(navigation),
            ...PalantirCommandPalette.actionEntries(navigation),
            ...PalantirCommandPalette.applicationEntries(navigation)
        ];
    }

    /**
     * Rank an entry against the query: the label beats the keywords, and a match at the start of a word
     * beats one inside it. Zero means no match.
     */
    static score(entry, terms) {
        const label = entry.label.toLowerCase();
        const text = `${label} ${entry.detail ?? ""} ${entry.keywords ?? ""}`.toLowerCase();
        let score = 0;
        for (const term of terms) {
            if (label.startsWith(term)) {
                score += 8;
            } else if (new RegExp(`(^|[\\s›·#/._-])${PalantirCommandPalette.quote(term)}`).test(label)) {
                score += 5;
            } else if (label.includes(term)) {
                score += 3;
            } else if (text.includes(term)) {
                score += 1;
            } else {
                return 0;
            }
        }
        return score;
    }

    static quote(text) {
        return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    }

    static search(query) {
        const trimmed = query.trim();
        const terms = trimmed.toLowerCase().split(/\s+/).filter(Boolean);
        const all = PalantirCommandPalette.entries();
        let results;
        if (terms.length === 0) {
            results = all.filter(entry => entry.category === "Pages" || entry.category === "Actions");
        } else {
            results = all
                .map(entry => ({entry, score: PalantirCommandPalette.score(entry, terms)}))
                .filter(result => result.score > 0)
                .sort((first, second) => second.score - first.score)
                .map(result => result.entry);
        }
        results = results.slice(0, PalantirCommandPalette.MAX_RESULTS);
        const categories = terms.length === 0
            ? PalantirCommandPalette.CATEGORY_ORDER
            : [...new Set(results.map(entry => entry.category))];
        const grouped = categories.flatMap(category => results.filter(entry => entry.category === category));
        if (trimmed && !/\s/.test(trimmed) && PalantirUserLookup.availableSources().length > 0) {
            grouped.push({
                category: "Users",
                label: `Look up user ${trimmed}`,
                detail: "Sessions, devices, consent and activity",
                icon: "mdi-account-search",
                run: () => lookUpPalantirUser(trimmed)
            });
        }
        return grouped.map(entry => ({...entry, value: entry.label}));
    }

    /**
     * Define, once, an autocomplete whose menu stays inside the palette, with a heading per category and an
     * icon and a detail line per entry. It is defined on first use so jQuery UI is certainly loaded.
     */
    static defineWidget() {
        if ($.palantir?.paletteautocomplete) {
            return;
        }
        $.widget("palantir.paletteautocomplete", $.ui.autocomplete, {
            _create: function () {
                this._super();
                this.widget().menu("option", "items", "> :not(.palantir-command-palette__category)");
            },
            _renderMenu: function (ul, items) {
                let category = "";
                ul.addClass("palantir-command-palette__menu").attr("aria-label", "Search results");
                for (const item of items) {
                    if (item.category !== category) {
                        category = item.category;
                        $("<li>", {"class": "palantir-command-palette__category", "aria-hidden": "true"})
                            .text(category)
                            .appendTo(ul);
                    }
                    this._renderItemData(ul, item);
                }
            },
            _renderItem: function (ul, item) {
                const wrapper = $("<div>", {"class": "palantir-command-palette__item"});
                $("<i>", {"class": `mdi ${item.icon}`, "aria-hidden": "true"}).appendTo(wrapper);
                const text = $("<span>", {"class": "palantir-command-palette__text"}).appendTo(wrapper);
                $("<span>", {"class": "palantir-command-palette__label"}).text(item.label).appendTo(text);
                if (item.detail) {
                    $("<span>", {"class": "palantir-command-palette__detail"}).text(item.detail).appendTo(text);
                }
                return $("<li>").append(wrapper).appendTo(ul);
            },
            _resizeMenu: function () {
                this.menu.element.css("width", "");
            }
        });
    }

    static dialog() {
        let dialog = $("#palantirCommandPalette");
        if (dialog.length > 0) {
            return dialog;
        }
        PalantirCommandPalette.defineWidget();
        dialog = $(`
            <div id="palantirCommandPalette" class="palantir-command-palette" title="Search Palantir">
                <label class="palantir-command-palette__search">
                    <i class="mdi mdi-magnify" aria-hidden="true"></i>
                    <input id="palantirCommandPaletteInput" type="search" autocomplete="off" spellcheck="false"
                           aria-label="Search pages, sections, applications and actions"
                           placeholder="Pages, sections, applications, actions or a username"/>
                </label>
                <div id="palantirCommandPaletteResults" class="palantir-command-palette__results"></div>
                <p class="palantir-command-palette__hint" aria-hidden="true">
                    <kbd>↑</kbd><kbd>↓</kbd> to move · <kbd>Enter</kbd> to open · <kbd>Esc</kbd> to close
                </p>
            </div>`).appendTo("body");

        dialog.dialog({
            autoOpen: false,
            modal: true,
            width: Math.min(640, window.innerWidth - 32),
            classes: {"ui-dialog": "palantir-command-palette-dialog"},
            position: {my: "center top", at: "center top+80", of: window},
            open: () => {
                const input = $("#palantirCommandPaletteInput");
                input.val("").trigger("focus");
                input.paletteautocomplete("search", "");
            },
            close: () => $("#palantirCommandPaletteInput").paletteautocomplete("close")
        });

        $("#palantirCommandPaletteInput").paletteautocomplete({
            appendTo: "#palantirCommandPaletteResults",
            minLength: 0,
            delay: 0,
            autoFocus: true,
            source: (request, response) => response(PalantirCommandPalette.search(request.term)),
            select: (event, ui) => {
                event.preventDefault();
                dialog.dialog("close");
                Promise.resolve()
                    .then(() => ui.item.run())
                    .catch(error => console.error("Palantir could not open the selection", error));
            },
            focus: event => event.preventDefault(),
            response: (event, ui) => {
                $("#palantirCommandPaletteResults").attr("data-empty", String(ui.content.length === 0));
            }
        });
        $("#palantirCommandPaletteInput").on("keydown", event => {
            if (event.key === "Escape") {
                event.preventDefault();
                dialog.dialog("close");
            }
        });
        return dialog;
    }

    static open() {
        const dialog = PalantirCommandPalette.dialog();
        if (!dialog.dialog("isOpen")) {
            dialog.dialog("option", "width", Math.min(640, window.innerWidth - 32));
            dialog.dialog("open");
        }
    }
}

function openPalantirCommandPalette() {
    PalantirCommandPalette.open();
}

/**
 * Wire the sidebar search button and the keys that open the palette and the shortcut overlay. Ctrl/⌘+K works in
 * form fields too. "/" and "?" are read from the character the key produces rather than from the key itself, so
 * they work on every keyboard layout and on the numeric keypad, and they still type into a form field.
 */
function initializeCommandPalette() {
    $("#palantirCommandPaletteButton").off("click.palette").on("click.palette", () => openPalantirCommandPalette());
    const mac = /Mac|iPhone|iPad/.test(navigator.platform || navigator.userAgent);
    $("#palantirCommandPaletteButton .palantir-nav-search-key").text(mac ? "⌘K" : "Ctrl K");
    $("#palantirCommandPaletteButton").attr("title", `Search Palantir (${mac ? "⌘K" : "Ctrl+K"})`);
    $(document).off("keydown.palette").on("keydown.palette", event => {
        if ((event.ctrlKey || event.metaKey) && !event.altKey && !event.shiftKey && event.key?.toLowerCase() === "k") {
            event.preventDefault();
            openPalantirCommandPalette();
            return;
        }
        const target = event.target;
        const typing = target instanceof HTMLElement
            && (target.isContentEditable || ["INPUT", "TEXTAREA", "SELECT"].includes(target.tagName));
        if (typing || event.ctrlKey || event.metaKey || event.altKey) {
            return;
        }
        if (event.key === "/") {
            event.preventDefault();
            openPalantirCommandPalette();
        } else if (event.key === "?") {
            event.preventDefault();
            openPalantirShortcuts();
        }
    });
}
