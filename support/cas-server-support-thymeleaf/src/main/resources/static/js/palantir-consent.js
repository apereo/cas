async function initializeConsentOperations() {
    if (CasActuatorEndpoints.attributeConsent()) {
        const consentAttributesTable = $("#consentAttributesTable").DataTable({
            pageLength: 10,
            autoWidth: false,
            drawCallback: settings => {
                $("#consentAttributesTable tr").addClass("mdc-data-table__row");
                $("#consentAttributesTable td").addClass("mdc-data-table__cell");
            }
        });

        const consentTable = $("#consentTable").DataTable({
            pageLength: 10,
            autoWidth: false,
            columnDefs: [
                {visible: false, targets: 0},
                {visible: false, targets: 3},
            ],
            drawCallback: settings => {
                $("#consentTable tr").addClass("mdc-data-table__row");
                $("#consentTable td").addClass("mdc-data-table__cell");
            }
        });
        consentTable.clear();

        function viewConsentAttributes(attributes) {
            consentAttributesTable.clear();
            for (const [key, value] of Object.entries(attributes)) {
                consentAttributesTable.row.add({
                    0: `${palantirCode(key)}`,
                    1: `${palantirCode(value)}`
                });
            }
            consentAttributesTable.draw();

            let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("consentAttributes-dialog"));
            dialog["open"]();
        }

        function deleteConsent(context) {
            const {id, principal} = context.rowData.decision;
            PalantirDialogs.confirm({
                title: `Are You Sure You Want to Delete This Entry for ${principal}?`,
                text: "Once deleted, you may not be able to recover this entry."
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.attributeConsent()}/${encodeURIComponent(principal)}/${encodeURIComponent(id)}`,
                            type: "DELETE",
                            contentType: "application/x-www-form-urlencoded",
                            success: (response, status, xhr) => {
                                context.row.remove().draw();
                                showConsentUsername(consentTable.rows().count());
                            },
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }

        initializeDataTableContextMenu({
            table: consentTable,
            selector: "#consentTable tbody tr",
            items: {
                view: {name: "View Attributes", icon: contextMenuIcon("mdi-eye")},
                delete: {name: "Delete Consent", write: "attributeConsent", icon: contextMenuIcon("mdi-delete")}
            },
            callback: (key, context) => {
                if (key === "view") {
                    viewConsentAttributes(context.rowData.attributes);
                } else if (key === "delete") {
                    deleteConsent(context);
                }
            }
        });

        let consentUsername = "";
        let consentLookup = 0;

        function showConsentUsername(count) {
            $("#showAllConsentButton").prop("hidden", consentUsername === "");
            $("#consentUsernameHint").text(consentUsername === ""
                ? "Every consent decision is shown; enter a username to see only the decisions of that username."
                : `${count} consent decision${count === 1 ? "" : "s"} for ${consentUsername}. Export still includes every decision.`);
        }

        function fetchConsentDecisions() {
            const lookup = ++consentLookup;
            const url = consentUsername === ""
                ? CasActuatorEndpoints.attributeConsent()
                : `${CasActuatorEndpoints.attributeConsent()}/${encodeURIComponent(consentUsername)}`;
            $.get(url, response => {
                if (lookup !== consentLookup) {
                    return;
                }
                consentTable.clear();
                for (const source of response) {
                    consentTable.row.add({
                        0: `${palantirCode(source.decision.id)}`,
                        1: `${palantirCode(source.decision.principal)}`,
                        2: `${palantirCode(source.decision.service)}`,
                        3: `<code></code>`,
                        4: `${palantirCode(source.decision.createdDate)}`,
                        5: `${palantirCode(source.decision.options)}`,
                        6: palantirCode(`${source.decision.reminder} ${source.decision.reminderTimeUnit}`),
                        decision: source.decision,
                        attributes: source.attributes
                    });
                }
                consentTable.draw();
                showConsentUsername(response.length);
            }).fail((xhr, status, error) => {
                if (lookup !== consentLookup) {
                    return;
                }
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }

        fetchConsentDecisions();

        $("#fmConsentUsername").off("submit").on("submit", event => {
            event.preventDefault();
            consentUsername = String($("#consentUsername").val() ?? "").trim();
            fetchConsentDecisions();
        });
        $("#showAllConsentButton").off("click").on("click", () => {
            $("#consentUsername").val("").trigger("input");
            consentUsername = "";
            fetchConsentDecisions();
            $("#consentUsername").trigger("focus");
        });

        $("button[name=exportAllConsent]").off().on("click", () => {
            if (CasActuatorEndpoints.attributeConsent()) {
                fetch(`${CasActuatorEndpoints.attributeConsent()}/export`, {
                    credentials: 'include'
                })
                    .then(response => {
                        const filename = response.headers.get("filename");
                        response.blob().then(blob => {
                            const link = document.createElement("a");
                            link.href = window.URL.createObjectURL(blob);
                            link.download = filename;
                            document.body.appendChild(link);
                            link.click();
                            document.body.removeChild(link);
                        });

                    })
                    .catch(error => console.error("Error fetching file:", error));
            }
        });
    }
}
