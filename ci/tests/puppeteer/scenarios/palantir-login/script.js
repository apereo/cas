/* global $, CAS_FEATURES, CasActuatorEndpoints, openRegisteredServiceWizardDialog, isPopulatingWizard,
   activateDashboardTab, currentActiveTab, Swal, Tabs, Chart, PalantirHome, topApplicationsChart, CasDiscoveryProfile,
   PalantirScripts, ace, Diff, initializeDashboardTab, openNewConfigurationPropertyDialog,
   PalantirLinks, PalantirPages, PalantirPanelFocus, registeredServicesById, navigateToApplication, PalantirCsrf,
   palantirOneAtATime */
const assert = require("assert");
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
/**
 * Pages and applications have links: showing a page puts its key in the address, Back and Forward move between pages,
 * and opening a link shows the page, or reveals the application, it names. The page shown last is remembered by key.
 *
 * @param page the puppeteer page
 */
async function verifyLinks(page) {
    await cas.separator();
    const available = await page.evaluate(() => ["system", "applications"].every((key) => PalantirPages.available(Tabs.byKey(key))));
    if (!available) {
        await cas.logy("The System or Applications page is not offered; skipping the links");
        return;
    }
    await page.evaluate(() => PalantirPages.open(Tabs.SYSTEM));
    await page.waitForFunction(() => location.hash === "#system" && currentActiveTab === Tabs.SYSTEM.index, {timeout: 10000});
    await page.evaluate(() => PalantirPages.open(Tabs.APPLICATIONS));
    await page.waitForFunction(() => location.hash === "#applications" && currentActiveTab === Tabs.APPLICATIONS.index, {timeout: 10000});
    const remembered = await page.evaluate(() => localStorage.getItem("PalantirSelectedTab"));
    assert(remembered === "applications", `The page shown last is remembered as ${remembered}`);
    await page.goBack();
    await page.waitForFunction(() => location.hash === "#system" && currentActiveTab === Tabs.SYSTEM.index, {timeout: 10000});
    await page.goForward();
    await page.waitForFunction(() => location.hash === "#applications" && currentActiveTab === Tabs.APPLICATIONS.index, {timeout: 10000});
    await cas.logg("Showing pages puts their links in the address, and Back and Forward move between them");

    const id = await page.evaluate(() => [...registeredServicesById.keys()][0] ?? null);
    if (id === null) {
        await cas.logy("No application to link to; skipping the application link");
        return;
    }
    await cas.goto(page, `${DASHBOARD_URL}#applications/${id}`);
    await waitForDashboard(page);
    await page.waitForFunction((serviceId) => currentActiveTab === Tabs.APPLICATIONS.index
        && location.hash === `#applications/${serviceId}`
        && $("#applicationsTable").DataTable().rows(".selected").data().toArray().some((row) => String(row.serviceId) === serviceId),
    {timeout: 30000}, id);
    const link = await page.evaluate((serviceId) => PalantirLinks.url(Tabs.APPLICATIONS, serviceId), id);
    assert(link.endsWith(`/palantir/dashboard#applications/${id}`), link);
    await cas.logg(`A link to an application opens Applications with it selected: ${link}`);

    const cancelled = await page.evaluate(() => {
        const banners = [];
        const banner = window.displayBanner;
        window.displayBanner = (message) => banners.push(String(message?.message ?? message));
        return Promise.all([navigateToApplication("987654321987"), PalantirLinks.follow("#applications")])
            .finally(() => {
                window.displayBanner = banner;
            })
            .then(() => ({banners, selected: $("#applicationsTable").DataTable().rows(".selected").count()
                + $("#applicationsCards .palantir-application-card.is-selected").length}));
    });
    assert(cancelled.banners.length === 0, `Going back to Applications stops the application still on its way: ${JSON.stringify(cancelled)}`);
    assert(cancelled.selected === 0, `Going back to Applications selects no application: ${JSON.stringify(cancelled)}`);
    await cas.logg("Following a link to Applications stops an application that was still being looked for");

    await cas.goto(page, `${DASHBOARD_URL}#system`);
    await waitForDashboard(page);
    await page.waitForFunction(() => currentActiveTab === Tabs.SYSTEM.index, {timeout: 30000});
    await page.evaluate(() => PalantirPages.open(Tabs.APPLICATIONS));
    await page.waitForFunction(() => currentActiveTab === Tabs.APPLICATIONS.index, {timeout: 10000});
}

/**
 * Check that a change sent with the browser's session needs the CSRF token, and that Palantir has the token to send.
 * The requests go around Palantir's own sending, which adds the token, and are expected to fail; their failures are
 * taken off the list of unexpected ones.
 *
 * @param page the puppeteer page
 * @param failures the failures recorded so far
 */
