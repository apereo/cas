/* global $, CAS_FEATURES, CasActuatorEndpoints, PalantirDashboardConfiguration,
   RegisteredServiceSections, openRegisteredServiceWizardDialog, isPopulatingWizard,
   activateDashboardTab, ace, currentActiveTab, registeredServicesById, PalantirUserLookup,
   PalantirServiceHistory, Swal, applicationActionHandler, applicationRowContext, fetchServices, notyf,
   PalantirSamlMetadataImport, PalantirApplicationAccess, Tabs, PalantirServiceEditor, PalantirServiceTemplates,
   PalantirCommandPalette, Chart, PalantirApplicationSelection, PalantirHome, PalantirModules, signInsChart,
   topApplicationsChart */
const assert = require("assert");
const fs = require("fs");
const os = require("os");
const path = require("path");
const cas = require("../../cas.js");

const DASHBOARD_URL = "https://localhost:8443/cas/palantir/dashboard";

const TABS = [
    {index: 0, label: "Applications", button: "#applicationsTabButton", tabs: "applications-tabs", tables: ["applicationsTable", "entityHistoryTable"]},
    {index: 1, label: "System", button: "#systemTabButton", tabs: "system-tabs", tables: ["systemTable", "httpRequestMappingsTable"]},
    {index: 2, label: "Tickets", button: "#ticketsTabButton", tabs: "tickets-tabs", tables: []},
    {index: 3, label: "Tasks", button: "#tasksTabButton", tabs: "tasks-tabs", tables: ["scheduledTasksTable", "threadDumpTable"]},
    {index: 4, label: "Access Strategy", button: "#accessStrategyTabButton", tabs: "accessstrategy-tabs", tables: ["accessStrategyAttributesTable"]},
    {index: 5, label: "Logs & Audits", button: "#loggingTabButton", tabs: "logging-tabs", tables: ["loggersTable", "auditEventsTable", {id: "casEventsTable", requires: "events"}]},
    {index: 6, label: "SSO Sessions", button: "#ssoSessionsTabButton", tabs: "ssosessions-tabs", tables: ["ssoSessionsTable", "springSessionsTable"]},
    {index: 7, label: "Configuration", button: "#configurationTabButton", tabs: "configuration-tabs", tables: ["configurationTable", "configPropsTable"]},
    {index: 8, label: "Attribute Resolution", button: "#personDirectoryTabButton", tabs: "attributes-tab", tables: ["attributeDefinitionsTable", "attributeRepositoriesTable"]},
    {index: 9, label: "Authentication", button: "#authenticationTabButton", tabs: "authentication-tabs", tables: ["authenticationHandlersTable"]},
    {index: 10, label: "Consent", button: "#consentTabButton", tabs: null, tables: [{id: "consentTable", requires: "attributeConsent"}]},
    {index: 11, label: "Protocols", button: "#protocolsTabButton", tabs: "protocols-tabs", tables: []},
    {index: 12, label: "Throttles", button: "#throttlesTabButton", tabs: null, tables: ["throttlesTable"]},
    {index: 13, label: "MFA", button: "#mfaTabButton", tabs: "mfa-tabs", tables: ["mfaDevicesTable"]},
    {index: 14, label: "Multitenancy", button: "#tenantsTabButton", tabs: "tenancy-tabs", tables: ["tenantsTable"]},
    {index: 15, label: "Cluster", button: "#clusterTabButton", tabs: "cluster-tabs", tables: []},
    {index: 16, label: "Users", button: "#usersTabButton", tabs: null, tables: []},
    {index: 17, label: "Home", button: "#homeTabButton", tabs: null, tables: []}
];

const PROTOCOL_FIELDS = {
    OidcRegisteredService: ["#registeredServiceClientId", "input.registered-service-client-secret-value",
        {selector: "#registeredServiceAllowedResources", advanced: true}],
    OAuthRegisteredService: ["#registeredServiceClientId", "input.registered-service-client-secret-value",
        {selector: "#registeredServiceAllowedResources", advanced: true}],
    SamlRegisteredService: ["#registeredServiceMetadataLocation"],
    WSFederationRegisteredService: ["#registeredServiceWsFederationRealm"],
    CasRegisteredService: [
        {selector: "#registeredServiceSupportedProtocols", advanced: true},
        {selector: "#registeredServiceRedirectUrl", advanced: true}
    ]
};

/**
 * Record every uncaught error and console error for the whole run.
 *
 * Tabs, widgets and wizard sections are all built on demand now, and a builder that throws is
 * caught and logged rather than surfaced, so the console is the only place a broken panel shows up.
 *
 * @param page the puppeteer page
 * @returns {string[]} the live array failures are appended to
 */
function trackFailures(page) {
    const failures = [];
    page.on("pageerror", (error) => failures.push(`pageerror: ${error.message}`));
    page.on("console", (message) => {
        if (message.type() === "error") {
            const url = message.location()?.url;
            failures.push(url ? `console: ${message.text()} [${url}]` : `console: ${message.text()}`);
        }
    });
    page.on("response", (response) => {
        if (response.status() >= 400) {
            failures.push(`response: ${response.status()} ${response.request().method()} ${response.url()}`);
        }
    });
    return failures;
}

/**
 * Wait until the dashboard has finished starting up.
 *
 * The rail is only populated once the feature catalog resolves, so a visible dashboard means
 * initializePalantir got past the point where it decides which tabs exist.
 *
 * @param page the puppeteer page
 */
async function waitForDashboard(page) {
    await page.waitForFunction(() => {
        const dashboard = document.querySelector("#dashboard");
        return dashboard !== null
            && !dashboard.classList.contains("d-none")
            && typeof CAS_FEATURES !== "undefined"
            && typeof openRegisteredServiceWizardDialog === "function";
    }, {timeout: 60000});
    const features = await page.evaluate(() => CAS_FEATURES.length);
    await cas.logg(`Dashboard ready with ${features} CAS features`);
    assert(features > 0);
}

/**
 * Open a tab and assert that activating it built everything that tab owns.
 *
 * This is the check that matters for on-demand initialization: the panel has to become visible, its
 * jQuery UI tab set has to become a real widget, and every data table the tab's initializer creates
 * has to exist. A tab whose rail entry is hidden is not enabled in this deployment and is skipped.
 *
 * @param page the puppeteer page
 * @param tab an entry of TABS
 * @returns {Promise<boolean>} true when the tab was present and verified
 */
async function verifyTab(page, tab) {
    const visible = await page.evaluate((button) => $(button).is(":visible"), tab.button);
    if (!visible) {
        await cas.logy(`Tab ${tab.label} is not enabled in this deployment; skipping`);
        return false;
    }

    await page.evaluate((button) => $(button).trigger("click"), tab.button);
    await page.waitForFunction((index) => {
        const panel = document.querySelector(`#attribute-tab-${index}`);
        return panel !== null && !panel.classList.contains("d-none");
    }, {timeout: 30000}, tab.index);

    await page.waitForFunction((entry) => {
        const tabsReady = entry.tabs === null || $(`#${entry.tabs}`).data("ui-tabs") !== undefined;
        return tabsReady && entry.tables
            .map((table) => typeof table === "string" ? {id: table} : table)
            .filter((table) => !table.requires || CasActuatorEndpoints[table.requires]())
            .every((table) => $.fn.dataTable.isDataTable(`#${table.id}`));
    }, {timeout: 60000}, tab);

    await cas.logg(`Tab ${tab.label} initialized`);
    return true;
}

/**
 * Walk every tab on the rail, then return to Applications.
 *
 * @param page the puppeteer page
 */
async function verifyTabs(page) {
    await cas.separator();
    let verified = 0;
    for (const tab of TABS) {
        if (await verifyTab(page, tab)) {
            verified += 1;
        }
    }
    await cas.logg(`Verified ${verified} dashboard tabs`);
    assert(verified >= 2);
    await page.evaluate(() => $("#applicationsTabButton").trigger("click"));
    await cas.screenshot(page);
}

/**
 * Assert the feature catalog draws a card per feature and that its search narrows the cards.
 *
 * Searching for one of the modules on screen must keep only cards that name it, and a query nothing
 * matches must leave no card and show the empty state.
 *
 * @param page the puppeteer page
 */
async function verifyFeatureCatalog(page) {
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.casFeatures()))) {
        await cas.logy("Feature catalog endpoint is not available; skipping");
        return;
    }
    await page.evaluate(() => {
        $("#systemTabButton").trigger("click");
        $("#casfeaturestab").trigger("click");
    });
    await page.waitForFunction(() => $("#casFeaturesCatalog .palantir-feature-card").length > 0, {timeout: 30000});
    const total = await page.evaluate(() => $("#casFeaturesCatalog .palantir-feature-card").length);
    const module = await page.evaluate(() => $("#casFeaturesCatalog .palantir-feature-card__modules li").first().text().trim());
    await cas.logg(`Feature catalog shows ${total} features`);

    if (module) {
        await cas.type(page, "#casFeaturesSearch", module);
        const matches = await page.evaluate((query) => $("#casFeaturesCatalog .palantir-feature-card").toArray()
            .map((card) => card.textContent.toLowerCase().includes(query.toLowerCase())), module);
        await cas.logg(`Searching for ${module} keeps ${matches.length} of ${total} features`);
        assert(matches.length > 0 && matches.length <= total && matches.every(Boolean));
    }

    await cas.type(page, "#casFeaturesSearch", "no-such-feature-or-module");
    assert(await page.evaluate(() => $("#casFeaturesCatalog .palantir-feature-card").length === 0
        && $("#casFeaturesEmpty").is(":visible")));
    await page.evaluate(() => $("#casFeaturesSearch").val("").trigger("input"));
    assert(await page.evaluate((count) => $("#casFeaturesCatalog .palantir-feature-card").length === count, total));
    await page.evaluate(() => $("#applicationsTabButton").trigger("click"));
}

/**
 * Assert the applications tab loaded and drew the service registry, and that its toolbar works.
 *
 * The row count is reported rather than asserted: the registry this scenario starts from is not
 * fixed, and only a table that never became a DataTable is a fault. The editor and wizard entries
 * live inside the New menu rather than the toolbar, so the menu is opened to reach them.
 *
 * @param page the puppeteer page
 */
