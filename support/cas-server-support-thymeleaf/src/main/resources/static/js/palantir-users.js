/**
 * The Users page: everything the enabled actuator endpoints know about one user, on one page.
 *
 * Each source reads one endpoint the dashboard already calls from its own tab and draws one card.
 * Sources load in parallel and independently, so a slow or failing endpoint leaves the other cards
 * usable. A source whose endpoint is not enabled has no card, and the page itself is hidden when no
 * source is available. A source with nothing recorded has no visible card.
 */
class PalantirUserLookup {

    static lookupSequence = 0;

    static currentUsername = "";

    static firstCardOpened = 0;

    static tokens = new Map();

    static resolvedAttributes = null;

    static counts = new Map();

    static settled = 0;

    static relativeTime = null;

    static ACTION_ENDPOINTS = {
        "end-session": "ssoSessions",
        "revoke-token": "oauthTokens",
        "revoke-all-tokens": "oauthTokens",
        "remove-mfa-device": "mfaDevices",
        "remove-trusted-device": "multifactorTrustedDevices",
        "remove-throttle": "throttles",
        "revoke-consent": "attributeConsent"
    };

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
                render: sessions => PalantirUserLookup.sessionCards(sessions)
            },
            {
                key: "oauthTokens",
                title: "OAuth & OIDC Tokens",
                icon: "mdi-key-chain",
                available: () => Boolean(CasActuatorEndpoints.oauthTokens()),
                load: async username => {
                    const tokens = await PalantirUserLookup.get(CasActuatorEndpoints.oauthTokens(), {username});
                    return (Array.isArray(tokens) ? tokens : []).filter(token => typeof token?.id === "string");
                },
                render: tokens => PalantirUserLookup.tokenGroups(tokens)
            },
            {
                key: "mfaDevices",
                title: "MFA Devices",
                icon: "mdi-cellphone-key",
                available: () => Boolean(CasActuatorEndpoints.mfaDevices())
                    && PalantirDashboardConfiguration.availableMultifactorProviders().length > 0,
                load: async username => Object.values(await PalantirUserLookup.get(
                    `${CasActuatorEndpoints.mfaDevices()}/${encodeURIComponent(username)}`) ?? {}),
                render: (devices, username) => PalantirUserLookup.deviceCards(devices, username)
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
                load: async (username, sequence) => {
                    const response = await PalantirUserLookup.get(
                        `${CasActuatorEndpoints.personDirectory()}/cache/${encodeURIComponent(username)}`);
                    if (sequence === PalantirUserLookup.lookupSequence) {
                        PalantirUserLookup.resolvedAttributes = {username: username, attributes: response?.attributes ?? null};
                    }
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
                            0: `${palantirCode(name)}`,
                            1: `${palantirCode([].concat(values).join(", "))}`
                        });
                    }
                    table.draw();
                }
            },
            {
                key: "release",
                title: "Attribute Release",
                icon: "mdi-account-eye",
                counted: false,
                available: () => PalantirAttributeRelease.available(),
                load: async () => [true],
                render: () => `
                    <div class="palantir-user-release">
                        <p class="palantir-user-card__note">
                            <i class="mdi mdi-information-outline" aria-hidden="true"></i>
                            Pick an application to see the attributes it would receive for this user.
                        </p>
                        <div class="palantir-user-release__row">
                            <label class="palantir-access__field palantir-access__target" id="userReleaseApplicationField">
                                <i class="mdi mdi-application-outline" aria-hidden="true"></i>
                                <span class="palantir-sr-only">Application</span>
                                <input id="userReleaseApplication" type="text" autocomplete="off" spellcheck="false"
                                       placeholder="Application name, service id, client id or id"/>
                            </label>
                            <button type="button" class="palantir-button" data-user-release>
                                <i class="mdi mdi-account-eye" aria-hidden="true"></i>Preview
                            </button>
                        </div>
                        <div id="userReleaseResult" aria-live="polite"></div>
                    </div>`,
                afterRender: async () => {
                    PalantirAttributeRelease.selected = null;
                    const sequence = PalantirUserLookup.lookupSequence;
                    const field = document.getElementById("userReleaseApplication");
                    const choices = await PalantirAttributeRelease.applications();
                    if (sequence !== PalantirUserLookup.lookupSequence || !field?.isConnected || $(field).autocomplete("instance")) {
                        return;
                    }
                    PalantirAttributeRelease.choices = choices;
                    const input = $(field);
                    PalantirApplicationSuggestions.attach(input, {
                        field: "#userReleaseApplicationField",
                        source: term => PalantirAttributeRelease.choices.filter(service => PalantirApplicationSuggestions.matches(service, term)),
                        select: service => {
                            PalantirAttributeRelease.selected = service;
                            input.val(service.name ?? PalantirAccessCheck.identifierOf(service));
                        }
                    });
                    input.on("focus", function () {
                        if (!$(this).val()) {
                            $(this).autocomplete("search", "");
                        }
                    }).on("input", function () {
                        const selected = PalantirAttributeRelease.selected;
                        if (selected && $(this).val() !== (selected.name ?? PalantirAccessCheck.identifierOf(selected))) {
                            PalantirAttributeRelease.selected = null;
                        }
                    });
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
                render: accounts => PalantirUserLookup.accountCards(accounts)
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
                        count: 500,
                        principal: `^${PalantirUserLookup.quoteRegex(username)}$`
                    });
                    return (Array.isArray(records) ? records : [])
                        .sort((first, second) => String(second.whenActionWasPerformed ?? "")
                            .localeCompare(String(first.whenActionWasPerformed ?? "")));
                },
                render: () => `
                    <table id="userLookupActivityTable" class="mdc-data-table__table table table-striped noborder">
                        <thead>
                        <tr class="mdc-data-table__header-row">
                            <th class="mdc-data-table__header-cell" role="columnheader" scope="col">When</th>
                            <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Action</th>
                            <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Target</th>
                            <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Resource</th>
                            <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Client</th>
                        </tr>
                        </thead>
                        <tbody class="mdc-data-table__content">
                        </tbody>
                    </table>`,
                afterRender: records => {
                    const table = $("#userLookupActivityTable").DataTable({
                        pageLength: 10,
                        autoWidth: false,
                        order: [],
                        columnDefs: [
                            {
                                targets: 0,
                                render: (data, type) => type === "display"
                                    ? escapePalantirMarkup(PalantirUserLookup.when(data)) : data
                            },
                            {
                                targets: 2,
                                render: (data, type) => type === "display" ? palantirAuditTarget(data) : data
                            },
                            {targets: 3, className: "palantir-user-card__resource"}
                        ],
                        drawCallback: () => {
                            $("#userLookupActivityTable tr").addClass("mdc-data-table__row");
                            $("#userLookupActivityTable td").addClass("mdc-data-table__cell");
                            namePalantirAuditTargetApplications("#userLookupActivityTable");
                        }
                    });
                    $("#userLookupActivityTable").off("click.auditTarget")
                        .on("click.auditTarget", "[data-audit-application]", openPalantirAuditTargetApplication);
                    for (const record of records) {
                        const resource = typeof record.resourceOperatedUpon === "string"
                            ? record.resourceOperatedUpon : JSON.stringify(record.resourceOperatedUpon ?? "");
                        table.row.add({
                            0: String(record.whenActionWasPerformed ?? ""),
                            1: `${palantirCode(record.actionPerformed ?? "")}`,
                            2: record.target ? String(record.target) : "",
                            3: escapePalantirMarkup(resource),
                            4: escapePalantirMarkup(record.clientInfo?.clientIpAddress ?? "")
                        });
                    }
                    table.draw();
                }
            }
        ];
    }

    /**
     * What kind of token a ticket is, from its id: CAS starts refresh token ids with RT- and access token ids with AT-.
     *
     * @param token the token
     * @returns {string} the kind of token
     */
    /**
     * Draw each SSO session as a card: when the user signed in and how long ago, what the session is marked as, the
     * facts an operator looks for (where from, when last used, when it ends, how often it was used, how the user
     * signed in), the applications it signed the user in to, and its ticket, shortened, with a way to copy it whole.
     *
     * @param sessions the sessions of the user
     * @returns {string} the markup
     */
    static sessionCards(sessions) {
        const cards = sessions.map(session => {
            const ticket = String(session.ticket_granting_ticket ?? "");
            const attributes = session.authentication_attributes ?? {};
            const policy = session.expiration_policy ?? {};
            const ends = PalantirUserLookup.earliest([policy.idleExpirationTime, policy.maxExpirationTime]);
            const services = Object.values(session.authenticated_services ?? {})
                .map(service => String(service?.originalUrl ?? service?.id ?? service ?? ""))
                .filter(url => url !== "");
            const applications = [...new Set(services)];
            const handlers = [].concat(attributes.successfulAuthenticationHandlers ?? []);
            const badges = [
                {label: "Active", style: "is-success"},
                session.remember_me === true ? {label: "Remember Me", style: "is-accent"} : null,
                session.is_proxied === true ? {label: "Proxied", style: "is-warn"} : null
            ].filter(Boolean);
            const facts = [
                {label: "IP Address", value: [].concat(attributes.clientIpAddress ?? [])[0], mono: true},
                {label: "Last Used", value: session.last_used_date_formatted},
                {label: "Ends", value: ends === null ? null : PalantirUserLookup.ago(ends), hint: ends === null ? null : PalantirUserLookup.when(ends)},
                {label: "Uses", value: session.number_of_uses},
                {label: "Signed In With", value: handlers.join(", ")}
            ];
            return `
                <article class="palantir-identity-item palantir-session">
                    <header class="palantir-identity-item__head">
                        <span class="palantir-identity-item__icon" aria-hidden="true"><i class="mdi mdi-monitor-account"></i></span>
                        <div class="palantir-identity-item__title">
                            <span class="palantir-identity-item__name">${escapePalantirMarkup(PalantirUserLookup.when(session.authentication_date) || "Unknown Sign-In Time")}</span>
                            <span class="palantir-identity-item__when">${escapePalantirMarkup(PalantirUserLookup.ago(session.authentication_date))}</span>
                        </div>
                        <span class="palantir-identity-item__badges">${PalantirUserLookup.badges(badges)}</span>
                        <button type="button" class="palantir-button is-danger palantir-identity-item__action" data-user-action="end-session"
                                data-palantir-write="ssoSessions" data-ticket="${escapePalantirMarkup(ticket)}">
                            <i class="mdi mdi-logout" aria-hidden="true"></i>End Session
                        </button>
                    </header>
                    ${PalantirUserLookup.facts(facts)}
                    <div class="palantir-identity-item__section">
                        <span class="palantir-identity-item__label">Applications <span class="palantir-identity-item__count">${applications.length}</span></span>
                        ${applications.length === 0
                            ? "<span class=\"palantir-identity-item__none\">None Yet</span>"
                            : `<span class="palantir-identity-item__chips">${applications.map(url => PalantirUserLookup.serviceChip(url)).join("")}</span>`}
                    </div>
                    <footer class="palantir-identity-item__foot">
                        <span class="palantir-identity-item__label">Ticket</span>
                        <code class="palantir-identity-item__code" title="${escapePalantirMarkup(ticket)}">${escapePalantirMarkup(PalantirUserLookup.maskToken(ticket))}</code>
                        <button type="button" class="palantir-identity-item__copy" data-user-copy="${escapePalantirMarkup(ticket)}"
                                title="Copy Ticket" aria-label="Copy Ticket"><i class="mdi mdi-content-copy" aria-hidden="true"></i></button>
                    </footer>
                </article>`;
        });
        return `<div class="palantir-identity-list">${cards.join("")}</div>`;
    }

    /**
     * Draw the tokens of the user grouped by the application they were issued to, newest first: each token says what
     * kind it is, what it allows, how it was obtained, when it was issued, last used and ends, and shows its id
     * shortened only, since the id is the token itself.
     *
     * @param tokens the tokens of the user
     * @returns {string} the markup
     */
    static tokenGroups(tokens) {
        PalantirUserLookup.tokens = new Map(tokens.map((token, index) => [String(index), token.id]));
        const groups = new Map();
        tokens.forEach((token, index) => {
            const client = String(token.clientId ?? "Unknown Application");
            const group = groups.get(client);
            if (group) {
                group.push({token, index});
            } else {
                groups.set(client, [{token, index}]);
            }
        });
        const time = value => PalantirUserLookup.dateOf(value)?.getTime() ?? 0;
        const sections = [...groups.entries()].map(([client, entries]) => {
            const items = entries
                .sort((first, second) => time(second.token.creationTime) - time(first.token.creationTime))
                .map(({token, index}) => {
                    const refresh = PalantirUserLookup.tokenKind(token) === "Refresh token";
                    const scopes = [].concat(token.scopes ?? []).filter(scope => scope !== null && scope !== "");
                    const ends = PalantirUserLookup.tokenEnds(token);
                    const facts = [
                        {label: "Grant", value: token.grantType ? PalantirUserLookup.titleCase(String(token.grantType).toLowerCase().replaceAll("_", " ")) : null},
                        {label: "Issued", value: PalantirUserLookup.ago(token.creationTime), hint: PalantirUserLookup.when(token.creationTime)},
                        {label: "Last Used", value: PalantirUserLookup.ago(token.lastTimeUsed), hint: PalantirUserLookup.when(token.lastTimeUsed)},
                        {label: "Ends", value: ends === null ? null : PalantirUserLookup.ago(ends), hint: ends === null ? null : PalantirUserLookup.when(ends)}
                    ];
                    return `
                        <article class="palantir-identity-item palantir-token ${refresh ? "is-refresh" : "is-access"}">
                            <header class="palantir-identity-item__head">
                                <span class="palantir-identity-item__icon" aria-hidden="true"><i class="mdi ${refresh ? "mdi-autorenew" : "mdi-key-variant"}"></i></span>
                                <div class="palantir-identity-item__title">
                                    <span class="palantir-identity-item__name">${refresh ? "Refresh Token" : "Access Token"}</span>
                                    <code class="palantir-identity-item__code">${escapePalantirMarkup(PalantirUserLookup.maskToken(token.id))}</code>
                                </div>
                                <button type="button" class="palantir-button is-danger palantir-identity-item__action" data-user-action="revoke-token"
                                        data-palantir-write="oauthTokens" data-token="${index}">
                                    <i class="mdi mdi-key-remove" aria-hidden="true"></i>Revoke
                                </button>
                            </header>
                            <div class="palantir-identity-item__section">
                                <span class="palantir-identity-item__label">Scopes</span>
                                ${scopes.length === 0
                                    ? "<span class=\"palantir-identity-item__none\">None</span>"
                                    : `<span class="palantir-identity-item__chips">${scopes.map(scope =>
                                        `<span class="palantir-identity-chip is-scope">${escapePalantirMarkup(scope)}</span>`).join("")}</span>`}
                            </div>
                            ${PalantirUserLookup.facts(facts)}
                        </article>`;
                }).join("");
            return `
                <section class="palantir-token-client">
                    <header class="palantir-token-client__head">
                        <i class="mdi mdi-application-braces-outline" aria-hidden="true"></i>
                        <span class="palantir-token-client__name">${escapePalantirMarkup(client)}</span>
                        <span class="palantir-identity-item__count">${PalantirUserLookup.count(entries.length, "token")}</span>
                    </header>
                    <div class="palantir-identity-list">${items}</div>
                </section>`;
        });
        return `
            <div class="palantir-token-summary">
                <span class="palantir-token-summary__text">
                    <strong>${PalantirUserLookup.count(tokens.length, "token")}</strong>
                    issued to ${PalantirUserLookup.count(groups.size, "application")}
                </span>
                <button type="button" class="palantir-button is-danger palantir-user-card__bulk" data-user-action="revoke-all-tokens"
                        data-palantir-write="oauthTokens">
                    <i class="mdi mdi-key-remove" aria-hidden="true"></i>Revoke All Tokens
                </button>
            </div>
            ${sections.join("")}`;
    }

    /**
     * Draw each MFA device as a card: what it is and which provider it belongs to, with an icon for that provider, the
     * facts recorded for it, such as its model, number, when it was last used and when it expires, and a way to
     * remove it.
     *
     * @param devices the devices of the user
     * @param username the user
     * @returns {string} the markup
     */
    static deviceCards(devices, username) {
        const cards = devices.map(device => {
            const provider = String(device?.details?.providerId ?? "Unknown");
            const kind = PalantirUserLookup.deviceKind(device);
            const facts = [
                {label: "Type", value: device.type},
                {label: "Model", value: device.model},
                {label: "Number", value: device.number, mono: true},
                {label: kind.registeredOnly ? "Registered" : "Last Used", value: PalantirUserLookup.ago(device.lastUsedDateTime),
                    hint: PalantirUserLookup.when(device.lastUsedDateTime)},
                {label: "Expires", value: PalantirUserLookup.ago(device.expirationDateTime), hint: PalantirUserLookup.when(device.expirationDateTime)}
            ];
            return `
                <article class="palantir-identity-item palantir-device is-${kind.style}">
                    <header class="palantir-identity-item__head">
                        <span class="palantir-identity-item__icon" aria-hidden="true"><i class="mdi ${kind.icon}"></i></span>
                        <div class="palantir-identity-item__title">
                            <span class="palantir-identity-item__name">${escapePalantirMarkup(device.name ?? device.type ?? device.id ?? "Unnamed Device")}</span>
                            <span class="palantir-identity-item__when">${escapePalantirMarkup(device.source ?? provider)}</span>
                        </div>
                        <button type="button" class="palantir-button is-danger palantir-identity-item__action" data-user-action="remove-mfa-device"
                                data-palantir-write="mfaDevices" data-username="${escapePalantirMarkup(username)}"
                                data-provider="${escapePalantirMarkup(provider)}" data-key="${escapePalantirMarkup(device.id ?? "")}">
                            <i class="mdi mdi-delete-outline" aria-hidden="true"></i>Remove
                        </button>
                    </header>
                    ${PalantirUserLookup.facts(facts)}
                </article>`;
        });
        return `<div class="palantir-identity-list">${cards.join("")}</div>`;
    }

    /**
     * The icon and accent of a device, by the provider it belongs to, and whether the provider records only when the
     * device was registered rather than when it was last used, as WebAuthn and YubiKey do.
     *
     * @param device the device
     * @returns {{icon: string, style: string, registeredOnly?: boolean}} the icon, the accent and how its date reads
     */
    static deviceKind(device) {
        const text = `${device?.details?.providerId ?? ""} ${device?.source ?? ""} ${device?.type ?? ""}`.toLowerCase();
        const kinds = [
            {match: /web\s*authn|fido|passkey/, icon: "mdi-fingerprint", style: "webauthn", registeredOnly: true},
            {match: /yubi/, icon: "mdi-usb-flash-drive-outline", style: "yubikey", registeredOnly: true},
            {match: /duo/, icon: "mdi-cellphone-message", style: "duo"},
            {match: /gauth|google|authenticator|totp/, icon: "mdi-shield-key-outline", style: "totp"},
            {match: /sms|email|simple|text/, icon: "mdi-message-lock-outline", style: "message"}
        ];
        return kinds.find(kind => kind.match.test(text)) ?? {icon: "mdi-cellphone-key", style: "other"};
    }

    /**
     * Draw the passwordless account of the user as a card: who the user is, how CAS reaches them, where the account
     * comes from, and how they sign in without a password, each choice in words.
     *
     * @param accounts the account of the user, if there is one
     * @returns {string} the markup
     */
    static accountCards(accounts) {
        const choice = value => {
            const text = String(value ?? "").toUpperCase();
            return text === "TRUE" ? "Yes" : text === "FALSE" ? "No" : text === "" || text === "UNDEFINED" ? "Default" : PalantirUserLookup.titleCase(text.toLowerCase());
        };
        const cards = accounts.map(account => {
            const name = account.name || account.username || "Unnamed Account";
            const initials = String(name).split(/[\s._@-]+/).filter(Boolean).slice(0, 2).map(part => part[0].toUpperCase()).join("");
            const phone = account.phone ?? account.phoneNumber;
            const contacts = [
                account.email ? {icon: "mdi-email-outline", value: account.email} : null,
                phone ? {icon: "mdi-phone-outline", value: phone} : null
            ].filter(Boolean);
            const clients = [].concat(account.allowedDelegatedClients ?? []).filter(Boolean);
            const attributes = Object.keys(account.attributes ?? {});
            const facts = [
                {label: "MFA Eligible", value: choice(account.multifactorAuthenticationEligible)},
                {label: "Delegated Sign-In", value: choice(account.delegatedAuthenticationEligible)},
                {label: "Asks For Password", value: account.requestPassword === true ? "Yes" : "No"},
                {label: "Selection Menu", value: account.allowSelectionMenu === true ? "Shown" : "Hidden"}
            ];
            return `
                <article class="palantir-identity-item palantir-account">
                    <header class="palantir-identity-item__head">
                        <span class="palantir-identity-item__icon palantir-account__avatar" aria-hidden="true">${escapePalantirMarkup(initials || "?")}</span>
                        <div class="palantir-identity-item__title">
                            <span class="palantir-identity-item__name">${escapePalantirMarkup(name)}</span>
                            ${account.username && account.username !== name
                                ? `<span class="palantir-identity-item__when">${escapePalantirMarkup(account.username)}</span>` : ""}
                        </div>
                        ${account.source ? `<span class="palantir-identity-item__badges">${PalantirUserLookup.badges([{label: account.source, style: "is-accent"}])}</span>` : ""}
                    </header>
                    ${contacts.length === 0 ? "" : `<div class="palantir-account__contacts">${contacts.map(contact => `
                        <span class="palantir-account__contact"><i class="mdi ${contact.icon}" aria-hidden="true"></i>${escapePalantirMarkup(contact.value)}</span>`).join("")}
                    </div>`}
                    ${PalantirUserLookup.facts(facts)}
                    ${clients.length === 0 ? "" : `
                    <div class="palantir-identity-item__section">
                        <span class="palantir-identity-item__label">Identity Providers <span class="palantir-identity-item__count">${clients.length}</span></span>
                        <span class="palantir-identity-item__chips">${clients.map(client =>
                            `<span class="palantir-identity-chip"><i class="mdi mdi-account-arrow-right-outline" aria-hidden="true"></i>${escapePalantirMarkup(client)}</span>`).join("")}</span>
                    </div>`}
                    ${attributes.length === 0 ? "" : `
                    <div class="palantir-identity-item__section">
                        <span class="palantir-identity-item__label">Attributes <span class="palantir-identity-item__count">${attributes.length}</span></span>
                        <span class="palantir-identity-item__chips">${attributes.map(attribute =>
                            `<span class="palantir-identity-chip" title="${escapePalantirMarkup([].concat(account.attributes[attribute] ?? []).join(", "))}">${escapePalantirMarkup(attribute)}</span>`).join("")}</span>
                    </div>`}
                </article>`;
        });
        return `<div class="palantir-identity-list">${cards.join("")}</div>`;
    }

    /**
     * Facts about a session or token as labelled values; a fact with no value is left out.
     *
     * @param facts each with a label, a value, an optional hint shown on hover and whether the value is monospaced
     * @returns {string} the markup
     */
    static facts(facts) {
        const shown = facts.filter(fact => fact.value !== null && fact.value !== undefined && String(fact.value) !== "");
        return shown.length === 0 ? "" : `<dl class="palantir-identity-facts">${shown.map(fact => `
            <div class="palantir-identity-facts__fact">
                <dt>${escapePalantirMarkup(fact.label)}</dt>
                <dd class="${fact.mono ? "is-mono" : ""}" ${fact.hint ? `title="${escapePalantirMarkup(fact.hint)}"` : ""}>${escapePalantirMarkup(String(fact.value))}</dd>
            </div>`).join("")}</dl>`;
    }

    static badges(badges) {
        return badges.map(badge => `<span class="palantir-identity-badge ${badge.style}">${escapePalantirMarkup(badge.label)}</span>`).join("");
    }

    /**
     * An application a session signed the user in to, shown by its host and path, with the whole address on hover.
     *
     * @param url the address of the application
     * @returns {string} the markup
     */
    static serviceChip(url) {
        let shown = url;
        try {
            const parsed = new URL(url);
            shown = `${parsed.host}${parsed.pathname === "/" ? "" : parsed.pathname}`;
        } catch (error) {
            shown = url;
        }
        return `<span class="palantir-identity-chip" title="${escapePalantirMarkup(url)}"><i class="mdi mdi-web" aria-hidden="true"></i>${escapePalantirMarkup(shown)}</span>`;
    }

    /**
     * When a token ends: when it has lived as long as it may, or sooner when it is not used for as long as it may stay
     * unused, as far as its expiration policy says.
     *
     * @param token the token
     * @returns {Date|null} when it ends, or null when that is not known
     */
    static tokenEnds(token) {
        const policy = token.expirationPolicy ?? {};
        const after = (start, seconds) => {
            const date = PalantirUserLookup.dateOf(start);
            const amount = Number(seconds);
            return date && Number.isFinite(amount) && amount > 0 ? new Date(date.getTime() + amount * 1000) : null;
        };
        return PalantirUserLookup.earliest([
            after(token.creationTime, policy.timeToLive),
            after(token.lastTimeUsed ?? token.creationTime, policy.timeToIdle)
        ]);
    }

    static earliest(values) {
        const dates = values.map(value => PalantirUserLookup.dateOf(value)).filter(Boolean);
        return dates.length === 0 ? null : new Date(Math.min(...dates.map(date => date.getTime())));
    }

    static titleCase(text) {
        return String(text).replace(/\b\w/g, letter => letter.toUpperCase());
    }

    /**
     * How long ago, or how soon, a moment is, in the words of the browser's locale.
     *
     * @param value a date as {@link #dateOf} reads it
     * @returns {string} such as "5 minutes ago" or "in 2 hours", or an empty string when it is not a date
     */
    static ago(value) {
        const date = PalantirUserLookup.dateOf(value);
        if (date === null) {
            return "";
        }
        const seconds = Math.round((date.getTime() - Date.now()) / 1000);
        const units = [["year", 31536000], ["month", 2592000], ["week", 604800], ["day", 86400], ["hour", 3600], ["minute", 60]];
        PalantirUserLookup.relativeTime ??= new Intl.RelativeTimeFormat(undefined, {numeric: "auto"});
        const format = PalantirUserLookup.relativeTime;
        const unit = units.find(([, size]) => Math.abs(seconds) >= size);
        return unit ? format.format(Math.round(seconds / unit[1]), unit[0]) : format.format(0, "second");
    }

    /**
     * Read a date as CAS writes one: an ISO date, an epoch in milliseconds, or the parts of a date as a list.
     *
     * @param value the date
     * @returns {Date|null} the date, or null when it is none
     */
    static dateOf(value) {
        if (value === null || value === undefined || value === "") {
            return null;
        }
        if (value instanceof Date) {
            return Number.isNaN(value.getTime()) ? null : value;
        }
        const date = new Date(Array.isArray(value) ? Date.UTC(value[0], (value[1] ?? 1) - 1, value[2] ?? 1,
            value[3] ?? 0, value[4] ?? 0, value[5] ?? 0) : value);
        return Number.isNaN(date.getTime()) ? null : date;
    }

    static tokenKind(token) {
        return /^RT-/.test(token.id) || /RefreshToken/.test(token["@class"] ?? "") ? "Refresh token" : "Access token";
    }

    /**
     * Show a token by its first and last characters only: the id of an access or refresh token is the token itself,
     * which anyone holding it can use.
     *
     * @param id the token id
     * @returns {string} the masked id
     */
    static maskToken(id) {
        return id.length > 16 ? `${id.slice(0, 10)}…${id.slice(-4)}` : `${id.slice(0, 4)}…`;
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
                    ${row.action ? `<button type="button" class="palantir-button" data-user-action="${row.action.name}" data-palantir-write="${PalantirUserLookup.ACTION_ENDPOINTS[row.action.name] ?? ""}" ${data}>${escapePalantirMarkup(row.action.label)}</button>` : ""}
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
        const count = state === "loaded" && source.counted !== false ? content.length : null;
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
        const counted = count => {
            if (sequence === PalantirUserLookup.lookupSequence) {
                PalantirUserLookup.counts.set(source.key, count);
                PalantirUserLookup.summarize(username, sequence);
            }
        };
        try {
            const records = await source.load(username, sequence);
            replace(records.length === 0 ? "empty" : "loaded", records);
            counted(records.length);
            return {source, count: records.length};
        } catch (error) {
            replace("error", error.message);
            counted(null);
            return {source, count: null};
        }
    }

    /**
     * Summarize what the cards of a lookup found, once every card has answered, and again whenever a card loads later,
     * such as one reloaded after an action.
     *
     * @param username the user being looked up
     * @param sequence the lookup the cards belong to
     */
    static summarize(username, sequence) {
        if (sequence !== PalantirUserLookup.lookupSequence || PalantirUserLookup.settled !== sequence) {
            return;
        }
        const sources = PalantirUserLookup.availableSources();
        const counts = PalantirUserLookup.counts;
        const found = sources.filter(source => counts.get(source.key) > 0 && source.counted !== false);
        const failed = sources.filter(source => counts.has(source.key) && counts.get(source.key) === null);
        const parts = found.map(source => `${source.title} ${counts.get(source.key)}`);
        const summary = [
            parts.length > 0 ? `${username}: ${parts.join(" · ")}` : `Nothing is recorded for ${username}.`,
            failed.length > 0 ? `${failed.length} ${failed.length === 1 ? "source" : "sources"} could not be read.` : ""
        ].filter(Boolean).join(" ");
        $("#userLookupSummary").text(summary);
    }

    /**
     * Show no user: what an earlier lookup found is taken off the page, and the answers it is still waiting for are
     * dropped when they arrive.
     */
    static clear() {
        PalantirUserLookup.lookupSequence++;
        PalantirUserLookup.currentUsername = "";
        PalantirUserLookup.tokens = new Map();
        PalantirUserLookup.resolvedAttributes = null;
        PalantirUserLookup.counts = new Map();
        $("#userLookupResults table").filter((index, table) => $.fn.dataTable.isDataTable(table))
            .each((index, table) => $(table).DataTable().destroy());
        $("#userLookupResults").empty();
        $("#userLookupSummary").text("");
        $("#userLookupUsername").val("");
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
        PalantirLinks.shown(Tabs.USERS, trimmed);
        PalantirUserLookup.tokens = new Map();
        PalantirUserLookup.resolvedAttributes = null;
        PalantirUserLookup.counts = new Map();
        const sequence = ++PalantirUserLookup.lookupSequence;
        const sources = PalantirUserLookup.availableSources();
        $("#userLookupResults table").filter((index, table) => $.fn.dataTable.isDataTable(table))
            .each((index, table) => $(table).DataTable().destroy());
        $("#userLookupResults").html(sources.map(source => PalantirUserLookup.card(source, "loading", null, trimmed)).join(""));
        $("#userLookupSummary").text(`Looking up ${trimmed}…`);
        await Promise.all(sources.map(source => PalantirUserLookup.loadSource(source, trimmed, sequence)));
        if (sequence !== PalantirUserLookup.lookupSequence) {
            return;
        }
        PalantirUserLookup.settled = sequence;
        PalantirUserLookup.summarize(trimmed, sequence);

    }

    /**
     * Run a card action after the operator confirms it, then reload that card. What the action changes is settled when
     * its button is clicked, for the user then shown and named in the question, so going Back or looking up someone
     * else while the question is open never turns it on another user; the card is reloaded only if that user is still
     * shown.
     *
     * @param button the action button that was clicked
     */
    static runAction(button) {
        const data = button.dataset;
        const actions = {
            "end-session": {
                title: "End This SSO Session?",
                url: () => `${CasActuatorEndpoints.ssoSessions()}/${encodeURIComponent(data.ticket)}`
            },
            "remove-mfa-device": {
                title: "Remove This MFA Device?",
                url: () => `${CasActuatorEndpoints.mfaDevices()}/${encodeURIComponent(data.username)}/${encodeURIComponent(data.provider)}/${encodeURIComponent(data.key)}`
            },
            "remove-trusted-device": {
                title: "Remove This Trusted Device?",
                url: () => `${CasActuatorEndpoints.multifactorTrustedDevices()}/${encodeURIComponent(data.key)}`
            },
            "remove-throttle": {
                title: "Remove This Throttled Attempt?",
                url: () => `${CasActuatorEndpoints.throttles()}?key=${encodeURIComponent(data.key)}`
            },
            "revoke-consent": {
                title: "Revoke This Consent Decision?",
                url: () => `${CasActuatorEndpoints.attributeConsent()}/${encodeURIComponent(data.principal)}/${encodeURIComponent(data.id)}`
            },
            "revoke-token": {
                title: "Revoke This Token?",
                text: "The application can no longer use it, and has to ask the user again.",
                urls: () => [PalantirUserLookup.tokens.get(data.token)].filter(Boolean)
                    .map(token => `${CasActuatorEndpoints.oauthTokens()}/${encodeURIComponent(token)}`)
            },
            "revoke-all-tokens": {
                title: `Revoke Every Token of ${PalantirUserLookup.currentUsername}?`,
                text: "Their applications can no longer use these tokens, and have to ask the user again.",
                urls: () => [...PalantirUserLookup.tokens.values()]
                    .map(token => `${CasActuatorEndpoints.oauthTokens()}/${encodeURIComponent(token)}`)
            }
        };
        const action = actions[data.userAction];
        const card = button.closest("[data-user-source]");
        const source = PalantirUserLookup.sources().find(entry => entry.key === card?.dataset.userSource);
        if (!action || !source) {
            return;
        }
        const username = PalantirUserLookup.currentUsername;
        const sequence = PalantirUserLookup.lookupSequence;
        const urls = action.urls ? action.urls() : [action.url()];
        Swal.fire({
            titleText: action.title,
            text: action.text ?? "This cannot be undone.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        }).then(async result => {
            if (!result.isConfirmed) {
                return;
            }
            for (const url of urls) {
                try {
                    await $.ajax({url: url, type: "DELETE", contentType: "application/x-www-form-urlencoded"});
                } catch (xhr) {
                    displayBanner(xhr);
                    break;
                }
            }
            if (sequence === PalantirUserLookup.lookupSequence) {
                await PalantirUserLookup.loadSource(source, username, sequence);
            }
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
    if (currentActiveTab !== Tabs.USERS.index || !isDashboardTabInitialized(Tabs.USERS)) {
        PalantirPages.open(Tabs.USERS);
    }
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
    }).on("click", "[data-user-copy]", async event => {
        const button = event.currentTarget;
        try {
            await navigator.clipboard.writeText(button.dataset.userCopy);
            button.classList.add("is-copied");
            setTimeout(() => button.classList.remove("is-copied"), 1500);
        } catch (error) {
            console.debug("The ticket could not be copied", error);
        }
    });
    $("#userLookupResults").on("click", "[data-user-navigate=interrupts]", event => {
        openInterruptInquiry(event.currentTarget.dataset.username);
    });
    $("#userLookupResults").on("click", "[data-user-release]", async event => {
        const picked = PalantirAttributeRelease.picked(String($("#userReleaseApplication").val() ?? ""));
        if (picked === null) {
            $("#userReleaseResult").html(`<p class="palantir-user-card__note is-error"><i class="mdi mdi-alert-circle-outline" aria-hidden="true"></i>${
                escapePalantirMarkup("Pick an application from the list: what is typed names no single application.")}</p>`);
            $("#userReleaseApplication").trigger("focus");
            return;
        }
        const button = $(event.currentTarget).prop("disabled", true);
        const service = String(picked.id);
        const result = $("#userReleaseResult").html("<p class=\"palantir-user-card__note\">Working out the attributes…</p>");
        try {
            const released = await PalantirAttributeRelease.preview(PalantirUserLookup.currentUsername, service);
            const resolved = PalantirUserLookup.resolvedAttributes;
            result.html(PalantirAttributeRelease.render(released.attributes,
                resolved?.username === PalantirUserLookup.currentUsername ? resolved.attributes : null));
        } catch (error) {
            result.html(`<p class="palantir-user-card__note is-error"><i class="mdi mdi-alert-circle-outline" aria-hidden="true"></i>${escapePalantirMarkup(error.message)}</p>`);
        } finally {
            button.prop("disabled", false);
        }
    });
}

