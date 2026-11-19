async function initializeServicesOperations() {
    const columnDefinitions = [
        {visible: true, targets: 0, width: "84px", className: "palantir-application-type"},
        {visible: true, targets: 1},
        {visible: true, targets: 2},
        {visible: true, targets: 3},
        {visible: false, targets: 4}
    ];

    const applicationsTable = $("#applicationsTable").DataTable({
        deferRender: true,
        pageLength: 25,
        autoWidth: false,
        columnDefs: columnDefinitions,
        rowCallback: (row, data) => {
            row.classList.toggle("is-disabled", PalantirApplicationAccess.isServiceDisabled(data.serviceId));
            row.classList.toggle("is-checked", PalantirApplicationSelection.has(data.serviceId));
        },
        drawCallback: settings => {
            $("#applicationsTable tr").addClass("mdc-data-table__row");
            $("#applicationsTable td").addClass("mdc-data-table__cell");
            PalantirApplicationSelection.refresh();
        }
    });
    applicationsTable.on("click", "tbody tr", e => e.currentTarget.classList.remove("selected"));
    PalantirApplicationSelection.initialize();

    $("#entityHistoryTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        order: [[3, "asc"]],
        layout: {topStart: null, topEnd: null},
        columnDefs: [
            {visible: false, type: "num", targets: 3}
        ],
        drawCallback: settings => {
            $("#entityHistoryTable tr").addClass("mdc-data-table__row");
            $("#entityHistoryTable td").addClass("mdc-data-table__cell");
        }
    });

    initializeApplicationCards();
    initializeApplicationTypeFilter();
    await fetchServices();
    await initializeFooterButtons();
}

let serviceEditorInstance = null;
let editServiceDialogInstance = null;

/**
 * Sizes the plain editor dialog to the window, leaving a small margin.
 *
 * Matches the wizard beside it: the editor is a working surface rather than a message, so it takes
 * the whole window, and Ace is told to re-measure afterwards since it cannot detect a container
 * resize on its own.
 */
function sizeServiceEditorDialog() {
    const element = $("#editServiceDialog");
    if (!element.hasClass("ui-dialog-content") || !element.dialog("isOpen")) {
        return;
    }
    const margin = 24;
    element.dialog("option", {
        width: Math.max(320, $(window).width() - margin * 2),
        height: Math.max(320, $(window).height() - margin * 2)
    });
    element.dialog("option", "position", {my: "center", at: "center", of: window});
    serviceEditorInstance?.resize(true);
}

/**
 * The plain service-definition editor, as a jQuery UI dialog.
 *
 * A jQuery UI dialog like the wizard and every other dialog in Palantir: the widget supplies the
 * focus trap, Escape handling, a title bar and role=dialog, which the hand-written MDC markup did
 * not. The returned handle exposes open and close so the shared save handler can drive this dialog
 * and the wizard's through the same two calls.
 *
 * @returns {{open: function, close: function}} a handle onto the dialog
 */
function getEditServiceDialogInstance() {
    if (!editServiceDialogInstance) {
        const element = $("#editServiceDialog");
        if (!element.hasClass("ui-dialog-content")) {
            element.removeClass("hide").dialog({
                autoOpen: false,
                modal: true,
                closeOnEscape: true,
                draggable: false,
                resizable: false,
                classes: {"ui-dialog": "ui-corner-all registered-service-wizard-dialog"},
                beforeClose: () => PalantirServiceEditGuard.beforeClose("editor"),
                close: () => {
                    $(window).off("resize.serviceEditor");
                    PalantirServiceEditGuard.closed("editor");
                }
            });
        }
        editServiceDialogInstance = {
            open: () => {
                element.dialog("open");
                sizeServiceEditorDialog();
                $(window)
                    .off("resize.serviceEditor")
                    .on("resize.serviceEditor", sizeServiceEditorDialog);
                const text = serviceEditorInstance.getValue();
                let serviceId = null;
                if (element.attr("newService") !== "true") {
                    try {
                        serviceId = JSON.parse(text).id;
                    } catch (error) {
                        serviceId = null;
                    }
                }
                const key = PalantirServiceEditGuard.key("editor", serviceId);
                const carried = PalantirServiceEditor.pendingText !== null;
                PalantirServiceEditGuard.offerDraft(key, text).then(draft => {
                    if (draft !== null) {
                        serviceEditorInstance.setValue(draft, -1);
                    }
                    PalantirServiceEditGuard.watch("editor", key, draft !== null || carried);
                    PalantirServiceEditor.opened(element.attr("newService") === "true", draft !== null);
                });
            },
            close: () => element.dialog("close")
        };
    }
    return editServiceDialogInstance;
}

/**
 * The content type the import endpoint expects for a file of application definitions, chosen by its extension.
 *
 * @param file the chosen file
 * @returns {string} the content type
 */
function serviceImportContentType(file) {
    const name = file.name.toLowerCase();
    if (name.endsWith(".zip")) {
        return "application/octet-stream";
    }
    if (name.endsWith(".yaml") || name.endsWith(".yml")) {
        return "application/yaml";
    }
    return "application/json";
}

/**
 * Import a JSON or YAML application definition, or a zip of JSON definitions as exported by Palantir, once the
 * operator confirms it.
 *
 * @param file the chosen file
 * @returns {Promise<void>} resolved once the import is sent or declined
 */
/**
 * Say why CAS refused an application definition when it said why, and fall back to the usual banner otherwise.
 *
 * @param xhr the failed request
 * @param title what failed
 */
