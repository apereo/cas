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

    static serviceDefinitions() {
        return this.all().serviceDefinitions || {};
    }

    static supportedServiceTypes() {
        return this.all().supportedServiceTypes || {};
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

    static trumbowygIconsPath() {
        return this.all().trumbowygIconsPath;
    }
}

/**
 * Register every dashboard tab's initializers.
 *
 * Nothing here runs yet. Each entry is invoked the first time its tab is activated, which keeps a
 * cold load down to the requests the visible tab actually needs instead of every tab's data tables
 * and actuator calls at once.
 */
function registerPalantirTabInitializers() {
    registerDashboardTabInitializers(Tabs.APPLICATIONS,
        initializeApplicationsCharts,
        initializeServicesOperations);
    registerDashboardTabInitializers(Tabs.SYSTEM,
        initializeSystemCharts,
        initializeSystemOperations,
        initializeCasSpringWebflowOperations,
        initializePrometheusCharts);
    registerDashboardTabInitializers(Tabs.TICKETS,
        initializeTicketsOperations);
    registerDashboardTabInitializers(Tabs.TASKS,
        initializeTasksCharts,
        initializeScheduledTasksOperations);
    registerDashboardTabInitializers(Tabs.ACCESS_STRATEGY,
        initializeAccessStrategyOperations,
        initializeHeimdallOperations);
    registerDashboardTabInitializers(Tabs.LOGGING,
        initializeLoggingOperations,
        initializeAuditEventsOperations,
        initializeCasEventsOperations);
    registerDashboardTabInitializers(Tabs.SSO_SESSIONS,
        initializeSsoSessionOperations);
    registerDashboardTabInitializers(Tabs.CONFIGURATION,
        initializeConfigurationOperations);
    registerDashboardTabInitializers(Tabs.PERSON_DIRECTORY,
        initializePersonDirectoryOperations);
    registerDashboardTabInitializers(Tabs.AUTHENTICATION,
        initializeAuthenticationOperations);
    registerDashboardTabInitializers(Tabs.CONSENT,
        initializeConsentOperations);
    registerDashboardTabInitializers(Tabs.PROTOCOLS,
        initializeCasProtocolOperations,
        initializeSAML1ProtocolOperations,
        initializeSAML2ProtocolOperations,
        initializeOidcProtocolOperations);
    registerDashboardTabInitializers(Tabs.THROTTLES,
        initializeThrottlesOperations);
    registerDashboardTabInitializers(Tabs.MFA,
        initializeMultifactorOperations,
        initializeTrustedMultifactorOperations);
    registerDashboardTabInitializers(Tabs.MULTITENANCY,
        initializeMultitenancyOperations);
    registerDashboardTabInitializers(Tabs.CLUSTER,
        initializeClusterTopologyOperations);
}

/**
 * Resolve the tab to open.
 *
 * The tab the operator last used is preferred, then Applications, then whichever destination the
 * deployment's enabled actuator endpoints actually left on the rail. Only the first visible entry
 * is guaranteed to exist, so it is the last resort rather than Applications, which a deployment
 * without the registered services endpoint does not show at all.
 *
 * @returns {number} the tab index to activate
 */
function resolveInitialDashboardTab() {
    const storedTab = window.localStorage.getItem("PalantirSelectedTab");
    if ($(`nav.sidebar-navigation ul li[data-tab-index=${storedTab}]`).is(":visible")) {
        return Number(storedTab);
    }
    if ($(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.APPLICATIONS.index}]`).is(":visible")) {
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
        applySharedDialogStyle();
        await initializeCasFeatures();
        showElements("#dashboard");
        const visibleCount = processNavigationTabs();
        updatePalantirNavigationGroups();
        const selectedTab = resolveInitialDashboardTab();
        if (visibleCount === 0 || selectedTab < 0) {
            $("#dashboard").hide();
            Swal.fire({
                title: "Palantir is unavailable!",
                text: "Palantir requires a number of actuator endpoints to be enabled and exposed, "
                    + "and your CAS deployment fails to do so.",
                icon: "warning",
                showConfirmButton: false
            });
            return;
        }

        registerPalantirTabInitializers();

        await initializeHotKeyOperations();
        await initializePalantirSession();

        $(`nav.sidebar-navigation ul li[data-tab-index=${selectedTab}]`).click();
        await initializeDashboardTab(selectedTab);
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
        $("#palantirPageTitle").text(label);
        $("#palantirPageGroup").text(group).toggleClass("d-none", group === "");
    }
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
                                                                         
