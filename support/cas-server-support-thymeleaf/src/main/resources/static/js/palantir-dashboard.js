class PalantirDashboardTab {
    constructor(name, index, shortcut) {
        this.name = name;
        this.index = index;
        this.shortcut = shortcut;
    }
}

class Tabs {
    static APPLICATIONS = new PalantirDashboardTab("Applications Tab", 0, "a");
    static SYSTEM = new PalantirDashboardTab("System Tab", 1, "s");
    static TICKETS = new PalantirDashboardTab("Tickets Tab", 2, "t");
    static TASKS = new PalantirDashboardTab("Tasks Tab", 3, "k");
    static ACCESS_STRATEGY = new PalantirDashboardTab("Access Strategy Tab", 4, "z");
    static LOGGING = new PalantirDashboardTab("Logging Tab", 5, "l");
    static SSO_SESSIONS = new PalantirDashboardTab("SSO Sessions Tab", 6, "o");
    static CONFIGURATION = new PalantirDashboardTab("Configuration Tab", 7, "c");
    static PERSON_DIRECTORY = new PalantirDashboardTab("Person Directory Tab", 8, "d");
    static AUTHENTICATION = new PalantirDashboardTab("Authentication Tab", 9, "h");
    static CONSENT = new PalantirDashboardTab("Consent Tab", 10, "e");
    static PROTOCOLS = new PalantirDashboardTab("Protocols Tab", 11, "p");
    static THROTTLES = new PalantirDashboardTab("Throttles Tab", 12, "r");
    static MFA = new PalantirDashboardTab("MFA Tab", 13, "m");
    static MULTITENANCY = new PalantirDashboardTab("Multitenancy Tab", 14, "u");
    static CLUSTER = new PalantirDashboardTab("Cluster & High Availability Tab", 15, "v");
    static SETTINGS = new PalantirDashboardTab("Settings Dialog", 100, ",");
    static LOGOUT = new PalantirDashboardTab("Logout", 200, "x");

    static values() {
        return Object.values(Tabs);
    }
}

let currentActiveTab = Tabs.APPLICATIONS.index;
let dashboardTabBarInstance = null;

/**
 * The dashboard's MDC tab bar.
 *
 * A new MDCTabBar used to be constructed on every tab activation, each one attaching its own
 * listeners to the same element. The instance is built once and reused.
 *
 * @returns {MDCTabBar} the dashboard tab bar
 */
function dashboardTabBar() {
    if (!dashboardTabBarInstance) {
        dashboardTabBarInstance = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    }
    return dashboardTabBarInstance;
}

/**
 * Lazy per-tab initialization.
 *
 * Every tab used to be initialized during page load, which built all fifty-odd data tables and
 * fired every tab's actuator requests before the operator had looked at anything. A tab's
 * initializers now run the first time that tab is activated, so the initial load pays only for the
 * tab actually on screen.
 *
 * Registration order within a tab is preserved, and each tab is initialized at most once: the
 * promise for an in-flight initialization is returned to concurrent callers so a fast double click
 * cannot start the same work twice.
 */
const palantirTabInitializers = new Map();
const palantirTabInitialization = new Map();

/**
 * Register initializers to run the first time the given tab is activated.
 *
 * @param dashboardTab the tab, or its numeric index
 * @param initializers the functions to run, in order
 */
function registerDashboardTabInitializers(dashboardTab, ...initializers) {
    const tabIndex = typeof dashboardTab === "number" ? dashboardTab : dashboardTab.index;
    const registered = palantirTabInitializers.get(tabIndex) ?? [];
    palantirTabInitializers.set(tabIndex, registered.concat(initializers));
}

/**
 * Run a tab's registered initializers once.
 *
 * The panel's jQuery UI widgets are built first, because the initializers bind tabsactivate
 * handlers, read tabs("option", "active") and populate selectmenus.
 *
 * Failures are logged rather than rethrown: one tab that cannot initialize -- an actuator endpoint
 * that answers 403, say -- must not stop the operator from using the rest of the dashboard.
 *
 * @param dashboardTab the tab, or its numeric index
 * @returns {Promise<void>} resolved once the tab's initializers have run
 */
