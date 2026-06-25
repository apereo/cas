function removeSpringSession(button, id) {
    Swal.fire({
        title: "Are you sure you want to delete this session?",
        text: "Once deleted, you may not be able to recover this session.",
        icon: "question",
        showConfirmButton: true,
        showDenyButton: true
    })
        .then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: `${CasActuatorEndpoints.sessions()}/${encodeURIComponent(id)}`,
                    type: "DELETE",
                    contentType: "application/x-www-form-urlencoded",
                    success: (response, status, xhr) => {
                        let nearestTr = $(button).closest("tr");
                        const springSessionsTable = $("#springSessionsTable").DataTable();
                        springSessionsTable.row(nearestTr).remove().draw();
                    },
                    error: (xhr, status, error) => {
                        console.error("Error fetching data:", error);
                        displayBanner(xhr);
                    }
                });
            }
        });
}

function escapeSsoHtml(str) {
    return String(str ?? "").replace(/[&<>"']/g, s => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[s]));
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
        return escapeSsoHtml(value);
    }
    const seconds = Number(value);
    if (!Number.isFinite(seconds)) {
        return escapeSsoHtml(value);
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

function renderSpringSessionDetails(session, springSessionDetailsTable) {
    springSessionDetailsTable.clear();
    if (session) {
        for (const attribute of springSessionAttributes(session)) {
            springSessionDetailsTable.row.add({
                0: `<code>${escapeSsoHtml(attribute)}</code>`
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
        drawCallback: settings => {
            $("#springSessionsTable tr").addClass("mdc-data-table__row");
            $("#springSessionsTable td").addClass("mdc-data-table__cell");
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
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, target: 7}
        ],
        drawCallback: settings => {
            $("#ssoSessionsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionsTable td").addClass("mdc-data-table__cell");
        }
    });

    $("#ssoSessionUsername").on("keypress", e => {
        if (e.which === 13) {
            $("#ssoSessionButton").click();
        }
    });
    $("#springSessionUsername").on("keypress", e => {
        if (e.which === 13) {
            $("#springSessionsButton").click();
        }
    });

    $("#springSessionsButton").off().on("click", () => {
        if (CasActuatorEndpoints.sessions()) {
            const form = document.getElementById("fmSpringSessions");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#springSessionUsername").val();
            Swal.fire({
                icon: "info",
                title: `Fetching Spring Sessions for ${username}`,
                text: "Please wait while Spring Sessions are retrieved...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });
            springSessionsTable.clear();
            springSessionsById.clear();
            $.ajax({
                url: `${CasActuatorEndpoints.sessions()}?username=${username}`,
                type: "GET",
                contentType: "application/x-www-form-urlencoded",
                success: (response, status, xhr) => {
                    for (const session of response.sessions) {
                        springSessionsById.set(session.id, session);
                        const attributes = springSessionAttributes(session);
                        springSessionsTable.row.add({
                            0: `<code>${escapeSsoHtml(session.id)}</code>`,
                            1: `<code>${escapeSsoHtml(session.creationTime)}</code>`,
                            2: `<code>${escapeSsoHtml(session.lastAccessedTime)}</code>`,
                            3: `<code>${formatSpringSessionIdleTime(session)}</code>`,
                            4: `<code>${formatSpringSessionDuration(springSessionMaxInactiveInterval(session))}</code>`,
                            5: `<code>${attributes.length}</code>`,
                            6: `
                                <button type="button" name="viewSpringSession"
                                    data-id='${escapeSsoHtml(session.id)}'
                                    title="View Spring Session Details"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                    <span class="mdc-button__label">
                                        <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                                    </span>
                                </button>
                                <button type="button" name="removeSpringSession" 
                                    data-id='${escapeSsoHtml(session.id)}'
                                    title="Remove Spring Session"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                    <span class="mdc-button__label">
                                        <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                                    </span>
                                </button>
                            `
                        });
                    }
                    springSessionsTable.draw();
                    $("button[name=viewSpringSession]").off().on("click", function () {
                        const id = $(this).data("id");
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
                    });
                    $("button[name=removeSpringSession]").off().on("click", function () {
                        removeSpringSession(this, $(this).data("id"));
                    });
                    Swal.close();
                },
                error: (xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    Swal.close();
                    displayBanner(xhr);
                }
            });
        }
    });
    
    $("#removeSsoSessionButton").off().on("click", () => {
        if (CasActuatorEndpoints.ssoSessions()) {
            const form = document.getElementById("fmSsoSessions");
            if (!form.reportValidity()) {
                return false;
            }

            Swal.fire({
                title: "Are you sure you want to delete all sessions for the user?",
                text: "Once deleted, you may not be able to recover this and user's SSO session will be removed.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        const username = $("#ssoSessionUsername").val();

                        $.ajax({
                            url: `${CasActuatorEndpoints.ssoSessions()}/users/${username}`,
                            type: "DELETE",
                            contentType: "application/x-www-form-urlencoded",
                            success: (response, status, xhr) => ssoSessionsTable.clear().draw(),
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=ssoSessionButton]").off().on("click", () => {
        if (CasActuatorEndpoints.ssoSessions()) {
            const form = document.getElementById("fmSsoSessions");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#ssoSessionUsername").val();
            Swal.fire({
                icon: "info",
                title: `Fetching SSO Sessions for ${username}`,
                text: "Please wait while single sign-on sessions are retrieved...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });

            ssoSessionsTable.clear();
            ssoSessionApplicationsTable.clear();

            $.ajax({
                url: `${CasActuatorEndpoints.ssoSessions()}/users/${username}`,
                type: "GET",
                contentType: "application/x-www-form-urlencoded",
                success: (response, status, xhr) => {
                    for (const session of response.activeSsoSessions) {
                        const attributes = {
                            principal: session["principal_attributes"],
                            authentication: session["authentication_attributes"]
                        };

                        const services = {};
                        for (const [key, value] of Object.entries(session["authenticated_services"])) {
                            services[key] = {id: value.id};
                        }

                        let serviceButtons = `
                            <button type="button" name="removeSsoSession" href="#" 
                                data-ticketgrantingticket='${session.ticket_granting_ticket}'
                                title="Remove SSO Session"
                                class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                            </button>
                            <button type="button" name="viewSsoSession" href="#" 
                                data-ticketgrantingticket='${session.ticket_granting_ticket}'
                                title="View SSO Session Details"
                                class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-account-eye min-width-32x" aria-hidden="true"></i>
                                <span id="sessionAttributes" class="d-none">${JSON.stringify(attributes)}</span>
                                <span id="sessionServices" class="d-none">${JSON.stringify(services)}</span>
                            </button>
                            <button type="button" name="copySsoSessionTicketGrantingTicket" href="#" 
                                data-ticketgrantingticket='${session.ticket_granting_ticket}'
                                title="Copy Ticket Granting Ticket to Clipboard"
                                class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-content-copy min-width-32x" aria-hidden="true"></i>
                            </button>
                    `;

                        ssoSessionsTable.row.add({
                            0: `<code>${session["authentication_date"]}</code>`,
                            1: `<code>${session["ticket_granting_ticket"]}</code>`,
                            2: `<code>${session["authentication_attributes"]?.clientIpAddress?.[0]}</code>`,
                            3: `<code>${session["authentication_attributes"]?.userAgent?.[0]}</code>`,
                            4: `<code>${session["number_of_uses"]}</code>`,
                            5: `<code>${session["remember_me"]}</code>`,
                            6: `${serviceButtons}`,
                            7: `${JSON.stringify(attributes)}`
                        });
                    }
                    ssoSessionsTable.draw();

                    Swal.close();

                    $("button[name=viewSsoSession]").off().on("click", function () {
                        ssoSessionApplicationsTable.clear();
                        const sessionServices = JSON.parse($(this).children("#sessionServices").text());
                        for (const [key, value] of Object.entries(sessionServices)) {
                            ssoSessionApplicationsTable.row.add({
                                0: `<code>${key}</code>`,
                                1: `<code>${value.id}</code>`
                            });
                        }

                        const attributes = JSON.parse($(this).children("#sessionAttributes").text());
                        for (const [key, value] of Object.entries(attributes.principal)) {
                            ssoSessionDetailsTable.row.add({
                                0: `<code>Principal</code>`,
                                1: `<code>${key}</code>`,
                                2: `<code>${value}</code>`
                            });
                        }
                        for (const [key, value] of Object.entries(attributes.authentication)) {
                            ssoSessionDetailsTable.row.add({
                                0: `<code>Authentication</code>`,
                                1: `<code>${key}</code>`,
                                2: `<code>${value}</code>`
                            });
                        }
                        ssoSessionDetailsTable.draw();
                        ssoSessionApplicationsTable.draw();

                        let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("ssoSession-dialog"));
                        dialog["open"]();
                    });

                    $("button[name=removeSsoSession]").off().on("click", function () {
                        const ticket = $(this).data("ticketgrantingticket");

                        Swal.fire({
                            title: "Are you sure you want to delete this session?",
                            text: "Once deleted, you may not be able to recover this session.",
                            icon: "question",
                            showConfirmButton: true,
                            showDenyButton: true
                        })
                            .then((result) => {
                                if (result.isConfirmed) {
                                    $.ajax({
                                        url: `${CasActuatorEndpoints.ssoSessions()}/${ticket}`,
                                        type: "DELETE",
                                        contentType: "application/x-www-form-urlencoded",
                                        success: (response, status, xhr) => {
                                            let nearestTr = $(this).closest("tr");
                                            ssoSessionsTable.row(nearestTr).remove().draw();
                                        },
                                        error: (xhr, status, error) => {
                                            console.error("Error fetching data:", error);
                                            displayBanner(xhr);
                                        }
                                    });
                                }
                            });
                    });

                    $("button[name=copySsoSessionTicketGrantingTicket]").off().on("click", function () {
                        const ticket = $(this).data("ticketgrantingticket");
                        copyToClipboard(ticket);
                    });

                },
                error: (xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    Swal.close();
                    displayBanner(xhr);
                }
            });
        }
    });

    $("button[name=removeAllSsoSessionsButton]").off().on("click", () => {
        if (CasActuatorEndpoints.ssoSessions()) {
            Swal.fire({
                title: "Are you sure you want to delete all sessions for all users?",
                text: "Once deleted, you may not be able to recover and ALL sso sessions for ALL users will be removed.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.ssoSessions()}`,
                            type: "DELETE",
                            contentType: "application/x-www-form-urlencoded",
                            success: (response, status, xhr) => ssoSessionsTable.clear().draw(),
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
