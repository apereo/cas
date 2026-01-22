async function initializeHeimdallOperations() {

    if (!CAS_FEATURES.includes("Authorization")) {
        $("#heimdall").addClass("d-none");
        return;
    }

    const heimdallResourcesTable = $("#heimdallResourcesTable").DataTable({
        pageLength: 10,
        columnDefs: [
            {visible: false, targets: 0},
            {visible: false, targets: 1},
            {visible: true, targets: 5}
        ],
        autoWidth: false,
        drawCallback: settings => {
            $("#heimdallResourcesTable tr").addClass("mdc-data-table__row");
            $("#heimdallResourcesTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="4">Namespace: ${group}</td>
                                        </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    if (CasActuatorEndpoints.heimdall()) {
        const heimdallViewResourceEditor = initializeAceEditor("heimdallViewResourceEditor", "json");
        heimdallViewResourceEditor.setReadOnly(true);

        function fetchHeimdallResources(heimdallViewResourceEditor) {
            $.get(`${CasActuatorEndpoints.heimdall()}/resources`, response => {
                heimdallResourcesTable.clear();
                for (const [key, value] of Object.entries(response)) {
                    for (const resource of Object.values(value)) {
                        let buttons = `
                        <button type="button" name="viewHeimdallResource" href="#" 
                            data-id="${resource.id}" data-namespace="${key}"
                            title="View Resource"
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                            <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                        </button>
                    `;
                        heimdallResourcesTable.row.add({
                            0: `<code>${key}</code>`,
                            1: `${resource.id ?? "N/A"}`,
                            2: `<code>${resource.pattern ?? "N/A"}</code>`,
                            3: `<code>${resource.method ?? "N/A"}</code>`,
                            4: `<code>${resource.enforceAllPolicies ?? "false"}</code>`,
                            5: buttons
                        });
                    }
                }
                heimdallResourcesTable.draw();

                $("button[name=viewHeimdallResource]").off().on("click", function () {
                    const namespace = $(this).data("namespace");
                    const resourceId = $(this).data("id");
                    $.get(`${CasActuatorEndpoints.heimdall()}/resources/${namespace}/${resourceId}`, response => {
                        heimdallViewResourceEditor.setValue(JSON.stringify(response, null, 2));
                        heimdallViewResourceEditor.gotoLine(1);

                        const beautify = ace.require("ace/ext/beautify");
                        beautify.beautify(heimdallViewResourceEditor.session);

                        const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("heimdallViewResourceDialog"));
                        dialog["open"]();
                    })
                        .fail((xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                        });
                });
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }

        fetchHeimdallResources(heimdallViewResourceEditor);

        setInterval(() => {
            if (currentActiveTab === Tabs.ACCESS_STRATEGY.index) {
                fetchHeimdallResources();
            }
        }, palantirSettings().refreshInterval);
    }

}

async function initializeAccessStrategyOperations() {

    if (!CAS_FEATURES.includes("SAMLIdentityProvider")) {
        $("#accessEntityIdLabel").addClass("d-none");
    }
    if (!CAS_FEATURES.includes("OpenIDConnect") && !CAS_FEATURES.includes("OAuth")) {
        $("#accessClientIdLabel").addClass("d-none");
    }

    const accessStrategyEditor = initializeAceEditor("accessStrategyEditor");
    accessStrategyEditor.setReadOnly(true);

    const accessStrategyAttributesTable = $("#accessStrategyAttributesTable").DataTable({
        pageLength: 10,
        drawCallback: settings => {
            $("#accessStrategyAttributesTable tr").addClass("mdc-data-table__row");
            $("#accessStrategyAttributesTable td").addClass("mdc-data-table__cell");
        }
    });

    $("button[name=accessStrategyButton]").off().on("click", () => {
        if (CasActuatorEndpoints.serviceAccess()) {
            hideBanner();
            accessStrategyAttributesTable.clear();

            const form = document.getElementById("fmAccessStrategy");
            if (!form.reportValidity()) {
                return false;
            }

            accessStrategyEditor.setValue("");
            const formData = $(form).serializeArray();
            const renamedData = formData.filter(item => item.value !== "").map(item => {
                const newName = $(`[name="${item.name}"]`).data("param-name") || item.name;
                return {name: newName, value: item.value};
            });

            $.ajax({
                url: CasActuatorEndpoints.serviceAccess(),
                type: "POST",
                contentType: "application/x-www-form-urlencoded",
                data: $.param(renamedData),
                success: (response, status, xhr) => {
                    $("#accessStrategyEditorContainer").removeClass("d-none");
                    $("#accessStrategyAttributesContainer").removeClass("d-none");

                    accessStrategyEditor.setValue(JSON.stringify(response.registeredService, null, 2));
                    accessStrategyEditor.gotoLine(1);

                    for (const [key, value] of Object.entries(response.authentication.principal.attributes)) {
                        accessStrategyAttributesTable.row.add({
                            0: `<code>${key}</code>`,
                            1: `<code>${value}</code>`
                        });
                    }
                    accessStrategyAttributesTable.draw();
                    updateNavigationSidebar();
                    $("#authorizedServiceNavigation").off().on("click", () => navigateToApplication(response.registeredService.id));
                },
                error: (xhr, status, error) => {
                    $("#accessStrategyEditorContainer").addClass("d-none");
                    $("#accessStrategyAttributesContainer").addClass("d-none");
                    displayBanner(`Status ${xhr.status}: Service is unauthorized.`);
                }
            });
        }
    });
}