function initializeDashboardTab(dashboardTab) {
    const tabIndex = typeof dashboardTab === "number" ? dashboardTab : dashboardTab.index;
    const inFlight = palantirTabInitialization.get(tabIndex);
    if (inFlight) {
        return inFlight;
    }
    const initializers = palantirTabInitializers.get(tabIndex) ?? [];
    const initialization = (async () => {
        initializePalantirWidgets(document.getElementById(`attribute-tab-${tabIndex}`));
        for (const initializer of initializers) {
            try {
                await initializer();
            } catch (e) {
                console.error(`Unable to initialize tab ${tabIndex}:`, e);
            }
        }
        await restoreActiveTabs();
        await updateNavigationSidebar();
    })();
    palantirTabInitialization.set(tabIndex, initialization);
    return initialization;
}

/**
 * Whether the given tab has already been initialized or is being initialized.
 *
 * @param dashboardTab the tab, or its numeric index
 * @returns {boolean} true when the tab's initializers have already been started
 */
function isDashboardTabInitialized(dashboardTab) {
    const tabIndex = typeof dashboardTab === "number" ? dashboardTab : dashboardTab.index;
    return palantirTabInitialization.has(tabIndex);
}

function activateDashboardTab(idx) {
    try {
        const tabIndex = Number(idx);
        switch(tabIndex) {
            case Tabs.SETTINGS.index:
                $("#palantirSettingsDialog").dialog({
                    position: {
                        my: "center top",
                        at: "center top+100",
                        of: window
                    },
                    autoOpen: false,
                    modal: true,
                    width: 850,
                    height: "auto",
                    buttons: {
                        OK: function () {
                            const storedSettings = localStorage.getItem("PalantirSettings");
                            const palantirSettings = storedSettings ? JSON.parse(storedSettings) : {};
                            palantirSettings.refreshInterval = Number($("#palantirRefreshInterval").val());
                            localStorage.setItem("PalantirSettings", JSON.stringify(palantirSettings));
                            $(this).dialog("close");
                            location.reload(true);
                        },
                        Cancel: function () {
                            $(this).dialog("close");
                        }
                    },
                    open: function () {
                        initializePalantirActuatorsTable();
                        cas.init("#palantirSettingsDialog");
                        try {
                            $("#palantirRefreshInterval").selectmenu("destroy");
                        } catch (e) {
                        } finally {
                            $("#palantirRefreshInterval").selectmenu({
                                appendTo: $(this).closest(".ui-dialog")
                            });
                            $("#palantirRefreshInterval")
                                .val(palantirSettings().refreshInterval / 1000)
                                .selectmenu("refresh");
                        }
                    },
                    close: function () {
                        $(this).dialog("destroy");
                    }
                });
                $("#palantirSettingsDialog").dialog("open");
                break;
            case Tabs.LOGOUT.index:
                const url = retrieveDashboardUrl();
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = `${url}/logout`;
                document.body.appendChild(form);
                form.submit();
                break;
            default:
                dashboardTabBar().activateTab(tabIndex);
                currentActiveTab = tabIndex;
                updateNavigationSidebar();
                notifyPalantirPollingContextChanged();
                initializeDashboardTab(tabIndex).then(() => {
                    notifyPalantirPollingContextChanged();
                    if (currentActiveTab === Tabs.CLUSTER.index && typeof refreshActiveClusterTab === "function") {
                        refreshActiveClusterTab();
                    }
                });
                break;
        }
    } catch (e) {
        console.error("An error occurred while activating tab:", e);
    }
}

function retrieveDashboardUrl() {
    let url = new URL(location.href).pathname;
    if (!url.endsWith("/dashboard")) {
        url += "/dashboard";
    }
    return url;
}

/**
 * Marks a rail destination as the current one.
 *
 * The active class stays on the list item, because the stylesheet and several
 * selectors key off it. aria-current moves to the button inside it: the button
 * is the interactive element, so it is what assistive technology reports the
 * current location on.
 *
 * @param selectedItem the rail list item, or any element inside it
 * @returns {number} the tab index the rail item points at
 */
