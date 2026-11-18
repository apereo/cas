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
            remove: {name: "Remove Throttled Attempt", write: "throttles", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            if (key === "remove") {
                removeThrottledAttempt(context);
            }
        }
    });

    let throttlesUsername = "";

    function throttledValue(value) {
        return `${palantirCode(value ?? "")}`;
    }

    function showThrottlesUsername(count) {
        $("#showAllThrottlesButton").prop("hidden", throttlesUsername === "");
        $("#throttlesUsernameHint").text(throttlesUsername === ""
            ? "Every throttled attempt is shown; enter a username to see only that username's attempts, matched regardless of case."
            : `${count} throttled attempt${count === 1 ? "" : "s"} for ${throttlesUsername}. Clear and Release still apply to every attempt.`);
    }

    function fetchThrottledAttempts() {
        throttlesTable.clear();
        const url = throttlesUsername === ""
            ? CasActuatorEndpoints.throttles()
            : `${CasActuatorEndpoints.throttles()}?username=${encodeURIComponent(throttlesUsername)}`;
        $.get(url, response => {
            for (const record of response) {
                throttlesTable.row.add({
                    0: throttledValue(record.key),
                    1: throttledValue(record.id),
                    2: throttledValue(record.value),
                    3: throttledValue(record.username),
                    4: throttledValue(record.clientIpAddress),
                    5: throttledValue(record.expiration),
                    key: record.key
                });
            }
            throttlesTable.draw();
            showThrottlesUsername(response.length);
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    if (CasActuatorEndpoints.throttles()) {
        fetchThrottledAttempts();

        $("#fmThrottlesUsername").off("submit").on("submit", event => {
            event.preventDefault();
            throttlesUsername = String($("#throttlesUsername").val() ?? "").trim();
            fetchThrottledAttempts();
        });
        $("#showAllThrottlesButton").off("click").on("click", () => {
            $("#throttlesUsername").val("").trigger("input");
            throttlesUsername = "";
            fetchThrottledAttempts();
            $("#throttlesUsername").trigger("focus");
        });

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
