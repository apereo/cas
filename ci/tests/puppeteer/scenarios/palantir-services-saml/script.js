/* global $, CAS_FEATURES, CasActuatorEndpoints, PalantirDashboardConfiguration, openRegisteredServiceWizardDialog,
   isPopulatingWizard, ace, currentActiveTab, Swal, fetchServices, PalantirSamlMetadataImport, PalantirSamlMetadataUpload,
   Tabs */
const assert = require("assert");
const path = require("path");
const cas = require("../../cas.js");

const DASHBOARD_URL = "https://localhost:8443/cas/palantir/dashboard";

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
 * Answer the unsaved-changes question, if closing a dialog asked it, by discarding the changes, which also removes
 * the draft kept for them.
 *
 * @param page the puppeteer page
 * @param name the registered service type under test, for the log
 */
async function discardUnsavedChanges(page, name) {
    await cas.sleep(500);
    const asked = await page.evaluate(() => $(".swal2-popup:visible").length > 0
        && $(".swal2-title").text() === "Discard Your Changes?");
    if (asked) {
        await page.click(".swal2-deny");
        await cas.logg(`${name}: closing the wizard with changes asked before discarding them`);
    }
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
    await page.waitForFunction(() => $(".swal2-title").text() === "Filled from Metadata"
        || $(".swal2-title").text() === "The Metadata Could Not Be Read", {timeout: 60000});
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
    assert(outcome.title === "Filled from Metadata");
    assert(outcome.serviceId && outcome.serviceId.length > 0);
    assert(outcome.metadataLocation === metadataLocation);
    await page.evaluate(() => Swal.close());
    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await discardUnsavedChanges(page, "SAML2 metadata import");
    await page.waitForFunction(() => !$("#editServiceWizardDialog").dialog("isOpen"), {timeout: 15000});
}

/**
 * Store the SAML2 test service provider's metadata with the MongoDb metadata manager, then use Manage Metadata in the
 * SAML2 wizard: with unsaved changes it asks first, Keep Editing stays in the wizard, and Discard Changes closes it and
 * shows that manager's entries under Protocols, the stored entry among them. The entry is removed afterwards.
 *
 * @param page the puppeteer page
 */