function selectSidebarMenuButton(selectedItem) {
    const item = $(selectedItem).closest("li");
    const index = item.data("tab-index");
    if (index !== Tabs.SETTINGS.index && index !== Tabs.LOGOUT.index) {
        $("nav.sidebar-navigation ul li").removeClass("active");
        $("nav.sidebar-navigation ul li .sidebar-navigation-button").removeAttr("aria-current");
        item.addClass("active");
        item.find(".sidebar-navigation-button").attr("aria-current", "page");
        window.localStorage.setItem("PalantirSelectedTab", index);
    }
    return index;
}

function processNavigationTabs() {
    if (!CasActuatorEndpoints.registeredServices()) {
        hideElements($("#applicationsTabButton"));
        hideElements($(`#attribute-tab-${Tabs.APPLICATIONS.index}`));
    }
    if (!CasActuatorEndpoints.metrics() || !CasActuatorEndpoints.auditEvents()
        || !CasActuatorEndpoints.heapDump() || !CasActuatorEndpoints.health() || !CasActuatorEndpoints.statistics()) {
        hideElements($("#systemTabButton"));
        hideElements($(`#attribute-tab-${Tabs.SYSTEM.index}`));
    }
    if (!CasActuatorEndpoints.httpExchanges()) {
        hideElements($("#httprequeststab").parent());
    }
    if (!CasActuatorEndpoints.startup()) {
        hideElements($("#casstartuptab").parent());
    }
    if (!CasActuatorEndpoints.dependencies()) {
        hideElements($("#system-info-tabs"));
        hideElements($("#casdependenciestab").parent());
        hideElements($("#casvulnerabilitiestab").parent());
    }
    if (!CasActuatorEndpoints.metrics()) {
        hideElements($("#systemmetricstab").parent());
    }
    if (!CasActuatorEndpoints.mappings()) {
        hideElements($("#httprequestsmappingstab").parent());
    }
    if (!CasActuatorEndpoints.prometheus()) {
        hideElements($("#prometheusmetricstab").parent());
    }
    if (!CasActuatorEndpoints.springWebflow()) {
        hideElements($("#caswebflowtab").parent());
    }
    if (!CasActuatorEndpoints.ticketRegistry()) {
        hideElements($("#ticketsTabButton"));
        hideElements($(`#attribute-tab-${Tabs.TICKETS.index}`));
    }
    if (!CasActuatorEndpoints.scheduledTasks()) {
        hideElements($("#tasksTabButton"));
        hideElements($(`#attribute-tab-${Tabs.TASKS.index}`));
    }
    if (!CasActuatorEndpoints.personDirectory()) {
        hideElements($("#personDirectoryTabButton"));
        hideElements($(`#attribute-tab-${Tabs.PERSON_DIRECTORY.index}`));
    }
    if (!CasActuatorEndpoints.authenticationHandlers() || !CasActuatorEndpoints.authenticationPolicies()) {
        hideElements($("#authenticationTabButton"));
        hideElements($(`#attribute-tab-${Tabs.AUTHENTICATION.index}`));
    }
    if (!CasActuatorEndpoints.serviceAccess()) {
        hideElements($("#accessStrategyTabButton"));
        hideElements($(`#attribute-tab-${Tabs.ACCESS_STRATEGY.index}`));
    }
    if (!CasActuatorEndpoints.ssoSessions() || !CasActuatorEndpoints.sessions()) {
        hideElements($("#ssoSessionsTabButton"));
        hideElements($(`#attribute-tab-${Tabs.SSO_SESSIONS.index}`));
    }
    if (!CasActuatorEndpoints.ssoSessions()) {
       hideElements($("#ssosessionstab").parent());
    }
    if (!CasActuatorEndpoints.sessions()) {
        hideElements($("#springsessionstab").parent());
    }
    
    if (!CasActuatorEndpoints.auditLog()) {
        hideElements($("#auditEvents").parent());
    }
    if (!CasActuatorEndpoints.events()) {
        hideElements($("#casEvents").parent());
    }
    if ((!CasActuatorEndpoints.loggingConfig() || !CasActuatorEndpoints.loggers()) && !CasActuatorEndpoints.auditLog()) {
        hideElements($("#loggingTabButton"));
        hideElements($(`#attribute-tab-${Tabs.LOGGING.index}`));
    }
    if (!CasActuatorEndpoints.env() || !CasActuatorEndpoints.configProps()) {
        hideElements($("#configurationTabButton"));
        hideElements($(`#attribute-tab-${Tabs.CONFIGURATION.index}`));
    }
    if (!CasActuatorEndpoints.attributeConsent() || !CAS_FEATURES.includes("Consent")) {
        hideElements($("#consentTabButton"));
        hideElements($(`#attribute-tab-${Tabs.CONSENT.index}`));
    }
    if (!CasActuatorEndpoints.casValidate()) {
        $("#casprotocol").parent().remove();
        hideElements($("#casProtocolContainer"));
    }
    if (!CasActuatorEndpoints.samlPostProfileResponse() || !CAS_FEATURES.includes("SAMLIdentityProvider")) {
        $("#saml2protocol").parent().remove();
        hideElements($("#saml2ProtocolContainer"));
    }
    if (!CasActuatorEndpoints.samlValidate() || !CAS_FEATURES.includes("SAML")) {
        hideElements($("#saml1ProtocolContainer"));
        $("#saml1protocol").parent().remove();
    }
    if (!CasActuatorEndpoints.casConfig()) {
        hideElements($("#config-encryption-tab"));
        $("#casConfigSecurity").parent().remove();
    }
    if (!CasActuatorEndpoints.refresh()) {
        hideElements($("#refreshConfigurationButton"));
    }
    if (!CasActuatorEndpoints.configurationMetadata()) {
        hideElements($("#casConfigSearch"));
    }
    if (!CasActuatorEndpoints.beans()) {
        hideElements($("#springBeansTabItem"));
    }
    if (!CasActuatorEndpoints.conditions()) {
        hideElements($("#springConditionsTabItem"));
    }
    const oidcProtocolAvailable = CasActuatorEndpoints.oidcJwks() && CAS_FEATURES.includes("OpenIDConnect");
    if (!oidcProtocolAvailable && !CasActuatorEndpoints.oauthClientSecrets()) {
        $("#oidcprotocol").parent().remove();
        hideElements($("#oidcProtocolContainer"));
    }
    if (!CasActuatorEndpoints.samlValidate() && !CasActuatorEndpoints.casValidate()
        && !CasActuatorEndpoints.samlPostProfileResponse() && !CasActuatorEndpoints.oidcJwks()
        && !CasActuatorEndpoints.oauthClientSecrets()) {
        hideElements($("#protocolsTabButton"));
        hideElements($(`#attribute-tab-${Tabs.PROTOCOLS.index}`));
    }
    if (!CasActuatorEndpoints.throttles()) {
        hideElements($("#throttlesTabButton"));
        hideElements($(`#attribute-tab-${Tabs.THROTTLES.index}`));
    }
    if (!CasActuatorEndpoints.mfaDevices() || PalantirDashboardConfiguration.availableMultifactorProviders().length === 0) {
        hideElements($("#mfaTabButton"));
        hideElements($("#mfaDevicesTab").parent());
        hideElements($(`#attribute-tab-${Tabs.MFA.index}`));
    }
    if (!CasActuatorEndpoints.multifactorTrustedDevices() || PalantirDashboardConfiguration.availableMultifactorProviders().length === 0) {
        hideElements($("#trustedMfaDevicesTab").parent());
    }
    if (!CasActuatorEndpoints.multitenancy() || !CAS_FEATURES.includes("Multitenancy")) {
        hideElements($("#tenantsTabButton"));
    }
    if (!CasActuatorEndpoints.clusterTopology()) {
        hideElements($("#clusterTabButton"));
        hideElements($(`#attribute-tab-${Tabs.CLUSTER.index}`));
    }
    if (!CasActuatorEndpoints.restart()) {
        hideElements($("#restartServerButton"));
    }
    if (!CasActuatorEndpoints.shutdown()) {
        hideElements($("#shutdownServerButton"));
    }
    if (!PalantirDashboardConfiguration.mutablePropertySourcesAvailable()) {
        hideElements($("#mutableConfigSources"));
    }
    if (!PalantirDashboardConfiguration.scriptFactoryAvailable() || !CasActuatorEndpoints.groovyCache()) {
        hideElements($("#groovyScriptingTabItem"));
    }
    return $("nav.sidebar-navigation ul li:visible").length;
}

