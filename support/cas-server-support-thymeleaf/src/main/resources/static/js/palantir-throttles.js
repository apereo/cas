async function initializeThrottlesOperations() {
    const throttlesTable = $("#throttlesTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "15%", targets: 1},
            {width: "10%", targets: 2},
            {width: "15%", targets: 3},
            {width: "10%", targets: 4},
            {width: "20%", targets: 5}
        ],
        drawCallback: settings => {
            $("#throttlesTable tr").addClass("mdc-data-table__row");
            $("#throttlesTable td").addClass("mdc-data-table__cell");
        }
    });

    function removeThrottledAttempt(context) {
        const key = context.rowData.key;
        Swal.fire({
            title: "Are you sure you want to delete this entry?",
            text: "Once deleted, you may not be able to recover this entry.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `${CasActuatorEndpoints.throttles()}?key=${encodeURIComponent(key)}`,
                        type: "DELETE",
                        contentType: "application/x-www-form-urlencoded",
                        success: (response, status, xhr) => context.row.remove().draw(),
                        error: (xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                        }
                    });
                }
            });
    }

    initializeDataTableContextMenu({
        table: throttlesTable,
        selector: "#throttlesTable tbody tr",
        items: {
            remove: {name: "Remove Throttled Attempt", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            if (key === "remove") {
                removeThrottledAttempt(context);
            }
        }
    });

    function fetchThrottledAttempts() {
        throttlesTable.clear();
        $.get(CasActuatorEndpoints.throttles(), response => {
            for (const record of response) {
                throttlesTable.row.add({
                    0: `<code>${record.key}</code>`,
                    1: `<code>${record.id}</code>`,
                    2: `<code>${record.value}</code>`,
                    3: `<code>${record.username}</code>`,
                    4: `<code>${record.clientIpAddress}</code>`,
                    5: `<code>${record.expiration}</code>`,
                    key: record.key
                });
            }
            throttlesTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    if (CasActuatorEndpoints.throttles()) {
        fetchThrottledAttempts();

        $("button[name=releaseThrottlesButton]").off().on("click", () =>
            Swal.fire({
                title: "Are you sure you want to release throttled entries?",
                text: "Released entries, when eligible, will be removed from the authentication throttling store.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.throttles()}`,
                            type: "DELETE",
                            data: {
                                clear: false
                            },
                            success: (response, textStatus, jqXHR) => fetchThrottledAttempts(),
                            error: (jqXHR, textStatus, errorThrown) => displayBanner(jqXHR)
                        });
                    }
                }));

        $("button[name=clearThrottlesButton]").off().on("click", () =>
            Swal.fire({
                title: "Are you sure you want to clear throttled entries?",
                text: "All entries will be removed from the authentication throttling store.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.throttles()}`,
                            type: "DELETE",
                            data: {
                                clear: true
                            },
                            success: (response, textStatus, jqXHR) => fetchThrottledAttempts(),
                            error: (jqXHR, textStatus, errorThrown) => displayBanner(jqXHR)
                        });
                    }
                }));
    }
}
