function removeIdentityProvider(idp, type) {
    if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
        Swal.fire({
            title: `Are you sure you want to delete ${idp}?`,
            text: `
                Removing this identity provider is only possible if it's owned and managed by a dynamic configuration source. 
                Once removed, you may not be able to revert this.
            `,
            icon: "question",
            showConfirmButton: true,
            showDenyButton: true
        })
            .then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: `${CasActuatorEndpoints.casConfig()}/retrieve`,
                        method: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(
                            {
                                name: `cas.authn.pac4j.${type}\\[\\d+\\].*`,
                                value: idp
                            }
                        )
                    })
                        .done(function (data, textStatus, jqXHR) {
                            if (data.length === 0) {
                                Swal.fire("Error", `
                                No configuration entries could be found for the identity provider ${idp}.
                                Are you sure it's managed via a dynamic configuration source?
                                `, "error");
                                return;
                            }
                            if (data.length === 1) {
                                const group = `${data[0].name.match(/^(.*?\[\d+\])/)[1].replace("[", "\\[").replace("]", "\\]")}.*`;
                                $.ajax({
                                    url: `${CasActuatorEndpoints.casConfig()}/retrieve`,
                                    method: "POST",
                                    contentType: "application/json",
                                    data: JSON.stringify(
                                        {
                                            name: group,
                                            propertySource: data[0].propertySource
                                        }
                                    )
                                })
                                    .done(function (data, textStatus, jqXHR) {
                                        const payloadFor = (entry) => JSON.stringify({
                                            name: entry.name,
                                            propertySource: entry.propertySource
                                        });

                                        const requests = data.map(entry =>
                                            Promise.resolve(
                                                $.ajax({
                                                    url: CasActuatorEndpoints.casConfig(),
                                                    method: "DELETE",
                                                    contentType: "application/json",
                                                    data: payloadFor(entry)
                                                })
                                            )
                                        );
                                        Promise.all(requests)
                                            .then(() => {
                                                $.get(CasActuatorEndpoints.env(), res => {
                                                    reloadConfigurationTable(res);
                                                    refreshCasServerConfiguration(`The identity provider ${idp} has been removed.`);
                                                })
                                                    .fail((xhr) => {
                                                        displayBanner(xhr);
                                                    });
                                            })
                                            .catch(jqXHR => {
                                                displayBanner(jqXHR);
                                                Swal.fire("Error", "At least one delete request failed.", "error");
                                            });
                                    })
                                    .fail(function (jqXHR, textStatus, errorThrown) {
                                        console.error("Error:", textStatus, errorThrown);
                                        displayBanner(jqXHR);
                                    });

                            }
                        })
                        .fail(function (jqXHR, textStatus, errorThrown) {
                            console.error("Error:", textStatus, errorThrown);
                            displayBanner(jqXHR);
                        });
                }
            });
    }
}

