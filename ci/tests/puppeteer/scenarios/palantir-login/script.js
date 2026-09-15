/* global $, CAS_FEATURES, CasActuatorEndpoints, PalantirDashboardConfiguration,
   RegisteredServiceSections, openRegisteredServiceWizardDialog, isPopulatingWizard,
   activateDashboardTab */
const assert = require("assert");
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
    {index: 15, label: "Cluster", button: "#clusterTabButton", tabs: "cluster-tabs", tables: []}
];

const PROTOCOL_FIELDS = {
    OidcRegisteredService: ["#registeredServiceClientId", ".registered-service-client-secret-value"],
    OAuthRegisteredService: ["#registeredServiceClientId", ".registered-service-client-secret-value"],
    SamlRegisteredService: ["#registeredServiceMetadataLocation"],
    WSFederationRegisteredService: ["#registeredServiceWsFederationRealm"],
    CasRegisteredService: ["#registeredServiceSupportedProtocols", "#registeredServiceRedirectUrl"]
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
            failures.push(`console: ${message.text()}`);
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
 * Assert the applications tab loaded and drew the service registry.
 *
 * The row count is reported rather than asserted: this scenario ships no service definitions, so an
 * empty registry is a legitimate outcome and only a table that never became a DataTable is a fault.
 *
 * @param page the puppeteer page
 */
async function verifyRegisteredServices(page) {
    await cas.separator();
    await page.waitForFunction(() => $.fn.dataTable.isDataTable("#applicationsTable"), {timeout: 30000});
    const count = await page.evaluate(() => $("#applicationsTable").DataTable().data().count());
    await cas.logg(`Applications table drew ${count} registered services`);

    for (const button of ["newServiceWizard", "newServicePlain", "importService", "exportAll", "reloadAll"]) {
        await cas.assertVisibility(page, `#${button}`);
    }
}

/**
 * Assert the wizard's fields do not exist until the dialog is opened.
 *
 * @param page the puppeteer page
 */
async function verifyWizardIsDeferred(page) {
    const built = await page.evaluate(() => ({
        nameField: document.querySelector("#registeredServiceName") !== null,
        params: document.querySelectorAll("#editServiceWizardForm [data-param-name]").length
    }));
    await cas.logg(`Before opening the wizard: name field present=${built.nameField}, fields=${built.params}`);
    assert(built.nameField === false);
    assert(built.params === 0);
}

/**
 * Open the wizard for one service type and check what it built.
 *
 * Every section the wizard knows about is checked against whether it should be available for this
 * service type, this advanced-options setting and the deployment's feature catalog; each available
 * section must have a sidebar entry that reveals its panel; and each protocol field must be visible
 * exactly once, inside the Protocol group and not inside another type's section.
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
        const expected = entry.protocolFields ?? [];
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
            for (const selector of expected) {
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

        for (const selector of expected) {
            if (!seen.has(selector)) {
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
 * Exercise the service wizard for every service type the deployment supports.
 *
 * The wizard's fields are built when the dialog is first opened rather than while the page parses,
 * so this is what proves the deferred construction produces the same form. Each type is opened,
 * inspected with advanced options collapsed and expanded, and dismissed through Cancel.
 *
 * @param page the puppeteer page
 */
async function verifyServiceWizard(page) {
    await cas.separator();
    await verifyWizardIsDeferred(page);

    const types = await page.evaluate(() => Object.keys(PalantirDashboardConfiguration.supportedServiceTypes()).sort());
    await cas.logg(`Deployment supports ${types.length} service types`);
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

        await cas.logg(`Wizard verified for ${name}`);
        await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
        await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") === false, {timeout: 15000});
    }
    await cas.screenshot(page);
}

/**
 * Open and dismiss the plain definition editor.
 *
 * It is a jQuery UI dialog now rather than hand-written MDC markup, so this checks the widget is
 * created, the Ace editor inside it is attached, and Cancel closes it.
 *
 * @param page the puppeteer page
 */
async function verifyPlainEditorDialog(page) {
    await cas.separator();
    await page.evaluate(() => $("#newServicePlain").trigger("click"));
    await page.waitForFunction(() => $("#editServiceDialog").hasClass("ui-dialog-content")
        && $("#editServiceDialog").dialog("isOpen")
        && document.querySelector("#serviceEditor").classList.contains("ace_editor"), {timeout: 30000});
    await cas.logg("Plain definition editor opened as a jQuery UI dialog");
    await cas.assertVisibility(page, "#saveService");
    await cas.assertVisibility(page, "#cancelService");

    await page.evaluate(() => $("#cancelService").trigger("click"));
    await page.waitForFunction(() => $("#editServiceDialog").dialog("isOpen") === false, {timeout: 15000});
    await cas.logg("Plain definition editor dismissed through Cancel");
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
    await page.evaluate(() => $("#palantirSettingsDialog").dialog("close"));
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const failures = trackFailures(page);

    await cas.goto(page, DASHBOARD_URL);
    let response = await cas.loginWith(page, "casadmin", "password");
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    await cas.screenshot(page);
    assert(response.status() === 200);

    response = await cas.goto(page, DASHBOARD_URL);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await waitForDashboard(page);
    await verifyRegisteredServices(page);
    await verifyTabs(page);
    await verifyServiceWizard(page);
    await verifyPlainEditorDialog(page);
    await verifySettingsDialog(page);

    if (failures.length > 0) {
        await cas.logr(`Browser reported ${failures.length} error(s):`);
        for (const failure of failures) {
            await cas.logr(`  ${failure}`);
        }
    }
    assert(failures.length === 0, `Browser errors: ${failures.join(" | ")}`);
    await cas.logg("Palantir dashboard verified with no browser errors");

    await cas.closeBrowser(browser);
})();
