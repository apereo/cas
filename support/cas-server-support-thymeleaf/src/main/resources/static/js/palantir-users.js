/**
 * The Users page: everything the enabled actuator endpoints know about one user, on one page.
 *
 * Each source reads one endpoint the dashboard already calls from its own tab and draws one card.
 * Sources load in parallel and independently, so a slow or failing endpoint leaves the other cards
 * usable. A source whose endpoint is not enabled has no card, and the page itself is hidden when no
 * source is available.
 */
class PalantirUserLookup {

    static lookupSequence = 0;

    static currentUsername = "";

    static firstCardOpened = 0;

    /**
     * The sources the page can draw, in the order their cards appear.
     *
     * Each source names its card, says whether its endpoint is enabled, reads the user's records and
     * draws them. Records are drawn with escaped text only; actions are buttons carrying a
     * data-user-action and the ids the action needs. A source drawn as a DataTable, like every other table
     * in Palantir, renders the empty table and fills it in afterRender once the card is on the page.
     *
     * @returns {Array<object>} every source the page knows about
     */
    static sources() {
        return [
            {
                key: "sessions",
                title: "SSO Sessions",
                icon: "mdi-account-key",
                available: () => Boolean(CasActuatorEndpoints.ssoSessions()),
                load: async username => {
                    const response = await PalantirUserLookup.get(
                        `${CasActuatorEndpoints.ssoSessions()}/users/${encodeURIComponent(username)}`);
                    return response?.activeSsoSessions ?? [];
                },
                render: sessions => PalantirUserLookup.list(sessions.map(session => ({
                    primary: PalantirUserLookup.when(session.authentication_date),
                    secondary: [
                        session.authentication_attributes?.clientIpAddress?.[0],
                        PalantirUserLookup.count(Object.keys(session.authenticated_services ?? {}).length, "application"),
                        session.remember_me === true ? "remember me" : null
                    ],
                    code: session.ticket_granting_ticket,
                    action: {name: "end-session", label: "End", data: {ticket: session.ticket_granting_ticket}}
                })))
            },
            {
                key: "mfaDevices",
                title: "MFA Devices",
                icon: "mdi-cellphone-key",
                available: () => Boolean(CasActuatorEndpoints.mfaDevices())
                    && PalantirDashboardConfiguration.availableMultifactorProviders().length > 0,
                load: async username => Object.values(await PalantirUserLookup.get(
                    `${CasActuatorEndpoints.mfaDevices()}/${encodeURIComponent(username)}`) ?? {}),
                render: (devices, username) => PalantirUserLookup.list(devices.map(device => ({
                    primary: device.name ?? device.type ?? device.id,
                    secondary: [device.type, device.source, device.lastUsedDateTime ? `last used ${PalantirUserLookup.when(device.lastUsedDateTime)}` : null],
                    action: {
                        name: "remove-mfa-device",
                        label: "Remove",
                        data: {username: username, provider: device?.details?.providerId ?? "Unknown", key: device.id}
                    }
                })))
            },
            {
                key: "trustedDevices",
                title: "Trusted Devices",
                icon: "mdi-shield-check",
                available: () => Boolean(CasActuatorEndpoints.multifactorTrustedDevices())
                    && PalantirDashboardConfiguration.availableMultifactorProviders().length > 0,
                load: async username => Object.values(await PalantirUserLookup.get(
                    `${CasActuatorEndpoints.multifactorTrustedDevices()}/${encodeURIComponent(username)}`) ?? {}),
                render: devices => PalantirUserLookup.list(devices.map(device => ({
                    primary: device.name ?? device.deviceFingerprint,
                    secondary: [device.multifactorAuthenticationProvider, PalantirUserLookup.when(device.recordDate),
                        device.expirationDate ? `expires ${PalantirUserLookup.when(device.expirationDate)}` : null],
                    action: {name: "remove-trusted-device", label: "Remove", data: {key: device.recordKey}}
                })))
            },
            {
                key: "consent",
                title: "Consent Decisions",
                icon: "mdi-check-decagram",
                available: () => Boolean(CasActuatorEndpoints.attributeConsent()) && CAS_FEATURES.includes("Consent"),
                load: async username => await PalantirUserLookup.get(
                    `${CasActuatorEndpoints.attributeConsent()}/${encodeURIComponent(username)}`) ?? [],
                render: entries => PalantirUserLookup.list(entries.map(entry => ({
                    primary: entry.decision?.service,
                    secondary: [PalantirUserLookup.when(entry.decision?.createdDate),
                        PalantirUserLookup.count(Object.keys(entry.attributes ?? {}).length, "attribute")],
                    action: {
                        name: "revoke-consent",
                        label: "Revoke",
                        data: {principal: entry.decision?.principal, id: entry.decision?.id}
                    }
                })))
            },
            {
                key: "attributes",
                title: "Attributes",
                icon: "mdi-card-account-details",
                available: () => Boolean(CasActuatorEndpoints.personDirectory()),
                load: async username => {
                    const response = await PalantirUserLookup.get(
                        `${CasActuatorEndpoints.personDirectory()}/cache/${encodeURIComponent(username)}`);
                    return Object.entries(response?.attributes ?? {});
                },
                render: () => `
                    <table id="userLookupAttributesTable" class="mdc-data-table__table table table-striped noborder">
                        <thead>
                        <tr class="mdc-data-table__header-row">
                            <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Attribute</th>
                            <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Value(s)</th>
                        </tr>
                        </thead>
                        <tbody class="mdc-data-table__content">
                        </tbody>
                    </table>`,
                afterRender: attributes => {
                    const table = $("#userLookupAttributesTable").DataTable({
                        pageLength: 10,
                        autoWidth: false,
                        drawCallback: () => {
                            $("#userLookupAttributesTable tr").addClass("mdc-data-table__row");
                            $("#userLookupAttributesTable td").addClass("mdc-data-table__cell");
                        }
                    });
                    for (const [name, values] of attributes) {
                        table.row.add({
                            0: `<code>${escapePalantirMarkup(name)}</code>`,
                            1: `<code>${escapePalantirMarkup([].concat(values).join(", "))}</code>`
                        });
                    }
                    table.draw();
                }
            },
            {
                key: "impersonation",
                title: "Impersonation",
                icon: "mdi-account-switch",
                available: () => Boolean(CasActuatorEndpoints.impersonation()),
                load: async username => await PalantirUserLookup.get(
                    `${CasActuatorEndpoints.impersonation()}/${encodeURIComponent(username)}`) ?? [],
                render: accounts => PalantirUserLookup.list(accounts.map(account => ({
                    primary: account,
                    secondary: ["may sign in as this account"]
                })))
            },
            {
                key: "passwordless",
                title: "Passwordless Account",
                icon: "mdi-email-fast",
                available: () => Boolean(CasActuatorEndpoints.passwordless()),
                load: async username => {
                    const account = await PalantirUserLookup.get(CasActuatorEndpoints.passwordless(), {username});
                    return account && typeof account === "object" ? [account] : [];
                },
                render: accounts => PalantirUserLookup.list(accounts.map(account => ({
                    primary: account.name ?? account.username,
                    secondary: [account.email, account.phoneNumber, account.source]
                })))
            },
            {
                key: "passwordHistory",
                title: "Password History",
                icon: "mdi-form-textbox-password",
                available: () => Boolean(CasActuatorEndpoints.passwordManagement()),
                load: async username => await PalantirUserLookup.get(
                    `${CasActuatorEndpoints.passwordManagement()}/history/${encodeURIComponent(username)}`) ?? [],
                render: records => PalantirUserLookup.list(records.map(record => ({
                    primary: PalantirUserLookup.when(record.recordDate),
                    secondary: ["password changed"]
                })))
            },
            {
                key: "interrupts",
                title: "Interrupts",
                icon: "mdi-alert-octagon-outline",
                available: () => Boolean(CasActuatorEndpoints.interrupts()),
                load: async username => {
                    const report = await PalantirUserLookup.get(`${CasActuatorEndpoints.interrupts()}/${encodeURIComponent(username)}`);
                    const failed = (report?.inquirers ?? []).some(inquirer => inquirer.error);
                    return report && (report.interrupt || failed) ? [report] : [];
                },
                render: (reports, username) => PalantirUserLookup.interruptCard(reports[0], username)
            },
            {
                key: "throttles",
                title: "Throttled Attempts",
                icon: "mdi-timer-lock",
                available: () => Boolean(CasActuatorEndpoints.throttles()),
                load: async username => {
                    const records = await PalantirUserLookup.get(CasActuatorEndpoints.throttles(), {username});
                    return Array.isArray(records) ? records : [];
                },
                render: records => PalantirUserLookup.list(records.map(record => ({
                    primary: record.clientIpAddress ?? record.username,
                    secondary: [PalantirUserLookup.when(record.value),
                        record.expiration ? `expires ${PalantirUserLookup.when(record.expiration)}` : null],
                    code: record.key,
                    action: {name: "remove-throttle", label: "Remove", data: {key: record.key}}
                })))
            },
            {
                key: "activity",
                title: "Recent Activity",
                icon: "mdi-history",
                available: () => Boolean(CasActuatorEndpoints.auditLog()),
                load: async username => {
                    const records = await PalantirUserLookup.get(CasActuatorEndpoints.auditLog(), {
                        interval: $("#userLookupAuditWindow").val() || "P7D",
                        principal: `^${PalantirUserLookup.quoteRegex(username)}$`
                    });
                    return (Array.isArray(records) ? records : [])
                        .sort((first, second) => String(second.whenActionWasPerformed ?? "")
                            .localeCompare(String(first.whenActionWasPerformed ?? "")));
                },
                render: records => `
                    <table class="palantir-user-card__table">
                        <thead><tr><th scope="col">When</th><th scope="col">Action</th>
                            <th scope="col">Resource</th><th scope="col">Client</th></tr></thead>
                        <tbody>${records.slice(0, 100).map(record => `
                            <tr>
                                <td>${escapePalantirMarkup(PalantirUserLookup.when(record.whenActionWasPerformed))}</td>
                                <td><code>${escapePalantirMarkup(record.actionPerformed ?? "")}</code></td>
                                <td class="palantir-user-card__resource">${escapePalantirMarkup(
                    typeof record.resourceOperatedUpon === "string"
                        ? record.resourceOperatedUpon : JSON.stringify(record.resourceOperatedUpon ?? ""))}</td>
                                <td>${escapePalantirMarkup(record.clientInfo?.clientIpAddress ?? "")}</td>
                            </tr>`).join("")}
                        </tbody>
                    </table>
                    ${records.length > 100 ? `<p class="palantir-user-card__more">Showing the latest 100 of ${records.length} events.</p>` : ""}`
            }
        ];
    }

