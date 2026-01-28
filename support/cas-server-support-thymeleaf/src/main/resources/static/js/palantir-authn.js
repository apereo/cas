async function initializeAuthenticationOperations() {
    const authenticationHandlersTable = $("#authenticationHandlersTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0},
        ],
        order: [0, "asc"],
        drawCallback: settings => {
            $("#authenticationHandlersTable tr").addClass("mdc-data-table__row");
            $("#authenticationHandlersTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="3">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    authenticationHandlersTable.clear();
    if (CasActuatorEndpoints.authenticationHandlers()) {
        $.get(CasActuatorEndpoints.authenticationHandlers(), response => {
            for (const handler of response) {
                authenticationHandlersTable.row.add({
                    0: `${handler.name}`,
                    1: `<code>${handler.type}</code>`,
                    2: `<code>${handler.state}</code>`,
                    3: `<code>${handler.order}</code>`
                });
            }
            authenticationHandlersTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    const authenticationPoliciesTable = $("#authenticationPoliciesTable").DataTable({
        pageLength: 10,
        order: [0, "asc"],
        autoWidth: false,
        drawCallback: settings => {
            $("#authenticationPoliciesTable tr").addClass("mdc-data-table__row");
            $("#authenticationPoliciesTable td").addClass("mdc-data-table__cell");
        }
    });

    authenticationPoliciesTable.clear();
    if (CasActuatorEndpoints.authenticationPolicies()) {
        $.get(CasActuatorEndpoints.authenticationPolicies(), response => {
            for (const handler of response) {
                authenticationPoliciesTable.row.add({
                    0: `${handler.name}`,
                    1: `<code>${handler.order}</code>`
                });
            }
            authenticationPoliciesTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    const toolbar = document.createElement("div");
    let toolbarEntries = `
        <button type="button" id="loadExternalIdentityProvidersTableButton"
                onclick="loadExternalIdentityProvidersTable()"
                title="Reload external identity providers from sources"
                class="mdc-button mdc-button--raised">
            <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-refresh" aria-hidden="true"></i>Reload</span>
        </button>
    `;

    if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
        toolbarEntries += `
            <button type="button" id="newExternalIdentityProvider"
                    onclick="newExternalIdentityProvider()"
                    title="Create a new external identity provider"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>New</span>
            </button>
        `;
    }

    toolbar.innerHTML = toolbarEntries;
    $("#delegatedClientsTable").DataTable({
        pageLength: 10,
        order: [0, "asc"],
        autoWidth: false,
        layout: {
            topStart: toolbar
        },
        columnDefs: [
            {visible: false, targets: 0},
            {visible: false, targets: 3}
        ],
        drawCallback: settings => {
            $("#delegatedClientsTable tr").addClass("mdc-data-table__row");
            $("#delegatedClientsTable td").addClass("mdc-data-table__cell");
            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        let samlButtons = "";
                        let toolbarButtons = "";

                        rows.data().each(entry => {
                            if (entry[0] === group) {
                                if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
                                    toolbarButtons = `
                                        <span class="px-2" style="float: right;">
                                            <button type="button" 
                                                    name="removeIdentityProvider" 
                                                    href="#"
                                                    title="Remove Identity Provider"
                                                    onclick="removeIdentityProvider('${group}', '${entry[3]}')" 
                                                    data-client-name='${group}'
                                                    data-type='${entry[3]}'
                                                    class="mdc-button mdc-button--raised toolbar">
                                                <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                                            </button>
                                        </span>
                                    `.trim();
                                }

                                if (entry[3] === "saml2") {
                                    samlButtons = `
                                    <span class="px-2"  style="float: right;">
                                            <button type="button" title="Service Provider Metadata" 
                                                    title="View Service Provider Metadata"
                                                    name="saml2ClientSpMetadata" href="#" clientName='${group}'
                                                    class="mdc-button mdc-button--raised toolbar pr-2">
                                                <i class="mdi mdi-text-box min-width-32x" aria-hidden="true"></i>
                                                Service Provider Metadata
                                            </button>
                                            <button type="button" title="Identity Provider Metadata" 
                                                    title="View Identity Provider Metadata"
                                                    name="saml2ClientIdpMetadata" href="#" clientName='${group}'
                                                    class="mdc-button mdc-button--raised toolbar pr-2">
                                                <i class="mdi mdi-file-xml-box min-width-32x" aria-hidden="true"></i>
                                                Identity Provider Metadata
                                            </button>
                                    </span>
                                    `.trim();
                                }
                            }
                        });
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                <td colspan="2"><span class="idp-group">${group}</span>${toolbarButtons.trim()} ${samlButtons.trim()}</td>
                            </tr>`.trim()
                        );
                        configureSaml2ClientMetadataButtons();
                        last = group;
                    }
                });
        }
    });


    await loadExternalIdentityProvidersTable();
}
