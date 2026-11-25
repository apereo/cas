/* global $, CAS_FEATURES, CasActuatorEndpoints, PalantirDashboardConfiguration, openRegisteredServiceWizardDialog,
   isPopulatingWizard, ace, currentActiveTab, Swal, fetchServices, PalantirSamlMetadataImport, PalantirSamlMetadataNames, PalantirSamlMetadataUpload,
   Tabs, PalantirScripts, RegisteredServiceSections */
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
            && typeof PalantirScripts === "function";
    }, {timeout: 60000});
    await page.evaluate(() => PalantirScripts.load("applications"));
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
 * Open the wizard for a new SAML2 application and bring its SAML2 section on screen, as choosing it in the wizard's
 * navigation does: the wizard opens on its first step, where the fields of the SAML2 section are not shown.
 *
 * @param page the puppeteer page
 */
async function openSamlWizardSection(page) {
    const samlClass = "org.apereo.cas.support.saml.services.SamlRegisteredService";
    await page.evaluate((serviceClass) => openRegisteredServiceWizardDialog({"@class": serviceClass}, {duplicate: true}), samlClass);
    await page.waitForFunction(() => $("#editServiceWizardDialog").dialog("isOpen") && !isPopulatingWizard, {timeout: 30000});
    await page.evaluate(() => {
        RegisteredServiceSections.activate("editServiceWizardMenuItemSaml");
        RegisteredServiceSections.render();
    });
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
 * The metadata location comes before the entity id. Choosing a metadata query server or a metadata store as the
 * location of an application with no entity id asks whether to accept every service provider of that source: yes
 * makes the entity id .+, no leaves it to be entered.
 *
 * @param page the puppeteer page
 */
async function verifyMetadataSourceTrust(page) {
    await cas.separator();
    await openSamlWizardSection(page);
    await page.waitForFunction(() => $("#registeredServiceMetadataLocation").is(":visible"), {timeout: 10000});
    await cas.sleep(1000);
    const order = await page.evaluate(() => {
        const fields = $("#editServiceWizardSamlContainer").children("[id$='FieldContainer']").map((index, element) => element.id).get();
        return fields.indexOf("registeredServiceMetadataLocationFieldContainer") < fields.indexOf("registeredServiceIdFieldContainer");
    });
    await cas.log(`The metadata location comes before the entity id: ${order}`);
    assert(order);

    const choose = (location) => page.evaluate((value) => {
        $("#registeredServiceId").val("").trigger("input");
        $("#registeredServiceMetadataLocation").val(value).trigger("input").trigger("change");
    }, location);
    const asked = () => page.waitForFunction(() => Swal.isVisible()
        && $(".swal2-title").text() === "Accept Every Service Provider From This Source?", {timeout: 15000});

    await choose("https://mdq.example.org/entities/{0}");
    await asked();
    await page.click(".swal2-confirm");
    await page.waitForFunction(() => $("#registeredServiceId").val() === ".+", {timeout: 10000});
    await cas.log("Accepting every service provider of a metadata query server sets the entity id to .+");

    await choose("mongodb://");
    await asked();
    await page.click(".swal2-cancel");
    await cas.sleep(500);
    const declined = await page.evaluate(() => ({value: $("#registeredServiceId").val(), focused: document.activeElement?.id}));
    await cas.log(`Declining leaves the entity id to be entered: ${JSON.stringify(declined)}`);
    assert(declined.value === "" && declined.focused === "registeredServiceId");

    const plain = await page.evaluate(async () => {
        $("#registeredServiceMetadataLocation").val("https://sp.example.org/metadata.xml").trigger("input").trigger("change");
        await new Promise((resolve) => setTimeout(resolve, 1000));
        return Swal.isVisible();
    });
    assert(!plain, "A metadata location for one service provider should not ask about accepting every one");

    await page.evaluate(() => $("#cancelServiceWizard").trigger("click"));
    await discardUnsavedChanges(page, "SAML2 metadata source trust");
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

    const names = await page.evaluate(async () => {
        const preview = await PalantirSamlMetadataImport.request("http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp");
        const md = "urn:oasis:names:tc:SAML:2.0:metadata";
        const ui = "urn:oasis:names:tc:SAML:metadata:ui";
        const provider = (entityId, inside = "", after = "") => `<md:EntityDescriptor xmlns:md="${md}" xmlns:mdui="${ui}" entityID="${entityId}">
            <md:SPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">${inside}</md:SPSSODescriptor>${after}</md:EntityDescriptor>`;
        return {
            uploaded: PalantirSamlMetadataNames.documentName(preview.metadata),
            displayName: PalantirSamlMetadataNames.documentName(provider("https://sp.example.org/shibboleth",
                "<md:Extensions><mdui:UIInfo><mdui:DisplayName xml:lang=\"en\">Example Portal</mdui:DisplayName></mdui:UIInfo></md:Extensions>")),
            organization: PalantirSamlMetadataNames.documentName(provider("https://sp.example.org/shibboleth", "",
                "<md:Organization><md:OrganizationName xml:lang=\"en\">Example Org</md:OrganizationName></md:Organization>")),
            host: PalantirSamlMetadataNames.documentName(provider("https://www.wiki.example.org/sp")),
            urn: PalantirSamlMetadataNames.documentName(provider("urn:mace:example.org:sp:portal")),
            broken: PalantirSamlMetadataNames.documentName("<md:EntityDescriptor")
        };
    });
    await cas.log(`Names suggested for uploaded metadata: ${JSON.stringify(names)}`);
    assert(names.uploaded !== "" && names.displayName === "Example Portal" && names.organization === "Example Org");
    assert(names.host === "wiki.example.org" && names.urn === "portal" && names.broken === "");

    const contacts = await page.evaluate(() => {
        const md = "urn:oasis:names:tc:SAML:2.0:metadata";
        const person = (type, name, email = "") => `<md:ContactPerson contactType="${type}"><md:GivenName>${name}</md:GivenName>${
            email ? `<md:EmailAddress>mailto:${email}</md:EmailAddress>` : ""}</md:ContactPerson>`;
        const xml = `<md:EntityDescriptor xmlns:md="${md}" entityID="https://contacts.example.org/sp">
            <md:SPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol"/>
            ${person("technical", "One", "one@example.org")}${person("support", "Two")}${person("administrative", "Three")}${person("other", "Four")}
            </md:EntityDescriptor>`;
        const entity = new DOMParser().parseFromString(xml, "application/xml").documentElement;
        const imported = PalantirSamlMetadataImport.describe(entity, "").definition.contacts?.[1] ?? [];
        return imported.map((contact) => contact.name);
    });
    await cas.log(`Contacts imported from metadata: ${JSON.stringify(contacts)}`);
    assert(JSON.stringify(contacts) === JSON.stringify(["One", "Two", "Three", "Four"]));

    await openSamlWizardSection(page);
    await page.waitForFunction(() => $("#registeredServiceMetadataManage").is(":visible") && $("#registeredServiceMetadataUpload").is(":visible"),
        {timeout: 10000});
    await cas.sleep(1000);
    const typed = manager.sourceId.replace("://", "").slice(0, 3);
    await page.evaluate((term) => $("#registeredServiceMetadataLocation").val(term).trigger("focusin"), typed);
    await page.waitForFunction((sourceId) => $("#registeredServiceMetadataLocation").autocomplete("instance")
        && $("#registeredServiceMetadataLocation").autocomplete("widget").find(".palantir-suggestion code").toArray()
            .some((code) => code.textContent === sourceId), {timeout: 10000}, manager.sourceId);
    const suggested = await page.evaluate((sourceId) => {
        const input = $("#registeredServiceMetadataLocation");
        input.autocomplete("widget").find(".palantir-suggestion").filter((index, item) => $(item).find("code").text() === sourceId)
            .find(".ui-menu-item-wrapper").trigger("click");
        const picked = input.val();
        input.autocomplete("close").val("https://sp.example.org/custom-metadata.xml").trigger("input");
        const typedValue = input.val();
        input.val("").trigger("input");
        $("#registeredServiceMetadataLocation").val("").trigger("focusin");
        return {picked, typedValue};
    }, manager.sourceId);
    await cas.sleep(1000);
    assert(!await page.evaluate(() => Swal.isVisible()), "A question about a location since changed should not be asked");
    await page.evaluate(() => $("#registeredServiceMetadataLocation").val("").trigger("focusin"));
    await page.waitForFunction(() => {
        const shown = $("#registeredServiceMetadataLocation").autocomplete("widget").find(".palantir-suggestion code").toArray()
            .map((code) => code.textContent);
        return shown.includes("https://") && shown.includes("https://mdq.incommon.org/entities/{0}");
    }, {timeout: 10000});
    await page.evaluate(() => $("#registeredServiceMetadataLocation").autocomplete("close"));
    await cas.log(`Typing ${typed} into the metadata location suggests ${manager.sourceId}: ${JSON.stringify(suggested)}`);
    assert(suggested.picked === manager.sourceId && suggested.typedValue === "https://sp.example.org/custom-metadata.xml");
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
 * metadata is shown; then delete the application. The card's menu does not always open on the first click on a slower
 * machine, so it is opened again until it offers the check.
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
    let opened = false;
    for (let attempt = 1; attempt <= 3 && !opened; attempt++) {
        const covered = await page.evaluate((selector) => {
            const button = document.querySelector(selector);
            const bounds = button.getBoundingClientRect();
            const top = document.elementFromPoint(bounds.left + bounds.width / 2, bounds.top + bounds.height / 2);
            return button.contains(top) ? null : `${top?.tagName ?? "nothing"}#${top?.id ?? ""}.${String(top?.className ?? "").replace(/\s+/g, ".")}`;
        }, menu);
        if (covered !== null) {
            await cas.logy(`The menu button of application ${serviceId} is covered by ${covered} before click ${attempt}`);
        }
        await page.click(menu);
        opened = await page.waitForFunction(() => $(".context-menu-list:visible .context-menu-item:visible")
            .filter((index, item) => $(item).text().includes("Check Metadata Health")).length > 0, {timeout: 5000})
            .then(() => true, () => false);
        if (!opened) {
            await cas.logy(`The menu of application ${serviceId} did not open on click ${attempt}`);
        }
    }
    assert(opened, `The menu of application ${serviceId} offers Check Metadata Health`);
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
 * The Applications page is not open yet, and opening it reads the applications, so they are not read again here:
 * in CI, the attempts that read them first, before the page had built its table, stopped answering.
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
    await verifyMetadataSourceTrust(page);
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