async function verifySessionWritesCarryToken(page, failures) {
    await cas.separator();
    const before = failures.length;
    const answers = await page.evaluate(() => {
        const send = (token) => new Promise((resolve) => {
            const xhr = new XMLHttpRequest();
            xhr.open("POST", CasActuatorEndpoints.health());
            if (token) {
                xhr.setRequestHeader("X-CSRF-TOKEN", token);
            }
            xhr.onloadend = () => resolve(xhr.status);
            xhr.send();
        });
        const token = PalantirCsrf.token();
        return Promise.all([send(null), send(token)]).then(([without, sent]) => ({token: token !== null, without, sent}));
    });
    failures.push(...failures.splice(before).filter((failure) => !/actuator\/health/.test(failure)));
    await cas.log(`A change sent with the session: ${JSON.stringify(answers)}`);
    assert(answers.token, "Palantir has the CSRF token to send");
    assert(answers.without === 403, `A change without the token is refused: ${answers.without}`);
    assert(answers.sent !== 403, `A change with the token gets past the check: ${answers.sent}`);
    await cas.logg("Changes made with the browser's session need the CSRF token, which Palantir sends");
}

/**
 * Check that a poll runs one at a time: calls made while its request is on its way start nothing new.
 *
 * @param page the puppeteer page
 */
async function verifyPollsDoNotPileUp(page) {
    const started = await page.evaluate(() => {
        let count = 0;
        let finish = null;
        const poll = palantirOneAtATime(() => {
            count++;
            return new Promise((resolve) => {
                finish = resolve;
            });
        });
        poll();
        poll();
        poll();
        const whileRunning = count;
        finish();
        return new Promise((resolve) => setTimeout(resolve, 0)).then(() => {
            poll();
            return {whileRunning, after: count};
        });
    });
    assert(started.whileRunning === 1 && started.after === 2, `Polls run one at a time: ${JSON.stringify(started)}`);
    await cas.logg("A slow poll is not started again while it is still on its way");
}

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
    const before = await page.evaluate(() => ({
        order: $("#homeGrid > [data-home-widget]").map((index, element) => element.dataset.homeWidget).get(),
        layout: localStorage.getItem("PalantirHomeLayout"),
        title: $("#homeGrid > [data-home-widget='health'] h3")[0].getBoundingClientRect().height,
        ticks: PalantirHome.healthChart?.scales?.y?.options?.ticks?.font?.size ?? null
    }));
    await page.click("#homeGrid [data-home-for='health'][data-home-action='spotlight']");
    await page.waitForFunction(() => $("#homeGrid").hasClass("is-spotlight")
        && $("#homeGrid > [data-home-widget]:visible").length === 1
        && $("#homeGrid > [data-home-widget='health']").is(":visible"), {timeout: 10000});
    const spotlit = await page.evaluate(() => {
        const card = $("#homeGrid > [data-home-widget='health']");
        return {
            width: Math.round(card.outerWidth()),
            grid: Math.round($("#homeGrid").width()),
            pressed: card.find("[data-home-action='spotlight']").attr("aria-pressed"),
            layout: localStorage.getItem("PalantirHomeLayout"),
            title: card.find("h3")[0].getBoundingClientRect().height,
            ticks: PalantirHome.healthChart?.scales?.y?.options?.ticks?.font?.size ?? null,
            zoom: Number.parseFloat(getComputedStyle(document.documentElement).getPropertyValue("--palantir-spotlight-zoom"))
        };
    });
    await cas.log(`Home card in the spotlight: ${JSON.stringify(spotlit)}`);
    assert(spotlit.pressed === "true" && spotlit.width >= spotlit.grid - 2 && spotlit.layout === before.layout);
    assert(spotlit.zoom <= 1 || spotlit.title > before.title * 1.1, `The card in the spotlight is magnified: ${before.title} to ${spotlit.title}`);
    assert(spotlit.zoom <= 1 || spotlit.ticks === null || spotlit.ticks > (before.ticks ?? 12));
    await cas.screenshot(page);
    await page.keyboard.press("Escape");
    await page.waitForFunction(() => !$("#homeGrid").hasClass("is-spotlight"), {timeout: 10000});
    const restored = await page.evaluate(() => $("#homeGrid > [data-home-widget='health'] h3")[0].getBoundingClientRect().height);
    assert(Math.abs(restored - before.title) < 1, `The card is back to its size: ${restored} for ${before.title}`);
    await page.click("#homeGrid [data-home-for='health'][data-home-action='spotlight']");
    await page.waitForFunction(() => $("#homeGrid").hasClass("is-spotlight"), {timeout: 10000});
    await page.click("#homeGrid [data-home-for='health'][data-home-action='spotlight']");
    await page.waitForFunction((order) => !$("#homeGrid").hasClass("is-spotlight")
        && JSON.stringify($("#homeGrid > [data-home-widget]:visible").map((index, element) => element.dataset.homeWidget).get())
            === JSON.stringify(order), {timeout: 10000}, before.order);
    await cas.logg("A Home card can be put in the spotlight and taken out of it, with the layout as it was");

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
    const readTheme = () => page.evaluate(() => {
        const chart = Object.values(Chart.instances ?? {}).find((instance) => instance.scales?.y?.getLabelItems?.().length > 0);
        const label = chart?.scales.y.getLabelItems()[0];
        return {
            theme: document.documentElement.getAttribute("data-theme"),
            stored: localStorage.getItem("PalantirTheme"),
            label: label ? String(label.options?.color ?? label.color) : null,
            text: getComputedStyle(document.documentElement).getPropertyValue("--palantir-text-muted").trim()
        };
    });
    const initial = await readTheme();
    await cas.click(page, "#palantirThemeToggle");
    const switched = await readTheme();
    await cas.logg(`Theme switched from ${initial.theme} to ${switched.theme}`);
    assert(switched.theme !== initial.theme);
    assert(switched.stored === switched.theme);
    await cas.log(`Chart labels follow the theme at once: ${initial.label} to ${switched.label}`);
    assert(switched.label === null || switched.label === switched.text, `Chart labels are ${switched.label}, not ${switched.text}`);
    await cas.click(page, "#palantirThemeToggle");
    const restored = await readTheme();
    assert(restored.theme === initial.theme);
    assert(restored.stored === initial.theme);
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
    await page.waitForFunction(() => $(".swal2-title").text() === "Reset All Preferences?", {timeout: 10000});
    await page.click(".swal2-cancel");
    await page.waitForFunction(() => !Swal.isVisible(), {timeout: 10000});
    const kept = await page.evaluate(() => sessionStorage.getItem("PalantirResetProbe"));
    await cas.logg(`Declining to reset the preferences keeps them: ${kept}`);
    assert(kept === "kept");
    await page.evaluate(() => sessionStorage.removeItem("PalantirResetProbe"));
    await page.evaluate(() => $("#palantirSettingsDialog").dialog("close"));
}