function configureSaml2ClientMetadataButtons() {
    async function showSamlMetadata(payload) {
        const saml2Editor = initializeAceEditor("delegatedClientsSaml2Editor", "xml");
        saml2Editor.setReadOnly(true);

        function simplePrettyXml(xml) {
            let pad = 0;
            return xml
                .replace(/(>)(<)(\/*)/g, "$1\n$2$3")
                .split("\n")
                .map(line => {
                    let indent = 0;
                    if (line.match(/^<\/\w/)) {
                        pad--;
                    } else if (line.match(/^<\w([^>]*[^/])?>$/)) {
                        indent = 1;
                    }
                    const result = "  ".repeat(pad) + line;
                    pad += indent;
                    return result;
                })
                .join("\n");
        }

        saml2Editor.setValue(simplePrettyXml(new XMLSerializer().serializeToString(payload)));
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

async function loadExternalIdentityProvidersTable() {
    const delegatedClientsTable = $("#delegatedClientsTable").DataTable();
    delegatedClientsTable.clear();
    if (CasActuatorEndpoints.delegatedClients()) {
        Swal.fire({
            icon: "info",
            title: `Loading Identity Providers`,
            text: "Please wait while external identity providers are being retrieved...",
            allowOutsideClick: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });
        $.get(CasActuatorEndpoints.delegatedClients(), response => {
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
            $("#delegatedclients").parent().removeClass("d-none");
            updateNavigationSidebar();
            configureSaml2ClientMetadataButtons();
            Swal.close();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            $("#delegatedClientsContainer").addClass("d-none");
            $("#delegatedclients").parent().addClass("d-none");
            displayBanner(xhr);
            Swal.close();
        });
    } else {
        $("#delegatedClientsContainer").addClass("d-none");
        $("#delegatedclients").parent().addClass("d-none");
    }
}

function newExternalIdentityProvider() {
    if (mutablePropertySourcesAvailable && CasActuatorEndpoints.casConfig()) {
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
            availableProviders.push({
                value: "KEYCLOAK",
                text: "Keycloak"
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
                {value: "", text: "CAS 3.0", selected: true},
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
            cssClasses: "OAUTH2 OIDC KEYCLOAK hide",
            paramName: "id"
        });
        createInputField({
            labelTitle: "Client Secret",
            name: "externalIdentityProviderOAuthOidcClientSecret",
            required: true,
            containerId: dialogContainer,
            title: "Define the Client secret provided by the OAuth2/OIDC identity provider.",
            cssClasses: "OAUTH2 OIDC KEYCLOAK hide",
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
            cssClasses: "OIDC KEYCLOAK hide",
            paramName: "discovery-uri"
        });

        createInputField({
            labelTitle: "Realm",
            name: "externalIdentityProviderKeycloakRealm",
            required: true,
            containerId: dialogContainer,
            title: "Keycloak realm used to construct metadata discovery URI.",
            cssClasses: "KEYCLOAK hide",
            paramName: "realm"
        });
        createInputField({
            labelTitle: "Base URI",
            name: "externalIdentityProviderKeycloakBaseUri",
            required: true,
            containerId: dialogContainer,
            title: "Keycloak base URL used to construct metadata discovery URI.",
            cssClasses: "KEYCLOAK hide",
            paramName: "base-uri"
        });

        createInputField({
            labelTitle: "Scope",
            name: "externalIdentityProviderOAuthScope",
            required: true,
            containerId: dialogContainer,
            title: "Define the scope of the OAuth2 identity provider.",
            cssClasses: "OAUTH2 OIDC KEYCLOAK hide",
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
                {value: "", text: "NONE", selected: true},
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
            $("#newExternalIdentityProviderDialog input:visible").first().focus();
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
                        const currentType = $("#externalIdPTypeSelect").val();
                        let group = "";
                        switch (currentType) {
                        case "CAS":
                            group = "cas.authn.pac4j.cas[]";
                            break;
                        case "KEYCLOAK":
                            group = "cas.authn.pac4j.oidc[].keycloak";
                            break;
                        case "OIDC":
                            group = "cas.authn.pac4j.oidc[].generic";
                            break;
                        case "OAUTH2":
                            group = "cas.authn.pac4j.oauth2[]";
                            break;
                        case "SAML2":
                            group = "cas.authn.pac4j.saml[]";
                            break;
                        }

                        const formFields = $(
                            `
                            #newExternalIdentityProviderDialog input:visible,
                            #newExternalIdentityProviderDialog label.${currentType} > select[data-param-name],
                            #newExternalIdentityProviderDialog label.always-show > select[data-param-name]
                            `
                        ).toArray();

                        const payload = formFields
                            .filter(input => {
                                const v = $(input).val();
                                return v !== null && v !== "";
                            })
                            .map((input, index) => (
                                {
                                    name: `${group}.${$(input).data("param-name")}`,
                                    value: $(input).val()
                                }
                            ));

                        $.ajax({
                            url: `${CasActuatorEndpoints.casConfig()}/update`,
                            method: "POST",
                            contentType: "application/json",
                            data: JSON.stringify(payload),
                            success: response => {
                                $(this).dialog("close");
                                $.get(CasActuatorEndpoints.env(), async res => {
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


                    }
                },
                Cancel: function () {
                    $(this).dialog("close");
                }
            },
            open: function () {
                cas.init("#newExternalIdentityProviderDialog");
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