/**
 * The attributes an application would receive for a user, worked out by CAS with the application's attribute release
 * policy, as on the Users page and from an application's menu. The user's attributes are resolved without a password,
 * so attributes that only come from authenticating, such as those a login adds, are not part of the preview.
 */
class PalantirAttributeRelease {
    static choices = [];

    static selected = null;

    /**
     * The application to preview: the one picked from the suggestions, else the one application whose id or name is
     * typed. When what is typed is the id of one application and the name of another, or the name of several, nothing
     * is picked and the operator is asked to choose from the list.
     *
     * @param typed what the field holds
     * @returns {object|null} the application, or null when none is picked or exactly named
     */
    static picked(typed) {
        const text = typed.trim();
        if (PalantirAttributeRelease.selected) {
            return PalantirAttributeRelease.selected;
        }
        if (text === "") {
            return null;
        }
        const named = PalantirAttributeRelease.choices.filter(service => String(service.id) === text
            || String(service.name ?? "").toLowerCase() === text.toLowerCase());
        return named.length === 1 ? named[0] : null;
    }

    /**
     * Whether the preview can run: the release endpoint is enabled and accepts the POST it answers to.
     *
     * @returns {boolean} true when the preview can run
     */
    static available() {
        return Boolean(CasActuatorEndpoints.releaseAttributes())
            && Boolean(CasActuatorEndpoints.registeredServices())
            && CasActuatorEndpoints.writable("releaseAttributes");
    }

