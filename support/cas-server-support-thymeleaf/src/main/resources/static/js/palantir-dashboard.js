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
    static TASKS = new PalantirDashboardTab("Tasks Tab", 3, "");
    static ACCESS_STRATEGY = new PalantirDashboardTab("Access Strategy Tab", 4, "z");
    static LOGGING = new PalantirDashboardTab("Logging Tab", 5, "l");
    static SSO_SESSIONS = new PalantirDashboardTab("SSO Sessions Tab", 6, "o");
    static CONFIGURATION = new PalantirDashboardTab("Configuration Tab", 7, "c");
    static PERSON_DIRECTORY = new PalantirDashboardTab("Person Directory Tab", 8, "d");
    static AUTHENTICATION = new PalantirDashboardTab("Authentication Tab", 9, "h");
    static CONSENT = new PalantirDashboardTab("Consent Tab", 10, "");
    static PROTOCOLS = new PalantirDashboardTab("Protocols Tab", 11, "p");
    static THROTTLES = new PalantirDashboardTab("Throttles Tab", 12, "");
    static MFA = new PalantirDashboardTab("MFA Tab", 13, "m");
    static MULTITENANCY = new PalantirDashboardTab("Multitenancy Tab", 14, "");
    static SETTINGS = new PalantirDashboardTab("Settings Dialog", 100, ",");

    static values() {
        return Object.values(Tabs);
    }
}

let currentActiveTab = Tabs.APPLICATIONS.index;