async function verifyHome(page) {
    await cas.separator();
    await page.waitForFunction(() => currentActiveTab === Tabs.HOME.index
        && $("#homeGrid > [data-home-widget='status']").length === 1, {timeout: 30000});
    const home = await page.evaluate(() => ({
        widgets: $("#homeGrid > [data-home-widget]").map((index, element) => element.dataset.homeWidget).get(),
        status: $("#homeGrid .palantir-home__status-title").text(),
        version: PalantirHome.summary.sections.server.version
    }));
    await cas.logg(`Home opens first with ${home.widgets.join(", ")}; ${home.status}; CAS ${home.version}`);
    assert(home.widgets.includes("health"));
    assert(home.widgets.includes("resources"));
    assert(home.widgets.includes("endpoints"));
    await page.waitForFunction(() => Chart.getChart(document.getElementById("homeHealthChart")) !== undefined, {timeout: 10000});
    const slowest = await page.evaluate(() => $("#homeGrid [data-home-widget='endpoints'] tbody tr").length);
    await cas.logg(`Slowest endpoints listed on Home: ${slowest}`);

    await page.click("#homeGrid [data-home-for='resources'][data-home-action='hide']");
    await page.waitForFunction(() => $("#homeGrid > [data-home-widget='resources']").length === 0
        && JSON.parse(localStorage.getItem("PalantirHomeLayout")).hidden.includes("resources"), {timeout: 10000});
    await page.click("#homeCustomize");
    await page.waitForSelector("#homeCustomizeMenu [data-home-action='show'][data-home-for='resources']", {visible: true, timeout: 10000});
    await page.click("#homeCustomizeMenu [data-home-action='show'][data-home-for='resources']");
    await page.waitForFunction(() => $("#homeGrid > [data-home-widget='resources']").length === 1, {timeout: 10000});
    await cas.logg("A Home card can be hidden and shown again");

    await page.click("#homeGrid [data-home-for='resources'][data-home-action='earlier']");
    await page.waitForFunction(() => {
        const order = $("#homeGrid > [data-home-widget]").map((index, element) => element.dataset.homeWidget).get();
        const stored = JSON.parse(localStorage.getItem("PalantirHomeLayout")).order;
        return order.indexOf("resources") < order.indexOf("health") && stored.indexOf("resources") < stored.indexOf("health");
    }, {timeout: 10000});
    await page.click("#homeCustomize");
    await page.click("#homeCustomizeMenu [data-home-action='reset']");
    await page.waitForFunction(() => localStorage.getItem("PalantirHomeLayout") === null
        && $("#homeGrid > [data-home-widget]").first().data("homeWidget") === "status", {timeout: 10000});

    assert(await page.$("#homeGrid .palantir-home__widget-head button:not(.palantir-home__title-link):not([data-home-action])") === null);
    await page.click("#homeGrid [data-home-widget='types'] h3 .palantir-home__title-link");
    await page.waitForFunction(() => currentActiveTab === Tabs.APPLICATIONS.index, {timeout: 30000});
}

async function verifyRegisteredServices(page) {
    await cas.separator();
    await page.waitForFunction(() => $.fn.dataTable.isDataTable("#applicationsTable"), {timeout: 30000});
    const count = await page.evaluate(() => $("#applicationsTable").DataTable().data().count());
    await cas.logg(`Applications table drew ${count} registered services`);

    for (const button of ["newService", "importService", "exportAll", "reloadAll"]) {
        await cas.assertVisibility(page, `#${button}`);
    }

    const views = await page.evaluate(() => ({
        cards: $("#applicationsCards .palantir-application-card").length,
        rows: $("#applicationsTable").DataTable().rows({search: "applied", page: "current"}).count()
    }));
    await cas.logg(`Card view shows ${views.cards} cards for ${views.rows} rows on the table's page`);
    assert(views.cards === views.rows);

    await page.waitForFunction(() => /every minute|^No /.test($("#topApplicationsChartStatus").text()), {timeout: 30000});
    const top = await page.evaluate(() => ({
        status: $("#topApplicationsChartStatus").text(),
        names: topApplicationsChart.data.labels,
        shown: $("#topApplicationsChart").is(":visible"),
        last: $("#applications-tab > *").filter(":visible").last().find("#topApplicationsChart").length === 1
    }));
    await cas.logg(`Top applications: ${top.status} ${top.names.join(", ")}; shown: ${top.shown}`);
    assert(top.shown === top.names.length > 0, "The top applications chart is shown only when it has data");
    assert(!top.shown || top.last, "The top applications chart is the last thing on the applications page");
    const openable = await page.$("#topApplicationsList [data-top-application]:not(.is-gone)");
    if (top.shown && openable !== null) {
        await openable.click();
        await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("instance") !== undefined
            && $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard, {timeout: 30000});
        await cas.logg(`A top application opens: ${await page.evaluate(() => $("#editServiceWizardDialog").dialog("option", "title"))}`);
        await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
        await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});
    }
    await verifyApplicationTypeFilter(page);

    if (count > 0) {
        await page.waitForFunction(() => $("#servicesChartTitle").closest(".palantir-chart-card").is(":visible"), {timeout: 10000});
        await page.evaluate(() => $("[data-palantir-magnify=servicesChart]").trigger("click"));
        await page.waitForFunction(() => {
            const canvas = document.querySelector(".palantir-magnifier canvas");
            return canvas !== null && Chart.getChart(canvas) !== undefined && canvas.getBoundingClientRect().width > 400;
        }, {timeout: 10000});
        await cas.logg("The applications chart opens magnified");
        await page.evaluate(() => $(".palantir-magnifier").dialog("close"));
    }

    await cas.click(page, "#newService");
    await page.waitForFunction(() => $("#newServicePlain").is(":visible")
        && $("#newServiceWizard").is(":visible"), {timeout: 15000});
    await cas.logg("New application menu offers both the editor and the wizard");
}

/**
 * Choose a type in the applications type filter and check that the table and the cards keep only applications of
 * that type, as many as the filter counts, then show all of them again. The filter is hidden while the registry
 * holds a single type, which is only reported.
 *
 * @param page the puppeteer page
 */
async function verifyApplicationTypeFilter(page) {
    const filter = await page.evaluate(() => ({
        visible: $("#applicationsTypeFilter").is(":visible"),
        types: $("#applicationsTypeFilter button[data-application-type]").map(function () {
            return {type: String($(this).data("application-type")), count: Number($(this).find(".palantir-type-filter__count").text())};
        }).get()
    }));
    if (!filter.visible) {
        await cas.logg(`Type filter is hidden for ${filter.types.length - 1} application type(s)`);
        return;
    }
    const chosen = filter.types.find((entry) => entry.type !== "all");
    await cas.click(page, `#applicationsTypeFilter button[data-application-type="${chosen.type}"]`);
    await page.waitForFunction((type) => $(`#applicationsTypeFilter button[data-application-type="${type}"]`).attr("aria-pressed") === "true",
        {timeout: 10000}, chosen.type);
    const shown = await page.evaluate(() => ({
        rows: $("#applicationsTable").DataTable().rows({search: "applied"}).count(),
        badges: $("#applicationsCards .palantir-application-card .palantir-protocol-badge").map(function () {
            return [...this.classList].find((name) => name.startsWith("is-"));
        }).get()
    }));
    await cas.logg(`Type ${chosen.type} shows ${shown.rows} applications and ${shown.badges.length} cards`);
    assert(shown.rows === chosen.count);
    assert(shown.badges.every((badge) => badge === `is-${chosen.type}`));
    await cas.click(page, "#applicationsTypeFilter button[data-application-type=\"all\"]");
    await page.waitForFunction((total) => $("#applicationsTable").DataTable().rows({search: "applied"}).count() === total,
        {timeout: 10000}, filter.types.find((entry) => entry.type === "all").count);
    await cas.logg("Type filter shows all applications again");
}

/**
 * Switch the theme from the navigation bar and back, and check that each choice is applied and remembered.
 *
 * @param page the puppeteer page
 */
async function verifyTheme(page) {
    await cas.separator();
    const readTheme = () => page.evaluate(() => ({
        theme: document.documentElement.getAttribute("data-theme"),
        stored: localStorage.getItem("PalantirTheme")
    }));
    const initial = await readTheme();
    await cas.click(page, "#palantirThemeToggle");
    const switched = await readTheme();
    await cas.logg(`Theme switched from ${initial.theme} to ${switched.theme}`);
    assert(switched.theme !== initial.theme);
    assert(switched.stored === switched.theme);
    await cas.click(page, "#palantirThemeToggle");
    const restored = await readTheme();
    assert(restored.theme === initial.theme);
    assert(restored.stored === initial.theme);
}

/**
 * Walk the wizard's steps with Next, from the first step to the review.
 *
 * @param page the puppeteer page
 * @returns {Promise<string[]>} the steps visited, in order
 */
async function walkWizardSteps(page) {
    return page.evaluate(() => {
        RegisteredServiceSections.start();
        const visited = [RegisteredServiceSections.currentStepKey()];
        while (!$("#nextServiceWizardStep").hasClass("hide") && visited.length < 10) {
            $("#nextServiceWizardStep").trigger("click");
            visited.push(RegisteredServiceSections.currentStepKey());
        }
        return visited;
    });
}

/**
 * Assert that startup does not build the wizard: its fields are built in the background once the dashboard is shown
 * and the browser is idle, so they either do not exist yet or were built after the dashboard appeared.
 *
 * @param page the puppeteer page
 */
async function verifyWizardIsDeferred(page) {
    const built = await page.evaluate(() => {
        const shown = performance.getEntriesByName("palantir-dashboard-shown")[0]?.startTime ?? null;
        const wizard = performance.getEntriesByName("palantir-wizard-built")[0]?.startTime ?? null;
        return {
            shown: shown,
            wizard: wizard,
            params: document.querySelectorAll("#editServiceWizardForm [data-param-name]:not(input[type='hidden'][data-switch-btn])").length
        };
    });
    await cas.logg(`Dashboard shown at ${built.shown} ms; wizard built at ${built.wizard ?? "not yet"} ms; dynamic fields=${built.params}`);
    assert(built.shown !== null);
    if (built.wizard === null) {
        assert(built.params === 0);
    } else {
        assert(built.wizard > built.shown);
    }
}