/**
 * Restore the inner jQuery UI tab selections the operator last used.
 *
 * Runs after each dashboard tab is initialized. Only containers on screen are restored: one that is
 * not yet a tabs widget would throw, and one in a hidden panel would have its stored selection reset
 * to the first entry, because none of its entries are :visible and the reset writes itself back to
 * storage. Restoring an outer container can reveal a nested one, so the sweep repeats while it keeps
 * making progress instead of depending on the order the selections were stored in.
 *
 * @returns {Promise<void>} resolved once the stored selections have been applied
 */
async function restoreActiveTabs() {
    const storedTabs = localStorage.getItem("ActiveTabs");
    const pending = new Map(Object.entries(storedTabs ? JSON.parse(storedTabs) : {}));

    let restoredAny = true;
    while (restoredAny && pending.size > 0) {
        restoredAny = false;
        for (const [key, value] of [...pending]) {
            const tabs = $(`#${key}`);
            if (tabs.length === 0 || !tabs.data("ui-tabs")) {
                pending.delete(key);
                continue;
            }
            if (!tabs.is(":visible")) {
                continue;
            }
            pending.delete(key);
            if (tabs.find("> ul > li").eq(Number(value)).is(":visible")) {
                tabs.tabs("option", "active", Number(value));
            } else {
                tabs.tabs("option", "active", 0);
            }
            tabs.tabs("refresh");
            restoredAny = true;
        }
    }
    notifyPalantirPollingContextChanged();
}

