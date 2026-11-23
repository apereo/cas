/* global $, CAS_FEATURES, CasActuatorEndpoints, openRegisteredServiceWizardDialog, currentActiveTab,
   registeredServicesById, PalantirUserLookup, Tabs, PalantirCommandPalette, PalantirCredentialRepositories, PalantirAccessCheck,
   Swal,
   PalantirAttributeRelease */
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
 * Open the command palette with Ctrl+K and with "/", and check that Enter goes to the chosen match: a page by its
 * name, then the second of two applications with the same name, whose row alone is marked.
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
    const placement = await page.evaluate(() => {
        const box = $(".palantir-command-palette-dialog")[0].getBoundingClientRect();
        return {
            width: Math.round(box.width),
            expectedWidth: Math.min(880, window.innerWidth - 32),
            offsetX: Math.round(box.left + box.width / 2 - window.innerWidth / 2),
            offsetY: Math.round(box.top + box.height / 2 - window.innerHeight / 2)
        };
    });
    await cas.log(`Command palette placement: ${JSON.stringify(placement)}`);
    assert(Math.abs(placement.width - placement.expectedWidth) <= 2 && Math.abs(placement.offsetX) <= 2 && Math.abs(placement.offsetY) <= 2);
    await cas.logg("The command palette is large and in the middle of the screen");
    await cas.logg(`Command palette offers ${first} first for "${target.label}"`);
    assert(first === target.label);
    await page.keyboard.press("Enter");
    await page.waitForFunction((index) => currentActiveTab === index
        && !$("#palantirCommandPalette").dialog("isOpen"), {timeout: 10000}, target.index);
    await cas.logg(`Command palette opened ${target.label}`);

    await page.keyboard.down("Control");
    await page.keyboard.press("k");
    await page.keyboard.up("Control");
    await page.waitForFunction((label) => $("#palantirCommandPalette").dialog("isOpen")
        && $(".palantir-command-palette__item.ui-state-active .palantir-command-palette__label").text() === label,
    {timeout: 10000}, target.label);
    const restored = await page.evaluate(() => {
        const input = $("#palantirCommandPaletteInput")[0];
        return {value: input.value, selected: input.selectionStart === 0 && input.selectionEnd === input.value.length};
    });
    await cas.logg(`Command palette reopens with ${JSON.stringify(restored)} and ${target.label} marked`);
    assert(restored.value === target.label && restored.selected);
    await page.keyboard.press("Escape");
    await page.waitForFunction(() => !$("#palantirCommandPalette").dialog("isOpen"), {timeout: 10000});

    const application = "Portal";
    await cas.sleep(1000);
    await page.evaluate(() => document.activeElement?.blur());
    await page.keyboard.press("/");
    const match = await searchCommandPalette(page, application);
    assert(match === application);
    await page.keyboard.press("ArrowDown");
    const chosen = await page.evaluate(() => $(".palantir-command-palette__item.ui-state-active .palantir-command-palette__detail")
        .text().match(/#(\d+)/)?.[1]);
    await cas.log(`Command palette chose the application ${application} with id ${chosen}`);
    assert(chosen === "1" || chosen === "2");
    await page.keyboard.press("Enter");
    await page.waitForFunction((id) => {
        const selected = $("#applicationsTable").DataTable().rows(".selected").data().toArray();
        return currentActiveTab === 0 && selected.length === 1 && String(selected[0].serviceId) === id;
    }, {timeout: 10000}, chosen);
    await cas.logg(`Command palette marked the application ${application} with id ${chosen}, and only that one`);

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
    assert(first === "Look Up User casuser", `First match was ${first}`);
    await page.keyboard.press("Enter");
    await page.waitForFunction(() => currentActiveTab === 16
        && $("#userLookupResults .palantir-user-card").length > 0
        && $("#userLookupResults .palantir-user-card.is-loading").length === 0, {timeout: 60000});
    await page.waitForFunction(() => document.activeElement?.id === "userLookupUsername", {timeout: 10000});
    await cas.logg("The Users page puts the cursor in the username field");
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
    const tokens = await page.evaluate(() => ({
        available: PalantirUserLookup.availableSources().some((source) => source.key === "oauthTokens"),
        read: PalantirUserLookup.counts.has("oauthTokens"),
        buttons: $("#userLookupResults [data-user-read]").length
    }));
    await cas.log(`OAuth and OpenID Connect tokens load with the other sources: ${JSON.stringify(tokens)}`);
    assert(tokens.buttons === 0 && (!tokens.available || tokens.read));
    const empty = await page.evaluate(() => PalantirUserLookup.availableSources()
        .filter((source) => PalantirUserLookup.counts.get(source.key) === 0)
        .map((source) => ({key: source.key, hidden: $(`#userLookupResults [data-user-source="${source.key}"]`).prop("hidden") === true})));
    await cas.log(`Sources with nothing recorded have no card: ${JSON.stringify(empty)}`);
    assert(empty.every((source) => source.hidden));
    const release = await page.evaluate(async () => {
        const card = $("#userLookupResults [data-user-source='release']");
        if (card.length === 0 || card.prop("hidden") || PalantirAttributeRelease.choices.length === 0) {
            return null;
        }
        card.prop("open", true);
        $("#userReleaseApplication").val("").trigger("focus").autocomplete("search", "");
        await new Promise((resolve) => setTimeout(resolve, 500));
        const menu = $("#userReleaseApplication").autocomplete("widget");
        const shown = {
            styled: menu.hasClass("palantir-access__suggestions") && menu.is(":visible"),
            header: menu.find(".palantir-access__suggestions-header").text(),
            first: menu.find(".palantir-access__suggestion").length > 0
        };
        menu.find(".ui-menu-item").not(".palantir-access__suggestions-header").first().find(".ui-menu-item-wrapper").trigger("click");
        return {...shown, picked: $("#userReleaseApplication").val(), selected: PalantirAttributeRelease.selected?.name ?? null,
            select: $("#userLookupResults select#userReleaseApplication").length};
    });
    if (release !== null) {
        await cas.log(`Attribute release picks its application like the access check: ${JSON.stringify(release)}`);
        assert(release.styled && /Application/.test(release.header) && release.first);
        assert(release.selected !== null && release.picked === release.selected && release.select === 0);
    }
    const activity = await page.evaluate(() => ({
        shown: $("#userLookupActivityTable").length > 0,
        headers: $("#userLookupActivityTable thead th").toArray().map((cell) => cell.textContent.trim()),
        targets: $("#userLookupActivityTable tbody td:nth-child(3)").toArray()
            .filter((cell) => $(cell).find(".audit-target-application, .audit-target-value, .audit-target-none").length > 0).length,
        rows: $("#userLookupActivityTable tbody tr").not(":has(.dt-empty)").length
    }));
    await cas.log(`Recent activity of the user: ${JSON.stringify(activity)}`);
    if (activity.shown) {
        assert(JSON.stringify(activity.headers) === JSON.stringify(["When", "Action", "Target", "Resource", "Client"]));
        assert(activity.targets === activity.rows);
        await cas.logg("Recent activity lists the target of each record, as the audit log does");
    }
    await cas.screenshot(page);
    const linked = await page.evaluate(() => location.hash);
    assert(linked.startsWith("#users/"), `The lookup is linked as ${linked}`);
    await page.evaluate(() => history.back());
    await page.waitForFunction(() => location.hash === "#users" && $("#userLookupResults").children().length === 0
        && $("#userLookupSummary").text() === "" && $("#userLookupUsername").val() === "", {timeout: 10000});
    await cas.logg(`Going Back from ${linked} to #users clears the user that was shown`);
    await page.evaluate(() => history.forward());
    await page.waitForFunction((hash) => location.hash === hash && $("#userLookupResults .palantir-user-card").length > 0,
        {timeout: 60000}, linked);
    await cas.logg(`Going Forward to ${linked} shows that user again`);
    const unexpected = failures.splice(before).filter((failure) => !/^response: 404 GET .*\/actuator\//.test(failure)
        && !/status of 404/.test(failure));
    failures.push(...unexpected);
}

/**
 * Draw an SSO session and tokens of two applications and check that each is a card of its own with labelled facts,
 * the applications and scopes as chips, the tokens grouped by application, and only shortened ids on show.
 *
 * @param page the puppeteer page
 */
async function verifySessionAndTokenCards(page) {
    const drawn = await page.evaluate(() => {
        const now = Date.now();
        const ticket = "TGT-1-abcdefghijklmnopqrstuvwxyz0123456789-example";
        const session = PalantirUserLookup.sessionCards([{
            authentication_date: new Date(now - 600000).toISOString(),
            number_of_uses: 3,
            ticket_granting_ticket: ticket,
            remember_me: true,
            authentication_attributes: {clientIpAddress: ["127.0.0.1"]},
            expiration_policy: {maxExpirationTime: new Date(now + 3600000).toISOString()},
            authenticated_services: {"ST-1": {originalUrl: "https://app.example.org/home"}}
        }]);
        const secret = "AT-1-abcdefghijklmnopqrstuvwxyz0123456789-example";
        const tokens = PalantirUserLookup.tokenGroups([
            {id: secret, clientId: "client-one", scopes: ["openid", "profile"], grantType: "AUTHORIZATION_CODE",
                creationTime: new Date(now - 60000).toISOString(), expirationPolicy: {timeToLive: 3600, timeToIdle: 600}},
            {id: "RT-1-abcdefghijklmnopqrstuvwxyz-example", "@class": "OAuth20DefaultRefreshToken", clientId: "client-one",
                scopes: ["offline_access"], creationTime: new Date(now - 60000).toISOString()},
            {id: "AT-2-zyxwvutsrqponmlkjihgfedcba-example", clientId: "client-two", scopes: ["openid"],
                creationTime: new Date(now - 120000).toISOString()}
        ]);
        const sessionView = $(`<div>${session}</div>`);
        const tokenView = $(`<div>${tokens}</div>`);
        return {
            sessionLabels: sessionView.find(".palantir-identity-facts dt").toArray().map((label) => label.textContent.trim()),
            badges: sessionView.find(".palantir-identity-badge").toArray().map((badge) => badge.textContent.trim()),
            applications: sessionView.find(".palantir-identity-chip").toArray().map((chip) => chip.textContent.trim()),
            ends: sessionView.find(".palantir-identity-facts__fact:contains('Ends') dd").text().trim(),
            ticketShown: sessionView.find(".palantir-identity-item__code").text().includes(ticket),
            ticketCopied: sessionView.find("[data-user-copy]").attr("data-user-copy") === ticket,
            end: sessionView.find("[data-user-action='end-session']").text().trim(),
            clients: tokenView.find(".palantir-token-client__name").toArray().map((name) => name.textContent.trim()),
            kinds: tokenView.find(".palantir-token .palantir-identity-item__name").toArray().map((name) => name.textContent.trim()),
            scopes: tokenView.find(".palantir-identity-chip.is-scope").length,
            grant: tokenView.find(".palantir-identity-facts__fact:contains('Grant') dd").first().text().trim(),
            secretShown: tokenView.text().includes(secret),
            revokeAll: tokenView.find("[data-user-action='revoke-all-tokens']").text().trim(),
            revokes: tokenView.find("[data-user-action='revoke-token']").length
        };
    });
    await cas.log(`Session and token cards: ${JSON.stringify(drawn)}`);
    assert(JSON.stringify(drawn.sessionLabels) === JSON.stringify(["IP Address", "Ends", "Uses"]));
    assert(JSON.stringify(drawn.badges) === JSON.stringify(["Active", "Remember Me"]));
    assert(JSON.stringify(drawn.applications) === JSON.stringify(["app.example.org/home"]));
    assert(drawn.ends.length > 0 && !drawn.ticketShown && drawn.ticketCopied && drawn.end === "End Session");
    assert(JSON.stringify(drawn.clients) === JSON.stringify(["client-one", "client-two"]));
    assert(JSON.stringify(drawn.kinds) === JSON.stringify(["Access Token", "Refresh Token", "Access Token"]));
    assert(drawn.scopes === 4 && drawn.grant === "Authorization Code" && !drawn.secretShown);
    assert(drawn.revokeAll === "Revoke All Tokens" && drawn.revokes === 3);
    await cas.logg("SSO sessions and tokens are drawn as cards with labelled facts, grouped tokens and shortened ids");

    const accounts = await page.evaluate(() => {
        const account = $(`<div>${PalantirUserLookup.accountCards([{username: "casuser", name: "Example User", email: "casuser@example.org",
            phone: "+15550100", source: "json", multifactorAuthenticationEligible: "TRUE", delegatedAuthenticationEligible: "UNDEFINED",
            requestPassword: false, allowedDelegatedClients: ["GitHubClient"], attributes: {department: ["engineering"]}}])}</div>`);
        const devices = $(`<div>${PalantirUserLookup.deviceCards([
            {id: "1", name: "Phone", type: "Mobile", source: "Duo Security", details: {providerId: "mfa-duo"}},
            {id: "2", name: "Key", source: "WebAuthn FIDO2", details: {providerId: "mfa-webauthn"}}], "casuser")}</div>`);
        return {
            avatar: account.find(".palantir-account__avatar").text().trim(),
            contacts: account.find(".palantir-account__contact").toArray().map((contact) => contact.textContent.trim()),
            facts: account.find(".palantir-identity-facts__fact").toArray()
                .map((fact) => `${$(fact).find("dt").text().trim()}=${$(fact).find("dd").text().trim()}`),
            chips: account.find(".palantir-identity-chip").length,
            kinds: devices.find(".palantir-device").toArray().map((device) => device.className.match(/is-(\w+)/)[1]),
            removes: devices.find("[data-user-action='remove-mfa-device']").toArray()
                .map((button) => `${button.dataset.provider}/${button.dataset.key}/${button.textContent.trim()}`)
        };
    });
    await cas.log(`Passwordless account and MFA device cards: ${JSON.stringify(accounts)}`);
    assert(accounts.avatar === "EU" && JSON.stringify(accounts.contacts) === JSON.stringify(["casuser@example.org", "+15550100"]));
    assert(JSON.stringify(accounts.facts) === JSON.stringify(["MFA Eligible=Yes", "Delegated Sign-In=Default",
        "Asks For Password=No", "Selection Menu=Hidden"]) && accounts.chips === 2);
    assert(JSON.stringify(accounts.kinds) === JSON.stringify(["duo", "webauthn"]));
    assert(JSON.stringify(accounts.removes) === JSON.stringify(["mfa-duo/1/Remove", "mfa-webauthn/2/Remove"]));
    await cas.logg("Passwordless accounts and MFA devices are drawn as cards with labelled facts");
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
 * List the accounts casuser may impersonate on the Impersonation section of the Authentication page, which shows one
 * card per account, then fetch the attributes of one of them, which opens attribute resolution on the person
 * directory page with that account filled in and its attributes listed.
 *
 * @param page the puppeteer page
 */
async function verifyImpersonationAccounts(page) {
    await cas.separator();
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.impersonation()))) {
        await cas.logy("Impersonation endpoint is not available; skipping");
        return;
    }
    await page.evaluate(() => $("#authenticationTabButton").trigger("click"));
    await page.waitForFunction(() => $("#authentication-tabs").data("ui-tabs") !== undefined, {timeout: 30000});
    await page.evaluate(() => {
        const tabs = $("#authentication-tabs");
        tabs.tabs("option", "active", tabs.find("> ul > li").index($("#surrogacy").parent()));
    });
    await page.waitForSelector("#surrogateUsername", {visible: true});
    await page.type("#surrogateUsername", "casuser");
    await page.click("#fetchSurrogateAccountsButton");
    await page.waitForFunction(() => $("#surrogateAccountsCards [data-surrogate-account]").length === 2, {timeout: 30000});
    const accounts = await page.evaluate(() => $("#surrogateAccountsCards [data-surrogate-account]").toArray()
        .map((card) => card.dataset.surrogateAccount).sort());
    await cas.logg(`${await page.evaluate(() => $("#surrogateAccountsSummary").text())} ${accounts.join(", ")}`);
    assert(accounts.join(",") === "banderson,jsmith");
    await cas.screenshot(page);
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.personDirectory()))) {
        await cas.logy("Person directory endpoint is not available; skipping the attributes of an account");
        return;
    }
    await page.click("#surrogateAccountsCards [data-surrogate-attributes='banderson']");
    await page.waitForFunction(() => currentActiveTab === Tabs.PERSON_DIRECTORY.index
        && $("#personUsername").val() === "banderson"
        && $("#personDirectoryTable").DataTable().rows().count() > 0, {timeout: 30000});
    const attributes = await page.evaluate(() => $("#personDirectoryTable").DataTable().rows().count());
    await cas.logg(`Attribute resolution for banderson lists ${attributes} attribute(s)`);
    await cas.screenshot(page);
}

