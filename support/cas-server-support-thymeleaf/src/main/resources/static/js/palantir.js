/**
 * Internal Functions
 */
const Tabs = {
    APPLICATIONS: 0,
    SYSTEM: 1,
    TICKETS: 2,
    TASKS: 3,
    ACCESS_STRATEGY: 4,
    LOGGING: 5,
    SSO_SESSIONS: 6,
    CONFIGURATION: 7,
    PERSON_DIRECTORY: 8,
    AUTHENTICATION: 9,
    CONSENT: 10,
    PROTOCOLS: 11,
    THROTTLES: 12,
    MFA: 13,
    MULTITENANCY: 14,
    SETTINGS: 100
};

/**
 * Charts objects.
 */
let servicesChart = null;
let memoryChart = null;
let statisticsChart = null;
let systemHealthChart = null;
let jvmThreadsChart = null;
let httpRequestResponsesChart = null;
let httpRequestsByUrlChart = null;
let auditEventsChart = null;
let threadDumpChart = null;

let currentActiveTab = Tabs.APPLICATIONS;

const CAS_FEATURES = [];

let notyf = null;

class PalantirSettings {
    constructor(data = {}) {
        const parsedInterval = Number(data.refreshInterval);
        this.refreshInterval = Number.isFinite(parsedInterval)
            ? parsedInterval * 1000
            : 10 * 1000;
    }
}

function palantirSettings() {
    try {
        const raw = JSON.parse(localStorage.getItem("PalantirSettings")) ?? {};
        return new PalantirSettings(raw);
    } catch {
        return new PalantirSettings();
    }
}

function newExternalIdentityProvider() {
    if (mutablePropertySourcesAvailable && actuatorEndpoints.casconfig) {
        const dialogContainer = $("<div>", {
            id: "newExternalIdentityProviderDialog"
        });

        const availableProviders = [];
        if (CAS_FEATURES.includes("DelegatedAuthentication.cas")) {
            availableProviders.push({
                value: "CAS",
                text: "CAS Server"
            });
        }
        if (CAS_FEATURES.includes("DelegatedAuthentication.oidc")) {
            availableProviders.push({
                value: "OIDC",
                text: "OpenID Connect Provider"
            });
            availableProviders.push({
                value: "OAUTH2",
                text: "OAuth2 Identity Provider"
            });
        }
        if (CAS_FEATURES.includes("DelegatedAuthentication.saml")) {
            availableProviders.push({
                value: "SAML2",
                text: "SAML2 Identity Provider"
            });
        }

        createSelectField({
            containerId: dialogContainer,
            labelTitle: "Identity Provider Type:",
            id: "externalIdPTypeSelect",
            options: availableProviders,
            cssClasses: "always-show"
        });
        

        createInputField({
            labelTitle: "Login URL",
            name: "externalIdentityProviderCasLoginUrl",
            required: true,
            containerId: dialogContainer,
            title: "Define the CAS server's login URL for redirecting authentication requests.",
            cssClasses: "CAS hide",
            paramName: "login-url"
        });
        createSelectField({
            containerId: dialogContainer,
            labelTitle: "Protocol:",
            id: "externalIdentityProviderCasProtocol",
            options: [
                {value: "CAS30", text: "CAS 3.0"},
                {value: "CAS20", text: "CAS 2.0"},
                {value: "CAS10", text: "CAS 1.0"},
                {value: "SAML", text: "SAML"}
            ],
            cssClasses: "CAS hide",
            paramName: "protocol"
        });

        createInputField({
            labelTitle: "Client ID",
            name: "externalIdentityProviderOAuthOidcClientId",
            required: true,
            containerId: dialogContainer,
            title: "Define the Client ID provided by the OAuth2/OIDC identity provider.",
            cssClasses: "OAUTH2 OIDC hide",
            paramName: "id"
        });
        createInputField({
            labelTitle: "Client Secret",
            name: "externalIdentityProviderOAuthOidcClientSecret",
            required: true,
            containerId: dialogContainer,
            title: "Define the Client secret provided by the OAuth2/OIDC identity provider.",
            cssClasses: "OAUTH2 OIDC hide",
            paramName: "secret"
        });
        createInputField({
            labelTitle: "Authorize URL",
            name: "externalIdentityProviderOAuthAuthorizeUrl",
            required: true,
            containerId: dialogContainer,
            title: "Define the authorization URL of the OAuth2 identity provider.",
            cssClasses: "OAUTH2 hide",
            paramName: "auth-url"
        });
        createInputField({
            labelTitle: "Token URL",
            name: "externalIdentityProviderOAuthTokenUrl",
            required: true,
            containerId: dialogContainer,
            title: "Define the token URL of the OAuth2 identity provider.",
            cssClasses: "OAUTH2 hide",
            paramName: "token-url"
        });
        createInputField({
            labelTitle: "Profile URL",
            name: "externalIdentityProviderOAuthProfileUrl",
            required: true,
            containerId: dialogContainer,
            title: "Define the profile URL of the OAuth2 identity provider.",
            cssClasses: "OAUTH2 hide",
            paramName: "profile-url"
        });
        createInputField({
            labelTitle: "Discovery URL",
            name: "externalIdentityProviderOidcDiscoveryUrl",
            required: true,
            containerId: dialogContainer,
            title: "Define the discovery URL of the OIDC identity provider.",
            cssClasses: "OIDC hide",
            paramName: "discovery-uri"
        });
        createInputField({
            labelTitle: "Scope",
            name: "externalIdentityProviderOAuthScope",
            required: true,
            containerId: dialogContainer,
            title: "Define the scope of the OAuth2 identity provider.",
            cssClasses: "OAUTH2 OIDC hide",
            paramName: "scope"
        });

        createInputField({
            labelTitle: "Keystore Password",
            name: "externalIdentityProviderSaml2KeystorePassword",
            required: true,
            containerId: dialogContainer,
            title: "Define the keystore password for the SAML2 identity provider.",
            cssClasses: "SAML2 hide",
            paramName: "keystore-password"
        });
        createInputField({
            labelTitle: "Private Key Password",
            name: "externalIdentityProviderSaml2PrivateKeyPassword",
            required: true,
            containerId: dialogContainer,
            title: "Define the private key password for the SAML2 identity provider.",
            cssClasses: "SAML2 hide",
            paramName: "private-key-password"
        });
        createInputField({
            labelTitle: "Keystore Path",
            name: "externalIdentityProviderSaml2KeystorePath",
            required: true,
            containerId: dialogContainer,
            title: "Define the keystore path for the SAML2 identity provider.",
            cssClasses: "SAML2 hide",
            paramName: "keystore-path"
        });
        createInputField({
            labelTitle: "Service Provider Entity ID",
            name: "externalIdentityProviderSaml2SpEntityId",
            required: true,
            containerId: dialogContainer,
            title: "Define the service provider entity ID for the SAML2 identity provider.",
            cssClasses: "SAML2 hide",
            paramName: "service-provider-entity-id"
        });
        createInputField({
            labelTitle: "Service Provider Metadata Location",
            name: "externalIdentityProviderSaml2SpMetadataLocation",
            required: true,
            containerId: dialogContainer,
            title: "Define the service provider metadata location for the SAML2 identity provider.",
            cssClasses: "SAML2 hide",
            paramName: "metadata.service-provider.file-system.location"
        });
        createInputField({
            labelTitle: "Identity Provider Metadata Location",
            name: "externalIdentityProviderSaml2IdpMetadataLocation",
            required: true,
            containerId: dialogContainer,
            title: "Define the identity provider metadata location for the SAML2 identity provider.",
            cssClasses: "SAML2 hide",
            paramName: "metadata.identity-provider-metadata-path"
        });
        createInputField({
            labelTitle: "Destination Binding",
            name: "externalIdentityProviderSaml2DestinationBinding",
            required: true,
            containerId: dialogContainer,
            title: "Define the destination binding for the SAML2 identity provider.",
            cssClasses: "SAML2 hide",
            paramName: "destination-binding"
        });

        createInputField({
            labelTitle: "Client Name",
            name: "externalIdentityProviderClientName",
            required: false,
            containerId: dialogContainer,
            title: "Define a unique name for the external identity provider client.",
            cssClasses: "always-show",
            paramName: "client-name"
        });
        createInputField({
            labelTitle: "Display Name",
            name: "externalIdentityProviderDisplayName",
            required: false,
            containerId: dialogContainer,
            title: "Define the display name for the external identity provider.",
            cssClasses: "always-show",
            paramName: "display-name"
        });
        createInputField({
            labelTitle: "Principal ID Attribute",
            name: "externalIdentityProviderPrincipalIdAttribute",
            required: false,
            containerId: dialogContainer,
            title: "Define the attribute that contains the principal ID from the identity provider.",
            cssClasses: "always-show",
            paramName: "principal-id-attribute"
        });
        createSelectField({
            containerId: dialogContainer,
            labelTitle: "Auto Redirect:",
            id: "externalIdentityProviderAutoRedirectType",
            options: [
                {value: "NONE", text: "NONE"},
                {value: "CLIENT", text: "CLIENT"},
                {value: "SERVER", text: "SERVER"}
            ],
            cssClasses: "always-show",
            paramName: "auto-redirect-type"
        });

        function handleExternalIdentityProviderTypeChange(type) {
            $(`#newExternalIdentityProviderDialog .${type}`).show();
            $("#newExternalIdentityProviderDialog [id$='SelectContainer']")
                .not(`.${type}`)
                .not(".always-show")
                .hide();
            $("#newExternalIdentityProviderDialog [id$='FieldContainer']")
                .not(`.${type}`)
                .not(".always-show")
                .hide();
            $("#newExternalIdentityProviderDialog [id$='ButtonPanel']")
                .not(`.${type}`)
                .not(".always-show")
                .hide();
            $("#newExternalIdentityProviderDialog [id$='FieldContainer'] input")
                .val("");
        }
        
        
        dialogContainer.dialog({
            position: {
                my: "center top",
                at: "center top+100",
                of: window
            },
            autoOpen: false,
            modal: true,
            width: 600,
            height: "auto",
            title: "New External Identity Provider",
            buttons: {
                OK: async function () {
                    let proceed = true;
                    const inputs = $("#newExternalIdentityProviderDialog input:visible").toArray();
                    for (const input of inputs) {
                        if (!input.checkValidity()) {
                            input.reportValidity?.();
                            input.focus();
                            proceed = false;
                            break;
                        }
                    }

                    if (proceed) {
                        $(this).dialog("close");
                        $.get(actuatorEndpoints.env, res => {
                            reloadConfigurationTable(res);
                            refreshCasServerConfiguration(`Identity Provider Created`);
                        })
                            .fail((xhr) => {
                                displayBanner(xhr);
                            });
                    }
                },
                Cancel: function () {
                    $(this).dialog("close");
                }
            },
            open: function () {
                cas.init();
                $("#newExternalIdentityProviderDialog #externalIdentityProviderClientName").focus();
                $("#newExternalIdentityProviderDialog .jqueryui-selectmenu").selectmenu({
                    width: "330px",
                    change: function (event, ui) {
                        const type = ui.item.value;
                        handleExternalIdentityProviderTypeChange(type);
                    }
                });
                const currentType = $("#externalIdPTypeSelect").val();
                handleExternalIdentityProviderTypeChange(currentType);
            },
            close: function () {
                $(this).dialog("destroy");
            }
        });
        dialogContainer.dialog("open");
    }
}

function effectiveConfigPropertyValue(name) {
    $.get(`${actuatorEndpoints.env}/${name}`, response => {
        Swal.fire({
            title: "Effective Property Value",
            html: `
                The effective configuration value for <code>${name}</code> is:<p/>
                <pre><code class="border-rounded language-html">${name}=${response.property.value}</code></pre>
            `,
            icon: "info",
            width: "50%",
            showDenyButton: true,
            confirmButtonText: "OK",
            denyButtonText: "Copy",
            didOpen: () => {
                highlightElements();
                Swal.getDenyButton().addEventListener("click", async () => {
                    const text = `${name}=${response.property.value}`;
                    copyToClipboard(text);
                    setTimeout(() => Swal.resetValidationMessage(), 100);
                });
            }
        });
    })
        .fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
}

function deleteConfigPropertyValue(button, name) {
    if (mutablePropertySourcesAvailable && actuatorEndpoints.casconfig) {
        const mutableConfigurationTable = $("#mutableConfigurationTable").DataTable();
        const currentRow = mutableConfigurationTable.row($(button).closest("tr"));
        const propertySource = $(button).data("source").replace("bootstrapProperties-", "");
        Swal.fire({
            title: `Are you sure you want to delete this entry from ${propertySource}?`,
            text: "Once removed, you may not be able to revert this.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `${actuatorEndpoints.casconfig}`,
                        method: "DELETE",
                        contentType: "application/json",
                        data: JSON.stringify(
                            {
                                name: name,
                                propertySource: propertySource
                            }
                        )
                    })
                        .done(function (data, textStatus, jqXHR) {
                            const sources = data.join(",");
                            console.log("Removed property from configuration sources", sources);
                            currentRow.remove().draw(false);
                            refreshCasServerConfiguration(`${sources}: Property ${name} Removed`);
                        })
                        .fail(function (jqXHR, textStatus, errorThrown) {
                            console.error("Error:", textStatus, errorThrown);
                            displayBanner(jqXHR);
                        });
                }
            });
    }
}

function updateConfigPropertyValue(button, name) {
    if (mutablePropertySourcesAvailable && actuatorEndpoints.casconfig) {
        const mutableConfigurationTable = $("#mutableConfigurationTable").DataTable();
        const currentRow = mutableConfigurationTable.row($(button).closest("tr"));
        const propertySource = $(button).data("source").replace("bootstrapProperties-", "");
        const rowData = currentRow.data();

        openNewConfigurationPropertyDialog({
            name: name,
            value: $(rowData[2]).text(),
            propertySource: propertySource,
            updateEntry: true
        });
    }
}

function deleteAllConfigurationProperties(button) {
    function deletePropertiesFromSource(propertySource) {
        Swal.close();
        $.ajax({
            url: `${actuatorEndpoints.casconfig}`,
            method: "DELETE",
            contentType: "application/json",
            data: JSON.stringify(
                {
                    propertySource: propertySource
                }
            )
        })
            .done(function (data, textStatus, jqXHR) {
                $.get(actuatorEndpoints.env, res => {
                    reloadConfigurationTable(res);
                    refreshCasServerConfiguration(`${propertySource}: All Properties Removed`);
                })
                    .fail((xhr) => {
                        displayBanner(xhr);
                    });
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.error("Error:", textStatus, errorThrown);
                displayBanner(jqXHR);
            });
    }

    if (mutablePropertySources.length === 1) {
        deletePropertiesFromSource(mutablePropertySources[0]);
    } else {
        Swal.fire({
            title: "Which property source do you want to clear?",
            input: "select",
            icon: "question",
            inputOptions: mutablePropertySources,
            inputPlaceholder: "Choose a property source...",
            showCancelButton: true
        }).then((result) => {
            if (result.isConfirmed) {
                deletePropertiesFromSource(result.value);
            }
        });
    }
}

function importConfigurationProperties(button) {
    function parseProperties(text) {
        const result = {};
        text.split(/\r?\n/).forEach(line => {
            line = line.trim();
            if (!line || line.startsWith("#") || line.startsWith("!")) {
                return;
            }

            const idx = line.indexOf("=");
            if (idx === -1) {
                return;
            }
            const key = line.substring(0, idx).trim();
            result[key] = line.substring(idx + 1).trim();
        });
        return result;
    }

    function importProperties(payload) {
        $.ajax({
            url: `${actuatorEndpoints.casconfig}/update`,
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(payload),
            success: response => {
                Swal.close();
                $.get(actuatorEndpoints.env, res => {
                    reloadConfigurationTable(res);
                    refreshCasServerConfiguration(`New Property ${name} Created`);
                })
                    .fail((xhr) => {
                        displayBanner(xhr);
                    });
            },
            error: (xhr, status, error) => {
                Swal.close();
                console.error(`Error: ${status} / ${error} / ${xhr.responseText}`);
                displayBanner(xhr);
            }
        });
    }

    Swal.fire({
        title: "Are you sure you want to import configuration properties?",
        input: "select",
        inputOptions: mutablePropertySources,
        html: `
            <p class="text-justify">Properties will be imported and applied to the CAS server configuration. You may import multiple files at once.
            Supported file formats are .properties and .yml/.yaml files. You may choose the target property source to which the properties will be applied.</p>
        `,
        icon: "question",
        showConfirmButton: true,
        showDenyButton: true
    }).then((result) => {
        if (result.isConfirmed) {
            const propertySource = mutablePropertySources[Number(result.value)];

            $("#configurationFilesToImport").click();
            $("#configurationFilesToImport").change(event => {
                const files = Array.from(event.target.files);
                files.forEach(file => {
                    const reader = new FileReader();
                    reader.readAsText(file);
                    reader.onload = e => {
                        const content = e.target.result;
                        const ext = file.name.split(".").pop().toLowerCase();

                        Swal.fire({
                            icon: "info",
                            title: `${propertySource}: Importing Properties`,
                            text: "Please wait while the configuration properties are being imported...",
                            allowOutsideClick: false,
                            showConfirmButton: false,
                            didOpen: () => Swal.showLoading()
                        });
                        if (ext === "properties") {
                            const props = parseProperties(content);
                            const payload = Object.entries(props).map(([key, value]) => ({
                                name: key,
                                value
                            }));
                            importProperties(payload);
                        } else if (ext === "yml" || ext === "yaml") {
                            const props = flattenJSON(jsyaml.load(content));
                            const payload = Object.entries(props).map(([key, value]) => ({
                                name: key,
                                value
                            }));
                            importProperties(payload);
                        }
                    };
                });
                $("#configurationFilesToImport").val("");
            });
        }
    });

}

async function encryptConfigProperty(value) {
    try {
        return await $.ajax({
            method: "POST",
            url: `${actuatorEndpoints.casconfig}/encrypt`,
            data: value,
            contentType: "text/plain; charset=utf-8",
            dataType: "text"
        });
    } catch (xhr) {
        displayBanner(xhr);
        throw xhr;
    }
}

function createNewConfigurationProperty(button) {
    openNewConfigurationPropertyDialog({
        name: "",
        propertySource: "",
        updateEntry: false
    });

}

