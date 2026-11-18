function mfaProviderPropertiesMutable() {
    return PalantirDashboardConfiguration.mutablePropertySourcesAvailable() && CasActuatorEndpoints.casConfig();
}

/**
 * Move to the configuration tab's dynamic sources panel.
 *
 * The configuration tab is initialized on first activation, so this waits for that to finish before
 * selecting the inner panel: its handlers are not bound until then, and restoring the operator's
 * stored inner selection would otherwise run afterwards and undo this one.
 *
 * @returns {Promise<void>} resolved once the panel is selected
 */
async function switchToConfigurationDynamicSourcesTab() {
    activateDashboardTab(Tabs.CONFIGURATION.index);
    selectSidebarMenuTab(Tabs.CONFIGURATION.index);
    await initializeDashboardTab(Tabs.CONFIGURATION);
    $("#configuration-tabs").tabs("option", "active", $("#mutableConfigSources").index());
}

async function overrideMfaProviderConfigurationPropertyValue(propertyName, propertyValue) {
    if (mfaProviderPropertiesMutable()) {
        await switchToConfigurationDynamicSourcesTab();
        overrideConfigPropertyValue(propertyName, propertyValue);
    }
}

function overrideMfaProviderPropertyValue(button) {
    overrideMfaProviderConfigurationPropertyValue($(button).data("key"), $(button).data("value"));
}