/**
 * Check that the editor, the diff library and the application wizard are not part of the page itself but are fetched
 * when first needed, with the editor in its minified build.
 *
 * @param page the puppeteer page
 */
async function verifyScriptsOnFirstUse(page) {
    await cas.separator();
    const state = await page.evaluate(async () => {
        const deferred = $("script[defer]").toArray().map((script) => script.src)
            .filter((src) => /ace-builds|\/diff\/|palantir-services-wizard|palantir-services-chains/.test(src));
        await PalantirScripts.load("wizard");
        return {
            deferred,
            ace: typeof ace,
            diff: typeof Diff,
            wizard: typeof openRegisteredServiceWizardDialog,
            minified: $("script[src*='ace-builds']").toArray().every((script) => script.src.includes("/src-min-noconflict/"))
        };
    });
    await cas.log(`Scripts fetched on first use: ${JSON.stringify(state)}`);
    assert(state.deferred.length === 0);
    assert(state.ace === "object" && state.diff === "object" && state.wizard === "function" && state.minified);
    await cas.logg("The editor, the diff library and the wizard are fetched on first use");
}

/**
 * The authentication handlers show as cards, one per handler on the table's current page, with the table itself out
 * of sight; searching narrows the cards.
 *
 * @param page the puppeteer page
 */
async function verifyAuthenticationHandlerCards(page) {
    await cas.separator();
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.authenticationHandlers()))) {
        await cas.logy("The authentication handlers endpoint is not available; skipping");
        return;
    }
    await page.evaluate(async () => {
        $("#authenticationTabButton").trigger("click");
        await initializeDashboardTab(Tabs.AUTHENTICATION);
    });
    await page.waitForFunction(() => $("#authenticationHandlersCards .palantir-record-card").length > 0, {timeout: 30000});
    const shown = await page.evaluate(() => ({
        cards: $("#authenticationHandlersCards .palantir-record-card").length,
        rows: $("#authenticationHandlersTable").DataTable().rows({page: "current", search: "applied"}).count(),
        tableVisible: $("#authenticationHandlersTable").is(":visible"),
        first: $("#authenticationHandlersCards .palantir-record-card h4").first().text()
    }));
    await cas.log(`Authentication handlers as cards: ${JSON.stringify(shown)}`);
    assert(shown.cards === shown.rows && !shown.tableVisible);
    const narrowed = await page.evaluate((name) => {
        $("#authenticationHandlersTable").DataTable().search(name).draw();
        const count = $("#authenticationHandlersCards .palantir-record-card").length;
        $("#authenticationHandlersTable").DataTable().search("").draw();
        return count;
    }, shown.first);
    assert(narrowed >= 1 && narrowed <= shown.cards);
    await cas.logg("Authentication handlers show as cards that follow the table's search");
}

