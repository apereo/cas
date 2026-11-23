/* global $, CAS_FEATURES, CasActuatorEndpoints, openRegisteredServiceWizardDialog, isPopulatingWizard,
   currentActiveTab, registeredServicesById, PalantirServiceHistory, Swal, applicationActionHandler,
   applicationRowContext, fetchServices, notyf, PalantirApplicationAccess, Tabs, PalantirApplicationSelection,
   importServiceFiles, importServiceFile, palantirParseJson */
const assert = require("assert");
const fs = require("fs");
const os = require("os");
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
    const strategies = await page.evaluate(() => $("#registeredServiceAccessStrategy option").map((_, option) => option.value).get());
    await cas.logg(`Access strategies offered: ${strategies.length}`);
    assert(strategies.includes("org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy"));
    assert(!strategies.includes("org.apereo.cas.permit.PermitRegisteredServiceAccessStrategy"));
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
 * Import a YAML application definition through the Import button's file input, check that it is imported once
 * and shows up in the applications table, and delete it afterwards. Importing several files includes one CAS cannot
 * read, which CAS refuses with 400; that refusal is expected, so it is not counted as a browser error.
 *
 * @param page the puppeteer page
 * @param failures the failures recorded so far
 */
async function verifyServiceImport(page, failures) {
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

    await cas.separator();
    const names = ["MultiImportOne", "MultiImportTwo", "MultiImportThree"];
    await removeLeftoverApplications(page, names);
    const folder = fs.mkdtempSync(path.join(os.tmpdir(), "palantir-import-many-"));
    try {
        const definitions = names.map((name, index) => {
            const definition = path.join(folder, `${name}.json`);
            fs.writeFileSync(definition, JSON.stringify({
                "@class": "org.apereo.cas.services.CasRegisteredService",
                serviceId: `^https://multi-import-${index}.example.org/.*`,
                name: name
            }));
            return definition;
        });
        const broken = path.join(folder, "Broken.json");
        fs.writeFileSync(broken, "{ not json");
        const archive = path.join(folder, "Archive.zip");
        fs.writeFileSync(archive, "PK");

        await (await page.$("#serviceFileInput")).uploadFile(definitions[0], archive);
        await page.waitForFunction(() => $(".swal2-title").text() === "Choose Files of One Kind", {timeout: 10000});
        await cas.logg("A JSON file and an archive together are refused before anything is sent");
        await page.evaluate(() => Swal.close());

        const before = failures.length;
        await (await page.$("#serviceFileInput")).uploadFile(...definitions, broken);
        await page.waitForFunction(() => $(".swal2-title").text() === "Import 4 Applications?", {timeout: 10000});
        await page.click(".swal2-confirm");
        await page.waitForFunction(() => $(".swal2-title").text() === "3 Applications Imported", {timeout: 60000});
        const summary = await page.evaluate(() => $(".swal2-html-container").text());
        await cas.logg(`Importing several JSON files: ${summary}`);
        assert(summary.includes("Broken.json"));
        const unexpected = failures.splice(before).filter((failure) => !/registeredServices\/import/.test(failure));
        failures.push(...unexpected);
        await page.evaluate(() => Swal.close());
        const many = await page.evaluate((imported) => [...registeredServicesById.values()]
            .filter((service) => imported.includes(String(service.name))).map((service) => service.id), names);
        assert(many.length === 3, `Imported applications: ${JSON.stringify(many)}`);
    } finally {
        await removeLeftoverApplications(page, names);
        fs.rmSync(folder, {recursive: true, force: true});
    }
    await verifyImportsStopWithTheSession(page);
    await verifyListAndExportGuards(page);
}

/**
 * Check that an older read of the applications answered late never replaces a newer one, that large ids are kept
 * exactly, and that a login page answered to an export is not saved as one.
 *
 * @param page the puppeteer page
 */
