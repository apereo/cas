
async function initializeSsoSessionOperations() {
    const ssoSessionApplicationsTable = $("#ssoSessionApplicationsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "40%", targets: 0},
            {width: "60%", targets: 1}
        ],
        drawCallback: settings => {
            $("#ssoSessionApplicationsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionApplicationsTable td").addClass("mdc-data-table__cell");
        }
    });

    const ssoSessionDetailsTable = $("#ssoSessionDetailsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "10%", targets: 0},
            {width: "20%", targets: 1},
            {width: "70%", targets: 2}
        ],
        drawCallback: settings => {
            $("#ssoSessionDetailsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionDetailsTable td").addClass("mdc-data-table__cell");
        }
    });

    const ssoSessionsTable = $("#ssoSessionsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "13%", targets: 0},
            {width: "37%", targets: 1},
            {width: "10%", targets: 2},
            {width: "15%", targets: 3},
            {width: "5%", targets: 4},
            {width: "8%", targets: 5},
            {width: "12%", targets: 6},
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

    $("#removeSsoSessionButton").off().on("click", () => {
        if (actuatorEndpoints.ssosessions) {
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
                            url: `${actuatorEndpoints.ssosessions}/users/${username}`,
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
        if (actuatorEndpoints.ssosessions) {
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
                url: `${actuatorEndpoints.ssosessions}/users/${username}`,
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
                                        url: `${actuatorEndpoints.ssosessions}/${ticket}`,
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
        if (actuatorEndpoints.ssosessions) {
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
                            url: `${actuatorEndpoints.ssosessions}`,
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
