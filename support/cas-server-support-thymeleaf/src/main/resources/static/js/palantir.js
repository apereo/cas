let notyf = null;

class PalantirDashboardConfiguration {
    static all() {
        return window.palantirDashboardConfiguration || {};
    }

    static casServerPrefix() {
        return this.all().casServerPrefix;
    }

    static actuatorEndpoints() {
        return this.all().actuatorEndpoints || {};
    }

    static actuatorEndpointAccess() {
        return this.all().actuatorEndpointAccess || {};
    }

    static serviceTemplates() {
        return this.all().serviceTemplates || [];
    }

    static supportedServiceTypes() {
        return this.all().supportedServiceTypes || {};
    }

    /**
     * Whether this deployment carries an access strategy, since some live in modules of their own.
     *
     * @param type the access strategy class name
     * @returns {boolean} true when the strategy is present, or when CAS did not say which strategies are
     */
    static accessStrategyAvailable(type) {
        const strategies = this.all().supportedAccessStrategies ?? [];
        return strategies.length === 0 || strategies.includes(type);
    }

    static serviceProperties() {
        return this.all().serviceProperties || {};
    }

    static availableMultifactorProviders() {
        return this.all().availableMultifactorProviders || [];
    }

    static scriptFactoryAvailable() {
        return this.all().scriptFactoryAvailable || false;
    }

    static mutablePropertySources() {
        return this.all().mutablePropertySources || [];
    }

    static mutablePropertySourcesAvailable() {
        return this.all().mutablePropertySourcesAvailable || false;
    }

    /**
     * Whether CAS can encrypt and decrypt configuration values, because configuration security is set up.
     *
     * @returns {boolean} true when configuration values can be encrypted
     */
    static configurationEncryptionAvailable() {
        return this.all().configurationEncryptionAvailable === true;
    }

    static trumbowygIconsPath() {
        return this.all().trumbowygIconsPath;
    }
}

/**
 * Register every dashboard tab's initializers.
 *
 * Nothing here runs yet. Each entry is invoked the first time its tab is activated, which keeps a
 * cold load down to the requests the visible tab actually needs instead of every tab's data tables
 * and actuator calls at once. Each is named only when it runs, because the page's scripts are fetched
 * when the page is first opened.
 */
function registerPalantirTabInitializers() {
    registerDashboardTabInitializers(Tabs.APPLICATIONS,
        () => initializeApplicationsCharts(),
        () => initializeServicesOperations());
    registerDashboardTabInitializers(Tabs.SYSTEM,
        () => initializeSystemCharts(),
        () => initializeSystemOperations(),
        () => initializeCasSpringWebflowOperations(),
        () => initializePrometheusCharts());
    registerDashboardTabInitializers(Tabs.TICKETS,
        () => initializeTicketsOperations());
    registerDashboardTabInitializers(Tabs.TASKS,
        () => initializeTasksCharts(),
        () => initializeScheduledTasksOperations());
    registerDashboardTabInitializers(Tabs.ACCESS_STRATEGY,
        () => initializeAccessStrategyOperations(),
        () => initializeHeimdallOperations());
    registerDashboardTabInitializers(Tabs.LOGGING,
        () => initializeLoggingOperations(),
        () => initializeAuditEventsOperations(),
        () => initializeCasEventsOperations());
    registerDashboardTabInitializers(Tabs.SSO_SESSIONS,
        () => initializeSsoSessionOperations());
    registerDashboardTabInitializers(Tabs.CONFIGURATION,
        () => initializeConfigurationOperations());
    registerDashboardTabInitializers(Tabs.PERSON_DIRECTORY,
        () => initializePersonDirectoryOperations());
    registerDashboardTabInitializers(Tabs.AUTHENTICATION,
        () => initializeAuthenticationOperations());
    registerDashboardTabInitializers(Tabs.CONSENT,
        () => initializeConsentOperations());
    registerDashboardTabInitializers(Tabs.PROTOCOLS,
        () => initializeCasProtocolOperations(),
        () => initializeSAML1ProtocolOperations(),
        () => initializeSAML2ProtocolOperations(),
        () => initializeOidcProtocolOperations());
    registerDashboardTabInitializers(Tabs.THROTTLES,
        () => initializeThrottlesOperations());
    registerDashboardTabInitializers(Tabs.MFA,
        () => initializeMultifactorOperations(),
        () => initializeTrustedMultifactorOperations(),
        () => PalantirCredentialRepositories.initialize());
    registerDashboardTabInitializers(Tabs.MULTITENANCY,
        () => initializeMultitenancyOperations());
    registerDashboardTabInitializers(Tabs.CLUSTER,
        () => initializeClusterTopologyOperations());
    registerDashboardTabInitializers(Tabs.USERS,
        () => initializeUserLookupOperations());
    registerDashboardTabInitializers(Tabs.HOME,
        () => initializePalantirHome());
}