    /**
     * The applications to choose from, by name.
     *
     * @returns {Promise<object[]>} the applications
     */
    static async applications() {
        let services = typeof registeredServicesById !== "undefined" ? [...registeredServicesById.values()] : [];
        if (services.length === 0) {
            try {
                const response = await $.get(CasActuatorEndpoints.registeredServices());
                services = Array.isArray(response?.[1]) ? response[1] : [];
            } catch (error) {
                console.debug("The applications could not be read for the attribute release preview", error);
            }
        }
        return services.slice().sort((left, right) => String(left.name ?? "").localeCompare(String(right.name ?? "")));
    }

    /**
     * Ask CAS for the attributes an application would receive for a user.
     *
     * @param username the user
     * @param service the numeric id of the application
     * @returns {Promise<{username: string, attributes: object}>} the released attributes
     */
    static async preview(username, service) {
        try {
            return await $.ajax({
                url: CasActuatorEndpoints.releaseAttributes(),
                type: "POST",
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify({username: username, service: String(service)})
            });
        } catch (xhr) {
            const message = xhr?.responseJSON?.message ?? xhr?.responseJSON?.error;
            throw new Error(message
                ? `CAS could not work out the attributes: ${message}`
                : "CAS could not work out the attributes for this user and application; the application may be disabled.");
        }
    }

