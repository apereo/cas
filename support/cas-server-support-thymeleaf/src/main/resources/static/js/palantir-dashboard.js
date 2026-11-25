/**
 * A page of the dashboard. Its key is how the page is named in links, such as #applications/42, and what the browser
 * remembers as the page last shown; it does not change when pages are added or moved. The index is how the page is
 * wired to its panel and rail item.
 */
class PalantirDashboardTab {
    constructor(name, index, shortcut, key) {
        this.name = name;
        this.index = index;
        this.shortcut = shortcut;
        this.key = key;
    }
}

class Tabs {
    static APPLICATIONS = new PalantirDashboardTab("Applications Tab", 0, "a", "applications");
    static SYSTEM = new PalantirDashboardTab("System Tab", 1, "s", "system");
    static TICKETS = new PalantirDashboardTab("Tickets Tab", 2, "t", "tickets");
    static TASKS = new PalantirDashboardTab("Tasks Tab", 3, "k", "tasks");
    static ACCESS_STRATEGY = new PalantirDashboardTab("Access Strategy Tab", 4, "z", "access");
    static LOGGING = new PalantirDashboardTab("Logging Tab", 5, "l", "logging");
    static SSO_SESSIONS = new PalantirDashboardTab("SSO Sessions Tab", 6, "o", "sessions");
    static CONFIGURATION = new PalantirDashboardTab("Configuration Tab", 7, "c", "configuration");
    static PERSON_DIRECTORY = new PalantirDashboardTab("Person Directory Tab", 8, "d", "attributes");
    static AUTHENTICATION = new PalantirDashboardTab("Authentication Tab", 9, "h", "authentication");
    static CONSENT = new PalantirDashboardTab("Consent Tab", 10, "e", "consent");
    static PROTOCOLS = new PalantirDashboardTab("Protocols Tab", 11, "p", "protocols");
    static THROTTLES = new PalantirDashboardTab("Throttles Tab", 12, "r", "throttles");
    static MFA = new PalantirDashboardTab("MFA Tab", 13, "m", "mfa");
    static MULTITENANCY = new PalantirDashboardTab("Multitenancy Tab", 14, "u", "tenants");
    static CLUSTER = new PalantirDashboardTab("Cluster & High Availability Tab", 15, "v", "cluster");
    static USERS = new PalantirDashboardTab("Users Tab", 16, "i", "users");
    static HOME = new PalantirDashboardTab("Home", 17, "g", "home");
    static SETTINGS = new PalantirDashboardTab("Settings Dialog", 100, ",", "settings");
    static LOGOUT = new PalantirDashboardTab("Logout", 200, "x", "logout");

    static values() {
        return Object.values(Tabs).filter(value => value instanceof PalantirDashboardTab);
    }

    static byKey(key) {
        return Tabs.values().find(tab => tab.key === String(key ?? "").toLowerCase()) ?? null;
    }

    static byIndex(index) {
        return Tabs.values().find(tab => tab.index === Number(index)) ?? null;
    }

    /**
     * The page a remembered or linked value names: its key, or the index an older version of Palantir remembered.
     *
     * @param value the key or index
     * @returns {PalantirDashboardTab|null} the page, or null when the value names none
     */
    static of(value) {
        if (value instanceof PalantirDashboardTab) {
            return value;
        }
        const text = String(value ?? "").trim();
        return /^\d+$/.test(text) ? Tabs.byIndex(text) : Tabs.byKey(text);
    }
}

/**
 * Showing pages by what they are rather than by their place.
 */
class PalantirPages {
    static item(tab) {
        return $(`nav.sidebar-navigation ul li[data-page="${Tabs.of(tab)?.key}"]`);
    }

    /**
     * Whether a page is offered here: its rail item is on the page and not put away.
     *
     * @param tab the page
     * @returns {boolean} true when the page can be shown
     */
    static available(tab) {
        const item = PalantirPages.item(tab);
        return item.length > 0 && item.is(":visible");
    }