    /**
     * Count things in words: 1 application, 2 applications.
     *
     * @param count how many
     * @param noun the singular noun
     * @returns {string} the count and the noun
     */
    static count(count, noun) {
        return `${count} ${noun}${count === 1 ? "" : "s"}`;
    }

    /**
     * Show a date the way the browser's locale writes it; a value that is not a date is shown as given.
     *
     * @param value an ISO date, an epoch in milliseconds or nothing
     * @returns {string} the formatted date, or an empty string
     */
    static when(value) {
        if (value === null || value === undefined || value === "") {
            return "";
        }
        const date = new Date(Array.isArray(value) ? Date.UTC(value[0], (value[1] ?? 1) - 1, value[2] ?? 1,
            value[3] ?? 0, value[4] ?? 0, value[5] ?? 0) : value);
        return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
    }

    /**
     * The sources whose endpoints this deployment enables.
     *
     * @returns {Array<object>} the available sources
     */
    static availableSources() {
        return PalantirUserLookup.sources().filter(source => source.available());
    }

    /**
     * Read one endpoint as JSON.
     *
     * The audit log endpoint only answers requests that declare a form content type, so every read
     * declares one. A 404 means the endpoint has nothing for this user and reads as no records; a
     * successful empty body reads as null.
     *
     * @param url the endpoint
     * @param data the query parameters, if any
     * @returns {Promise<any>} the parsed body, or null when there is none
     */
    static get(url, data) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: url,
                type: "GET",
                data: data,
                dataType: "text",
                contentType: "application/x-www-form-urlencoded",
                success: body => {
                    if (!body || body.trim() === "") {
                        resolve(null);
                        return;
                    }
                    try {
                        resolve(JSON.parse(body));
                    } catch (error) {
                        reject(new Error("The endpoint did not answer with JSON."));
                    }
                },
                error: xhr => xhr.status === 404
                    ? resolve(null)
                    : reject(new Error(xhr.status ? `The endpoint answered ${xhr.status}.` : "The endpoint could not be reached."))
            });
        });
    }

    /**
     * Quote a username for the audit endpoint, which matches its principal filter as a regular expression.
     *
     * @param value the username
     * @returns {string} the username with every regular expression metacharacter escaped
     */
    static quoteRegex(value) {
        return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    }

    /**
     * Draw what the login flow would show the user without an application: the interrupt with its message as
     * sanitized markup and its links, any interrupt source that failed, a note that interrupts for particular
     * applications are not covered, and a way to open the full inquiry on the Authentication page.
     *
     * @param report the interrupt inquiry report
     * @param username the user being looked up
     * @returns {string} the card body markup
     */
    static interruptCard(report, username) {
        const interrupt = report.interrupt;
        const rows = [];
        let message = "";
        if (interrupt) {
            const redirect = interrupt.autoRedirect
                ? `redirects ${Number(interrupt.autoRedirectAfterSeconds) > 0 ? `after ${interrupt.autoRedirectAfterSeconds} seconds` : "immediately"}`
                : null;
            const behavior = [interrupt.block ? "blocks the login" : "lets the user continue",
                interrupt.ssoEnabled ? "single sign-on afterwards" : "no single sign-on afterwards", redirect]
                .filter(Boolean).join(" · ");
            const text = interrupt.message
                ? sanitizePalantirHtml(interrupt.message)
                : escapePalantirMarkup(interrupt.messageCode || "Interrupts the login");
            message = `
                <div class="palantir-user-card__message">${text}</div>
                <p class="palantir-user-card__secondary">${escapePalantirMarkup(behavior)}</p>`;
        }
        for (const inquirer of (report.inquirers ?? []).filter(entry => entry.error)) {
            rows.push({primary: `${inquirer.name} failed`, secondary: [inquirer.error]});
        }
        const links = Object.entries(interrupt?.links ?? {})
            .map(([name, url]) => `<a class="palantir-tag" href="${escapePalantirMarkup(url)}" target="_blank" rel="noopener noreferrer">${escapePalantirMarkup(name)}</a>`)
            .join(" ");
        return `
            ${message}
            ${rows.length > 0 ? PalantirUserLookup.list(rows) : ""}
            ${links ? `<p class="palantir-user-card__links">${links}</p>` : ""}
            <p class="palantir-user-card__note">
                <i class="mdi mdi-information-outline" aria-hidden="true"></i>
                Asked without an application: interrupts that apply only when signing in to a particular application are not shown.
            </p>
            <button type="button" class="palantir-button" data-user-navigate="interrupts" data-username="${escapePalantirMarkup(username)}">
                <i class="mdi mdi-open-in-app" aria-hidden="true"></i>Open in Interrupts
            </button>`;
    }

    /**
     * Draw records as a list of rows: a primary line, a muted secondary line, an optional code line and
     * an optional action button.
     *
     * @param rows the rows to draw
     * @returns {string} the list markup
     */
    static list(rows) {
        return `<ul class="palantir-user-card__list">${rows.map(row => {
            const secondary = [].concat(row.secondary ?? []).filter(value => value !== null && value !== undefined && value !== "");
            const data = Object.entries(row.action?.data ?? {})
                .map(([name, value]) => `data-${name}="${escapePalantirMarkup(value ?? "")}"`)
                .join(" ");
            return `
                <li>
                    <div class="palantir-user-card__row-text">
                        <span class="palantir-user-card__primary">${escapePalantirMarkup(row.primary ?? "Unnamed")}</span>
                        ${secondary.length > 0 ? `<span class="palantir-user-card__secondary">${secondary.map(escapePalantirMarkup).join(" · ")}</span>` : ""}
                        ${row.code ? `<code class="palantir-user-card__code">${escapePalantirMarkup(row.code)}</code>` : ""}
                    </div>
                    ${row.action ? `<button type="button" class="palantir-button" data-user-action="${row.action.name}" ${data}>${escapePalantirMarkup(row.action.label)}</button>` : ""}
                </li>`;
        }).join("")}</ul>`;
    }

    /**
     * Draw one card in its loading, empty, failed or loaded state.
     *
     * Cards are disclosure widgets stacked on top of each other. A source with nothing recorded keeps a hidden
     * card, so that reloading it after an action can bring it back.
     *
     * @param source the source the card belongs to
     * @param state one of loading, empty, error or loaded
     * @param content the records, or the error message for the failed state
     * @param username the user being looked up
     * @param open whether the card is expanded
     * @returns {string} the card markup
     */
    static card(source, state, content, username, open = false) {
        if (state === "empty") {
            return `<details class="palantir-user-card is-empty" data-user-source="${source.key}" hidden></details>`;
        }
        const count = state === "loaded" ? content.length : null;
        const body = {
            loading: "<p class=\"palantir-user-card__note\">Loading…</p>",
            error: `<p class="palantir-user-card__note is-error"><i class="mdi mdi-alert-circle-outline" aria-hidden="true"></i>${escapePalantirMarkup(content)}</p>`,
            loaded: state === "loaded" ? source.render(content, username) : ""
        }[state];
        return `
            <details class="palantir-user-card is-${state}" data-user-source="${source.key}"
                     aria-busy="${state === "loading"}"${open ? " open" : ""}>
                <summary class="palantir-user-card__head">
                    <i class="mdi ${source.icon}" aria-hidden="true"></i>
                    <h4>${escapePalantirMarkup(source.title)}</h4>
                    ${count === null ? "" : `<span class="palantir-user-card__count">${count}</span>`}
                    ${state === "error" ? "<i class=\"mdi mdi-alert-circle-outline palantir-user-card__alert\" aria-hidden=\"true\"></i>" : ""}
                    <i class="mdi mdi-chevron-down palantir-user-card__chevron" aria-hidden="true"></i>
                </summary>
                <div class="palantir-user-card__body">${body}</div>
            </details>`;
    }

    /**
     * Expand the first card that has something to show, once every card before it has answered, so the
     * card that opens does not depend on which endpoint answers first. Runs once per lookup.
     *
     * @param sequence the lookup the cards belong to
     */
    static openFirstCard(sequence) {
        if (PalantirUserLookup.firstCardOpened === sequence || sequence !== PalantirUserLookup.lookupSequence) {
            return;
        }
        for (const card of $("#userLookupResults .palantir-user-card").toArray()) {
            if (card.classList.contains("is-empty")) {
                continue;
            }
            if (!card.classList.contains("is-loading")) {
                card.open = true;
                PalantirUserLookup.firstCardOpened = sequence;
            }
            return;
        }
    }

    /**
     * Load one source for the user and redraw its card, unless a newer lookup has started since. A card
     * that is reloaded keeps whether it was expanded.
     *
     * @param source the source to load
     * @param username the user being looked up
     * @param sequence the lookup this load belongs to
     * @returns {Promise<{source: object, count: number|null}>} the source and how many records it found
     */
    static async loadSource(source, username, sequence) {
        const replace = (state, content) => {
            if (sequence === PalantirUserLookup.lookupSequence) {
                const current = $(`#userLookupResults [data-user-source="${source.key}"]`);
                current.find("table").filter((index, table) => $.fn.dataTable.isDataTable(table))
                    .each((index, table) => $(table).DataTable().destroy());
                current.replaceWith(PalantirUserLookup.card(source, state, content, username, current.prop("open") === true));
                if (state === "loaded" && source.afterRender) {
                    source.afterRender(content, username);
                }
                PalantirUserLookup.openFirstCard(sequence);
            }
        };
        try {
            const records = await source.load(username);
            replace(records.length === 0 ? "empty" : "loaded", records);
            return {source, count: records.length};
        } catch (error) {
            replace("error", error.message);
            return {source, count: null};
        }
    }

    /**
     * Look up a user: draw a loading card per available source, load them all in parallel, then
     * summarize what was found.
     *
     * @param username the user to look up
     * @returns {Promise<void>} resolved once every source has answered
     */
    static async lookUp(username) {
        const trimmed = String(username ?? "").trim();
        if (trimmed === "") {
            return;
        }
        PalantirUserLookup.currentUsername = trimmed;
        const sequence = ++PalantirUserLookup.lookupSequence;
        const sources = PalantirUserLookup.availableSources();
        $("#userLookupResults table").filter((index, table) => $.fn.dataTable.isDataTable(table))
            .each((index, table) => $(table).DataTable().destroy());
        $("#userLookupResults").html(sources.map(source => PalantirUserLookup.card(source, "loading", null, trimmed)).join(""));
        $("#userLookupSummary").text(`Looking up ${trimmed}…`);
        const results = await Promise.all(sources.map(source => PalantirUserLookup.loadSource(source, trimmed, sequence)));
        if (sequence !== PalantirUserLookup.lookupSequence) {
            return;
        }
        const found = results.filter(result => result.count > 0);
        const failed = results.filter(result => result.count === null);
        const parts = found.map(result => `${result.source.title} ${result.count}`);
        const summary = parts.length > 0 ? `${trimmed}: ${parts.join(" · ")}` : `Nothing is recorded for ${trimmed}.`;
        $("#userLookupSummary").text(failed.length > 0
            ? `${summary} ${failed.length} ${failed.length === 1 ? "source" : "sources"} could not be read.`
            : summary);
    }

    /**
     * Run a card action after the operator confirms it, then reload that card.
     *
     * @param button the action button that was clicked
     */
    static runAction(button) {
        const data = button.dataset;
        const actions = {
            "end-session": {
                title: "End this SSO session?",
                url: () => `${CasActuatorEndpoints.ssoSessions()}/${encodeURIComponent(data.ticket)}`
            },
            "remove-mfa-device": {
                title: "Remove this MFA device?",
                url: () => `${CasActuatorEndpoints.mfaDevices()}/${encodeURIComponent(data.username)}/${encodeURIComponent(data.provider)}/${encodeURIComponent(data.key)}`
            },
            "remove-trusted-device": {
                title: "Remove this trusted device?",
                url: () => `${CasActuatorEndpoints.multifactorTrustedDevices()}/${encodeURIComponent(data.key)}`
            },
            "remove-throttle": {
                title: "Remove this throttled attempt?",
                url: () => `${CasActuatorEndpoints.throttles()}?key=${encodeURIComponent(data.key)}`
            },
            "revoke-consent": {
                title: "Revoke this consent decision?",
                url: () => `${CasActuatorEndpoints.attributeConsent()}/${encodeURIComponent(data.principal)}/${encodeURIComponent(data.id)}`
            }
        };
        const action = actions[data.userAction];
        const card = button.closest("[data-user-source]");
        const source = PalantirUserLookup.sources().find(entry => entry.key === card?.dataset.userSource);
        if (!action || !source) {
            return;
        }
        Swal.fire({
            title: action.title,
            text: "This cannot be undone.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        }).then(result => {
            if (!result.isConfirmed) {
                return;
            }
            $.ajax({
                url: action.url(),
                type: "DELETE",
                contentType: "application/x-www-form-urlencoded",
                success: () => PalantirUserLookup.loadSource(source, PalantirUserLookup.currentUsername, PalantirUserLookup.lookupSequence),
                error: xhr => displayBanner(xhr)
            });
        });
    }
}