    /**
     * Draw the released attributes, and the user's attributes that are not released under the same name.
     *
     * @param attributes the released attributes, by name
     * @param resolved the user's attributes, by name, when known
     * @returns {string} the markup
     */
    static render(attributes, resolved) {
        const released = Object.entries(attributes ?? {}).sort(([left], [right]) => left.localeCompare(right));
        const rows = released.map(([name, values]) => `
            <tr class="mdc-data-table__row">
                <td class="mdc-data-table__cell">${palantirCode(name)}</td>
                <td class="mdc-data-table__cell">${palantirCode([].concat(values ?? []).join(", "))}</td>
            </tr>`).join("");
        const withheld = resolved
            ? Object.keys(resolved).filter(name => !Object.hasOwn(attributes ?? {}, name)).sort()
            : [];
        return `
            ${released.length === 0
        ? "<p class=\"palantir-user-card__note\">The application would receive no attributes for this user.</p>"
        : `<table class="mdc-data-table__table table table-striped noborder palantir-user-release__table">
                    <thead>
                    <tr class="mdc-data-table__header-row">
                        <th class="mdc-data-table__header-cell" scope="col">Released As</th>
                        <th class="mdc-data-table__header-cell" scope="col">Value(s)</th>
                    </tr>
                    </thead>
                    <tbody class="mdc-data-table__content">${rows}</tbody>
                </table>`}
            ${withheld.length > 0 ? `
                <p class="palantir-user-card__secondary">Not released under the same name:
                    ${withheld.map(name => `<span class="palantir-tag">${escapePalantirMarkup(name)}</span>`).join(" ")}</p>` : ""}
            <p class="palantir-user-card__note">
                <i class="mdi mdi-information-outline" aria-hidden="true"></i>
                Worked out by the application's attribute release policy without signing the user in; what a protocol adds
                on top, such as SAML2 attribute name formats or OpenID Connect claims mapped from scopes, is not shown.
            </p>`;
    }

