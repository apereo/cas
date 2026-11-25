function removeSpringSession(id) {
    PalantirDialogs.confirm({
        title: "Are you sure you want to delete this session?",
        text: "Once deleted, you may not be able to recover this session."
    })
        .then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: `${CasActuatorEndpoints.sessions()}/${encodeURIComponent(id)}`,
                    type: "DELETE",
                    contentType: "application/x-www-form-urlencoded",
                    success: () => {
                        $("#springSessionsTable").DataTable().rows((index, data) => data.sessionId === id).remove().draw();
                    },
                    error: (xhr, status, error) => {
                        console.error("Error fetching data:", error);
                        displayBanner(xhr);
                    }
                });
            }
        });
}

function formatSpringSessionDuration(value) {
    if (value === undefined || value === null || value === "") {
        return "&mdash;";
    }
    if (typeof value === "string" && value.startsWith("PT")) {
        const match = value.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d+)?)S)?/);
        if (match) {
            const seconds = (Number(match[1] ?? 0) * 3600)
                + (Number(match[2] ?? 0) * 60)
                + Number(match[3] ?? 0);
            return formatSpringSessionDuration(seconds);
        }
        return escapePalantirMarkup(value);
    }
    const seconds = Number(value);
    if (!Number.isFinite(seconds)) {
        return escapePalantirMarkup(value);
    }
    if (seconds < 60) {
        return `${Math.round(seconds)}s`;
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.round(seconds % 60);
    if (minutes < 60) {
        return remainingSeconds > 0 ? `${minutes}m ${remainingSeconds}s` : `${minutes}m`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return remainingMinutes > 0 ? `${hours}h ${remainingMinutes}m` : `${hours}h`;
}

function formatSpringSessionIdleTime(session) {
    const lastAccessed = Date.parse(session.lastAccessedTime);
    if (!Number.isFinite(lastAccessed)) {
        return "&mdash;";
    }
    const seconds = Math.max(0, Math.floor((Date.now() - lastAccessed) / 1000));
    return formatSpringSessionDuration(seconds);
}

function springSessionMaxInactiveInterval(session) {
    return session.maxInactiveInterval
        ?? session.maxInactiveIntervalInSeconds
        ?? session.maxInactiveSeconds
        ?? session.maxInactiveTime;
}

function springSessionAttributes(session) {
    if (session.attributes && typeof session.attributes === "object") {
        return Object.keys(session.attributes);
    }
    if (session.sessionAttributes && typeof session.sessionAttributes === "object") {
        return Object.keys(session.sessionAttributes);
    }
    const names = session.attributeNames ?? [];
    return Array.isArray(names) ? names : [];
}

/**
 * A short description of the browser and system behind a user agent, and the kind of device it runs on.
 *
 * @param {string} userAgent the user agent
 * @returns {{device: string, icon: string, summary: string}} the device kind, its icon and the description
 */
function describePalantirUserAgent(userAgent) {
    const agent = String(userAgent ?? "");
    if (agent === "") {
        return {device: "unknown", icon: "mdi-devices", summary: "Unknown browser"};
    }
    const browsers = [[/Edg\//, "Edge"], [/OPR\/|Opera/, "Opera"], [/Firefox\//, "Firefox"], [/Chrome\/|CriOS\//, "Chrome"],
        [/Safari\//, "Safari"], [/curl\//i, "curl"], [/HeadlessChrome/, "Headless Chrome"]];
    const systems = [[/iPhone|iPad|iPod/, "iOS"], [/Android/, "Android"], [/Windows/, "Windows"],
        [/Mac OS X|Macintosh/, "macOS"], [/CrOS/, "ChromeOS"], [/Linux/, "Linux"]];
    const browser = browsers.find(([pattern]) => pattern.test(agent))?.[1];
    const system = systems.find(([pattern]) => pattern.test(agent))?.[1];
    const tablet = /iPad|Tablet/.test(agent) || (/Android/.test(agent) && !/Mobile/.test(agent));
    const mobile = !tablet && /Mobile|iPhone|Android/.test(agent);
    const summary = browser && system ? `${browser} on ${system}` : browser ?? system ?? agent.split(/[\s/]/)[0];
    return tablet
        ? {device: "tablet", icon: "mdi-tablet", summary}
        : mobile ? {device: "mobile", icon: "mdi-cellphone", summary} : {device: "desktop", icon: "mdi-monitor", summary};
}

/**
 * One single sign-on session as a card: when and from where the user signed in, how the session was used, and
 * the applications it reached.
 *
 * @param {object} session the session as the endpoint answers it
 * @returns {string} the card markup
 */
function ssoSessionCard(session) {
    const escape = escapePalantirMarkup;
    const authentication = session.authentication_attributes ?? {};
    const date = palantirRecordDate(session.authentication_date);
    const agent = describePalantirUserAgent(authentication.userAgent?.[0]);
    const ticket = String(session.ticket_granting_ticket ?? "");
    const uses = Number(session.number_of_uses ?? 0);
    const applications = Object.keys(session.authenticated_services ?? {});
    const rememberMe = String(session.remember_me) === "true";
    const method = [].concat(authentication.authenticationMethod ?? authentication.successfulAuthenticationHandlers ?? [])[0];
    const clientIp = authentication.clientIpAddress?.[0];
    const details = [["Signed In", palantirRecordWhen(date), "is-text"], ["Method", method, ""], ["Ticket", ticket, "is-wrap"]]
        .filter(([, value]) => value !== undefined && value !== null && String(value).trim() !== "");
    const shown = applications.slice(0, 3);
    return `
        <article class="palantir-record-card is-sso" data-sso-ticket="${escape(ticket)}" aria-label="Session signed in ${escape(palantirRecordWhen(date))}">
            <header class="palantir-record-card__head">
                <span class="palantir-protocol-badge is-sso" title="${escape(agent.device)}"><i class="mdi ${agent.icon}" aria-hidden="true"></i></span>
                <div class="palantir-record-card__title">
                    <h4 title="${escape(palantirRecordWhen(date))}">Signed in ${escape(palantirRecordAgo(date))}</h4>
                    <p title="${escape(authentication.userAgent?.[0] ?? "")}">${escape(agent.summary)}${clientIp ? ` · ${escape(clientIp)}` : ""}</p>
                </div>
            </header>
            <ul class="palantir-record-card__flags">
                <li class="is-on"><i class="mdi mdi-counter" aria-hidden="true"></i>${uses} ${uses === 1 ? "use" : "uses"}</li>
                <li class="is-on"><i class="mdi mdi-apps" aria-hidden="true"></i>${applications.length} ${applications.length === 1 ? "application" : "applications"}</li>
                <li class="${rememberMe ? "is-on" : ""}">
                    <i class="mdi ${rememberMe ? "mdi-check-circle" : "mdi-minus-circle-outline"}" aria-hidden="true"></i>Remember me<span class="palantir-sr-only">: ${rememberMe ? "yes" : "no"}</span>
                </li>
            </ul>
            ${shown.length > 0 ? `
                <p class="palantir-record-card__label">Applications</p>
                <ul class="palantir-record-card__apps">
                    ${shown.map(url => `<li title="${escape(url)}"><i class="mdi mdi-web" aria-hidden="true"></i><span>${escape(url)}</span></li>`).join("")}
                    ${applications.length > shown.length ? `<li class="is-more">and ${applications.length - shown.length} more</li>` : ""}
                </ul>` : ""}
            ${details.length > 0 ? `<dl class="palantir-record-card__details">${details.map(([label, value, style]) => `
                <div><dt>${label}</dt><dd class="${style}" title="${escape(value)}">${escape(value)}</dd></div>`).join("")}</dl>` : ""}
            <footer class="palantir-record-card__actions">
                <button type="button" class="palantir-button" data-sso-action="view">
                    <i class="mdi mdi-account-eye-outline" aria-hidden="true"></i>Details
                </button>
                <button type="button" class="palantir-button" data-sso-action="copy">
                    <i class="mdi mdi-content-copy" aria-hidden="true"></i>Copy ticket
                </button>
                <button type="button" class="palantir-icon-button palantir-record-card__delete" data-sso-action="remove"
                        data-palantir-write="ssoSessions" aria-label="Remove this session" title="Remove session">
                    <i class="mdi mdi-delete-outline" aria-hidden="true"></i>
                </button>
            </footer>
        </article>`;
}

/**
 * One Spring session as a card: when it started and was last used, and how long it has left before it expires
 * for being idle.
 *
 * @param {object} session the session as the endpoint answers it
 * @returns {string} the card markup
 */
function springSessionCard(session) {
    const escape = escapePalantirMarkup;
    const id = String(session.id ?? "");
    const created = palantirRecordDate(session.creationTime);
    const accessed = palantirRecordDate(session.lastAccessedTime);
    const attributes = springSessionAttributes(session).length;
    const maxInactive = palantirSessionSeconds(springSessionMaxInactiveInterval(session));
    const idle = accessed ? Math.max(0, Math.floor((Date.now() - accessed.getTime()) / 1000)) : null;
    let meter = "";
    if (maxInactive !== null && maxInactive > 0 && idle !== null) {
        const left = Math.max(0, maxInactive - idle);
        const share = Math.round(left / maxInactive * 100);
        const state = left === 0 ? "is-expired" : share <= 20 ? "is-low" : "";
        meter = `
            <div class="palantir-record-card__meter ${state}">
                <div class="palantir-record-card__meter-label">
                    <span>${left === 0 ? "Idle for longer than allowed" : "Expires if idle for"}</span>
                    <strong>${left === 0 ? "Expired" : formatSpringSessionDuration(left)}</strong>
                </div>
                <div class="palantir-record-card__meter-track" role="progressbar" aria-label="Time left before the session expires"
                     aria-valuemin="0" aria-valuemax="100" aria-valuenow="${share}">
                    <div class="palantir-record-card__meter-fill" style="width: ${share}%"></div>
                </div>
            </div>`;
    }
    return `
        <article class="palantir-record-card is-spring" data-spring-session="${escape(id)}" aria-label="Spring session ${escape(id)}">
            <header class="palantir-record-card__head">
                <span class="palantir-protocol-badge is-spring" title="Spring session"><i class="mdi mdi-leaf" aria-hidden="true"></i></span>
                <div class="palantir-record-card__title">
                    <h4 title="${escape(id)}">${escape(id)}</h4>
                    <p>Started ${escape(palantirRecordAgo(created))} · last used ${escape(palantirRecordAgo(accessed))}</p>
                </div>
            </header>
            <ul class="palantir-record-card__flags">
                <li class="is-on"><i class="mdi mdi-timer-sand" aria-hidden="true"></i>Idle ${formatSpringSessionIdleTime(session)}</li>
                <li class="is-on"><i class="mdi mdi-clock-outline" aria-hidden="true"></i>Limit ${formatSpringSessionDuration(springSessionMaxInactiveInterval(session))}</li>
                <li class="${attributes > 0 ? "is-on" : ""}"><i class="mdi mdi-format-list-bulleted" aria-hidden="true"></i>${attributes} ${attributes === 1 ? "attribute" : "attributes"}</li>
            </ul>
            ${meter}
            <dl class="palantir-record-card__details">
                <div><dt>Started</dt><dd class="is-text">${escape(palantirRecordWhen(created))}</dd></div>
                <div><dt>Last used</dt><dd class="is-text">${escape(palantirRecordWhen(accessed))}</dd></div>
            </dl>
            <footer class="palantir-record-card__actions">
                <button type="button" class="palantir-button" data-spring-action="view">
                    <i class="mdi mdi-eye-outline" aria-hidden="true"></i>Details
                </button>
                <button type="button" class="palantir-icon-button palantir-record-card__delete" data-spring-action="remove"
                        data-palantir-write="sessions" aria-label="Remove this session" title="Remove session">
                    <i class="mdi mdi-delete-outline" aria-hidden="true"></i>
                </button>
            </footer>
        </article>`;
}

function palantirSessionSeconds(value) {
    if (value === undefined || value === null || value === "") {
        return null;
    }
    if (typeof value === "string" && value.startsWith("PT")) {
        const match = value.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d+)?)S)?/);
        return match ? Number(match[1] ?? 0) * 3600 + Number(match[2] ?? 0) * 60 + Number(match[3] ?? 0) : null;
    }
    const seconds = Number(value);
    return Number.isFinite(seconds) ? seconds : null;
}