async function verifyListAndExportGuards(page) {
    const guarded = await page.evaluate(() => {
        const ajax = $.ajax;
        const pending = [];
        const listing = (options) => typeof options === "object" && String(options.url ?? "") === CasActuatorEndpoints.registeredServices()
            && String(options.type ?? "GET").toUpperCase() === "GET";
        $.ajax = function (options) {
            if (!listing(options)) {
                return ajax.apply(this, arguments);
            }
            const deferred = $.Deferred();
            pending.push(deferred);
            return deferred.promise();
        };
        const service = (id, name) => ({"@class": "org.apereo.cas.services.CasRegisteredService", id, name, serviceId: `^https://${name}.example.org`});
        const older = fetchServices();
        const newer = fetchServices();
        $.ajax = ajax;
        pending[1].resolve(["java.util.ArrayList", [service("9007199254740993", "Newer")]]);
        pending[0].resolve(["java.util.ArrayList", [service("9007199254740992", "Older")]]);
        return Promise.all([older, newer]).then(() => {
            const shown = [...registeredServicesById.values()].map((entry) => entry.name);
            const exact = palantirParseJson("[{\"id\": 9007199254740993}, {\"id\": 42}]").map((entry) => entry.id);
            return {shown, exact};
        });
    });
    await cas.log(`Reading the applications in overlapping requests: ${JSON.stringify(guarded)}`);
    assert(JSON.stringify(guarded.shown) === JSON.stringify(["Newer"]), "An older list answered late does not replace a newer one");
    assert(JSON.stringify(guarded.exact) === JSON.stringify(["9007199254740993", 42]), "Large ids are kept exactly");
    await page.evaluate(() => fetchServices());

    const exported = await page.evaluate(() => {
        const id = [...registeredServicesById.keys()][0];
        const original = window.fetch;
        window.fetch = (url, options) => String(url).includes("/registeredServices/export")
            ? Promise.resolve(new Response("<html><body>Log In</body></html>", {status: 200, headers: {"Content-Type": "text/html"}}))
            : original(url, options);
        PalantirApplicationSelection.set(id, true);
        return PalantirApplicationSelection.exportSelected()
            .then(() => ({title: $(".swal2-title").text(), kept: PalantirApplicationSelection.selected.has(String(id))}))
            .finally(() => {
                window.fetch = original;
                Swal.close();
                PalantirApplicationSelection.clear();
            });
    });
    await cas.log(`Exporting when the login page answers: ${JSON.stringify(exported)}`);
    assert(exported.title === "The Selected Applications Were Not Exported" && exported.kept,
        "A login page is not saved as an export, and the selection is kept");
    await cas.logg("Late lists, large ids and login pages answered to exports are handled");
}

/**
 * Answer imports as an expired session does, with the login page instead of the application CAS created, and check
 * that a batch stops sending files and a single file is not reported as imported.
 *
 * @param page the puppeteer page
 */