/**
 * Type property names in the new configuration property dialog and press Enter: the name typed is taken as it is,
 * even when the suggestions hold longer names that start with it, and a name that is suggested exactly is taken too.
 *
 * @param page the puppeteer page
 */
async function verifyPropertyNameEntry(page) {
    await cas.separator();
    await page.evaluate(async () => {
        $("#configurationTabButton").trigger("click");
        await initializeDashboardTab(Tabs.CONFIGURATION);
        openNewConfigurationPropertyDialog({});
    });
    await page.waitForFunction(() => $("#newConfigurationDialog").dialog("isOpen"), {timeout: 10000});
    for (const name of ["cas.server", "cas.server.name", "custom.example.setting"]) {
        await page.evaluate(() => {
            const select = $("#newConfigPropertyName")[0].tomselect;
            select.clear(true);
            select.focus();
        });
        await page.keyboard.type(name);
        await cas.sleep(500);
        await page.keyboard.press("Enter");
        await cas.sleep(500);
        const chosen = await page.evaluate(() => $("#newConfigPropertyName")[0].tomselect.getValue());
        await cas.log(`Typing ${name} and pressing Enter chooses ${chosen}`);
        assert(chosen === name);
    }
    await cas.logg("The property name typed is the one chosen when Enter is pressed");
    await page.evaluate(() => $("#newConfigurationDialog").dialog("close"));
}

/**
 * Turn zen mode on with Shift+Z from the Applications page, check that it hides the header, navigation and footer while
 * the page shortcuts still work, then leave it again.
 *
 * @param page the puppeteer page
 */
async function verifyZenMode(page) {
    await page.evaluate(() => {
        document.activeElement?.blur();
        $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.APPLICATIONS.index}]`).trigger("click");
    });
    await page.waitForFunction(() => currentActiveTab === Tabs.APPLICATIONS.index, {timeout: 10000});
    await page.evaluate(async () => {
        await initializeDashboardTab(Tabs.APPLICATIONS);
        await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    });
    const focus = await page.evaluate(() => {
        const expected = PalantirPanelFocus.field(`#attribute-tab-${Tabs.APPLICATIONS.index}`)[0];
        return {expected: expected ? expected.id || expected.className : null, focused: expected ? document.activeElement === expected : null};
    });
    await cas.log(`Opening the Applications page gives focus to ${JSON.stringify(focus)}`);
    if (focus.expected !== null) {
        assert(focus.focused);
        await page.keyboard.press("Escape");
        await page.waitForFunction(() => document.activeElement === document.body || document.activeElement === null, {timeout: 10000});
        await cas.logg("Opening a page puts the cursor in its first field, and Escape leaves it so the shortcuts work again");
    }
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
 * The multifactor providers page lists every provider CAS registers, ranked, and shows the selected one with its key
 * facts and its settings, each once and with its source; Duo Security says whether Duo answers, from the Duo ping
 * endpoint, and the filter narrows both the list and the settings.
 *
 * @param page the puppeteer page
 */