    /**
     * Show a page, as clicking its rail item does.
     *
     * @param tab the page, or its key
     */
    static open(tab) {
        PalantirPages.item(tab).trigger("click");
    }
}

/**
 * Links to what Palantir shows, to paste into a ticket or a message: #applications for a page, #applications/42 for
 * an application and #users/casuser for a user. Showing a page or an application puts its link in the address bar,
 * as an entry of the browser history, so Back and Forward move between them; opening a link, or going Back or
 * Forward, shows what it names.
 */
class PalantirLinks {
    static following = null;

    static DETAILS = {
        applications: id => navigateToApplication(id),
        users: username => lookUpPalantirUser(username)
    };

    static CLEARED = {
        users: () => {
            if (typeof PalantirUserLookup !== "undefined") {
                PalantirUserLookup.clear();
            }
        }
    };

    static CANCELLED = {
        applications: () => {
            palantirApplicationNavigation++;
            if (typeof clearApplicationRowSelection === "function") {
                clearApplicationRowSelection();
            }
        }
    };

    /**
     * What a link names: the page and what on it, if anything.
     *
     * @param hash the fragment of the link
     * @returns {{tab: PalantirDashboardTab|null, detail: string|null}} the page and the detail
     */
    static parse(hash) {
        const text = String(hash ?? "").replace(/^#/, "");
        const separator = text.indexOf("/");
        const page = separator < 0 ? text : text.slice(0, separator);
        let detail = separator < 0 ? "" : text.slice(separator + 1);
        try {
            detail = decodeURIComponent(detail);
        } catch (error) {
            detail = "";
        }
        const tab = Tabs.byKey(page);
        return {tab: tab && tab.index < Tabs.SETTINGS.index ? tab : null, detail: detail.trim() === "" ? null : detail};
    }

    static fragment(tab, detail = null) {
        return `#${Tabs.of(tab).key}${detail === null || detail === undefined || detail === "" ? "" : `/${encodeURIComponent(String(detail))}`}`;
    }

    /**
     * The full link to a page, or to something on it.
     *
     * @param tab the page
     * @param detail what on the page, such as an application id, if anything
     * @returns {string} the link
     */
    static url(tab, detail = null) {
        return `${location.origin}${location.pathname}${location.search}${PalantirLinks.fragment(tab, detail)}`;
    }

    /**
     * Put what is shown in the address bar, unless it is what a link being followed shows, which is already there. Any
     * other page or detail shown meanwhile, such as the operator opening another page while a user is still being
     * looked up, takes over from that link, so the address follows what is on screen.
     *
     * @param tab the page shown
     * @param detail what on it, if anything
     */
    static shown(tab, detail = null) {
        const page = Tabs.of(tab);
        if (!page || page.index >= Tabs.SETTINGS.index) {
            return;
        }
        const following = PalantirLinks.following;
        if (following !== null) {
            if (following.tab === page && (detail === null || String(detail) === following.detail)) {
                return;
            }
            PalantirLinks.following = null;
        }
        const current = PalantirLinks.parse(location.hash);
        if (detail === null && current.tab === page) {
            return;
        }
        const fragment = PalantirLinks.fragment(page, detail);
        if (location.hash === "" || location.hash === "#") {
            history.replaceState(null, "", fragment);
        } else if (location.hash !== fragment) {
            history.pushState(null, "", fragment);
        }
    }

    /**
     * Show what a link names: its page, then the application or user it names on that page. A link to a page that
     * names nothing on it, such as going Back to #users after looking someone up, leaves nothing named shown, and
     * stops what was still on its way to being shown there, such as an application whose page was still being read,
     * and selects nothing there, such as the application that was selected.
     * A link another navigation took over from while its page was being prepared, such as the operator opening another
     * page, stops there instead of bringing its page back.
     *
     * @param hash the fragment of the link
     * @returns {Promise<boolean>} true when the link named a page that is offered here
     */
    static async follow(hash) {
        const {tab, detail} = PalantirLinks.parse(hash);
        if (!tab || !PalantirPages.available(tab)) {
            return false;
        }
        const following = {tab, detail};
        PalantirLinks.following = following;
        if (detail === null && PalantirLinks.CANCELLED[tab.key]) {
            PalantirLinks.CANCELLED[tab.key]();
        }
        try {
            if (currentActiveTab !== tab.index || !isDashboardTabInitialized(tab)) {
                PalantirPages.open(tab);
            }
            await initializeDashboardTab(tab);
            if (PalantirLinks.following !== following) {
                return true;
            }
            if (detail !== null && PalantirLinks.DETAILS[tab.key]) {
                await PalantirLinks.DETAILS[tab.key](detail);
            } else if (detail === null && PalantirLinks.CLEARED[tab.key]) {
                PalantirLinks.CLEARED[tab.key]();
            }
        } finally {
            if (PalantirLinks.following === following) {
                PalantirLinks.following = null;
            }
        }
        return true;
    }

    /**
     * Copy the link to a page, or to something on it, and say so.
     *
     * @param tab the page
     * @param detail what on the page, if anything
     * @returns {Promise<void>} resolved once copied
     */
    static async copy(tab, detail = null) {
        const link = PalantirLinks.url(tab, detail);
        try {
            await navigator.clipboard.writeText(link);
            notyf.success("Link copied.");
        } catch (error) {
            console.debug("The link could not be copied", error);
            await Swal.fire({titleText: "Copy This Link", input: "text", inputValue: link, inputAttributes: {readonly: "true"}});
        }
    }

    static bind() {
        window.removeEventListener("popstate", PalantirLinks.onPopState);
        window.addEventListener("popstate", PalantirLinks.onPopState);
    }

    /**
     * Follow Back and Forward, unless a dialog is open over the page, such as an application being edited: the page
     * under it stays, and so does its link.
     */
    static onPopState() {
        const shown = Tabs.byIndex(currentActiveTab);
        if ($(".ui-dialog:visible").length > 0 && shown) {
            history.pushState(null, "", PalantirLinks.fragment(shown));
            return;
        }
        PalantirLinks.follow(location.hash);
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
 * A tab whose scripts could not be fetched is not initialized at all, and not marked as initialized, so going back to
 * it tries again instead of leaving its controls unbound.
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
        try {
            await PalantirScripts.forPage(tabIndex);
        } catch (e) {
            PalantirScripts.failed(e);
            palantirTabInitialization.delete(tabIndex);
            return;
        }
        initializePalantirWidgets(document.getElementById(`attribute-tab-${tabIndex}`));
        const shownEarly = showStoredTabs();
        for (const initializer of initializers) {
            try {
                await initializer();
            } catch (e) {
                console.error(`Unable to initialize tab ${tabIndex}:`, e);
            }
        }
        await restoreActiveTabs(shownEarly);
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
                        my: "center",
                        at: "center",
                        of: window
                    },
                    autoOpen: false,
                    modal: true,
                    width: Math.min(850, $(window).width() - 32),
                    height: Math.min(760, $(window).height() - 48),
                    buttons: {
                        OK: function () {
                            let palantirSettings = {};
                            try {
                                const stored = JSON.parse(localStorage.getItem(PALANTIR_SETTINGS_KEY) ?? "{}");
                                palantirSettings = stored !== null && typeof stored === "object" && !Array.isArray(stored) ? stored : {};
                            } catch (error) {
                                console.debug("Palantir replaces settings it could not read", error);
                            }
                            palantirSettings.refreshInterval = Number($("#palantirRefreshInterval").val());
                            try {
                                localStorage.setItem(PALANTIR_SETTINGS_KEY, JSON.stringify(palantirSettings));
                            } catch (error) {
                                console.debug("Palantir could not keep the settings", error);
                            }
                            forgetPalantirSettings();
                            PalantirTheme.apply($("#palantirTheme").val());
                            $(this).dialog("close");
                            PalantirPolling.beat();
                        },
                        Cancel: function () {
                            $(this).dialog("close");
                        }
                    },
                    open: function () {
                        initializePalantirWidgets(this);
                        $("#palantir-settings-tabs")
                            .off("tabsactivate.palantirSettings")
                            .on("tabsactivate.palantirSettings", (event, ui) => {
                                if (ui.newPanel.is("#palantirEndpoints-tab")
                                    && $.fn.dataTable.isDataTable("#palantirActuatorsTable")) {
                                    $("#palantirActuatorsTable").DataTable().columns.adjust();
                                }
                            });
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
                        if ($("#palantirTheme").selectmenu("instance")) {
                            $("#palantirTheme").selectmenu("destroy");
                        }
                        $("#palantirTheme").selectmenu({
                            appendTo: $(this).closest(".ui-dialog")
                        });
                        $("#palantirTheme")
                            .val(PalantirTheme.preference())
                            .selectmenu("refresh");
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
                PalantirCsrf.addTo(form);
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
                    if (currentActiveTab === tabIndex) {
                        PalantirPanelFocus.into(`#attribute-tab-${tabIndex}`);
                    }
                    if (currentActiveTab === Tabs.HOME.index && typeof PalantirHome !== "undefined") {
                        PalantirHome.activated();
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
        const page = Tabs.byIndex(index);
        try {
            window.localStorage.setItem("PalantirSelectedTab", page?.key ?? index);
            window.sessionStorage.setItem("PalantirSelectedTab", page?.key ?? index);
        } catch (error) {
            console.debug("The current page is not kept for a reload", error);
        }
        PalantirLinks.shown(page);
        updatePalantirNavigationGroups();
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
    if (!PalantirAvailability.credentialRepositories()) {
        hideElements($("#mfaRepositoriesTab").parent());
    }
    if (!CasActuatorEndpoints.multitenancy() || !CAS_FEATURES.includes("Multitenancy")) {
        hideElements($("#tenantsTabButton"));
    }
    if (!CasActuatorEndpoints.clusterTopology()) {
        hideElements($("#clusterTabButton"));
        hideElements($(`#attribute-tab-${Tabs.CLUSTER.index}`));
    }
    if (!PalantirAvailability.userLookup()) {
        hideElements($("#usersTabButton"));
        hideElements($(`#attribute-tab-${Tabs.USERS.index}`));
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
 * Show the inner jQuery UI tab selections the operator last used as soon as their containers exist, before the page
 * is initialized, so a reload does not show the first entry while the page's data is read. Only entries already on
 * screen are selected, and nothing is reset or written back; restoreActiveTabs finishes the job once the page is
 * initialized.
 *
 * @returns {Set<string>} the ids of the containers whose selection was changed
 */
function showStoredTabs() {
    const shown = new Set();
    let storedTabs;
    try {
        storedTabs = JSON.parse(localStorage.getItem("ActiveTabs") ?? "{}") ?? {};
    } catch (error) {
        console.debug("The last inner pages cannot be read from the browser", error);
        return shown;
    }
    const pending = new Map(Object.entries(storedTabs));
    let progressed = true;
    while (progressed && pending.size > 0) {
        progressed = false;
        for (const [key, value] of [...pending]) {
            const tabs = $(`#${key}`);
            if (tabs.length === 0 || !tabs.data("ui-tabs") || !tabs.is(":visible")) {
                continue;
            }
            pending.delete(key);
            const index = palantirInnerTabIndex(tabs, value);
            if (index >= 0 && tabs.tabs("option", "active") !== index && tabs.find("> ul > li").eq(index).is(":visible")) {
                tabs.tabs("option", "active", index);
                shown.add(key);
                progressed = true;
            }
        }
    }
    return shown;
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
 * A container that showStoredTabs already switched, before the page's own handlers were bound, is told again that its
 * entry was activated, so the page loads what that entry shows just as when the selection is restored here.
 *
 * @param {Set<string>} shownEarly the ids of the containers switched before the page was initialized
 * @returns {Promise<void>} resolved once the stored selections have been applied
 */
async function restoreActiveTabs(shownEarly = new Set()) {
    let storedTabs = {};
    try {
        storedTabs = JSON.parse(localStorage.getItem("ActiveTabs") ?? "{}") ?? {};
    } catch (error) {
        console.debug("The last inner pages cannot be read from the browser", error);
    }
    const pending = new Map(Object.entries(storedTabs));

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
            const index = palantirInnerTabIndex(tabs, value);
            const entry = index >= 0 ? tabs.find("> ul > li").eq(index) : $();
            if (shownEarly.has(key) && entry.is(":visible") && tabs.tabs("option", "active") === index) {
                const panel = $(`#${entry.attr("aria-controls")}`);
                tabs.trigger("tabsactivate", [{newTab: entry, newPanel: panel, oldTab: $(), oldPanel: $()}]);
            } else if (entry.is(":visible")) {
                tabs.tabs("option", "active", index);
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
    updatePalantirNavigationGroups();
}

let palantirApplicationNavigation = 0;

/**
 * Select a registered service on the applications tab, revealing that tab first. The applications are read again
 * when the service is not among them yet, such as when it was just saved. When the operator moves to another page, or
 * to another application, while the applications are prepared or read, nothing is selected and the address is left to
 * the newer navigation.
 *
 * @param serviceIdToFind the registered service id to look for
 */
async function navigateToApplication(serviceIdToFind) {
    const navigation = ++palantirApplicationNavigation;
    if (currentActiveTab !== Tabs.APPLICATIONS.index) {
        PalantirPages.open(Tabs.APPLICATIONS);
    }
    const stillShown = () => currentActiveTab === Tabs.APPLICATIONS.index && navigation === palantirApplicationNavigation;
    await initializeDashboardTab(Tabs.APPLICATIONS);
    if (!stillShown() || typeof fetchServices !== "function") {
        return;
    }
    const tabs = $("#applications-tabs");
    if (tabs.data("ui-tabs")) {
        tabs.tabs("option", "active", 0);
    }
    const id = String(serviceIdToFind);
    if (!registeredServicesById.has(id)) {
        await fetchServices();
        if (!stillShown()) {
            return;
        }
    }
    if (!revealApplicationRow(id)) {
        displayBanner(`Could not find a registered service with id ${id}`);
        return;
    }
    requestAnimationFrame(() => $(`#applicationsCards .palantir-application-card[data-service-id="${CSS.escape(id)}"]:visible`)
        .first().each((index, card) => card.scrollIntoView({block: "center"})));
    PalantirLinks.shown(Tabs.APPLICATIONS, id);
}

/**
 * Watch the Palantir session and reload once it has expired. An expired session answers 401 or 403, or redirects to the
 * login page; the redirect is not followed, since the login page would answer 200. A server that cannot be reached or
 * answers with an error, such as while it restarts, says nothing about the session, so the check simply tries again.
 */
async function initializePalantirSession() {
    let expired = false;
    PalantirPolling.every("session", async () => {
        if (expired) {
            return;
        }
        const url = retrieveDashboardUrl();
        let result;
        try {
            result = await fetch(`${url}/session`, {credentials: "include", redirect: "manual", headers: {"Accept": "application/json"}});
        } catch (error) {
            console.debug("The session could not be checked just now", error);
            return;
        }
        const ended = result.type === "opaqueredirect" || result.status === 401 || result.status === 403
            || (result.status >= 300 && result.status < 400);
        if (ended) {
            expired = true;

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
    }, {every: 15000});
}

/**
 * Open the Users page and look up the given user, as the command palette does.
 *
 * @param username the user to look up
 * @returns {Promise<void>} resolved once the lookup has finished
 */
async function lookUpPalantirUser(username) {
    if (currentActiveTab !== Tabs.USERS.index || !isDashboardTabInitialized(Tabs.USERS)) {
        PalantirPages.open(Tabs.USERS);
    }
    await initializeDashboardTab(Tabs.USERS);
    if (typeof PalantirUserLookup === "undefined") {
        return;
    }
    $("#userLookupUsername").val(username);
    await PalantirUserLookup.lookUp(username);
}
