async function initializePersonDirectoryOperations() {
    const personDirectoryTable = $("#personDirectoryTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: () => {
            $("#personDirectoryTable tr").addClass("mdc-data-table__row");
            $("#personDirectoryTable td").addClass("mdc-data-table__cell");
        }
    });

    const attrDefToolbar = document.createElement("div");
    let attrDefToolbarEntries = "";
    if (CasActuatorEndpoints.attributeDefinitions()) {
        attrDefToolbarEntries += `
            <button type="button" id="newAttributeDefinitionButton"
                    onclick="newAttributeDefinition()"
                    title="Create a new attribute definition"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>New</span>
            </button>
            <button type="button" id="deleteAllAttributeDefinitionsButton"
                    onclick="deleteAllAttributeDefinitions()"
                    title="Delete all attribute definitions"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-delete-sweep" aria-hidden="true"></i>Delete All</span>
            </button>
            <button type="button" id="exportAttributeDefinitionsButton"
                    onclick="exportAttributeDefinitions()"
                    title="Export attribute definitions as JSON"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-export" aria-hidden="true"></i>Export</span>
            </button>
            <button type="button" id="importAttributeDefinitionsButton"
                    onclick="document.getElementById('importAttrDefFileInput').click()"
                    title="Import attribute definitions from JSON"
                    class="mdc-button mdc-button--raised">
                <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-import" aria-hidden="true"></i>Import</span>
            </button>
            <input type="file" id="importAttrDefFileInput" accept=".json" style="display:none"
                   onchange="importAttributeDefinitions(this)"/>
        `;
    }
    attrDefToolbar.innerHTML = attrDefToolbarEntries;

    const attributeDefinitionsTable = $("#attributeDefinitionsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "32px", targets: 0, className: "text-center"}
        ],
        layout: {
            topStart: attrDefToolbar
        },
        drawCallback: () => {
            $("#attributeDefinitionsTable tr").addClass("mdc-data-table__row");
            $("#attributeDefinitionsTable td").addClass("mdc-data-table__cell");
        }
    });

    const attributeRepositoriesTable = $("#attributeRepositoriesTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        drawCallback: () => {
            $("#attributeRepositoriesTable tr").addClass("mdc-data-table__row");
            $("#attributeRepositoriesTable td").addClass("mdc-data-table__cell");
        }
    });

    $("button[name=personDirectoryClearButton]").off().on("click", () => {
        if (CasActuatorEndpoints.personDirectory()) {
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
                            url: `${CasActuatorEndpoints.personDirectory()}/cache/${username}`,
                            type: "DELETE",
                            contentType: "application/json",
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
        if (CasActuatorEndpoints.personDirectory()) {
            const form = document.getElementById("fmPersonDirectory");
            if (!form.reportValidity()) {
                return false;
            }
            const username = $("#personUsername").val();
            personDirectoryTable.clear();
            $.ajax({
                url: `${CasActuatorEndpoints.personDirectory()}/cache/${username}`,
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
    if (CasActuatorEndpoints.attributeDefinitions()) {
        $.get(CasActuatorEndpoints.attributeDefinitions(), response => {
            const checked = `<i class='mdc-tab__icon mdi mdi-check-circle' aria-hidden='true'></i>`;
            const unchecked = `<i class='mdc-tab__icon mdi mdi-checkbox-blank-circle-outline' aria-hidden='true'></i>`;
            
            for (const definition of response) {
                attributeDefinitionsTable.row.add({
                    0: attrDefTypeIcon(definition),
                    1: `<code>${definition.key ?? "N/A"}</code>`,
                    2: `<code>${definition.name ?? "N/A"}</code>`,
                    3: `<code>${definition.scoped ? checked : unchecked}</code>`,
                    4: `<code>${definition.encrypted ? checked : unchecked}</code>`,
                    5: `<code>${definition.singleValue ? checked : unchecked}</code>`,
                    6: `<code>${definition.attribute ?? "N/A"}</code>`,
                    7: `<code>${definition.patternFormat ?? "N/A"}</code>`,
                    8: `<code>${definition.canonicalizationMode ?? "N/A"}</code>`,
                    9: `<code>${definition.flattened ? checked : unchecked}</code>`,
                    10: `<code>${definition?.friendlyName ?? "N/A"}</code>`,
                    11: `<code>${definition?.urn ?? "N/A"}</code>`,
                    12: `<button type="button" name="viewAttrDefn" href="#" data-key="${definition.key}"
                                title="View Attribute Definition"
                                class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                            <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                         </button>
                         <button type="button" name="editAttrDefn" href="#" data-key="${definition.key}"
                                title="Edit Attribute Definition"
                                class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                            <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                         </button>
                         <button type="button" name="deleteAttrDefn" href="#" data-key="${definition.key}"
                                title="Delete Attribute Definition"
                                class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                            <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                         </button>`
                });
                attributeDefinitions++;
            }
            if (attributeDefinitions > 0) {
                attributeDefinitionsTable.draw();
            }
            showElements($("#attributeDefinitionsTab").parent());
            bindAttrDefDeleteButtons();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    } else {
        hideElements($("#attributeDefinitionsTab").parent());
    }

    if (!CasActuatorEndpoints.attributeDefinitions()) {
        hideElements($("#newAttributeDefinitionButton"));
    }

    attributeRepositoriesTable.clear();
    let attributeRepositories = 0;
    if (CasActuatorEndpoints.personDirectory()) {
        $.get(`${CasActuatorEndpoints.personDirectory()}/repositories`, response => {
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

function attrDefTypeIcon(definition) {
    const cls = definition["@class"] || "";
    if (cls.includes("SamlIdPAttributeDefinition")) {
        return `<i title="SAML" class="mdi mdi-alpha-s-box-outline"></i>`;
    } else if (cls.includes("OidcAttributeDefinition")) {
        return `<i title="OpenID Connect" class="mdi mdi-alpha-o-box-outline"></i>`;
    } else if (cls.includes("OAuthAttributeDefinition")) {
        return `<i title="OAuth" class="mdi mdi-alpha-o-circle-outline"></i>`;
    }
    return `<i title="Default" class="mdi mdi-alpha-d-box-outline"></i>`;
}

function reloadAttributeDefinitionsTable() {
    if (!CasActuatorEndpoints.attributeDefinitions()) {
        return;
    }
    const table = $("#attributeDefinitionsTable").DataTable();
    table.clear();
    $.get(CasActuatorEndpoints.attributeDefinitions(), response => {
        const checked = `<i class='mdc-tab__icon mdi mdi-check-circle' aria-hidden='true'></i>`;
        const unchecked = `<i class='mdc-tab__icon mdi mdi-checkbox-blank-circle-outline' aria-hidden='true'></i>`;
        for (const definition of response) {
            table.row.add({
                0: attrDefTypeIcon(definition),
                1: `<code>${definition.key ?? "N/A"}</code>`,
                2: `<code>${definition.name ?? "N/A"}</code>`,
                3: `<code>${definition.scoped ? checked : unchecked}</code>`,
                4: `<code>${definition.encrypted ? checked : unchecked}</code>`,
                5: `<code>${definition.singleValue ? checked : unchecked}</code>`,
                6: `<code>${definition.attribute ?? "N/A"}</code>`,
                7: `<code>${definition.patternFormat ?? "N/A"}</code>`,
                8: `<code>${definition.canonicalizationMode ?? "N/A"}</code>`,
                9: `<code>${definition.flattened ? checked : unchecked}</code>`,
                10: `<code>${definition?.friendlyName ?? "N/A"}</code>`,
                11: `<code>${definition?.urn ?? "N/A"}</code>`,
                12: `<button type="button" name="viewAttrDefn" href="#" data-key="${definition.key}"
                            title="View Attribute Definition"
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                        <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                     </button>
                     <button type="button" name="editAttrDefn" href="#" data-key="${definition.key}"
                            title="Edit Attribute Definition"
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                        <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                     </button>
                     <button type="button" name="deleteAttrDefn" href="#" data-key="${definition.key}"
                            title="Delete Attribute Definition"
                            class="mdc-button mdc-button--raised btn btn-link min-width-32x">
                        <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
                     </button>`
            });
        }
        table.draw();
        if (response.length > 0) {
            showElements($("#attributeDefinitionsTab").parent());
        }
        bindAttrDefDeleteButtons();
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayBanner(xhr);
    });
}

function deleteAllAttributeDefinitions() {
    if (!CasActuatorEndpoints.attributeDefinitions()) {
        return;
    }
    const table = $("#attributeDefinitionsTable").DataTable();
    const keys = [];
    table.rows().every(function () {
        const data = this.data();
        const keyHtml = data[1];
        const key = $(keyHtml).text();
        if (key && key !== "N/A") {
            keys.push(key);
        }
    });
    if (keys.length === 0) {
        Swal.fire("Pay Attention!", "There are no attribute definitions to delete.", "info");
        return;
    }
    Swal.fire({
        title: `Are you sure you want to delete all ${keys.length} attribute definition(s)?`,
        text: "Once deleted, you may not be able to recover these entries.",
        icon: "question",
        showConfirmButton: true,
        showDenyButton: true
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: CasActuatorEndpoints.attributeDefinitions(),
                type: "DELETE",
                contentType: "application/json",
                data: JSON.stringify(keys),
                success: () => {
                    reloadAttributeDefinitionsTable();
                    Swal.fire("Success", "All attribute definitions deleted.", "success");
                },
                error: (xhr, status, error) => {
                    console.error("Error deleting resources:", error);
                    displayBanner(xhr);
                }
            });
        }
    });
}

function exportAttributeDefinitions() {
    if (!CasActuatorEndpoints.attributeDefinitions()) {
        return;
    }
    $.get(CasActuatorEndpoints.attributeDefinitions(), response => {
        const blob = new Blob([JSON.stringify(response, null, 2)], {type: "application/json"});
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "attribute-definitions.json";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }).fail((xhr, status, error) => {
        console.error("Error exporting data:", error);
        displayBanner(xhr);
    });
}

function importAttributeDefinitions(fileInput) {
    if (!CasActuatorEndpoints.attributeDefinitions() || !fileInput.files || fileInput.files.length === 0) {
        return;
    }
    const file = fileInput.files[0];
    const reader = new FileReader();
    reader.onload = function (e) {
        try {
            let definitions = JSON.parse(e.target.result);
            if (!Array.isArray(definitions)) {
                definitions = [definitions];
            }
            $.ajax({
                url: CasActuatorEndpoints.attributeDefinitions(),
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(definitions),
                success: () => {
                    reloadAttributeDefinitionsTable();
                    Swal.fire("Success", `Imported ${definitions.length} attribute definition(s).`, "success");
                },
                error: (xhr, status, error) => {
                    console.error(`Error importing: ${status} / ${error} / ${xhr.responseText}`);
                    displayBanner(xhr);
                }
            });
        } catch (ex) {
            Swal.fire("Error", "Invalid JSON file.", "error");
        }
        fileInput.value = "";
    };
    reader.readAsText(file);
}

function bindAttrDefDeleteButtons() {
    $("button[name=deleteAttrDefn]").off().on("click", function () {
        const key = $(this).attr("data-key");
        if (CasActuatorEndpoints.attributeDefinitions()) {
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
                            url: CasActuatorEndpoints.attributeDefinitions(),
                            type: "DELETE",
                            contentType: "application/json",
                            data: JSON.stringify([key]),
                            success: () => {
                                reloadAttributeDefinitionsTable();
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

    $("button[name=viewAttrDefn]").off().on("click", function () {
        const key = $(this).attr("data-key");
        if (CasActuatorEndpoints.attributeDefinitions()) {
            $.get(`${CasActuatorEndpoints.attributeDefinitions()}/${key}`, response => {
                const editor = initializeAceEditor("viewAttrDefEditor", "json");
                editor.setReadOnly(true);
                editor.setValue(JSON.stringify(response, null, 2), -1);
                editor.gotoLine(1);
                const beautify = ace.require("ace/ext/beautify");
                beautify.beautify(editor.session);
                const dialog = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("viewAttrDefDialog"));
                dialog["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    });

    $("button[name=editAttrDefn]").off().on("click", function () {
        const key = $(this).attr("data-key");
        editAttributeDefinition(key);
    });
}

function toggleAttrDefEditorVisibility() {
    const showEditor = $("#showAttrDefEditor").val();
    const $editorContainer = $("#attrDefEditorContainer");
    const $controlsPanel = $("#attrDefControlsPanel");

    localStorage.setItem("showAttrDefEditorPreference", showEditor);

    if (showEditor === "true" || showEditor === true) {
        showElements($editorContainer);
        $controlsPanel.css({"max-width": "520px"});
        const editor = ace.edit("attrDefEditor");
        if (editor) {
            editor.resize();
            generateAttrDefPayload();
        }
    } else {
        hideElements($editorContainer);
        $controlsPanel.css({"max-width": "none"});
    }
}

function generateAttrDefPayload() {
    const editor = ace.edit("attrDefEditor");
    if (!editor) {
        return;
    }

    const nameVal = $("#attributeDefinitionKey").val() || "";
    const payload = {};

    const definitionType = $("#attributeDefinitionType").val();
    if (definitionType === "SAML") {
        payload["@class"] = "org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition";
    } else if (definitionType === "OIDC") {
        payload["@class"] = "org.apereo.cas.oidc.claims.OidcAttributeDefinition";
    } else if (definitionType === "OAUTH") {
        payload["@class"] = "org.apereo.cas.support.oauth.web.response.accesstoken.ext.OAuthAttributeDefinition";
    } else {
        payload["@class"] = "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition";
    }

    const addStr = (key, id) => {
        const v = $(`#${id}`).val();
        if (v && v.trim().length > 0) {
            payload[key] = v;
        }
    };

    const addBool = (key, id) => {
        const v = $(`#newAttributeDefinitionDialog #${id}`).val();
        if (v === "true") {
            payload[key] = true;
        }
    };

    const addSelect = (key, id, defaultVal) => {
        const v = $(`#${id}`).val();
        if (v && v !== defaultVal) {
            payload[key] = v;
        }
    };

    if (nameVal.trim().length > 0) {
        payload.key = nameVal;
    }

    addStr("name", "attributeDefinitionName");

    addStr("attribute", "attributeDefinitionAttribute");
    addStr("patternFormat", "attributeDefinitionPatternFormat");
    addStr("script", "attributeDefinitionScript");
    addSelect("canonicalizationMode", "attributeDefinitionCanonicalizationMode", "NONE");
    addStr("flattened", "attributeDefinitionFlattened");
    addSelect("hashingStrategy", "attributeDefinitionHashingStrategy", "NONE");
    addStr("expiration", "attributeDefinitionExpiration");
    addBool("scoped", "attributeDefinitionScoped");
    addBool("encrypted", "attributeDefinitionEncrypted");
    addBool("singleValue", "attributeDefinitionSingleValue");

    const patterns = collectAttrDefPatterns();
    if (Object.keys(patterns).length > 0) {
        payload.patterns = {"@class": "java.util.TreeMap", ...patterns};
    }

    if (definitionType === "SAML") {
        addStr("friendlyName", "attributeDefinitionFriendlyName");
        addStr("urn", "attributeDefinitionUrn");
        addStr("salt", "attributeDefinitionSalt");
        addBool("persistent", "attributeDefinitionPersistent");
    } else if (definitionType === "OIDC") {
        addStr("trustFramework", "attributeDefinitionTrustFramework");
        addBool("structured", "attributeDefinitionStructured");
    }

    const hasContent = Object.keys(payload).some(k => k !== "@class");
    editor.setValue(hasContent ? JSON.stringify(payload, null, 2) : "", -1);
}

function collectAttrDefPatterns() {
    const patterns = {};
    const containerId = "registeredServiceAttrDefPatternKeyMapContainer";
    $(`#${containerId} .attrDefPatternKey-map-row`).each(function () {
        const key = $(this).find(`input[id=registeredServiceAttrDefPatternKey]`).val();
        const value = $(this).find(`input[id=registeredServiceAttrDefPatternValue]`).val();
        if (key && key.trim().length > 0) {
            patterns[key] = value || "";
        }
    });
    return patterns;
}

function handleAttributeDefinitionTypeChange(type) {
    hideElements($("#newAttributeDefinitionDialog .SAML"));
    hideElements($("#newAttributeDefinitionDialog .OIDC"));
    if (type === "SAML") {
        showElements($("#newAttributeDefinitionDialog .SAML"));
    } else if (type === "OIDC") {
        showElements($("#newAttributeDefinitionDialog .OIDC"));
    }
    generateAttrDefPayload();
}

function newAttributeDefinition() {
    if (!CasActuatorEndpoints.attributeDefinitions()) {
        return;
    }
    openAttributeDefinitionDialog(null);
}

function openAttributeDefinitionDialog(existingDefinition) {
    const isEditMode = existingDefinition !== null;

    const dialogContainer = $("<div>", {
        id: "newAttributeDefinitionDialog"
    });

    const flexContainer = $("<div>", {
        id: "attrDefDialogContainer",
        class: "d-flex"
    });

    const controlsPanel = $("<div>", {
        id: "attrDefControlsPanel",
        class: "mr-2",
        css: {"min-width": "450px", "max-width": "520px"}
    });

    const editorContainer = $("<div>", {
        id: "attrDefEditorContainer",
        class: "flex-grow-1"
    });

    editorContainer.append(`<pre class="ace-editor ace-relative w-100 h-100 mb-0 mt-0 ace-absolute" id="attrDefEditor"></pre>`);

    flexContainer.append(controlsPanel, editorContainer);
    dialogContainer.append(flexContainer);

    const availableTypes = [
        {value: "DEFAULT", text: "Default"}
    ];
    if (CAS_FEATURES.includes("SAMLIdentityProvider")) {
        availableTypes.push({value: "SAML", text: "SAML2"});
    }
    if (CAS_FEATURES.includes("OpenIDConnect")) {
        availableTypes.push({value: "OIDC", text: "OpenID Connect"});
    }
    if (CAS_FEATURES.includes("OpenIDConnect") || CAS_FEATURES.includes("OAuth")) {
        availableTypes.push({value: "OAUTH", text: "OAUTH"});
    }

    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Type:",
        id: "attributeDefinitionType",
        options: availableTypes,
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "Key",
        name: "attributeDefinitionKey",
        required: true,
        containerId: controlsPanel,
        title: "The key of the attribute definition.",
        cssClasses: "always-show",
        paramName: "key"
    });

    createInputField({
        labelTitle: "Name",
        name: "attributeDefinitionName",
        required: false,
        containerId: controlsPanel,
        title: "The name of the attribute definition.",
        cssClasses: "always-show",
        paramName: "name"
    });

    createInputField({
        labelTitle: "Attribute",
        name: "attributeDefinitionAttribute",
        required: false,
        containerId: controlsPanel,
        title: "The mapped attribute name.",
        cssClasses: "always-show",
        paramName: "attribute"
    });

    createInputField({
        labelTitle: "Pattern Format",
        name: "attributeDefinitionPatternFormat",
        required: false,
        containerId: controlsPanel,
        title: "The pattern format for the attribute value.",
        cssClasses: "always-show",
        paramName: "patternFormat"
    });

    createInputField({
        labelTitle: "Script",
        name: "attributeDefinitionScript",
        required: false,
        containerId: controlsPanel,
        title: "The script to run for the attribute value.",
        cssClasses: "always-show",
        paramName: "script"
    });

    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Canonicalization Mode:",
        id: "attributeDefinitionCanonicalizationMode",
        options: [
            {value: "NONE", text: "NONE"},
            {value: "UPPER", text: "UPPER"},
            {value: "LOWER", text: "LOWER"}
        ],
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "Flattened",
        name: "attributeDefinitionFlattened",
        required: false,
        containerId: controlsPanel,
        title: "Whether the attribute value should be flattened.",
        cssClasses: "always-show",
        paramName: "flattened"
    });

    createSelectField({
        containerId: controlsPanel,
        labelTitle: "Hashing Strategy:",
        id: "attributeDefinitionHashingStrategy",
        options: [
            {value: "NONE", text: "NONE"},
            {value: "HEX", text: "HEX"},
            {value: "BASE64", text: "BASE64"},
            {value: "SHA1", text: "SHA1"},
            {value: "SHA256", text: "SHA256"},
            {value: "SHA512", text: "SHA512"}
        ],
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "Expiration",
        name: "attributeDefinitionExpiration",
        required: false,
        containerId: controlsPanel,
        title: "The expiration for the attribute definition.",
        cssClasses: "always-show",
        paramName: "expiration"
    });

    createInputField({
        labelTitle: "Friendly Name",
        name: "attributeDefinitionFriendlyName",
        required: false,
        containerId: controlsPanel,
        title: "The friendly name of the SAML attribute.",
        cssClasses: "SAML hide",
        paramName: "friendlyName"
    });

    createInputField({
        labelTitle: "URN",
        name: "attributeDefinitionUrn",
        required: false,
        containerId: controlsPanel,
        title: "The URN of the SAML attribute.",
        cssClasses: "SAML hide",
        paramName: "urn"
    });

    createInputField({
        labelTitle: "Salt",
        name: "attributeDefinitionSalt",
        required: false,
        containerId: controlsPanel,
        title: "The salt used for generating persistent identifiers.",
        cssClasses: "SAML hide",
        paramName: "salt"
    });

    createInputField({
        labelTitle: "Trust Framework",
        name: "attributeDefinitionTrustFramework",
        required: false,
        containerId: controlsPanel,
        title: "The trust framework for the OIDC attribute.",
        cssClasses: "OIDC hide",
        paramName: "trustFramework"
    });

    $("body").append(dialogContainer);

    createMappedInputField({
        header: "Patterns",
        containerId: "attrDefControlsPanel",
        keyField: "attrDefPatternKey",
        keyLabel: "Pattern (Regex)",
        valueField: "attrDefPatternValue",
        valueLabel: "Value / Script",
        cssClasses: "always-show",
        onChangeCallback: generateAttrDefPayload
    }).addClass("mb-3");

    $("#newAttributeDefinitionDialogFields").children().each(function () {
        const $clone = $(this).clone(true);
        hideElements($clone.find("[id$='checkBoxPanel']"));
        $clone.find("[id$='checkBoxPanel']").empty();
        if ($clone.find(".always-show").length > 0 || $clone.hasClass("always-show")) {
            showElements($clone.find("[id$='SwitchButtonPanel']"));
        }
        controlsPanel.append($clone);
    });

    const $origFields = $("#newAttributeDefinitionDialogFields").detach();

    const savedEditorPref = localStorage.getItem("showAttrDefEditorPreference");
    if (savedEditorPref !== null) {
        const $editorInput = controlsPanel.find("#showAttrDefEditor");
        $editorInput.val(savedEditorPref);
        const $editorBtn = controlsPanel.find("#showAttrDefEditorButton");
        if (savedEditorPref === "true") {
            $editorBtn.removeClass("mdc-switch--unselected").addClass("mdc-switch--selected");
            $editorBtn.attr("aria-checked", "true");
        } else {
            $editorBtn.removeClass("mdc-switch--selected").addClass("mdc-switch--unselected");
            $editorBtn.attr("aria-checked", "false");
        }
    }


    dialogContainer.dialog({
        title: isEditMode ? "Edit Attribute Definition" : "New Attribute Definition",
        modal: true,
        width: 1600,
        autoOpen: true,
        height: Math.min($(window).height() * 0.8, 950),
        position: {
            my: "center top",
            at: "center top+100",
            of: window
        },
        buttons: {
            Save: function () {
                let valid = true;
                $("#newAttributeDefinitionDialog input:visible").each(function () {
                    if (!this.checkValidity()) {
                        this.reportValidity();
                        valid = false;
                        return false;
                    }
                });
                if (!valid) {
                    return;
                }

                const keyVal = $("#attributeDefinitionKey").val();
                const payload = {
                    "@class": "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
                    key: keyVal,
                    name: $("#attributeDefinitionName").val() || undefined,
                    attribute: $("#attributeDefinitionAttribute").val() || undefined,
                    patternFormat: $("#attributeDefinitionPatternFormat").val() || undefined,
                    script: $("#attributeDefinitionScript").val() || undefined,
                    canonicalizationMode: ($("#attributeDefinitionCanonicalizationMode").val() !== "NONE" ? $("#attributeDefinitionCanonicalizationMode").val() : undefined),
                    flattened: $("#attributeDefinitionFlattened").val() || undefined,
                    hashingStrategy: ($("#attributeDefinitionHashingStrategy").val() !== "NONE" ? $("#attributeDefinitionHashingStrategy").val() : undefined),
                    expiration: $("#attributeDefinitionExpiration").val() || undefined,
                    scoped: $("#newAttributeDefinitionDialog #attributeDefinitionScoped").val() === "true",
                    encrypted: $("#newAttributeDefinitionDialog #attributeDefinitionEncrypted").val() === "true",
                    singleValue: $("#newAttributeDefinitionDialog #attributeDefinitionSingleValue").val() === "true"
                };

                const patterns = collectAttrDefPatterns();
                if (Object.keys(patterns).length > 0) {
                    payload.patterns = {"@class": "java.util.TreeMap", ...patterns};
                }

                const definitionType = $("#attributeDefinitionType").val();
                if (definitionType === "SAML") {
                    payload["@class"] = "org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition";
                    payload.friendlyName = $("#attributeDefinitionFriendlyName").val() || undefined;
                    payload.urn = $("#attributeDefinitionUrn").val() || undefined;
                    payload.salt = $("#attributeDefinitionSalt").val() || undefined;
                    payload.persistent = $("#newAttributeDefinitionDialog #attributeDefinitionPersistent").val() === "true";
                } else if (definitionType === "OIDC") {
                    payload["@class"] = "org.apereo.cas.oidc.claims.OidcAttributeDefinition";
                    payload.trustFramework = $("#attributeDefinitionTrustFramework").val() || undefined;
                    payload.structured = $("#newAttributeDefinitionDialog #attributeDefinitionStructured").val() === "true";
                } else if (definitionType === "OAUTH") {
                    payload["@class"] = "org.apereo.cas.support.oauth.web.response.accesstoken.ext.OAuthAttributeDefinition";
                }

                $.ajax({
                    url: CasActuatorEndpoints.attributeDefinitions(),
                    method: "POST",
                    contentType: "application/json",
                    data: JSON.stringify([payload]),
                    success: () => {
                        dialogContainer.dialog("close");
                        reloadAttributeDefinitionsTable();
                        Swal.fire("Success", isEditMode ? "Attribute definition updated successfully." : "Attribute definition created successfully.", "success");
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
            const $dialog = $(this).closest(".ui-dialog");
            $dialog.css({
                "position": "fixed",
                "top": "20px",
                "left": "50%",
                "transform": "translateX(-50%)",
                "margin": "0"
            });
            $(this).css({
                "overflow-y": "visible"
            });
            $(this).closest(".ui-dialog").css("overflow", "visible");

            const $buttonPane = $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane");
            $buttonPane.css({"display": "flex", "align-items": "center"});
            const $togglesContainer = $("<div>", {
                css: {"display": "flex", "align-items": "center", "margin-right": "auto"}
            });
            controlsPanel.find("#showAttrDefEditorSwitchButtonPanel").detach().appendTo($togglesContainer);
            $buttonPane.prepend($togglesContainer);

            cas.init("#newAttributeDefinitionDialog");
            for (const el of $togglesContainer.find(".mdc-switch").toArray()) {
                const switchElement = new mdc.switchControl.MDCSwitch(el);
                const switchInputs = document.querySelectorAll(`input[data-switch-btn="${el.id}"]`);
                if (switchInputs.length === 1) {
                    el.addEventListener("click", () => {
                        const switchInput = switchInputs[0];
                        switchInput.value = switchElement.selected;
                        $(el).data("param-selected", switchElement.selected);
                        $(switchInput).trigger("change");
                        toggleAttrDefEditorVisibility();
                    });
                }
            }

            const attrDefEditorInstance = initializeAceEditor("attrDefEditor", "json");
            attrDefEditorInstance.setReadOnly(true);

            const showEditor = localStorage.getItem("showAttrDefEditorPreference");
            if (showEditor === "false") {
                hideElements($("#attrDefEditorContainer"));
                $("#attrDefControlsPanel").css({"max-width": "none"});
            }

            $("#newAttributeDefinitionDialog .jqueryui-selectmenu").selectmenu({
                width: "330px",
                change: function (event, ui) {
                    if ($(this).attr("id") === "attributeDefinitionType") {
                        handleAttributeDefinitionTypeChange(ui.item.value);
                    }
                    generateAttrDefPayload();
                }
            });
            handleAttributeDefinitionTypeChange($("#attributeDefinitionType").val());
            $("#attributeDefinitionTypeSelectContainer").css({
                "display": "flex",
                "align-items": "center",
                "white-space": "nowrap",
                "gap": "8px"
            });

            $("#newAttributeDefinitionDialog").on("input", "input[data-param-name]", function () {
                generateAttrDefPayload();
            });
            $("#newAttributeDefinitionDialog").on("change", "input[data-switch-btn]", function () {
                generateAttrDefPayload();
            });
            $togglesContainer.on("change", "input[data-switch-btn]", function () {
                generateAttrDefPayload();
            });

            generateAttrDefPayload();

            if (isEditMode) {
                const cls = existingDefinition["@class"] || "";
                let defType = "DEFAULT";
                if (cls.includes("SamlIdPAttributeDefinition")) {
                    defType = "SAML";
                } else if (cls.includes("OidcAttributeDefinition")) {
                    defType = "OIDC";
                } else if (cls.includes("OAuthAttributeDefinition")) {
                    defType = "OAUTH";
                }
                $("#attributeDefinitionType").val(defType);
                $("#attributeDefinitionType").selectmenu("refresh");
                handleAttributeDefinitionTypeChange(defType);

                const setVal = (id, value) => {
                    if (value !== undefined && value !== null) {
                        const $input = $(`#${id}`);
                        $input.val(value).trigger("input");
                        const $label = $input.closest(".mdc-text-field").find(".mdc-floating-label");
                        if ($label.length > 0 && String(value).length > 0) {
                            $label.addClass("mdc-floating-label--float-above");
                        }
                    }
                };
                const setBool = (id, value) => {
                    const el = $(`#newAttributeDefinitionDialog #${id}`);
                    if (el.length > 0) {
                        el.val(value === true ? "true" : "false");
                        const btnId = el.attr("data-switch-btn");
                        if (btnId) {
                            const btn = $(`#${btnId}`);
                            if (value === true) {
                                btn.removeClass("mdc-switch--unselected").addClass("mdc-switch--selected");
                                btn.attr("aria-checked", "true");
                            } else {
                                btn.removeClass("mdc-switch--selected").addClass("mdc-switch--unselected");
                                btn.attr("aria-checked", "false");
                            }
                        }
                    }
                };

                setVal("attributeDefinitionKey", existingDefinition.key);
                setVal("attributeDefinitionName", existingDefinition.name);
                setVal("attributeDefinitionAttribute", existingDefinition.attribute);
                setVal("attributeDefinitionPatternFormat", existingDefinition.patternFormat);
                setVal("attributeDefinitionScript", existingDefinition.script);
                setVal("attributeDefinitionFlattened", existingDefinition.flattened);
                setVal("attributeDefinitionExpiration", existingDefinition.expiration);

                if (existingDefinition.canonicalizationMode) {
                    $("#attributeDefinitionCanonicalizationMode").val(existingDefinition.canonicalizationMode);
                    $("#attributeDefinitionCanonicalizationMode").selectmenu("refresh");
                }
                if (existingDefinition.hashingStrategy) {
                    $("#attributeDefinitionHashingStrategy").val(existingDefinition.hashingStrategy);
                    $("#attributeDefinitionHashingStrategy").selectmenu("refresh");
                }

                setBool("attributeDefinitionScoped", existingDefinition.scoped);
                setBool("attributeDefinitionEncrypted", existingDefinition.encrypted);
                setBool("attributeDefinitionSingleValue", existingDefinition.singleValue);

                if (defType === "SAML") {
                    setVal("attributeDefinitionFriendlyName", existingDefinition.friendlyName);
                    setVal("attributeDefinitionUrn", existingDefinition.urn);
                    setVal("attributeDefinitionSalt", existingDefinition.salt);
                    setBool("attributeDefinitionPersistent", existingDefinition.persistent);
                } else if (defType === "OIDC") {
                    setVal("attributeDefinitionTrustFramework", existingDefinition.trustFramework);
                    setBool("attributeDefinitionStructured", existingDefinition.structured);
                }

                if (existingDefinition.patterns) {
                    for (const [patternKey, patternValue] of Object.entries(existingDefinition.patterns)) {
                        if (patternKey !== "@class") {
                            const $container = $("#registeredServiceAttrDefPatternKeyMapContainer");
                            const $addBtn = $container.find("button[name=addAttrDefPatternKeyMapEntry]");
                            if ($addBtn.length > 0) {
                                $addBtn.trigger("click");
                                const $rows = $container.find(".attrDefPatternKey-map-row");
                                const $lastRow = $rows.last();
                                $lastRow.find("input[id=registeredServiceAttrDefPatternKey]").val(patternKey);
                                $lastRow.find("input[id=registeredServiceAttrDefPatternValue]").val(patternValue);
                            }
                        }
                    }
                }

                generateAttrDefPayload();
            }

            $("#attributeDefinitionKey").trigger("focus");
        },
        close: function () {
            // Restore original fields for future dialog opens
            $("#attributedefinitions-tab").prepend($origFields);
            $(this).dialog("destroy");
            dialogContainer.remove();
        }
    });
}

function editAttributeDefinition(key) {
    if (!CasActuatorEndpoints.attributeDefinitions()) {
        return;
    }
    $.get(`${CasActuatorEndpoints.attributeDefinitions()}/${key}`, response => {
        openAttributeDefinitionDialog(response);
    }).fail((xhr, status, error) => {
        console.error("Error fetching attribute definition:", error);
        displayBanner(xhr);
    });
}
