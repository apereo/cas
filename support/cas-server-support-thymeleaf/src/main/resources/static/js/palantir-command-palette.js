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

    static sectionEntries(navigation) {
        const entries = [];
        for (const item of navigation) {
            const panel = $(`#attribute-tab-${item.index}`);
            const outer = panel.find(".jqueryui-tabs").filter((index, tabs) => $(tabs).parentsUntil(panel, ".jqueryui-tabs").length === 0).first();
            if (outer.length === 0) {
                continue;
            }
            outer.find("> ul > li").each((position, tab) => {
                const anchor = $(tab).find("a").first();
                const label = anchor.text().trim();
                if (!label || PalantirCommandPalette.isHidden(tab) || PalantirCommandPalette.isHidden(anchor)) {
                    return;
                }
                entries.push({
                    category: "Sections",
                    label: `${item.label} › ${label}`,
                    detail: item.group,
                    icon: item.icon,
                    keywords: label,
                    run: async () => {
                        await PalantirCommandPalette.goTo(item.index);
                        if (outer.data("ui-tabs")) {
                            outer.tabs("option", "active", position);
                        }
                    }
                });
            });
        }
        return entries;
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
                keywords: [service.id, service.serviceId, service.clientId, service.description]
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
 * Wire the sidebar search button and Ctrl/⌘+K, which works in form fields too; "/" is bound with the other
 * hotkeys so it still types a slash in a field.
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
        }
    });
}
