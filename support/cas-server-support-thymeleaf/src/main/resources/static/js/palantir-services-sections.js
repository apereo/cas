/**
 * Section navigation for the registered service editor.
 *
 * The editor carries more than thirty sections, most of which are expiration
 * policies that only apply to one protocol. Presented as one flat accordion
 * they read as an undifferentiated list. This replaces that accordion with a
 * grouped rail: sections are collected into a handful of named groups, one
 * section is shown at a time, and the rail can be filtered by name or narrowed
 * to the sections that actually hold a value.
 *
 * The section markup is untouched. Every direct-child h3 of
 * #editServiceWizardSectionPanels is a section header, the element after it is
 * its panel, and data-section-group on the header says where it belongs.
 * Adding a section needs nothing here.
 */
class RegisteredServiceSections {

    static NAV_SELECTOR = "#editServiceWizardSectionNav";

    static PANELS_SELECTOR = "#editServiceWizardSectionPanels";

    static CURRENT_CLASS = "is-current-section";

    static STORAGE_SECTION = "registeredServiceWizardSection";

    static STORAGE_GROUPS = "registeredServiceWizardCollapsedGroups";

    static OTHER_GROUP = "Other";

    /**
     * The order groups appear in the rail.
     *
     * Ordered by how often a section in each group is actually edited, so the
     * thirteen expiration policies sit below the things most people came for.
     * A group named on a header but missing here is appended under Other.
     */
    static GROUP_ORDER = Object.freeze([
        "Basics",
        "Protocol",
        "Attributes",
        "Access & Authentication",
        "Tickets & Tokens",
        "Sessions & Flow",
        "Security"
    ]);

    static model = null;

    static filterText = "";

    static collapsedGroups = new Set();

    /**
     * Reads the section list out of the DOM, once.
     *
     * Header text is deliberately not cached: the wizard rewrites some headers
     * when the service type is known, so titles are read fresh on every render.
     *
     * @returns {Array} one entry per section, in document order
     */
    static sections() {
        if (RegisteredServiceSections.model !== null) {
            return RegisteredServiceSections.model;
        }
        const panels = document.querySelector(RegisteredServiceSections.PANELS_SELECTOR);
        if (!panels) {
            return [];
        }
        const model = [];
        [...panels.children].forEach((node, index) => {
            if (node.tagName !== "H3") {
                return;
            }
            const panel = node.nextElementSibling;
            if (!panel || panel.tagName === "H3") {
                return;
            }
            const key = node.id || panel.id || `service-section-${index}`;
            if (!node.id) {
                node.id = `${key}-header`;
            }
            panel.setAttribute("role", "region");
            panel.setAttribute("aria-labelledby", node.id);
            model.push({ key: key, header: node, panel: panel });
        });
        RegisteredServiceSections.model = model;
        return model;
    }

    /**
     * Whether a section applies to the service type being edited.
     *
     * The wizard hides inapplicable sections two different ways: hideElements
     * adds the hide and d-none classes, while the service-type switch calls
     * jQuery's hide(), which only writes an inline display. Both are checked.
     *
     * What is deliberately not checked is :visible, because this class hides
     * every section but the current one through a stylesheet rule; asking
     * whether a section is on screen would answer a different question.
     *
     * @param section an entry from sections()
     * @returns {boolean} true when the section should be offered in the rail
     */
    static isAvailable(section) {
        const header = section.header;
        if (header.classList.contains("hide") || header.classList.contains("d-none")) {
            return false;
        }
        return header.style.display !== "none";
    }

    /**
     * Reads the section title as it currently reads on screen.
     *
     * @param section an entry from sections()
     * @returns {string} the header's text
     */
    static titleOf(section) {
        return (section.header.textContent || "").trim();
    }

    /**
     * The group a section belongs to.
     *
     * @param section an entry from sections()
     * @returns {string} the declared group, or Other when none is declared
     */
    static groupOf(section) {
        return section.header.getAttribute("data-section-group") || RegisteredServiceSections.OTHER_GROUP;
    }

    /**
     * The key of the section currently on screen.
     *
     * @returns {string|null} the current section key, or null when none is shown
     */
    static currentKey() {
        const current = RegisteredServiceSections.sections()
            .find(section => section.header.classList.contains(RegisteredServiceSections.CURRENT_CLASS));
        return current ? current.key : null;
    }

    /**
     * Shows one section and hides the rest.
     *
     * An unknown key, or one belonging to a section that does not apply to this
     * service type, falls back to the first available section so the editor is
     * never left blank.
     *
     * @param key the section key to show
     * @returns {string|null} the key actually shown
     */
    static activate(key) {
        const sections = RegisteredServiceSections.sections();
        const available = sections.filter(RegisteredServiceSections.isAvailable);
        const target = available.find(section => section.key === key) || available[0] || null;
        sections.forEach(section => {
            const isTarget = target !== null && section.key === target.key;
            section.header.classList.toggle(RegisteredServiceSections.CURRENT_CLASS, isTarget);
            section.panel.classList.toggle(RegisteredServiceSections.CURRENT_CLASS, isTarget);
        });
        if (target !== null) {
            RegisteredServiceSections.store(RegisteredServiceSections.STORAGE_SECTION, target.key);
        }
        RegisteredServiceSections.markCurrentInNav();
        return target ? target.key : null;
    }