/**
 * Open the wizard for one service type and check what it built.
 *
 * Every section the wizard knows about is checked against whether it should be available for this
 * service type, this advanced-options setting and the deployment's feature catalog; each available
 * section must have a sidebar entry that reveals its panel; and each protocol field must exist
 * exactly once and be reachable inside the Protocol group when its advanced setting permits it.
 *
 * @param page the puppeteer page
 * @param serviceClass the registered service class to open the wizard for
 * @param hideAdvanced whether advanced options are collapsed for this pass
 * @returns {Promise<string[]>} the problems found, empty when the wizard is correct
 */
async function inspectWizard(page, serviceClass, hideAdvanced) {
    const name = serviceClass.split(".").pop();
    await page.evaluate((entry) => {
        if ($("#hideAdvancedOptions").val() !== String(entry.hideAdvanced)) {
            $("#hideAdvancedOptionsButton").trigger("click");
        }
    }, {hideAdvanced});

    return page.evaluate((entry) => {
        const errors = [];
        const featureSections = {
            registeredServiceAcceptableUsagePolicy: "AcceptableUsagePolicy",
            registeredServiceWebflowInterruptPolicy: "InterruptNotifications",
            registeredServicePasswordlessPolicy: "PasswordlessAuthn",
            registeredServiceSurrogatePolicy: "SurrogateAuthentication"
        };
        const expected = (entry.protocolFields ?? [])
            .map((field) => typeof field === "string" ? {selector: field} : field);
        const seen = new Set();

        for (const section of RegisteredServiceSections.sections()) {
            const classes = [...section.header.classList];
            const protocolClasses = classes.filter((value) => value.startsWith("class-"));
            const feature = featureSections[section.header.id];
            const shouldExist = (protocolClasses.length === 0 || protocolClasses.includes(`class-${entry.name}`))
                && (!entry.hideAdvanced || !classes.includes("advanced-option"))
                && (!feature || CAS_FEATURES.includes(feature))
                && (section.header.id !== "registeredServiceMfaPolicy"
                    || PalantirDashboardConfiguration.availableMultifactorProviders().length > 0);

            if (RegisteredServiceSections.isAvailable(section) !== shouldExist) {
                errors.push(`Incorrect availability: ${section.key}`);
            }
            if (!RegisteredServiceSections.GROUP_ORDER.includes(RegisteredServiceSections.groupOf(section))) {
                errors.push(`Ungrouped section: ${section.key}`);
            }
            if (!RegisteredServiceSections.isAvailable(section)) {
                continue;
            }

            const link = document.querySelector(`[data-section-key="${section.key}"]`);
            if (!link) {
                errors.push(`Missing sidebar entry: ${section.key}`);
                continue;
            }
            link.click();
            if (!$(section.panel).is(":visible")) {
                errors.push(`Hidden selected panel: ${section.key}`);
            }
            for (const {selector} of expected) {
                if ($(section.panel).find(selector).is(":visible")) {
                    seen.add(selector);
                    if (RegisteredServiceSections.groupOf(section) !== "Protocol") {
                        errors.push(`Protocol field in wrong group: ${selector}`);
                    }
                }
            }
            $(section.panel).find("[class*='class-']:visible").each((_, element) => {
                if (![...element.classList].includes(`class-${entry.name}`)
                    && !element.classList.contains("always-show")) {
                    errors.push(`Wrong protocol field: ${element.id}`);
                }
            });
        }

        for (const {selector, advanced} of expected) {
            if (document.querySelectorAll(`#editServiceWizardForm ${selector}`).length !== 1) {
                errors.push(`Missing or duplicate protocol field: ${selector}`);
            }
            if (entry.hideAdvanced && advanced) {
                if (seen.has(selector)) {
                    errors.push(`Visible advanced protocol field: ${selector}`);
                }
            } else if (!seen.has(selector)) {
                errors.push(`Unreachable protocol field: ${selector}`);
            }
        }
        if ($("input[data-param-name='properties'][data-mapped-field-role='key']").first().val() !== "wizard.setting") {
            errors.push("Saved properties were not populated");
        }
        return errors;
    }, {name, hideAdvanced, protocolFields: PROTOCOL_FIELDS[name]});
}

/**
 * Pick an entry in a jQuery UI selectmenu through the widget's own menu.
 *
 * The open menu is driven with its focus and select methods rather than a click at the item's
 * coordinates: the menu sits in the dialog, which can scroll under a coordinate click and leave the
 * choice unmade. Selecting through the menu still runs the selectmenu's change callback, which is
 * what the wizard relies on to reveal a type's fields.
 *
 * @param page the puppeteer page
 * @param selectId the id of the underlying select element
 * @param text the label of the entry to choose
 */
async function chooseDropdownOption(page, selectId, text) {
    const chosen = await page.evaluate((entry) => {
        const $select = $(`#${entry.selectId}`);
        $select.selectmenu("open");
        const $menu = $select.selectmenu("menuWidget");
        const $item = $menu.find("li").filter((_, item) => item.textContent.trim() === entry.text).first();
        if ($item.length === 0) {
            $select.selectmenu("close");
            return null;
        }
        $menu.menu("focus", null, $item);
        $menu.menu("select", $.Event("click", {target: $item[0]}));
        return $select.val();
    }, {selectId, text});
    assert(chosen, `${selectId} has no ${text} entry`);
}

/**
 * Assert the wizard's type dropdowns react to a choice.
 *
 * The wizard's fields are built after the Applications panel has set up its widgets, so each dropdown
 * that declares change handlers must still carry them; choosing a username attribute provider type
 * must reveal that type's fields, and going back to the default must hide them again.
 *
 * @param page the puppeteer page
 * @param name the registered service type under test, for the log
 */
async function verifyWizardDropdowns(page, name) {
    const unbound = await page.evaluate(() => {
        RegisteredServiceSections.activate("editServiceWizardMenuItemUsernameAttribute");
        return $("#editServiceWizardForm select.jqueryui-selectmenu")
            .filter((_, select) => $(select).data("change-handler")
                && typeof $(select).selectmenu("option", "change") !== "function")
            .map((_, select) => select.id)
            .get();
    });
    assert(unbound.length === 0, `${name}: dropdowns without change handlers: ${unbound.join(", ")}`);

    const staticFields = "#editServiceWizardMenuItemUsernameAttribute .StaticRegisteredServiceUsernameProvider";
    await chooseDropdownOption(page, "registeredServiceUsernameAttributeProvider", "STATIC");
    await page.waitForFunction((selector) => $(selector).is(":visible"), {timeout: 10000}, staticFields);
    await chooseDropdownOption(page, "registeredServiceUsernameAttributeProvider", "DEFAULT");
    await page.waitForFunction((selector) => !$(selector).is(":visible"), {timeout: 10000}, staticFields);
    await cas.logg(`${name}: wizard dropdowns reveal the fields of the chosen type`);
}

/**
 * Assert the wizard's date fields offer the date picker and that a picked date reaches the definition.
 *
 * The service expiration date takes the day as is; the time-based access strategy gets the start of
 * the picked day for its start and the last second for its end.
 *
 * The picker opens when its field takes focus. When clicking the field does not open it, which has been
 * seen for the second service type the wizard is opened for, the state that decides it (the element
 * under the click, the focused element and the date picker's own bookkeeping) is logged and the picker
 * is opened through its API, so the rest of the check still runs; a field without a date picker fails.
 *
 * @param page the puppeteer page
 * @param name the registered service type under test, for the log
 */
async function verifyWizardDatePickers(page, name) {
    const day = "#ui-datepicker-div td[data-handler='selectDay']";
    const pick = async (selector) => {
        await page.waitForSelector(selector, {visible: true, timeout: 10000});
        await page.click(selector);
        const opened = await page.waitForSelector(day, {visible: true, timeout: 5000}).then(() => true, () => false);
        if (!opened) {
            const state = await page.$eval(selector, (input) => {
                const box = input.getBoundingClientRect();
                const target = document.elementFromPoint(box.left + box.width / 2, box.top + box.height / 2);
                return {
                    hasDatepicker: $(input).hasClass("hasDatepicker"),
                    focused: document.activeElement === input,
                    activeElement: document.activeElement?.id || document.activeElement?.tagName,
                    elementAtClick: target === null ? null : `${target.tagName}#${target.id}.${target.className}`,
                    lastInput: $.datepicker._lastInput?.id ?? null,
                    datepickerShowing: $.datepicker._datepickerShowing
                };
            });
            await cas.logy(`${name}: clicking ${selector} did not open the date picker: ${JSON.stringify(state)}`);
            assert(state.hasDatepicker, `${name}: ${selector} has no date picker`);
            await page.$eval(selector, (input) => $(input).datepicker("show"));
            await page.waitForSelector(day, {visible: true, timeout: 10000});
        }
        await page.click(`${day} a`);
        return page.$eval(selector, (input) => input.value);
    };
    await page.evaluate(() => {
        if ($("#hideAdvancedOptions").val() === "true") {
            $("#hideAdvancedOptionsButton").trigger("click");
        }
        RegisteredServiceSections.activate("editServiceWizardMenuItemExpirationPolicy");
    });
    const expiration = await pick("#registeredServiceExpirationPolicyExpirationDate");
    assert(/^\d{4}-\d{2}-\d{2}$/.test(expiration), `${name}: expiration date ${expiration}`);

    await page.evaluate(() => RegisteredServiceSections.activate("editServiceWizardMenuItemAccessStrategy"));
    await chooseDropdownOption(page, "registeredServiceAccessStrategy", "TIME BASED");
    const starting = await pick("#registeredServiceAccessStrategyTimeBasedStartingDateTime");
    const ending = await pick("#registeredServiceAccessStrategyTimeBasedEndingDateTime");
    assert(starting.endsWith("T00:00:00") && ending.endsWith("T23:59:59"), `${name}: ${starting} to ${ending}`);

    await page.waitForFunction((values) => {
        const definition = ace.edit("wizardServiceEditor").getValue();
        return values.every((value) => definition.includes(value));
    }, {timeout: 10000}, [expiration, starting, ending]);
    await chooseDropdownOption(page, "registeredServiceAccessStrategy", "DEFAULT");
    await page.evaluate(() => $("#registeredServiceExpirationPolicyExpirationDate").val("").trigger("input"));
    await cas.logg(`${name}: date fields pick ${expiration}, ${starting} and ${ending} into the definition`);
}

/**
 * Answer the unsaved-changes question, if closing a dialog asked it, by discarding the changes, which also removes
 * the draft kept for them.
 *
 * @param page the puppeteer page
 * @param name the registered service type under test, for the log
 */
