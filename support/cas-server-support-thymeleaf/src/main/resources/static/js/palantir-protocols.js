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

            showElements("#casProtocolEditorContainer");
            showElements("#casProtocolServiceEditorContainer");

            updateNavigationSidebar();
            $("#casProtocolServiceNavigation").off().on("click", () => navigateToApplication(data.registeredService.id));
        }).fail((xhr, status, error) => {
            displayBanner(xhr);
            hideElements("#casProtocolEditorContainer");
            hideElements("#casProtocolServiceEditorContainer");
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
            showElements("#saml1ProtocolEditorContainer");
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
            hideElements("#saml2ProtocolEditorContainer");
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
                        showElements("#oidcProtocolEditorContainer");
                    },
                    error: (xhr, textStatus, errorThrown) => {
                        hideElements("#oidcProtocolEditorContainer");
                        console.error("Error fetching data:", errorThrown);
                        displayBanner(xhr);
                    }
                });

            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                hideElements("#oidcProtocolEditorContainer");
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

        if (CasActuatorEndpoints.oidcJwks() && CAS_FEATURES.includes("OpenIDConnect.client-jwks-registration")) {
            showElements($("#oidcclientjwks-li"));

            const oidcClientJwksTable = $("#oidcClientJwksTable").DataTable({
                pageLength: 10,
                autoWidth: false,
                drawCallback: settings => {
                    $("#oidcClientJwksTable tr").addClass("mdc-data-table__row");
                    $("#oidcClientJwksTable td").addClass("mdc-data-table__cell");
                }
            });

            $.get(`${CasActuatorEndpoints.oidcJwks()}/clients`, response => {
                oidcClientJwksTable.clear();
                for (const entry of response) {
                    const deleteButton = `
                        <button type="button" name="deleteOidcClientJwksEntry" href="#"
                            data-jkt="${entry.jkt}"
                            title="Delete"
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                            <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                        </button>
                    `;
                    oidcClientJwksTable.row.add({
                        0: `<code>${entry.jkt}</code>`,
                        1: `<code>${entry.createdAt}</code>`,
                        2: `<code>${entry.jwk}</code>`,
                        3: deleteButton
                    });
                }
                oidcClientJwksTable.draw();

                $("button[name=deleteOidcClientJwksEntry]").off().on("click", function () {
                    const jkt = $(this).data("jkt");
                    const row = $(this).closest("tr");
                    Swal.fire({
                        title: "Are you sure you want to delete this client JWKS entry?",
                        text: "Once deleted, the change will take effect immediately.",
                        icon: "question",
                        showConfirmButton: true,
                        showDenyButton: true
                    }).then((result) => {
                        if (result.isConfirmed) {
                            $.ajax({
                                url: `${CasActuatorEndpoints.oidcJwks()}/clients/${jkt}`,
                                type: "DELETE",
                                success: (response, textStatus, jqXHR) => {
                                    Swal.fire({
                                        title: "Done!",
                                        text: "Client JWKS entry has been deleted successfully.",
                                        showConfirmButton: false,
                                        icon: "success",
                                        timer: 1500
                                    });
                                    oidcClientJwksTable.row(row).remove().draw();
                                },
                                error: (jqXHR, textStatus, errorThrown) => {
                                    console.error("Error deleting client JWKS entry:", errorThrown);
                                    displayBanner(jqXHR);
                                }
                            });
                        }
                    });
                });
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        } else {
            hideElements($("#oidcclientjwks-li"));
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
                showElements("#saml2ProtocolEditorContainer");
                hideElements("#saml2ProtocolLogoutEditor");
            }).fail((xhr, status, error) => {
                displayBanner(xhr);
                hideElements("#saml2ProtocolEditorContainer");
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

                    showElements("#saml2ProtocolEditorContainer");
                    showElements("#saml2ProtocolLogoutEditor");
                },
                error: (xhr, textStatus, errorThrown) => {
                    displayBanner(xhr);
                    hideElements("#saml2ProtocolEditorContainer");
                    hideElements("#saml2ProtocolLogoutEditor");
                }
            });
        });

        $("button[name=saml2MetadataCacheInvalidateButton]").off().on("click", () => {
            hideBanner();
            hideElements("#saml2MetadataCacheEditorContainer");

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
                    showElements("#saml2MetadataCacheEditorContainer");
                    showElements("#saml2MetadataCacheDetails");
                    $(this).prop("disabled", false);
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    displayBanner(jqXHR);
                    hideElements("#saml2MetadataCacheEditorContainer");
                    hideElements("#saml2MetadataCacheDetails");
                    $(this).prop("disabled", false);
                }
            });

        });


        if (CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()) {
            showElements($("#saml2metadatamgmt-li"));

            const saml2MetadataManagerEntriesTable = $("#saml2MetadataManagerEntriesTable").DataTable({
                pageLength: 10,
                autoWidth: false,
                drawCallback: settings => {
                    $("#saml2MetadataManagerEntriesTable tr").addClass("mdc-data-table__row");
                    $("#saml2MetadataManagerEntriesTable td").addClass("mdc-data-table__cell");
                }
            });

            const saml2MetadataManagerEntryEditor = initializeAceEditor("saml2MetadataManagerEntryDialogEditor", "xml");
            saml2MetadataManagerEntryEditor.setReadOnly(true);

            function parseEntityIdFromMetadata(xmlValue) {
                try {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(xmlValue, "application/xml");
                    const entityDescriptor = doc.querySelector("EntityDescriptor") || doc.documentElement;
                    return entityDescriptor.getAttribute("entityID") || "N/A";
                } catch (e) {
                    console.error("Error parsing entity ID from metadata:", e);
                    return "N/A";
                }
            }

            function fetchMetadataManagerEntries(managerName) {
                saml2MetadataManagerEntriesTable.clear().draw();
                $.get(`${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${managerName}`, response => {
                    for (const entry of response) {
                        const entityId = parseEntityIdFromMetadata(entry.value);
                        const buttons = `
                            <button type="button" name="viewSaml2MetadataManagerEntry" href="#"
                                data-entry-id="${entry.id}" data-manager-name="${managerName}"
                                title="View Metadata"
                                class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                                <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                            </button>
                            <button type="button" name="deleteSaml2MetadataManagerEntry" href="#"
                                data-entry-id="${entry.id}" data-manager-name="${managerName}"
                                title="Delete Metadata"
                                class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                                <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                            </button>
                        `;
                        saml2MetadataManagerEntriesTable.row.add({
                            0: `<code>${entry.id}</code>`,
                            1: `<code>${entry.name}</code>`,
                            2: `<code>${entityId}</code>`,
                            3: buttons
                        });
                    }
                    saml2MetadataManagerEntriesTable.draw();

                    $("button[name=viewSaml2MetadataManagerEntry]").off().on("click", function () {
                        const entryId = $(this).data("entry-id");
                        const mgrName = $(this).data("manager-name");
                        $.get(`${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${mgrName}/${entryId}`, entry => {
                            saml2MetadataManagerEntryEditor.setValue(entry.value);
                            saml2MetadataManagerEntryEditor.gotoLine(1);
                            const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("saml2MetadataManagerEntryDialog"));
                            dialog["open"]();
                        }).fail((xhr, status, error) => {
                            console.error("Error fetching data:", error);
                            displayBanner(xhr);
                        });
                    });

                    $("button[name=deleteSaml2MetadataManagerEntry]").off().on("click", function () {
                        const entryId = $(this).data("entry-id");
                        const mgrName = $(this).data("manager-name");
                        Swal.fire({
                            title: "Are you sure you want to delete this metadata entry?",
                            text: "Once deleted, the change will take effect immediately.",
                            icon: "question",
                            showConfirmButton: true,
                            showDenyButton: true
                        }).then((result) => {
                            if (result.isConfirmed) {
                                $.ajax({
                                    url: `${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${mgrName}/${entryId}`,
                                    type: "DELETE",
                                    contentType: "application/x-www-form-urlencoded",
                                    success: (response, textStatus, jqXHR) => {
                                        Swal.fire({
                                            title: "Done!",
                                            text: "Metadata entry has been deleted successfully.",
                                            showConfirmButton: false,
                                            icon: "success",
                                            timer: 2000
                                        });
                                        fetchMetadataManagerEntries(mgrName);
                                    },
                                    error: (jqXHR, textStatus, errorThrown) => {
                                        console.error("Error deleting metadata entry:", errorThrown);
                                        displayBanner(jqXHR);
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

            $("button[name=saml2MetadataUploadButton]").off().on("click", () => {
                hideBanner();
                const selectedManagerName = $("#saml2MetadataManagersSelect option:selected").text();

                const metadataXmlEditor = initializeAceEditor("saml2MetadataUploadXmlEditor", "xml");
                metadataXmlEditor.setReadOnly(true);
                const signatureEditor = initializeAceEditor("saml2MetadataUploadSignatureEditor", "text");
                signatureEditor.setReadOnly(true);

                function adjustUploadEditorLayout() {
                    const xmlVisible = !$("#saml2MetadataUploadXmlEditorContainer").hasClass("d-none");
                    const sigVisible = !$("#saml2MetadataUploadSignatureEditorContainer").hasClass("d-none");
                    if (xmlVisible && sigVisible) {
                        $("#saml2MetadataUploadXmlEditorContainer").css("width", "50%");
                        $("#saml2MetadataUploadSignatureEditorContainer").css("width", "50%");
                    } else if (xmlVisible) {
                        $("#saml2MetadataUploadXmlEditorContainer").css("width", "100%");
                    } else if (sigVisible) {
                        $("#saml2MetadataUploadSignatureEditorContainer").css("width", "100%");
                    }
                    metadataXmlEditor.resize();
                    signatureEditor.resize();
                }

                $("#saml2MetadataUploadXmlFileButton").off().on("click", () => {
                    $("#saml2MetadataUploadXmlFile").click();
                });
                $("#saml2MetadataUploadXmlFile").off().on("change", function () {
                    if (this.files.length > 0) {
                        this.files[0].text().then(content => {
                            metadataXmlEditor.setValue(content);
                            metadataXmlEditor.gotoLine(1);
                            showElements($("#saml2MetadataUploadXmlEditorContainer"));
                            adjustUploadEditorLayout();
                        });
                    }
                });
                $("#saml2MetadataUploadSignatureFileButton").off().on("click", () => {
                    $("#saml2MetadataUploadSignatureFile").click();
                });
                $("#saml2MetadataUploadSignatureFile").off().on("change", function () {
                    if (this.files.length > 0) {
                        this.files[0].text().then(content => {
                            signatureEditor.setValue(content);
                            signatureEditor.gotoLine(1);
                            showElements($("#saml2MetadataUploadSignatureEditorContainer"));
                            adjustUploadEditorLayout();
                        });
                    }
                });

                $("#saml2MetadataUploadDialog").dialog({
                    autoOpen: false,
                    modal: true,
                    width: 1150,
                    height: 880,
                    position: {
                        my: "center top",
                        at: "center top+200",
                        of: window
                    },
                    buttons: {
                        Upload: async function () {
                            if (!$("#saml2MetadataUploadForm")[0].reportValidity()) {
                                return;
                            }
                            const name = $("#saml2MetadataUploadName").val();
                            const xmlText = metadataXmlEditor.getValue().trim();
                            if (!xmlText) {
                                Swal.fire({
                                    title: "Missing File",
                                    text: "Please select a metadata XML file to upload.",
                                    icon: "warning"
                                });
                                return;
                            }
                            const sigText = signatureEditor.getValue().trim();
                            const payload = {name: name, value: xmlText, signature: sigText};
                            $.ajax({
                                url: `${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${selectedManagerName}`,
                                type: "POST",
                                contentType: "application/json",
                                data: JSON.stringify(payload),
                                success: (response) => {
                                    $("#saml2MetadataUploadDialog").dialog("close");
                                    Swal.fire({
                                        title: "Done!",
                                        text: "Metadata has been uploaded successfully.",
                                        icon: "success",
                                        timer: 2000
                                    });
                                    fetchMetadataManagerEntries(selectedManagerName);
                                },
                                error: (xhr, status, error) => {
                                    console.error("Error uploading metadata:", error);
                                    displayBanner(xhr);
                                }
                            });
                        },
                        Cancel: function () {
                            $(this).dialog("close");
                        }
                    },
                    open: function () {
                        $("#saml2MetadataUploadName").val("");
                        $("#saml2MetadataUploadXmlFile").val("");
                        $("#saml2MetadataUploadSignatureFile").val("");
                        metadataXmlEditor.setValue("");
                        signatureEditor.setValue("");
                        hideElements("#saml2MetadataUploadXmlEditorContainer");
                        hideElements("#saml2MetadataUploadSignatureEditorContainer");
                        $("#saml2MetadataUploadName").focus();
                    },
                    close: function () {
                        $(this).dialog("destroy");
                    }
                });
                $("#saml2MetadataUploadDialog").dialog("open");
            });

            $("#saml2MetadataManagersSelect").empty().selectmenu({
                width: "500px",
                change: function (event, ui) {
                    fetchMetadataManagerEntries(ui.item.label);
                }
            });
            $.get(`${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers`, response => {
                response.forEach((manager, idx) => {
                    $("#saml2MetadataManagersSelect").append(
                        $("<option>", {
                            value: manager.sourceId,
                            text: manager.name,
                            selected: idx === 0
                        })
                    );
                });
                $("#saml2MetadataManagersSelect").selectmenu("refresh");
                if (response.length > 0) {
                    fetchMetadataManagerEntries(response[0].name);
                }
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        } else {
            hideElements("#saml2metadatamgmt-li");
        }

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
