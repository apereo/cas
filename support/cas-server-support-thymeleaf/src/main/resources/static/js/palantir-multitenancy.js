async function initializeMultitenancyOperations() {
    const tenantsTable = $("#tenantsTable").DataTable({
        pageLength: 10,
        autoWidth: true,
        drawCallback: settings => {
            $("#tenantsTable tr").addClass("mdc-data-table__row");
            $("#tenantsTable td").addClass("mdc-data-table__cell");
        }
    });

    function fetchTenants() {
        tenantsTable.clear();
        $.get(`${actuatorEndpoints.multitenancy}/tenants`, response => {
            for (const tenant of Object.values(response)) {
                let buttons = `
                     <button type="button" name="viewTenantDefinition" href="#" 
                            data-tenant-id='${tenant.id}' onclick="showTenantDefinition('${tenant.id}')"
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                    </button>
                `;

                tenantsTable.row.add({
                    0: `<code>${tenant.id}</code>`,
                    1: `<code>${tenant.description ?? ""}</code>`,
                    2: buttons
                });
            }
            tenantsTable.draw();
        })
            .fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
    }

    fetchTenants();
    setInterval(() => {
        if (currentActiveTab === Tabs.MULTITENANCY.index) {
            fetchTenants();
        }
    }, palantirSettings().refreshInterval);
}

function showTenantDefinition(id) {
    $.get(`${actuatorEndpoints.multitenancy}/tenants/${id}`, response => {
        let tenantDefinitionDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("tenantDefinitionDialog"));
        const editor = initializeAceEditor("tenantDefinitionDialogEditor", "json");
        editor.setValue(JSON.stringify(response, null, 2));
        editor.gotoLine(1);
        editor.setReadOnly(true);
        tenantDefinitionDialog["open"]();
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayBanner(xhr);
    });
}
