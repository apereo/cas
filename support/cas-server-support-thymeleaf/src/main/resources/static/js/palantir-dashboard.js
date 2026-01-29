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
    static LOGOUT = new PalantirDashboardTab("Logout", 200, "x");

    static values() {
        return Object.values(Tabs);
    }
}

let currentActiveTab = Tabs.APPLICATIONS.index;

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
                const url = new URL(location.href);
                fetch(`${url.pathname}/logout`, {
                    method: 'GET',
                    credentials: 'include'
                }).then((response) => {
                    window.location.reload();
                });
                break;
            default:
                let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
                tabs.activateTab(tabIndex);
                currentActiveTab = tabIndex;
                updateNavigationSidebar();
                break;
        }
    } catch (e) {
        console.error("An error occurred while activating tab:", e);
    }
}

function selectSidebarMenuButton(selectedItem) {
    const index = $(selectedItem).data("tab-index");
    if (index !== Tabs.SETTINGS.index && index !== Tabs.LOGOUT.index) {
        $("nav.sidebar-navigation ul li").removeClass("active");
        $(selectedItem).addClass("active");
        window.localStorage.setItem("PalantirSelectedTab", index);
    }
    return index;
}

function processNavigationTabs() {
    if (!CasActuatorEndpoints.registeredServices()) {
        hideElements($("#applicationsTabButton"));
        hideElements($(`#attribute-tab-${Tabs.APPLICATIONS.index}`));
    }
    if (!CasActuatorEndpoints.metrics() || !CasActuatorEndpoints.httpExchanges() || !CasActuatorEndpoints.auditEvents()
        || !CasActuatorEndpoints.heapDump() || !CasActuatorEndpoints.health() || !CasActuatorEndpoints.statistics()) {
        hideElements($("#systemTabButton"));
        hideElements($(`#attribute-tab-${Tabs.SYSTEM.index}`));
    }
    if (!CasActuatorEndpoints.metrics()) {
        hideElements($("#systemmetricstab").parent());
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
    if (!CasActuatorEndpoints.oidcJwks() || !CAS_FEATURES.includes("OpenIDConnect")) {
        $("#oidcprotocol").parent().remove();
        hideElements($("#oidcProtocolContainer"));
    }
    if (!CasActuatorEndpoints.samlValidate() && !CasActuatorEndpoints.casValidate()
        && !CasActuatorEndpoints.samlPostProfileResponse() && !CasActuatorEndpoints.oidcJwks()) {
        hideElements($("#protocolsTabButton"));
        hideElements($(`#attribute-tab-${Tabs.PROTOCOLS.index}`));
    }
    if (!CasActuatorEndpoints.throttles()) {
        hideElements($("#throttlesTabButton"));
        hideElements($(`#attribute-tab-${Tabs.THROTTLES.index}`));
    }
    if (!CasActuatorEndpoints.mfaDevices() || availableMultifactorProviders.length === 0) {
        hideElements($("#mfaTabButton"));
        hideElements($("#mfaDevicesTab").parent());
        hideElements($(`#attribute-tab-${Tabs.MFA.index}`));
    }
    if (!CasActuatorEndpoints.multifactorTrustedDevices() || availableMultifactorProviders.length === 0) {
        hideElements($("#trustedMfaDevicesTab").parent());
    }
    if (!CasActuatorEndpoints.multitenancy() || !CAS_FEATURES.includes("Multitenancy")) {
        hideElements($("#tenantsTabButton"));
    }
    if (!CasActuatorEndpoints.restart()) {
        hideElements($("#restartServerButton"));
    }
    if (!CasActuatorEndpoints.shutdown()) {
        hideElements($("#shutdownServerButton"));
    }
    if (!mutablePropertySourcesAvailable) {
        hideElements($("#mutableConfigSources"));
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

async function initializePalantirSession() {
    setInterval(async () => {
        const url = new URL(location.href);
        const result = await fetch(`${url.pathname}/session`, { credentials: "include" });
        if (result.status !== 200) {
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