    /**
     * Runs a callback against every section that applies to this service type.
     *
     * Each section is brought on screen before the callback runs, because the
     * editor only renders the current one and browser field validation cannot
     * report on a field that is not displayed. Returning false stops the walk.
     * The caller is responsible for restoring the previously shown section.
     *
     * @param callback invoked with each available section
     */
    static eachAvailableSection(callback) {
        const available = RegisteredServiceSections.sections().filter(RegisteredServiceSections.isAvailable);
        for (const section of available) {
            RegisteredServiceSections.activate(section.key);
            if (callback(section) === false) {
                return;
            }
        }
    }

    /**
     * Writes a value to local storage, ignoring browsers that refuse.
     *
     * @param key the storage key
     * @param value the value to store
     */
    static store(key, value) {
        try {
            window.localStorage.setItem(key, value);
        } catch (error) {
            console.debug("Palantir could not persist the service editor section state", error);
        }
    }

    /**
     * Reads a value from local storage, tolerating browsers that refuse.
     *
     * @param key the storage key
     * @returns {string|null} the stored value, or null
     */
    static read(key) {
        try {
            return window.localStorage.getItem(key);
        } catch (error) {
            return null;
        }
    }

    /**
     * Builds the parts of the rail that survive a re-render.
     *
     * The filter box and the toggles are created once so that typing in the
     * filter does not destroy the element being typed into.
     */
    static buildToolbar() {
        const nav = document.querySelector(RegisteredServiceSections.NAV_SELECTOR);
        if (!nav || nav.children.length > 0) {
            return;
        }
        nav.innerHTML = `
            <div class="registered-service-sections__toolbar">
                <label class="registered-service-sections__search" for="editServiceWizardSectionFilter">
                    <i class="mdi mdi-magnify" aria-hidden="true"></i>
                    <span class="registered-service-sections__search-label">Filter sections</span>
                    <input type="search" id="editServiceWizardSectionFilter"
                           autocomplete="off" placeholder="Filter sections">
                </label>
                <div class="registered-service-sections__controls">
                    <span id="editServiceWizardSectionCount"
                          class="registered-service-sections__count"></span>
                </div>
            </div>
            <div id="editServiceWizardSectionList" class="registered-service-sections__list"></div>
            <p id="editServiceWizardSectionStatus" class="hide" role="status" aria-live="polite"></p>`;
    }

    /**
     * Redraws the group list from the current sections, filter and toggles.
     */
    static render() {
        const list = document.getElementById("editServiceWizardSectionList");
        if (!list) {
            return;
        }
        const term = RegisteredServiceSections.filterText.trim().toLowerCase();
        const available = RegisteredServiceSections.sections().filter(RegisteredServiceSections.isAvailable);
        const buckets = new Map();
        const totals = new Map();
        available.forEach(section => {
            const group = RegisteredServiceSections.groupOf(section);
            totals.set(group, (totals.get(group) || 0) + 1);
            const matchesTerm = term === "" || RegisteredServiceSections.titleOf(section).toLowerCase().includes(term)
                || group.toLowerCase().includes(term);
            if (!matchesTerm) {
                return;
            }
            if (!buckets.has(group)) {
                buckets.set(group, []);
            }
            buckets.get(group).push({ section: section });
        });

        const order = [...RegisteredServiceSections.GROUP_ORDER,
            ...[...buckets.keys()].filter(group => !RegisteredServiceSections.GROUP_ORDER.includes(group))];
        const currentKey = RegisteredServiceSections.currentKey();
        const shown = [...buckets.values()].reduce((sum, entries) => sum + entries.length, 0);

        const markup = order.filter(group => buckets.has(group)).map(group => {
            const entries = buckets.get(group);
            const counts = totals.get(group);
            const expanded = term !== "" || !RegisteredServiceSections.collapsedGroups.has(group);
            const groupId = `service-section-group-${group.replace(/[^a-z0-9]+/gi, "-").toLowerCase()}`;
            const items = entries.map(entry => `
                <li>
                    <button type="button" class="registered-service-sections__link${entry.section.key === currentKey ? " is-selected" : ""}"
                            data-section-key="${entry.section.key}"
                            ${entry.section.key === currentKey ? "aria-current=\"true\"" : ""}>
                        <span class="registered-service-sections__link-text">${RegisteredServiceSections.escape(RegisteredServiceSections.titleOf(entry.section))}</span>
                    </button>
                </li>`).join("");
            return `
                <section class="registered-service-sections__group${expanded ? " is-open" : ""}">
                    <h4 class="registered-service-sections__group-heading">
                        <button type="button" class="registered-service-sections__group-toggle"
                                data-section-group-toggle="${RegisteredServiceSections.escape(group)}"
                                aria-expanded="${expanded}" aria-controls="${groupId}">
                            <i class="mdi mdi-chevron-right registered-service-sections__caret" aria-hidden="true"></i>
                            <span class="registered-service-sections__group-name">${RegisteredServiceSections.escape(group)}</span>
                            <span class="registered-service-sections__group-count">${counts}</span>
                        </button>
                    </h4>
                    <ul id="${groupId}" class="registered-service-sections__group-items">${items}</ul>
                </section>`;
        }).join("");

        list.innerHTML = markup || `
            <p class="registered-service-sections__empty">No section matches that filter.</p>`;
        const count = document.getElementById("editServiceWizardSectionCount");
        if (count) {
            count.textContent = `${shown} of ${available.length}`;
        }
    }