function renderSpringSessionDetails(session, springSessionDetailsTable) {
    springSessionDetailsTable.clear();
    if (session) {
        for (const attribute of springSessionAttributes(session)) {
            springSessionDetailsTable.row.add({
                0: `${palantirCode(attribute)}`
            });
        }
    }
    springSessionDetailsTable.draw();
    $("#springSession-dialog-title").text(`Spring Session Details: ${session?.id ?? ""}`);
}

async function initializeSsoSessionOperations() {
    const ssoSessionApplicationsTable = $("#ssoSessionApplicationsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#ssoSessionApplicationsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionApplicationsTable td").addClass("mdc-data-table__cell");
        }
    });

    const ssoSessionDetailsTable = $("#ssoSessionDetailsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#ssoSessionDetailsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionDetailsTable td").addClass("mdc-data-table__cell");
        }
    });

    const springSessionsTable = $("#springSessionsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: function () {
            $("#springSessionsTable tr").addClass("mdc-data-table__row");
            $("#springSessionsTable td").addClass("mdc-data-table__cell");
            renderPalantirRecordCards(this.api(), "springSessionsCards", springSessionCard,
                username => `No Spring sessions were found for ${username}.`);
        }
    });

    const springSessionDetailsTable = $("#springSessionDetailsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: settings => {
            $("#springSessionDetailsTable tr").addClass("mdc-data-table__row");
            $("#springSessionDetailsTable td").addClass("mdc-data-table__cell");
        }
    });

    const springSessionsById = new Map();

    const ssoSessionsTable = $("#ssoSessionsTable").DataTable({
        deferRender: true,
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 6, render: palantirTextColumn}
        ],
        drawCallback: function () {
            $("#ssoSessionsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionsTable td").addClass("mdc-data-table__cell");
            renderPalantirRecordCards(this.api(), "ssoSessionsCards", ssoSessionCard,
                username => username === "" ? "Every single sign-on session was removed." : `No single sign-on sessions were found for ${username}.`);
        }
    });
    placePalantirRecordCards("ssoSessionsTable", "ssoSessionsCards", "Search for a username to see their sessions.");
    placePalantirRecordCards("springSessionsTable", "springSessionsCards", "Search for a username to see their sessions.");

    function viewSpringSession(id) {
        const session = springSessionsById.get(id);
        renderSpringSessionDetails(session, springSessionDetailsTable);
        let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("springSession-dialog"));
        dialog["open"]();
        $.get(`${CasActuatorEndpoints.sessions()}/${encodeURIComponent(id)}`, response => {
            const detailedSession = response.session ?? response;
            detailedSession.id ??= id;
            springSessionsById.set(id, detailedSession);
            renderSpringSessionDetails(detailedSession, springSessionDetailsTable);
        }).fail(() => renderSpringSessionDetails(session, springSessionDetailsTable));
    }

    initializeDataTableContextMenu({
        table: springSessionsTable,
        selector: "#springSessionsTable tbody tr",
        items: {
            view: {name: "View Spring Session Details", icon: contextMenuIcon("mdi-eye")},
            remove: {name: "Remove Spring Session", write: "sessions", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            const id = context.rowData.sessionId;
            if (key === "view") {
                viewSpringSession(id);
            } else if (key === "remove") {
                removeSpringSession(id);
            }
        }
    });

    function viewSsoSession(rowData) {
        ssoSessionApplicationsTable.clear();
        ssoSessionDetailsTable.clear();
        for (const [key, value] of Object.entries(rowData.services)) {
            ssoSessionApplicationsTable.row.add({
                0: `${palantirCode(key)}`,
                1: `${palantirCode(value.id)}`
            });
        }

        for (const [key, value] of Object.entries(rowData.attributes.principal)) {
            ssoSessionDetailsTable.row.add({
                0: `<code>Principal</code>`,
                1: `${palantirCode(key)}`,
                2: `${palantirCode(value)}`
            });
        }
        for (const [key, value] of Object.entries(rowData.attributes.authentication)) {
            ssoSessionDetailsTable.row.add({
                0: `<code>Authentication</code>`,
                1: `${palantirCode(key)}`,
                2: `${palantirCode(value)}`
            });
        }
        ssoSessionDetailsTable.draw();
        ssoSessionApplicationsTable.draw();

        let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("ssoSession-dialog"));
        dialog["open"]();
    }

    function removeSsoSession(ticket) {
        PalantirDialogs.confirm({
            title: "Are you sure you want to delete this session?",
            text: "Once deleted, you may not be able to recover this session."
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `${CasActuatorEndpoints.ssoSessions()}/${encodeURIComponent(ticket)}`,
                        type: "DELETE",
                        contentType: "application/x-www-form-urlencoded",
                        success: () => ssoSessionsTable.rows((index, data) => data.ticketGrantingTicket === ticket).remove().draw(),
                        error: (xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                        }
                    });
                }
            });
    }

    initializeDataTableContextMenu({
        table: ssoSessionsTable,
        selector: "#ssoSessionsTable tbody tr",
        items: {
            view: {name: "View SSO Session Details", icon: contextMenuIcon("mdi-account-eye")},
            copy: {name: "Copy Ticket Granting Ticket", icon: contextMenuIcon("mdi-content-copy")},
            remove: {name: "Remove SSO Session", write: "ssoSessions", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            if (key === "view") {
                viewSsoSession(context.rowData);
            } else if (key === "copy") {
                copyToClipboard(context.rowData.ticketGrantingTicket);
            } else if (key === "remove") {
                removeSsoSession(context.rowData.ticketGrantingTicket);
            }
        }
    });

    $("#ssoSessionsCards").off("click").on("click", "[data-sso-action]", event => {
        const button = event.currentTarget;
        const ticket = button.closest("[data-sso-ticket]").dataset.ssoTicket;
        const action = button.dataset.ssoAction;
        if (action === "copy") {
            copyToClipboard(ticket);
        } else if (action === "remove") {
            removeSsoSession(ticket);
        } else if (action === "view") {
            const row = ssoSessionsTable.rows((index, data) => data.ticketGrantingTicket === ticket).data().toArray()[0];
            if (row) {
                viewSsoSession(row);
            }
        }
    });

    $("#springSessionsCards").off("click").on("click", "[data-spring-action]", event => {
        const button = event.currentTarget;
        const id = button.closest("[data-spring-session]").dataset.springSession;
        if (button.dataset.springAction === "remove") {
            removeSpringSession(id);
        } else {
            viewSpringSession(id);
        }
    });

    $("#springSessionsButton").off().on("click", async () => {
        if (CasActuatorEndpoints.sessions()) {
            const form = document.getElementById("fmSpringSessions");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#springSessionUsername").val();
            const {answered, value: response} = await PalantirBusy.during({
                region: "#springsession-tab",
                after: "#fmSpringSessions",
                button: "#springSessionsButton",
                label: `Looking up the Spring sessions of ${username}`,
                failure: `The Spring sessions of ${username} could not be read`,
                request: () => $.ajax({
                    url: `${CasActuatorEndpoints.sessions()}?username=${encodeURIComponent(username)}`,
                    type: "GET",
                    contentType: "application/x-www-form-urlencoded"
                })
            });
            if (answered) {
                springSessionsTable.clear();
                springSessionsById.clear();
                for (const session of response.sessions) {
                    springSessionsById.set(session.id, session);
                    const attributes = springSessionAttributes(session);
                    springSessionsTable.row.add({
                        0: `${palantirCode(session.id)}`,
                        1: `${palantirCode(session.creationTime)}`,
                        2: `${palantirCode(session.lastAccessedTime)}`,
                        3: `${palantirCode(formatSpringSessionIdleTime(session))}`,
                        4: `${palantirCode(formatSpringSessionDuration(springSessionMaxInactiveInterval(session)))}`,
                        5: `${palantirCode(attributes.length)}`,
                        sessionId: session.id,
                        record: session
                    });
                }
                $("#springSessionsCards").data("searched", username);
                springSessionsTable.draw();
            }
        }
        return false;
    });

    $("#removeSsoSessionButton").off().on("click", () => {
        if (CasActuatorEndpoints.ssoSessions()) {
            const form = document.getElementById("fmSsoSessions");
            if (!form.reportValidity()) {
                return false;
            }

            PalantirDialogs.confirm({
                title: "Are you sure you want to delete all sessions for the user?",
                text: "Once deleted, you may not be able to recover this and user's SSO session will be removed."
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        const username = $("#ssoSessionUsername").val();

                        $.ajax({
                            url: `${CasActuatorEndpoints.ssoSessions()}/users/${encodeURIComponent(username)}`,
                            type: "DELETE",
                            contentType: "application/x-www-form-urlencoded",
                            success: () => {
                                $("#ssoSessionsCards").data("searched", username);
                                ssoSessionsTable.clear().draw();
                            },
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=ssoSessionButton]").off().on("click", async () => {
        if (CasActuatorEndpoints.ssoSessions()) {
            const form = document.getElementById("fmSsoSessions");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#ssoSessionUsername").val();
            const {answered, value: response} = await PalantirBusy.during({
                region: "#ssosessions-tab",
                after: "#fmSsoSessions",
                button: "button[name=ssoSessionButton]",
                label: `Looking up the single sign-on sessions of ${username}`,
                failure: `The single sign-on sessions of ${username} could not be read`,
                request: () => $.ajax({
                    url: `${CasActuatorEndpoints.ssoSessions()}/users/${encodeURIComponent(username)}`,
                    type: "GET",
                    contentType: "application/x-www-form-urlencoded"
                })
            });
            if (answered) {
                ssoSessionsTable.clear();
                ssoSessionApplicationsTable.clear();
                for (const session of response.activeSsoSessions) {
                    const attributes = {
                        principal: session["principal_attributes"],
                        authentication: session["authentication_attributes"]
                    };

                    const services = {};
                    for (const [key, value] of Object.entries(session["authenticated_services"])) {
                        services[key] = {id: value.id};
                    }

                    ssoSessionsTable.row.add({
                        0: `${palantirCode(session["authentication_date"])}`,
                        1: `${palantirCode(session["ticket_granting_ticket"])}`,
                        2: `${palantirCode(session["authentication_attributes"]?.clientIpAddress?.[0])}`,
                        3: `${palantirCode(session["authentication_attributes"]?.userAgent?.[0])}`,
                        4: `${palantirCode(session["number_of_uses"])}`,
                        5: `${palantirCode(session["remember_me"])}`,
                        6: JSON.stringify(attributes),
                        ticketGrantingTicket: session.ticket_granting_ticket,
                        attributes: attributes,
                        services: services,
                        record: session
                    });
                }
                $("#ssoSessionsCards").data("searched", username);
                ssoSessionsTable.draw();
            }
        }
        return false;
    });

    $("button[name=removeAllSsoSessionsButton]").off().on("click", () => {
        if (CasActuatorEndpoints.ssoSessions()) {
            PalantirDialogs.confirm({
                title: "Are you sure you want to delete all sessions for all users?",
                text: "Once deleted, you may not be able to recover and ALL sso sessions for ALL users will be removed."
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.ssoSessions()}`,
                            type: "DELETE",
                            contentType: "application/x-www-form-urlencoded",
                            success: () => {
                                $("#ssoSessionsCards").data("searched", "");
                                ssoSessionsTable.clear().draw();
                            },
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });

        }
    });

}