async function verifyImportsStopWithTheSession(page) {
    const answered = await page.evaluate(async () => {
        const ajax = $.ajax;
        const sent = [];
        const importing = (answer) => function (options) {
            const imports = typeof options === "object" && options?.type === "POST" && String(options.url ?? "").endsWith("/import");
            return imports ? answer(options) : ajax.apply(this, arguments);
        };
        const confirm = async (title) => {
            const until = Date.now() + 10000;
            while (!(Swal.isVisible() && $(".swal2-title").text() === title) && Date.now() < until) {
                await new Promise((resolve) => setTimeout(resolve, 50));
            }
            Swal.clickConfirm();
        };
        $.ajax = importing((options) => {
            sent.push(options.data.name);
            const expired = sent.length >= 2;
            const deferred = $.Deferred();
            const request = deferred.promise();
            request.status = expired ? 200 : 201;
            request.getResponseHeader = (name) => name.toLowerCase() === "id"
                ? (expired ? null : "987654")
                : (expired ? "text/html;charset=UTF-8" : "application/json");
            deferred.resolve("");
            return request;
        });
        try {
            const files = ["StopOne.json", "StopTwo.json", "StopThree.json"].map((name) => new File(["{}"], name, {type: "application/json"}));
            const batch = importServiceFiles(files);
            await confirm("Import 3 Applications?");
            await batch;
            const stopped = {sent: [...sent], title: $(".swal2-title").text(), text: $(".swal2-html-container").text()};
            Swal.close();
            const single = importServiceFile(new File(["{}"], "StopSingle.json", {type: "application/json"}));
            await confirm("Import StopSingle.json?");
            await single;
            const refused = $(".swal2-title").text();
            Swal.close();

            $.ajax = importing((options) => {
                const deferred = $.Deferred();
                const request = deferred.promise();
                const dropped = options.data.name === "DropTwo.json";
                request.status = dropped ? 0 : 201;
                request.getResponseHeader = (name) => name.toLowerCase() === "id" && !dropped ? "987655" : null;
                if (dropped) {
                    deferred.reject(request);
                } else {
                    deferred.resolve("");
                }
                return request;
            });
            const dropping = importServiceFiles(["DropOne.json", "DropTwo.json"].map((name) => new File(["{}"], name, {type: "application/json"})));
            await confirm("Import 2 Applications?");
            await dropping;
            const dropped = {title: $(".swal2-title").text(), text: $(".swal2-html-container").text()};
            Swal.close();

            $.ajax = importing(() => {
                const deferred = $.Deferred();
                const request = deferred.promise();
                request.status = 200;
                request.responseText = "";
                request.getResponseHeader = () => null;
                deferred.resolve("");
                return request;
            });
            const legacy = importServiceFile(new File([new Uint8Array([80, 75])], "legacy.zip", {type: "application/zip"}));
            await confirm("Import legacy.zip?");
            await legacy;
            const earlier = {title: $(".swal2-title").text(), text: $(".swal2-html-container").text()};
            Swal.close();
            return {stopped, refused, dropped, earlier};
        } finally {
            $.ajax = ajax;
        }
    });
    await cas.log(`Imports once the session expired: ${JSON.stringify(answered)}`);
    assert(JSON.stringify(answered.stopped.sent) === JSON.stringify(["StopOne.json", "StopTwo.json"]), "The batch stops sending files");
    assert(answered.stopped.title === "Import Stopped" && answered.stopped.text.includes("StopThree.json"));
    assert(answered.refused === "Nothing Imported", "A single file the login page answered is not reported as imported");
    assert(answered.dropped.title === "1 Application Imported" && answered.dropped.text.includes("DropTwo.json")
        && answered.dropped.text.includes("may already be imported"), "A file CAS did not answer for is not reported as rejected");
    assert(answered.earlier.title === "Imported" && answered.earlier.text.includes("did not say how many"),
        "An archive an earlier CAS imported without a count is reported as imported");
    await cas.logg("Imports stop once CAS answers with the login page, and say when CAS did not answer");
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
    const selectBoth = async () => {
        await page.waitForFunction(() => !Swal.isVisible() && $(".swal2-container").length === 0, {timeout: 10000});
        await (await cell(ids[0])).click();
        await (await cell(ids[1])).click();
        await page.waitForFunction(() => !$("#applicationsSelectionBar").prop("hidden")
            && $(".palantir-selection-bar__count").text().startsWith("2 "), {timeout: 10000});
    };
    const cleared = async (action) => {
        await page.waitForFunction(() => PalantirApplicationSelection.selected.size === 0 && $("#applicationsSelectionBar").prop("hidden")
            && $("#applicationsTable tbody tr.is-checked").length === 0, {timeout: 10000});
        await cas.logg(`${action} the selection clears it and hides the selection bar`);
    };
    await selectBoth();
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
    await cleared("Exporting");

    const reimported = await page.evaluate(async (url) => {
        const bytes = await (await fetch(url, {credentials: "include"})).arrayBuffer();
        const importing = importServiceFile(new File([bytes], "services.zip", {type: "application/zip"}));
        const until = Date.now() + 10000;
        while (!(Swal.isVisible() && $(".swal2-title").text() === "Import services.zip?") && Date.now() < until) {
            await new Promise((resolve) => setTimeout(resolve, 50));
        }
        Swal.clickConfirm();
        await importing;
        const answer = {title: $(".swal2-title").text(), text: $(".swal2-html-container").text()};
        Swal.close();
        return answer;
    }, exported.url());
    await cas.log(`Importing the exported archive: ${JSON.stringify(reimported)}`);
    assert(reimported.title === "Imported" && reimported.text.startsWith("2 applications from services.zip are imported"),
        `The archive import says what CAS imported: ${JSON.stringify(reimported)}`);

    for (const disable of [true, false]) {
        const button = disable ? "disableSelectedServices" : "enableSelectedServices";
        await selectBoth();
        await page.waitForFunction((name) => !$(`button[name=${name}]`).prop("disabled"), {timeout: 10000}, button);
        await page.click(`button[name=${button}]`);
        await page.waitForSelector(".swal2-confirm", {visible: true});
        await page.click(".swal2-confirm");
        await page.waitForFunction((expected) => $(".swal2-title").text() === `2 Applications ${expected ? "Disabled" : "Enabled"}`,
            {timeout: 60000}, disable);
        await page.evaluate(() => Swal.close());
        const saved = [await strategy(ids[0]), await strategy(ids[1])];
        await cas.logg(`${disable ? "Disabled" : "Enabled"} the selection: ${JSON.stringify(saved)}`);
        assert(saved.every((value) => isDisabledAccessStrategy(value) === disable));
        await cleared(disable ? "Disabling" : "Enabling");
    }

    await selectBoth();
    await page.click("button[name=deleteSelectedServices]");
    await page.waitForSelector(".swal2-confirm", {visible: true});
    await page.click(".swal2-confirm");
    await page.waitForFunction(() => $(".swal2-title").text() === "2 Applications Deleted", {timeout: 60000});
    const remaining = await page.evaluate(async (created) => {
        const response = await fetch(CasActuatorEndpoints.registeredServices(), {headers: {"Accept": "application/json"}});
        const registered = new Set((await response.json())[1].map((service) => String(service.id)));
        return created.map((id) => registered.has(String(id)));
    }, ids);
    await cas.logg(`After deleting the selection, the applications still registered: ${remaining.join(", ")}`);
    assert(!remaining[0] && !remaining[1] && remaining[2]);
    await cleared("Deleting");

    await page.evaluate(async (id) => {
        Swal.close();
        $("#applicationsTable").DataTable().search("").draw();
        await fetch(`${CasActuatorEndpoints.registeredServices()}/${id}`, {method: "DELETE", headers: {"Content-Type": "application/json"}});
        fetchServices();
    }, ids[2]);
}