    /**
     * Keeps the rail's selected state in step with the section on screen.
     */
    static markCurrentInNav() {
        const currentKey = RegisteredServiceSections.currentKey();
        let selectedLink = null;
        document.querySelectorAll("[data-section-key]").forEach(link => {
            const selected = link.getAttribute("data-section-key") === currentKey;
            link.classList.toggle("is-selected", selected);
            if (selected) {
                link.setAttribute("aria-current", "true");
                selectedLink = link;
            } else {
                link.removeAttribute("aria-current");
            }
        });
        RegisteredServiceSections.revealInNav(selectedLink);
    }

    /**
     * Scrolls the rail just far enough to bring an entry into view.
     *
     * Only the rail scrolls. scrollIntoView would be shorter but is free to
     * scroll the dialog and the page with it, which is disorienting when the
     * editor is reopened on a section near the bottom of a long list.
     *
     * @param link the rail entry to reveal, or null
     */
    static revealInNav(link) {
        const nav = document.querySelector(RegisteredServiceSections.NAV_SELECTOR);
        if (!nav || !link) {
            return;
        }
        const above = link.offsetTop - nav.scrollTop;
        const below = above + link.offsetHeight - nav.clientHeight;
        if (above < 0) {
            nav.scrollTop += above;
        } else if (below > 0) {
            nav.scrollTop += below;
        }
    }

    /**
     * Escapes text for insertion into the rail markup.
     *
     * @param value the text to escape
     * @returns {string} the escaped text
     */
    static escape(value) {
        return String(value).replace(/[&<>"']/g, character => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
        })[character]);
    }

    /**
     * Rebuilds the rail and makes sure a valid section is on screen.
     *
     * Called wherever the wizard used to refresh the accordion: after the
     * service type is chosen, after advanced options are toggled, and when the
     * JSON editor is shown or hidden.
     */
    static refresh() {
        if (document.querySelector(RegisteredServiceSections.PANELS_SELECTOR) === null) {
            return;
        }
        RegisteredServiceSections.buildToolbar();
        const current = RegisteredServiceSections.currentKey()
            || RegisteredServiceSections.read(RegisteredServiceSections.STORAGE_SECTION);
        RegisteredServiceSections.activate(current);
        RegisteredServiceSections.render();
    }

    /**
     * Wires the rail up. Safe to call more than once.
     */
    static initialize() {
        const nav = document.querySelector(RegisteredServiceSections.NAV_SELECTOR);
        if (!nav || nav.dataset.initialized === "true") {
            return;
        }
        nav.dataset.initialized = "true";

        const collapsed = RegisteredServiceSections.read(RegisteredServiceSections.STORAGE_GROUPS);
        if (collapsed) {
            try {
                RegisteredServiceSections.collapsedGroups = new Set(JSON.parse(collapsed));
            } catch (error) {
                RegisteredServiceSections.collapsedGroups = new Set();
            }
        }
        RegisteredServiceSections.buildToolbar();

        nav.addEventListener("click", event => {
            const link = event.target.closest("[data-section-key]");
            if (link && nav.contains(link)) {
                RegisteredServiceSections.activate(link.getAttribute("data-section-key"));
                RegisteredServiceSections.render();
                return;
            }
            const toggle = event.target.closest("[data-section-group-toggle]");
            if (toggle && nav.contains(toggle)) {
                RegisteredServiceSections.toggleGroup(toggle.getAttribute("data-section-group-toggle"));
                return;
            }
        });

        nav.addEventListener("input", event => {
            const filter = event.target.closest("#editServiceWizardSectionFilter");
            if (filter && nav.contains(filter)) {
                RegisteredServiceSections.filterText = filter.value || "";
                RegisteredServiceSections.render();
            }
        });

        RegisteredServiceSections.render();
    }

    /**
     * Opens or closes one group and remembers which way it was left.
     *
     * @param group the group name to toggle
     */
    static toggleGroup(group) {
        if (RegisteredServiceSections.collapsedGroups.has(group)) {
            RegisteredServiceSections.collapsedGroups.delete(group);
        } else {
            RegisteredServiceSections.collapsedGroups.add(group);
        }
        RegisteredServiceSections.store(RegisteredServiceSections.STORAGE_GROUPS,
            JSON.stringify([...RegisteredServiceSections.collapsedGroups]));
        RegisteredServiceSections.render();
    }

}