async function verifySamlMetadataManage(page) {
    await cas.separator();
    const managers = await page.evaluate(async () => (PalantirSamlMetadataUpload.available()
        ? PalantirSamlMetadataUpload.loadManagers() : []));
    if (managers.length === 0) {
        await cas.logy("SAML2 metadata management is not available; skipping");
        return;
    }
    const manager = managers[0];
    const entryName = `ManageMetadataExample-${Date.now()}`;
    const stored = await page.evaluate(async (managerName, name) => {
        const preview = await PalantirSamlMetadataImport.request("http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp");
        const response = await fetch(`${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${encodeURIComponent(managerName)}`, {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify({name: name, value: preview.metadata})
        });
        return response.status;
    }, manager.name, entryName);
    await cas.log(`Stored ${entryName} with ${manager.name} (${manager.sourceId}): ${stored}`);
    assert(stored === 200);

    const samlClass = "org.apereo.cas.support.saml.services.SamlRegisteredService";
    await page.evaluate((serviceClass) => openRegisteredServiceWizardDialog({"@class": serviceClass}, {duplicate: true}), samlClass);
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard
        && $("#registeredServiceMetadataManage").is(":visible") && $("#registeredServiceMetadataUpload").is(":visible"),
    {timeout: 30000});
    await cas.sleep(1000);
    await page.evaluate((location) => {
        $("#registeredServiceMetadataLocation").val(location).trigger("input").trigger("blur");
        $("#registeredServiceName").val("ManageMetadataWizard").trigger("input");
    }, manager.sourceId);

    await page.evaluate(() => $("#registeredServiceMetadataManage").trigger("click"));
    await page.waitForFunction(() => $(".swal2-popup:visible").length > 0 && $(".swal2-title").text() === "Discard Your Changes?",
        {timeout: 10000});
    await page.click(".swal2-cancel");
    await cas.sleep(1000);
    const kept = await page.evaluate(() => ({
        open: $("#editServiceWizardDialog").dialog("isOpen"),
        applications: currentActiveTab === Tabs.APPLICATIONS.index
    }));
    await cas.log(`Keep Editing leaves the wizard open: ${JSON.stringify(kept)}`);
    assert(kept.open && kept.applications);

    await page.evaluate(() => $("#registeredServiceMetadataManage").trigger("click"));
    await page.waitForFunction(() => $(".swal2-popup:visible").length > 0 && $(".swal2-title").text() === "Discard Your Changes?",
        {timeout: 10000});
    await page.click(".swal2-deny");
    await page.waitForFunction((sourceId, name) => !$("#editServiceWizardDialog").dialog("isOpen")
        && currentActiveTab === Tabs.PROTOCOLS.index
        && $("#saml2metadatamgmt-tab").is(":visible")
        && String($("#saml2MetadataManagersSelect").val()).toLowerCase() === sourceId.toLowerCase()
        && $.fn.dataTable.isDataTable("#saml2MetadataManagerEntriesTable")
        && $("#saml2MetadataManagerEntriesTable").DataTable().rows().data().toArray().some((row) => row.entryName === name),
    {timeout: 30000}, manager.sourceId, entryName);
    await cas.logg(`Manage Metadata shows the entries of ${manager.name}, ${entryName} among them`);
    await cas.screenshot(page);

    await page.evaluate(async (managerName, name) => {
        const base = `${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${encodeURIComponent(managerName)}`;
        const entries = await (await fetch(base, {headers: {"Accept": "application/json"}})).json();
        for (const entry of entries.filter((candidate) => candidate.name === name)) {
            await fetch(`${base}/${entry.id}`, {method: "DELETE", headers: {"Content-Type": "application/x-www-form-urlencoded"}});
        }
    }, manager.name, entryName);
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
    assert(health.checks["Service Provider"] === "ok");
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
 * Delete the applications an earlier attempt of this scenario left behind, found by name. In-process retries run
 * against the same CAS server, so a step that failed before deleting what it created would otherwise meet its own
 * leftovers on the next attempt.
 *
 * @param page the puppeteer page
 * @param names the names of the applications this scenario creates
 */
async function removeLeftoverApplications(page, names) {
    const removed = await page.evaluate(async (leftovers) => {
        const response = await fetch(CasActuatorEndpoints.registeredServices(), {headers: {"Accept": "application/json"}});
        const body = await response.json();
        const services = (Array.isArray(body?.[1]) ? body[1] : []).filter((service) => leftovers.includes(service.name));
        for (const service of services) {
            await fetch(`${CasActuatorEndpoints.registeredServices()}/${service.id}`, {
                method: "DELETE",
                headers: {"Content-Type": "application/json"}
            });
        }
        if (services.length > 0) {
            await fetchServices();
        }
        return services.map((service) => `${service.name} (${service.id})`);
    }, names);
    if (removed.length > 0) {
        await cas.logy(`Removed applications left by an earlier attempt: ${removed.join(", ")}`);
    }
}

/**
 * Open the Applications page and wait until its table is built. A new visit lands on Home, and the applications
 * steps of this scenario need the table, the cards and the applications loaded.
 *
 * @param page the puppeteer page
 */
async function openApplications(page) {
    await page.evaluate(() => $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.APPLICATIONS.index}]`).trigger("click"));
    await page.waitForFunction(() => currentActiveTab === Tabs.APPLICATIONS.index
        && $.fn.dataTable.isDataTable("#applicationsTable"), {timeout: 30000});
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
    await removeLeftoverApplications(page, ["MetadataHealthExample"]);
    await openApplications(page);
    await verifySamlMetadataImport(page);
    await verifySamlMetadataHealth(page);
    await verifySamlMetadataManage(page);

    if (failures.length > 0) {
        await cas.logr(`Browser reported ${failures.length} error(s):`);
        for (const failure of failures) {
            await cas.logr(`  ${failure}`);
        }
    }
    assert(failures.length === 0, `Browser errors: ${failures.join(" | ")}`);
    await cas.logg("Palantir SAML2 metadata tools verified with no browser errors");

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();