    /**
     * Preview the attributes an application would receive, from its menu: ask for the user, then show the attributes.
     *
     * @param serviceId the numeric id of the application
     * @returns {Promise<void>} resolved once the preview is closed
     */
    static async openForApplication(serviceId) {
        const service = typeof registeredServicesById !== "undefined" ? registeredServicesById.get(String(serviceId)) : null;
        const name = service?.name ?? `application ${serviceId}`;
        const asked = await Swal.fire({
            titleText: `Preview What ${name} Receives`,
            input: "text",
            inputLabel: "Username",
            inputValue: PalantirUserLookup.currentUsername,
            inputAttributes: {autocapitalize: "off", autocomplete: "off"},
            showCancelButton: true,
            confirmButtonText: "Preview",
            showLoaderOnConfirm: true,
            inputValidator: value => String(value ?? "").trim() === "" ? "Enter a username." : null,
            preConfirm: async value => {
                try {
                    return {username: String(value).trim(), released: await PalantirAttributeRelease.preview(String(value).trim(), serviceId)};
                } catch (error) {
                    Swal.showValidationMessage(error.message);
                    return false;
                }
            }
        });
        if (!asked.isConfirmed) {
            return;
        }
        let resolved = null;
        if (CasActuatorEndpoints.personDirectory()) {
            try {
                resolved = (await $.get(`${CasActuatorEndpoints.personDirectory()}/cache/${encodeURIComponent(asked.value.username)}`))?.attributes ?? null;
            } catch (error) {
                resolved = null;
            }
        }
        await Swal.fire({
            titleText: `What ${name} Receives for ${asked.value.username}`,
            html: `<div class="palantir-user-release is-dialog">${PalantirAttributeRelease.render(asked.value.released.attributes, resolved)}</div>`,
            width: Math.min(900, $(window).width() - 32),
            confirmButtonText: "Close"
        });
    }
}