/**
 * Resolve the tab to open.
 *
 * A link to a page opens that page. A reload stays on the page this browser tab was showing; a new visit opens Home. Without Home, the
 * tab the operator last used is preferred, then Applications, then whichever destination the
 * deployment's enabled actuator endpoints actually left on the rail. Only the first visible entry
 * is guaranteed to exist, so it is the last resort rather than Applications, which a deployment
 * without the registered services endpoint does not show at all.
 *
 * @returns {number} the tab index to activate
 */
function resolveInitialDashboardTab() {
    const shown = value => {
        const tab = Tabs.of(value);
        return tab && tab.index < Tabs.SETTINGS.index && PalantirPages.available(tab) ? tab.index : null;
    };
    const linked = PalantirLinks.parse(location.hash).tab;
    if (linked && shown(linked) !== null) {
        return linked.index;
    }
    let reloadedTab = null;
    let storedTab = null;
    try {
        reloadedTab = window.sessionStorage.getItem("PalantirSelectedTab");
        storedTab = window.localStorage.getItem("PalantirSelectedTab");
    } catch (error) {
        console.debug("The last page cannot be read from the browser", error);
    }
    const reloaded = shown(reloadedTab);
    if (reloaded !== null) {
        return reloaded;
    }
    if (shown(Tabs.HOME.index) !== null) {
        return Tabs.HOME.index;
    }
    const stored = shown(storedTab);
    if (stored !== null) {
        return stored;
    }
    if (PalantirPages.available(Tabs.APPLICATIONS)) {
        return Tabs.APPLICATIONS.index;
    }
    const firstContentTab = $("nav.sidebar-navigation ul li:visible")
        .map((_, item) => Number($(item).data("tab-index")))
        .get()
        .find(index => Number.isFinite(index) && index < Tabs.SETTINGS.index);
    return firstContentTab ?? -1;
}

/**
 * Start the dashboard.
 *
 * The dashboard is revealed before the rail is processed, because processNavigationTabs counts its
 * destinations with :visible and nothing inside a display:none subtree is visible. Only the
 * selected tab is initialized; the rest follow as they are opened.
 */
