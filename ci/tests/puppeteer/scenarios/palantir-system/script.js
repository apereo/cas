/* global $, CAS_FEATURES, CasActuatorEndpoints, PalantirDashboardConfiguration, openRegisteredServiceWizardDialog,
   currentActiveTab, notyf, Tabs, PalantirModules, signInsChart,
   PalantirStartupTimeline */
const assert = require("assert");
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

/**
 * Open the Application Startup page twice: the steps are read once, drawn with a progress bar that goes away once
 * they are all shown, and found by name.
 *
 * @param page the puppeteer page
 */
async function verifyStartupTimeline(page) {
    await cas.separator();
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.startup()))) {
        await cas.logy("The startup endpoint is not available; skipping");
        return;
    }
    const requests = [];
    const listener = (request) => {
        if (/\/actuator\/startup/.test(request.url())) {
            requests.push(request.url());
        }
    };
    page.on("request", listener);
    const open = (tab) => page.evaluate((href) => {
        $("#systemTabButton").trigger("click");
        $("#system-tabs").tabs("option", "active", $(`#system-tabs ul li a[href='${href}']`).parent().index());
    }, tab);
    await open("#casstartup-tab");
    await page.waitForFunction(() => $("#startupProgress").prop("hidden")
        && ($("#startupTimelineList .startup-step-row").length > 0 || $("#startupTimelineList p").length > 0), {timeout: 60000});
    await open("#systemstatus-tab");
    await open("#casstartup-tab");
    await cas.sleep(1000);
    page.off("request", listener);
    const drawn = await page.evaluate(() => ({
        rows: $("#startupTimelineList .startup-step-row").length,
        steps: PalantirStartupTimeline.steps?.length ?? 0
    }));
    await cas.logg(`Application Startup drew ${drawn.rows} of ${drawn.steps} steps with ${requests.length} request(s)`);
    assert(requests.length === 1 && drawn.rows === drawn.steps);
    if (drawn.rows > 0) {
        const name = await page.evaluate(() => $("#startupTimelineList > .startup-step-row").last().find(".startup-step-name").first().text());
        await page.evaluate((value) => $("#startupStepFilter").val(value).trigger("input"), name);
        await page.waitForFunction((value) => $("#startupTimelineList .startup-step-row:visible").first()
            .find(".startup-step-name").first().text() === value, {timeout: 10000}, name);
        await page.evaluate(() => $("#startupStepFilter").val("").trigger("input"));
    }
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
    await verifyReadOnlyEndpoint(page);
    await verifyCasModules(page);
    await verifySystemMetrics(page);
    await verifySignIns(page);
    await verifyStartupTimeline(page);

    if (failures.length > 0) {
        await cas.logr(`Browser reported ${failures.length} error(s):`);
        for (const failure of failures) {
            await cas.logr(`  ${failure}`);
        }
    }
    assert(failures.length === 0, `Browser errors: ${failures.join(" | ")}`);
    await cas.logg("Palantir system pages verified with no browser errors");
    await cas.closeBrowser(browser);
})();
