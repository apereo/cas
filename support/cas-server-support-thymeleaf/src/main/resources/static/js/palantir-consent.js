async function initializeConsentOperations() {
    if (actuatorEndpoints.attributeconsent) {
        const consentAttributesTable = $("#consentAttributesTable").DataTable({
            pageLength: 10,
            autoWidth: false,
            columnDefs: [
                {width: "40%", targets: 0},
                {width: "60%", targets: 1}
            ],
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
                {width: "12%", targets: 1},
                {width: "20%", targets: 2},
                {visible: false, targets: 3},
                {width: "15%", targets: 4},
                {width: "8%", targets: 5},
                {width: "8%", targets: 6},
                {width: "12%", targets: 7}
            ],
            drawCallback: settings => {
                $("#consentTable tr").addClass("mdc-data-table__row");
                $("#consentTable td").addClass("mdc-data-table__cell");
            }
        });
        consentTable.clear();
        $.get(actuatorEndpoints.attributeconsent, response => {
            for (const source of response) {

                let consentButtons = `
                 <button type="button" name="viewConsentAttributes" href="#" 
                        title="View Attributes"
                        consentId='${source.decision.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                    <span class="d-none">${JSON.stringify(source.attributes)}</span>
                </button>
                <button type="button" name="deleteConsent" href="#"
                        title="Delete Consent"
                        principal='${source.decision.principal}'
                        consentId='${source.decision.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                </button>
                `;

                consentTable.row.add({
                    0: `<code>${source.decision.id}</code>`,
                    1: `<code>${source.decision.principal}</code>`,
                    2: `<code>${source.decision.service}</code>`,
                    3: `<code></code>`,
                    4: `<code>${source.decision.createdDate}</code>`,
                    5: `<code>${source.decision.options}</code>`,
                    6: `<code>${source.decision.reminder} ${source.decision.reminderTimeUnit}</code>`,
                    7: `${consentButtons}`
                });
            }
            consentTable.draw();

            $("button[name=viewConsentAttributes]").off().on("click", function () {
                const attributes = JSON.parse($(this).children("span").first().text());
                for (const [key, value] of Object.entries(attributes)) {
                    consentAttributesTable.row.add({
                        0: `<code>${key}</code>`,
                        1: `<code>${value}</code>`
                    });
                }
                consentAttributesTable.draw();

                let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("consentAttributes-dialog"));
                dialog["open"]();
            });

            $("button[name=deleteConsent]").off().on("click", function () {
                const id = $(this).attr("consentId");
                const principal = $(this).attr("principal");
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
                                url: `${actuatorEndpoints.attributeconsent}/${principal}/${id}`,
                                type: "DELETE",
                                contentType: "application/x-www-form-urlencoded",
                                success: (response, status, xhr) => {
                                    let nearestTr = $(this).closest("tr");
                                    consentTable.row(nearestTr).remove().draw();
                                },
                                error: (xhr, status, error) => {
                                    console.error("Error fetching data:", error);
                                    displayBanner(xhr);
                                }
                            });
                        }
                    });
            });

        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });


        $("button[name=exportAllConsent]").off().on("click", () => {
            if (actuatorEndpoints.attributeconsent) {
                fetch(`${actuatorEndpoints.attributeconsent}/export`)
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