function openNewConfigurationPropertyDialog(config) {
    $("#newConfigurationDialog").dialog({
        autoOpen: false,
        modal: true,
        width: 600,
        height: "auto",
        buttons: {
            OK: async function () {
                if (!$("#newConfigurationForm")[0].reportValidity()) {
                    return;
                }

                const name = $("#newConfigPropertyName").val();
                let value = $("#newConfigPropertyValue").val();
                const encrypt = $("#encryptConfigProperty").val();

                if (encrypt && encrypt === "true" && actuatorEndpoints.casconfig) {
                    value = await encryptConfigProperty(value);
                }

                const sources = $("#propertySourcesSelect").val();
                console.log(`Creating new property ${name} with value ${value} in source ${sources}`);

                let payload;
                if (sources.length === 0) {
                    payload = [{name: name, value: value}];
                } else {
                    payload = sources.map(entry => ({
                        name: name,
                        value: value,
                        propertySource: entry
                    }));
                }

                $.ajax({
                    url: `${actuatorEndpoints.casconfig}/update`,
                    method: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(payload),
                    success: response => {
                        $(this).dialog("close");
                        $.get(actuatorEndpoints.env, res => {
                            reloadConfigurationTable(res);
                            refreshCasServerConfiguration(`New Property ${name} Created`);
                        })
                            .fail((xhr) => {
                                displayBanner(xhr);
                            });
                    },
                    error: (xhr, status, error) => {
                        console.error(`Error: ${status} / ${error} / ${xhr.responseText}`);
                        displayBanner(xhr);
                    }
                });
            },
            Cancel: function () {
                $(this).dialog("close");
            }
        },
        open: function () {
            $(this).css("overflow", "visible");

            $("#newConfigPropertyName").val(config.name ?? "");
            $("#newConfigPropertyValue").val(config.value ?? "");
            $("#propertySourcesSelect").val(config.propertySource ?? "");
            if (config.updateEntry) {
                $("#newConfigPropertyName").parent().hide();
                $("#propertySourcesSection").hide();
                $("#newConfigPropertyValue").focus();
            } else {
                $("#propertySourcesSection").show();
                $("#newConfigPropertyName").parent().show();
                $("#newConfigPropertyName").focus();
            }
        }
    });
    $("#newConfigurationDialog").dialog("open");
}

function refreshCasServerConfiguration(title) {
    if (actuatorEndpoints.refresh || actuatorEndpoints.busrefresh) {
        Swal.fire({
            showConfirmButton: true,
            showCancelButton: true,
            icon: "question",
            title: title,
            width: "35%",
            confirmButtonText: "Refresh",
            html: `Do you want to refresh the CAS runtime context to apply the changes?
            <p class="text-justify"/>CAS will rebind components to external configuration properties <strong>internally without a restart</strong>, 
            allowing components to be refreshed to reflect the changes. In-flight requests and operations keep running normally
            using the existing/old CAS components until they are fully refreshed in the runtime application context.
            <p class="text-justify"/>
            <strong>Note: Not every component is refreshable.</strong> Configuration properties that control
            fundamental aspects of CAS operation may not be dynamically reloaded. In such cases, a full restart of the CAS server
            may be required for the changes to take effect.
            `
        })
            .then((reloadResult) => {
                if (reloadResult.isConfirmed) {
                    Swal.close();
                    Swal.fire({
                        icon: "info",
                        title: "Refreshing CAS Server",
                        text: "Please wait while the CAS application context is being refreshed...",
                        allowOutsideClick: false,
                        showConfirmButton: false,
                        didOpen: () => Swal.showLoading()
                    });

                    const endpoint = actuatorEndpoints.busrefresh || actuatorEndpoints.refresh;
                    $.post(endpoint)
                        .done(() => {
                            Swal.close();
                            Swal.fire("Success!", "CAS configuration has been reloaded successfully.", "success");
                        })
                        .fail((jqXHR, textStatus, errorThrown) => {
                            console.error("Error:", textStatus, errorThrown);
                            Swal.fire("Error", "Failed to reload CAS configuration.", "error");
                            displayBanner(jqXHR);
                        });
                }
            });
    }
}

function reloadConfigurationTable(response) {
    const configurationTable = $("#configurationTable").DataTable();
    configurationTable.clear();

    const mutableConfigurationTable = $("#mutableConfigurationTable").DataTable();
    mutableConfigurationTable.clear();

    $("#casPropertySourcesChipset").empty();

    for (const source of response.propertySources) {
        let propertySourceChip = `
            <div class="mdc-chip" role="row">
                <div class="mdc-chip__ripple"></div>
                <span role="gridcell">
                  <span class="mdc-chip__text">${source.name}</span>
                </span>
            </div>
        `.trim();
        $("#casPropertySourcesChipset").append($(propertySourceChip));

        const properties = flattenJSON(source.properties);
        for (const [key, value] of Object.entries(properties)) {
            if (!key.endsWith(".origin")) {
                const propertyName = key.replace(".value", "");
                let buttons = `
                            <button type="button" name="effectiveConfigPropertyValueButton" href="#" 
                                    data-key='${propertyName}'
                                    onclick="effectiveConfigPropertyValue('${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                            </button>
                        `;
                configurationTable.row.add({
                    0: `${camelcaseToTitleCase(source.name)}`,
                    1: `<code>${propertyName}</code>`,
                    2: `<code>${value}</code>`,
                    3: buttons
                });


                if (mutablePropertySources.some(entry => source.name.endsWith(entry))) {
                    const propertyName = key.replace(".value", "");
                    let buttons = `
                            <button type="button" name="effectiveConfigPropertyValueButton" href="#" 
                                    data-key='${propertyName}'
                                    onclick="effectiveConfigPropertyValue('${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                            </button>
                            <button type="button" name="updateConfigPropertyValueButton" href="#" 
                                    data-key='${propertyName}'
                                    data-source='${source.name}'
                                    onclick="updateConfigPropertyValue(this, '${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-content-save-edit min-width-32x" aria-hidden="true"></i>
                            </button>
                            <button type="button" name="deleteConfigPropertyValueButton" href="#" 
                                    data-key='${propertyName}'
                                    data-source='${source.name}'
                                    onclick="deleteConfigPropertyValue(this, '${propertyName}')"
                                    class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                            </button>
                        `;
                    mutableConfigurationTable.row.add({
                        0: `${camelcaseToTitleCase(source.name)}`,
                        1: `<code>${propertyName}</code>`,
                        2: `<code>${value}</code>`,
                        3: buttons
                    });
                }
            }
        }
    }
    configurationTable.search("").draw();
    mutableConfigurationTable.search("").draw();
}

async function fetchCasFeatures() {
    return new Promise((resolve, reject) => {
        if (!actuatorEndpoints.casFeatures) {
            console.error("CAS Features endpoint is not available.");
            resolve(CAS_FEATURES);
        } else if (CAS_FEATURES.length === 0) {
            $.get(actuatorEndpoints.casFeatures, response => {
                for (const element of response) {
                    const featureName = element.trim().replace("CasFeatureModule.", "");
                    CAS_FEATURES.push(featureName);
                }
                resolve(CAS_FEATURES);
            });
        } else {
            resolve(CAS_FEATURES);
        }
    });
}

