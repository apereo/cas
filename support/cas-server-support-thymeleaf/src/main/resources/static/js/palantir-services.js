async function initializeServicesOperations() {
    const columnDefinitions = [
        {visible: true, targets: 0},
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

    initializeApplicationCards();
    await fetchServices();
    await initializeFooterButtons();

    let serviceDefinitionsEntries = "";
    for (let type in PalantirDashboardConfiguration.serviceDefinitions()) {
        serviceDefinitionsEntries += `<h3>${type} Templates</h3>`;
        const entries = PalantirDashboardConfiguration.serviceDefinitions()[type];
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

        const entries = PalantirDashboardConfiguration.serviceDefinitions()[type];
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

let serviceEditorInstance = null;
let editServiceDialogInstance = null;
let entityHistoryDialogInstance = null;

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
                close: () => $(window).off("resize.serviceEditor")
            });
        }
        editServiceDialogInstance = {
            open: () => {
                element.dialog("open");
                sizeServiceEditorDialog();
                $(window)
                    .off("resize.serviceEditor")
                    .on("resize.serviceEditor", sizeServiceEditorDialog);
            },
            close: () => element.dialog("close")
        };
    }
    return editServiceDialogInstance;
}

function getEntityHistoryDialogInstance() {
    if (!entityHistoryDialogInstance) {
        entityHistoryDialogInstance = window.mdc.dialog.MDCDialog.attachTo(document.getElementById("viewEntityHistoryDialog"));
    }
    return entityHistoryDialogInstance;
}