/**
 * Look up the throttled attempts of one username on the Throttles page, check the hint and the button that shows all
 * attempts again, then show them all. The page needs the throttles endpoint, which the throttle module provides.
 *
 * @param page the puppeteer page
 */
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
 * The multifactor credential repositories each get a card with their counts; an export of Google Authenticator
 * accounts imports back as an archive, and deleting every record asks for the repository's name first.
 *
 * @param page the puppeteer page
 */
async function verifyCredentialRepositories(page) {
    await cas.separator();
    const expected = await page.evaluate(() => PalantirCredentialRepositories.available().map((repository) => repository.key));
    if (expected.length === 0) {
        await cas.logy("No multifactor credential repository endpoint is available; skipping");
        return;
    }
    await page.evaluate(() => {
        $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.MFA.index}]`).trigger("click");
        $("#mfaRepositoriesTab").trigger("click");
    });
    await page.waitForFunction((count) => $("#mfaRepositoriesCards [data-mfa-repository]").length === count
        && $("#mfaRepositoriesCards .palantir-record-card__title p").toArray().every((line) => /user/.test(line.textContent)),
    {timeout: 30000}, expected.length);
    const lines = await page.evaluate(() => $("#mfaRepositoriesCards .palantir-record-card__title p").toArray().map((line) => line.textContent));
    await cas.logg(`Credential repositories: ${lines.join(" | ")}`);

    if (expected.includes("gauthCredentialRepository")) {
        const roundTrip = await page.evaluate(async () => {
            const endpoint = CasActuatorEndpoints.gauthCredentialRepository();
            const exported = await fetch(`${endpoint}/export`, {credentials: "include"});
            const archive = await exported.blob();
            const imported = await fetch(`${endpoint}/import`, {
                method: "POST",
                credentials: "include",
                headers: {"Content-Type": "application/zip", "Accept": "application/json"},
                body: archive
            });
            return {exported: exported.status, imported: imported.status, body: await imported.json()};
        });
        await cas.log(`Google Authenticator export ${roundTrip.exported}, import ${roundTrip.imported}: ${JSON.stringify(roundTrip.body)}`);
        assert(roundTrip.exported === 200 && roundTrip.imported === 201 && typeof roundTrip.body.imported === "number");
    }

    await page.evaluate(() => {
        $("#mfaRepositoriesCards [data-mfa-repository] [data-mfa-repository-action=import]").first().closest("[data-mfa-repository]")
            .find("[data-mfa-repository-action=delete]").prop("disabled", false).trigger("click");
    });
    await page.waitForFunction(() => Swal.isVisible() && $(".swal2-confirm").is(":visible"), {timeout: 10000});
    const confirmation = await page.evaluate(() => ({
        typed: $(".swal2-input").is(":visible"),
        confirm: $(".swal2-confirm").text().trim(),
        cancel: $(".swal2-cancel").text().trim()
    }));
    await cas.logg(`Deleting every record asks: ${JSON.stringify(confirmation)}`);
    assert(!confirmation.typed && confirmation.confirm === "Delete All" && confirmation.cancel === "Cancel");
    await page.click(".swal2-cancel");
    await page.waitForFunction(() => !Swal.isVisible(), {timeout: 10000});
}

/**
 * Show the audit records of one username on the audit events page, then those of every username again. The admin
 * who signed in has audit records, and every record shown must be one of theirs.
 *
 * @param page the puppeteer page
 */
async function verifyAuditEventsUsername(page) {
    await cas.separator();
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.auditLog()))) {
        await cas.logy("Audit log endpoint is not available; skipping");
        return;
    }
    await page.evaluate(() => {
        $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.LOGGING.index}]`).trigger("click");
        $("#auditEvents").trigger("click");
    });
    await page.waitForFunction(() => $("#auditEventsUsername").is(":visible"), {timeout: 30000});
    await page.evaluate(() => $("#auditEventsUsername").val("CASADMIN"));
    const [response] = await Promise.all([
        page.waitForResponse((answer) => answer.url().includes("principal=%5ECASADMIN%24"), {timeout: 30000}),
        page.click("#filterAuditEventsButton")
    ]);
    assert(response.status() === 200);
    await page.waitForFunction(() => $("#auditEventsUsernameHint").text().includes("for CASADMIN")
        && $("#showAllAuditEventsButton").is(":visible"), {timeout: 15000});
    const principals = await page.evaluate(() => $("#auditEventsTable").DataTable().rows().data().toArray()
        .map((entry) => String(entry.principal).toLowerCase()));
    await cas.logg(`Audit records of one username: ${await page.evaluate(() => $("#auditEventsUsernameHint").text())}`);
    assert(principals.every((principal) => principal === "casadmin"));
    await page.click("#showAllAuditEventsButton");
    await page.waitForFunction(() => $("#auditEventsUsername").val() === "" && !$("#showAllAuditEventsButton").is(":visible"), {timeout: 15000});
}

