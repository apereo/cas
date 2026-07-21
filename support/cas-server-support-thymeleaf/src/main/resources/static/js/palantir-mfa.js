function mfaProviderPropertiesMutable() {
    return PalantirDashboardConfiguration.mutablePropertySourcesAvailable() && CasActuatorEndpoints.casConfig();
}

function escapeMfaHtml(str) {
    return String(str ?? "").replace(/[&<>"']/g, s => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[s]));
}

function switchToConfigurationDynamicSourcesTab() {
    activateDashboardTab(Tabs.CONFIGURATION.index);
    selectSidebarMenuTab(Tabs.CONFIGURATION.index);
    $("#configuration-tabs").tabs("option", "active", $("#mutableConfigSources").index());
}

function overrideMfaProviderConfigurationPropertyValue(propertyName, propertyValue) {
    if (mfaProviderPropertiesMutable()) {
        switchToConfigurationDynamicSourcesTab();
        overrideConfigPropertyValue(propertyName, propertyValue);
    }
}

function overrideMfaProviderPropertyValue(button) {
    overrideMfaProviderConfigurationPropertyValue($(button).data("key"), $(button).data("value"));
}

function showMfaProviderConfigurationPropertyHelp(propertyName) {
    if (CasActuatorEndpoints.configurationMetadata()) {
        switchToConfigurationDynamicSourcesTab();
        searchForConfigPropertyButton(propertyName);
    }
}

async function populateMultifactorProviderTables() {
    if (CasActuatorEndpoints.discoveryProfile()) {
        CasDiscoveryProfile.fetchIfNeeded()
            .done(async () => {
                showElements($("#mfaProvidersTab").parent());
                const mutableProperties = mfaProviderPropertiesMutable();

                for (const [key, value] of Object.entries(CasDiscoveryProfile.multifactorAuthenticationProviders())) {
                    let icon = "mdi-two-factor-authentication";
                    if (key.includes("gauth")) {
                        icon = "mdi-google";
                    } else if (key.includes("web")) {
                        icon = "mdi-fingerprint";
                    }

                    $("#mfaProvidersGridPanel").append(`
                        <div class="min-height-90 mb-4">
                            <div class="mdc-card p-4 m-auto gradient-card">
                                <h3>
                                    <i class="p-1 mdc-tab__icon mdi ${icon}" style="vertical-align:baseline;" aria-hidden="true"></i>
                                    ${value}
                                    <sup class="mr-2">${key}</sup>
                                </h3>
                            </div>
                            <div class="p-2 m-auto min-height-90" style="background: white;">
                                <table id="mfaTable-${key}" class="mdc-data-table__table table table-striped noborder mfa-provider-table">
                                    <thead>
                                    <tr class="mdc-data-table__header-row">
                                        <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Property</th>
                                        <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Description</th>
                                    </tr>
                                    </thead>
                                    <tbody class="mdc-data-table__content">
                                    
                                    </tbody>
                                </table>
                            </div>

                        </div>
                    `);
                }
                $(".mfa-provider-table").DataTable({
                    searching: false,
                    lengthChange: false,
                    columnDefs: [
                        {
                            targets: 1,
                            className: "dt-left wrap"
                        }
                    ],
                    drawCallback(settings) {
                        const node = new $.fn.dataTable.Api(settings).table().node();
                        $(node).find("tr").addClass("mdc-data-table__row");
                        $(node).find("td").addClass("mdc-data-table__cell");
                    }
                }).clear();
                $(".mfa-provider-table").each(function () {
                    const table = $(this).DataTable();
                    initializeDataTableContextMenu({
                        table: table,
                        selector: `#${$.escapeSelector(this.id)} tbody tr`,
                        items: {
                            help: {
                                name: "Configuration Property Help",
                                icon: contextMenuIcon("mdi-help"),
                                visible: () => CasActuatorEndpoints.configurationMetadata()
                            },
                            override: {
                                name: "Override Configuration Property Value",
                                icon: contextMenuIcon("mdi-arrow-left-circle"),
                                visible: () => mutableProperties
                            }
                        },
                        callback: (key, context) => {
                            if (key === "help") {
                                showMfaProviderConfigurationPropertyHelp(context.rowData.propertyName);
                            } else if (key === "override") {
                                overrideMfaProviderConfigurationPropertyValue(context.rowData.propertyName, context.rowData.propertyValue);
                            }
                        }
                    });
                });

                for (const key of Object.keys(CasDiscoveryProfile.multifactorAuthenticationProviders())) {
                    const namespace = key.includes("duo") ? "duo" : key.replace("mfa-", "")
                        .replace("webauthn", "web-authn");
                    const configPrefix = `cas.authn.mfa.${namespace}`;
                    $.get(`${CasActuatorEndpoints.env()}?pattern=${configPrefix}`, response => {
                        response.propertySources.forEach(source => {
                            let properties = source.properties && Object.entries(source.properties || {});

                            if (key.includes("duo") && properties.length > 0) {
                                const entry = properties.find(([propKey, propValue]) => {
                                    const providerName = CasDiscoveryProfile.multifactorAuthenticationProviders()[propKey];
                                    return (propKey.endsWith(".id") && propValue.value === key)
                                        || (propKey.endsWith(".name") && propValue.value === providerName);
                                });
                                let prefix = `${configPrefix}[0]`;
                                if (entry) {
                                    prefix = entry[0].replace(".id", "").replace(".name", "");
                                }
                                properties = properties.filter(([propKey, propValue]) => propKey.startsWith(prefix));
                            }
                            properties.forEach(([propKey, propValue]) => {
                                if (propKey.startsWith(configPrefix)) {
                                    const table = $(`#mfaTable-${key}`).DataTable();
                                    table.row.add({
                                        0: `<code>${escapeMfaHtml(propKey)}</code>`,
                                        1: `<code>${escapeMfaHtml(propValue.value)}</code>`,
                                        propertyName: propKey,
                                        propertyValue: propValue.value
                                    }).draw(false);
                                }
                            });
                        });

                        updateNavigationSidebar();
                    })
                        .fail((xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                        });
                }

            });
    } else {
        hideElements($("#mfaProvidersTab").parent());
    }
}

async function initializeMultifactorOperations() {
    const mfaDevicesTable = $("#mfaDevicesTable").DataTable({
        pageLength: 10,
        autoWidth: true,
        columnDefs: [
            {visible: false, targets: 8}
        ],
        drawCallback: settings => {
            $("#mfaDevicesTable tr").addClass("mdc-data-table__row");
            $("#mfaDevicesTable td").addClass("mdc-data-table__cell");
        }
    });

    function removeMfaDevice(context) {
        const {key, providerId, username} = context.rowData;
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
                        url: `${CasActuatorEndpoints.mfaDevices()}/${username}/${providerId}/${key}`,
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
        table: mfaDevicesTable,
        selector: "#mfaDevicesTable tbody tr",
        items: {
            remove: {name: "Remove MFA Device", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            if (key === "remove") {
                removeMfaDevice(context);
            }
        }
    });

    $("#mfaDevicesButton").off().on("click", () => {
        function fetchMfaDevices() {
            const username = $("#mfaUsername").val();
            $("#mfaDevicesButton").prop("disabled", true);
            Swal.fire({
                icon: "info",
                title: `Fetching Devices for ${username}`,
                text: "Please wait while registered multifactor devices are retrieved...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });

            mfaDevicesTable.clear();
            $.get(`${CasActuatorEndpoints.mfaDevices()}/${username}`, response => {
                for (const device of Object.values(response)) {
                    mfaDevicesTable.row.add({
                        0: `<code>${device.name ?? "N/A"}</code>`,
                        1: `<code>${device.type ?? "N/A"}</code>`,
                        2: `<code>${device.id ?? "N/A"}</code>`,
                        3: `<code>${device.number ?? "N/A"}</code>`,
                        4: `<code>${device.model ?? "N/A"}</code>`,
                        5: `<code>${device.lastUsedDateTime ?? "N/A"}</code>`,
                        6: `<code>${device.expirationDateTime ?? "N/A"}</code>`,
                        7: `<code>${device.source ?? "N/A"}</code>`,
                        8: `<span>${device.payload}</span>`,
                        providerId: device?.details?.providerId ?? "Unknown",
                        key: device.id,
                        username: username
                    });
                }
                mfaDevicesTable.draw();
                $("#mfaDevicesButton").prop("disabled", false);
                Swal.close();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                $("#mfaDevicesButton").prop("disabled", false);
                Swal.close();
            });
        }

        const fmMfaDevices = document.getElementById("fmMfaDevices");
        if (!fmMfaDevices.checkValidity()) {
            fmMfaDevices.reportValidity();
            return false;
        }

        fetchMfaDevices();
    });
    await populateMultifactorProviderTables();
}

async function initializeTrustedMultifactorOperations() {
    const mfaTrustedDevicesTable = $("#mfaTrustedDevicesTable").DataTable({
        pageLength: 10,
        autoWidth: true,
        columnDefs: [
            {visible: false, targets: 7}
        ],
        drawCallback: settings => {
            $("#mfaTrustedDevicesTable tr").addClass("mdc-data-table__row");
            $("#mfaTrustedDevicesTable td").addClass("mdc-data-table__cell");
        }
    });

    function removeMfaTrustedDevice(context) {
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
                        url: `${CasActuatorEndpoints.multifactorTrustedDevices()}/${key}`,
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
        table: mfaTrustedDevicesTable,
        selector: "#mfaTrustedDevicesTable tbody tr",
        items: {
            remove: {name: "Remove Trusted MFA Device", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            if (key === "remove") {
                removeMfaTrustedDevice(context);
            }
        }
    });

    $("#mfaTrustedDevicesButton").off().on("click", () => {
        if (CasActuatorEndpoints.multifactorTrustedDevices()) {
            mfaTrustedDevicesTable.clear();
            const username = $("#mfaTrustedUsername").val();
            $("#mfaTrustedDevicesButton").prop("disabled", true);
            Swal.fire({
                icon: "info",
                title: `Fetching Multifactor Trusted Devices for ${username}`,
                text: "Please wait while registered multifactor trusted devices are retrieved...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });

            $.get(`${CasActuatorEndpoints.multifactorTrustedDevices()}/${username}`, response => {
                for (const device of Object.values(response)) {
                    mfaTrustedDevicesTable.row.add({
                        0: `<code>${device.id ?? "N/A"}</code>`,
                        1: `<code>${device.principal ?? "N/A"}</code>`,
                        2: `<code>${device.deviceFingerprint ?? "N/A"}</code>`,
                        3: `<code>${device.recordDate ?? "N/A"}</code>`,
                        4: `<code>${device.name ?? "N/A"}</code>`,
                        5: `<code>${device.expirationDate ?? "N/A"}</code>`,
                        6: `<code>${device.multifactorAuthenticationProvider ?? "N/A"}</code>`,
                        7: `<code>${device.recordKey ?? "N/A"}</code>`,
                        key: device.recordKey
                    });
                }
                mfaTrustedDevicesTable.draw();
                $("#mfaTrustedDevicesButton").prop("disabled", false);
                Swal.close();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                $("#mfaTrustedDevicesButton").prop("disabled", false);
                Swal.close();
            });
        }
    });

}