/**
 * Open the Users page and look up the given user, as the command palette does.
 *
 * @param username the user to look up
 * @returns {Promise<void>} resolved once the lookup has finished
 */
async function lookUpPalantirUser(username) {
    $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.USERS.index}]`).trigger("click");
    await initializeDashboardTab(Tabs.USERS);
    $("#userLookupUsername").val(username);
    await PalantirUserLookup.lookUp(username);
}

/**
 * Wire the Users page: the lookup form, the activity window and the card actions.
 */
async function initializeUserLookupOperations() {
    $("#userLookupButton").off("click").on("click", () => {
        const form = document.getElementById("fmUserLookup");
        if (form.reportValidity()) {
            PalantirUserLookup.lookUp($("#userLookupUsername").val());
        }
    });
    $("#userLookupAuditWindow").off("change selectmenuchange").on("change selectmenuchange", () => {
        const activity = PalantirUserLookup.availableSources().find(source => source.key === "activity");
        if (activity && PalantirUserLookup.currentUsername) {
            PalantirUserLookup.loadSource(activity, PalantirUserLookup.currentUsername, PalantirUserLookup.lookupSequence);
        }
    });
    $("#userLookupResults").off("click").on("click", "[data-user-action]", event => {
        PalantirUserLookup.runAction(event.currentTarget);
    });
    $("#userLookupResults").on("click", "[data-user-navigate=interrupts]", event => {
        openInterruptInquiry(event.currentTarget.dataset.username);
    });
}