function activateDashboardTab(idx) {
    try {
        const tabIndex = Number(idx);
        if (tabIndex === Tabs.SETTINGS.index) {
            $("#palantirSettingsDialog").dialog({
                position: {
                    my: "center top",
                    at: "center top+100",
                    of: window
                },
                autoOpen: false,
                modal: true,
                width: 600,
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
        } else {
            let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
            tabs.activateTab(tabIndex);
            currentActiveTab = tabIndex;
            updateNavigationSidebar();
        }
    } catch (e) {
        console.error("An error occurred while activating tab:", e);
    }
}

function selectSidebarMenuButton(selectedItem) {
    const index = $(selectedItem).data("tab-index");
    if (index !== Tabs.SETTINGS.index) {
        $("nav.sidebar-navigation ul li").removeClass("active");
        $(selectedItem).addClass("active");
        window.localStorage.setItem("PalantirSelectedTab", index);
    }
    return index;
}

function processNavigationTabs() {
    if (!actuatorEndpoints.registeredservices) {
        $("#applicationsTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.APPLICATIONS.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.metrics || !actuatorEndpoints.httpexchanges || !actuatorEndpoints.auditevents
        || !actuatorEndpoints.heapdump || !actuatorEndpoints.health || !actuatorEndpoints.statistics) {
        $("#systemTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.SYSTEM.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.metrics) {
        $("#systemmetricstab").parent().addClass("d-none");
    }
    if (!actuatorEndpoints.springWebflow) {
        $("#caswebflowtab").parent().addClass("d-none");
    }
    if (!actuatorEndpoints.ticketregistry) {
        $("#ticketsTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.TICKETS.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.scheduledtasks) {
        $("#tasksTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.TASKS.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.persondirectory) {
        $("#personDirectoryTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.PERSON_DIRECTORY.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.authenticationHandlers || !actuatorEndpoints.authenticationPolicies) {
        $("#authenticationTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.AUTHENTICATION.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.serviceaccess) {
        $("#accessStrategyTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.ACCESS_STRATEGY.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.ssosessions) {
        $("#ssoSessionsTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.SSO_SESSIONS.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.auditlog) {
        $("#auditEvents").parent().addClass("d-none");
    }
    if (!actuatorEndpoints.events) {
        $("#casEvents").parent().addClass("d-none");
    }
    if ((!actuatorEndpoints.loggingconfig || !actuatorEndpoints.loggers) && !actuatorEndpoints.auditlog) {
        $("#loggingTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.LOGGING.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.env || !actuatorEndpoints.configprops) {
        $("#configurationTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.CONFIGURATION.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.attributeconsent || !CAS_FEATURES.includes("Consent")) {
        $("#consentTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.CONSENT.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.casvalidate) {
        $("#casprotocol").parent().remove();
        $("#casProtocolContainer").addClass("d-none");
    }
    if (!actuatorEndpoints.samlpostprofileresponse || !CAS_FEATURES.includes("SAMLIdentityProvider")) {
        $("#saml2protocol").parent().remove();
        $("#saml2ProtocolContainer").addClass("d-none");
    }
    if (!actuatorEndpoints.samlvalidate || !CAS_FEATURES.includes("SAML")) {
        $("#saml1ProtocolContainer").addClass("d-none");
        $("#saml1protocol").parent().remove();
    }
    if (!actuatorEndpoints.casconfig) {
        $("#config-encryption-tab").addClass("d-none");
        $("#casConfigSecurity").parent().remove();
    }
    if (!actuatorEndpoints.refresh && !actuatorEndpoints.busrefresh) {
        $("#refreshConfigurationButton").addClass("d-none");
    }
    if (!actuatorEndpoints.configurationmetadata) {
        $("#casConfigSearch").addClass("d-none");
    }
    if (!actuatorEndpoints.oidcjwks || !CAS_FEATURES.includes("OpenIDConnect")) {
        $("#oidcprotocol").parent().remove();
        $("#oidcProtocolContainer").addClass("d-none");
    }
    if (!actuatorEndpoints.samlvalidate && !actuatorEndpoints.casvalidate
        && !actuatorEndpoints.samlpostprofileresponse && !actuatorEndpoints.oidcjwks) {
        $("#protocolsTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.PROTOCOLS.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.throttles) {
        $("#throttlesTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.THROTTLES.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.mfadevices || availableMultifactorProviders.length === 0) {
        $("#mfaTabButton").addClass("d-none");
        $("#mfaDevicesTab").parent().addClass("d-none");
        $(`#attribute-tab-${Tabs.MFA.index}`).addClass("d-none");
    }
    if (!actuatorEndpoints.multifactortrusteddevices || availableMultifactorProviders.length === 0) {
        $("#trustedMfaDevicesTab").parent().addClass("d-none");
    }
    if (!actuatorEndpoints.multitenancy || !CAS_FEATURES.includes("Multitenancy")) {
        $("#tenantsTabButton").addClass("d-none");
    }
    if (!actuatorEndpoints.restart) {
        $("#restartServerButton").addClass("d-none");
    }
    if (!actuatorEndpoints.shutdown) {
        $("#shutdownServerButton").addClass("d-none");
    }
    if (!mutablePropertySourcesAvailable) {
        $("#mutableConfigSources").addClass("d-none");
    }
    return $("nav.sidebar-navigation ul li:visible").length;
}

async function restoreActiveTabs() {
    const storedTabs = localStorage.getItem("ActiveTabs");
    const activeTabs = storedTabs ? JSON.parse(storedTabs) : {};
    for (const [key, value] of Object.entries(activeTabs)) {
        const tabs = $(`#${key}`);
        if (tabs.find("> ul > li").eq(Number(value)).is(":visible")) {
            tabs.tabs("option", "active", Number(value));
        } else {
            tabs.tabs("option", "active", 0);
        }
        tabs.tabs("refresh");
    }
}

async function updateNavigationSidebar() {
    $("nav.sidebar-navigation").css("height", $("#dashboard .mdc-card").css("height"));
}

function selectSidebarMenuTab(tab) {
    const applicationMenuItem = $(`nav.sidebar-navigation ul li[data-tab-index=${tab}]`);
    selectSidebarMenuButton(applicationMenuItem);
}

function navigateToApplication(serviceIdToFind) {
    let applicationsTable = $("#applicationsTable").DataTable();
    applicationsTable.search(String(serviceIdToFind));
    const foundRows = applicationsTable.rows({search: "applied"}).count();
    if (foundRows > 0) {
        const matchingRows = applicationsTable.rows({search: "applied"});
        matchingRows.nodes().to$().addClass("selected");
        applicationsTable.draw();
        activateDashboardTab(Tabs.APPLICATIONS.index);
        selectSidebarMenuTab(Tabs.APPLICATIONS.index);
    } else {
        displayBanner(`Could not find a registered service with id ${serviceIdToFind}`);
        applicationsTable.search("").draw();
    }
}