async function discardUnsavedChanges(page, name) {
    await cas.sleep(500);
    const asked = await page.evaluate(() => $(".swal2-popup:visible").length > 0
        && $(".swal2-title").text() === "Discard your changes?");
    if (asked) {
        await page.click(".swal2-deny");
        await cas.logg(`${name}: closing the wizard with changes asked before discarding them`);
    }
}

/**
 * Exercise the service wizard for each service type it models.
 *
 * The wizard's fields are built when the dialog is first opened rather than while the page parses,
 * so this is what proves the deferred construction produces the same form. Each type is opened,
 * inspected with advanced options collapsed and expanded, and dismissed through Cancel.
 *
 * The deployment's supported types come from a classpath scan and can include registered service
 * classes the wizard declares no protocol fields for; those are reported and skipped rather than
 * failed, since there is nothing to assert about them.
 *
 * @param page the puppeteer page
 */
async function verifyServiceWizard(page) {
    await cas.separator();
    await verifyWizardIsDeferred(page);

    const supported = await page.evaluate(() => Object.keys(PalantirDashboardConfiguration.supportedServiceTypes()).sort());
    const types = supported.filter((serviceClass) => PROTOCOL_FIELDS[serviceClass.split(".").pop()] !== undefined);
    const unmodelled = supported.filter((serviceClass) => !types.includes(serviceClass));
    await cas.logg(`Deployment supports ${supported.length} service types; ${types.length} are modelled by the wizard`);
    if (unmodelled.length > 0) {
        await cas.logy(`Not exercised, no protocol fields declared for: ${unmodelled.join(", ")}`);
    }
    assert(types.length > 0);

    for (const serviceClass of types) {
        const name = serviceClass.split(".").pop();
        await page.evaluate((entry) => openRegisteredServiceWizardDialog({
            "@class": entry.serviceClass,
            id: 123456,
            name: entry.name,
            serviceId: "https://wizard.example.org/.*",
            properties: {
                "@class": "java.util.HashMap",
                "wizard.setting": {
                    "@class": "org.apereo.cas.services.DefaultRegisteredServiceProperty",
                    values: ["java.util.HashSet", ["restored value"]]
                }
            }
        }), {serviceClass, name});

        await page.waitForFunction((expected) => !isPopulatingWizard
            && $("#registeredServiceName").val() === expected, {timeout: 30000}, name);

        for (const hideAdvanced of [true, false]) {
            const errors = await inspectWizard(page, serviceClass, hideAdvanced);
            if (errors.length > 0) {
                await cas.logr(`${name} (hideAdvanced=${hideAdvanced}): ${errors.join(", ")}`);
            }
            assert(errors.length === 0, `${name}: ${errors.join(", ")}`);
        }

        await verifyWizardDropdowns(page, name);
        await verifyWizardDatePickers(page, name);

        const steps = await walkWizardSteps(page);
        await cas.logg(`${name} steps: ${steps.join(" > ")}`);
        assert(steps[0] === "basics" && steps.at(-1) === "review" && new Set(steps).size === steps.length);

        await page.evaluate(() => $("#registeredServiceName").val("").trigger("input").trigger("change"));
        await page.waitForFunction(() => $("#saveServiceWizard").prop("disabled")
            && $("#saveServiceWizardHint").text().includes("Name"), {timeout: 10000});
        const required = await page.evaluate(() => $("#saveServiceWizardHint").text().trim());
        await cas.logg(`${name}: without a name, Save is disabled and the wizard says ${required}`);

        await cas.logg(`Wizard verified for ${name}`);
        await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
        await discardUnsavedChanges(page, name);
        await page.waitForFunction(() => $("#editServiceWizardDialog").hasClass("ui-dialog-content")
            && $("#editServiceWizardDialog").dialog("isOpen") === false, {timeout: 15000});
    }
    await cas.screenshot(page);
}

/**
 * The JSON editor opens on a start screen of templates for a new definition: the templates that ship with CAS are
 * offered for every protocol of the deployment, a search narrows them, and choosing one moves to the editor with a
 * definition CAS can read. A syntax error is reported on its line, and Cancel asks before discarding the edits.
 *
 * @param page the puppeteer page
 */
async function verifyPlainEditorDialog(page) {
    await cas.separator();
    await cas.click(page, "#newServicePlain");
    await page.waitForFunction(() => $("#editServiceDialog").hasClass("ui-dialog-content")
        && $("#editServiceDialog").dialog("isOpen")
        && PalantirServiceEditor.view === "start"
        && $("#serviceTemplateCards [data-template-key]").length > 1, {timeout: 30000});
    const start = await page.evaluate(() => ({
        templates: PalantirServiceTemplates.all().length,
        protocols: [...new Set(PalantirServiceTemplates.all().map((template) => template.protocol.key))],
        continueDisabled: $("#continueServiceTemplate").prop("disabled")
    }));
    await cas.logg(`JSON editor offers ${start.templates} template(s) for ${start.protocols.join(", ")}`);
    assert(start.templates >= 20);
    assert(start.protocols.includes("cas") && start.protocols.includes("saml") && start.protocols.includes("oidc"));
    assert(start.continueDisabled === true);

    await page.type("#serviceTemplateSearch", "proxy tickets");
    await page.waitForFunction(() => $("#serviceTemplateCards [data-template-key]").length === 2, {timeout: 10000});
    await page.click("#serviceTemplateCards [data-template-key]:not([data-template-key=blank])");
    await page.click("#continueServiceTemplate");
    await page.waitForFunction(() => PalantirServiceEditor.view === "editor"
        && $("#serviceEditorCheck").attr("data-state") === "ok", {timeout: 10000});
    const definition = await page.evaluate(() => JSON.parse(ace.edit("serviceEditor").getValue()));
    await cas.log(`Started from the template ${definition.name}`);
    assert(definition.proxyPolicy !== undefined);
    assert(definition.id === undefined);

    await cas.click(page, "#validateService");
    await page.waitForFunction(() => !$("#serviceEditorProblem").prop("hidden"), {timeout: 15000});
    const validation = await page.evaluate(() => $("#serviceEditorProblem").attr("data-state"));
    await cas.logg(`Validating the template answers ${validation}`);
    assert(validation === "ok");

    await page.evaluate(() => ace.edit("serviceEditor").session.insert({row: 2, column: 0}, "    \"broken\"\n"));
    await page.waitForFunction(() => $("#serviceEditorCheck").attr("data-state") === "error", {timeout: 10000});
    const syntax = await page.evaluate(() => $("#serviceEditorCheck").text());
    await cas.logg(`A syntax error is reported as: ${syntax}`);
    assert(syntax.startsWith("Line "));

    await page.evaluate(() => $("#cancelService").trigger("click"));
    await discardUnsavedChanges(page, "JSON editor");
    await page.waitForFunction(() => $("#editServiceDialog").dialog("isOpen") === false, {timeout: 15000});
    await cas.logg("JSON editor dismissed through Cancel");
}

/**
 * Open the settings dialog, which builds the actuator table the first time it is shown.
 *
 * @param page the puppeteer page
 */
async function verifySettingsDialog(page) {
    await cas.separator();
    await page.evaluate(() => activateDashboardTab(100));
    await page.waitForFunction(() => $("#palantirSettingsDialog").hasClass("ui-dialog-content")
        && $.fn.dataTable.isDataTable("#palantirActuatorsTable")
        && $("#palantirActuatorsTable").DataTable().data().count() > 0, {timeout: 30000});
    const endpoints = await page.evaluate(() => $("#palantirActuatorsTable").DataTable().data().count());
    await cas.logg(`Settings dialog lists ${endpoints} actuator endpoints`);

    await page.evaluate(() => sessionStorage.setItem("PalantirResetProbe", "kept"));
    await cas.click(page, "#resetPalantirPreferences");
    await page.waitForFunction(() => $(".swal2-title").text() === "Reset all preferences?", {timeout: 10000});
    await page.click(".swal2-cancel");
    await page.waitForFunction(() => !Swal.isVisible(), {timeout: 10000});
    const kept = await page.evaluate(() => sessionStorage.getItem("PalantirResetProbe"));
    await cas.logg(`Declining to reset the preferences keeps them: ${kept}`);
    assert(kept === "kept");
    await page.evaluate(() => sessionStorage.removeItem("PalantirResetProbe"));
    await page.evaluate(() => $("#palantirSettingsDialog").dialog("close"));
}

/**
 * Wait for the command palette to open with its search field focused, then type a query.
 *
 * @param page the puppeteer page
 * @param query what to type
 */
async function searchCommandPalette(page, query) {
    try {
        await page.waitForFunction(() => $("#palantirCommandPalette").dialog("instance") !== undefined
            && $("#palantirCommandPalette").dialog("isOpen")
            && document.activeElement?.id === "palantirCommandPaletteInput", {timeout: 10000});
    } catch (error) {
        const state = await page.evaluate(() => ({
            open: $("#palantirCommandPalette").dialog("instance") !== undefined && $("#palantirCommandPalette").dialog("isOpen"),
            focused: `${document.activeElement?.tagName}#${document.activeElement?.id}.${document.activeElement?.className}`,
            dialogs: $(".ui-dialog:visible .ui-dialog-title").toArray().map((title) => title.textContent),
            alert: $(".swal2-title:visible").text()
        }));
        await cas.logr(`The command palette did not open: ${JSON.stringify(state)}`);
        throw error;
    }
    await page.type("#palantirCommandPaletteInput", query);
    await page.waitForFunction((term) => $("#palantirCommandPaletteInput").paletteautocomplete("instance").term === term
        && $(".palantir-command-palette__item.ui-state-active").length === 1, {timeout: 10000}, query);
    return page.evaluate(() => $(".palantir-command-palette__item.ui-state-active .palantir-command-palette__label").text());
}

/**
 * Open the command palette with Ctrl+K and with "/", and check that Enter goes to the first match:
 * a page by its name, then an application, which filters the applications to it.
 *
 * The page opened first is never Users: that tab puts the focus in its username field once it has loaded,
 * which can happen after the "/" below is pressed, and "/" typed in a field is a slash, not the shortcut.
 *
 * @param page the puppeteer page
 */