/**
 * Show the consent decisions of one username on the Consent page, then every decision again. The username has no
 * decisions, so the page asks for that username alone and says it found none.
 *
 * @param page the puppeteer page
 */
async function verifyConsentUsername(page) {
    await cas.separator();
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.attributeConsent()))) {
        await cas.logy("Attribute consent endpoint is not available; skipping");
        return;
    }
    await page.evaluate(() => $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.CONSENT.index}]`).trigger("click"));
    await page.waitForFunction(() => $("#consentUsername").is(":visible"), {timeout: 30000});
    await page.evaluate(() => $("#consentUsername").val("PalantirNobody"));
    const [response] = await Promise.all([
        page.waitForResponse((answer) => answer.url().toLowerCase().endsWith("/attributeconsent/palantirnobody"), {timeout: 30000}),
        page.click("#filterConsentButton")
    ]);
    await page.waitForFunction(() => $("#consentUsernameHint").text().startsWith("0 consent decisions for PalantirNobody")
        && $("#showAllConsentButton").is(":visible"), {timeout: 10000});
    await cas.logg(`Consent decisions of one username: ${response.status()}, ${await page.evaluate(() => $("#consentUsernameHint").text())}`);
    assert(response.status() === 200);
    await page.click("#showAllConsentButton");
    await page.waitForFunction(() => $("#consentUsername").val() === "" && !$("#showAllConsentButton").is(":visible"), {timeout: 10000});
}

/**
 * Check access from the access page: a user without the group an application requires is denied, with the failed
 * step and the missing value shown, and an application without rules lets the user in. Both checks are listed as
 * recent checks.
 *
 * @param page the puppeteer page
 */
async function verifyAccessCheck(page) {
    await cas.separator();
    await page.evaluate(() => $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.ACCESS_STRATEGY.index}]`).trigger("click"));
    await page.waitForFunction(() => currentActiveTab === Tabs.ACCESS_STRATEGY.index
        && typeof PalantirAccessCheck !== "undefined" && PalantirAccessCheck.editor !== null, {timeout: 30000});
    if (!await page.evaluate(() => Boolean(CasActuatorEndpoints.serviceAccess()))) {
        await cas.logy("The service access endpoint is not available; skipping the access check");
        return;
    }
    await page.evaluate(() => localStorage.removeItem(PalantirAccessCheck.STORAGE_KEY));
    await page.click("#accessApplication");
    await page.type("#accessApplication", "Staff");
    await page.waitForFunction(() => $(".palantir-access__suggestions:visible .palantir-access__suggestion").length > 0, {timeout: 10000});
    const header = await page.evaluate(() => $(".palantir-access__suggestions:visible .palantir-access__suggestions-header").text().trim());
    await cas.log(`Suggestions: ${header}`);
    assert(header === "1 Application");
    await page.evaluate(() => $(".palantir-access__suggestions:visible .ui-menu-item:not(.palantir-access__suggestions-header)")
        .first().trigger("mouseenter").trigger("click"));
    await page.waitForFunction(() => PalantirAccessCheck.selected?.name === "Staff Portal", {timeout: 10000});
    const prefilled = await page.evaluate(() => ({
        value: $("#accessApplication").val(),
        resolved: $("#accessTargetHint").hasClass("is-resolved"),
        name: $("#accessTargetHint strong").text().trim()
    }));
    await cas.log(`Staff Portal is checked with ${prefilled.value}, resolved to ${prefilled.name}`);
    assert(prefilled.value === "https://staff.example.org/" && prefilled.resolved && prefilled.name === "Staff Portal");
    await page.evaluate(() => {
        $("#accessApplication").val("https://staff.example.org/home");
        PalantirAccessCheck.update();
    });
    await page.type("#accessUsername", "casuser");
    await page.click("#accessStrategyButton");
    await page.waitForFunction(() => $("#accessResult .palantir-access__verdict").length === 1, {timeout: 30000});
    const denied = await page.evaluate(() => ({
        verdict: $("#accessResult .palantir-access__verdict").attr("class"),
        title: $("#accessResult .palantir-access__verdict h3").text().trim(),
        reason: $("#accessResult .palantir-access__verdict p").text().trim(),
        failed: $("#accessResult .palantir-access__gate.is-failed .palantir-access__gate-title").toArray().map((gate) => gate.textContent.trim()),
        missing: $("#accessResult .palantir-access__rule.is-failed code").toArray().map((code) => code.textContent.trim())
    }));
    await cas.logg(`Access check for Staff Portal: ${JSON.stringify(denied)}`);
    assert(denied.verdict.includes("is-denied") && denied.title === "casuser Is Denied");
    assert(denied.failed.includes("Required Attributes") && denied.missing.includes("memberOf"));
    assert(denied.reason.includes("memberOf needs admins"));

    await page.evaluate(() => $("#accessTargetField [data-access-action=clear]").trigger("click"));
    assert(await page.evaluate(() => $("#accessApplication").val() === "" && PalantirAccessCheck.selected === null));
    await page.type("#accessApplication", "https://portal.example.org/one/home");
    await page.click("#accessStrategyButton");
    await page.waitForFunction(() => $("#accessResult .palantir-access__verdict.is-allowed").length === 1, {timeout: 30000});
    const allowed = await page.evaluate(() => ({
        title: $("#accessResult .palantir-access__verdict h3").text().trim(),
        gates: $("#accessResult .palantir-access__gate").length,
        recent: $("#accessRecentList [data-access-recent]").length
    }));
    await cas.logg(`Access check for Portal: ${JSON.stringify(allowed)}`);
    assert(allowed.title === "casuser May Access Portal" && allowed.gates > 0 && allowed.recent === 2);

    await page.evaluate(() => PalantirAccessCheck.openFor(3));
    await page.waitForFunction(() => $("#accessApplication").val() === "https://staff.example.org/"
        && PalantirAccessCheck.selected?.id === 3, {timeout: 10000});
    await cas.log("The applications' Check Access action opens the access check with Staff Portal picked");

    const protocols = await page.evaluate(() => {
        const saved = new Map(registeredServicesById);
        registeredServicesById.clear();
        registeredServicesVersion++;
        [
            {"@class": "org.apereo.cas.support.saml.services.SamlRegisteredService", id: 91, name: "Every SP", serviceId: ".*", evaluationOrder: 0},
            {"@class": "org.apereo.cas.services.OidcRegisteredService", id: 92, name: "Every RP", serviceId: "^http.*", clientId: "Client-One", evaluationOrder: 1},
            {"@class": "org.apereo.cas.services.CasRegisteredService", id: 93, name: "Every Client", serviceId: "^http.*", evaluationOrder: 2}
        ].forEach((service) => registeredServicesById.set(String(service.id), service));
        const resolved = (value) => {
            const answer = PalantirAccessCheck.resolve(value);
            return `${answer.parameter?.name}:${answer.service?.id}`;
        };
        PalantirAccessCheck.choose(null);
        const url = resolved("https://app.example.org/");
        const client = resolved("client-one");
        PalantirAccessCheck.choose(registeredServicesById.get("91"));
        const entity = resolved("https://sp.example.org/shibboleth");
        PalantirAccessCheck.choose(null);
        registeredServicesById.set("94", {"@class": "org.apereo.cas.support.saml.services.SamlRegisteredService", id: 94, name: "Named SP",
            serviceId: "https://named\\.example\\.org/sp", evaluationOrder: -1});
        registeredServicesVersion++;
        const pasted = PalantirAccessCheck.resolve("https://named.example.org/sp");
        $("#accessApplication").val("https://named.example.org/sp");
        PalantirAccessCheck.update();
        const offered = $("#accessTargetHint [data-access-action='pick']").map((_, button) => button.dataset.serviceId).get();
        $("#accessTargetHint [data-access-action='pick'][data-service-id='94']").trigger("click");
        const picked = resolved("https://named.example.org/sp");
        const kept = $("#accessApplication").val();
        PalantirAccessCheck.choose(null);
        registeredServicesById.clear();
        saved.forEach((service, id) => registeredServicesById.set(id, service));
        registeredServicesVersion++;
        return {url, client, entity, ambiguous: (pasted.choices ?? []).map((service) => service.id), offered, picked, kept};
    });
    const unavailable = await page.evaluate(() => [
        PalantirAccessCheck.verdict({username: "casuser", label: "Portal"}, 404, null).style,
        PalantirAccessCheck.verdict({username: "casuser", label: "Portal"}, 500, {allowed: false, reason: "broken", steps: []}).style
    ]);
    assert(unavailable.every((style) => style === "is-unavailable"));
    const repeated = await page.evaluate(() => {
        $("#accessCheckPassword").prop("checked", false).trigger("change");
        PalantirAccessCheck.repeat({label: "Portal", username: "casuser", password: true,
            parameter: {name: "service", value: "https://portal.example.org/one/home"}, applicationId: "1"});
        const asked = $("#accessCheckPassword").prop("checked") && !$("#accessPasswordField").prop("hidden")
            && document.activeElement?.id === "accessPassword";
        $("#accessCheckPassword").prop("checked", false).trigger("change");
        return asked;
    });
    await cas.log(`Repeating a check that checked the password asks for it: ${repeated}`);
    assert(repeated);
    await cas.log(`With catch-all SAML, OIDC and CAS applications: ${JSON.stringify(protocols)}`);
    assert(protocols.url === "service:93" && protocols.client === "client_id:92" && protocols.entity === "entityId:91");
    assert(JSON.stringify(protocols.ambiguous) === JSON.stringify([94, 93]) && JSON.stringify(protocols.offered) === JSON.stringify(["94", "93"]));
    assert(protocols.picked === "entityId:94" && protocols.kept === "https://named.example.org/sp");
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