function showUnreadableDefinition(xhr, title) {
    const reason = xhr?.status === 400 ? xhr.responseJSON?.error : null;
    if (reason) {
        Swal.fire({
            icon: "error",
            title: title,
            text: `CAS cannot read this definition: ${reason}`
        });
    } else {
        displayBanner(xhr);
    }
}

async function importServiceFile(file) {
    const contentType = serviceImportContentType(file);
    const archive = contentType === "application/octet-stream";
    const result = await Swal.fire({
        titleText: `Import ${file.name}?`,
        text: archive
            ? "Every JSON application definition in the archive takes effect immediately, replacing an application with the same id."
            : "The application takes effect immediately, replacing an application with the same id.",
        icon: "question",
        showDenyButton: true,
        confirmButtonText: "Import",
        denyButtonText: "Cancel"
    });
    if (!result.isConfirmed) {
        return;
    }
    const button = $("button[name=importService]");
    const wasDisabled = button.prop("disabled");
    button.prop("disabled", true);
    const processing = archive ? "CAS is reading and saving the applications in the archive..." : "CAS is reading and saving the application...";
    PalantirProgress.open(archive ? "Importing applications" : "Importing an application",
        `Uploading ${file.name}...`, `${file.name} · ${PalantirProgress.size(file.size)}`);
    try {
        const request = $.ajax({
            url: `${CasActuatorEndpoints.registeredServices()}/import`,
            type: "POST",
            contentType: contentType,
            data: file,
            processData: false,
            dataType: "text",
            xhr: () => {
                const xhr = $.ajaxSettings.xhr();
                xhr.upload?.addEventListener("progress", event => {
                    if (event.lengthComputable && event.loaded < event.total) {
                        PalantirProgress.update(`Uploading ${file.name}...`, event.loaded / event.total * 100);
                    } else {
                        PalantirProgress.update(processing);
                    }
                });
                xhr.upload?.addEventListener("load", () => PalantirProgress.update(processing));
                return xhr;
            }
        });
        await request;
        PalantirProgress.update("Refreshing the applications...");
        await PalantirApplicationSelection.reload();
        PalantirProgress.close();
        const id = request.getResponseHeader("id");
        Swal.fire({
            title: "Imported",
            text: archive
                ? `The applications in ${file.name} are imported.`
                : `${file.name} is imported${id ? ` as application ${id}` : ""}.`,
            icon: "success"
        });
    } catch (xhr) {
        PalantirProgress.close();
        if (xhr?.status === 400) {
            Swal.fire({
                title: "Nothing imported",
                text: `CAS could not read ${file.name} as an application definition.`,
                icon: "error"
            });
        } else if (xhr?.statusText !== PALANTIR_READ_ONLY) {
            console.error("Importing applications failed:", xhr?.status, xhr?.responseText);
            displayBanner(xhr);
        }
    } finally {
        button.prop("disabled", wasDisabled);
    }
}

