async function initializeCasProtocolOperations() {
    function buildCasProtocolPayload(endpoint, format) {
        const form = document.getElementById("fmCasProtocol");
        if (!form.reportValidity()) {
            return false;
        }
        const username = $("#casProtocolUsername").val();
        const password = $("#casProtocolPassword").val();
        const service = $("#casProtocolService").val();

        $.post(`${CasActuatorEndpoints.casValidate()}/${endpoint}`, {
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

async function initializeSAML1ProtocolOperations() {
    $("button[name=saml1ProtocolButton]").off().on("click", () => {
        const form = document.getElementById("fmSaml1Protocol");
        if (!form.reportValidity()) {
            return false;
        }
        const username = $("#saml1ProtocolUsername").val();
        const password = $("#saml1ProtocolPassword").val();
        const service = $("#saml1ProtocolService").val();

        $.post(`${CasActuatorEndpoints.samlValidate()}`, {
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
                        $.get(`${CasActuatorEndpoints.oidcJwks()}/rotate`, response => {
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
                        $.get(`${CasActuatorEndpoints.oidcJwks()}/revoke`, response => {
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

        if (CasActuatorEndpoints.env()) {
            const oidcConfigurationPropsTable = $("#oidcConfigurationPropsTable").DataTable({
                lengthChange: false
            });
            oidcConfigurationPropsTable.clear();

            $.get(`${CasActuatorEndpoints.env()}?pattern=cas.authn.oidc`, response => {
                response.propertySources.forEach(source => {
                    let properties = source.properties && Object.entries(source.properties || {});
                    properties.forEach(([propKey, propValue]) => {
                        oidcConfigurationPropsTable.row.add(
                            $("<tr class=\"mdc-data-table__row\">")
                                .append(`<td class="mdc-data-table__cell"><code>${propKey}</code></td>`)
                                .append(`<td class="mdc-data-table__cell"><code>${propValue.value}</code></td>`)
                        ).draw(false);
                    });
                });
                updateNavigationSidebar();
                showElements("#oidcConfigurationPropsPanel");
            });
        } else {
            hideElements("#oidcConfigurationPropsPanel");
        }
    }
}

async function initializeSAML2ProtocolOperations() {
    if (CAS_FEATURES.includes("SAMLIdentityProvider")) {
        if (CasActuatorEndpoints.info()) {
            $.get(CasActuatorEndpoints.info(), response => {
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

            $.post(`${CasActuatorEndpoints.samlPostProfileResponse()}`, {
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
                url: `${CasActuatorEndpoints.samlPostProfileResponse()}/logout/post`,
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
                            url: CasActuatorEndpoints.samlIdpRegisteredServiceMetadataCache(),
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
                url: CasActuatorEndpoints.samlIdpRegisteredServiceMetadataCache(),
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


        if (CasActuatorEndpoints.env()) {
            const saml2ConfigurationPropsTable = $("#saml2ConfigurationPropsTable").DataTable({
                lengthChange: false,
                columnDefs: [
                    {
                        targets: 1,
                        className: "dt-left"
                    }
                ]
            });
            saml2ConfigurationPropsTable.clear();

            $.get(`${CasActuatorEndpoints.env()}?pattern=cas.authn.saml-idp`, response => {
                response.propertySources.forEach(source => {
                    let properties = source.properties && Object.entries(source.properties || {});
                    properties.forEach(([propKey, propValue]) => {
                        saml2ConfigurationPropsTable.row.add(
                            $("<tr class=\"mdc-data-table__row\">")
                                .append(`<td class="mdc-data-table__cell"><code>${propKey}</code></td>`)
                                .append(`<td class="mdc-data-table__cell"><code>${propValue.value}</code></td>`)
                        ).draw(false);
                    });
                });
                updateNavigationSidebar();
                showElements("#saml2ConfigurationPropsPanel");
            });
        } else {
            hideElements("#saml2ConfigurationPropsPanel");
        }
    }
}
