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
                    0: `<code>${key}</code>`,
                    1: `<code>${value}</code>`
                });
            }
            consentAttributesTable.draw();

            let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("consentAttributes-dialog"));
            dialog["open"]();
        }

        function deleteConsent(context) {
            const {id, principal} = context.rowData.decision;
            Swal.fire({
                title: `Are you sure you want to delete this entry for ${principal}?`,
                text: "Once deleted, you may not be able to recover this entry.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.attributeConsent()}/${principal}/${id}`,
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
            table: consentTable,
            selector: "#consentTable tbody tr",
            items: {
                view: {name: "View Attributes", icon: contextMenuIcon("mdi-eye")},
                delete: {name: "Delete Consent", icon: contextMenuIcon("mdi-delete")}
            },
            callback: (key, context) => {
                if (key === "view") {
                    viewConsentAttributes(context.rowData.attributes);
                } else if (key === "delete") {
                    deleteConsent(context);
                }
            }
        });

        $.get(CasActuatorEndpoints.attributeConsent(), response => {
            for (const source of response) {
                consentTable.row.add({
                    0: `<code>${source.decision.id}</code>`,
                    1: `<code>${source.decision.principal}</code>`,
                    2: `<code>${source.decision.service}</code>`,
                    3: `<code></code>`,
                    4: `<code>${source.decision.createdDate}</code>`,
                    5: `<code>${source.decision.options}</code>`,
                    6: `<code>${source.decision.reminder} ${source.decision.reminderTimeUnit}</code>`,
                    decision: source.decision,
                    attributes: source.attributes
                });
            }
            consentTable.draw();

        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
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