async function initializeServiceButtons() {
    PalantirServiceEditGuard.initialize();
    getEditServiceDialogInstance();
    serviceEditorInstance = PalantirServiceEditor.editor();

    function viewEntityHistory(serviceId) {
        PalantirServiceHistory.open(serviceId);
    }

    initializeDataTableContextMenu({
        table: $("#entityHistoryTable").DataTable(),
        selector: "#entityHistoryTable tbody tr",
        items: {
            restore: {name: "Restore Version", write: "entityHistory", icon: contextMenuIcon("mdi-history")}
        },
        callback: (key, context) => {
            if (key === "restore") {
                PalantirServiceHistory.restore(context.rowData.serviceId, context.rowData.revisionId);
            }
        }
    });

    function viewEntityChangelog(serviceId) {
        $.get(`${CasActuatorEndpoints.entityHistory()}/registeredServices/${serviceId}/changelog`, response => {
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
    }

    function deleteService(context) {
        const serviceId = context.rowData.serviceId;
        if (CasActuatorEndpoints.registeredServices()) {
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
                            url: `${CasActuatorEndpoints.registeredServices()}/${serviceId}`,
                            type: "DELETE",
                            headers: {"Content-Type": "application/json"},
                            success: async () => {
                                $("#applicationsTable").DataTable().row(context.$row).remove().draw();
                                await fetchServices(() => updateNavigationSidebar());
                            },
                            error: (xhr, status, error) => {
                                console.error("Error deleting resource:", error);
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    }

    function editService(serviceId, wizard) {
        if (CasActuatorEndpoints.registeredServices()) {
            $.get(`${CasActuatorEndpoints.registeredServices()}/${serviceId}`, response => {
                if (wizard) {
                    PalantirServiceEditor.openApplicationInWizard(response);
                    return;
                }
                const value = JSON.stringify(response, null, 4);
                PalantirServiceEditGuard.remember(PalantirServiceEditGuard.key("editor", response.id), response);
                serviceEditorInstance.setValue(value, -1);
                serviceEditorInstance.gotoLine(1);
                const editServiceDialogElement = document.getElementById("editServiceDialog");
                $(editServiceDialogElement).attr("newService", false);
                getEditServiceDialogInstance()["open"]();
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    }

    $("button[name=saveService],button[name=saveServiceWizard]").off().on("click", async function () {
        if (CasActuatorEndpoints.registeredServices()) {
            let isNewService = false;
            let value = "";
            let dialogInstance = null;

            const saveButton = $(this);
            switch (saveButton.attr("name")) {
            case "saveServiceWizard":
                if (!PalantirWizardValidity.report()) {
                    return;
                }
                const editServiceWizardDialogElement = document.getElementById("editServiceWizardDialog");
                isNewService = $(editServiceWizardDialogElement).attr("newService") === "true";
                const wizardEditor = initializeAceEditor("wizardServiceEditor");
                value = wizardEditor.getValue();
                dialogInstance = editServiceWizardDialog;
                break;
            case "saveService":
                const editServiceDialogElement = document.getElementById("editServiceDialog");
                isNewService = $(editServiceDialogElement).attr("newService") === "true";
                value = serviceEditorInstance.getValue();
                dialogInstance = getEditServiceDialogInstance();
                break;
            }

            const mode = saveButton.attr("name") === "saveServiceWizard" ? "wizard" : "editor";
            const conflict = isNewService ? "save" : await PalantirServiceEditGuard.checkConflict(mode, value);
            if (conflict === "cancel") {
                return;
            }
            const confirmation = conflict === "overwrite"
                ? Promise.resolve({isConfirmed: true})
                : Swal.fire({
                    title: `Are you sure you want to ${isNewService ? "create" : "update"} this entry?`,
                    text: "Once updated, you may not be able to revert this entry.",
                    icon: "question",
                    showConfirmButton: true,
                    showDenyButton: true
                });
            confirmation
                .then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `${CasActuatorEndpoints.registeredServices()}`,
                            type: isNewService ? "POST" : "PUT",
                            contentType: "application/json",
                            data: value,
                            success: async response => {
                                PalantirServiceEditGuard.saved(mode);
                                dialogInstance["close"]();
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
                                const reason = xhr?.status === 400 ? xhr.responseJSON?.error : null;
                                if (mode === "editor" && reason) {
                                    PalantirServiceEditor.showProblem("Not saved", `CAS cannot read this definition: ${reason}`, PalantirServiceEditor.rowOf(reason));
                                } else {
                                    showUnreadableDefinition(xhr, "The application was not saved");
                                }
                            }
                        });
                    }
                });
        }
    });

    /**
     * Open a copy of an application in the wizard as a new application: named "Copy of" the original, without its
     * numeric id, with an empty service id to fill in, and with a new client id and secret where it has them.
     *
     * @param serviceId the numeric id of the application to duplicate
     */
    function duplicateService(serviceId) {
        if (CasActuatorEndpoints.registeredServices()) {
            $.get(`${CasActuatorEndpoints.registeredServices()}/${serviceId}`, response => {
                const clone = structuredClone(response);
                delete clone.id;
                clone.name = `Copy of ${response.name ?? serviceId}`;
                clone.serviceId = "";
                if (Object.hasOwn(clone, "clientId")) {
                    clone.clientId = generateRandom(24);
                }
                if (Object.hasOwn(clone, "clientSecret") || Object.hasOwn(clone, "clientSecrets")) {
                    delete clone.clientSecrets;
                    delete clone.clientSecretExpiration;
                    clone.clientSecret = generateRandom(48);
                }
                PalantirServiceEditor.openApplicationInWizard(clone, {duplicate: true, original: response.name ?? String(serviceId)});
            }).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
            });
        }
    }

    const applicationsTable = $("#applicationsTable").DataTable();
    initializeDataTableContextMenu({
        table: applicationsTable,
        selector: "#applicationsTable tbody tr",
        items: {
            editPlain: {name: "Edit in Editor", icon: contextMenuIcon("mdi-pencil")},
            editWizard: {name: "Edit in Wizard", icon: contextMenuIcon("mdi-wizard-hat")},
            duplicate: {name: "Duplicate Application", write: "registeredServices", icon: contextMenuIcon("mdi-content-duplicate")},
            disable: {
                name: "Disable Application",
                write: "registeredServices",
                icon: contextMenuIcon("mdi-cancel"),
                visible: context => !PalantirApplicationAccess.isServiceDisabled(context.rowData.serviceId)
            },
            enable: {
                name: "Enable Application",
                write: "registeredServices",
                icon: contextMenuIcon("mdi-check-circle-outline"),
                visible: context => PalantirApplicationAccess.isServiceDisabled(context.rowData.serviceId)
            },
            delete: {name: "Delete Service Definition", write: "registeredServices", icon: contextMenuIcon("mdi-delete")},
            sep1: "---------",
            samlMetadataHealth: {
                name: "Check Metadata Health",
                icon: contextMenuIcon("mdi-heart-pulse"),
                visible: context => PalantirSamlMetadataHealth.applies(registeredServicesById.get(String(context.rowData.serviceId)))
            },
            releasePreview: {
                name: "Preview Released Attributes",
                icon: contextMenuIcon("mdi-account-eye"),
                visible: () => PalantirAttributeRelease.available()
            },
            history: {
                name: "View Change History",
                icon: contextMenuIcon("mdi-history"),
                visible: () => CasActuatorEndpoints.entityHistory()
            },
            changelog: {
                name: "View Change Log",
                icon: contextMenuIcon("mdi-delta"),
                visible: () => CasActuatorEndpoints.entityHistory()
            }
        },
        callback: (key, context) => applicationActionHandler(key, context)
    });

    applicationActionHandler = (key, context) => {
        const serviceId = context.rowData.serviceId;
        if (key === "editPlain") {
            editService(serviceId, false);
        } else if (key === "editWizard") {
            editService(serviceId, true);
        } else if (key === "duplicate") {
            duplicateService(serviceId);
        } else if (key === "disable" || key === "enable") {
            PalantirApplicationAccess.change(serviceId, key === "disable");
        } else if (key === "delete") {
            deleteService(context);
        } else if (key === "history") {
            viewEntityHistory(serviceId);
        } else if (key === "changelog") {
            viewEntityChangelog(serviceId);
        } else if (key === "samlMetadataHealth") {
            PalantirSamlMetadataHealth.open(serviceId);
        } else if (key === "releasePreview") {
            PalantirAttributeRelease.openForApplication(serviceId);
        }
    };
}

