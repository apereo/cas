let oidcDiscoveryDocument = null;

/**
 * Read the OpenID Connect discovery document, once.
 *
 * Several panels need the same document -- the issuer label, the OP configuration dialog and the
 * token request form -- and each of them used to fetch it separately.
 *
 * @returns {Promise<object>} the discovery document
 */
function fetchOidcDiscoveryDocument() {
    if (!oidcDiscoveryDocument) {
        oidcDiscoveryDocument = Promise.resolve(
            $.get(`${PalantirDashboardConfiguration.casServerPrefix()}/oidc/.well-known/openid-configuration`))
            .catch(error => {
                oidcDiscoveryDocument = null;
                throw error;
            });
    }
    return oidcDiscoveryDocument;
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

function protocolConfigurationPropertiesMutable() {
    return PalantirDashboardConfiguration.mutablePropertySourcesAvailable() && CasActuatorEndpoints.casConfig();
}

/**
 * Move to the configuration tab, waiting for it to initialize.
 *
 * The tab is built on first activation, so its search and override controls do not exist until
 * that finishes.
 *
 * @returns {Promise<void>} resolved once the configuration tab is ready
 */
async function switchToConfigurationDynamicSourcesTabFromProtocol() {
    PalantirPages.open(Tabs.CONFIGURATION);
    await initializeDashboardTab(Tabs.CONFIGURATION);
}

let pendingSamlMetadataManager = null;

/**
 * Open the SAML2 service provider metadata management section under Protocols, with the metadata manager whose
 * source, such as <code>mongodb://</code>, is given selected once the managers are known.
 *
 * @param sourceId the source of the metadata manager
 */
async function showSamlMetadataManager(sourceId) {
    pendingSamlMetadataManager = sourceId;
    PalantirPages.open(Tabs.PROTOCOLS);
    await initializeDashboardTab(Tabs.PROTOCOLS);
    const outer = $("#protocols-tabs");
    const protocol = outer.find("> ul > li").index($("#saml2protocol").closest("li"));
    if (outer.data("ui-tabs") && protocol >= 0) {
        outer.tabs("option", "active", protocol);
    }
    const inner = $("#saml2-inner-tabs");
    const section = inner.find("> ul > li").index($("#saml2metadatamgmt-li"));
    if (inner.data("ui-tabs") && section >= 0 && !$("#saml2metadatamgmt-li").hasClass("d-none")) {
        inner.tabs("option", "active", section);
    }
    $("#saml2metadatamgmt-tab")[0]?.scrollIntoView({block: "start", behavior: "smooth"});
    selectPendingSamlMetadataManager();
}

/**
 * How SAML2 metadata names what it describes, shared by the metadata upload here and the wizard's metadata import, so
 * an uploaded document is named without fetching the wizard.
 */
class PalantirSamlMetadataNames {
    static NAMESPACES = {
        md: "urn:oasis:names:tc:SAML:2.0:metadata",
        mdui: "urn:oasis:names:tc:SAML:metadata:ui",
        mdattr: "urn:oasis:names:tc:SAML:metadata:attribute",
        saml: "urn:oasis:names:tc:SAML:2.0:assertion",
        xml: "http://www.w3.org/XML/1998/namespace"
    };

    /**
     * Parse metadata XML.
     *
     * @param text the XML
     * @returns {Document} the document
     */
    static parse(text) {
        const document = new DOMParser().parseFromString(text, "application/xml");
        if (document.getElementsByTagName("parsererror").length > 0) {
            throw new Error("This is not well-formed XML.");
        }
        return document;
    }

    /**
     * The entities of a metadata document that are SAML2 service providers.
     *
     * @param document the metadata document
     * @returns {Element[]} their EntityDescriptor elements
     */
    static serviceProviders(document) {
        const ns = PalantirSamlMetadataNames.NAMESPACES;
        return Array.from(document.getElementsByTagNameNS(ns.md, "EntityDescriptor"))
            .filter(entity => entity.getElementsByTagNameNS(ns.md, "SPSSODescriptor").length > 0);
    }

    /**
     * The name a service provider gives itself in its metadata: its display name, else the display name or the name of
     * its organization, else a readable form of its entity id.
     *
     * @param entity the provider's EntityDescriptor
     * @returns {string} the name, or an empty string
     */
    static nameOf(entity) {
        const ns = PalantirSamlMetadataNames.NAMESPACES;
        const children = (parent, namespace, name) => Array.from(parent?.children ?? [])
            .filter(child => child.namespaceURI === namespace && child.localName === name);
        const provider = entity.getElementsByTagNameNS(ns.md, "SPSSODescriptor")[0];
        const uiInfo = provider?.getElementsByTagNameNS(ns.mdui, "UIInfo")[0];
        const displayNames = uiInfo ? Array.from(uiInfo.getElementsByTagNameNS(ns.mdui, "DisplayName")) : [];
        const organization = children(entity, ns.md, "Organization")[0];
        return PalantirSamlMetadataNames.localized(displayNames)
            || PalantirSamlMetadataNames.localized(children(organization, ns.md, "OrganizationDisplayName"))
            || PalantirSamlMetadataNames.localized(children(organization, ns.md, "OrganizationName"))
            || PalantirSamlMetadataNames.readableEntityId(entity.getAttribute("entityID"));
    }

    /**
     * The text of the element in the operator's language, else in English, else the first one.
     *
     * @param elements the elements, one per language
     * @returns {string} the text, or an empty string
     */
    static localized(elements) {
        const ns = PalantirSamlMetadataNames.NAMESPACES;
        const languages = [String(navigator.language ?? "en").toLowerCase().split("-")[0], "en"];
        const language = element => String(element.getAttributeNS(ns.xml, "lang") || element.getAttribute("xml:lang") || "").toLowerCase();
        const chosen = languages.map(code => elements.find(element => language(element).split("-")[0] === code)).find(Boolean)
            ?? elements[0];
        return String(chosen?.textContent ?? "").trim();
    }

    /**
     * A readable form of an entity id: the host of a URL, without www, or the last part of a URN.
     *
     * @param entityId the entity id
     * @returns {string} the readable form, or the entity id as it is
     */
    static readableEntityId(entityId) {
        const value = String(entityId ?? "").trim();
        if (/^urn:/i.test(value)) {
            return value.split(":").filter(Boolean).pop() ?? value;
        }
        try {
            return new URL(value).hostname.replace(/^www\./i, "") || value;
        } catch (error) {
            return value;
        }
    }

    /**
     * The name to keep uploaded metadata under: the name of its one service provider, else the name the aggregate
     * gives itself, else the name of its first service provider.
     *
     * @param text the metadata XML
     * @returns {string} the name, or an empty string when the metadata names no service provider
     */
    static documentName(text) {
        try {
            const document = PalantirSamlMetadataNames.parse(String(text ?? ""));
            const providers = PalantirSamlMetadataNames.serviceProviders(document);
            const root = document.documentElement;
            const aggregate = root?.localName === "EntitiesDescriptor" ? String(root.getAttribute("Name") ?? "").trim() : "";
            if (providers.length === 1) {
                return PalantirSamlMetadataNames.nameOf(providers[0]);
            }
            return aggregate || (providers.length > 0 ? PalantirSamlMetadataNames.nameOf(providers[0]) : "");
        } catch (error) {
            return "";
        }
    }
}

/**
 * Upload SAML2 service provider metadata to a metadata manager: a name, the metadata XML file and, optionally, its
 * signature, shown side by side once chosen. The same dialog serves the metadata management section under Protocols
 * and the application wizard; when there are several managers to choose from, it asks which one.
 */
class PalantirSamlMetadataUpload {
    static managers = null;

    /**
     * Whether metadata can be uploaded from this page: the metadata endpoint is exposed and can be written to, and its
     * upload dialog is on the page.
     *
     * @returns {boolean} true when uploading is possible
     */
    static available() {
        return Boolean(CasActuatorEndpoints.samlIdpRegisteredServiceMetadata())
            && $("#saml2MetadataUploadDialog").length > 0
            && CasActuatorEndpoints.writable("samlIdPRegisteredServiceMetadata");
    }

    /**
     * The metadata managers registered with CAS, read once.
     *
     * @returns {Promise<{name: string, sourceId: string}[]>} the managers, or none when they cannot be read
     */
    static async loadManagers() {
        if (PalantirSamlMetadataUpload.managers === null) {
            try {
                const managers = await $.get(`${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers`);
                PalantirSamlMetadataUpload.managers = Array.isArray(managers) ? managers : [];
            } catch (error) {
                console.debug("The SAML2 metadata managers could not be read", error);
                return [];
            }
        }
        return PalantirSamlMetadataUpload.managers;
    }

    /**
     * Open the upload dialog.
     *
     * @param managers the metadata managers to upload to; the first is chosen, and a choice is offered when there are several
     * @param name the name to start with; while it is empty or still the one suggested, choosing the metadata file
     *        suggests the name of the service provider it describes
     * @param onUploaded called with the manager and the uploaded document once the metadata is stored
     */
    static open({managers, name = "", onUploaded = () => {}}) {
        hideBanner();
        const dialog = $("#saml2MetadataUploadDialog");
        const metadataXmlEditor = initializeAceEditor("saml2MetadataUploadXmlEditor", "xml", {useWorker: false});
        metadataXmlEditor.setReadOnly(true);
        const signatureEditor = initializeAceEditor("saml2MetadataUploadSignatureEditor", "text");
        signatureEditor.setReadOnly(true);

        const managerSelect = $("#saml2MetadataUploadManager").empty();
        managers.forEach((manager, index) => managerSelect.append($("<option>", {value: index, text: manager.name})));
        $("#saml2MetadataUploadManagerRow").toggleClass("d-none", managers.length < 2);

        const adjustUploadEditorLayout = () => {
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
        };
        const reading = {count: 0, latest: new Map(), closed: false};
        let suggested = "";
        const suggestName = content => {
            const field = $("#saml2MetadataUploadName");
            const typed = String(field.val() ?? "").trim();
            const found = PalantirSamlMetadataNames.documentName(content);
            if (found !== "" && (typed === "" || typed === suggested)) {
                suggested = found;
                field.val(found).trigger("input");
                field.closest(".mdc-text-field").find(".mdc-floating-label").addClass("mdc-floating-label--float-above");
            }
        };
        const current = (editor, read) => !reading.closed && reading.latest.get(editor) === read;
        const uploadButton = () => dialog.closest(".ui-dialog").find(".ui-dialog-buttonpane button").first();
        const readInto = (editor, container, onRead = () => {}) => function () {
            if (this.files.length === 0) {
                return;
            }
            const read = Symbol("read");
            reading.latest.set(editor, read);
            reading.count++;
            editor.setValue("");
            uploadButton().prop("disabled", true);
            this.files[0].text()
                .then(content => {
                    if (current(editor, read)) {
                        editor.setValue(content);
                        editor.gotoLine(1);
                        onRead(content);
                        showElements($(container));
                        adjustUploadEditorLayout();
                    }
                })
                .catch(error => {
                    if (current(editor, read)) {
                        PalantirDialogs.error("The File Could Not Be Read", String(error?.message ?? error));
                    }
                })
                .finally(() => {
                    reading.count--;
                    if (!reading.closed) {
                        uploadButton().prop("disabled", reading.count > 0);
                    }
                });
        };
        $("#saml2MetadataUploadXmlFileButton").off().on("click", () => $("#saml2MetadataUploadXmlFile").click());
        $("#saml2MetadataUploadXmlFile").off().on("change", readInto(metadataXmlEditor, "#saml2MetadataUploadXmlEditorContainer", suggestName));
        $("#saml2MetadataUploadSignatureFileButton").off().on("click", () => $("#saml2MetadataUploadSignatureFile").click());
        $("#saml2MetadataUploadSignatureFile").off().on("change", readInto(signatureEditor, "#saml2MetadataUploadSignatureEditorContainer"));

        dialog.dialog({
            autoOpen: false,
            modal: true,
            width: Math.min(1150, $(window).width() - 32),
            height: Math.min(880, $(window).height() - 32),
            position: {my: "center", at: "center", of: window},
            buttons: {
                Upload: function () {
                    if (reading.count > 0 || !$("#saml2MetadataUploadForm")[0].reportValidity()) {
                        return;
                    }
                    const xmlText = metadataXmlEditor.getValue().trim();
                    if (!xmlText) {
                        Swal.fire({
                            title: "Missing File",
                            text: "Please select a metadata XML file to upload.",
                            icon: "warning"
                        });
                        return;
                    }
                    const manager = managers[Number(managerSelect.val())] ?? managers[0];
                    const payload = {
                        name: $("#saml2MetadataUploadName").val(),
                        value: xmlText,
                        signature: signatureEditor.getValue().trim()
                    };
                    $.ajax({
                        url: `${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${encodeURIComponent(manager.name)}`,
                        type: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(payload),
                        success: response => {
                            dialog.dialog("close");
                            Swal.fire({
                                title: "Done!",
                                text: "Metadata has been uploaded successfully.",
                                icon: "success",
                                timer: 2000
                            });
                            onUploaded(manager, response ?? payload);
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
                $("#saml2MetadataUploadName").val(name);
                $("#saml2MetadataUploadXmlFile").val("");
                $("#saml2MetadataUploadSignatureFile").val("");
                metadataXmlEditor.setValue("");
                signatureEditor.setValue("");
                hideElements("#saml2MetadataUploadXmlEditorContainer");
                hideElements("#saml2MetadataUploadSignatureEditorContainer");
                $("#saml2MetadataUploadName").focus();
            },
            close: function () {
                reading.closed = true;
                $(this).dialog("destroy");
            }
        });
        dialog.dialog("open");
    }
}

/**
 * Select the metadata manager asked for by <code>showSamlMetadataManager</code> once its option exists.
 *
 * @returns {boolean} whether a manager was selected
 */
function selectPendingSamlMetadataManager() {
    const sourceId = pendingSamlMetadataManager;
    const select = $("#saml2MetadataManagersSelect");
    if (sourceId === null || select.find("option").length === 0) {
        return false;
    }
    pendingSamlMetadataManager = null;
    const option = select.find("option").filter((index, element) => String(element.value).toLowerCase() === String(sourceId).toLowerCase());
    if (option.length === 0) {
        notyf.error(`No SAML2 metadata manager handles ${sourceId} locations.`);
        return false;
    }
    select.val(option.val());
    if (select.selectmenu("instance")) {
        select.selectmenu("refresh");
    }
    select.trigger("palantir:managerselected");
    return true;
}

async function showProtocolConfigurationPropertyHelp(propertyName) {
    if (CasActuatorEndpoints.configurationMetadata()) {
        await switchToConfigurationDynamicSourcesTabFromProtocol();
        searchForConfigPropertyButton(propertyName);
    }
}

async function overrideProtocolConfigurationPropertyValue(propertyName, propertyValue) {
    if (protocolConfigurationPropertiesMutable()) {
        await switchToConfigurationDynamicSourcesTabFromProtocol();
        overrideConfigPropertyValue(propertyName, propertyValue);
    }
}

function initializeProtocolConfigurationPropertiesContextMenu(table, selector) {
    initializeDataTableContextMenu({
        table: table,
        selector: selector,
        items: {
            help: {
                name: "Configuration Property Help",
                icon: contextMenuIcon("mdi-help"),
                visible: () => CasActuatorEndpoints.configurationMetadata()
            },
            override: {
                name: "Override Configuration Property Value",
                icon: contextMenuIcon("mdi-arrow-left-circle"),
                visible: () => protocolConfigurationPropertiesMutable()
            }
        },
        callback: (key, context) => {
            const {propertyName, propertyValue} = context.rowData;
            if (key === "help") {
                showProtocolConfigurationPropertyHelp(propertyName);
            } else if (key === "override") {
                overrideProtocolConfigurationPropertyValue(propertyName, propertyValue);
            }
        }
    });
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
            const editor = initializeAceEditor("saml1ProtocolEditor", "xml", {useWorker: false});
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
    $.get(`${PalantirDashboardConfiguration.casServerPrefix()}/idp/metadata`, response => {
        showPalantirCodeDialog("saml2MetadataDialog", "SAML2 Identity Provider Metadata",
            new XMLSerializer().serializeToString(response), "xml");
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayBanner(xhr);
    });
}

function showOidcJwks() {
    $.get(`${PalantirDashboardConfiguration.casServerPrefix()}/oidc/jwks`, response => {
        showPalantirCodeDialog("oidcOpConfigurationDialog", "OpenID Connect JSON Web Keystore",
            JSON.stringify(response, null, 2), "json");
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayBanner(xhr);
    });
}

function buildOidcClientSecretRotationEndpoint() {
    return PalantirAvailability.clientSecretsUrl($("#oidcClientSecretRotationClientId").val()?.trim(), {
        expiredOnly: $("#oidcClientSecretRotationExpiredOnly").val() === "true",
        expireIn: $("#oidcClientSecretRotationExpireIn").val()?.trim()
    });
}

function renderOidcClientSecretRotationResult(response) {
    const editor = initializeAceEditor("oidcClientSecretRotationEditor", "json");
    editor.setReadOnly(true);
    editor.setValue(JSON.stringify(response, null, 2));
    editor.gotoLine(1);
    showElements("#oidcClientSecretRotationEditorContainer");
}

function refreshOidcInnerTabs() {
    const $tabs = $("#oidc-inner-tabs");
    if ($tabs.hasClass("ui-tabs")) {
        $tabs.tabs("refresh");
    }
}

function hideOidcProtocolOnlyTabs() {
    [
        "oidctokenexchange",
        "oidckeyrotation",
        "oidcclientjwks",
        "oidcdiscovery",
        "oidcconfigprops"
    ].forEach(tabId => {
        hideElements($(`#${tabId}`).parent());
        hideElements($(`#${tabId}-tab`));
    });
    refreshOidcInnerTabs();
}

function initializeOidcClientSecretRotationOperations() {
    if (CasActuatorEndpoints.oauthClientSecrets()) {
        showElements($("#oidcclientsecretrotation-li"));
        showElements($("#oidcclientsecretrotation-tab"));
        refreshOidcInnerTabs();

        $("button[name=oidcClientSecretRotationButton]").off().on("click", () => {
            hideBanner();
            const form = document.getElementById("fmOidcClientSecretRotation");
            if (!form.reportValidity()) {
                return false;
            }

            PalantirDialogs.confirm({
                title: "Are you sure you want to rotate client secrets?",
                text: "Once rotated, the change will take effect immediately."
            }).then((result) => {
                if (result.isConfirmed) {
                    $("#oidcClientSecretRotationButton").prop("disabled", true);
                    $.ajax({
                        url: buildOidcClientSecretRotationEndpoint(),
                        type: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        success: (response) => {
                            renderOidcClientSecretRotationResult(response);
                            Swal.fire({
                                title: "Done!",
                                text: "Client secrets have been rotated successfully.",
                                icon: "success",
                                showConfirmButton: false,
                                timer: 1500
                            });
                            $("#oidcClientSecretRotationButton").prop("disabled", false);
                        },
                        error: (xhr, textStatus, errorThrown) => {
                            console.error("Error rotating client secrets:", errorThrown);
                            displayBanner(xhr);
                            hideElements("#oidcClientSecretRotationEditorContainer");
                            $("#oidcClientSecretRotationButton").prop("disabled", false);
                        }
                    });
                }
            });
        });
    } else {
        hideElements($("#oidcclientsecretrotation-li"));
        hideElements($("#oidcclientsecretrotation-tab"));
        refreshOidcInnerTabs();
    }
}

async function initializeOidcProtocolOperations() {
    initializeOidcClientSecretRotationOperations();
    if (CAS_FEATURES.includes("OpenIDConnect")) {
        fetchOidcDiscoveryDocument().then(response => {
            highlightElements();
            $("#oidcIssuer").text(response.issuer);
        }).catch(error => console.error("Error fetching data:", error));

        $("button[name=oidcOpConfigurationButton]").off().on("click", () => {
            hideBanner();
            fetchOidcDiscoveryDocument().then(response => {
                showPalantirCodeDialog("oidcOpConfigurationDialog", "OpenID Connect Configuration",
                    JSON.stringify(response, null, 2), "json");
            }).catch(xhr => {
                console.error("Error fetching data:", xhr);
                displayBanner(xhr);
            });
        });

        $("button[name=oidcKeyRotationButton]").off().on("click", () => {
            hideBanner();
            PalantirDialogs.confirm({
                title: "Are You Sure You Want to Rotate Keys?",
                text: "Once rotated, the change will take effect immediately."
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $("#oidcKeyRotationButton").prop("disabled", true);
                        $.post(`${CasActuatorEndpoints.oidcJwks()}/rotate`, response => {
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
            PalantirDialogs.confirm({
                title: "Are You Sure You Want to Revoke Keys?",
                text: "Once revoked, the change will take effect immediately."
            })
                .then((result) => {
                    if (result.isConfirmed) {
                        $("#oidcKeyRevocationButton").prop("disabled", true);
                        $.post(`${CasActuatorEndpoints.oidcJwks()}/revoke`, response => {
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

            fetchOidcDiscoveryDocument().then(oidcConfiguration => {

                const clientId = $("#oidcProtocolClientId").val();
                const clientSecret = $("#oidcProtocolClientSecret").val();
                const scopes = encodeURIComponent($("#oidcProtocolScopes").val());

                $.ajax({
                    url: `${oidcConfiguration.token_endpoint}?grant_type=client_credentials&scope=${encodeURIComponent(scopes)}`,
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

            }).catch(xhr => {
                console.error("Error fetching data:", xhr);
                displayBanner(xhr);
                hideElements("#oidcProtocolEditorContainer");
            });


        });

        if (CasActuatorEndpoints.env()) {
            const oidcConfigurationPropsTable = $("#oidcConfigurationPropsTable").DataTable({
                deferRender: true,
                lengthChange: false,
                columnDefs: [
                    {
                        targets: 1,
                        className: "dt-left"
                    }
                ],
                drawCallback: () => {
                    $("#oidcConfigurationPropsTable tr").addClass("mdc-data-table__row");
                    $("#oidcConfigurationPropsTable td").addClass("mdc-data-table__cell");
                }
            });
            oidcConfigurationPropsTable.clear();
            initializeProtocolConfigurationPropertiesContextMenu(
                oidcConfigurationPropsTable,
                "#oidcConfigurationPropsTable tbody tr");

            $.get(`${CasActuatorEndpoints.env()}?pattern=cas.authn.oidc`, response => {
                response.propertySources.forEach(source => {
                    let properties = source.properties && Object.entries(source.properties || {});
                    properties.forEach(([propKey, propValue]) => {
                        oidcConfigurationPropsTable.row.add({
                            0: `${palantirCode(propKey)}`,
                            1: `${palantirCode(propValue.value)}`,
                            propertyName: propKey,
                            propertyValue: propValue.value
                        });
                    });
                });
                oidcConfigurationPropsTable.draw(false);
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

            function deleteOidcClientJwksEntry(context) {
                const {jkt, clientId} = context.rowData;
                PalantirDialogs.confirm({
                    title: "Are you sure you want to delete this client JWKS entry?",
                    text: "Once deleted, the change will take effect immediately."
                }).then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.oidcJwks()}/clients/${encodeURIComponent(clientId)}/${encodeURIComponent(jkt)}`,
                            type: "DELETE",
                            success: (response, textStatus, jqXHR) => {
                                Swal.fire({
                                    title: "Done!",
                                    text: "Client JWKS entry has been deleted successfully.",
                                    showConfirmButton: false,
                                    icon: "success",
                                    timer: 1500
                                });
                                context.row.remove().draw();
                            },
                            error: (jqXHR, textStatus, errorThrown) => {
                                console.error("Error deleting client JWKS entry:", errorThrown);
                                displayBanner(jqXHR);
                            }
                        });
                    }
                });
            }

            initializeDataTableContextMenu({
                table: oidcClientJwksTable,
                selector: "#oidcClientJwksTable tbody tr",
                items: {
                    delete: {name: "Delete", write: "oidcJwks", icon: contextMenuIcon("mdi-delete")}
                },
                callback: (key, context) => {
                    if (key === "delete") {
                        deleteOidcClientJwksEntry(context);
                    }
                }
            });

            $.get(`${CasActuatorEndpoints.oidcJwks()}/clients`, response => {
                oidcClientJwksTable.clear();
                for (const entry of response) {
                    oidcClientJwksTable.row.add({
                        0: `${palantirCode(entry.clientId)}`,
                        1: `${palantirCode(entry.jkt)}`,
                        2: `${palantirCode(entry.createdAt)}`,
                        3: `${palantirCode(entry.jwk)}`,
                        jkt: entry.jkt,
                        clientId: entry.clientId
                    });
                }
                oidcClientJwksTable.draw();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        } else {
            hideElements($("#oidcclientjwks-li"));
        }
    } else {
        hideOidcProtocolOnlyTabs();
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
                const editor = initializeAceEditor("saml2ProtocolEditor", "xml", {useWorker: false});
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
                    const saml2ProtocolLogoutEditor = initializeAceEditor("saml2ProtocolLogoutEditor", "xml", {useWorker: false});
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

            PalantirDialogs.confirm({
                title: "Are you sure you want to invalidate the cache entry?",
                text: "Once deleted, the change will take effect immediately."
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
                    const editor = initializeAceEditor("saml2MetadataCacheEditor", "xml", {useWorker: false});
                    editor.setReadOnly(true);
                    for (const [entityId, entry] of Object.entries(response)) {
                        editor.setValue(entry.metadata);
                        $("#saml2MetadataCacheDetails").html(`<i class="mdc-tab__icon mdi mdi-clock" aria-hidden="true"></i> Cache Instant: ${palantirCode(entry.cachedInstant)}`);
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

            const saml2MetadataManagerEntryEditor = initializeAceEditor("saml2MetadataManagerEntryDialogEditor", "xml", {useWorker: false});
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

            function viewSaml2MetadataManagerEntry(entryId, managerName) {
                $.get(`${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${encodeURIComponent(managerName)}/${encodeURIComponent(entryId)}`, entry => {
                    saml2MetadataManagerEntryEditor.setValue(entry.value);
                    saml2MetadataManagerEntryEditor.gotoLine(1);
                    const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("saml2MetadataManagerEntryDialog"));
                    dialog["open"]();
                }).fail((xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    displayBanner(xhr);
                });
            }

            function copySaml2MetadataEntityId(entityIdValue) {
                copyToClipboard(entityIdValue).then(() => {
                    Swal.fire({
                        title: "Copied",
                        html: `Entity ID ${palantirCode(entityIdValue)} has been copied to the clipboard.`,
                        showConfirmButton: false,
                        icon: "success",
                        timer: 2000
                    });
                }).catch(err => {
                    console.error("Failed to copy entity ID to clipboard:", err);
                    displayBanner({responseJSON: {message: "Failed to copy entity ID to clipboard."}});
                });
            }

            function registerSaml2MetadataManagerEntry(entryName, entityIdValue) {
                const saml2ServiceClass = Object.keys(PalantirDashboardConfiguration.supportedServiceTypes()).find(cls => cls.includes("SamlRegisteredService"));
                if (saml2ServiceClass) {
                    const partialService = {
                        "@class": saml2ServiceClass,
                        "name": entryName,
                        "serviceId": entityIdValue,
                        "metadataLocation": "mongodb://"
                    };
                    PalantirPages.open(Tabs.APPLICATIONS);
                    setTimeout(() => openPalantirWizard(partialService), 150);
                } else {
                    displayBanner("CAS is unable to register this entry as a SAML2 Service Provider");
                }
            }

            function deleteSaml2MetadataManagerEntry(entryId, managerName) {
                PalantirDialogs.confirm({
                    title: "Are you sure you want to delete this metadata entry?",
                    text: "Once deleted, the change will take effect immediately."
                }).then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${encodeURIComponent(managerName)}/${encodeURIComponent(entryId)}`,
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
                                fetchMetadataManagerEntries(managerName);
                            },
                            error: (jqXHR, textStatus, errorThrown) => {
                                console.error("Error deleting metadata entry:", errorThrown);
                                displayBanner(jqXHR);
                            }
                        });
                    }
                });
            }

            initializeDataTableContextMenu({
                table: saml2MetadataManagerEntriesTable,
                selector: "#saml2MetadataManagerEntriesTable tbody tr",
                items: {
                    view: {name: "View Metadata", icon: contextMenuIcon("mdi-eye")},
                    copyEntityId: {name: "Copy Entity ID", icon: contextMenuIcon("mdi-content-copy")},
                    register: {name: "Register as SAML2 Service Provider", write: "registeredServices", icon: contextMenuIcon("mdi-plus-circle")},
                    delete: {name: "Delete Metadata", write: "samlIdPRegisteredServiceMetadata", icon: contextMenuIcon("mdi-delete")}
                },
                callback: (key, context) => {
                    const {entryId, managerName, entityId, entryName} = context.rowData;
                    if (key === "view") {
                        viewSaml2MetadataManagerEntry(entryId, managerName);
                    } else if (key === "copyEntityId") {
                        copySaml2MetadataEntityId(entityId);
                    } else if (key === "register") {
                        registerSaml2MetadataManagerEntry(entryName, entityId);
                    } else if (key === "delete") {
                        deleteSaml2MetadataManagerEntry(entryId, managerName);
                    }
                }
            });

            function fetchMetadataManagerEntries(managerName) {
                saml2MetadataManagerEntriesTable.clear().draw();
                $.get(`${CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()}/managers/${encodeURIComponent(managerName)}`, response => {
                    for (const entry of response) {
                        const entityId = parseEntityIdFromMetadata(entry.value);
                        saml2MetadataManagerEntriesTable.row.add({
                            0: `${palantirCode(entry.id)}`,
                            1: `${palantirCode(entry.name)}`,
                            2: `${palantirCode(entityId)}`,
                            entryId: entry.id,
                            managerName: managerName,
                            entityId: entityId,
                            entryName: entry.name
                        });
                    }
                    saml2MetadataManagerEntriesTable.draw();
                }).fail((xhr, status, error) => {
                    console.error("Error fetching data:", error);
                    displayBanner(xhr);
                });
            }

            $("button[name=saml2MetadataUploadButton]").off().on("click", () => {
                const selected = $("#saml2MetadataManagersSelect option:selected");
                PalantirSamlMetadataUpload.open({
                    managers: [{name: selected.text(), sourceId: selected.val()}],
                    onUploaded: manager => fetchMetadataManagerEntries(manager.name)
                });
            });

            $("#saml2MetadataManagersSelect").empty().selectmenu({
                width: "500px",
                change: function (event, ui) {
                    fetchMetadataManagerEntries(ui.item.label);
                }
            });
            $("#saml2MetadataManagersSelect")
                .off("palantir:managerselected")
                .on("palantir:managerselected", () => fetchMetadataManagerEntries($("#saml2MetadataManagersSelect option:selected").text()));
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
                if (!selectPendingSamlMetadataManager() && response.length > 0) {
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
                deferRender: true,
                lengthChange: false,
                columnDefs: [
                    {
                        targets: 1,
                        className: "dt-left"
                    }
                ],
                drawCallback: () => {
                    $("#saml2ConfigurationPropsTable tr").addClass("mdc-data-table__row");
                    $("#saml2ConfigurationPropsTable td").addClass("mdc-data-table__cell");
                }
            });
            saml2ConfigurationPropsTable.clear();
            initializeProtocolConfigurationPropertiesContextMenu(
                saml2ConfigurationPropsTable,
                "#saml2ConfigurationPropsTable tbody tr");

            $.get(`${CasActuatorEndpoints.env()}?pattern=cas.authn.saml-idp`, response => {
                response.propertySources.forEach(source => {
                    let properties = source.properties && Object.entries(source.properties || {});
                    properties.forEach(([propKey, propValue]) => {
                        saml2ConfigurationPropsTable.row.add({
                            0: `${palantirCode(propKey)}`,
                            1: `${palantirCode(propValue.value)}`,
                            propertyName: propKey,
                            propertyValue: propValue.value
                        });
                    });
                });
                saml2ConfigurationPropsTable.draw(false);
                updateNavigationSidebar();
                showElements("#saml2ConfigurationPropsPanel");
            });
        } else {
            hideElements("#saml2ConfigurationPropsPanel");
        }
    }
}
