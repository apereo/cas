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
        await initializeCasFeatures();
        showElements("#dashboard");
        const visibleCount = processNavigationTabs();
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
        window.addEventListener("resize", updateNavigationSidebar, {passive: true});

        $(`nav.sidebar-navigation ul li[data-tab-index=${selectedTab}]`).click();
        await initializeDashboardTab(selectedTab);
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initializeAccordionAccessibility();
    initializePalantirInputIcons();
    initializePalantirPollingContext();
    initializePalantirWidgets();
    initializeTooltips();

    $("nav.sidebar-navigation ul li").off().on("click", function () {
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
                                                                         