async function verifyCommandPalette(page) {
    await cas.separator();
    const target = await page.evaluate(() => {
        const item = $("nav.sidebar-navigation ul li[data-tab-index]").toArray()
            .find((li) => Number(li.dataset.tabIndex) > 0 && Number(li.dataset.tabIndex) < 100
                && li.id !== "usersTabButton" && $(li).is(":visible"));
        return {index: Number(item.dataset.tabIndex), label: $(item).find(".sidebar-navigation-label").text().trim()};
    });
    await page.keyboard.down("Control");
    await page.keyboard.press("k");
    await page.keyboard.up("Control");
    const first = await searchCommandPalette(page, target.label);
    await cas.logg(`Command palette offers ${first} first for "${target.label}"`);
    assert(first === target.label);
    await page.keyboard.press("Enter");
    await page.waitForFunction((index) => currentActiveTab === index
        && !$("#palantirCommandPalette").dialog("isOpen"), {timeout: 10000}, target.index);
    await cas.logg(`Command palette opened ${target.label}`);

    const application = await page.evaluate(() => [...registeredServicesById.values()][0]?.name);
    if (application) {
        await cas.sleep(1000);
        await page.evaluate(() => document.activeElement?.blur());
        await page.keyboard.press("/");
        const match = await searchCommandPalette(page, application);
        assert(match === application);
        await page.keyboard.press("Enter");
        await page.waitForFunction((name) => currentActiveTab === 0
            && $("#applicationsTable").DataTable().search() === name, {timeout: 10000}, application);
        await cas.logg(`Command palette found the application ${application}`);
        await page.evaluate(() => {
            $("#applicationsTable").DataTable().search("").draw();
            $("#applications-tab .dt-search input").val("");
        });
    }

    const nested = await page.evaluate(() => PalantirCommandPalette.sectionEntries(PalantirCommandPalette.navigationItems())
        .map((entry) => entry.label)
        .find((label) => label.split(" › ").length > 2));
    if (nested) {
        await cas.sleep(1000);
        await page.evaluate(() => document.activeElement?.blur());
        await page.keyboard.press("/");
        const match = await searchCommandPalette(page, nested.split(" › ").at(-1));
        await cas.log(`Command palette offers ${match} for a section inside a section`);
        await page.evaluate((label) => PalantirCommandPalette.search(label).find((entry) => entry.label === label).run(), nested);
        await page.evaluate(() => $("#palantirCommandPalette").dialog("close"));
        const tab = nested.split(" › ").at(-1);
        await page.waitForFunction((name) => $(".jqueryui-tabs > ul > li.ui-tabs-active:visible").toArray()
            .some((element) => $(element).find("a").first().text().replace(/\s+/g, " ").trim() === name), {timeout: 15000}, tab);
        await cas.logg(`Command palette opened the nested section ${nested}`);
    }
    await cas.screenshot(page);
}

/**
 * Look a user up from the command palette and check that every source the deployment enables draws
 * a card that settles without an error.
 *
 * An endpoint that answers 404 has nothing for the user, which the page shows as an empty card, so
 * those responses are not counted as browser errors.
 *
 * @param page the puppeteer page
 * @param failures the failures recorded so far
 */
async function verifyUserLookup(page, failures) {
    await cas.separator();
    if (!await page.evaluate(() => $("#usersTabButton").is(":visible"))) {
        await cas.logy("No per-user endpoint is enabled; skipping the user lookup");
        return;
    }
    const before = failures.length;
    await cas.click(page, "#palantirCommandPaletteButton");
    const first = await searchCommandPalette(page, "casuser");
    assert(first === "Look up user casuser", `First match was ${first}`);
    await page.keyboard.press("Enter");
    await page.waitForFunction(() => currentActiveTab === 16
        && $("#userLookupResults .palantir-user-card").length > 0
        && $("#userLookupResults .palantir-user-card.is-loading").length === 0, {timeout: 60000});
    const lookup = await page.evaluate(() => ({
        sources: PalantirUserLookup.availableSources().length,
        cards: $("#userLookupResults .palantir-user-card").toArray()
            .map((card) => `${card.dataset.userSource}:${card.className.match(/is-(loaded|empty|error)/)?.[1]}`),
        summary: $("#userLookupSummary").text()
    }));
    await cas.log(`User lookup: ${lookup.cards.join(", ")}`);
    await cas.logg(lookup.summary);
    assert(lookup.cards.length === lookup.sources);
    assert(lookup.cards.every((card) => !card.endsWith(":error")), `Failed sources: ${lookup.cards.join(", ")}`);
    const unexpected = failures.splice(before).filter((failure) => !/^response: 404 GET .*\/actuator\//.test(failure)
        && !/status of 404/.test(failure));
    failures.push(...unexpected);
    await cas.screenshot(page);
}

/**
 * Record two versions of a new service, open its history, check that the latest version is selected and
 * compared side by side with the version before it, then select the earlier version, restore it, and delete
 * the service afterwards.
 *
 * @param page the puppeteer page
 */
async function verifyServiceHistory(page) {
    await cas.separator();
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.entityHistory() && CasActuatorEndpoints.registeredServices()))) {
        await cas.logy("Service history is not available; skipping");
        return;
    }
    const serviceId = await page.evaluate(async () => {
        const save = async (method, service) => {
            const response = await fetch(CasActuatorEndpoints.registeredServices(), {
                method: method,
                headers: {"Content-Type": "application/json", "Accept": "application/json"},
                body: JSON.stringify(service)
            });
            return response.json();
        };
        const created = await save("POST", {
            "@class": "org.apereo.cas.services.CasRegisteredService",
            serviceId: "^https://history.example.org/.*",
            name: "HistoryExample",
            description: "First description"
        });
        await save("PUT", {...created, description: "Second description"});
        return created.id;
    });
    await cas.log(`Recorded two versions of service ${serviceId}`);

    await page.evaluate((id) => PalantirServiceHistory.open(id), serviceId);
    await page.waitForFunction(() => $("#viewEntityHistoryDialog").dialog("instance") !== undefined
        && $("#viewEntityHistoryDialog").dialog("isOpen")
        && $("#entityHistoryDiff .palantir-diff__table").length === 1, {timeout: 30000});
    const history = await page.evaluate(() => ({
        versions: $("#entityHistoryTable").DataTable().rows().count(),
        latestSelected: $("#entityHistoryTable tbody tr:first-child").hasClass("is-selected"),
        compare: $("#entityHistoryCompare").val(),
        changed: $("#entityHistoryDiff td.is-added, #entityHistoryDiff td.is-removed").length,
        summary: $("#entityHistorySummary").text(),
        author: $("#entityHistoryTable tbody tr:first-child .palantir-history__author").text()
    }));
    await cas.log(`History: ${history.versions} versions; ${history.summary}; latest changed by ${history.author}`);
    assert(history.versions >= 2);
    assert(history.author.length > 0);
    assert(history.latestSelected);
    assert(history.compare === "previous");
    assert(history.changed > 0);
    await cas.screenshot(page);

    await page.click("#entityHistoryTable tbody tr:nth-child(2)");
    await page.waitForFunction(() => $("#entityHistoryTable tbody tr:nth-child(2)").hasClass("is-selected")
        && !$(".palantir-history-restore").button("option", "disabled"), {timeout: 10000});

    await cas.click(page, ".palantir-history-restore");
    await page.waitForSelector(".swal2-confirm", {visible: true});
    await page.click(".swal2-confirm");
    await page.waitForFunction(() => !$("#viewEntityHistoryDialog").dialog("isOpen")
        && $(".swal2-title").text() === "Service Restored", {timeout: 30000});
    const description = await page.evaluate(async (id) => {
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {headers: {"Accept": "application/json"}});
        return (await response.json()).description;
    }, serviceId);
    await cas.logg(`Restored service ${serviceId} to its first version: ${description}`);
    assert(description === "First description");

    await page.waitForFunction((id) => $(`#applicationsTable span[serviceId=${id}]`).length > 0, {timeout: 30000}, serviceId);
    await page.evaluate((id) => applicationActionHandler("duplicate", applicationRowContext(String(id))), serviceId);
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("instance") !== undefined
        && $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard
        && $("#registeredServiceName").val() === "Copy of HistoryExample", {timeout: 30000});
    const duplicate = await page.evaluate(() => ({
        title: $("#editServiceWizardDialog").dialog("option", "title"),
        newService: $("#editServiceWizardDialog").attr("newService")
    }));
    await cas.logg(`${duplicate.title} opened as a new application`);
    assert(duplicate.newService === "true");
    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await discardUnsavedChanges(page, "Duplicate");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});

    await page.evaluate(async (id) => {
        Swal.close();
        await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {
            method: "DELETE",
            headers: {"Content-Type": "application/json"}
        });
    }, serviceId);
}

/**
 * Open an application in the editor, change it behind the editor's back, and check that saving the edit shows what
 * changed instead of overwriting it; keep editing, then discard the edit and delete the application.
 *
 * @param page the puppeteer page
 */
async function verifyEditConflict(page) {
    await cas.separator();
    const serviceId = await page.evaluate(async () => {
        const response = await fetch(CasActuatorEndpoints.registeredServices(), {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify({
                "@class": "org.apereo.cas.services.CasRegisteredService",
                serviceId: "^https://conflict.example.org/.*",
                name: "ConflictExample",
                description: "Original description"
            })
        });
        const created = await response.json();
        await fetchServices();
        return created.id;
    });
    await page.waitForFunction((id) => $(`#applicationsTable span[serviceId=${id}]`).length > 0, {timeout: 30000}, serviceId);
    await page.evaluate((id) => applicationActionHandler("editPlain", applicationRowContext(String(id))), serviceId);
    await page.waitForFunction(() => $("#editServiceDialog").dialog("instance") !== undefined
        && $("#editServiceDialog").dialog("isOpen"), {timeout: 30000});
    await cas.sleep(1000);

    await page.evaluate(async (id) => {
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {headers: {"Accept": "application/json"}});
        const service = await response.json();
        await fetch(CasActuatorEndpoints.registeredServices(), {
            method: "PUT",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify({...service, description: "Changed elsewhere"})
        });
        const editor = ace.edit("serviceEditor");
        const edited = JSON.parse(editor.getValue());
        edited.name = "ConflictExampleEdited";
        editor.setValue(JSON.stringify(edited, null, 4), -1);
    }, serviceId);
    await cas.sleep(1000);

    await page.evaluate(() => $("#saveService").trigger("click"));
    await page.waitForFunction(() => $(".swal2-title").text() === "This application has changed", {timeout: 30000});
    const changed = await page.evaluate(() => $(".palantir-conflict__diff td.is-added").length);
    await cas.logg(`Saving a stale edit shows ${changed} changed line(s) instead of overwriting`);
    assert(changed > 0);
    await page.click(".swal2-cancel");
    await page.waitForFunction(() => !Swal.isVisible() && $("#editServiceDialog").dialog("isOpen"), {timeout: 10000});
    const description = await page.evaluate(async (id) => {
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {headers: {"Accept": "application/json"}});
        return (await response.json()).description;
    }, serviceId);
    assert(description === "Changed elsewhere");

    await page.evaluate(() => $("#cancelService").trigger("click"));
    await discardUnsavedChanges(page, "Edit conflict");
    await page.waitForFunction(() => !$("#editServiceDialog").dialog("isOpen"), {timeout: 15000});
    await page.evaluate(async (id) => {
        await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {
            method: "DELETE",
            headers: {"Content-Type": "application/json"}
        });
    }, serviceId);
}