/**
 * Disable and enable an application whose OR chain has one branch switched off: enabling it again must give back the
 * chain it had, rather than switch that branch on as well and let in more people than before.
 *
 * @param page the puppeteer page
 */
async function verifyEnableKeepsChain(page) {
    await cas.separator();
    const outcome = await page.evaluate(() => {
        const strategy = {
            "@class": PalantirApplicationAccess.CHAINING,
            operator: "OR",
            strategies: ["java.util.ArrayList", [
                {"@class": PalantirApplicationAccess.DEFAULT, enabled: false, requiredAttributes: {"@class": "java.util.HashMap", memberOf: ["java.util.HashSet", ["admins"]]}},
                {"@class": "org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy", startingDateTime: "2020-01-01T00:00:00Z"}
            ]]
        };
        const disabled = PalantirApplicationAccess.disable(strategy);
        const enabled = PalantirApplicationAccess.enable(disabled);
        return {
            disabled: PalantirApplicationAccess.isDisabled(disabled),
            same: JSON.stringify(enabled) === JSON.stringify(strategy)
        };
    });
    await cas.log(`Disabled: ${outcome.disabled}, chain given back as it was: ${outcome.same}`);
    assert(outcome.disabled);
    assert(outcome.same);
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
    await removeLeftoverApplications(page, ["HistoryExample", "ImportExample", "BulkExampleOne", "BulkExampleTwo", "BulkExampleThree",
        "MultiImportOne", "MultiImportTwo", "MultiImportThree"]);
    await openApplications(page);
    await verifyServiceHistory(page);
    await verifyServiceImport(page, failures);
    await verifyApplicationBulkActions(page);
    await verifyEnableKeepsChain(page);

    if (failures.length > 0) {
        await cas.logr(`Browser reported ${failures.length} error(s):`);
        for (const failure of failures) {
            await cas.logr(`  ${failure}`);
        }
    }
    assert(failures.length === 0, `Browser errors: ${failures.join(" | ")}`);
    await cas.logg("Palantir application history and actions verified with no browser errors");
    await cas.closeBrowser(browser);
})();