/**
 * Wait for the two applications of the scenario to be loaded, since the command palette finds an application among
 * them. Both are named Portal, so going to one must mark it by its id rather than by its name.
 *
 * @param page the puppeteer page
 */
async function waitForApplications(page) {
    await page.waitForFunction(() => [...registeredServicesById.values()].filter((service) => service.name === "Portal").length === 2,
        {timeout: 30000});
    await cas.logg("Both applications named Portal are loaded");
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
    await openApplications(page);
    await waitForApplications(page);
    await verifyCommandPalette(page);
    await verifyUserLookup(page, failures);
    await verifySessionAndTokenCards(page);
    await verifyInterruptInquiry(page);
    await verifyImpersonationAccounts(page);
    await verifyThrottlesUsername(page);
    await verifyConsentUsername(page);
    await verifyAuditEventsUsername(page);
    await verifyCredentialRepositories(page);
    await verifyAccessCheck(page);

    if (failures.length > 0) {
        await cas.logr(`Browser reported ${failures.length} error(s):`);
        for (const failure of failures) {
            await cas.logr(`  ${failure}`);
        }
    }
    assert(failures.length === 0, `Browser errors: ${failures.join(" | ")}`);
    await cas.logg("Palantir search and user lookups verified with no browser errors");
    await cas.closeBrowser(browser);
})();