/**
 * Fill the SAML2 wizard from the metadata of the SAML2 test service provider, read by the server from its URL, and
 * check that the entity id and the metadata location come from it; then discard the wizard.
 *
 * @param page the puppeteer page
 */
async function verifySamlMetadataImport(page) {
    await cas.separator();
    const samlClass = "org.apereo.cas.support.saml.services.SamlRegisteredService";
    const available = await page.evaluate((serviceClass) => Object.hasOwn(PalantirDashboardConfiguration.supportedServiceTypes(), serviceClass)
        && PalantirSamlMetadataImport.canReadUrls(), samlClass);
    if (!available) {
        await cas.logy("SAML2 metadata import is not available; skipping");
        return;
    }
    const metadataLocation = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
    await page.evaluate((serviceClass) => openRegisteredServiceWizardDialog({"@class": serviceClass}, {duplicate: true}), samlClass);
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard
        && $("#fillServiceWizardFromMetadata").is(":visible"), {timeout: 30000});
    await cas.sleep(1000);
    await page.evaluate(() => $("#fillServiceWizardFromMetadata").trigger("click"));
    await page.waitForSelector("#samlMetadataImportUrl", {visible: true});
    await page.evaluate(() => $("#samlMetadataImportUrl").val(""));
    await page.type("#samlMetadataImportUrl", metadataLocation);
    await page.click(".swal2-confirm");
    await page.waitForFunction(() => $(".swal2-title").text() === "Filled from metadata"
        || $(".swal2-title").text() === "The metadata could not be read", {timeout: 60000});
    const outcome = await page.evaluate(() => {
        const definition = JSON.parse(ace.edit("wizardServiceEditor").getValue());
        return {
            title: $(".swal2-title").text(),
            message: $(".swal2-html-container").text(),
            serviceId: definition.serviceId,
            metadataLocation: definition.metadataLocation
        };
    });
    await cas.log(`${outcome.title}: ${outcome.message}`);
    assert(outcome.title === "Filled from metadata");
    assert(outcome.serviceId && outcome.serviceId.length > 0);
    assert(outcome.metadataLocation === metadataLocation);
    await page.evaluate(() => Swal.close());
    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await discardUnsavedChanges(page, "SAML2 metadata import");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});
}

/**
 * Register the SAML2 test service provider with its metadata URL, check the health of its metadata from the menu of
 * its card, and check that the metadata is downloaded, holds the service provider and is resolved by CAS, and that the
 * metadata is shown; then delete the application.
 *
 * @param page the puppeteer page
 */
async function verifySamlMetadataHealth(page) {
    await cas.separator();
    const available = await page.evaluate(() => Boolean(CasActuatorEndpoints.registeredServices()
        && CasActuatorEndpoints.samlIdpRegisteredServiceMetadataCache()));
    if (!available) {
        await cas.logy("SAML2 metadata health is not available; skipping");
        return;
    }
    const metadataLocation = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
    const serviceId = await page.evaluate(async (location) => {
        const preview = await PalantirSamlMetadataImport.request(location);
        const response = await fetch(CasActuatorEndpoints.registeredServices(), {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify({
                "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
                serviceId: preview.entityId,
                name: "MetadataHealthExample",
                metadataLocation: location
            })
        });
        return (await response.json()).id;
    }, metadataLocation);
    await cas.log(`Registered SAML2 application ${serviceId} for ${metadataLocation}`);

    await page.evaluate(() => fetchServices());
    await cas.click(page, "#applicationsCardsView");
    await page.evaluate(() => $("#applicationsTable").DataTable().search("MetadataHealthExample").draw());
    const menu = `#applicationsCards .palantir-application-card[data-service-id="${serviceId}"] [data-application-action="menu"]`;
    await page.waitForSelector(menu, {visible: true, timeout: 30000});
    await page.click(menu);
    await page.locator(".context-menu-list ::-p-text(Check Metadata Health)").click();
    await page.waitForFunction(() => $("#samlMetadataHealthDialog").dialog("instance") !== undefined
        && $("#samlMetadataHealthDialog").dialog("isOpen")
        && ["ok", "warn", "fail"].includes($("#samlMetadataHealthSummary").attr("data-status")), {timeout: 60000});
    const health = await page.evaluate(() => ({
        status: $("#samlMetadataHealthSummary").attr("data-status"),
        checks: Object.fromEntries($("#samlMetadataHealthChecks li").map(function () {
            return [[$(this).attr("data-check-title"), $(this).attr("data-check-status")]];
        }).get()),
        details: $("#samlMetadataHealthChecks li").map(function () {
            return $(this).text().replace(/\s+/g, " ").trim();
        }).get(),
        metadata: !$("#samlMetadataHealthMetadata").prop("hidden")
    }));
    for (const detail of health.details) {
        await cas.log(detail);
    }
    await cas.logg(`Metadata health of application ${serviceId}: ${health.status}`);
    await cas.screenshot(page);
    assert(health.checks["Download"] === "ok");
    assert(health.checks["Service provider"] === "ok");
    assert(health.checks["Resolution by CAS"] === "ok");
    assert(health.metadata);

    await page.evaluate(async (id) => {
        $("#samlMetadataHealthDialog").dialog("close");
        $("#applicationsTable").DataTable().search("").draw();
        await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {
            method: "DELETE",
            headers: {"Content-Type": "application/json"}
        });
        fetchServices();
    }, serviceId);
}

/**
 * Open an application whose multifactor policy only names principal attributes in the wizard, and check that the
 * definition the wizard generates keeps them with the policy's type, so that CAS accepts it.
 *
 * @param page the puppeteer page
 */