async function updateNavigationSidebar() {
    $("nav.sidebar-navigation").css("height", $("#dashboard .mdc-card").css("height"));
}

function selectSidebarMenuTab(tab) {
    const applicationMenuItem = $(`nav.sidebar-navigation ul li[data-tab-index=${tab}]`);
    selectSidebarMenuButton(applicationMenuItem);
}

/**
 * Select a registered service on the applications tab, revealing that tab first.
 *
 * The search is drawn before the match is marked: the table renders rows lazily, so a row that is
 * not on the current page has no node to mark, and a node created by the draw would not carry the
 * class anyway.
 *
 * @param serviceIdToFind the registered service id to look for
 */
async function navigateToApplication(serviceIdToFind) {
    activateDashboardTab(Tabs.APPLICATIONS.index);
    selectSidebarMenuTab(Tabs.APPLICATIONS.index);
    await initializeDashboardTab(Tabs.APPLICATIONS);

    let applicationsTable = $("#applicationsTable").DataTable();
    applicationsTable.search(String(serviceIdToFind)).draw();
    const foundRows = applicationsTable.rows({search: "applied"}).count();
    if (foundRows > 0) {
        applicationsTable.rows({search: "applied", page: "current"}).nodes().to$().addClass("selected");
    } else {
        displayBanner(`Could not find a registered service with id ${serviceIdToFind}`);
        applicationsTable.search("").draw();
    }
}

async function initializePalantirSession() {
    const intervalId = setInterval(async () => {
        if (document.visibilityState !== "visible") {
            return;
        }
        const url = retrieveDashboardUrl();
        const result = await fetch(`${url}/session`, { credentials: "include" });
        if (result.status !== 200) {
            clearInterval(intervalId);
            
            Swal.close();
            Swal.fire({
                title: "Session Expired",
                text: "Your Palantir session has expired. The dashboard will reload shortly.",
                icon: "info",
                timer: 3000,
                timerProgressBar: true,
                showConfirmButton: false
            }).then((result) => {
                if (result.dismiss === Swal.DismissReason.timer) {
                    activateDashboardTab(Tabs.LOGOUT.index);
                }
            });

        }
    }, 15000);
}