async function initializePalantir() {
    try {
        installJQueryCompatibility();
        allowAlertsOverModalDialogs();
        applySharedDialogStyle();
        keepPalantirPopupsInView();
        PalantirMagnifier.initialize();
        initializeTableAndDialogAccessibility();
        initializePalantirEndpointAccess();
        await initializeCasFeatures();
        showElements("#dashboard");
        performance.mark("palantir-dashboard-shown");
        const visibleCount = processNavigationTabs();
        updatePalantirNavigationGroups();
        const selectedTab = resolveInitialDashboardTab();
        if (visibleCount === 0 || selectedTab < 0) {
            $("#dashboard").hide();
            Swal.fire({
                title: "Palantir Is Unavailable!",
                text: "Palantir requires a number of actuator endpoints to be enabled and exposed, "
                    + "and your CAS deployment fails to do so.",
                icon: "warning",
                showConfirmButton: false
            });
            return;
        }

        registerPalantirTabInitializers();
        PalantirServiceEditGuard.removeExpiredDrafts();

        await initializeHotKeyOperations();
        initializeCommandPalette();
        await initializePalantirSession();

        PalantirLinks.bind();
        const linked = PalantirLinks.parse(location.hash);
        if (linked.tab?.index === selectedTab) {
            await PalantirLinks.follow(location.hash);
        } else {
            PalantirPages.open(Tabs.byIndex(selectedTab));
            await initializeDashboardTab(selectedTab);
        }
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

/**
 * Close the rail overlay used on narrow screens, as the account profile does with its sections.
 *
 * @param {boolean} restoreFocus whether focus returns to the toggle that opened the overlay
 */
function closeSidebarSections(restoreFocus) {
    const dashboard = document.getElementById("dashboard");
    if (!dashboard || !dashboard.classList.contains("is-nav-open")) {
        return;
    }
    dashboard.classList.remove("is-nav-open");
    const toggle = dashboard.querySelector("[data-palantir-action='sections']");
    if (toggle) {
        toggle.setAttribute("aria-expanded", "false");
        if (restoreFocus) {
            toggle.focus();
        }
    }
}

function openSidebarSections() {
    const dashboard = document.getElementById("dashboard");
    dashboard.classList.add("is-nav-open");
    const toggle = dashboard.querySelector("[data-palantir-action='sections']");
    if (toggle) {
        toggle.setAttribute("aria-expanded", "true");
    }
    const current = dashboard.querySelector("nav.sidebar-navigation [aria-current='page']")
        || dashboard.querySelector("nav.sidebar-navigation .sidebar-navigation-button");
    if (current) {
        current.focus();
    }
}

/**
 * Close every open menu of the navigation bar.
 *
 * @param {Element|null} restoreFocusTo the element that receives focus afterwards, if any
 */
function closePalantirNavigationMenus(restoreFocusTo = null) {
    document.querySelectorAll("#palantirNavigation .palantir-nav-group.is-open").forEach(group => {
        group.classList.remove("is-open");
        group.querySelector(".palantir-nav-group-toggle")?.setAttribute("aria-expanded", "false");
    });
    restoreFocusTo?.focus();
}

/**
 * Open one menu of the navigation bar and close the others.
 *
 * Focus moves into the menu once the menu is drawn: the browser refuses focus to an element whose
 * visibility has just changed, for a frame or more, so the first destination is tried frame by frame.
 *
 * @param {Element} group the .palantir-nav-group to open
 * @param {boolean} focusFirst whether the first destination in the menu receives focus
 */
function openPalantirNavigationMenu(group, focusFirst) {
    closePalantirNavigationMenus();
    group.classList.add("is-open");
    group.querySelector(".palantir-nav-group-toggle")?.setAttribute("aria-expanded", "true");
    if (focusFirst) {
        const first = $(group).find("ul li:not(.hide) .sidebar-navigation-button").get(0);
        const focusWhenDrawn = attempts => {
            first?.focus();
            if (first && document.activeElement !== first && attempts > 0 && group.classList.contains("is-open")) {
                requestAnimationFrame(() => focusWhenDrawn(attempts - 1));
            }
        };
        requestAnimationFrame(() => focusWhenDrawn(20));
    }
}

/**
 * Keep the grouped menus in step with their destinations, and name the page after the current one.
 *
 * A group is hidden when the deployment hides all of its destinations, and marked when it holds the
 * current destination. The page heading shows the current destination's label and, when it sits in
 * a group, the group's name above it.
 */
function updatePalantirNavigationGroups() {
    document.querySelectorAll("#palantirNavigation .palantir-nav-group").forEach(group => {
        const destinations = [...group.querySelectorAll("ul li")];
        const available = destinations.filter(item => !item.classList.contains("hide") && !item.classList.contains("d-none"));
        group.classList.toggle("d-none", available.length === 0);
        group.classList.toggle("has-active", destinations.some(item => item.classList.contains("active")));
    });
    const active = document.querySelector("#palantirNavigation ul li.active");
    if (active) {
        const label = active.querySelector(".sidebar-navigation-label")?.textContent.trim() ?? "";
        const group = active.closest(".palantir-nav-group")?.querySelector(".palantir-nav-group-name")?.textContent.trim() ?? "";
        $("#palantirPageTitle").text(active.dataset.tabIndex === String(Tabs.HOME.index) ? palantirGreeting() : label);
        $("#palantirPageGroup").text(group).toggleClass("d-none", group === "");
    }
}

/**
 * The heading of Home: a greeting for the time of day on this computer, naming the user signed in to Palantir.
 *
 * @param {Date} now the moment to greet for
 * @returns {string} the greeting
 */
function palantirGreeting(now = new Date()) {
    const hour = now.getHours();
    const part = hour < 12 ? "Morning" : hour < 18 ? "Afternoon" : "Evening";
    const user = document.getElementById("palantirPageTitle")?.dataset.palantirUser?.trim() ?? "";
    return user === "" ? `Good ${part}!` : `Good ${part}, ${user}!`;
}

/**
 * Wire the menus of the navigation bar: toggles, outside clicks, Escape and the arrow keys.
 */
function initializePalantirNavigationMenus() {
    const navigation = document.getElementById("palantirNavigation");
    if (!navigation) {
        return;
    }
    navigation.addEventListener("click", event => {
        const toggle = event.target.closest(".palantir-nav-group-toggle");
        if (toggle) {
            const group = toggle.closest(".palantir-nav-group");
            if (group.classList.contains("is-open")) {
                closePalantirNavigationMenus();
            } else {
                openPalantirNavigationMenu(group, false);
            }
        }
    });
    navigation.addEventListener("keydown", event => {
        const toggle = event.target.closest(".palantir-nav-group-toggle");
        if (toggle && event.key === "ArrowDown") {
            event.preventDefault();
            openPalantirNavigationMenu(toggle.closest(".palantir-nav-group"), true);
            return;
        }
        const menu = event.target.closest(".palantir-nav-menu");
        if (menu && (event.key === "ArrowDown" || event.key === "ArrowUp")) {
            event.preventDefault();
            const buttons = $(menu).find("ul li:not(.hide) .sidebar-navigation-button").get();
            const index = buttons.indexOf(event.target.closest(".sidebar-navigation-button"));
            const next = event.key === "ArrowDown" ? index + 1 : index - 1;
            buttons[(next + buttons.length) % buttons.length]?.focus();
        }
    });
    document.addEventListener("click", event => {
        if (!event.target.closest("#palantirNavigation .palantir-nav-group")) {
            closePalantirNavigationMenus();
        }
    });
    document.addEventListener("keydown", event => {
        const open = document.querySelector("#palantirNavigation .palantir-nav-group.is-open");
        if (event.key === "Escape" && open) {
            closePalantirNavigationMenus(open.querySelector(".palantir-nav-group-toggle"));
        }
    });
    navigation.addEventListener("focusout", event => {
        const group = event.target.closest(".palantir-nav-group");
        if (group && !group.contains(event.relatedTarget)) {
            group.classList.remove("is-open");
            group.querySelector(".palantir-nav-group-toggle")?.setAttribute("aria-expanded", "false");
        }
    });
}

/**
 * Show focus rings only while the keyboard is used to move between controls.
 *
 * A browser draws the ring on whatever has focus as soon as any key is pressed, so a control clicked a moment
 * earlier, or the main panel, gained a ring when a single-key shortcut was used. The ring is hidden after a click,
 * a tap or a shortcut, and returns once Tab or an arrow key moves focus.
 */
function initializePalantirFocusRings() {
    const root = document.documentElement;
    document.addEventListener("pointerdown", () => root.setAttribute("data-palantir-pointer-focus", ""), true);
    document.addEventListener("keydown", event => {
        const key = typeof event.key === "string" ? event.key : "";
        if (key === "Tab" || key.startsWith("Arrow")) {
            root.removeAttribute("data-palantir-pointer-focus");
        } else if (key.length === 1 && key !== " " && !event.target.closest?.("input, textarea, select, [contenteditable='true']")) {
            root.setAttribute("data-palantir-pointer-focus", "");
        }
    }, true);
}

function initializeSidebarSections() {
    $("#dashboard [data-palantir-action='sections']").off("click").on("click", () => {
        if ($("#dashboard").hasClass("is-nav-open")) {
            closeSidebarSections(true);
        } else {
            openSidebarSections();
        }
    });
    $("#dashboard [data-palantir-action='close-sections']").off("click").on("click", () => closeSidebarSections(true));
    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && $("#dashboard").hasClass("is-nav-open")) {
            closeSidebarSections(true);
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    initializeAccordionAccessibility();
    initializePalantirInputIcons();
    initializePalantirPollingContext();
    initializePalantirWidgets();
    initializeTooltips();

    PalantirTheme.initialize();
    PalantirZenMode.initialize();
    initializePalantirFocusRings();
    PalantirPanelFocus.initialize();
    initializeSidebarSections();
    initializePalantirNavigationMenus();
    $("nav.sidebar-navigation ul li").off().on("click", function () {
        closeSidebarSections(false);
        closePalantirNavigationMenus();
        hideBanner();
        const index = selectSidebarMenuButton(this);
        activateDashboardTab(index);
    });
    notyf = new Notyf({
        duration: 3000,
        ripple: true,
        dismissable: true,
        position: {
            x: "center",
            y: "bottom"
        }
    });

    initializePalantir();
});
                                                                         