async function verifyMultifactorPolicyRoundTrip(page) {
    await cas.separator();
    await page.evaluate(() => openRegisteredServiceWizardDialog({
        "@class": "org.apereo.cas.services.CasRegisteredService",
        id: 654321,
        name: "MultifactorPolicyRoundTrip",
        serviceId: "https://mfa-round-trip.example.org/.*",
        multifactorPolicy: {
            "@class": "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
            principalAttributeNameTrigger: "gender",
            bypassPrincipalAttributeName: "gender"
        }
    }));
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard
        && $("#registeredServiceName").val() === "MultifactorPolicyRoundTrip", {timeout: 30000});
    await cas.sleep(2000);
    const outcome = await page.evaluate(async () => {
        const text = ace.edit("wizardServiceEditor").getValue();
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/validate`, {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: text
        });
        return {policy: JSON.parse(text).multifactorPolicy, status: response.status, body: await response.text()};
    });
    await cas.log(`Generated multifactor policy: ${JSON.stringify(outcome.policy)}; validation answered ${outcome.status} ${outcome.body}`);
    assert(outcome.policy["@class"] === "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy");
    assert(outcome.policy.principalAttributeNameTrigger === "gender" && outcome.policy.bypassPrincipalAttributeName === "gender");
    assert(outcome.status === 200);
    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await discardUnsavedChanges(page, "Multifactor policy round trip");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});
}

/**
 * Create three applications, disable one from its context menu action and enable it again, then select two of them
 * with their checkboxes and delete them together; the third is removed afterwards. The Applications page is opened
 * first, since an earlier step may have left another page showing.
 *
 * @param page the puppeteer page
 */
async function verifyApplicationBulkActions(page) {
    await cas.separator();
    const ids = await page.evaluate(async () => {
        const created = [];
        for (const suffix of ["One", "Two", "Three"]) {
            const response = await fetch(CasActuatorEndpoints.registeredServices(), {
                method: "POST",
                headers: {"Content-Type": "application/json", "Accept": "application/json"},
                body: JSON.stringify({
                    "@class": "org.apereo.cas.services.CasRegisteredService",
                    serviceId: `^https://bulk-${suffix.toLowerCase()}.example.org/.*`,
                    name: `BulkExample${suffix}`
                })
            });
            created.push((await response.json()).id);
        }
        return created;
    });
    await page.evaluate(() => $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.APPLICATIONS.index}]`).trigger("click"));
    await page.waitForFunction(() => currentActiveTab === Tabs.APPLICATIONS.index, {timeout: 30000});
    await page.evaluate(() => fetchServices());
    await page.waitForFunction((created) => created.every((id) => registeredServicesById.has(String(id))), {timeout: 30000}, ids);
    await page.evaluate(() => {
        $("#applicationsTableView").trigger("click");
        $("#applicationsTable").DataTable().search("BulkExample").draw();
    });
    await page.waitForFunction(() => $("#applicationsTable").is(":visible"), {timeout: 30000});
    const strategy = async (id) => page.evaluate(async (serviceId) => {
        const response = await fetch(`${CasActuatorEndpoints.registeredServices()}/${serviceId}`, {headers: {"Accept": "application/json"}});
        return (await response.json()).accessStrategy ?? null;
    }, id);

    for (const disable of [true, false]) {
        await page.evaluate(([id, key]) => applicationActionHandler(key, applicationRowContext(String(id))), [ids[0], disable ? "disable" : "enable"]);
        await page.waitForSelector(".swal2-confirm", {visible: true});
        await page.click(".swal2-confirm");
        await page.waitForFunction(([id, expected]) => PalantirApplicationAccess.isServiceDisabled(id) === expected
            && $(`#applicationsTable tbody tr:has([data-select-service="${id}"])`).hasClass("is-disabled") === expected,
        {timeout: 30000}, [ids[0], disable]);
        const saved = await strategy(ids[0]);
        await cas.log(`${disable ? "Disabled" : "Enabled"} application ${ids[0]}: ${JSON.stringify(saved)}`);
        assert(isDisabledAccessStrategy(saved) === disable);
    }

    await page.evaluate(() => {
        $("#applicationsTableView").trigger("click");
        $("#applicationsTable").DataTable().search("BulkExample").draw();
    });
    const cell = async (id) => (await page.waitForFunction((serviceId) => $(`#applicationsTable tbody tr:has([data-select-service="${serviceId}"]) > td:visible`)
        .not(".palantir-application-type").get(0), {timeout: 30000}, id)).asElement();
    await (await cell(ids[0])).click();
    await (await cell(ids[1])).click();
    await page.waitForFunction(() => !$("#applicationsSelectionBar").prop("hidden")
        && $(".palantir-selection-bar__count").text().startsWith("2 "), {timeout: 10000});
    await page.evaluate(() => notyf.dismissAll());
    const [exported] = await Promise.all([
        page.waitForResponse((response) => response.url().includes("/registeredServices/export?"), {timeout: 30000}),
        page.click("button[name=exportSelectedServices]")
    ]);
    const requested = new URL(exported.url()).searchParams.getAll("ids").sort();
    await cas.logg(`Exporting the selection asks for ${requested.join(", ")} and answers ${exported.status()} with ${exported.headers()["filename"]}`);
    assert(exported.status() === 200);
    assert(exported.headers()["filename"] === "services.zip");
    assert(JSON.stringify(requested) === JSON.stringify([String(ids[0]), String(ids[1])].sort()));
    const archive = await page.evaluate(async (url) => {
        const bytes = new Uint8Array(await (await fetch(url, {credentials: "include"})).arrayBuffer());
        return {size: bytes.length, signature: bytes.length >= 4 ? new DataView(bytes.buffer).getUint32(0, true) : 0};
    }, exported.url());
    await cas.log(`The exported archive holds ${archive.size} bytes`);
    assert(archive.size > 0 && archive.signature === 0x04034b50);

    for (const disable of [true, false]) {
        const button = disable ? "disableSelectedServices" : "enableSelectedServices";
        await page.waitForFunction((name) => !$(`button[name=${name}]`).prop("disabled"), {timeout: 10000}, button);
        await page.click(`button[name=${button}]`);
        await page.waitForSelector(".swal2-confirm", {visible: true});
        await page.click(".swal2-confirm");
        await page.waitForFunction((expected) => $(".swal2-title").text() === `2 applications ${expected ? "disabled" : "enabled"}`,
            {timeout: 60000}, disable);
        await page.evaluate(() => Swal.close());
        const saved = [await strategy(ids[0]), await strategy(ids[1])];
        await cas.logg(`${disable ? "Disabled" : "Enabled"} the selection: ${JSON.stringify(saved)}`);
        assert(saved.every((value) => isDisabledAccessStrategy(value) === disable));
        await page.waitForFunction((selected) => selected.every((id) => PalantirApplicationSelection.has(String(id))), {timeout: 10000}, [ids[0], ids[1]]);
    }

    await page.click("button[name=deleteSelectedServices]");
    await page.waitForSelector(".swal2-confirm", {visible: true});
    await page.click(".swal2-confirm");
    await page.waitForFunction(() => $(".swal2-title").text() === "2 applications deleted", {timeout: 60000});
    const remaining = await page.evaluate(async (created) => {
        const response = await fetch(CasActuatorEndpoints.registeredServices(), {headers: {"Accept": "application/json"}});
        const registered = new Set((await response.json())[1].map((service) => String(service.id)));
        return created.map((id) => registered.has(String(id)));
    }, ids);
    await cas.logg(`After deleting the selection, the applications still registered: ${remaining.join(", ")}`);
    assert(!remaining[0] && !remaining[1] && remaining[2]);

    await page.evaluate(async (id) => {
        Swal.close();
        $("#applicationsTable").DataTable().search("").draw();
        await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {method: "DELETE", headers: {"Content-Type": "application/json"}});
        fetchServices();
    }, ids[2]);
}

/**
 * Whether a saved access strategy turns the application off, as Palantir decides it.
 *
 * @param strategy the access strategy
 * @returns {boolean} true when the application is disabled
 */
function isDisabledAccessStrategy(strategy) {
    if (!strategy) {
        return false;
    }
    if (strategy["@class"] === "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy") {
        return strategy.enabled === false;
    }
    const strategies = Array.isArray(strategy.strategies?.[1]) ? strategy.strategies[1] : strategy.strategies ?? [];
    return strategy["@class"] === "org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy"
        && strategies.some(isDisabledAccessStrategy);
}

/**
 * The ticket registry endpoint is read-only in this scenario: its write controls are disabled and a request that
 * would change something through it is refused before it is sent, while other endpoints stay writable.
 *
 * @param page the puppeteer page
 */
async function verifyReadOnlyEndpoint(page) {
    await cas.separator();
    const deletes = [];
    const onRequest = (request) => {
        if (request.method() === "DELETE" && request.url().includes("/actuator/ticketRegistry")) {
            deletes.push(request.url());
        }
    };
    page.on("request", onRequest);
    const outcome = await page.evaluate(async () => {
        let refused = null;
        try {
            await $.ajax({url: `${CasActuatorEndpoints.ticketRegistry()}/clean`, type: "DELETE"});
        } catch (xhr) {
            refused = xhr.statusText;
        }
        return {
            access: PalantirDashboardConfiguration.actuatorEndpointAccess().ticketRegistry,
            writable: CasActuatorEndpoints.writable("ticketRegistry"),
            servicesWritable: CasActuatorEndpoints.writable("registeredServices"),
            cleanDisabled: $("#cleanTicketsButton").prop("disabled"),
            newServiceDisabled: $("#newService").prop("disabled"),
            refused: refused
        };
    });
    page.off("request", onRequest);
    await cas.log(`Ticket registry access: ${JSON.stringify(outcome)}`);
    assert(outcome.access === "READ_ONLY");
    assert(!outcome.writable && outcome.servicesWritable);
    assert(outcome.cleanDisabled && !outcome.newServiceDisabled);
    assert(outcome.refused === "palantir-read-only");
    assert(deletes.length === 0);
    await page.evaluate(() => notyf.dismissAll());
}

/**
 * Import a YAML application definition through the Import button's file input, check that it is imported once
 * and shows up in the applications table, and delete it afterwards.
 *
 * @param page the puppeteer page
 */
async function verifyServiceImport(page) {
    await cas.separator();
    const file = path.join(fs.mkdtempSync(path.join(os.tmpdir(), "palantir-import-")), "ImportExample.yml");
    fs.writeFileSync(file, [
        "--- !<org.apereo.cas.services.CasRegisteredService>",
        "serviceId: \"^https://import.example.org/.*\"",
        "name: \"ImportExample\"",
        "description: \"Imported from YAML\""
    ].join("\n"));

    const imports = [];
    const onRequest = (request) => {
        if (request.method() === "POST" && request.url().endsWith("/registeredServices/import")) {
            imports.push(request.headers()["content-type"]);
        }
    };
    page.on("request", onRequest);
    const input = await page.$("#serviceFileInput");
    await input.uploadFile(file);
    await page.waitForSelector(".swal2-confirm", {visible: true});
    await page.click(".swal2-confirm");
    await page.waitForFunction(() => $(".swal2-title").text() === "Imported", {timeout: 30000});
    const message = await page.evaluate(() => $(".swal2-html-container").text());
    await cas.logg(message);
    await page.evaluate(() => Swal.close());

    const listed = await page.waitForFunction(() => [...registeredServicesById.values()]
        .find((service) => service.name === "ImportExample")?.id, {timeout: 30000});
    const serviceId = await listed.jsonValue();
    page.off("request", onRequest);
    await cas.log(`Import requests: ${imports.join(", ")}`);
    assert(imports.length === 1 && imports[0].startsWith("application/yaml"));
    assert(serviceId !== undefined);
    await page.waitForFunction((id) => $(`#applicationsTable span[serviceId=${id}]`).length > 0, {timeout: 30000}, serviceId);
    await cas.logg(`Imported application ${serviceId} is listed`);

    await page.evaluate(async (id) => {
        await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {
            method: "DELETE",
            headers: {"Content-Type": "application/json"}
        });
    }, serviceId);
    fs.rmSync(path.dirname(file), {recursive: true, force: true});
}

/**
 * Ask the Interrupts section of the Authentication page which interrupt a user would see, and check that it
 * reports the resolved user and the answer of every interrupt source.
 *
 * @param page the puppeteer page
 */
async function verifyInterruptInquiry(page) {
    await cas.separator();
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.interrupts()))) {
        await cas.logy("Interrupts endpoint is not available; skipping");
        return;
    }
    await page.evaluate(() => $("#authenticationTabButton").trigger("click"));
    await page.waitForFunction(() => $.fn.dataTable.isDataTable("#interruptInquirersTable"), {timeout: 30000});
    await page.evaluate(() => {
        const tabs = $("#authentication-tabs");
        tabs.tabs("option", "active", tabs.find("> ul > li").index($("#interrupts").parent()));
    });
    await page.waitForSelector("#interruptUsername", {visible: true});
    await page.type("#interruptUsername", "casuser");
    await page.keyboard.press("Enter");
    await page.waitForFunction(() => $("#interruptInquiryResult").is(":visible")
        || $("#interruptInquiryMessage").is(":visible"), {timeout: 30000});
    const inquiry = await page.evaluate(() => ({
        principal: $("#interruptInquiryPrincipal").text(),
        status: $("#interruptInquiryStatus").text(),
        sources: $("#interruptInquirersTable").DataTable().rows().count(),
        message: $("#interruptInquiryMessage").is(":visible") ? $("#interruptInquiryMessage").text().trim() : ""
    }));
    await cas.logg(`Interrupt inquiry for ${inquiry.principal}: ${inquiry.status}, ${inquiry.sources} source(s)`);
    assert(inquiry.message === "", inquiry.message);
    assert(inquiry.principal === "casuser");
    await cas.screenshot(page);
}

/**
 * Look up the throttled attempts of one username on the Throttles page, check the hint and the button that shows all
 * attempts again, then show them all. The page needs the throttles endpoint, which the throttle module provides.
 *
 * @param page the puppeteer page
 */
