let reloadTenantsTable = null;

async function initializeMultitenancyOperations() {
    const tenantsToolbar = document.createElement("div");
    tenantsToolbar.innerHTML = `
        <button type="button" id="newTenantButton"
                onclick="newTenant()"
                title="Create New Tenant"
                class="mdc-button mdc-button--raised">
            <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>New</span>
        </button>
        <button type="button" id="exportTenantsButton"
                onclick="exportTenants()"
                title="Export Tenants"
                class="mdc-button mdc-button--raised ms-2">
            <span class="mdc-button__label"><i class="mdc-tab__icon mdi mdi-download" aria-hidden="true"></i>Export</span>
        </button>
    `;

    const tenantsTable = $("#tenantsTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        layout: {
            topStart: tenantsToolbar
        },
        drawCallback: settings => {
            $("#tenantsTable tr").addClass("mdc-data-table__row");
            $("#tenantsTable td").addClass("mdc-data-table__cell");
        }
    });

    function fetchTenants() {
        tenantsTable.clear();
        $.get(`${CasActuatorEndpoints.multitenancy()}/tenants`, response => {
            for (const tenant of Object.values(response)) {
                let buttons = `
                     <button type="button" name="viewTenantDefinition" href="#" 
                            title="View Tenant Definition"
                            data-tenant-id='${tenant.id}' onclick="showTenantDefinition('${tenant.id}')"
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-eye min-width-32x" aria-hidden="true"></i>
                    </button>
                    <button type="button" name="editTenantDefinition" href="#"
                            title="Edit Tenant"
                            data-tenant-id='${tenant.id}' onclick="editTenant('${tenant.id}')"
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-pencil min-width-32x" aria-hidden="true"></i>
                    </button>
                    <button type="button" name="duplicateTenantDefinition" href="#"
                            title="Duplicate Tenant"
                            data-tenant-id='${tenant.id}' onclick="duplicateTenant('${tenant.id}')"
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-content-copy min-width-32x" aria-hidden="true"></i>
                    </button>
                    <button type="button" name="deleteTenantDefinition" href="#"
                            title="Delete Tenant"
                            data-tenant-id='${tenant.id}' onclick="deleteTenant('${tenant.id}', this)"
                            class="mdc-button mdc-button--raised min-width-32x">
                        <i class="mdi mdi-delete min-width-32x" aria-hidden="true"></i>
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
    reloadTenantsTable = fetchTenants;
    setInterval(() => {
        if (currentActiveTab === Tabs.MULTITENANCY.index) {
            fetchTenants();
        }
    }, palantirSettings().refreshInterval);
}

function toggleTenantJsonEditorVisibility() {
    const showEditor = $("#showTenantJsonEditor").val();
    const $editorContainer = $("#tenantEditorContainer");
    const $controlsPanel = $("#tenantControlsPanel");

    localStorage.setItem("showTenantJsonEditorPreference", showEditor);

    if (showEditor === "true" || showEditor === true) {
        showElements($editorContainer);
        $controlsPanel.css({"max-width": "520px"});
        setTimeout(() => {
            const editor = ace.edit("tenantEditor");
            if (editor) {
                editor.resize(true);
                editor.renderer.updateFull();
            }
        }, 50);
    } else {
        hideElements($editorContainer);
        $controlsPanel.css({"max-width": "none"});
    }
}

function generateTenantPayload() {
    const editor = ace.edit("tenantEditor");
    if (!editor) {
        return;
    }

    let payload = { };

    const idVal = $("#tenantId").val();
    if (idVal && idVal.trim().length > 0) {
        payload.id = idVal.trim();
    }

    const descVal = $("#tenantDescription").val();
    if (descVal && descVal.trim().length > 0) {
        payload.description = descVal.trim();
    }

    const parseCsvToList = val => {
        if (!val || val.trim().length === 0) {
            return [];
        }
        return val.split(",").map(s => s.trim()).filter(s => s.length > 0);
    };

    const authPolicy = {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantAuthenticationPolicy"
    };
    const authHandlers = parseCsvToList($("#tenantAuthenticationHandlers").val());
    if (authHandlers.length > 0) {
        authPolicy.authenticationHandlers = ["java.util.ArrayList", authHandlers];
    }
    const attrRepos = parseCsvToList($("#tenantAttributeRepositories").val());
    if (attrRepos.length > 0) {
        authPolicy.attributeRepositories = ["java.util.ArrayList", attrRepos];
    }

    const authProtocolPolicy = {
        "@class": "org.apereo.cas.multitenancy.TenantCasAuthenticationProtocolPolicy"
    };
    const supportedProtocols = parseCsvToList($("#tenantSupportedProtocols").val());
    if (supportedProtocols.length > 0) {
        authProtocolPolicy.supportedProtocols = ["java.util.HashSet", supportedProtocols];
    }
    if (Object.keys(authProtocolPolicy).length > 1) {
        authPolicy.authenticationProtocolPolicy = authProtocolPolicy;
    }

    if (Object.keys(authPolicy).length > 1) {
        payload.authenticationPolicy = authPolicy;
    }

    const delegatedPolicy = {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantDelegatedAuthenticationPolicy"
    };
    const allowedProviders = parseCsvToList($("#tenantExternalIdentityProviders").val());
    if (allowedProviders.length > 0) {
        delegatedPolicy.allowedProviders = ["java.util.ArrayList", allowedProviders];
    }
    if (Object.keys(delegatedPolicy).length > 1) {
        payload.delegatedAuthenticationPolicy = delegatedPolicy;
    }

    const uiPolicy = {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantUserInterfacePolicy"
    };
    const themeNameVal = $("#tenantThemeName").val();
    if (themeNameVal && themeNameVal.trim().length > 0) {
        uiPolicy.themeName = themeNameVal.trim();
    }
    if (Object.keys(uiPolicy).length > 1) {
        payload.userInterfacePolicy = uiPolicy;
    }

    const properties = {};
    $("#registeredServiceTenantPropertyKeyMapContainer .tenantPropertyKey-map-row").each(function () {
        const key = $(this).find("input[id=registeredServiceTenantPropertyKey]").val();
        const value = $(this).find("input[id=registeredServiceTenantPropertyValue]").val();
        if (key && key.trim().length > 0) {
            properties[key] = value || "";
        }
    });
    if (Object.keys(properties).length > 0) {
        payload.properties = {"@class": "java.util.TreeMap", ...properties};
    }

    if (Object.keys(payload).length > 0) {
        payload = {
            "@class": "org.apereo.cas.multitenancy.TenantDefinition",
            ...payload
        };
        editor.setValue(JSON.stringify(payload, null, 2), -1);
    } else {
        editor.setValue("");
    }
}

function exportTenants() {
    $.get(`${CasActuatorEndpoints.multitenancy()}/tenants`, response => {
        const blob = new Blob([JSON.stringify(response, null, 2)], {type: "application/json"});
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "tenants.json";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }).fail((xhr, status, error) => {
        console.error("Error exporting data:", error);
        displayBanner(xhr);
    });
}

function editTenant(id) {
    $.get(`${CasActuatorEndpoints.multitenancy()}/tenants/${id}`, response => {
        newTenant(response, {editMode: true});
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayBanner(xhr);
    });
}

function duplicateTenant(id) {
    $.get(`${CasActuatorEndpoints.multitenancy()}/tenants/${id}`, response => {
        newTenant(response);
    }).fail((xhr, status, error) => {
        console.error("Error fetching data:", error);
        displayBanner(xhr);
    });
}

function extractArray(field) {
    if (!field) {
        return [];
    }
    if (Array.isArray(field) && field.length === 2 && typeof field[0] === "string" && Array.isArray(field[1])) {
        return field[1];
    }
    if (Array.isArray(field)) {
        return field;
    }
    return [];
}

function setFieldValue(selector, value) {
    if (!value || String(value).trim().length === 0) {
        return;
    }
    const $input = $(selector);
    $input.val(value);
    const $textField = $input.closest(".mdc-text-field");
    $textField.addClass("mdc-text-field--label-floating");
    $textField.find(".mdc-floating-label").addClass("mdc-floating-label--float-above");
    $textField.find(".mdc-notched-outline").addClass("mdc-notched-outline--notched");
}

function prefillTenantDialog(data, editMode = false) {
    setFieldValue("#tenantDescription", data.description);

    const authPolicy = data.authenticationPolicy;
    if (authPolicy) {
        setFieldValue("#tenantAuthenticationHandlers", extractArray(authPolicy.authenticationHandlers).join(", "));
        setFieldValue("#tenantAttributeRepositories", extractArray(authPolicy.attributeRepositories).join(", "));
        const proto = authPolicy.authenticationProtocolPolicy;
        if (proto) {
            setFieldValue("#tenantSupportedProtocols", extractArray(proto.supportedProtocols).join(", "));
        }
    }

    const delegated = data.delegatedAuthenticationPolicy;
    if (delegated) {
        setFieldValue("#tenantExternalIdentityProviders", extractArray(delegated.allowedProviders).join(", "));
    }

    const uiPolicy = data.userInterfacePolicy;
    if (uiPolicy && uiPolicy.themeName) {
        setFieldValue("#tenantThemeName", uiPolicy.themeName);
    }

    const properties = data.properties;
    if (properties) {
        // Handle both plain-object {"@class":"...", k:v} and typed-array ["java.util.TreeMap",{k:v}] formats
        let propsObj = (Array.isArray(properties) && properties.length === 2 && typeof properties[1] === "object")
            ? properties[1] : properties;
        const entries = Object.entries(propsObj).filter(([k]) => k !== "@class");
        if (entries.length > 0) {
            // Scope through the first row's parent to avoid stale/duplicate global ID matches
            const $firstRow = $("#registeredServiceTenantPropertyKeyMapContainer .tenantPropertyKey-map-row").first();
            const $container = $firstRow.parent();
            const $toAppend = $container.children("span[id$='ToAppend']");

            for (let i = 1; i < entries.length; i++) {
                const $newRow = $firstRow.clone(false);
                $newRow.find("input").val("");
                $newRow.find(".mdc-text-field").removeClass("mdc-text-field--label-floating");
                $newRow.find(".mdc-floating-label").removeClass("mdc-floating-label--float-above");
                if ($toAppend.length > 0) {
                    $toAppend.append($newRow);
                } else {
                    // Fallback: insert after the first row
                    $firstRow.after($newRow);
                }
            }

            const allRows = $container.find(".tenantPropertyKey-map-row");

            entries.forEach(([key, value], idx) => {
                const row = allRows.eq(idx);
                row.find("input").filter((_i, el) => el.id === "registeredServiceTenantPropertyKey").val(key);
                row.find("input").filter((_i, el) => el.id === "registeredServiceTenantPropertyValue").val(value);
                row.find("input").each(function () {
                    if ($(this).val()) {
                        const $tf = $(this).closest(".mdc-text-field");
                        $tf.addClass("mdc-text-field--label-floating");
                        $tf.find(".mdc-floating-label").addClass("mdc-floating-label--float-above");
                        $tf.find(".mdc-notched-outline").addClass("mdc-notched-outline--notched");
                    }
                });
            });

            // Re-wire remove buttons and input-change callbacks for all rows
            $container.find("button[name$='RemoveButton']")
                .off("click").on("click", function () {
                    $(this).closest(".tenantPropertyKey-map-row").remove();
                    generateTenantPayload();
                });
            $container.find("input")
                .off("input").on("input", () => generateTenantPayload());
        }
    }

    if (editMode && data.id) {
        setFieldValue("#tenantId", data.id);
        $("#tenantId").prop("readonly", true).prop("disabled", true);
        $("#tenantDescription").trigger("focus");
    } else {
        $("#tenantId").val("").trigger("focus");
    }
    generateTenantPayload();
}

function newTenant(prefillData = null, options = {}) {
    const editMode = options.editMode === true;
    const dialogContainer = $("<div>", {
        id: "newTenantDialog"
    });

    const flexContainer = $("<div>", {
        id: "tenantDialogContainer",
        class: "d-flex"
    });

    const controlsPanel = $("<div>", {
        id: "tenantControlsPanel",
        class: "mr-2 overflow-auto",
        css: {"min-width": "450px", "max-width": "520px"}
    });

    const editorContainer = $("<div>", {
        id: "tenantEditorContainer",
        class: "flex-grow-1 d-flex flex-column",
        css: {"max-height": "700px"}
    });

    editorContainer.append(`
        <pre class="ace-editor ace-relative w-100 mb-0 mt-0" id="tenantEditor"></pre>
    `);

    flexContainer.append(controlsPanel, editorContainer);
    dialogContainer.append(flexContainer);

    createInputField({
        labelTitle: "ID",
        name: "tenantId",
        paramName: "id",
        required: true,
        containerId: controlsPanel,
        title: "Define a unique identifier for this tenant.",
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "Description",
        name: "tenantDescription",
        paramName: "description",
        required: false,
        containerId: controlsPanel,
        title: "Describe this tenant.",
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "Authentication Handlers",
        name: "tenantAuthenticationHandlers",
        paramName: "authenticationHandlers",
        required: false,
        containerId: controlsPanel,
        title: "Comma-separated list of authentication handler bean names for this tenant.",
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "Attribute Repositories",
        name: "tenantAttributeRepositories",
        paramName: "attributeRepositories",
        required: false,
        containerId: controlsPanel,
        title: "Comma-separated list of attribute repository bean names for this tenant.",
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "Supported Protocols",
        name: "tenantSupportedProtocols",
        paramName: "supportedProtocols",
        required: false,
        containerId: controlsPanel,
        title: "Comma-separated list of supported authentication protocols (e.g. CAS30, SAML1).",
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "External Identity Providers",
        name: "tenantExternalIdentityProviders",
        paramName: "delegatedAuthenticationPolicy",
        required: false,
        containerId: controlsPanel,
        title: "Comma-separated list of external identity provider names allowed for this tenant.",
        cssClasses: "always-show"
    });

    createInputField({
        labelTitle: "Theme",
        name: "tenantThemeName",
        paramName: "userInterfacePolicy",
        required: false,
        containerId: controlsPanel,
        title: "Name of the UI theme to apply for this tenant.",
        cssClasses: "always-show"
    });

    $("body").append(dialogContainer);

    createMappedInputField({
        header: "Properties",
        containerId: "tenantControlsPanel",
        keyField: "tenantPropertyKey",
        keyLabel: "Key",
        valueField: "tenantPropertyValue",
        valueLabel: "Value",
        cssClasses: "always-show",
        onChangeCallback: generateTenantPayload
    }).addClass("mb-3");

    $("#newTenantDialogFields").children().each(function () {
        const $clone = $(this).clone(true);
        hideElements($clone.find("[id$='checkBoxPanel']"));
        $clone.find("[id$='checkBoxPanel']").empty();
        if ($clone.find(".always-show").length > 0 || $clone.hasClass("always-show")) {
            showElements($clone.find("[id$='SwitchButtonPanel']"));
        }
        controlsPanel.append($clone);
    });

    const $origFields = $("#newTenantDialogFields").detach();
                               
    const savedEditorPref = localStorage.getItem("showTenantJsonEditorPreference");
    if (savedEditorPref !== null) {
        const $editorInput = controlsPanel.find("#showTenantJsonEditor");
        $editorInput.val(savedEditorPref);
        const $editorBtn = controlsPanel.find("#showTenantJsonEditorButton");
        if (savedEditorPref === "true") {
            $editorBtn.removeClass("mdc-switch--unselected").addClass("mdc-switch--selected");
            $editorBtn.attr("aria-checked", "true");
        } else {
            $editorBtn.removeClass("mdc-switch--selected").addClass("mdc-switch--unselected");
            $editorBtn.attr("aria-checked", "false");
        }
    }

    dialogContainer.dialog({
        title: editMode ? "Edit Tenant" : "New Tenant",
        modal: true,
        width: 1400,
        autoOpen: false,
        position: {
            my: "center top",
            at: "center top+50",
            of: window
        },
        buttons: {
            Save: function () {
                const $dlg = $(this);
                const editor = ace.edit("tenantEditor");
                if (!editor) {
                    return;
                }
                const json = editor.getValue();
                if (!json || json.trim().length === 0) {
                    return;
                }
                let payload;
                try {
                    payload = JSON.parse(json);
                } catch (e) {
                    displayBanner(e);
                    return;
                }
                $.ajax({
                    url: `${CasActuatorEndpoints.multitenancy()}/tenants`,
                    method: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(payload),
                    success: () => {
                        $dlg.dialog("close");
                        if (reloadTenantsTable) {
                            reloadTenantsTable();
                        }
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
                "top": "50px",
                "left": "50%",
                "transform": "translateX(-50%)",
                "margin": "0"
            });

            const maxHeight = $(window).height() - 200;
            $(this).css({
                "max-height": maxHeight + "px",
                "overflow-y": "auto",
                "overflow-x": "visible"
            });
            $(this).closest(".ui-dialog").css("overflow", "visible");

            const editorHeight = maxHeight - 40;
            $("#tenantEditor").css("height", editorHeight + "px");

            const $buttonPane = $dialog.find(".ui-dialog-buttonpane");
            $buttonPane.css({"display": "flex", "align-items": "center"});
            const $togglesContainer = $("<div>", {
                css: {"display": "flex", "align-items": "center", "margin-right": "auto"}
            });
            controlsPanel.find("#showTenantJsonEditorSwitchButtonPanel").detach().appendTo($togglesContainer);
            $buttonPane.prepend($togglesContainer);

            cas.init("#newTenantDialog");
            for (const el of $togglesContainer.find(".mdc-switch").toArray()) {
                const switchElement = new mdc.switchControl.MDCSwitch(el);
                const switchInputs = document.querySelectorAll(`input[data-switch-btn="${el.id}"]`);
                if (switchInputs.length === 1) {
                    el.addEventListener("click", () => {
                        const switchInput = switchInputs[0];
                        switchInput.value = switchElement.selected;
                        $(el).data("param-selected", switchElement.selected);
                        $(switchInput).trigger("change");
                        toggleTenantJsonEditorVisibility();
                    });
                }
            }
            cas.init(".ui-dialog-buttonpane");

            const tenantEditorInstance = initializeAceEditor("tenantEditor", "json");
            tenantEditorInstance.setReadOnly(true);
            setTimeout(() => {
                tenantEditorInstance.resize(true);
                tenantEditorInstance.renderer.updateFull();
            }, 50);

            $("#newTenantDialog").on("input", "input[data-param-name]", function () {
                generateTenantPayload();
            });

            if (prefillData) {
                prefillTenantDialog(prefillData, editMode);
            } else {
                generateTenantPayload();
            }

            const showEditor = localStorage.getItem("showTenantJsonEditorPreference");
            if (showEditor === "false") {
                hideElements($("#tenantEditorContainer"));
                $("#tenantControlsPanel").css({"max-width": "none"});
            }
        },
        close: function () {
            $("#tenants-tab").append($origFields);
            $(this).dialog("destroy").remove();
        }
    });

    dialogContainer.dialog("open");
}

function deleteTenant(id, btn) {
    Swal.fire({
        title: `Are you sure you want to delete tenant <strong>${id}</strong>?`,
        text: "Once removed, you may not be able to revert this.",
        icon: "question",
        showConfirmButton: true,
        showDenyButton: true
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: `${CasActuatorEndpoints.multitenancy()}/tenants/${id}`,
                method: "DELETE",
                contentType: "application/json"
            })
                .done(() => {
                    const table = $("#tenantsTable").DataTable();
                    table.row($(btn).closest("tr")).remove().draw(false);
                })
                .fail((xhr, status, error) => {
                    console.error("Error:", status, error);
                    displayBanner(xhr);
                });
        }
    });
}

function showTenantDefinition(id) {
    $.get(`${CasActuatorEndpoints.multitenancy()}/tenants/${id}`, response => {
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