async function showMfaProviderConfigurationPropertyHelp(propertyName) {
    if (CasActuatorEndpoints.configurationMetadata()) {
        await switchToConfigurationDynamicSourcesTab();
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
                            <div class="p-2 m-auto min-height-90 palantir-card-body">
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
                            const table = $(`#mfaTable-${key}`).DataTable();
                            properties.forEach(([propKey, propValue]) => {
                                if (propKey.startsWith(configPrefix)) {
                                    table.row.add({
                                        0: `${palantirCode(propKey)}`,
                                        1: `${palantirCode(propValue.value)}`,
                                        propertyName: propKey,
                                        propertyValue: propValue.value
                                    });
                                }
                            });
                            table.draw(false);
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

/**
 * The kind of a multifactor device, from the provider that registered it, for the badge of its card.
 *
 * @param {object} device the device
 * @returns {{key: string, icon: string}} the badge style and its icon
 */
function mfaDeviceKind(device) {
    const source = `${device.source ?? ""} ${device.type ?? ""}`.toLowerCase();
    const kinds = [["duo", "mdi-shield-check-outline"], ["google", "mdi-two-factor-authentication"],
        ["webauthn", "mdi-fingerprint"], ["fido", "mdi-fingerprint"], ["yubi", "mdi-usb-flash-drive-outline"],
        ["simple", "mdi-message-text-lock-outline"], ["sms", "mdi-message-text-lock-outline"], ["mobile", "mdi-cellphone-key"]];
    const kind = kinds.find(([match]) => source.includes(match));
    return {key: kind ? kind[0] : "other", icon: kind ? kind[1] : "mdi-key-chain-variant"};
}

/**
 * One registered multifactor device as a card: its name and provider, when it was last used and when it expires,
 * and what identifies it.
 *
 * @param {object} device the device as the endpoint answers it
 * @returns {string} the card markup
 */
function mfaDeviceCard(device) {
    const escape = escapePalantirMarkup;
    const kind = mfaDeviceKind(device);
    const id = String(device.id ?? "");
    const providerId = device?.details?.providerId ?? "Unknown";
    const name = device.name || id || "Unnamed device";
    const source = device.source || "Multifactor device";
    const lastUsed = palantirRecordDate(device.lastUsedDateTime);
    const expires = palantirRecordDate(device.expirationDateTime);
    const expired = expires !== null && expires.getTime() <= Date.now();
    const details = [["ID", id, "is-wrap"], ["Number", device.number, ""], ["Model", device.model, "is-text"],
        ["Last used", lastUsed ? palantirRecordWhen(lastUsed) : null, "is-text"]]
        .filter(([, value]) => value !== undefined && value !== null && String(value).trim() !== "");
    return `
        <article class="palantir-record-card is-mfa${expired ? " is-expired" : ""}" data-mfa-device="${escape(id)}"
                 data-mfa-provider="${escape(providerId)}" aria-label="${escape(name)}">
            <header class="palantir-record-card__head">
                <span class="palantir-protocol-badge is-mfa" title="${escape(source)}"><i class="mdi ${kind.icon}" aria-hidden="true"></i></span>
                <div class="palantir-record-card__title">
                    <h4 title="${escape(name)}">${escape(name)}</h4>
                    <p>${escape(source)}${device.type ? ` · ${escape(device.type)}` : ""}</p>
                </div>
            </header>
            <ul class="palantir-record-card__flags">
                <li class="${lastUsed ? "is-on" : ""}"><i class="mdi mdi-history" aria-hidden="true"></i>${lastUsed
        ? `Used ${escape(palantirRecordAgo(lastUsed))}` : "No recorded use"}</li>
                <li class="${expired ? "is-alert" : expires ? "is-on" : ""}"><i class="mdi ${expired ? "mdi-alert-circle-outline" : "mdi-calendar-clock"}" aria-hidden="true"></i>${expired
        ? "Expired" : expires ? `Expires ${escape(palantirRecordWhen(expires))}` : "Does not expire"}</li>
            </ul>
            ${details.length > 0 ? `<dl class="palantir-record-card__details">${details.map(([label, value, style]) => `
                <div><dt>${label}</dt><dd class="${style}" title="${escape(value)}">${escape(value)}</dd></div>`).join("")}</dl>` : ""}
            <footer class="palantir-record-card__actions">
                <button type="button" class="palantir-button" data-mfa-action="copy"${id === "" ? " disabled" : ""}>
                    <i class="mdi mdi-content-copy" aria-hidden="true"></i>Copy ID
                </button>
                <button type="button" class="palantir-icon-button palantir-record-card__delete" data-mfa-action="remove"
                        data-palantir-write="mfaDevices" aria-label="Remove ${escape(name)}" title="Remove device">
                    <i class="mdi mdi-delete-outline" aria-hidden="true"></i>
                </button>
            </footer>
        </article>`;
}

async function initializeMultifactorOperations() {
    const mfaDevicesTable = $("#mfaDevicesTable").DataTable({
        pageLength: 10,
        autoWidth: true,
        columnDefs: [
            {visible: false, targets: 8}
        ],
        drawCallback: function () {
            $("#mfaDevicesTable tr").addClass("mdc-data-table__row");
            $("#mfaDevicesTable td").addClass("mdc-data-table__cell");
            renderPalantirRecordCards(this.api(), "mfaDevicesCards", mfaDeviceCard,
                username => `No multifactor devices are registered for ${username}.`);
        }
    });
    placePalantirRecordCards("mfaDevicesTable", "mfaDevicesCards", "Search for a username to see their devices.");

    function removeMfaDevice({key, providerId, username}) {
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
                        url: `${CasActuatorEndpoints.mfaDevices()}/${encodeURIComponent(username)}/${encodeURIComponent(providerId)}/${encodeURIComponent(key)}`,
                        type: "DELETE",
                        contentType: "application/x-www-form-urlencoded",
                        success: () => mfaDevicesTable
                            .rows((index, data) => data.key === key && data.providerId === providerId)
                            .remove()
                            .draw(),
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
            remove: {name: "Remove MFA Device", write: "mfaDevices", icon: contextMenuIcon("mdi-delete")}
        },
        callback: (key, context) => {
            if (key === "remove") {
                removeMfaDevice(context.rowData);
            }
        }
    });

    $("#mfaDevicesCards").off("click").on("click", "[data-mfa-action]", event => {
        const button = event.currentTarget;
        const card = button.closest("[data-mfa-device]");
        const row = mfaDevicesTable.rows((index, data) => String(data.key) === card.dataset.mfaDevice
            && data.providerId === card.dataset.mfaProvider).data().toArray()[0];
        if (!row) {
            return;
        }
        if (button.dataset.mfaAction === "copy") {
            copyToClipboard(String(row.key));
        } else if (button.dataset.mfaAction === "remove") {
            removeMfaDevice(row);
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
            $.get(`${CasActuatorEndpoints.mfaDevices()}/${encodeURIComponent(username)}`, response => {
                for (const device of Object.values(response)) {
                    mfaDevicesTable.row.add({
                        0: `${palantirCode(device.name ?? "N/A")}`,
                        1: `${palantirCode(device.type ?? "N/A")}`,
                        2: `${palantirCode(device.id ?? "N/A")}`,
                        3: `${palantirCode(device.number ?? "N/A")}`,
                        4: `${palantirCode(device.model ?? "N/A")}`,
                        5: `${palantirCode(device.lastUsedDateTime ?? "N/A")}`,
                        6: `${palantirCode(device.expirationDateTime ?? "N/A")}`,
                        7: `${palantirCode(device.source ?? "N/A")}`,
                        8: palantirCode(device.payload ?? ""),
                        providerId: device?.details?.providerId ?? "Unknown",
                        key: device.id,
                        username: username,
                        record: device
                    });
                }
                $("#mfaDevicesCards").data("searched", username);
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
            remove: {name: "Remove Trusted MFA Device", write: "multifactorTrustedDevices", icon: contextMenuIcon("mdi-delete")}
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
                        0: `${palantirCode(device.id ?? "N/A")}`,
                        1: `${palantirCode(device.principal ?? "N/A")}`,
                        2: `${palantirCode(device.deviceFingerprint ?? "N/A")}`,
                        3: `${palantirCode(device.recordDate ?? "N/A")}`,
                        4: `${palantirCode(device.name ?? "N/A")}`,
                        5: `${palantirCode(device.expirationDate ?? "N/A")}`,
                        6: `${palantirCode(device.multifactorAuthenticationProvider ?? "N/A")}`,
                        7: `${palantirCode(device.recordKey ?? "N/A")}`,
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