async function verifyZenMode(page) {
    await page.evaluate(() => {
        document.activeElement?.blur();
        $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.APPLICATIONS.index}]`).trigger("click");
    });
    await page.keyboard.down("Shift");
    await page.keyboard.press("KeyZ");
    await page.keyboard.up("Shift");
    await page.waitForFunction(() => document.documentElement.hasAttribute("data-palantir-zen")
        && !$("#app-bar").is(":visible") && localStorage.getItem("PalantirZenMode") === "true", {timeout: 10000});
    await page.keyboard.press("s");
    await page.waitForFunction(() => currentActiveTab === Tabs.SYSTEM.index, {timeout: 10000});
    await cas.logg("Zen mode hides the header, navigation and footer, and the page shortcuts still work");
    await page.click("#palantirZenExit");
    await page.waitForFunction(() => !document.documentElement.hasAttribute("data-palantir-zen")
        && $("#app-bar").is(":visible") && localStorage.getItem("PalantirZenMode") === null, {timeout: 10000});
}

/**
 * The Modules tab under System Info lists the CAS modules the server runs. A build that does not record module
 * details, as local builds do not, reports none, and the tab says so.
 */
async function verifyCasModules(page) {
    await page.evaluate(() => {
        $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.SYSTEM.index}]`).trigger("click");
        $("#system-tabs").tabs("option", "active", $("#system-tabs ul li a[href='#systeminfo-tab']").parent().index());
        $("#system-info-tabs").tabs("option", "active", $("#system-info-tabs ul li a[href='#casmodules-tab']").parent().index());
    });
    await page.waitForFunction(() => PalantirModules.modules !== null, {timeout: 30000});
    const modules = await page.evaluate(() => ({
        count: PalantirModules.modules.length,
        cards: $("#casModulesCards .palantir-modules__card").length,
        empty: $("#casModulesCards .palantir-empty-state").text()
    }));
    await cas.logg(`CAS modules: ${modules.count}`);
    if (modules.count === 0) {
        assert(modules.empty.includes("no CAS modules"));
        return;
    }
    assert(modules.cards === modules.count);
    await page.type("#casModulesFilter", "palantir");
    await page.waitForFunction(() => $("#casModulesCards .palantir-modules__card").toArray()
        .every((card) => card.textContent.toLowerCase().includes("palantir")), {timeout: 10000});
    await page.click("#casModulesCards .palantir-modules__card [data-module-action='dependency']");
    const declaration = await page.evaluate(() => $("#casModulesCards .palantir-modules__dependency:not([hidden]) code").text());
    await cas.log(`Declared as: ${declaration}`);
    assert(declaration.includes("org.apereo.cas") && declaration.includes("palantir"));
    await page.evaluate(() => $("#casModulesFilter").val("").trigger("input"));
}

async function verifySystemMetrics(page) {
    await page.evaluate(() => {
        localStorage.removeItem("PalantirPinnedMetrics");
        $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.SYSTEM.index}]`).trigger("click");
    });
    await page.waitForFunction(() => currentActiveTab === Tabs.SYSTEM.index, {timeout: 30000});
    await page.evaluate(() => {
        const index = $("#system-tabs ul li a[href='#systemmetrics-tab']").parent().index();
        $("#system-tabs").tabs("option", "active", index);
    });
    await page.waitForFunction(() => $("#metricsTiles [data-metric-tile]").length > 0
        && $("#metricsTiles .palantir-metrics__tile-number").toArray().every((element) => element.textContent !== "…"), {timeout: 30000});
    const tiles = await page.evaluate(() => $("#metricsTiles [data-metric-tile]").map((index, element) =>
        `${$(element).find(".palantir-metrics__tile-name").text()}=${$(element).find(".palantir-metrics__tile-number").text()}`).get());
    await cas.logg(`Metrics board: ${tiles.join(", ")}`);
    assert(tiles.some((tile) => tile.startsWith("jvm.memory.used=") && tile.endsWith("%")));

    await page.click("#metricsExplorerView");
    await page.waitForFunction(() => $("#metricsList [data-metric='jvm.threads.peak']").length === 1, {timeout: 30000});
    await page.click("#metricsList [data-metric='jvm.threads.peak']");
    await page.waitForFunction(() => $("#metricsDetailName").text() === "jvm.threads.peak"
        && $("#metricsDetailStats .palantir-metrics__stat").length > 0, {timeout: 30000});
    const value = await page.evaluate(() => $("#metricsDetailStats strong").first().text());
    await cas.logg(`Peak threads in the explorer: ${value}`);
    assert(/^\d[\d,.]*$/.test(value));

    await page.click("#metricsDetailPin");
    await page.waitForFunction(() => JSON.parse(localStorage.getItem("PalantirPinnedMetrics"))
        .some((pin) => pin.name === "jvm.threads.peak"), {timeout: 10000});
    await page.click("#metricsDetailPin");
    await page.waitForFunction(() => !JSON.parse(localStorage.getItem("PalantirPinnedMetrics"))
        .some((pin) => pin.name === "jvm.threads.peak"), {timeout: 10000});
    await page.click("#metricsBoardView");
    await page.evaluate(() => localStorage.removeItem("PalantirPinnedMetrics"));
}

async function verifySignIns(page) {
    await page.evaluate(() => {
        const index = $("#system-tabs ul li a[href='#systemstatus-tab']").parent().index();
        $("#system-tabs").tabs("option", "active", index);
    });
    await page.waitForFunction(() => /sign-ins · counted at/.test($("#signInsChartStatus").text()), {timeout: 30000});
    const status = await page.evaluate(() => $("#signInsChartStatus").text());
    const counted = await page.evaluate(() => signInsChart.data.datasets.map((dataset) => dataset.data.length));
    await cas.logg(`Sign-ins chart: ${status}; hours per series ${counted.join(", ")}`);
    assert(counted.every((hours) => hours === 2));

    await page.waitForFunction(() => /every minute|^No /.test($("#multifactorProvidersChartStatus").text()), {timeout: 30000});
    await cas.logg(`Multifactor by provider: ${await page.evaluate(() => $("#multifactorProvidersChartStatus").text())}`);
    assert(await page.$("#systemstatus-tab #topApplicationsChart") === null);
}

async function verifyThrottlesUsername(page) {
    await cas.separator();
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.throttles()))) {
        await cas.logy("Throttles endpoint is not available; skipping");
        return;
    }
    await page.evaluate(() => $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.THROTTLES.index}]`).trigger("click"));
    await page.waitForFunction(() => $("#throttlesUsername").is(":visible"), {timeout: 30000});
    await page.evaluate(() => $("#throttlesUsername").val("PalantirNobody"));
    const [response] = await Promise.all([
        page.waitForResponse((answer) => answer.url().includes("/throttles?username=PalantirNobody"), {timeout: 30000}),
        page.click("#filterThrottlesButton")
    ]);
    await page.waitForFunction(() => $("#throttlesUsernameHint").text().startsWith("0 throttled attempts for PalantirNobody")
        && $("#showAllThrottlesButton").is(":visible"), {timeout: 10000});
    await cas.logg(`Throttled attempts of one username: ${response.status()}, ${await page.evaluate(() => $("#throttlesUsernameHint").text())}`);
    assert(response.status() === 200);
    await page.click("#showAllThrottlesButton");
    await page.waitForFunction(() => $("#throttlesUsername").val() === "" && !$("#showAllThrottlesButton").is(":visible"), {timeout: 10000});
}

/**
 * Open the keyboard shortcuts overlay with ? and close it with Escape.
 *
 * @param page the puppeteer page
 */
async function verifyShortcutsOverlay(page) {
    await cas.separator();
    await page.evaluate(() => document.activeElement?.blur());
    await page.keyboard.down("Shift");
    await page.keyboard.press("Slash");
    await page.keyboard.up("Shift");
    await page.waitForFunction(() => $("#palantirShortcutsDialog").dialog("instance") !== undefined
        && $("#palantirShortcutsDialog").dialog("isOpen"), {timeout: 10000});
    const groups = await page.evaluate(() => $("#palantirShortcutsDialog .palantir-shortcuts__group h4").toArray().map((heading) => heading.textContent));
    await cas.logg(`Keyboard shortcuts: ${groups.join(", ")}`);
    assert(groups.includes("Pages") && groups.includes("Anywhere"));
    await page.keyboard.press("Escape");
    await page.waitForFunction(() => !$("#palantirShortcutsDialog").dialog("isOpen"), {timeout: 10000});
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    
    const failures = trackFailures(page);

    await cas.goto(page, DASHBOARD_URL);
    if (await cas.isVisible(page, "#username")) {
        const login = await cas.loginWith(page, "casadmin", "password");
        await cas.sleep(1000);
        await cas.log(`${login.status()} ${login.statusText()}`);
        assert(login.status() === 200);
    }
    await cas.screenshot(page);

    await page.evaluate(() => window.localStorage.clear());
    const response = await cas.goto(page, DASHBOARD_URL);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await waitForDashboard(page);
    await verifyHome(page);
    await verifyRegisteredServices(page);
    await verifyPlainEditorDialog(page);
    await verifyTheme(page);
    await verifyTabs(page);
    await verifyFeatureCatalog(page);
    await verifyServiceWizard(page);
    await verifySettingsDialog(page);
    await verifyCommandPalette(page);
    await verifyUserLookup(page, failures);
    await verifyServiceHistory(page);
    await verifyServiceImport(page);
    await verifyEditConflict(page);
    await verifySamlMetadataImport(page);
    await verifyMultifactorPolicyRoundTrip(page);
    await verifyApplicationBulkActions(page);
    await verifySamlMetadataHealth(page);
    await verifyReadOnlyEndpoint(page);
    await verifyInterruptInquiry(page);
    await verifyThrottlesUsername(page);
    await verifyCasModules(page);
    await verifySystemMetrics(page);
    await verifySignIns(page);
    await verifyZenMode(page);
    await verifyShortcutsOverlay(page);

    if (failures.length > 0) {
        await cas.logr(`Browser reported ${failures.length} error(s):`);
        for (const failure of failures) {
            await cas.logr(`  ${failure}`);
        }
    }
    assert(failures.length === 0, `Browser errors: ${failures.join(" | ")}`);
    await cas.logg("Palantir dashboard verified with no browser errors");

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();