async function verifyMfaProviders(page) {
    await cas.separator();
    const expected = await page.evaluate(() => Object.keys(CasDiscoveryProfile.multifactorAuthenticationProviders()));
    if (expected.length === 0) {
        await cas.logy("No multifactor provider is registered; skipping");
        return;
    }
    await page.evaluate(() => {
        $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.MFA.index}]`).trigger("click");
        $("#mfaProvidersTab").trigger("click");
    });
    await page.waitForFunction((count) => $("#mfaProvidersList [data-mfa-provider-id]").length === count
        && $("#mfaProviderDetail .palantir-mfa-providers__fact").length > 0
        && !$("#mfaProvidersList .palantir-mfa-providers__chip").toArray().some((chip) => /…$/.test(chip.textContent.trim())),
    {timeout: 60000}, expected.length);
    const listed = await page.evaluate(() => $("#mfaProvidersList [data-mfa-provider-id]").toArray()
        .map((item) => `${item.dataset.mfaProviderId}: ${$(item).find(".palantir-mfa-providers__chip").text().trim()}`));
    await cas.logg(`MFA providers: ${listed.join(" | ")}`);

    const duo = expected.find((id) => id.includes("duo"));
    if (duo && await page.evaluate(() => Boolean(CasActuatorEndpoints.duoPing()))) {
        await page.click(`[data-mfa-provider-id="${duo}"]`);
        await page.waitForFunction(() => $("#mfaProviderDetail [data-mfa-provider-action=ping]").length === 1, {timeout: 10000});
        const status = await page.evaluate(() => $("#mfaProviderDetail .palantir-mfa-providers__title .palantir-mfa-providers__chip").text().trim());
        await cas.logg(`${duo}: ${status}`);
        assert(/^(Reachable · \d+ ms|Unreachable|Not Checked)$/.test(status));
        const settings = await page.evaluate(() => $("#mfaProviderSettingsTable").DataTable().rows().data().toArray()
            .map((entry) => entry.name));
        assert(settings.length > 0 && settings.every((name) => name.startsWith("cas.authn.mfa.duo[")));
        assert(new Set(settings).size === settings.length);
        const hidden = await page.evaluate(() => {
            const entry = $("#mfaProviderSettingsTable").DataTable().rows().data().toArray().find((row) => /secret-key/.test(row.name));
            return entry ? $(entry.value).text().trim() : "";
        });
        assert(hidden === "" || hidden === "••••••", `A Duo secret is shown: ${hidden}`);

        if (await page.evaluate(() => Boolean(CasActuatorEndpoints.duoAdmin() && CasActuatorEndpoints.duoAccountStatus()))) {
            await page.type("#mfaDuoUsername", "duocode");
            await page.click(".palantir-mfa-duo__lookup button[type=submit]");
            await page.waitForFunction(() => $(".palantir-mfa-duo .palantir-mfa-duo__status").length === 1
                || $(".palantir-mfa-duo .palantir-mfa-duo__note.is-alert").length > 0, {timeout: 30000});
            const account = await page.evaluate(() => ({
                signIn: $(".palantir-mfa-duo__status .palantir-mfa-providers__chip").text().trim(),
                facts: $(".palantir-mfa-duo .palantir-mfa-providers__fact small").toArray().map((label) => label.textContent),
                actions: $(".palantir-mfa-duo__actions [data-mfa-provider-action]").toArray().map((button) => button.dataset.mfaProviderAction),
                note: $(".palantir-mfa-duo .palantir-mfa-duo__note").text().trim()
            }));
            await cas.logg(`Duo account of duocode: ${JSON.stringify(account)}`);
            assert(account.signIn.length > 0, `Duo did not report how duocode signs in: ${account.note}`);
            assert(account.facts.includes("Account") && account.facts.includes("Last Login"));
            assert(account.actions.includes("duo-bypass") && account.actions.includes("duo-edit"));
            await page.click(".palantir-mfa-duo__actions [data-mfa-provider-action=duo-edit]");
            await page.waitForFunction(() => $(".palantir-mfa-duo__edit select[name=status]").length === 1, {timeout: 10000});
            const status = await page.evaluate(() => $(".palantir-mfa-duo__edit select[name=status]").val());
            assert(["AUTH", "ALLOW", "DENY"].includes(status));
            await page.click(".palantir-mfa-duo__edit [data-mfa-provider-action=duo-cancel]");
            await page.waitForFunction(() => $(".palantir-mfa-duo__edit").length === 0, {timeout: 10000});
        }
    }

    await page.evaluate(() => $("#mfaProvidersFilter").val("no provider has this setting").trigger("input"));
    await page.waitForFunction(() => $("#mfaProvidersList [data-mfa-provider-id]").length === 0, {timeout: 10000});
    await page.evaluate(() => $("#mfaProvidersFilter").val("").trigger("input"));
    await page.waitForFunction((count) => $("#mfaProvidersList [data-mfa-provider-id]").length === count, {timeout: 10000}, expected.length);
    await cas.logg("Filtering providers and settings narrows the list and brings it back");
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
    await verifySessionWritesCarryToken(page, failures);
    await verifyPollsDoNotPileUp(page);
    await verifyScriptsOnFirstUse(page);
    await verifyHome(page);
    await verifyRegisteredServices(page);
    await verifyTheme(page);
    await verifyTabs(page);
    await verifyLinks(page);
    await verifyFeatureCatalog(page);
    await verifyPropertyNameEntry(page);
    await verifyAuthenticationHandlerCards(page);
    await verifySettingsDialog(page);
    await verifyZenMode(page);
    await verifyShortcutsOverlay(page);
    await verifyMfaProviders(page);

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