/**
 * Read the registered services into the applications table.
 *
 * @param callback called with the table once it holds the services
 * @returns {Promise<boolean>} resolved once the services are in the table, with true, or could not be read, with false
 */
async function fetchServices(callback) {
    if (CasActuatorEndpoints.registeredServices()) {
        return new Promise(resolve => $.get(CasActuatorEndpoints.registeredServices(), response => {
            let serviceCountByType = [0, 0, 0, 0, 0];
            let applicationsTable = $("#applicationsTable").DataTable();
            applicationsTable.clear();
            registeredServicesById.clear();

            for (const service of response[1]) {
                registeredServicesById.set(String(service.id), service);
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

                const name = escapePalantirMarkup(service.name ?? "");
                const links = [
                    applicationLink(service.informationUrl, "Information URL"),
                    applicationLink(service.privacyUrl, "Privacy URL")
                ].filter(Boolean);
                let serviceDetails = `<span serviceId="${service.id}" title="${name}">${name}</span>`;
                if (PalantirApplicationAccess.isDisabled(service.accessStrategy)) {
                    serviceDetails += " <span class=\"palantir-status-tag is-disabled\"><i class=\"mdi mdi-cancel\" aria-hidden=\"true\"></i>Disabled</span>";
                }
                if (links.length > 0) {
                    serviceDetails += `<div class="palantir-application-links">${links.join("")}</div>`;
                }

                let serviceIdDetails = `<span serviceId="${service.id}" class="text-wrap">${escapePalantirMarkup(service.serviceId ?? "")}</span>`;
                const metadataLink = serviceClass.includes("SamlRegisteredService")
                    ? samlMetadataLocationMarkup(service.metadataLocation)
                    : null;
                if (metadataLink) {
                    serviceIdDetails += `<div class="palantir-application-links">${metadataLink}</div>`;
                }

                applicationsTable.row.add({
                    0: `${PalantirApplicationSelection.checkbox(service.id, service.name ?? service.id)}<i serviceId='${service.id}' title='${serviceClass}' class='mdi ${icon}'></i>`,
                    1: `${serviceDetails}`,
                    2: `${serviceIdDetails}`,
                    3: `<span serviceId='${service.id}' class="text-wrap palantir-application-description">${renderPalantirDescription(service.description)}</span>`,
                    4: `${service.id}`,
                    serviceId: service.id
                });
            }

            applicationsTable.off("draw.palantirServiceButtons").on("draw.palantirServiceButtons", async () => {
                await initializeServiceButtons();
            });

            updateApplicationTypeFilter();
            applicationsTable.draw();
            prepareRegisteredServiceWizardWhenIdle();

            $("#servicesChartTitle").closest(".palantir-chart-card").prop("hidden", registeredServicesById.size === 0);
            if (servicesChart) {
                servicesChart.data.datasets[0].data = serviceCountByType;
                servicesChart.update();
            }

            if (callback !== undefined) {
                callback(applicationsTable);
            }
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            $("#servicesChartTitle").closest(".palantir-chart-card").prop("hidden", registeredServicesById.size === 0);
            displayBanner(xhr);
        }).done(() => resolve(true)).fail(() => resolve(false)));
    } else {
        $("#servicesChartTitle").closest(".palantir-chart-card").prop("hidden", true);
    }
}