async function initializeServiceButtons() {
    getEditServiceDialogInstance();
    serviceEditorInstance = initializeAceEditor("serviceEditor");

    function viewEntityHistory(serviceId) {
        const entityHistoryTable = $("#entityHistoryTable").DataTable();
        $.get(`${CasActuatorEndpoints.entityHistory()}/registeredServices/${serviceId}`, response => {
            entityHistoryTable.clear();

            const editor = initializeAceEditor("entityHistoryEditor", "json");
            editor.setValue("");
            editor.setReadOnly(true);

            for (const item of response) {
                entityHistoryTable.row.add({
                    0: `<code>${item.id}</code>`,
                    1: `<code>${item.date}</code>`,
                    2: `${JSON.stringify(item.entity, null, 4)}`,
                    serviceId: serviceId,
                    revisionId: item.id
                });
            }

            entityHistoryTable.draw();
            entityHistoryTable.off("click", "tbody tr").on("click", "tbody tr", function () {
                let data = entityHistoryTable.row(this).data();
                editor.setValue(data[2]);
                editor.gotoLine(1);
                editor.setReadOnly(true);
            });

            if (response.length > 0) {
                getEntityHistoryDialogInstance()["open"]();
            } else {
                Swal.fire("No History!", "There are no changes recorded for this application definition.", "info");
            }

        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    initializeDataTableContextMenu({
        table: $("#entityHistoryTable").DataTable(),
        selector: "#entityHistoryTable tbody tr",
        items: {
            restore: {name: "Restore Version", icon: contextMenuIcon("mdi-history")}
        },
        callback: (key, context) => {
            if (key === "restore") {
                const {serviceId, revisionId} = context.rowData;
                Swal.fire({
                    title: "Restore this version?",
                    text: `Version ${revisionId} will replace the current definition of service ${serviceId}.`,
                    icon: "question",
                    showCancelButton: true,
                    confirmButtonText: "Restore",
                    showLoaderOnConfirm: true,
                    allowOutsideClick: () => !Swal.isLoading(),
                    preConfirm: () => $.ajax({
                        url: `${CasActuatorEndpoints.entityHistory()}/registeredServices/${encodeURIComponent(serviceId)}/restore/${encodeURIComponent(revisionId)}`,
                        type: "POST",
                        headers: {"Accept": "application/json"}
                    }).catch(xhr => {
                        console.error("Error restoring service:", xhr);
                        Swal.showValidationMessage("Unable to restore this version. Please try again or check your access to service history.");
                    })
                }).then(result => {
                    if (result.isConfirmed) {
                        getEntityHistoryDialogInstance()["close"]();
                        fetchServices(() => updateNavigationSidebar());
                        Swal.fire("Service Restored", `Version ${revisionId} is now the current definition of service ${serviceId}.`, "success");
                    }
                });
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
                            success: response => {
                                let applicationsTable = $("#applicationsTable").DataTable();
                                applicationsTable.row(context.$row).remove().draw();
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
                    openRegisteredServiceWizardDialog(response);
                    return;
                }
                const value = JSON.stringify(response, null, 4);
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

    $("button[name=saveService],button[name=saveServiceWizard]").off().on("click", function () {
        if (CasActuatorEndpoints.registeredServices()) {
            let isNewService = false;
            let value = "";
            let dialogInstance = null;

            const saveButton = $(this);
            switch (saveButton.attr("name")) {
            case "saveServiceWizard":
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
                            url: `${CasActuatorEndpoints.registeredServices()}`,
                            type: isNewService ? "POST" : "PUT",
                            contentType: "application/json",
                            data: value,
                            success: async response => {
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
                                displayBanner(xhr);
                            }
                        });
                    }
                });
        }
    });

    function copyService(serviceId) {
        if (CasActuatorEndpoints.registeredServices()) {
            $.get(`${CasActuatorEndpoints.registeredServices()}/${serviceId}`, response => {
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
                serviceEditorInstance.setValue(value, -1);
                serviceEditorInstance.gotoLine(1);
                serviceEditorInstance.findAll("...", {regExp: false});

                const editServiceDialogElement = document.getElementById("editServiceDialog");
                $(editServiceDialogElement).attr("newService", true);
                getEditServiceDialogInstance()["open"]();
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
            copy: {name: "Copy Service Definition", icon: contextMenuIcon("mdi-content-copy")},
            delete: {name: "Delete Service Definition", icon: contextMenuIcon("mdi-delete")},
            sep1: "---------",
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
        } else if (key === "copy") {
            copyService(serviceId);
        } else if (key === "delete") {
            deleteService(context);
        } else if (key === "history") {
            viewEntityHistory(serviceId);
        } else if (key === "changelog") {
            viewEntityChangelog(serviceId);
        }
    };
}

async function fetchServices(callback) {
    if (CasActuatorEndpoints.registeredServices()) {
        $.get(CasActuatorEndpoints.registeredServices(), response => {
            let serviceCountByType = [0, 0, 0, 0, 0];
            let applicationsTable = $("#applicationsTable").DataTable();
            applicationsTable.clear();
            registeredServicesById.clear();
            let metadataSourcesCount = 0;

            let saml2MetadataProvidersTable = $("#saml2MetadataProvidersTable").DataTable();
            saml2MetadataProvidersTable.clear();

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

                applicationsTable.row.add({
                    0: `<i serviceId='${service.id}' title='${serviceClass}' class='mdi ${icon}'></i>`,
                    1: `${serviceDetails}`,
                    2: `${serviceIdDetails}`,
                    3: `<span serviceId='${service.id}' class="text-wrap">${service.description ?? ""}</span>`,
                    4: `${service.id}`,
                    serviceId: service.id
                });

                if (serviceClass.includes("SamlRegisteredService")) {
                    const metadataLocation = service.metadataLocation;
                    saml2MetadataProvidersTable.row.add({
                        0: `<span serviceId='${service.id}' class="text-wrap">${service.id ?? ""}</span>`,
                        1: `<span serviceId='${service.id}' class="text-wrap">${service.name ?? ""}</span>`,
                        2: metadataLocation
                    });
                    metadataSourcesCount++;
                }
            }
            const metadataSourcesTab = $("#saml2metadataproviders").parent().add("#saml2metadataproviders");
            if (metadataSourcesCount > 0) {
                showElements(metadataSourcesTab);
            } else {
                hideElements(metadataSourcesTab);
            }

            applicationsTable.on("draw", async () => {
                await initializeServiceButtons();
            });

            applicationsTable.search("").draw();
            saml2MetadataProvidersTable.search("").draw();

            if (servicesChart) {
                servicesChart.data.datasets[0].data = serviceCountByType;
                servicesChart.update();
            }

            if (callback !== undefined) {
                callback(applicationsTable);
            }
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
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
        if (CasActuatorEndpoints.registeredServices()) {
            const editServiceDialogElement = document.getElementById("editServiceDialog");
            const editor = initializeAceEditor("serviceEditor", "json");
            editor.setValue("");
            editor.gotoLine(1);

            $(editServiceDialogElement).attr("newService", true);
            getEditServiceDialogInstance()["open"]();
        }
    });

    $("button[name=importService]").off().on("click", () => {
        if (CasActuatorEndpoints.registeredServices()) {
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
                                    url: `${CasActuatorEndpoints.registeredServices()}`,
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

let applicationCardsSelection = "";

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
function escapePalantirMarkup(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
    })[character]);
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
        if (service.metadataLocation) {
            links.push(`<a href="${escapePalantirMarkup(service.metadataLocation)}" target="_blank" rel="noopener">Metadata Location</a>`);
        }
        const tags = [];
        if (service.evaluationOrder !== undefined && service.evaluationOrder !== null) {
            tags.push(`<span class="palantir-tag">Order ${escapePalantirMarkup(service.evaluationOrder)}</span>`);
        }
        if (service.clientId) {
            tags.push(`<span class="palantir-tag">Client ${escapePalantirMarkup(service.clientId)}</span>`);
        }
        const selected = node && node.classList.contains("selected");
        return `
            <article class="palantir-application-card${selected ? " is-selected" : ""}" data-service-id="${escapePalantirMarkup(rowData.serviceId)}">
                <header class="palantir-application-card-header">
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
                ${service.description ? `<p class="palantir-application-card-description">${escapePalantirMarkup(service.description)}</p>` : ""}
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
        container.html(`<p class="palantir-empty-state">${searching ? "No application matches that search." : "No applications are registered yet."}</p>`);
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
    $("#applicationsCardsView, #applicationsTableView").off("click").on("click", event => {
        showApplicationsView($(event.currentTarget).data("applications-view"));
    });

    $("#applications-tabs").off("tabsactivate.palantirCards").on("tabsactivate.palantirCards", (event, ui) => {
        $(".palantir-view-switch").toggleClass("d-none", !ui.newPanel.is("#applications-tab"));
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