async function fetchServices(callback) {
    if (actuatorEndpoints.registeredservices) {
        $.get(actuatorEndpoints.registeredservices, response => {
            let serviceCountByType = [0, 0, 0, 0, 0];
            let applicationsTable = $("#applicationsTable").DataTable();
            applicationsTable.clear();

            let saml2MetadataProvidersTable = $("#saml2MetadataProvidersTable").DataTable();
            saml2MetadataProvidersTable.clear();

            for (const service of response[1]) {
                let icon = "mdi-web-box";
                const serviceClass = service["@class"];
                if (serviceClass.includes("CasRegisteredService")) {
                    icon = "mdi-alpha-c-box-outline";
                    serviceCountByType[0]++;
                } else if (serviceClass.includes("SamlRegisteredService")) {
                    icon = "mdi-alpha-s-box-outline";
                    serviceCountByType[1]++;
                } else if (serviceClass.includes("OAuthRegisteredService")) {
                    icon = "mdi-alpha-o-circle-outline";
                    serviceCountByType[2]++;
                } else if (serviceClass.includes("OidcRegisteredService")) {
                    icon = "mdi-alpha-o-box-outline";
                    serviceCountByType[3]++;
                } else if (serviceClass.includes("WSFederationRegisteredService")) {
                    icon = "mdi-alpha-w-box-outline";
                    serviceCountByType[4]++;
                }

                let serviceDetails = `<span serviceId="${service.id}" title='${service.name}'>${service.name}</span>`;
                serviceDetails += "<p>";
                if (service.informationUrl) {
                    serviceDetails += `<a target="_blank" rel="noopener" href='${service.informationUrl}'>Information URL</a>`;
                }
                if (service.privacyUrl) {
                    serviceDetails += `&nbsp;<a target="_blank" rel="noopener" href='${service.privacyUrl}'>Privacy URL</a>`;
                }

                let serviceIdDetails = `<span serviceId='${service.id}' class="text-wrap">${service.serviceId}</span>`;
                if (serviceClass.includes("SamlRegisteredService")) {
                    serviceIdDetails += `<br><p><a href='${service.metadataLocation}' target='_blank'>Metadata Location</a>`;
                }

                let serviceButtons = `
                 <button type="button" name="editService" href="#" serviceId='${service.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                </button>
                <button type="button" name="deleteService" href="#" serviceId='${service.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                </button>
                <button type="button" name="copyService" href="#" serviceId='${service.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-content-copy min-width-32x" aria-hidden="true"></i>
                </button>
                `;
                if (actuatorEndpoints.entityhistory) {
                    serviceButtons += `
                    <button type="button" name="viewEntityHistory" href="#" serviceId='${service.id}'
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                        <i class="mdi mdi-history min-width-32x" aria-hidden="true"></i>
                    </button>
                    <button type="button" name="viewEntityChangelog" href="#" serviceId='${service.id}'
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                        <i class="mdi mdi-delta min-width-32x" aria-hidden="true"></i>
                    </button>
                    `;
                }

                applicationsTable.row.add({
                    0: `<i serviceId='${service.id}' title='${serviceClass}' class='mdi ${icon}'></i>`,
                    1: `${serviceDetails}`,
                    2: `${serviceIdDetails}`,
                    3: `<span serviceId='${service.id}' class="text-wrap">${service.description ?? ""}</span>`,
                    4: `<span serviceId='${service.id}'>${serviceButtons.trim()}</span>`,
                    5: `${service.id}`
                });

                let metadataSourcesCount = 0;
                if (serviceClass.includes("SamlRegisteredService")) {
                    const metadataLocation = service.metadataLocation;
                    saml2MetadataProvidersTable.row.add({
                        0: `<span serviceId='${service.id}' class="text-wrap">${service.id ?? ""}</span>`,
                        1: `<span serviceId='${service.id}' class="text-wrap">${service.name ?? ""}</span>`,
                        2: metadataLocation
                    });
                    metadataSourcesCount++;
                }
                $("#saml2metadataproviders").toggle(metadataSourcesCount > 0);
            }

            applicationsTable.on("draw", async () => {
                await initializeServiceButtons();
            });

            applicationsTable.search("").draw();
            saml2MetadataProvidersTable.search("").draw();

            servicesChart.data.datasets[0].data = serviceCountByType;
            servicesChart.update();

            if (callback !== undefined) {
                callback(applicationsTable);
            }
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }
}

function selectSidebarMenuTab(tab) {
    const applicationMenuItem = $(`nav.sidebar-navigation ul li[data-tab-index=${tab}]`);
    selectSidebarMenuButton(applicationMenuItem);
}

function navigateToApplication(serviceIdToFind) {
    let applicationsTable = $("#applicationsTable").DataTable();
    applicationsTable.search(String(serviceIdToFind));
    const foundRows = applicationsTable.rows({search: "applied"}).count();
    if (foundRows > 0) {
        const matchingRows = applicationsTable.rows({search: "applied"});
        matchingRows.nodes().to$().addClass("selected");
        applicationsTable.draw();
        activateDashboardTab(Tabs.APPLICATIONS);
        selectSidebarMenuTab(Tabs.APPLICATIONS);
    } else {
        displayBanner(`Could not find a registered service with id ${serviceIdToFind}`);
        applicationsTable.search("").draw();
    }
}

async function initializeFooterButtons() {
    $("button[name=copyServiceDefinitionWizard]").off().on("click", () => {
        const editor = initializeAceEditor("wizardServiceEditor");
        copyToClipboard(editor.getValue());
    });


    $("button[name=validateServiceWizard]").off().on("click", () => {
        const $accordion = $("#editServiceWizardMenu");
        let valid = true;
        const originalIndex = $("#editServiceWizardMenu").accordion("option", "active");
        $("#editServiceWizardMenu .ui-accordion-header:visible").each(function () {
            const $header = $(this);
            const index = $accordion
                .find(".ui-accordion-header")
                .index($header);
            $accordion.accordion("option", "active", index);

            $("#editServiceWizardForm .ui-accordion-content:visible input:visible").each(function () {
                const input = $(this);
                valid = input.get(0).checkValidity();
                if (!valid) {
                    input.get(0).reportValidity();
                    return false;
                }
            });
            if (!valid) {
                return false;
            }
        });
        if (valid) {
            const currentIndex = $("#editServiceWizardMenu").accordion("option", "active");
            if (originalIndex !== currentIndex) {
                $("#editServiceWizardMenu").accordion("option", "active", originalIndex);
            }
            if (actuatorEndpoints.registeredservices) {
                const editor = initializeAceEditor("wizardServiceEditor");
                $.ajax({
                    url: `${actuatorEndpoints.registeredservices}/validate`,
                    type: "POST",
                    contentType: "application/json",
                    data: editor.getValue(),
                    success: response => {
                        const message = `
                            The given application definition is valid and can be parsed by CAS. Please note that validity
                            does not imply behavioral correctness. It only indicates that the generated
                            application definition adheres to the expected schema and structure required by CAS
                            and can be stored successfully.
                        `;
                        Swal.fire("Success", message, "info");
                    },
                    error: (xhr, status, error) => {
                        console.error(`Error: ${status} / ${error} / ${xhr.responseText}`);
                        displayBanner(xhr);
                    }
                });
            }
        }
    });

    $("button[name=newServiceWizard]").off().on("click", () => {
        openRegisteredServiceWizardDialog();
    });

    $("button[name=newServicePlain]").off().on("click", () => {
        if (actuatorEndpoints.registeredservices) {
            const editServiceDialogElement = document.getElementById("editServiceDialog");
            let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(editServiceDialogElement);
            const editor = initializeAceEditor("serviceEditor", "json");
            editor.setValue("");
            editor.gotoLine(1);

            $(editServiceDialogElement).attr("newService", true);
            editServiceDialog["open"]();
        }
    });

    $("button[name=importService]").off().on("click", () => {
        if (actuatorEndpoints.registeredservices) {
            $("#serviceFileInput").click();
            $("#serviceFileInput").change(event =>
                Swal.fire({
                    title: "Are you sure you want to import this entry?",
                    text: "Once imported, the entry should take immediate effect.",
                    icon: "question",
                    showConfirmButton: true,
                    showDenyButton: true
                })
                    .then((result) => {
                        if (result.isConfirmed) {
                            const file = event.target.files[0];
                            const reader = new FileReader();
                            reader.readAsText(file);
                            reader.onload = e => {
                                const fileContent = e.target.result;

                                $.ajax({
                                    url: `${actuatorEndpoints.registeredservices}`,
                                    type: "PUT",
                                    contentType: "application/json",
                                    data: fileContent,
                                    success: response => $("#reloadAll").click(),
                                    error: (xhr, status, error) => {
                                        console.error("File upload failed:", error);
                                        displayBanner(xhr);
                                    }
                                });
                            };
                        }
                    }));
        }
    });

    $("button[name=exportService]").off().on("click", () => {
        if (actuatorEndpoints.registeredservices) {
            let serviceId = $(exportServiceButton).attr("serviceId");
            fetch(`${actuatorEndpoints.registeredservices}/export/${serviceId}`)
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

    $("button[name=exportAll]").off().on("click", () => {
        if (actuatorEndpoints.registeredservices) {
            fetch(`${actuatorEndpoints.registeredservices}/export`)
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

/**
 * Initialization Functions
 */

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
            {width: "50%", targets: 2},
            {width: "20%", targets: 3},
            {width: "9%", targets: 4},
            {width: "10%", targets: 5}
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
                                            <td colspan="6">Namespace: ${group}</td>
                                        </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    if (actuatorEndpoints.heimdall) {
        const heimdallViewResourceEditor = initializeAceEditor("heimdallViewResourceEditor", "json");
        heimdallViewResourceEditor.setReadOnly(true);

        function fetchHeimdallResources(heimdallViewResourceEditor) {
            $.get(`${actuatorEndpoints.heimdall}/resources`, response => {
                heimdallResourcesTable.clear();
                for (const [key, value] of Object.entries(response)) {
                    for (const resource of Object.values(value)) {
                        let buttons = `
                        <button type="button" name="viewHeimdallResource" href="#" 
                            data-id="${resource.id}" data-namespace="${key}"
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                            <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                        </button>
                    `;
                        heimdallResourcesTable.row.add({
                            0: `<code>${key}</code>`,
                            1: `${resource.id}`,
                            2: `<code>${resource.pattern}</code>`,
                            3: `<code>${resource.method}</code>`,
                            4: `<code>${resource.enforceAllPolicies ?? "false"}</code>`,
                            5: buttons
                        });
                    }
                }
                heimdallResourcesTable.draw();

                $("button[name=viewHeimdallResource]").off().on("click", function () {
                    const namespace = $(this).data("namespace");
                    const resourceId = $(this).data("id");
                    $.get(`${actuatorEndpoints.heimdall}/resources/${namespace}/${resourceId}`, response => {
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
            if (currentActiveTab === Tabs.ACCESS_STRATEGY) {
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
        if (actuatorEndpoints.serviceaccess) {
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
                url: actuatorEndpoints.serviceaccess,
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

function hideBanner() {
    notyf.dismissAll();
}

function displayBanner(error) {
    let message = "";
    if (error.hasOwnProperty("status")) {
        switch (error.status) {
        case 401:
            message = "You are not authorized to access this resource. Are you sure you are authenticated?";
            break;
        case 403:
            message = "You are forbidden from accessing this resource. Are you sure you have the necessary permissions and the entry is correctly registered with CAS?";
            break;
        case 400:
        case 500:
        case 503:
            message = "Unable to process or accept the request. Check CAS server logs for details.";
            break;
        case 0:
            message = "Unable to contact the CAS server. Are you sure the server is reachable?";
            break;
        default:
            message = `HTTP error: ${error.status}. `;
            break;
        }
    }
    if (error.hasOwnProperty("path")) {
        message += `Unable to make an API call to ${error.path}. Is the endpoint enabled and available?`;
    }
    if (message.length === 0) {
        if (typeof error === "string") {
            message = error;
        } else {
            message = error.message;
        }
    }
    notyf.dismissAll();
    notyf.error(message);
}

function initializeJvmMetrics() {
    function fetchJvmThreadMetric(metricName) {
        return new Promise((resolve, reject) =>
            $.get(`${actuatorEndpoints.metrics}/${metricName}`, response => resolve(response.measurements[0].value)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                reject(error);
            })
        );
    }

    async function fetchJvmThreadsMetrics() {
        const promises = [
            "jvm.threads.daemon",
            "jvm.threads.live",
            "jvm.threads.peak",
            "jvm.threads.started",
            "jvm.threads.states"
        ].map(metric => fetchJvmThreadMetric(metric));
        const results = await Promise.all(promises);
        return results.map(result => Number(result));
    }

    async function fetchThreadDump() {
        return new Promise((resolve, reject) =>
            $.get(actuatorEndpoints.threaddump, response => {
                let threadData = {};
                for (const thread of response.threads) {
                    if (!threadData[thread.threadState]) {
                        threadData[thread.threadState] = 0;
                    }
                    threadData[thread.threadState] += 1;
                }
                resolve(threadData);
            }).fail((xhr, status, error) => {
                console.error("Error thread dump:", error);
                displayBanner(xhr);
                reject(error);
            }));

    }

    if (actuatorEndpoints.metrics) {
        fetchJvmThreadsMetrics()
            .then(payload => {
                jvmThreadsChart.data.datasets[0].data = payload;
                jvmThreadsChart.update();
            });
    }


    if (actuatorEndpoints.threaddump) {
        fetchThreadDump().then(payload => {
            threadDumpChart.data.labels = Object.keys(payload);
            const values = Object.values(payload);
            threadDumpChart.data.datasets[0] = {
                label: `${values.reduce((sum, value) => sum + value, 0)} Threads`,
                data: values
            };
            threadDumpChart.update();
        });
    }
}

function waitForActuator(endpoint, intervalMs = 2000) {
    return new Promise((resolve) => {
        function poll() {
            $.ajax({
                url: endpoint,
                method: "GET",
                dataType: "json",
                timeout: 3000
            })
                .done(function (data) {
                    resolve(data);
                })
                .fail(function () {
                    setTimeout(poll, intervalMs);
                });
        }

        poll();
    });
}

async function initializeScheduledTasksOperations() {

    const threadDumpTable = $("#threadDumpTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "5%", targets: 0},
            {width: "35%", targets: 1},
            {width: "30%", targets: 2},
            {width: "10%", targets: 3},
            {width: "10%", targets: 4},
            {width: "10%", targets: 5}
        ],
        drawCallback: settings => {
            $("#threadDumpTable tr").addClass("mdc-data-table__row");
            $("#threadDumpTable td").addClass("mdc-data-table__cell");
        }
    });


    const groupColumn = 0;
    const scheduledtasks = $("#scheduledTasksTable").DataTable({
        pageLength: 25,
        columnDefs: [{visible: false, targets: groupColumn}],
        order: [groupColumn, "asc"],
        drawCallback: settings => {
            $("#scheduledTasksTable tr").addClass("mdc-data-table__row");
            $("#scheduledTasksTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(groupColumn, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td>
                                        </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    function addScheduledTaskCategory(groupName, items) {
        if (items !== undefined && Array.isArray(items)) {
            for (const group of items) {
                const flattened = flattenJSON(group);
                for (const [key, value] of Object.entries(flattened)) {
                    const target = flattened["runnable.target"];
                    if (target !== value) {
                        scheduledtasks.row.add({
                            0: `<code>${camelcaseToTitleCase(groupName)} / ${getLastTwoWords(target)}</code>`,
                            1: `<code>${key}</code>`,
                            2: `<code>${value}</code>`
                        });
                    }
                }
            }
        }
    }

    if (actuatorEndpoints.scheduledtasks) {
        $.get(actuatorEndpoints.scheduledtasks, response => {
            scheduledtasks.clear();
            for (const group of Object.keys(response)) {
                addScheduledTaskCategory(group, response[group]);
            }
            scheduledtasks.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    if (actuatorEndpoints.metrics) {
        initializeJvmMetrics();
        setInterval(() => {
            if (currentActiveTab === Tabs.TASKS) {
                initializeJvmMetrics();
            }
        }, palantirSettings().refreshInterval);
    }

    function fetchThreadDump() {
        $.get(actuatorEndpoints.threaddump, response => {
            threadDumpTable.clear();
            for (const thread of response.threads) {
                threadDumpTable.row.add({
                    0: `<code>${thread.threadId}</code>`,
                    1: `<code>${thread.threadName}</code>`,
                    2: `<code>${thread.threadState}</code>`,
                    3: `<code>${thread.priority}</code>`,
                    4: `<code>${thread.daemon}</code>`,
                    5: `<code>${thread.suspended}</code>`
                });
            }
            threadDumpTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }


    if (actuatorEndpoints.threaddump) {
        fetchThreadDump();
        setInterval(() => {
            if (currentActiveTab === Tabs.TASKS) {
                fetchThreadDump();
            }
        }, palantirSettings().refreshInterval);
    }
}

async function initializeTicketsOperations() {
    const ticketEditor = initializeAceEditor("ticketEditor");
    ticketEditor.setReadOnly(true);

    $("button#searchTicketButton").off().on("click", () => {
        const ticket = document.getElementById("ticket");
        if (!ticket.checkValidity()) {
            ticket.reportValidity();
            return false;
        }
        const ticketId = $(ticket).val();
        const type = $("#ticketDefinitions .mdc-list-item--selected").attr("data-value").trim();
        if (ticket && type) {
            const decode = new mdc.switchControl.MDCSwitch(document.getElementById("decodeTicketButton")).selected;
            ticketEditor.setValue("");
            if (actuatorEndpoints.ticketregistry) {
                $.get(`${actuatorEndpoints.ticketregistry}/query?type=${type}&id=${ticketId}&decode=${decode}`,
                    response => {
                        ticketEditor.setValue(JSON.stringify(response, null, 2));
                        ticketEditor.gotoLine(1);
                    })
                    .fail((xhr, status, error) => {
                        console.error("Error fetching data:", error);
                        displayBanner(xhr);
                    });
            }
        }
    });

    $("button#cleanTicketsButton").off().on("click", () => {
        hideBanner();
        if (actuatorEndpoints.ticketregistry) {
            $.ajax({
                url: `${actuatorEndpoints.ticketregistry}/clean`,
                type: "DELETE",
                success: response => {
                    ticketEditor.setValue(JSON.stringify(response, null, 2));
                    ticketEditor.gotoLine(1);
                },
                error: (xhr, status, error) => {
                    console.error(`Error: ${status} / ${error} / ${xhr.responseText}`);
                    displayBanner(xhr);
                }
            });
        }

    });

    const ticketCatalogTable = $("#ticketCatalogTable").DataTable({

        pageLength: 10,
        columnDefs: [
            {visible: false, targets: 0}
        ],

        order: [0, "desc"],
        drawCallback: settings => {
            $("#ticketCatalogTable tr").addClass("mdc-data-table__row");
            $("#ticketCatalogTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    if (actuatorEndpoints.ticketregistry) {
        $.get(`${actuatorEndpoints.ticketregistry}/ticketCatalog`, response => {
            response.forEach(entry => {
                let item = `
                            <li class="mdc-list-item" data-value='${entry.prefix}' role="option">
                                <span class="mdc-list-item__ripple"></span>
                                <span class="mdc-list-item__text">${entry.apiClass}</span>
                            </li>
                        `;
                $("#ticketDefinitions").append($(item.trim()));

                const flattened = flattenJSON(entry);
                for (const [key, value] of Object.entries(flattened)) {
                    ticketCatalogTable.row.add({
                        0: `<code>${entry.prefix}</code>`,
                        1: `<code>${key}</code>`,
                        2: `<code>${value}</code>`
                    });
                }
                ticketCatalogTable.draw();
            });
            const ticketDefinitions = new mdc.select.MDCSelect(document.getElementById("ticketDefinitionsSelect"));
            ticketDefinitions.selectedIndex = 0;
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    const ticketExpirationPoliciesTable = $("#ticketExpirationPoliciesTable").DataTable({
        pageLength: 10,
        columnDefs: [
            {visible: false, targets: 0}
        ],

        order: [0, "desc"],
        drawCallback: settings => {
            $("#ticketExpirationPoliciesTable tr").addClass("mdc-data-table__row");
            $("#ticketExpirationPoliciesTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    if (actuatorEndpoints.ticketExpirationPolicies) {
        $.get(actuatorEndpoints.ticketExpirationPolicies, response => {
            ticketExpirationPoliciesTable.clear();
            for (const key of Object.keys(response)) {
                const policy = response[key];
                delete policy.name;

                const flattened = flattenJSON(policy);
                for (const [k, v] of Object.entries(flattened)) {
                    ticketExpirationPoliciesTable.row.add({
                        0: `<code>${key}</code>`,
                        1: `<code>${k}</code>`,
                        2: `<code>${v}</code>`
                    });
                }
            }
            ticketExpirationPoliciesTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }
}

async function initializeSsoSessionOperations() {
    const ssoSessionApplicationsTable = $("#ssoSessionApplicationsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "40%", targets: 0},
            {width: "60%", targets: 1}
        ],
        drawCallback: settings => {
            $("#ssoSessionApplicationsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionApplicationsTable td").addClass("mdc-data-table__cell");
        }
    });

    const ssoSessionDetailsTable = $("#ssoSessionDetailsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "10%", targets: 0},
            {width: "20%", targets: 1},
            {width: "70%", targets: 2}
        ],
        drawCallback: settings => {
            $("#ssoSessionDetailsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionDetailsTable td").addClass("mdc-data-table__cell");
        }
    });

    const ssoSessionsTable = $("#ssoSessionsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "13%", targets: 0},
            {width: "37%", targets: 1},
            {width: "10%", targets: 2},
            {width: "15%", targets: 3},
            {width: "5%", targets: 4},
            {width: "8%", targets: 5},
            {width: "12%", targets: 6},
            {visible: false, target: 7}
        ],
        drawCallback: settings => {
            $("#ssoSessionsTable tr").addClass("mdc-data-table__row");
            $("#ssoSessionsTable td").addClass("mdc-data-table__cell");
        }
    });

    $("#ssoSessionUsername").on("keypress", e => {
        if (e.which === 13) {
            $("#ssoSessionButton").click();
        }
    });

    $("#removeSsoSessionButton").off().on("click", () => {
        if (actuatorEndpoints.ssosessions) {
            const form = document.getElementById("fmSsoSessions");
            if (!form.reportValidity()) {
                return false;
            }

            Swal.fire({
                title: "Are you sure you want to delete all sessions for the user?",
                text: "Once deleted, you may not be able to recover this and user's SSO session will be removed.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true

            })
                .then((result) => {
                    if (result.isConfirmed) {
                        const username = $("#ssoSessionUsername").val();

                        $.ajax({
                            url: `${actuatorEndpoints.ssosessions}/users/${username}`,
                            type: "DELETE",
                            contentType: "application/x-www-form-urlencoded",
                            success: (response, status, xhr) => ssoSessionsTable.clear().draw(),
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=ssoSessionButton]").off().on("click", () => {
        if (actuatorEndpoints.ssosessions) {
            const form = document.getElementById("fmSsoSessions");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#ssoSessionUsername").val();
            Swal.fire({
                icon: "info",
                title: `Fetching SSO Sessions for ${username}`,
                text: "Please wait while single sign-on sessions are retrieved...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });

            ssoSessionsTable.clear();
            ssoSessionApplicationsTable.clear();

            $.ajax({
                url: `${actuatorEndpoints.ssosessions}/users/${username}`,
                type: "GET",
                contentType: "application/x-www-form-urlencoded",
                success: (response, status, xhr) => {
                    for (const session of response.activeSsoSessions) {
                        const attributes = {
                            principal: session["principal_attributes"],
                            authentication: session["authentication_attributes"]
                        };

                        const services = {};
                        for (const [key, value] of Object.entries(session["authenticated_services"])) {
                            services[key] = {id: value.id};
                        }

                        let serviceButtons = `
                            <button type="button" name="removeSsoSession" href="#" 
                                data-ticketgrantingticket='${session.ticket_granting_ticket}'
                                class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                            </button>
                            <button type="button" name="viewSsoSession" href="#" 
                                data-ticketgrantingticket='${session.ticket_granting_ticket}'
                                class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-account-eye min-width-32x" aria-hidden="true"></i>
                                <span id="sessionAttributes" class="d-none">${JSON.stringify(attributes)}</span>
                                <span id="sessionServices" class="d-none">${JSON.stringify(services)}</span>
                            </button>
                            <button type="button" name="copySsoSessionTicketGrantingTicket" href="#" 
                                data-ticketgrantingticket='${session.ticket_granting_ticket}'
                                class="mdc-button mdc-button--raised min-width-32x">
                                <i class="mdi mdi-content-copy min-width-32x" aria-hidden="true"></i>
                            </button>
                    `;

                        ssoSessionsTable.row.add({
                            0: `<code>${session["authentication_date"]}</code>`,
                            1: `<code>${session["ticket_granting_ticket"]}</code>`,
                            2: `<code>${session["authentication_attributes"]?.clientIpAddress?.[0]}</code>`,
                            3: `<code>${session["authentication_attributes"]?.userAgent?.[0]}</code>`,
                            4: `<code>${session["number_of_uses"]}</code>`,
                            5: `<code>${session["remember_me"]}</code>`,
                            6: `${serviceButtons}`,
                            7: `${JSON.stringify(attributes)}`
                        });
                    }
                    ssoSessionsTable.draw();

                    Swal.close();

                    $("button[name=viewSsoSession]").off().on("click", function () {
                        ssoSessionApplicationsTable.clear();
                        const sessionServices = JSON.parse($(this).children("#sessionServices").text());
                        for (const [key, value] of Object.entries(sessionServices)) {
                            ssoSessionApplicationsTable.row.add({
                                0: `<code>${key}</code>`,
                                1: `<code>${value.id}</code>`
                            });
                        }

                        const attributes = JSON.parse($(this).children("#sessionAttributes").text());
                        for (const [key, value] of Object.entries(attributes.principal)) {
                            ssoSessionDetailsTable.row.add({
                                0: `<code>Principal</code>`,
                                1: `<code>${key}</code>`,
                                2: `<code>${value}</code>`
                            });
                        }
                        for (const [key, value] of Object.entries(attributes.authentication)) {
                            ssoSessionDetailsTable.row.add({
                                0: `<code>Authentication</code>`,
                                1: `<code>${key}</code>`,
                                2: `<code>${value}</code>`
                            });
                        }
                        ssoSessionDetailsTable.draw();
                        ssoSessionApplicationsTable.draw();

                        let dialog = mdc.dialog.MDCDialog.attachTo(document.getElementById("ssoSession-dialog"));
                        dialog["open"]();
                    });

                    $("button[name=removeSsoSession]").off().on("click", function () {
                        const ticket = $(this).data("ticketgrantingticket");

                        Swal.fire({
                            title: "Are you sure you want to delete this session?",
                            text: "Once deleted, you may not be able to recover this session.",
                            icon: "question",
                            showConfirmButton: true,
                            showDenyButton: true
                        })
                            .then((result) => {
                                if (result.isConfirmed) {
                                    $.ajax({
                                        url: `${actuatorEndpoints.ssosessions}/${ticket}`,
                                        type: "DELETE",
                                        contentType: "application/x-www-form-urlencoded",
                                        success: (response, status, xhr) => {
                                            let nearestTr = $(this).closest("tr");
                                            ssoSessionsTable.row(nearestTr).remove().draw();
                                        },
                                        error: (xhr, status, error) => {
                                            console.error("Error fetching data:", error);
                                            displayBanner(xhr);
                                        }
                                    });
                                }
                            });
                    });

                    $("button[name=copySsoSessionTicketGrantingTicket]").off().on("click", function () {
                        const ticket = $(this).data("ticketgrantingticket");
                        copyToClipboard(ticket);
                    });

                },
                error: (xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    Swal.close();
                    displayBanner(xhr);
                }
            });
        }
    });

    $("button[name=removeAllSsoSessionsButton]").off().on("click", () => {
        if (actuatorEndpoints.ssosessions) {
            Swal.fire({
                title: "Are you sure you want to delete all sessions for all users?",
                text: "Once deleted, you may not be able to recover and ALL sso sessions for ALL users will be removed.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${actuatorEndpoints.ssosessions}`,
                            type: "DELETE",
                            contentType: "application/x-www-form-urlencoded",
                            success: (response, status, xhr) => ssoSessionsTable.clear().draw(),
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });

        }
    });

}

async function initializeLoggingOperations() {
    const toolbar = document.createElement("div");
    toolbar.innerHTML = `
        <button type="button" id="newLoggerButton" class="mdc-button mdc-button--raised">
            <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus" aria-hidden="true"></i>New</span>
        </button>
    `;
    const loggersTable = $("#loggersTable").DataTable({
        layout: {
            topStart: toolbar
        },
        pageLength: 25,
        autoWidth: false,
        columnDefs: [
            {width: "90%", targets: 0},
            {width: "10%", targets: 1}
        ],
        drawCallback: settings => {
            $("#loggersTable tr").addClass("mdc-data-table__row");
            $("#loggersTable td").addClass("mdc-data-table__cell");
        }
    });

    function determineLoggerColor(level) {
        let background = "darkgray";
        switch (level) {
        case "DEBUG":
            background = "cornflowerblue";
            break;
        case "INFO":
            background = "mediumseagreen";
            break;
        case "WARN":
            background = "darkorange";
            break;
        case "OFF":
            background = "black";
            break;
        case "ERROR":
            background = "red";
            break;
        }
        return background;
    }

    function handleLoggerLevelSelectionChange() {
        $("select[name=loggerLevelSelect]").off().on("change", function () {
            if (actuatorEndpoints.loggers) {
                const logger = $(this).data("logger");
                const level = $(this).val();
                const loggerData = {
                    "configuredLevel": level
                };
                $.ajax({
                    url: `${actuatorEndpoints.loggers}/${logger}`,
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(loggerData),
                    success: response => $(this).css("background-color", determineLoggerColor(level)),
                    error: (xhr, status, error) => {
                        console.error("Failed", error);
                        displayBanner(xhr);
                    }
                });
            }
        });
    }

    function fetchLoggerData(callback) {
        if (actuatorEndpoints.loggingconfig) {
            $.get(actuatorEndpoints.loggingconfig, response => callback(response)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    }

    function updateLoggersTable() {
        fetchLoggerData(response => {
            function addLoggerToTable(logger) {
                const background = determineLoggerColor(logger.level);
                const loggerLevel = `
                    <select data-logger='${logger.name}' name='loggerLevelSelect' class="palantir" style="color: whitesmoke; background-color: ${background}">
                        <option ${logger.level === "TRACE" ? "selected" : ""} value="TRACE">TRACE</option>
                        <option ${logger.level === "DEBUG" ? "selected" : ""} value="DEBUG">DEBUG</option>
                        <option ${logger.level === "INFO" ? "selected" : ""} value="INFO">INFO</option>
                        <option ${logger.level === "WARN" ? "selected" : ""} value="WARN">WARN</option>
                        <option ${logger.level === "ERROR" ? "selected" : ""} value="ERROR">ERROR</option>
                        <option ${logger.level === "OFF" ? "selected" : ""} value="OFF">OFF</option>
                    </select>
                    `;

                loggersTable.row.add({
                    0: `<code>${logger.name}</code>`,
                    1: `${loggerLevel.trim()}`
                });
            }

            loggersTable.clear();
            for (const logger of response.loggers) {
                addLoggerToTable(logger);
            }
            loggersTable.draw();

            $("#newLoggerButton").off().on("click", () =>
                Swal.fire({
                    input: "text",
                    inputAttributes: {
                        autocapitalize: "off"
                    },
                    showConfirmButton: true,
                    showDenyButton: true,
                    icon: "success",
                    title: "What's the name of the new logger?",
                    text: "The new logger will only be effective at runtime and will not be persisted."
                })
                    .then((result) => {
                        if (result.isConfirmed) {
                            addLoggerToTable({name: result.value, level: "INFO"});
                            loggersTable.draw();
                            handleLoggerLevelSelectionChange();
                            loggersTable.search(result.value).draw();
                        }
                    }));
            handleLoggerLevelSelectionChange();
        });
    }

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
    tabs.listen("MDCTabBar:activated", ev => {
        let index = ev.detail.index;
        if (index === Tabs.LOGGING) {
            updateLoggersTable();
        }
    });

    if (currentActiveTab === Tabs.LOGGING) {
        updateLoggersTable();
    }

    const logEndpoints = [actuatorEndpoints.cloudwatchlogs, actuatorEndpoints.gcplogs, actuatorEndpoints.loggingconfig, actuatorEndpoints.logfile];
    const hasLogEndpoint = logEndpoints.some(element => element !== undefined);

    if (hasLogEndpoint) {
        const scrollSwitch = new mdc.switchControl.MDCSwitch(document.getElementById("scrollLogsButton"));
        scrollSwitch.selected = true;

        if (actuatorEndpoints.logfile) {
            $("#logFileStream").parent().addClass("w-50");
            $("#logDataStream").parent().addClass("w-50");
        } else {
            $("#logFileStream").parent().addClass("d-none");
            $("#logDataStream").parent().addClass("w-100");
        }

        async function fetchLogsFrom(endpoint) {
            if (endpoint) {
                const logDataStream = $("#logDataStream");
                const level = $("#logLevelFilter").val();
                const count = $("#logCountFilter").val();

                $.ajax({
                    url: `${endpoint}/stream?level=${level}&count=${count}`,
                    type: "GET",
                    dataType: "json",
                    success: logEvents => {
                        logDataStream.empty();
                        logEvents.forEach(log => {
                            let className = `log-${log.level.toLowerCase()}`;
                            const logEntry = `<div>${new Date(log.timestamp).toLocaleString()} - <span class='${className}'>[${log.level}]</span> - ${log.message}</div>`;
                            logDataStream.append($(logEntry));
                        });
                        if (scrollSwitch.selected) {
                            logDataStream.scrollTop(logDataStream.prop("scrollHeight"));
                        }
                    },
                    error: (xhr, status, error) => console.error("Streaming logs failed:", error)
                });
            }
        }

        function startStreamingLogFile() {
            return setInterval(() => {
                if (currentActiveTab === Tabs.LOGGING) {
                    const logFileStream = $("#logFileStream");

                    $.ajax({
                        url: actuatorEndpoints.logfile,
                        type: "GET",
                        success: response => {
                            logFileStream.empty();
                            logFileStream.text(response);
                            if (scrollSwitch.selected) {
                                logFileStream.scrollTop(logFileStream.prop("scrollHeight"));
                            }
                        },
                        error: (xhr, status, error) => console.error("Streaming logs failed:", error)
                    });
                }
            }, $("#logRefreshFilter").val());
        }

        function startStreamingLogData() {
            return setInterval(() => {
                if (currentActiveTab === Tabs.LOGGING) {
                    fetchLogsFrom(actuatorEndpoints.cloudwatchlogs);
                    fetchLogsFrom(actuatorEndpoints.gcplogs);
                    fetchLogsFrom(actuatorEndpoints.loggingconfig);
                }
            }, $("#logRefreshFilter").val());
        }

        let refreshStreamInterval = startStreamingLogData();

        let refreshLogFileInterval = undefined;
        if (actuatorEndpoints.logfile) {
            refreshLogFileInterval = startStreamingLogFile();
        }

        $("#logRefreshFilter").selectmenu({
            change: (event, data) => {
                clearInterval(refreshStreamInterval);
                refreshStreamInterval = startStreamingLogData();

                if (refreshLogFileInterval) {
                    clearInterval(refreshLogFileInterval);
                    refreshLogFileInterval = startStreamingLogFile();
                }
            }
        });

        $("#downloadLogsButton").off().on("click", () => {
            try {
                $("#downloadLogsButton").prop("disabled", true);
                hideBanner();
                const text = $("#logDataStream").text();
                const blob = new Blob([text], {type: "text/plain"});
                const url = URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = "cas.log";
                a.click();
                URL.revokeObjectURL(url);
            } catch (e) {
                console.error("Error downloading log file:", e);
                displayBanner(e);
            } finally {
                $("#downloadLogsButton").prop("disabled", false);
            }
        });

    } else {
        $("#loggingDataStreamOps").parent().addClass("d-none");
    }

}

async function restoreActiveTabs() {
    const storedTabs = localStorage.getItem("ActiveTabs");
    const activeTabs = storedTabs ? JSON.parse(storedTabs) : {};
    for (const [key, value] of Object.entries(activeTabs)) {
        $(`#${key}`).tabs("option", "active", Number(value));
        $(`#${key}`).tabs("refresh");
    }
}

async function initializeCasSpringWebflowOperations() {
    function drawFlowStateDiagram() {
        $("#webflowMarkdownContainer").addClass("hide");
        $("#webflowDiagram").addClass("hide");
        hideBanner();
        const flowId = $("#webflowFilter").val();

        Swal.fire({
            icon: "info",
            title: `Fetching Webflow Definition`,
            text: `Please wait while webflow definition for ${flowId} is processed...`,
            allowOutsideClick: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });

        const selectedState = $("#webflowStateFilter").val();
        let url = `${actuatorEndpoints.springWebflow}?flowId=${flowId}`;
        if (selectedState && selectedState !== "all") {
            url += `&stateId=${selectedState}`;
        }
        $.ajax({
            url: url,
            type: "GET",
            headers: {
                "Content-Type": "application/json"
            },
            success: async (response, textStatus, xhr) => {
                const flow = response[flowId];

                if (!selectedState) {
                    const allStates = Object.keys(flow.states).sort();
                    $("#webflowStateFilter").empty().append(
                        $("<option>", {
                            value: "all",
                            text: "All",
                            selected: true
                        })
                    );

                    $.each(allStates, (idx, item) => {
                        $("#webflowStateFilter").append(
                            $("<option>", {
                                value: item,
                                text: item
                            })
                        );
                    });
                    $("#webflowStateFilter").selectmenu("refresh");
                }

                let diagramDefinition = `stateDiagram-v2\ndirection LR\n`;
                if (flow.startActions) {
                    diagramDefinition += `\t[*] --> Initialization: Start\n`;
                    diagramDefinition += `\tstate Initialization {\n`;
                    for (let i = 0; i < flow.startActions.length; i++) {
                        let action = flow.startActions[i];
                        if (action.startsWith("set ")) {
                            action = "Execute";
                        }

                        if (i === 0) {
                            diagramDefinition += `\t\t[*] --> ${action}: Then\n`;
                        } else {
                            let previousAction = flow.startActions[i - 1];
                            if (previousAction.startsWith("set ")) {
                                previousAction = "Execute";
                            }
                            diagramDefinition += `\t\t${previousAction} --> ${action}: Then\n`;
                        }
                        if (i === flow.startActions.length - 1) {
                            diagramDefinition += `\t\t${action} --> [*]: End\n`;
                        }
                    }
                    diagramDefinition += `\t}\n`;
                    diagramDefinition += `\tInitialization --> ${flow.startState}: Then\n`;
                } else {
                    diagramDefinition += `\t[*] --> ${flow.startState}: Start\n`;
                }

                for (let entry of Object.keys(flow.states)) {
                    const state = flow.states[entry];
                    entry = entry.trim().replace(/-/g, "_");
                    if (state.isEndState === true) {
                        diagramDefinition += `\t${entry} --> [*]: End\n`;
                    } else {

                        if (state.isViewState) {
                            if (!state.transitions) {
                                diagramDefinition += `\t${entry} --> [*]: End\n`;
                            }
                        }
                        if (state.transitions) {
                            for (const transition of state.transitions) {
                                let event = transition.substring(0, transition.indexOf("->")).trim().replace(/-/g, "_");
                                if (event === "*") {
                                    if (state.isDecisionState) {
                                        event = "Otherwise";
                                    } else {
                                        event = "Always";
                                    }
                                }
                                let target = transition.substring(transition.indexOf("->") + 2).trim().replace(/-/g, "_");
                                diagramDefinition += `\t${entry} --> ${target}: ${event}\n`;
                            }
                        }
                        if (state.actionList || state.entryActions) {
                            diagramDefinition += `\tstate ${entry} {\n`;

                            if (state.entryActions && state.entryActions.length > 0) {
                                for (let i = 0; i < state.entryActions.length; i++) {
                                    let action = state.entryActions[i].replace(/-/g, "_").trim();
                                    if (action.startsWith("set ")) {
                                        action = "Execute";
                                    }
                                    if (i === 0) {
                                        diagramDefinition += `\t\t[*] --> ${action}: Start\n`;
                                    } else {
                                        let previousAction = state.entryActions[i - 1].replace(/-/g, "_").trim();
                                        if (previousAction.startsWith("set ")) {
                                            previousAction = "Execute";
                                        }
                                        diagramDefinition += `\t\t${previousAction} --> ${action}: Then\n`;
                                    }
                                }
                            }

                            if (state.actionList && state.actionList.length > 0) {
                                let startActionState = state.entryActions
                                    ? state.entryActions[state.entryActions.length - 1].replace(/-/g, "_").trim()
                                    : "[*]";
                                if (startActionState.startsWith("set ")) {
                                    startActionState = "Execute";
                                }

                                for (let i = 0; i < state.actionList.length; i++) {
                                    let action = state.actionList[i];
                                    if (action.startsWith("set ")) {
                                        action = "Execute";
                                    }
                                    const label = startActionState === "[*]" ? "Start" : "Then";
                                    if (i === 0) {
                                        diagramDefinition += `\t\t${startActionState} --> ${action}: ${label}\n`;
                                    }
                                    if (i === state.actionList.length - 1) {
                                        diagramDefinition += `\t\t${action} --> [*]: End\n`;
                                    }
                                }
                            } else if (state.entryActions && state.entryActions.length > 0) {
                                let lastEntryAction = state.entryActions[state.entryActions.length - 1];
                                if (lastEntryAction.startsWith("set ")) {
                                    lastEntryAction = "Execute";
                                }
                                diagramDefinition += `\t\t${lastEntryAction} --> [*]: End\n`;
                            }
                            diagramDefinition += `\t}\n`;
                        }


                        if (state.viewState) {
                            diagramDefinition += `\tnote left of ${entry}\n\t\tView: ${state.viewId}\n\tend note\n`;
                        }
                    }
                }

                $("#webflowMarkdownContainer").removeClass("hide");
                $("#webflowMarkdown").empty().text(diagramDefinition);
                const {svg, bindFunctions} = await mermaid.render("webflowDiagram", diagramDefinition);
                const container = document.getElementById("webflowContainer");
                container.innerHTML = svg;
                $("#webflowDiagram").removeClass("hide");
                Swal.close();
                bindFunctions?.(container);
                updateNavigationSidebar();
            },
            error: (xhr, textStatus, errorThrown) => {
                console.error("Error fetching data:", errorThrown);
                Swal.close();
            }
        });
    }

    if (actuatorEndpoints.springWebflow) {
        mermaid.initialize({
            startOnLoad: false,
            securityLevel: "loose",
            theme: "base",
            logLevel: 4,
            themeVariables: {
                primaryColor: "deepskyblue",
                secondaryColor: "#73e600",
                lineColor: "deepskyblue"
            }
        });

        $("#webflowFilter").empty().selectmenu({
            change: (event, data) => {
                $("#webflowStateFilter").empty();
                drawFlowStateDiagram();
            }
        });
        $("#webflowStateFilter").empty().selectmenu({
            change: (event, data) => drawFlowStateDiagram()
        });

        $.ajax({
            url: `${actuatorEndpoints.springWebflow}`,
            type: "GET",
            headers: {
                "Content-Type": "application/json"
            },
            success: async (response, textStatus, xhr) => {
                const availableFlows = Object.keys(response);
                $.each(availableFlows, (idx, item) => {
                    $("#webflowFilter").append(
                        $("<option>", {
                            value: item,
                            text: item.toUpperCase(),
                            selected: idx === 0
                        })
                    );
                });
                $("#webflowFilter").selectmenu("refresh");
                drawFlowStateDiagram();
            },
            error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown)
        });
    }
}

async function initializeCasEventsOperations() {
    if (actuatorEndpoints.events) {
        const casEventsTable = $("#casEventsTable").DataTable({
            pageLength: 10,
            drawCallback: settings => {
                $("#casEventsTable tr").addClass("mdc-data-table__row");
                $("#casEventsTable td").addClass("mdc-data-table__cell");
            }
        });

        function fetchCasEvents() {
            return setInterval(() => {
                if (currentActiveTab === Tabs.LOGGING) {
                    $.ajax({
                        url: `${actuatorEndpoints.events}`,
                        type: "GET",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded"
                        },
                        success: (response, textStatus, xhr) => {
                            casEventsTable.clear();
                            const jsonEvents = JSON.parse(response);
                            if (jsonEvents.length > 0) {
                                for (const entry of Object.values(jsonEvents[1])) {
                                    const geoLocation = `${entry?.properties?.geoLatitude ?? ""} ${entry?.properties?.geoLongitude ?? ""} ${entry?.properties?.geoAccuracy ?? ""}`.trim();
                                    casEventsTable.row.add({
                                        0: `<code>${entry?.creationTime ?? "N/A"}</code>`,
                                        1: `<code>${getLastWord(entry?.type) ?? "N/A"}</code>`,
                                        2: `<code>${entry?.properties?.eventId ?? "N/A"}</code>`,
                                        3: `<code>${entry?.principalId ?? "N/A"}</code>`,
                                        4: `<code>${entry?.properties?.clientip ?? "N/A"}</code>`,
                                        5: `<code>${entry?.properties?.serverip ?? "N/A"}</code>`,
                                        6: `<code>${entry?.properties?.agent ?? "N/A"}</code>`,
                                        7: `<code>${entry?.properties?.tenant ?? "N/A"}</code>`,
                                        8: `<code>${entry?.properties?.deviceFingerprint ?? "N/A"}</code>`,
                                        9: `<code>${geoLocation.length == 0 ? "N/A" : geoLocation}</code>`
                                    });
                                }
                            }
                            casEventsTable.draw();
                        },
                        error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown)
                    });
                }
            }, $("#casEventsRefreshFilter").val());
        }

        let refreshInterval = undefined;
        if (actuatorEndpoints.events) {
            refreshInterval = fetchCasEvents();
        }
        $("#casEventsRefreshFilter").selectmenu({
            change: (event, data) => {
                if (refreshInterval) {
                    clearInterval(refreshInterval);
                    refreshInterval = fetchCasEvents();
                }
            }
        });
    }
}

async function initializeAuditEventsOperations() {
    if (actuatorEndpoints.auditlog) {
        const auditEventsTable = $("#auditEventsTable").DataTable({
            pageLength: 10,
            drawCallback: settings => {
                $("#auditEventsTable tr").addClass("mdc-data-table__row");
                $("#auditEventsTable td").addClass("mdc-data-table__cell");
            }
        });

        function fetchAuditLog() {
            return setInterval(() => {
                if (currentActiveTab === Tabs.LOGGING) {
                    const interval = $("#auditEventsIntervalFilter").val();
                    const count = $("#auditEventsCountFilter").val();

                    $.ajax({
                        url: `${actuatorEndpoints.auditlog}?interval=${interval}&count=${count}`,
                        type: "GET",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded"
                        },
                        success: (response, textStatus, xhr) => {

                            auditEventsTable.clear();
                            for (const entry of response) {
                                auditEventsTable.row.add({
                                    0: `<code>${entry?.principal ?? "N/A"}</code>`,
                                    1: `<code>${entry?.auditableResource ?? "N/A"}</code>`,
                                    2: `<code>${entry?.actionPerformed ?? "N/A"}</code>`,
                                    3: `<code>${entry?.whenActionWasPerformed ?? "N/A"}</code>`,
                                    4: `<code>${entry?.clientInfo?.clientIpAddress ?? "N/A"}</code>`,
                                    5: `<span>${entry?.clientInfo?.userAgent ?? "N/A"}</span>`
                                });
                            }
                            auditEventsTable.draw();
                        },
                        error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown)
                    });
                }
            }, $("#auditEventsRefreshFilter").val());
        }

        let refreshInterval = undefined;
        if (actuatorEndpoints.auditlog) {
            refreshInterval = fetchAuditLog();
        }
        $("#auditEventsRefreshFilter").selectmenu({
            change: (event, data) => {
                if (refreshInterval) {
                    clearInterval(refreshInterval);
                    refreshInterval = fetchAuditLog();
                }
            }
        });
    }
}

async function initializeSystemOperations() {
    function configureAuditEventsChart() {
        if (actuatorEndpoints.auditevents) {
            $.get(actuatorEndpoints.auditevents, response => {
                let auditData = [];
                const results = response.events.reduce((accumulator, event) => {
                    let timestamp = formatDateYearMonthDay(event.timestamp);
                    const type = event.type;

                    if (!accumulator[timestamp]) {
                        accumulator[timestamp] = {};
                    }

                    if (!accumulator[timestamp][type]) {
                        accumulator[timestamp][type] = 0;
                    }
                    accumulator[timestamp][type]++;
                    return accumulator;
                }, {});

                for (const [key, value] of Object.entries(results)) {
                    let auditEntry = Object.assign({timestamp: key}, value);
                    auditData.push(auditEntry);
                }

                auditEventsChart.data.labels = auditData.map(d => d.timestamp);
                let datasets = [];
                for (const entry of auditData) {
                    for (const type of Object.keys(auditData[0])) {
                        if (type !== "timestamp" && type !== "AUTHORIZATION_FAILURE") {
                            datasets.push({
                                borderWidth: 2,
                                data: auditData,
                                parsing: {
                                    xAxisKey: "timestamp",
                                    yAxisKey: type
                                },
                                label: type
                            });
                        }
                    }
                }
                auditEventsChart.data.datasets = datasets;
                auditEventsChart.update();
            });
        }
    }

    async function configureHttpRequestResponses() {
        if (actuatorEndpoints.httpexchanges) {
            $.get(actuatorEndpoints.httpexchanges, response => {
                function urlIsAcceptable(url) {
                    return !url.startsWith("/actuator")
                        && !url.startsWith("/webjars")
                        && !url.endsWith(".js")
                        && !url.endsWith(".ico")
                        && !url.endsWith(".png")
                        && !url.endsWith(".jpg")
                        && !url.endsWith(".jpeg")
                        && !url.endsWith(".gif")
                        && !url.endsWith(".svg")
                        && !url.endsWith(".css");
                }


                let httpSuccesses = [];
                let httpFailures = [];
                let httpSuccessesPerUrl = [];
                let httpFailuresPerUrl = [];

                let totalHttpSuccessPerUrl = 0;
                let totalHttpSuccess = 0;
                let totalHttpFailurePerUrl = 0;
                let totalHttpFailure = 0;

                for (const exchange of response.exchanges) {
                    let timestamp = formatDateYearMonthDayHourMinute(exchange.timestamp);
                    let url = exchange.request.uri
                        .replace(casServerPrefix, "")
                        .replaceAll(/\?.+/gi, "");

                    if (urlIsAcceptable(url)) {
                        if (exchange.response.status >= 100 && exchange.response.status <= 400) {
                            totalHttpSuccess++;
                            httpSuccesses.push({x: timestamp, y: totalHttpSuccess});
                            totalHttpSuccessPerUrl++;
                            httpSuccessesPerUrl.push({x: url, y: totalHttpSuccessPerUrl});
                        } else {
                            totalHttpFailure++;
                            httpFailures.push({x: timestamp, y: totalHttpFailure});
                            totalHttpFailurePerUrl++;
                            httpFailuresPerUrl.push({x: url, y: totalHttpFailurePerUrl});
                        }
                    }
                }
                httpRequestResponsesChart.data.datasets[0].data = httpSuccesses;
                httpRequestResponsesChart.data.datasets[0].label = "Success";
                httpRequestResponsesChart.data.datasets[1].data = httpFailures;
                httpRequestResponsesChart.data.datasets[1].label = "Failure";
                httpRequestResponsesChart.update();

                httpRequestsByUrlChart.data.datasets[0].data = httpSuccessesPerUrl;
                httpRequestsByUrlChart.data.datasets[0].label = "Success";
                httpRequestsByUrlChart.data.datasets[1].data = httpFailuresPerUrl;
                httpRequestsByUrlChart.data.datasets[1].label = "Failure";
                httpRequestsByUrlChart.update();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });

            $("#downloadHeapDumpButton").off().on("click", () => {
                $("#downloadHeapDumpButton").prop("disabled", true);
                fetch(actuatorEndpoints.heapdump)
                    .then(response =>
                        response.blob().then(blob => {
                            const link = document.createElement("a");
                            link.href = window.URL.createObjectURL(blob);
                            link.download = "heapdump";
                            document.body.appendChild(link);
                            link.click();
                            document.body.removeChild(link);
                            $("#downloadHeapDumpButton").prop("disabled", false);
                        }))
                    .catch(error => {
                        console.error("Error fetching file:", error);
                        $("#downloadHeapDumpButton").prop("disabled", false);
                    });
            });
        }
    }

    async function configureHealthChart() {
        function updateHealthChart(response) {
            if (response.components !== undefined) {
                const payload = {
                    labels: [],
                    data: [],
                    colors: []
                };
                Object.keys(response.components).forEach(key => {
                    payload.labels.push(camelcaseToTitleCase(key));
                    payload.data.push(1);
                    payload.colors.push(response.components[key].status === "UP" ? "rgb(5, 166, 31)" : "rgba(166, 45, 15)");
                });
                systemHealthChart.data.labels = payload.labels;
                systemHealthChart.data.datasets[0].data = payload.data;
                systemHealthChart.data.datasets[0].backgroundColor = payload.colors;
                systemHealthChart.data.datasets[0].borderColor = payload.colors;
                systemHealthChart.options.plugins.legend.labels.generateLabels = (chart => {
                    const originalLabels = Chart.defaults.plugins.legend.labels.generateLabels(chart);
                    originalLabels.forEach(label => {
                        label.fillStyle = response.status === "UP" ? "rgb(5, 166, 31)" : "rgba(166, 45, 15)";
                        label.lineWidth = 0;
                    });
                    return originalLabels;
                });
                systemHealthChart.update();
            }
        }

        if (actuatorEndpoints.health) {
            $.ajax({
                url: actuatorEndpoints.health,
                method: "GET",
                timeout: 5000,
                success: function (response) {
                    updateHealthChart(response);
                },
                error: function (xhr, textStatus, errorThrown) {
                    if (xhr.status === 503) {
                        const response = xhr.responseJSON;
                        updateHealthChart(response);
                    } else {
                        console.error("Error fetching health data:", errorThrown);
                        displayBanner(xhr);
                    }
                }
            });
        }
    }

    async function configureStatistics() {
        if (actuatorEndpoints.statistics) {
            $.get(actuatorEndpoints.statistics, response => {

                const expired = response.expiredTickets;
                const valid = response.validTickets;
                statisticsChart.data.datasets[0].data = [valid, expired];
                statisticsChart.update();
            }).fail((xhr, status, error) => console.error("Error fetching data:", error));
        }
    }

    async function fetchSystemData(callback) {
        if (actuatorEndpoints.info) {
            $.get(actuatorEndpoints.info, response => callback(response)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    }

    async function configureSystemMetrics() {
        $("#systemMetricNameFilter").selectmenu({
            change: function () {
                $(this).selectmenu("close");
                const metric = $(this).val();
                systemMetricsTagsTable.clear();
                systemMetricsMeasurementsTable.clear();
                $("#systemMetricNameDescriptionContainer").hide();

                if (metric && metric.length > 0) {
                    $.get(`${actuatorEndpoints.metrics}/${metric}`, response => {
                        let description = `${response.description ?? "No description is available"}. Metric is measured in ${response.baseUnit ?? "unknown units"}.`;
                        $("#systemMetricNameDescription").text(description);
                        $("#systemMetricNameDescriptionContainer").show();

                        response.availableTags.forEach(entry => {
                            systemMetricsTagsTable.row.add({
                                0: `<code>${entry.tag}</code>`,
                                1: `<code>${entry.values.join(",")}</code>`
                            });
                        });
                        systemMetricsTagsTable.draw();
                        response.measurements.forEach(entry => {
                            systemMetricsMeasurementsTable.row.add({
                                0: `<code>${entry.statistic}</code>`,
                                1: `<code>${entry.value}</code>`
                            });
                        });
                        systemMetricsMeasurementsTable.draw();
                    });
                }
            }
        });

        $("#systemMetricNameFilter").empty();
        $("#systemMetricNameFilter").append(
            $("<option>", {
                value: "",
                text: "Select a metric to view details..."
            })
        );

        if (actuatorEndpoints.metrics) {
            $.get(actuatorEndpoints.metrics, response => {
                for (const name of response.names) {
                    $("#systemMetricNameFilter").append(
                        $("<option>", {
                            value: name,
                            text: name
                        })
                    );
                    $("#systemMetricNameFilter").selectmenu("refresh");
                }
            });
        }
    }

    async function configureSystemData() {
        await fetchSystemData(response => {

            const maximum = convertMemoryToGB(response.systemInfo["JVM Maximum Memory"]);
            const free = convertMemoryToGB(response.systemInfo["JVM Free Memory"]);
            const total = convertMemoryToGB(response.systemInfo["JVM Total Memory"]);

            memoryChart.data.datasets[0].data = [maximum, total, free];
            memoryChart.update();
        });

        if (actuatorEndpoints.metrics) {
            $.get(`${actuatorEndpoints.metrics}/http.server.requests`, response => {
                let count = response.measurements[0].value;
                let totalTime = response.measurements[1].value.toFixed(2);
                let maxTime = response.measurements[2].value.toFixed(2);
                $("#httpRequestsCount").text(count);
                $("#httpRequestsTotalTime").text(`${totalTime}s`);
                $("#httpRequestsMaxTime").text(`${maxTime}s`);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
            $.get(`${actuatorEndpoints.metrics}/http.server.requests.active`, response => {
                let active = response.measurements[0].value;
                let duration = response.measurements[1].value.toFixed(2);
                $("#httpRequestsActive").text(active);
                $("#httpRequestsDuration").text(`${duration}s`);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
        configureHttpRequestResponses().then(configureAuditEventsChart());
    }

    const systemTable = $("#systemTable").DataTable({
        pageLength: 10,
        drawCallback: settings => {
            $("#systemTable tr").addClass("mdc-data-table__row");
            $("#systemTable td").addClass("mdc-data-table__cell");
        }
    });

    const systemMetricsTagsTable = $("#systemMetricsTagsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "50%", targets: 0},
            {width: "50%", targets: 1}
        ],
        drawCallback: settings => {
            $("#systemMetricsTagsTable tr").addClass("mdc-data-table__row");
            $("#systemMetricsTagsTable td").addClass("mdc-data-table__cell");
        }
    });

    const systemMetricsMeasurementsTable = $("#systemMetricsMeasurementsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "50%", targets: 0},
            {width: "50%", targets: 1}
        ],
        drawCallback: settings => {
            $("#systemMetricsMeasurementsTable tr").addClass("mdc-data-table__row");
            $("#systemMetricsMeasurementsTable td").addClass("mdc-data-table__cell");
        }
    });

    let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));

    async function configureSystemInfo() {
        await fetchSystemData(response => {
            const flattened = flattenJSON(response);
            systemTable.clear();

            for (const [key, value] of Object.entries(flattened)) {
                systemTable.row.add({
                    0: `<code>${key}</code>`,
                    1: `<code>${value}</code>`
                });
            }
            systemTable.draw();

            highlightElements();
            $("#casServerPrefix").text(casServerPrefix);
            $("#casServerHost").text(response.server["host"]);
        });
    }

    tabs.listen("MDCTabBar:activated", ev => {
        let index = ev.detail.index;
        if (index === Tabs.SYSTEM) {
            configureSystemInfo();
        }
    });


    setInterval(() => {
        if (currentActiveTab === Tabs.SYSTEM) {
            configureSystemData();
            configureHealthChart();
            configureStatistics();
        }
    }, palantirSettings().refreshInterval);

    await configureSystemData()
        .then(configureStatistics())
        .then(configureHealthChart())
        .then(configureSystemInfo())
        .then(configureSystemMetrics());

    $("button[name=shutdownServerButton]").off().on("click", function () {
        Swal.fire({
            title: "Are you sure you want to shut the server down?",
            text: "Once confirmed, the server will begin shutdown procedures. Note that this operation does not support clustered deployments.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: actuatorEndpoints.shutdown,
                        type: "POST",
                        headers: {"Content-Type": "application/json"},
                        success: response => {
                            Swal.fire("Shutting Down...", "CAS will start to shutdown shortly. You may close this window.", "info");
                        },
                        error: (xhr, status, error) => {
                            console.error("Error deleting resource:", error);
                            displayBanner(xhr);
                        }
                    });
                }
            });
    });

    $("button[name=restartServerButton]").off().on("click", function () {
        Swal.fire({
            title: "Are you sure you want to restart the server?",
            text: "Once confirmed, the server will begin restarting. Note that this operation does not support clustered deployments.",
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: actuatorEndpoints.restart,
                        type: "POST",
                        headers: {"Content-Type": "application/json"},
                        success: response => {
                            Swal.fire({
                                icon: "info",
                                title: `Restarting CAS`,
                                text: "Please wait while the CAS server is restarting...",
                                allowOutsideClick: false,
                                showConfirmButton: false,
                                didOpen: () => Swal.showLoading()
                            });
                            waitForActuator(actuatorEndpoints.info).then(function () {
                                Swal.close();
                            });
                        },
                        error: (xhr, status, error) => {
                            console.error("Error deleting resource:", error);
                            displayBanner(xhr);
                        }
                    });
                }
            });
    });
}

async function initializeCasFeatures() {
    return new Promise(async (resolve, reject) => {
        const features = await fetchCasFeatures();
        $("#casFeaturesChipset").empty();
        for (const featureName of features) {
            let feature = `
                            <div class="mdc-chip" role="row">
                                <div class="mdc-chip__ripple"></div>
                                <span role="gridcell">
                                  <span class="mdc-chip__text">${featureName}</span>
                                </span>
                            </div>
                        `.trim();
            $("#casFeaturesChipset").append($(feature));
        }
        resolve(CAS_FEATURES);
    });
}

async function initializeServiceButtons() {
    const serviceEditor = initializeAceEditor("serviceEditor");
    let editServiceDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("editServiceDialog"));

    if (actuatorEndpoints.registeredservices) {
        const entityHistoryTable = $("#entityHistoryTable").DataTable();
        $("button[name=viewEntityHistory]").off().on("click", function () {
            let serviceId = $(this).parent().attr("serviceId");
            $.get(`${actuatorEndpoints.entityhistory}/registeredServices/${serviceId}`, response => {
                entityHistoryTable.clear();

                const editor = initializeAceEditor("entityHistoryEditor", "json");
                editor.setValue("");
                editor.setReadOnly(true);

                for (const item of response) {
                    entityHistoryTable.row.add({
                        0: `<code>${item.id}</code>`,
                        1: `<code>${item.date}</code>`,
                        2: `${JSON.stringify(item.entity, null, 4)}`
                    });
                }

                entityHistoryTable.draw();
                entityHistoryTable.on("click", "tbody tr", function () {
                    let data = entityHistoryTable.row(this).data();
                    editor.setValue(data[2]);
                    editor.gotoLine(1);
                    editor.setReadOnly(true);
                });

                if (response.length > 0) {
                    const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("viewEntityHistoryDialog"));
                    dialog["open"]();
                } else {
                    Swal.fire("No History!", "There are no changes recorded for this application definition.", "info");
                }

            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });

        });

    }

    $("button[name=viewEntityChangelog]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");

        $.get(`${actuatorEndpoints.entityhistory}/registeredServices/${serviceId}/changelog`, response => {
            const editor = initializeAceEditor("entityChangelogEditor", "text");
            editor.setValue(response);
            editor.setReadOnly(true);
            editor.gotoLine(1);
            const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("viewEntityChangelogDialog"));
            dialog["open"]();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    });

    $("button[name=deleteService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
        if (actuatorEndpoints.registeredservices) {
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
                            url: `${actuatorEndpoints.registeredservices}/${serviceId}`,
                            type: "DELETE",
                            headers: {"Content-Type": "application/json"},
                            success: response => {
                                let nearestTr = $(this).closest("tr");
                                let applicationsTable = $("#applicationsTable").DataTable();
                                applicationsTable.row(nearestTr).remove().draw();
                            },
                            error: (xhr, status, error) => {
                                console.error("Error deleting resource:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=editService]").off().on("click", function () {
        let serviceId = $(this).parent().attr("serviceId");
        if (actuatorEndpoints.registeredservices) {
            $.get(`${actuatorEndpoints.registeredservices}/${serviceId}`, response => {
                const value = JSON.stringify(response, null, 4);
                serviceEditor.setValue(value, -1);
                serviceEditor.gotoLine(1);
                const editServiceDialogElement = document.getElementById("editServiceDialog");
                $(editServiceDialogElement).attr("newService", false);
                editServiceDialog["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    });

    $("button[name=saveService],button[name=saveServiceWizard]").off().on("click", function () {
        if (actuatorEndpoints.registeredservices) {
            let isNewService = false;
            let value = "";

            const saveButton = $(this);
            switch (saveButton.attr("name")) {
            case "saveServiceWizard":
                isNewService = true;
                const wizardEditor = initializeAceEditor("wizardServiceEditor");
                value = wizardEditor.getValue();
                break;
            case "saveService":
                const editServiceDialogElement = document.getElementById("editServiceDialog");
                isNewService = $(editServiceDialogElement).attr("newService") === "true";
                value = serviceEditor.getValue();
                break;
            }

            Swal.fire({
                title: `Are you sure you want to ${isNewService ? "create" : "update"} this entry?`,
                text: "Once updated, you may not be able to revert this entry.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${actuatorEndpoints.registeredservices}`,
                            type: isNewService ? "POST" : "PUT",
                            contentType: "application/json",
                            data: value,
                            success: async response => {
                                editServiceDialog["close"]();
                                await fetchServices(() => {
                                    let newServiceId = response.id;
                                    $("#applicationsTable tr").removeClass("selected");

                                    $(`#applicationsTable tr td span[serviceId=${newServiceId}]`).each(function () {
                                        $(this).closest("tr").addClass("selected");
                                    });
                                    updateNavigationSidebar();
                                });
                            },
                            error: (xhr, status, error) => {
                                console.error("Update failed:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=copyService]").off().on("click", function () {
        if (actuatorEndpoints.registeredservices) {
            let serviceId = $(this).parent().attr("serviceId");
            $.get(`${actuatorEndpoints.registeredservices}/${serviceId}`, response => {
                let clone = {...response};
                clone.serviceId = "...";
                clone.name = `...`;
                delete clone.id;

                ["clientId", "clientSecret", "metadataLocation"].forEach(entry => {
                    if (Object.hasOwn(clone, entry)) {
                        clone[entry] = `...`;
                    }
                });
                const value = JSON.stringify(clone, null, 4);
                serviceEditor.setValue(value, -1);
                serviceEditor.gotoLine(1);
                serviceEditor.findAll("...", {regExp: false});

                const editServiceDialogElement = document.getElementById("editServiceDialog");
                $(editServiceDialogElement).attr("newService", true);
                editServiceDialog["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    });
}

async function initializeServicesOperations() {
    const columnDefinitions = [
        {visible: true, targets: 0},
        {visible: true, targets: 1},
        {visible: true, targets: 2},
        {visible: true, targets: 3},
        {visible: true, targets: 4},
        {visible: false, targets: 5}
    ];

    const applicationsTable = $("#applicationsTable").DataTable({
        pageLength: 25,
        autoWidth: false,
        columnDefs: columnDefinitions,
        drawCallback: settings => {
            $("#applicationsTable tr").addClass("mdc-data-table__row");
            $("#applicationsTable td").addClass("mdc-data-table__cell");
        }
    });
    applicationsTable.on("click", "tbody tr", e => e.currentTarget.classList.remove("selected"));

    const saml2MetadataProvidersTable = $("#saml2MetadataProvidersTable").DataTable({
        pageLength: 25,
        autoWidth: false,
        columnDefs: [
            {width: "15%", targets: 0},
            {width: "35%", targets: 1},
            {width: "50%", targets: 2}
        ],
        drawCallback: settings => {
            $("#saml2MetadataProvidersTable tr").addClass("mdc-data-table__row");
            $("#saml2MetadataProvidersTable td").addClass("mdc-data-table__cell");
        }
    });
    saml2MetadataProvidersTable.on("click", "tbody tr", e => e.currentTarget.classList.remove("selected"));

    $("#entityHistoryTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "5%", targets: 0},
            {width: "25%", targets: 1},
            {visible: false, targets: 2}
        ],
        drawCallback: settings => {
            $("#entityHistoryTable tr").addClass("mdc-data-table__row");
            $("#entityHistoryTable td").addClass("mdc-data-table__cell");
        }
    });

    await fetchServices();
    await initializeFooterButtons();

    let serviceDefinitionsEntries = "";
    for (let type in serviceDefinitions) {
        serviceDefinitionsEntries += `<h3>${type} Templates</h3>`;
        const entries = serviceDefinitions[type];
        serviceDefinitionsEntries += "<div>";
        for (const entry of entries) {
            const definition = JSON.parse(entry);
            serviceDefinitionsEntries += `<a class="mr-4" name="serviceDefinitionEntry" title="${definition.description}" data-type="${type}" data-id="${definition.id}" href="#">${definition.name}-${definition.id}</a>`;
        }
        serviceDefinitionsEntries += "</div>";
    }
    $("#serviceTemplatesContainer")
        .html(serviceDefinitionsEntries)
        .accordion({
            collapsible: true,
            heightStyle: "content"
        })
        .tooltip();

    $("a[name=serviceDefinitionEntry]").off().on("click", function () {
        let type = $(this).data("type");
        let id = $(this).data("id");

        const entries = serviceDefinitions[type];
        for (const entry of entries) {
            const definition = JSON.parse(entry);
            if (definition.id === id) {
                const serviceEditor = initializeAceEditor("serviceEditor", "json");
                serviceEditor.setReadOnly(false);
                serviceEditor.setValue(JSON.stringify(definition, null, 2));
                serviceEditor.gotoLine(1);
            }
        }
    });

    if (Object.entries(serviceDefinitionsEntries).length > 0) {
        $("#serviceTemplatesContainer").show();
    } else {
        $("#serviceTemplatesContainer").hide();
    }
}

async function initializeAllCharts() {
    threadDumpChart = new Chart(document.getElementById("threadDumpChart").getContext("2d"), {
        type: "bar",
        options: {
            responsive: true,
            fill: true,
            borderWidth: 2,
            plugins: {
                legend: {
                    position: "top"
                },
                title: {
                    display: true
                }
            }
        }
    });
    threadDumpChart.update();

    servicesChart = new Chart(document.getElementById("servicesChart").getContext("2d"), {
        type: "pie",
        data: {
            labels: [
                "CAS",
                "SAML2",
                "OAuth",
                "OpenID Connect",
                "Ws-Federation"
            ],
            datasets: [{
                label: "Registered Services",
                data: [0, 0, 0, 0, 0],
                backgroundColor: [
                    "deepskyblue",
                    "indianred",
                    "mediumpurple",
                    "limegreen",
                    "slategrey"
                ],
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: "top"
                },
                title: {
                    display: true
                }
            }
        }
    });
    servicesChart.update();

    memoryChart = new Chart(document.getElementById("memoryChart").getContext("2d"), {
        type: "bar",
        data: {
            labels: ["Total Memory", "Free Memory", "Used Memory"],
            datasets: [{
                label: "Memory (GB)",
                data: [0, 0, 0],
                fill: true,
                backgroundColor: ["rgba(54, 162, 235, 0.2)", "rgba(75, 192, 192, 0.2)", "rgba(255, 99, 132, 0.2)"],
                borderColor: ["rgba(54, 162, 235, 1)", "rgba(75, 192, 192, 1)", "rgba(255, 99, 132, 1)"],
                borderWidth: 2
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                    stacked: true
                },
                x: {
                    stacked: true,
                    grid: {
                        offset: true
                    }
                }
            }
        }
    });

    statisticsChart = new Chart(document.getElementById("statisticsChart").getContext("2d"), {
        type: "bar",
        data: {
            labels: ["Current Tickets", "Expired (Removed) Tickets"],
            datasets: [{
                label: "Ticket Registry",
                data: [0, 0],
                fill: true,
                backgroundColor: ["rgba(75, 192, 192, 0.2)", "rgba(255, 99, 132, 0.2)"],
                borderColor: ["rgba(75, 192, 192, 1)", "rgba(255, 99, 132, 1)"],
                borderWidth: 2
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                    stacked: true,
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    stacked: true,
                    grid: {
                        offset: true
                    }
                }
            }
        }
    });
    auditEventsChart = new Chart(document.getElementById("auditEventsChart").getContext("2d"), {
        type: "bar",
        options: {
            plugins: {
                title: {
                    display: true,
                    text: "Audit Events"
                }
            }
        }
    });
    auditEventsChart.update();

    httpRequestResponsesChart = new Chart(document.getElementById("httpRequestResponsesChart").getContext("2d"), {
        type: "bar",
        data: {
            datasets: [
                {
                    data: [{x: "N/A", y: 0}, {x: "N/A", y: 0}],
                    label: "Success",
                    borderWidth: 2
                },
                {
                    data: [{x: "N/A", y: 0}, {x: "N/A", y: 0}],
                    label: "Failure",
                    borderWidth: 2
                }
            ]
        },
        options: {
            plugins: {
                title: {
                    display: true,
                    text: "HTTP Requests/Responses (Date)"
                }
            }
        }
    });
    httpRequestResponsesChart.update();

    httpRequestsByUrlChart = new Chart(document.getElementById("httpRequestsByUrlChart").getContext("2d"), {
        type: "bar",
        data: {
            datasets: [
                {
                    data: [{x: "N/A", y: 0}, {x: "N/A", y: 0}],
                    label: "Success",
                    borderWidth: 2
                },
                {
                    data: [{x: "N/A", y: 0}, {x: "N/A", y: 0}],
                    label: "Failure",
                    borderWidth: 2
                }
            ]
        },
        options: {
            plugins: {
                title: {
                    display: true,
                    text: "HTTP Requests/Responses (URL)"
                }
            }
        }
    });
    httpRequestsByUrlChart.update();

    systemHealthChart = new Chart(document.getElementById("systemHealthChart").getContext("2d"), {
        type: "bar",
        data: {
            datasets: [{
                label: "System Health",
                fill: true,
                borderWidth: 2
            }]
        },
        options: {
            indexAxis: "y",
            scales: {
                x: {
                    ticks: {
                        display: false
                    }
                }
            }
        }
    });

    jvmThreadsChart = new Chart(document.getElementById("jvmThreadsChart").getContext("2d"), {
        type: "bar",
        data: {
            labels: ["Daemon", "Live", "Peak", "Started", "States"],
            datasets: [{
                label: "JVM Thread Types",
                data: [0, 0, 0, 0, 0],
                fill: true,
                borderWidth: 2
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                    stacked: true,
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    stacked: true,
                    grid: {
                        offset: true
                    }
                }
            }
        }
    });

}

async function updateNavigationSidebar() {
    $("nav.sidebar-navigation").css("height", $("#dashboard .mdc-card").css("height"));
}

async function initializePersonDirectoryOperations() {
    const personDirectoryTable = $("#personDirectoryTable").DataTable({
        pageLength: 10,
        drawCallback: settings => {
            $("#personDirectoryTable tr").addClass("mdc-data-table__row");
            $("#personDirectoryTable td").addClass("mdc-data-table__cell");
        }
    });

    const attributeDefinitionsTable = $("#attributeDefinitionsTable").DataTable({
        pageLength: 10,
        drawCallback: settings => {
            $("#attributeDefinitionsTable tr").addClass("mdc-data-table__row");
            $("#attributeDefinitionsTable td").addClass("mdc-data-table__cell");
        }
    });

    const attributeRepositoriesTable = $("#attributeRepositoriesTable").DataTable({
        pageLength: 10,
        drawCallback: settings => {
            $("#attributeRepositoriesTable tr").addClass("mdc-data-table__row");
            $("#attributeRepositoriesTable td").addClass("mdc-data-table__cell");
        }
    });

    $("button[name=personDirectoryClearButton]").off().on("click", () => {
        if (actuatorEndpoints.persondirectory) {
            const form = document.getElementById("fmPersonDirectory");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#personUsername").val();
            Swal.fire({
                title: `Are you sure you want to delete the cache for ${username}?`,
                text: `Once the cached entry is removed, attribute repositories would be forced to fetch attributes for ${username} again`,
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        personDirectoryTable.clear();
                        $.ajax({
                            url: `${actuatorEndpoints.persondirectory}/cache/${username}`,
                            type: "DELETE",
                            contentType: "application/json",
                            success: (response, status, xhr) => {

                            },
                            error: (xhr, status, error) => {
                                console.error("Error fetching data:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    $("button[name=personDirectoryButton]").off().on("click", () => {
        if (actuatorEndpoints.persondirectory) {
            const form = document.getElementById("fmPersonDirectory");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#personUsername").val();
            personDirectoryTable.clear();
            $.ajax({
                url: `${actuatorEndpoints.persondirectory}/cache/${username}`,
                type: "GET",
                contentType: "application/json",
                success: (response, status, xhr) => {


                    for (const [key, values] of Object.entries(response.attributes)) {
                        personDirectoryTable.row.add({
                            0: `<code>${key}</code>`,
                            1: `<code>${values}</code>`
                        });
                    }
                    personDirectoryTable.draw();
                },
                error: (xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    displayBanner(xhr);
                }
            });
        }
    });

    attributeDefinitionsTable.clear();
    let attributeDefinitions = 0;
    if (actuatorEndpoints.attributeDefinitions) {
        $.get(actuatorEndpoints.attributeDefinitions, response => {
            for (const definition of response) {
                attributeDefinitionsTable.row.add({
                    0: `<code>${definition.key ?? "N/A"}</code>`,
                    1: `<code>${definition.name ?? "N/A"}</code>`,
                    2: `<code>${definition.scoped ?? "false"}</code>`,
                    3: `<code>${definition.encrypted ?? "false"}</code>`,
                    4: `<code>${definition.singleValue ?? "false"}</code>`,
                    5: `<code>${definition.attribute ?? "N/A"}</code>`,
                    6: `<code>${definition.patternFormat ?? "N/A"}</code>`,
                    7: `<code>${definition.canonicalizationMode ?? "N/A"}</code>`,
                    8: `<code>${definition.flattened ?? "false"}</code>`,
                    9: `<code>${definition?.friendlyName ?? "N/A"}</code>`,
                    10: `<code>${definition?.urn ?? "N/A"}</code>`
                });
                attributeDefinitions++;
            }
            attributeDefinitionsTable.draw();
            $("#attributeDefinitionsTab").toggle(attributeDefinitions > 0);
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    attributeRepositoriesTable.clear();
    let attributeRepositories = 0;
    if (actuatorEndpoints.persondirectory) {
        $.get(`${actuatorEndpoints.persondirectory}/repositories`, response => {
            for (const definition of response) {
                attributeRepositoriesTable.row.add({
                    0: `<code>${definition.id ?? "N/A"}</code>`,
                    1: `<code>${definition.order ?? "0"}</code>`,
                    2: `<code>${JSON.stringify(definition.tags)}</code>`
                });
                attributeRepositories++;
            }
            attributeRepositoriesTable.draw();
            $("#attributeRepositoriesTab").toggle(attributeRepositories > 0);
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }
}

async function initializeAuthenticationOperations() {
    const authenticationHandlersTable = $("#authenticationHandlersTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0},
            {width: "80%", targets: 1},
            {width: "10%", targets: 2},
            {width: "10%", targets: 3}
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

    function configureSaml2ClientMetadataButtons() {
        async function showSamlMetadata(payload) {
            const saml2Editor = initializeAceEditor("delegatedClientsSaml2Editor", "xml");
            saml2Editor.setReadOnly(true);

            saml2Editor.setValue(new XMLSerializer().serializeToString(payload));
            saml2Editor.gotoLine(1);

            const beautify = ace.require("ace/ext/beautify");
            beautify.beautify(saml2Editor.session);

            const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("delegatedClientsSaml2Dialog"));
            dialog["open"]();
        }

        $("button[name=saml2ClientSpMetadata]").off().on("click", function () {
            $(this).prop("disabled", true);
            const url = `${casServerPrefix}/sp/${$(this).attr("clientName")}/metadata`;
            $.get(url, payload => showSamlMetadata(payload))
                .always(() => $(this).prop("disabled", false));

        });
        $("button[name=saml2ClientIdpMetadata]").off().on("click", function () {
            $(this).prop("disabled", true);
            const clientName = `${$(this).attr("clientName")}`;
            const url = `${casServerPrefix}/sp/${clientName}/idp/metadata`;

            Swal.fire({
                icon: "info",
                title: `Fetching SAML2 Identity Provider Metadata for ${clientName}`,
                text: "Please wait while data is being retrieved...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });

            $.get(url, async payload => {
                await showSamlMetadata(payload);
                await updateNavigationSidebar();
                Swal.close();
            })
                .always(() => $(this).prop("disabled", false));
        });
    }

    authenticationHandlersTable.clear();
    if (actuatorEndpoints.authenticationHandlers) {
        $.get(actuatorEndpoints.authenticationHandlers, response => {
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
        columnDefs: [
            {width: "80%", targets: 0},
            {width: "20%", targets: 1}
        ],
        drawCallback: settings => {
            $("#authenticationPoliciesTable tr").addClass("mdc-data-table__row");
            $("#authenticationPoliciesTable td").addClass("mdc-data-table__cell");
        }
    });

    authenticationPoliciesTable.clear();
    if (actuatorEndpoints.authenticationPolicies) {
        $.get(actuatorEndpoints.authenticationPolicies, response => {
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
    let toolbarEntries = "";

    if (mutablePropertySourcesAvailable && actuatorEndpoints.casconfig) {
        toolbarEntries += `
            <button type="button" id="newExternalIdentityProvider"
                    onclick="newExternalIdentityProvider()"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>New</span>
            </button>
        `;
    }

    toolbar.innerHTML = toolbarEntries;
    const delegatedClientsTable = $("#delegatedClientsTable").DataTable({
        pageLength: 10,
        order: [0, "asc"],
        autoWidth: false,
        layout: {
            topStart: toolbar
        },
        columnDefs: [
            {visible: false, targets: 0},
            {width: "40%", targets: 1},
            {width: "60%", targets: 2},
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
                        rows.data().each(entry => {
                            if (entry[0] === group && entry[3] === "saml2") {
                                samlButtons = `
                                    <span class="px-2">
                                            <button type="button" title="Service Provider Metadata" name="saml2ClientSpMetadata" href="#" clientName='${group}'
                                                    class="mdc-button mdc-button--raised toolbar">
                                                <i class="mdi mdi-text-box min-width-32x" aria-hidden="true"></i>
                                                Service Provider Metadata
                                            </button>
                                            <button type="button" title="Identity Provider Metadata" name="saml2ClientIdpMetadata" href="#" clientName='${group}'
                                                    class="mdc-button mdc-button--raised toolbar">
                                                <i class="mdi mdi-file-xml-box min-width-32x" aria-hidden="true"></i>
                                                Identity Provider Metadata
                                            </button>
                                    </span>
                                    `.trim();
                            }
                        });
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                <td colspan="3">${group} ${samlButtons.trim()} </td>
                            </tr>`.trim()
                        );
                        configureSaml2ClientMetadataButtons();
                        last = group;
                    }
                });
        }
    });

    delegatedClientsTable.clear();
    if (actuatorEndpoints.delegatedClients) {
        $.get(actuatorEndpoints.delegatedClients, response => {
            for (const [key, idp] of Object.entries(response)) {
                const details = flattenJSON(idp);
                for (const [k, v] of Object.entries(details)) {
                    if (Object.keys(v).length > 0 && k !== "type") {
                        delegatedClientsTable.row.add({
                            0: `${key}`,
                            1: `<code>${toKebabCase(k)}</code>`,
                            2: `<code>${v}</code>`,
                            3: `${idp.type}`
                        });
                    }
                }
            }
            delegatedClientsTable.draw();
            $("#delegatedClientsContainer").removeClass("d-none");
            $("#delegatedclients").removeClass("d-none");
            updateNavigationSidebar();

            configureSaml2ClientMetadataButtons();

        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            $("#delegatedClientsContainer").addClass("d-none");
            $("#delegatedclients").addClass("d-none");
        });
    } else {
        $("#delegatedClientsContainer").addClass("d-none");
        $("#delegatedclients").addClass("d-none");
    }
}

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
                 <button type="button" name="viewConsentAttributes" href="#" consentId='${source.decision.id}'
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                    <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                    <span class="d-none">${JSON.stringify(source.attributes)}</span>
                </button>
                <button type="button" name="deleteConsent" href="#" 
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

async function initializeConfigurationOperations() {
    const configurationTable = $("#configurationTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0}
        ],
        order: [0, "asc"],
        drawCallback: settings => {
            $("#configurationTable tr").addClass("mdc-data-table__row");
            $("#configurationTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                <td colspan="3">${group}</td>
                            </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    const toolbar = document.createElement("div");
    let toolbarEntries = `
            <button type="button" id="reloadConfigurationTableButton" 
                    onclick="$.get(actuatorEndpoints.env, res => { reloadConfigurationTable(res); }).fail((xhr) => { displayBanner(xhr); });"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-database-arrow-down" aria-hidden="true"></i>Reload</span>
            </button>
    `;
    if (mutablePropertySourcesAvailable) {
        toolbarEntries += `
            <button type="button" onclick="createNewConfigurationProperty(this);" id="newConfigPropertyButton" class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus" aria-hidden="true"></i>New Property</span>
            </button>
            <button type="button" onclick="deleteAllConfigurationProperties(this);" id="deleteAllConfigurationPropertiesButton" class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-broom" aria-hidden="true"></i>Delete All</span>
            </button>
            <button type="button" onclick="importConfigurationProperties(this);" id="importConfigurationPropertiesButton" class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-file-import" aria-hidden="true"></i>Import</span>
            </button>
            <button type="button" id="refreshConfigurationButton"
                    onclick="refreshCasServerConfiguration('Context Refresh');" 
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-refresh" aria-hidden="true"></i>Refresh CAS</span>
            </button>
        `;
    }

    toolbar.innerHTML = toolbarEntries;
    const mutableConfigurationTable = $("#mutableConfigurationTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        layout: {
            topStart: toolbar
        },
        columnDefs: [
            {visible: false, targets: 0}
        ],
        order: [0, "asc"],
        drawCallback: settings => {
            $("#mutableConfigurationTable tr").addClass("mdc-data-table__row");
            $("#mutableConfigurationTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                <td colspan="3">${group}</td>
                            </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    function encryptOrDecryptConfig(op) {
        hideBanner();
        $("#configEncryptionResult").addClass("d-none");

        const form = document.getElementById("fmConfigEncryption");
        if (!form.reportValidity()) {
            return false;
        }
        const configValue = $("#configValue").val();
        if (actuatorEndpoints.casconfig) {
            $.post({
                url: `${actuatorEndpoints.casconfig}/${op}`,
                data: configValue,
                contentType: "text/plain"
            }, data => {
                $("#configEncryptionResult pre code").text(data);
                highlightElements();
                $("#configEncryptionResult").removeClass("d-none");
            }).fail((xhr, status, error) => {
                displayBanner(xhr);
                $("#configEncryptionResult").addClass("d-none");
            });
        }
    }

    configurationTable.clear();
    mutableConfigurationTable.clear();

    if (actuatorEndpoints.env) {
        $.get(actuatorEndpoints.env, response => {
            reloadConfigurationTable(response);

            $("#casActiveProfiles").empty();
            for (const element of response.activeProfiles) {
                let feature = `
                <div class="mdc-chip" role="row">
                    <div class="mdc-chip__ripple"></div>
                    <span role="gridcell">
                        <i class="mdi mdi-wrench" aria-hidden="true"></i>
                      <span class="mdc-chip__text">${element.trim()}</span>
                    </span>
                </div>
            `.trim();
                $("#casActiveProfiles").append($(feature));
            }
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }


    const configPropsTable = $("#configPropsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0}
        ],
        order: [0, "asc"],
        drawCallback: settings => {
            $("#configPropsTable tr").addClass("mdc-data-table__row");
            $("#configPropsTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });
    configPropsTable.clear();

    if (actuatorEndpoints.configprops) {
        $.get(actuatorEndpoints.configprops, response => {
            const casBeans = response.contexts["cas-1"].beans;
            const bootstrapBeans = response.contexts["bootstrap"].beans;
            for (const [sourceBean, bean] of Object.entries(casBeans)) {
                let flattened = flattenJSON(bean.properties);
                for (const [prop, propValue] of Object.entries(flattened)) {
                    const property = `${bean.prefix}.${prop}`;
                    if (Object.keys(propValue).length > 0) {
                        configPropsTable.row.add({
                            0: `${sourceBean}`,
                            1: `<code>${property}</code>`,
                            2: `<code>${propValue}</code>`
                        });
                    }
                }
            }
            for (const [sourceBean, bean] of Object.entries(bootstrapBeans)) {
                let flattened = flattenJSON(bean.properties);
                for (const [prop, propValue] of Object.entries(flattened)) {
                    const property = `${bean.prefix}.${prop}`;
                    if (Object.keys(propValue).length > 0) {
                        configPropsTable.row.add({
                            0: `${sourceBean}`,
                            1: `<code>${toKebabCase(property)}</code>`,
                            2: `<code>${propValue}</code>`
                        });
                    }
                }
            }
            configPropsTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    $("#encryptConfigButton").off().on("click", () => encryptOrDecryptConfig("encrypt"));
    $("#decryptConfigButton").off().on("click", () => encryptOrDecryptConfig("decrypt"));

    if (actuatorEndpoints.configurationmetadata) {

        const configSearchResultsTable = $("#configSearchResultsTable").DataTable({
            pageLength: 10,
            drawCallback: settings => {
                $("#configSearchResultsTable tr").addClass("mdc-data-table__row");
                $("#configSearchResultsTable td").addClass("mdc-data-table__cell").addClass("text-wrap");
            }
        });
        configSearchResultsTable.clear();

        $("button[name=configSearchButton]").off().on("click", () => {
            $("#configSearchResults").hide();
            configSearchResultsTable.clear();

            const form = document.getElementById("fmConfigSearch");
            if (!form.reportValidity()) {
                return false;
            }
            const searchQuery = $("#configSearchQuery").val();
            Swal.fire({
                icon: "info",
                title: `Fetching Results`,
                text: "Please wait while configuration metadata repository is consulted to find matches...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });
            $.get(`${actuatorEndpoints.configurationmetadata}/${searchQuery}`, response => {
                for (const entry of response) {
                    configSearchResultsTable.row.add({
                        0: entry.id,
                        1: entry.description
                    });
                }
                $("#configSearchResults").show();
                configSearchResultsTable.draw();
            })
                .fail((xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    displayBanner(xhr);
                })
                .always(() => Swal.close());
        });
    }
}

async function initializeCasProtocolOperations() {
    function buildCasProtocolPayload(endpoint, format) {
        const form = document.getElementById("fmCasProtocol");
        if (!form.reportValidity()) {
            return false;
        }
        const username = $("#casProtocolUsername").val();
        const password = $("#casProtocolPassword").val();
        const service = $("#casProtocolService").val();

        $.post(`${actuatorEndpoints.casvalidate}/${endpoint}`, {
            username: username,
            password: password,
            service: service
        }, data => {
            const casProtocolEditor = initializeAceEditor("casProtocolEditor", format);
            casProtocolEditor.setReadOnly(true);
            casProtocolEditor.setValue(data.response);
            casProtocolEditor.gotoLine(1);

            const casProtocolServiceEditor = initializeAceEditor("casProtocolServiceEditor", "json");
            casProtocolServiceEditor.setReadOnly(true);
            casProtocolServiceEditor.setValue(JSON.stringify(data.registeredService, null, 2));
            casProtocolServiceEditor.gotoLine(1);

            $("#casProtocolEditorContainer").removeClass("d-none");
            $("#casProtocolServiceEditorContainer").removeClass("d-none");

            updateNavigationSidebar();
            $("#casProtocolServiceNavigation").off().on("click", () => navigateToApplication(data.registeredService.id));
        }).fail((xhr, status, error) => {
            displayBanner(xhr);
            $("#casProtocolEditorContainer").addClass("d-none");
            $("#casProtocolServiceEditorContainer").addClass("d-none");
        });
    }

    $("button[name=casProtocolV1Button]").off().on("click", () => buildCasProtocolPayload("validate", "text"));
    $("button[name=casProtocolV2Button]").off().on("click", () => buildCasProtocolPayload("serviceValidate", "xml"));
    $("button[name=casProtocolV3Button]").off().on("click", () => buildCasProtocolPayload("p3/serviceValidate", "xml"));
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

    $("#mfaTrustedDevicesButton").off().on("click", () => {
        if (actuatorEndpoints.multifactortrusteddevices) {
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

            $.get(`${actuatorEndpoints.multifactortrusteddevices}/${username}`, response => {
                for (const device of Object.values(response)) {
                    let buttons = `
                     <button type="button" name="removeMfaTrustedDevice" href="#" 
                            data-key='${device.recordKey}'
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                    </button>
                `;
                    mfaTrustedDevicesTable.row.add({
                        0: `<code>${device.id ?? "N/A"}</code>`,
                        1: `<code>${device.principal ?? "N/A"}</code>`,
                        2: `<code>${device.deviceFingerprint ?? "N/A"}</code>`,
                        3: `<code>${device.recordDate ?? "N/A"}</code>`,
                        4: `<code>${device.name ?? "N/A"}</code>`,
                        5: `<code>${device.expirationDate ?? "N/A"}</code>`,
                        6: `<code>${device.multifactorAuthenticationProvider ?? "N/A"}</code>`,
                        7: `<code>${device.recordKey ?? "N/A"}</code>`,
                        8: `${buttons}`
                    });
                }
                mfaTrustedDevicesTable.draw();
                $("#mfaTrustedDevicesButton").prop("disabled", false);
                Swal.close();

                $("button[name=removeMfaTrustedDevice]").off().on("click", function () {
                    const key = $(this).data("key");
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
                                    url: `${actuatorEndpoints.multifactortrusteddevices}/${key}`,
                                    type: "DELETE",
                                    contentType: "application/x-www-form-urlencoded",
                                    success: (response, status, xhr) => {
                                        let nearestTr = $(this).closest("tr");
                                        mfaTrustedDevicesTable.row(nearestTr).remove().draw();
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
                $("#mfaTrustedDevicesButton").prop("disabled", false);
                Swal.close();
            });
        }
    });

}

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
        if (currentActiveTab === Tabs.MULTITENANCY) {
            fetchTenants();
        }
    }, palantirSettings().refreshInterval);
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
            $.get(`${actuatorEndpoints.mfadevices}/${username}`, response => {
                for (const device of Object.values(response)) {
                    let buttons = `
                     <button type="button" name="removeMfaDevice" href="#" 
                            data-provider='${device?.details?.providerId ?? "Unknown"}'
                            data-key='${device.id}'
                            data-username='${username}'
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                    </button>
                `;

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
                        9: `${buttons}`
                    });
                }
                mfaDevicesTable.draw();
                $("#mfaDevicesButton").prop("disabled", false);
                Swal.close();

                $("button[name=removeMfaDevice]").off().on("click", function () {
                    const key = $(this).data("key");
                    const providerId = $(this).data("provider");
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
                                    url: `${actuatorEndpoints.mfadevices}/${username}/${providerId}/${key}`,
                                    type: "DELETE",
                                    contentType: "application/x-www-form-urlencoded",
                                    success: (response, status, xhr) => {
                                        let nearestTr = $(this).closest("tr");
                                        mfaDevicesTable.row(nearestTr).remove().draw();
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
}

async function initializeThrottlesOperations() {
    const throttlesTable = $("#throttlesTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "15%", targets: 1},
            {width: "10%", targets: 2},
            {width: "15%", targets: 3},
            {width: "10%", targets: 4},
            {width: "20%", targets: 5},
            {width: "10%", targets: 6}
        ],
        drawCallback: settings => {
            $("#throttlesTable tr").addClass("mdc-data-table__row");
            $("#throttlesTable td").addClass("mdc-data-table__cell");
        }
    });

    function fetchThrottledAttempts() {
        throttlesTable.clear();
        $.get(actuatorEndpoints.throttles, response => {
            for (const record of response) {

                let buttons = `
                     <button type="button" name="removeThrottledAttempt" href="#" 
                            data-key='${record.key}'
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                    </button>
                `;

                throttlesTable.row.add({
                    0: `<code>${record.key}</code>`,
                    1: `<code>${record.id}</code>`,
                    2: `<code>${record.value}</code>`,
                    3: `<code>${record.username}</code>`,
                    4: `<code>${record.clientIpAddress}</code>`,
                    5: `<code>${record.expiration}</code>`,
                    6: `${buttons}`
                });
            }
            throttlesTable.draw();

            $("button[name=removeThrottledAttempt]").off().on("click", function () {
                const key = $(this).data("key");
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
                                url: `${actuatorEndpoints.throttles}?key=${encodeURIComponent(key)}`,
                                type: "DELETE",
                                contentType: "application/x-www-form-urlencoded",
                                success: (response, status, xhr) => {
                                    let nearestTr = $(this).closest("tr");
                                    throttlesTable.row(nearestTr).remove().draw();
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
    }

    if (actuatorEndpoints.throttles) {
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
                            url: `${actuatorEndpoints.throttles}`,
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
                            url: `${actuatorEndpoints.throttles}`,
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

async function initializeSAML1ProtocolOperations() {
    $("button[name=saml1ProtocolButton]").off().on("click", () => {
        const form = document.getElementById("fmSaml1Protocol");
        if (!form.reportValidity()) {
            return false;
        }
        const username = $("#saml1ProtocolUsername").val();
        const password = $("#saml1ProtocolPassword").val();
        const service = $("#saml1ProtocolService").val();

        $.post(`${actuatorEndpoints.samlvalidate}`, {
            username: username,
            password: password,
            service: service
        }, data => {
            $("#saml1ProtocolEditorContainer").removeClass("d-none");
            const editor = initializeAceEditor("saml1ProtocolEditor", "xml");
            editor.setReadOnly(true);
            editor.setValue(data.assertion);
            editor.gotoLine(1);

            const serviceEditor = initializeAceEditor("saml1ProtocolServiceEditor", "json");
            serviceEditor.setReadOnly(true);
            serviceEditor.setValue(JSON.stringify(data.registeredService, null, 2));
            serviceEditor.gotoLine(1);
        }).fail((xhr, status, error) => {
            displayBanner(xhr);
            $("#saml2ProtocolEditorContainer").addClass("d-none");
        });
    });

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

function showSaml2IdPMetadata() {
    $.get(`${casServerPrefix}/idp/metadata`, response => {
        let oidcOpConfigurationDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("saml2MetadataDialog"));
        const editor = initializeAceEditor("saml2MetadataDialogEditor", "xml");
        editor.setValue(new XMLSerializer().serializeToString(response));
        editor.gotoLine(1);
        editor.setReadOnly(true);
        oidcOpConfigurationDialog["open"]();
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayBanner(xhr);
    });
}

function highlightElements() {
    document
        .querySelectorAll("pre code[data-highlighted]")
        .forEach(el => delete el.dataset.highlighted);
    hljs.highlightAll();
}

function showOidcJwks() {
    $.get(`${casServerPrefix}/oidc/jwks`, response => {
        let oidcOpConfigurationDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("oidcOpConfigurationDialog"));
        const editor = initializeAceEditor("oidcOpConfigurationDialogEditor", "json");
        editor.setValue(JSON.stringify(response, null, 2));
        editor.gotoLine(1);
        editor.setReadOnly(true);
        oidcOpConfigurationDialog["open"]();
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayBanner(xhr);
    });
}

async function initializeOidcProtocolOperations() {
    if (CAS_FEATURES.includes("OpenIDConnect")) {
        $.get(`${casServerPrefix}/oidc/.well-known/openid-configuration`, response => {
            highlightElements();
            $("#oidcIssuer").text(response.issuer);
        });

        $("button[name=oidcOpConfigurationButton]").off().on("click", () => {
            hideBanner();
            $.get(`${casServerPrefix}/oidc/.well-known/openid-configuration`, response => {
                let oidcOpConfigurationDialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("oidcOpConfigurationDialog"));
                const editor = initializeAceEditor("oidcOpConfigurationDialogEditor", "json");
                editor.setValue(JSON.stringify(response, null, 2));
                editor.gotoLine(1);
                editor.setReadOnly(true);
                oidcOpConfigurationDialog["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        });

        $("button[name=oidcKeyRotationButton]").off().on("click", () => {
            hideBanner();
            Swal.fire({
                title: "Are you sure you want to rotate keys?",
                text: "Once rotated, the change will take effect immediately.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $("#oidcKeyRotationButton").prop("disabled", true);
                        $.get(`${actuatorEndpoints.oidcjwks}/rotate`, response => {
                            Swal.fire({
                                title: "Done!",
                                text: "Keys in the OpenID Connect keystore are successfully rotated.",
                                icon: "success",
                                timer: 1000
                            });
                            $("#oidcKeyRotationButton").prop("disabled", false);
                        }).fail((xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                            $("#oidcKeyRotationButton").prop("disabled", false);
                        });
                    }
                });
        });

        $("button[name=oidcKeyRevocationButton]").off().on("click", () => {
            hideBanner();
            Swal.fire({
                title: "Are you sure you want to revoke keys?",
                text: "Once revoked, the change will take effect immediately.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((willDo) => {
                    if (willDo) {
                        $("#oidcKeyRevocationButton").prop("disabled", true);
                        $.get(`${actuatorEndpoints.oidcjwks}/revoke`, response => {
                            Swal.fire({
                                title: "Done!",
                                text: "Keys in the OpenID Connect keystore are successfully revoked.",
                                showConfirmButton: false,
                                icon: "success",
                                timer: 1000
                            });
                            $("#oidcKeyRevocationButton").prop("disabled", false);
                        }).fail((xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                            $("#oidcKeyRevocationButton").prop("disabled", false);
                        });
                    }
                });
        });

        $("button[name=oidcProtocolButton]").off().on("click", () => {
            hideBanner();
            const oidcForm = document.getElementById("fmOidcProtocol");
            if (!oidcForm.checkValidity()) {
                oidcForm.reportValidity();
                return false;
            }

            function decodeJWT(token) {
                const parts = token.split(".");
                if (parts.length === 3) {
                    const header = JSON.parse(atob(parts[0]));
                    const payload = JSON.parse(atob(parts[1]));
                    const signature = parts[2];
                    return {
                        header: header,
                        payload: payload,
                        signature: signature
                    };
                }
                return {};
            }

            $.get(`${casServerPrefix}/oidc/.well-known/openid-configuration`, oidcConfiguration => {

                const clientId = $("#oidcProtocolClientId").val();
                const clientSecret = $("#oidcProtocolClientSecret").val();
                const scopes = encodeURIComponent($("#oidcProtocolScopes").val());

                $.ajax({
                    url: `${oidcConfiguration.token_endpoint}?grant_type=client_credentials&scope=${scopes}`,
                    type: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Basic ${btoa(`${clientId}:${clientSecret}`)}`
                    },
                    success: (response, textStatus, xhr) => {

                        const oidcProtocolEditorTokens = initializeAceEditor("oidcProtocolEditorTokens", "json");
                        oidcProtocolEditorTokens.setReadOnly(true);
                        oidcProtocolEditorTokens.setValue(JSON.stringify(response, null, 2));
                        oidcProtocolEditorTokens.gotoLine(1);

                        const oidcProtocolEditorIdTokenClaims = initializeAceEditor("oidcProtocolEditorIdTokenClaims", "json");
                        oidcProtocolEditorIdTokenClaims.setReadOnly(true);
                        const idToken = response.id_token;
                        const decodedIdToken = decodeJWT(idToken);
                        oidcProtocolEditorIdTokenClaims.setValue(JSON.stringify(decodedIdToken.payload, null, 2));
                        oidcProtocolEditorIdTokenClaims.gotoLine(1);


                        const oidcProtocolEditorProfile = initializeAceEditor("oidcProtocolEditorProfile", "json");
                        oidcProtocolEditorProfile.setReadOnly(true);
                        const accessToken = response.access_token;

                        $.post(`${oidcConfiguration.userinfo_endpoint}`, {
                            access_token: accessToken
                        }, data => {
                            oidcProtocolEditorProfile.setValue(JSON.stringify(data, null, 2));
                            oidcProtocolEditorProfile.gotoLine(1);
                        }).fail((xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                        });
                        $("#oidcProtocolEditorContainer").removeClass("d-none");
                    },
                    error: (xhr, textStatus, errorThrown) => {
                        $("#oidcProtocolEditorContainer").addClass("d-none");
                        console.error("Error fetching data:", errorThrown);
                        displayBanner(xhr);
                    }
                });

            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                $("#oidcProtocolEditorContainer").addClass("d-none");
            });


        });
    }
}

async function initializeSAML2ProtocolOperations() {
    if (CAS_FEATURES.includes("SAMLIdentityProvider")) {
        if (actuatorEndpoints.info) {
            $.get(actuatorEndpoints.info, response => {
                highlightElements();
                $("#saml2EntityId").text(response.saml2.entityId);
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }

        $("button[name=saml2ProtocolPostButton]").off().on("click", () => {
            const form = document.getElementById("fmSaml2Protocol");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#saml2ProtocolUsername").val();
            const password = $("#saml2ProtocolPassword").val();
            const entityId = $("#saml2ProtocolEntityId").val();

            $.post(`${actuatorEndpoints.samlpostprofileresponse}`, {
                username: username,
                password: password,
                entityId: entityId
            }, data => {
                const editor = initializeAceEditor("saml2ProtocolEditor", "xml");
                editor.setReadOnly(true);
                editor.setValue(new XMLSerializer().serializeToString(data));
                editor.gotoLine(1);
                $("#saml2ProtocolEditorContainer").removeClass("d-none");
                $("#saml2ProtocolLogoutEditor").addClass("d-none");
            }).fail((xhr, status, error) => {
                displayBanner(xhr);
                $("#saml2ProtocolEditorContainer").addClass("d-none");
            });
        });

        $("button[name=saml2ProtocolLogoutButton]").off().on("click", () => {
            const entityId = document.getElementById("saml2ProtocolEntityId");
            if (!entityId.checkValidity()) {
                entityId.reportValidity();
                return false;
            }
            $.ajax({
                url: `${actuatorEndpoints.samlpostprofileresponse}/logout/post`,
                type: "POST",
                data: {
                    entityId: $("#saml2ProtocolEntityId").val()
                },
                success: (response, textStatus, jqXHR) => {
                    const saml2ProtocolEditor = initializeAceEditor("saml2ProtocolEditor", "html");
                    saml2ProtocolEditor.setReadOnly(true);
                    saml2ProtocolEditor.setValue(response);
                    saml2ProtocolEditor.gotoLine(1);

                    const logoutRequest = atob(jqXHR.getResponseHeader("LogoutRequest"));
                    const saml2ProtocolLogoutEditor = initializeAceEditor("saml2ProtocolLogoutEditor", "xml");
                    saml2ProtocolLogoutEditor.setReadOnly(true);
                    saml2ProtocolLogoutEditor.setValue(logoutRequest);
                    saml2ProtocolLogoutEditor.gotoLine(1);

                    $("#saml2ProtocolEditorContainer").removeClass("d-none");
                    $("#saml2ProtocolLogoutEditor").removeClass("d-none");
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    displayBanner(xhr);
                    $("#saml2ProtocolEditorContainer").addClass("d-none");
                    $("#saml2ProtocolLogoutEditor").addClass("d-none");
                }
            });
        });

        $("button[name=saml2MetadataCacheInvalidateButton]").off().on("click", () => {
            hideBanner();
            $("#saml2MetadataCacheEditorContainer").addClass("d-none");

            Swal.fire({
                title: "Are you sure you want to invalidate the cache entry?",
                text: "Once deleted, the change will take effect immediately.",
                icon: "question",
                showConfirmButton: true,
                showDenyButton: true
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${actuatorEndpoints.samlidpregisteredservicemetadatacache}`,
                            type: "DELETE",
                            data: {
                                entityId: $("#saml2MetadataCacheEntityId").val(),
                                serviceId: $("#saml2MetadataCacheService").val()
                            },
                            success: (response, textStatus, jqXHR) =>
                                Swal.fire({
                                    title: "Cached metadata record(s) removed.",
                                    text: "Cached metadata entry has been removed successfully.",
                                    showConfirmButton: false,
                                    icon: "success",
                                    timer: 1000
                                }),
                            error: (jqXHR, textStatus, errorThrown) => displayBanner(jqXHR)
                        });
                    }
                });
        });

        $("button[name=saml2MetadataCacheFetchButton]").off().on("click", function () {
            hideBanner();

            $(this).prop("disabled", true);
            $.ajax({
                url: `${actuatorEndpoints.samlidpregisteredservicemetadatacache}`,
                type: "GET",
                data: {
                    entityId: $("#saml2MetadataCacheEntityId").val(),
                    serviceId: $("#saml2MetadataCacheService").val()
                },
                success: (response, textStatus, jqXHR) => {


                    const editor = initializeAceEditor("saml2MetadataCacheEditor", "xml");
                    editor.setReadOnly(true);
                    for (const [entityId, entry] of Object.entries(response)) {
                        editor.setValue(entry.metadata);
                        $("#saml2MetadataCacheDetails").html(`<i class="mdc-tab__icon mdi mdi-clock" aria-hidden="true"></i> Cache Instant: <code>${entry.cachedInstant}</code>`);
                    }
                    editor.gotoLine(1);
                    $("#saml2MetadataCacheEditorContainer").removeClass("d-none");
                    $("#saml2MetadataCacheDetails").removeClass("d-none");
                    $(this).prop("disabled", false);
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    displayBanner(jqXHR);
                    $("#saml2MetadataCacheEditorContainer").addClass("d-none");
                    $("#saml2MetadataCacheDetails").addClass("d-none");
                    $(this).prop("disabled", false);
                }
            });

        });
    }
}

function processNavigationTabs() {
    if (!actuatorEndpoints.registeredservices) {
        $("#applicationsTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.APPLICATIONS}`).addClass("d-none");
    }
    if (!actuatorEndpoints.metrics || !actuatorEndpoints.httpexchanges || !actuatorEndpoints.auditevents
        || !actuatorEndpoints.heapdump || !actuatorEndpoints.health || !actuatorEndpoints.statistics) {
        $("#systemTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.SYSTEM}`).addClass("d-none");
    }
    if (!actuatorEndpoints.metrics) {
        $("#systemmetricstab").parent().addClass("d-none");
    }
    if (!actuatorEndpoints.springWebflow) {
        $("#caswebflowtab").parent().addClass("d-none");
    }
    if (!actuatorEndpoints.ticketregistry) {
        $("#ticketsTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.TICKETS}`).addClass("d-none");
    }
    if (!actuatorEndpoints.scheduledtasks) {
        $("#tasksTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.TASKS}`).addClass("d-none");
    }
    if (!actuatorEndpoints.persondirectory) {
        $("#personDirectoryTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.PERSON_DIRECTORY}`).addClass("d-none");
    }
    if (!actuatorEndpoints.authenticationHandlers || !actuatorEndpoints.authenticationPolicies) {
        $("#authenticationTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.AUTHENTICATION}`).addClass("d-none");
    }
    if (!actuatorEndpoints.serviceaccess) {
        $("#accessStrategyTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.ACCESS_STRATEGY}`).addClass("d-none");
    }
    if (!actuatorEndpoints.ssosessions) {
        $("#ssoSessionsTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.SSO_SESSIONS}`).addClass("d-none");
    }
    if (!actuatorEndpoints.auditlog) {
        $("#auditEvents").parent().addClass("d-none");
    }
    if (!actuatorEndpoints.events) {
        $("#casEvents").parent().addClass("d-none");
    }
    if ((!actuatorEndpoints.loggingconfig || !actuatorEndpoints.loggers) && !actuatorEndpoints.auditlog) {
        $("#loggingTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.LOGGING}`).addClass("d-none");
    }
    if (!actuatorEndpoints.env || !actuatorEndpoints.configprops) {
        $("#configurationTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.CONFIGURATION}`).addClass("d-none");
    }
    if (!actuatorEndpoints.attributeconsent || !CAS_FEATURES.includes("Consent")) {
        $("#consentTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.CONSENT}`).addClass("d-none");
    }
    if (!actuatorEndpoints.casvalidate) {
        $("#casprotocol").parent().remove();
        $("#casProtocolContainer").addClass("d-none");
    }
    if (!actuatorEndpoints.samlpostprofileresponse || !CAS_FEATURES.includes("SAMLIdentityProvider")) {
        $("#saml2protocol").parent().remove();
        $("#saml2ProtocolContainer").addClass("d-none");
    }
    if (!actuatorEndpoints.samlvalidate || !CAS_FEATURES.includes("SAML")) {
        $("#saml1ProtocolContainer").addClass("d-none");
        $("#saml1protocol").parent().remove();
    }
    if (!actuatorEndpoints.casconfig) {
        $("#config-encryption-tab").addClass("d-none");
        $("#casConfigSecurity").parent().remove();
    }
    if (!actuatorEndpoints.refresh && !actuatorEndpoints.busrefresh) {
        $("#refreshConfigurationButton").addClass("d-none");
    }
    if (!actuatorEndpoints.configurationmetadata) {
        $("#casConfigSearch").addClass("d-none");
    }
    if (!actuatorEndpoints.oidcjwks || !CAS_FEATURES.includes("OpenIDConnect")) {
        $("#oidcprotocol").parent().remove();
        $("#oidcProtocolContainer").addClass("d-none");
    }
    if (!actuatorEndpoints.samlvalidate && !actuatorEndpoints.casvalidate
        && !actuatorEndpoints.samlpostprofileresponse && !actuatorEndpoints.oidcjwks) {
        $("#protocolsTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.PROTOCOLS}`).addClass("d-none");
    }
    if (!actuatorEndpoints.throttles) {
        $("#throttlesTabButton").addClass("d-none");
        $(`#attribute-tab-${Tabs.THROTTLES}`).addClass("d-none");
    }
    if (!actuatorEndpoints.mfadevices || availableMultifactorProviders.length === 0) {
        $("#mfaTabButton").addClass("d-none");
        $("#mfaDevicesTab").parent().addClass("d-none");
        $(`#attribute-tab-${Tabs.MFA}`).addClass("d-none");
    }
    if (!actuatorEndpoints.multifactortrusteddevices || availableMultifactorProviders.length === 0) {
        $("#trustedMfaDevicesTab").parent().addClass("d-none");
    }
    if (!actuatorEndpoints.multitenancy || !CAS_FEATURES.includes("Multitenancy")) {
        $("#tenantsTabButton").addClass("d-none");
    }
    if (!actuatorEndpoints.restart) {
        $("#restartServerButton").addClass("d-none");
    }
    if (!actuatorEndpoints.shutdown) {
        $("#shutdownServerButton").addClass("d-none");
    }
    if (!mutablePropertySourcesAvailable) {
        $("#mutableConfigSources").addClass("d-none");
    }
    return $("nav.sidebar-navigation ul li:visible").length;
}

async function initializePalantir() {
    try {
        setTimeout(() => {
            initializeCasFeatures().then(() => {
                let visibleCount = processNavigationTabs();
                if (visibleCount === 0) {
                    $("#dashboard").hide();
                    Swal.fire({
                        title: "Palantir is unavailable!",
                        text: `Palantir requires a number of actuator endpoints to be enabled and exposed, and your CAS deployment fails to do so.`,
                        icon: "warning",
                        showConfirmButton: false
                    });
                } else {
                    let selectedTab = window.localStorage.getItem("PalantirSelectedTab");
                    if (!$(`nav.sidebar-navigation ul li[data-tab-index=${selectedTab}]`).is(":visible")) {
                        selectedTab = Tabs.APPLICATIONS;
                    }
                    $(`nav.sidebar-navigation ul li[data-tab-index=${selectedTab}]`).click();
                    activateDashboardTab(selectedTab);

                    Promise.all([
                        initializeAllCharts(),
                        initializeScheduledTasksOperations(),
                        initializeServicesOperations(),
                        initializeAccessStrategyOperations(),
                        initializeHeimdallOperations(),
                        initializeTicketsOperations(),
                        initializeSystemOperations(),
                        initializeLoggingOperations(),
                        initializeSsoSessionOperations(),
                        initializeConfigurationOperations(),
                        initializePersonDirectoryOperations(),
                        initializeAuthenticationOperations(),
                        initializeConsentOperations(),
                        initializeCasProtocolOperations(),
                        initializeSAML2ProtocolOperations(),
                        initializeSAML1ProtocolOperations(),
                        initializeOidcProtocolOperations(),
                        initializeThrottlesOperations(),
                        initializeMultifactorOperations(),
                        initializeMultitenancyOperations(),
                        initializeTrustedMultifactorOperations(),
                        initializeAuditEventsOperations(),
                        initializeCasEventsOperations(),
                        initializeCasSpringWebflowOperations(),
                        restoreActiveTabs()
                    ]);

                    window.addEventListener("resize", updateNavigationSidebar, {passive: true});
                }
            });
        }, 2);
        $("#dashboard").removeClass("d-none");
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

function activateDashboardTab(idx) {
    try {
        const tabIndex = Number(idx);
        if (tabIndex === Tabs.SETTINGS) {
            $("#palantirSettingsDialog").dialog({
                position: {
                    my: "center top",
                    at: "center top+100",
                    of: window
                },
                autoOpen: false,
                modal: true,
                width: 600,
                height: "auto",
                buttons: {
                    OK: function () {
                        const storedSettings = localStorage.getItem("PalantirSettings");
                        const palantirSettings = storedSettings ? JSON.parse(storedSettings) : {};
                        palantirSettings.refreshInterval = Number($("#palantirRefreshInterval").val());
                        localStorage.setItem("PalantirSettings", JSON.stringify(palantirSettings));
                        $(this).dialog("close");
                        location.reload(true);
                    },
                    Cancel: function () {
                        $(this).dialog("close");
                    }
                },
                open: function () {
                    cas.init();
                    try {
                        $("#palantirRefreshInterval").selectmenu("destroy");
                    } catch (e) {
                    } finally {
                        $("#palantirRefreshInterval").selectmenu({
                            appendTo: $(this).closest(".ui-dialog")
                        });
                        $("#palantirRefreshInterval")
                            .val(palantirSettings().refreshInterval / 1000)
                            .selectmenu("refresh");
                    }
                },
                close: function () {
                    $(this).dialog("destroy");
                }
            });
            $("#palantirSettingsDialog").dialog("open");
        } else {
            let tabs = new mdc.tabBar.MDCTabBar(document.querySelector("#dashboardTabBar"));
            tabs.activateTab(tabIndex);
            currentActiveTab = tabIndex;
            updateNavigationSidebar();
        }
    } catch (e) {
        console.error("An error occurred while activating tab:", e);
    }
}

function selectSidebarMenuButton(selectedItem) {
    const index = $(selectedItem).data("tab-index");
    console.log("Selected sidebar menu item index:", index);
    if (index !== Tabs.SETTINGS) {
        $("nav.sidebar-navigation ul li").removeClass("active");
        $(selectedItem).addClass("active");
        window.localStorage.setItem("PalantirSelectedTab", index);
    }
    return index;
}

document.addEventListener("DOMContentLoaded", () => {
    $(".jqueryui-tabs").tabs({
        activate: function () {
            const tabId = $(this).attr("id");
            if (tabId) {
                const active = $(this).tabs("option", "active");
                const storedTabs = localStorage.getItem("ActiveTabs");
                const activeTabs = storedTabs ? JSON.parse(storedTabs) : {};
                activeTabs[tabId] = active;
                localStorage.setItem("ActiveTabs", JSON.stringify(activeTabs));
            }
        }
    }).off().on("click", () => updateNavigationSidebar());

    $(".jqueryui-menu").menu();
    $(".jqueryui-selectmenu").selectmenu({
        width: "360px",
        change: function (event, ui) {
            const $select = $(this);
            const handlerNames = $select.data("change-handler").split(",");
            for (const handlerName of handlerNames) {
                if (handlerName && handlerName.length > 0 && typeof window[handlerName] === "function") {
                    // console.log("Invoking change handler:", handlerName);
                    const result = window[handlerName]($select, ui);
                    if (result !== undefined && result === false) {
                        break;
                    }
                }
            }
        }
    });

    $("input.jquery-datepicker").datepicker({
        showAnim: "slideDown",
        onSelect: function (date, ins) {
            $(ins).val(date);
            generateServiceDefinition();
            $(`#${$(ins).prop("id")}`).prev().find(".mdc-notched-outline__notch").hide();
        }
    });

    $("nav.sidebar-navigation ul li").off().on("click", function () {
        hideBanner();
        const index = selectSidebarMenuButton(this);
        activateDashboardTab(index);
    });
    Swal.fire({
        icon: "info",
        title: "Initializing Palantir",
        text: "Please wait while Palantir is initializing...",
        allowOutsideClick: false,
        showConfirmButton: false
    });

    notyf = new Notyf({
        duration: 3000,
        ripple: true,
        dismissable: true,
        position: {
            x: "center",
            y: "bottom"
        }
    });

    initializePalantir().then(r =>
        Swal.fire({
            title: "Palantir is ready!",
            text: "Palantir is successfully initialized and is ready for use.",
            showConfirmButton: false,
            icon: "success",
            timer: 800
        }));
});