async function initializeFooterButtons() {
    $("button[name=cancelServiceWizard]").off().on("click", () => {
        const element = $("#editServiceWizardDialog");
        if (element.hasClass("ui-dialog-content")) {
            element.dialog("close");
        }
    });

    $("button[name=cancelService]").off().on("click", () => {
        const element = $("#editServiceDialog");
        if (element.hasClass("ui-dialog-content")) {
            element.dialog("close");
        }
    });

    $("button[name=fillServiceWizardFromMetadata]").off().on("click", () => PalantirSamlMetadataImport.open());

    $("#saveServiceWizardHint").off("click").on("click", () => PalantirWizardValidity.report());
    let wizardValidityTimer = null;
    $("#editServiceWizardDialog").off("input.palantirValidity change.palantirValidity")
        .on("input.palantirValidity change.palantirValidity", "input, select, textarea", () => {
            clearTimeout(wizardValidityTimer);
            wizardValidityTimer = setTimeout(() => PalantirWizardValidity.refresh(), 150);
        });
    $("button[name=copyServiceDefinitionWizard]").off().on("click", () => {
        const editor = initializeAceEditor("wizardServiceEditor");
        copyToClipboard(editor.getValue());
    });

    $("button[name=validateServiceWizard]").off().on("click", () => {
        let valid = true;
        const originalKey = RegisteredServiceSections.currentKey();
        const wasReviewing = RegisteredServiceSections.reviewing;
        RegisteredServiceSections.eachAvailableSection(section => {
            $(section.panel).find("input:visible").each(function () {
                const input = $(this);
                valid = input.get(0).checkValidity();
                if (!valid) {
                    input.get(0).reportValidity();
                    return false;
                }
            });
            return valid;
        });
        if (valid) {
            if (RegisteredServiceSections.currentKey() !== originalKey) {
                RegisteredServiceSections.activate(originalKey);
            }
            RegisteredServiceSections.setReviewing(wasReviewing);
            RegisteredServiceSections.render();
            if (CasActuatorEndpoints.registeredServices()) {
                const editor = initializeAceEditor("wizardServiceEditor");
                $.ajax({
                    url: `${CasActuatorEndpoints.registeredServices()}/validate`,
                    type: "POST",
                    contentType: "application/json",
                    data: editor.getValue(),
                    success: response => {
                        if (response === false || response === "false") {
                            Swal.fire({icon: "error", title: "The application definition is not valid", text: "CAS cannot read this definition."});
                            return;
                        }
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
                        showUnreadableDefinition(xhr, "The application definition is not valid");
                    }
                });
            }
        }
    });

    $("button[name=newServiceWizard]").off().on("click", () => {
        openRegisteredServiceWizardDialog();
    });

    $("button[name=newServicePlain]").off().on("click", () => {
        if (CasActuatorEndpoints.registeredServices()) {
            const editServiceDialogElement = document.getElementById("editServiceDialog");
            const editor = PalantirServiceEditor.editor();
            editor.setValue("");
            editor.gotoLine(1);

            $(editServiceDialogElement).attr("newService", true);
            getEditServiceDialogInstance()["open"]();
        }
    });

    $("#serviceFileInput")
        .attr("accept", ".json,.yaml,.yml,.zip")
        .off("change.import")
        .on("change.import", event => {
            const file = event.target.files[0];
            $(event.target).val("");
            if (file) {
                importServiceFile(file);
            }
        });

    $("button[name=importService]").off().on("click", () => {
        if (CasActuatorEndpoints.registeredServices()) {
            $("#serviceFileInput").trigger("click");
        }
    });

    $("button[name=exportService]").off().on("click", () => {
        if (CasActuatorEndpoints.registeredServices()) {
            let serviceId = $(exportServiceButton).attr("serviceId");
            fetch(`${CasActuatorEndpoints.registeredServices()}/export/${serviceId}`, {
                credentials: 'include'
            })
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
        if (CasActuatorEndpoints.registeredServices()) {
            fetch(`${CasActuatorEndpoints.registeredServices()}/export`, {
                credentials: 'include'
            })
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

let applicationActionHandler = null;

const registeredServicesById = new Map();

const APPLICATIONS_VIEW_STORAGE_KEY = "PalantirApplicationsView";

const APPLICATIONS_TYPE_STORAGE_KEY = "PalantirApplicationsType";

const APPLICATION_TYPES = [
    {key: "cas", label: "CAS"},
    {key: "saml", label: "SAML2"},
    {key: "oauth", label: "OAuth"},
    {key: "oidc", label: "OpenID Connect"},
    {key: "wsfed", label: "WS-Federation"},
    {key: "other", label: "Other"}
];

let applicationTypeFilter = storedApplicationTypeFilter();

let applicationCardsSelection = "";

/**
 * Mark the one row of a registered service in the applications table and open the page it is on. When the search or
 * the type filter hides it, both are cleared first.
 *
 * @param serviceId the numeric id of the registered service
 * @returns {boolean} true when the service is in the table
 */
function revealApplicationRow(serviceId) {
    const table = $("#applicationsTable").DataTable();
    const index = table.rows((_, data) => String(data.serviceId) === serviceId).indexes()[0];
    if (index === undefined) {
        return false;
    }
    const position = () => table.rows({search: "applied", order: "applied"}).indexes().toArray().indexOf(index);
    if (position() < 0) {
        table.search("");
        $("#applications-tab .dt-search input").val("");
        if (applicationTypeFilter === "all") {
            table.draw();
        } else {
            setApplicationTypeFilter("all");
        }
    }
    const length = table.page.len();
    table.page(length > 0 ? Math.floor(Math.max(position(), 0) / length) : 0).draw(false);
    table.rows(".selected").nodes().to$().removeClass("selected");
    const node = table.row(index).node();
    node?.classList.add("selected");
    node?.scrollIntoView({block: "center"});
    return true;
}

/**
 * The ids of the selected rows on the table's current page, as one string.
 *
 * @returns {string} the selected service ids, comma separated
 */
function selectedApplicationRows() {
    if (!$.fn.dataTable.isDataTable("#applicationsTable")) {
        return "";
    }
    const table = $("#applicationsTable").DataTable();
    return table.rows({search: "applied", page: "current"}).indexes().toArray()
        .filter(index => table.row(index).node()?.classList.contains("selected"))
        .map(index => table.row(index).data().serviceId)
        .join(",");
}

/**
 * Escape text for insertion into markup built as a string.
 *
 * @param value the text to escape
 * @returns {string} the escaped text
 */
/**
 * A link from an application's row to one of its pages, for web addresses only.
 *
 * @param url the address
 * @param label the link text
 * @returns {string|null} the link, or null when there is no web address
 */
function applicationLink(url, label) {
    if (typeof url !== "string" || !/^https?:\/\//i.test(url.trim())) {
        return null;
    }
    return `<a target="_blank" rel="noopener noreferrer" href="${escapePalantirMarkup(url.trim())}">${escapePalantirMarkup(label)}</a>`;
}

const SAML_METADATA_SOURCES = {
    mongodb: "MongoDB",
    jdbc: "JDBC",
    git: "Git",
    redis: "Redis",
    dynamodb: "DynamoDB",
    awss3: "Amazon S3",
    rest: "REST",
    json: "JSON",
    file: "File",
    classpath: "Classpath"
};

/**
 * Show where a SAML2 application reads its metadata from. A web address is a link to the metadata; a metadata
 * manager source such as <code>mongodb://</code> is tagged with its kind and links to the metadata management
 * section under Protocols; a file or classpath location is tagged with its kind.
 *
 * @param location the metadata location
 * @returns {string|null} the markup, or null when there is no location
 */
function samlMetadataLocationMarkup(location) {
    const value = typeof location === "string" ? location.trim() : "";
    if (value === "") {
        return null;
    }
    const link = applicationLink(value, "Metadata Location");
    if (link) {
        return link;
    }
    const scheme = (value.match(/^([a-z][a-z0-9+.-]*):/i)?.[1] ?? "").toLowerCase();
    const kind = SAML_METADATA_SOURCES[scheme] ?? (scheme === "" ? "Location" : scheme.toUpperCase());
    const tag = `<span class="palantir-tag palantir-metadata-source" title="${escapePalantirMarkup(value)}">`
        + `<i class="mdi mdi-database-outline" aria-hidden="true"></i>${escapePalantirMarkup(kind)}</span>`;
    if (!value.startsWith(`${scheme}://`) || !CasActuatorEndpoints.samlIdpRegisteredServiceMetadata() || $("#saml2protocol").length === 0) {
        return tag;
    }
    return `${tag}<a href="#" data-saml-metadata-manager="${escapePalantirMarkup(`${scheme}://`)}"
        title="Open the ${escapePalantirMarkup(kind)} metadata manager under Protocols">Metadata Location</a>`;
}

function escapePalantirMarkup(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
    })[character]);
}

/**
 * Render a registered service description, which the wizard's editor keeps as HTML, as safe markup: paragraphs, line
 * breaks and bold, italic and underlined text are kept without attributes, scripts and styles are dropped, and every
 * other element is replaced by its text, so a description from metadata or dynamic registration cannot inject markup.
 *
 * @param value the description
 * @returns {string} the markup to show
 */
function renderPalantirDescription(value) {
    const allowed = new Set(["P", "BR", "B", "STRONG", "I", "EM", "U"]);
    const dropped = new Set(["SCRIPT", "STYLE", "TEMPLATE", "NOSCRIPT"]);
    const copy = (from, to) => from.childNodes.forEach(node => {
        if (node.nodeType === Node.TEXT_NODE) {
            to.append(node.textContent);
        } else if (node.nodeType === Node.ELEMENT_NODE && !dropped.has(node.tagName)) {
            if (allowed.has(node.tagName)) {
                const element = document.createElement(node.tagName.toLowerCase());
                copy(node, element);
                to.append(element);
            } else {
                copy(node, to);
            }
        }
    });
    const target = document.createElement("div");
    copy(new DOMParser().parseFromString(String(value ?? ""), "text/html").body, target);
    return target.innerHTML;
}

/**
 * The text of a registered service description, without its markup, for search and tooltips.
 *
 * @param value the description
 * @returns {string} the text
 */
function describePalantirDescription(value) {
    const body = new DOMParser().parseFromString(String(value ?? ""), "text/html").body;
    body.querySelectorAll("script, style, template, noscript").forEach(element => element.remove());
    body.querySelectorAll("p, br").forEach(element => element.after(" "));
    return body.textContent.replace(/\s+/g, " ").trim();
}

/**
 * Describe the protocol of a registered service class for badges and summaries.
 *
 * @param serviceClass the fully qualified registered service class
 * @returns {{key: string, short: string, label: string}} the badge key, its short text and a readable name
 */
function applicationProtocolOf(serviceClass) {
    const friendly = PalantirDashboardConfiguration.supportedServiceTypes()[serviceClass];
    const name = String(serviceClass ?? "");
    const protocols = [
        {match: "CasRegisteredService", key: "cas", short: "CAS", label: "CAS"},
        {match: "SamlRegisteredService", key: "saml", short: "SAML", label: "SAML2 Service Provider"},
        {match: "OidcRegisteredService", key: "oidc", short: "OIDC", label: "OpenID Connect Relying Party"},
        {match: "OAuthRegisteredService", key: "oauth", short: "OAuth", label: "OAuth Client"},
        {match: "WSFederationRegisteredService", key: "wsfed", short: "WS", label: "WS-Federation Relying Party"}
    ];
    const protocol = protocols.find(entry => name.includes(entry.match));
    if (protocol) {
        return {key: protocol.key, short: protocol.short, label: friendly || protocol.label};
    }
    return {key: "other", short: name.split(".").pop().replace("RegisteredService", "").slice(0, 5) || "App", label: friendly || name.split(".").pop()};
}

/**
 * The view the operator last chose for the applications: cards or table.
 *
 * @returns {string} cards or table
 */
function applicationsView() {
    try {
        return window.localStorage.getItem(APPLICATIONS_VIEW_STORAGE_KEY) === "table" ? "table" : "cards";
    } catch (error) {
        return "cards";
    }
}

/**
 * Show the applications as cards or as the table, and remember the choice.
 *
 * @param view cards or table
 */
function showApplicationsView(view) {
    const cards = view !== "table";
    try {
        window.localStorage.setItem(APPLICATIONS_VIEW_STORAGE_KEY, cards ? "cards" : "table");
    } catch (error) {
        console.debug("Palantir could not persist the applications view", error);
    }
    $("#applications-tab").toggleClass("is-cards-view", cards);
    $("#applicationsCardsView").attr("aria-pressed", String(cards));
    $("#applicationsTableView").attr("aria-pressed", String(!cards));
    if (cards) {
        renderApplicationCards();
    } else if ($.fn.dataTable.isDataTable("#applicationsTable")) {
        $("#applicationsTable").DataTable().columns.adjust();
    }
}

/**
 * Summarize the registry above the applications: how many there are and of how many types.
 *
 * The rows of the table are counted, so a definition deleted from the table drops out at once.
 */
function updateApplicationsSummary() {
    const table = $.fn.dataTable.isDataTable("#applicationsTable") ? $("#applicationsTable").DataTable() : null;
    const services = table
        ? table.rows().data().toArray().map(row => registeredServicesById.get(String(row.serviceId)) ?? {})
        : [...registeredServicesById.values()];
    const types = new Set(services.map(service => applicationProtocolOf(service["@class"]).label));
    const count = services.length;
    const text = count === 0
        ? "No applications are registered yet."
        : `${count} application${count === 1 ? "" : "s"} · ${types.size} type${types.size === 1 ? "" : "s"}`;
    const summary = $("#applicationsSummary");
    if (summary.text() !== text) {
        summary.text(text);
    }
}

/**
 * The type of applications the operator last chose to show, or all.
 *
 * @returns {string} a type key, or all
 */
function storedApplicationTypeFilter() {
    try {
        return window.localStorage.getItem(APPLICATIONS_TYPE_STORAGE_KEY) || "all";
    } catch (error) {
        return "all";
    }
}

/**
 * Show only the applications of one type, or all of them, and remember the choice.
 *
 * The table filters its rows, so its paging and the card view drawn from it follow.
 *
 * @param type a type key, or all
 */
function setApplicationTypeFilter(type) {
    applicationTypeFilter = type;
    try {
        window.localStorage.setItem(APPLICATIONS_TYPE_STORAGE_KEY, type);
    } catch (error) {
        console.debug("Palantir could not persist the applications type filter", error);
    }
    $("#applicationsTypeFilter button[data-application-type]").each(function () {
        $(this).attr("aria-pressed", String($(this).data("application-type") === type));
    });
    if ($.fn.dataTable.isDataTable("#applicationsTable")) {
        $("#applicationsTable").DataTable().draw();
    }
}

/**
 * List the types of the registered applications, with how many of each, as the choices of the type filter.
 *
 * The filter is hidden while the registry holds a single type, and a chosen type that no application has any more
 * falls back to all.
 */
function updateApplicationTypeFilter() {
    const counts = new Map();
    for (const service of registeredServicesById.values()) {
        const key = applicationProtocolOf(service["@class"]).key;
        counts.set(key, (counts.get(key) ?? 0) + 1);
    }
    if (applicationTypeFilter !== "all" && !counts.has(applicationTypeFilter)) {
        applicationTypeFilter = "all";
    }
    const choices = [{key: "all", label: "All", count: registeredServicesById.size}]
        .concat(APPLICATION_TYPES.filter(type => counts.has(type.key)).map(type => ({...type, count: counts.get(type.key)})));
    $("#applicationsTypeFilter")
        .html(choices.map(choice => `
            <button type="button" data-application-type="${choice.key}" aria-pressed="${choice.key === applicationTypeFilter}">
                ${choice.key === "all" ? "" : `<span class="palantir-type-filter__dot is-${choice.key}" aria-hidden="true"></span>`}
                ${escapePalantirMarkup(choice.label)}<span class="palantir-type-filter__count">${choice.count}</span>
            </button>`).join(""))
        .prop("hidden", counts.size < 2);
}

/**
 * Wire the type filter: a search function that keeps the applications table to the chosen type, and its buttons.
 */
function initializeApplicationTypeFilter() {
    if (!$.fn.dataTable.ext.search.some(search => search.palantirApplicationType)) {
        const search = (settings, data, index, rowData) => applicationTypeFilter === "all"
            || new $.fn.dataTable.Api(settings).table().node().id !== "applicationsTable"
            || applicationProtocolOf(registeredServicesById.get(String(rowData?.serviceId))?.["@class"]).key === applicationTypeFilter;
        search.palantirApplicationType = true;
        $.fn.dataTable.ext.search.push(search);
    }
    $("#applicationsTypeFilter").off("click").on("click", "button[data-application-type]", event => {
        setApplicationTypeFilter(String($(event.currentTarget).data("application-type")));
    });
}

/**
 * Draw the card view from the rows the applications table currently shows.
 *
 * The table stays the source: its search, paging and the selected row decide which cards appear
 * and which one is marked, so both views always agree.
 */
function renderApplicationCards() {
    const container = $("#applicationsCards");
    if (container.length === 0 || !$.fn.dataTable.isDataTable("#applicationsTable")) {
        return;
    }
    const table = $("#applicationsTable").DataTable();
    applicationCardsSelection = selectedApplicationRows();
    const indexes = table.rows({search: "applied", page: "current"}).indexes().toArray();
    const cards = indexes.map(index => {
        const rowData = table.row(index).data();
        const node = table.row(index).node();
        const service = registeredServicesById.get(String(rowData.serviceId)) || {};
        const protocol = applicationProtocolOf(service["@class"]);
        const name = service.name ?? String(rowData.serviceId);
        const links = [];
        if (service.informationUrl) {
            links.push(`<a href="${escapePalantirMarkup(service.informationUrl)}" target="_blank" rel="noopener">Information URL</a>`);
        }
        if (service.privacyUrl) {
            links.push(`<a href="${escapePalantirMarkup(service.privacyUrl)}" target="_blank" rel="noopener">Privacy URL</a>`);
        }
        const metadataLocation = samlMetadataLocationMarkup(service.metadataLocation);
        if (metadataLocation) {
            links.push(metadataLocation);
        }
        const tags = [];
        if (service.evaluationOrder !== undefined && service.evaluationOrder !== null) {
            tags.push(`<span class="palantir-tag">Order ${escapePalantirMarkup(service.evaluationOrder)}</span>`);
        }
        if (service.clientId) {
            tags.push(`<span class="palantir-tag">Client ${escapePalantirMarkup(service.clientId)}</span>`);
        }
        const selected = node && node.classList.contains("selected");
        const disabled = PalantirApplicationAccess.isDisabled(service.accessStrategy);
        if (disabled) {
            tags.unshift("<span class=\"palantir-status-tag is-disabled\"><i class=\"mdi mdi-cancel\" aria-hidden=\"true\"></i>Disabled</span>");
        }
        const checked = PalantirApplicationSelection.has(rowData.serviceId);
        return `
            <article class="palantir-application-card${selected ? " is-selected" : ""}${disabled ? " is-disabled" : ""}${checked ? " is-checked" : ""}" data-service-id="${escapePalantirMarkup(rowData.serviceId)}">
                <header class="palantir-application-card-header">
                    ${PalantirApplicationSelection.checkbox(rowData.serviceId, name)}
                    <span class="palantir-protocol-badge is-${protocol.key}" title="${escapePalantirMarkup(service["@class"])}">${escapePalantirMarkup(protocol.short)}</span>
                    <div class="palantir-application-card-title">
                        <h4>${escapePalantirMarkup(name)}</h4>
                        <p>${escapePalantirMarkup(protocol.label)} · ID ${escapePalantirMarkup(rowData.serviceId)}</p>
                    </div>
                    <button type="button" class="palantir-icon-button" data-application-action="menu"
                            aria-label="More actions for ${escapePalantirMarkup(name)}" title="More actions">
                        <i class="mdi mdi-dots-horizontal" aria-hidden="true"></i>
                    </button>
                </header>
                <code class="palantir-application-card-pattern" title="${escapePalantirMarkup(service.serviceId)}">${escapePalantirMarkup(service.serviceId)}</code>
                ${describePalantirDescription(service.description) ? `<div class="palantir-application-card-description">${renderPalantirDescription(service.description)}</div>` : ""}
                ${tags.length > 0 || links.length > 0 ? `<div class="palantir-application-card-tags">${tags.join("")}${links.join("")}</div>` : ""}
                <footer class="palantir-application-card-actions">
                    <button type="button" class="palantir-button is-primary" data-application-action="editWizard">
                        <i class="mdi mdi-wizard-hat" aria-hidden="true"></i>Open in wizard
                    </button>
                    <button type="button" class="palantir-button" data-application-action="editPlain">
                        <i class="mdi mdi-code-json" aria-hidden="true"></i>JSON
                    </button>
                </footer>
            </article>`;
    });
    if (cards.length === 0) {
        const searching = table.search() !== "";
        const filtered = applicationTypeFilter !== "all";
        const message = searching || filtered
            ? `No application matches ${searching && filtered ? "that search and type" : searching ? "that search" : "that type"}.`
            : "No applications are registered yet.";
        container.html(`<p class="palantir-empty-state">${message}</p>`);
    } else {
        container.html(cards.join(""));
    }
}

/**
 * Find the table row behind a card, in the shape the row context menu hands its callback.
 *
 * @param serviceId the registered service id the card shows
 * @returns {object|null} the row, its data and its node, or null when the row is not drawn
 */
function applicationRowContext(serviceId) {
    const table = $("#applicationsTable").DataTable();
    const index = table.rows().indexes().toArray()
        .find(rowIndex => String(table.row(rowIndex).data().serviceId) === String(serviceId));
    if (index === undefined) {
        return null;
    }
    const row = table.row(index);
    return {row: row, rowData: row.data(), $row: $(row.node())};
}

/**
 * Wire the card view: the view switch, the card buttons, and redrawing whenever the table redraws or
 * the selected row changes. Other class changes on the rows, such as the highlight a context menu
 * puts on its row, leave the cards alone, so a card keeps focus while its menu is open.
 */
function initializeApplicationCards() {
    const tableRow = $("#applicationsTable").closest(".dt-layout-row");
    if (tableRow.length > 0) {
        $("#applicationsCards").insertAfter(tableRow);
    }
    $(document)
        .off("click.palantirSamlMetadataManager")
        .on("click.palantirSamlMetadataManager", "[data-saml-metadata-manager]", event => {
            event.preventDefault();
            showSamlMetadataManager(String($(event.currentTarget).data("saml-metadata-manager")));
        });
    $("#applicationsCardsView, #applicationsTableView").off("click").on("click", event => {
        showApplicationsView($(event.currentTarget).data("applications-view"));
    });

    $("#applicationsCards").off("click").on("click", "[data-application-action]", event => {
        const button = event.currentTarget;
        const card = $(button).closest(".palantir-application-card");
        const context = applicationRowContext(card.data("service-id"));
        if (!context) {
            return;
        }
        const action = $(button).data("application-action");
        if (action === "menu") {
            const bounds = button.getBoundingClientRect();
            context.$row.contextMenu({x: bounds.left + window.scrollX, y: bounds.bottom + window.scrollY});
        } else if (applicationActionHandler) {
            applicationActionHandler(action, context);
        }
        event.preventDefault();
    });

    const table = $("#applicationsTable").DataTable();
    table.off("draw.palantirCards").on("draw.palantirCards", () => {
        updateApplicationsSummary();
        renderApplicationCards();
    });

    const body = document.querySelector("#applicationsTable tbody");
    if (body && !body.dataset.cardsObserved) {
        body.dataset.cardsObserved = "true";
        let scheduled = false;
        new MutationObserver(() => {
            if (!scheduled) {
                scheduled = true;
                requestAnimationFrame(() => {
                    scheduled = false;
                    if ($("#applications-tab").hasClass("is-cards-view")
                        && selectedApplicationRows() !== applicationCardsSelection) {
                        renderApplicationCards();
                    }
                });
            }
        }).observe(body, {subtree: true, attributes: true, attributeFilter: ["class"]});
    }
    showApplicationsView(applicationsView());
}
