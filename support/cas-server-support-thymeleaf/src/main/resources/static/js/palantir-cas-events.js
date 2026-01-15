async function initializeCasEventsOperations() {
    if (actuatorEndpoints.events) {
        const casEventsTable = $("#casEventsTable").DataTable({
            pageLength: 10,
            drawCallback: settings => {
                $("#casEventsTable tr").addClass("mdc-data-table__row");
                $("#casEventsTable td").addClass("mdc-data-table__cell");
            }
        });

        function fetchCasEvents() {
            return setInterval(() => {
                if (currentActiveTab === Tabs.LOGGING.index) {
                    $.ajax({
                        url: `${actuatorEndpoints.events}`,
                        type: "GET",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded"
                        },
                        success: (response, textStatus, xhr) => {
                            casEventsTable.clear();
                            const jsonEvents = JSON.parse(response);
                            if (jsonEvents.length > 0) {
                                for (const entry of Object.values(jsonEvents[1])) {
                                    const geoLocation = `${entry?.properties?.geoLatitude ?? ""} ${entry?.properties?.geoLongitude ?? ""} ${entry?.properties?.geoAccuracy ?? ""}`.trim();
                                    casEventsTable.row.add({
                                        0: `<code>${entry?.creationTime ?? "N/A"}</code>`,
                                        1: `<code>${getLastWord(entry?.type) ?? "N/A"}</code>`,
                                        2: `<code>${entry?.properties?.eventId ?? "N/A"}</code>`,
                                        3: `<code>${entry?.principalId ?? "N/A"}</code>`,
                                        4: `<code>${entry?.properties?.clientip ?? "N/A"}</code>`,
                                        5: `<code>${entry?.properties?.serverip ?? "N/A"}</code>`,
                                        6: `<code>${entry?.properties?.agent ?? "N/A"}</code>`,
                                        7: `<code>${entry?.properties?.tenant ?? "N/A"}</code>`,
                                        8: `<code>${entry?.properties?.deviceFingerprint ?? "N/A"}</code>`,
                                        9: `<code>${geoLocation.length === 0 ? "N/A" : geoLocation}</code>`
                                    });
                                }
                            }
                            casEventsTable.draw();
                        },
                        error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown)
                    });
                }
            }, $("#casEventsRefreshFilter").val());
        }

        let refreshInterval = undefined;
        if (actuatorEndpoints.events) {
            refreshInterval = fetchCasEvents();
        }
        $("#casEventsRefreshFilter").selectmenu({
            change: (event, data) => {
                if (refreshInterval) {
                    clearInterval(refreshInterval);
                    refreshInterval = fetchCasEvents();
                }
            }
        });
    }
}

async function initializeAuditEventsOperations() {
    if (actuatorEndpoints.auditlog) {
        const auditEventsTable = $("#auditEventsTable").DataTable({
            pageLength: 10,
            drawCallback: settings => {
                $("#auditEventsTable tr").addClass("mdc-data-table__row");
                $("#auditEventsTable td").addClass("mdc-data-table__cell");
            }
        });

        function fetchAuditLog() {
            return setInterval(() => {
                if (currentActiveTab === Tabs.LOGGING.index) {
                    const interval = $("#auditEventsIntervalFilter").val();
                    const count = $("#auditEventsCountFilter").val();

                    $.ajax({
                        url: `${actuatorEndpoints.auditlog}?interval=${interval}&count=${count}`,
                        type: "GET",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded"
                        },
                        success: (response, textStatus, xhr) => {

                            auditEventsTable.clear();
                            for (const entry of response) {
                                auditEventsTable.row.add({
                                    0: `<code>${entry?.principal ?? "N/A"}</code>`,
                                    1: `<code>${entry?.auditableResource ?? "N/A"}</code>`,
                                    2: `<code>${entry?.actionPerformed ?? "N/A"}</code>`,
                                    3: `<code>${entry?.whenActionWasPerformed ?? "N/A"}</code>`,
                                    4: `<code>${entry?.clientInfo?.clientIpAddress ?? "N/A"}</code>`,
                                    5: `<span>${entry?.clientInfo?.userAgent ?? "N/A"}</span>`
                                });
                            }
                            auditEventsTable.draw();
                        },
                        error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown)
                    });
                }
            }, $("#auditEventsRefreshFilter").val());
        }

        let refreshInterval = undefined;
        if (actuatorEndpoints.auditlog) {
            refreshInterval = fetchAuditLog();
        }
        $("#auditEventsRefreshFilter").selectmenu({
            change: (event, data) => {
                if (refreshInterval) {
                    clearInterval(refreshInterval);
                    refreshInterval = fetchAuditLog();
                }
            }
        });
    }
}
